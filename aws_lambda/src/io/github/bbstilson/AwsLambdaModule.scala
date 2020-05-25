package io.github.bbstilson

import io.github.bbstilson.model._

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

  def awsRegion: Option[String] = None
  def s3Bucket: Option[String] = None
  def s3KeyPrefix: Option[String] = None
  def lambdaName: Option[String] = None // TODO: default to project name
  def lambdaRoleArn: Option[String] = None
  def lambdaHandlerName: Option[String] = None
  // def lambdaTimeout: Option[Int] = None
  // def lamdbaMemory: Option[Int] = None

  def deployLambda = T {
    val s3 = S3Client.create
    val lambda = LambdaClient.create
    val config = LambdaConfig(
      System.getenv.asScala.toMap,
      awsRegion,
      s3Bucket,
      s3KeyPrefix,
      lambdaName,
      lambdaRoleArn
      // lambdaTimeout,
      // lamdbaMemory
    )

    val jarPath: java.nio.file.Path = assembly().path.toNIO

    // AWS REQUESTS
    val putReq = PutObjectRequest.builder
      .bucket(config.bucket)
      .key(config.key)
      .build

    val updateFunctionCodeReq = UpdateFunctionCodeRequest.builder
      .functionName(config.name)
      .s3Bucket(config.bucket)
      .s3Key(config.key)
      .build

    def mkTagReq(arn: String): TagResourceRequest = {
      val tags = Map("deploy.timestamp" -> Instant.now.toString)
      TagResourceRequest.builder
        .resource(arn)
        .tags(tags.asJava)
        .build
    }

    // TODO: Figure out how to do progress listening in v2:
    // v1: https://docs.aws.amazon.com/AmazonS3/latest/dev/HLTrackProgressMPUJava.html
    val updateAttempt = for {
      _ <- Try { s3.putObject(putReq, jarPath) }
      updateResp <- Try { lambda.updateFunctionCode(updateFunctionCodeReq) }
      tagResp <- Try { lambda.tagResource(mkTagReq(updateResp.functionArn)) }
    } yield tagResp

    updateAttempt match {
      case Success(_)  => println("Deployed.")
      case Failure(ex) => println(formatException(ex))
    }
  }

  private def formatException(t: Throwable): String = {
    val msg = Option(t.getLocalizedMessage).getOrElse(t.toString)
    s"$msg\n${t.getStackTrace.mkString("", "\n", "\n")}"
  }
}
