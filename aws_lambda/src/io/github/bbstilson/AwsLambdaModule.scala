package io.github.bbstilson

import io.github.bbstilson.model.LambdaConfig

import mill._
import mill.scalalib._
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model._
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.time.Instant
import scala.util.{ Failure, Success, Try }
import scala.jdk.CollectionConverters._

trait AwsLambdaModule extends ScalaModule {

  import AwsLambdaModule._

  def s3Bucket: String
  def s3KeyPrefix: String
  def lambdaName: String
  def lambdaHandler: String

  def deployLambda = T {
    val config = LambdaConfig(
      s3Bucket,
      s3KeyPrefix,
      lambdaName,
      lambdaHandler
    )
    val jarPath: java.nio.file.Path = assembly().path.toNIO
    val putReq = mkPutReq(config.bucket, config.key)
    val updateCodeReq = mkUpdateCodeReq(config.name, config.bucket, config.key)

    // TODO: Figure out how to do progress listening in v2:
    // v1: https://docs.aws.amazon.com/AmazonS3/latest/dev/HLTrackProgressMPUJava.html
    val updateAttempt = for {
      s3 <- Try { S3Client.create }
      lambda <- Try { LambdaClient.create }
      _ <- Try { s3.putObject(putReq, jarPath) }
      updateResp <- Try { lambda.updateFunctionCode(updateCodeReq) }
      tagResp <- Try { lambda.tagResource(mkTagReq(updateResp.functionArn)) }
    } yield tagResp

    updateAttempt match {
      case Success(_)  => println(s"Succesfully deployed ${config.name}.")
      case Failure(ex) => println(formatException(ex))
    }
  }
}

object AwsLambdaModule {

  def mkPutReq(bucket: String, key: String): PutObjectRequest = {
    PutObjectRequest.builder
      .bucket(bucket)
      .key(key)
      .build
  }

  def mkUpdateCodeReq(
    name: String,
    bucket: String,
    key: String
  ): UpdateFunctionCodeRequest = {
    UpdateFunctionCodeRequest.builder
      .functionName(name)
      .s3Bucket(bucket)
      .s3Key(key)
      .build
  }

  def mkTagReq(arn: String): TagResourceRequest = {
    val tags = Map("deploy.timestamp" -> Instant.now.toString).asJava
    TagResourceRequest.builder
      .resource(arn)
      .tags(tags)
      .build
  }

  def formatException(t: Throwable): String = {
    val msg = Option(t.getLocalizedMessage).getOrElse(t.toString)
    s"$msg\n${t.getStackTrace.mkString("", "\n", "\n")}"
  }
}
