package io.github.bbstilson

import io.github.bbstilson.model._

import mill._
import mill.scalalib._

import com.amazonaws.event._
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.lambda.{ AWSLambda, AWSLambdaClientBuilder }
import com.amazonaws.services.lambda.model.{
  CreateFunctionRequest,
  FunctionCode,
  GetFunctionRequest,
  ResourceNotFoundException,
  Runtime => LambdaRuntime,
  TagResourceRequest,
  UpdateFunctionCodeRequest
}

import scala.util._
import scala.jdk.CollectionConverters._
import java.time.Instant
import java.io.File

trait AwsLambdaModule extends ScalaModule {

  import AwsLambdaModule._

  // The bucket where your jar should be uploaded.
  def s3Bucket: String
  // The prefix key where your jar should be uploaded.
  def s3KeyPrefix: String
  // The name of the Lambda function.
  def lambdaName: String
  // The name of the method within your code that Lambda calls to execute your function.
  // The format includes the file name.
  def lambdaHandler: String
  // The amount of memory available to the function at runtime.
  // Increasing the function's memory also increases its CPU allocation.
  // The default value is 128 MB. The value can be any multiple of 1 MB.
  def lambdaMemory: Option[Int] = None
  // The Amazon Resource Name (ARN) of the function's execution role.
  def lambdaRoleArn: Option[String] = None

  def deployLambda() = T.command {
    require(lambdaMemory.isEmpty || lambdaMemory.get >= DEFAULT_MEMORY)

    implicit val lambda: AWSLambda = AWSLambdaClientBuilder.defaultClient()

    val config = LambdaConfig(
      LambdaBucket(s3Bucket),
      LambdaPrefix(s3KeyPrefix),
      LambdaName(lambdaName),
      LambdaHandler(lambdaHandler),
      LambdaMemory(lambdaMemory.getOrElse(DEFAULT_MEMORY)),
      LambdaRoleArn(lambdaRoleArn)
    )

    // Upload jar.
    uploadJar(config.bucket, config.prefix, assembly().path.toIO)

    if (lambdaExists(config.name))
      updateLambda(config)
    else
      createLambda(config)
  }
}

private[bbstilson] object AwsLambdaModule {
  private val DEFAULT_MEMORY = 512

  private def lambdaExists(name: LambdaName)(implicit lambda: AWSLambda): Boolean =
    Try(lambda.getFunction(new GetFunctionRequest().withFunctionName(name.value))) match {
      case Success(_)                            => true
      case Failure(_: ResourceNotFoundException) => false
      case Failure(ex)                           => throw ex
    }

  private def uploadJar(
    bucket: LambdaBucket,
    prefix: LambdaPrefix,
    jarPath: File
  ): Unit = {
    val tm = TransferManagerBuilder.defaultTransferManager()

    val request = new PutObjectRequest(
      bucket.value,
      prefix.value,
      jarPath
    )

    val jarSizeInBytes = jarPath.length()
    var totalTransferred = 0L

    request.setGeneralProgressListener(new ProgressListener() {
      override def progressChanged(event: ProgressEvent): Unit = {
        totalTransferred += event.getBytesTransferred()
        val perc = "%.2f".format((totalTransferred.toDouble / jarSizeInBytes) * 100)
        Logger.prefix.ticker(s"$perc%")
      }
    })

    tm.upload(request).waitForCompletion()
  }

  private def createLambda(config: LambdaConfig)(implicit
    lambda: AWSLambda
  ): Unit = {
    Logger.logger.info("Creating lambda.")
    lambda.createFunction({
      val role = config.roleArn.value.getOrElse {
        throw new IllegalArgumentException(
          "You must provide a role arn in order to create a lambda."
        )
      }
      new CreateFunctionRequest()
        .withFunctionName(config.name.value)
        .withHandler(config.handler.value)
        .withMemorySize(config.memory.value)
        .withRuntime(LambdaRuntime.Java11)
        .withRole(role)
        .withCode(
          new FunctionCode()
            .withS3Bucket(config.bucket.value)
            .withS3Key(config.prefix.value)
        )
    })
    Logger.logger.info(s"Successfully created ${config.name.value}")
  }

  private def updateLambda(config: LambdaConfig)(implicit
    lambda: AWSLambda
  ): Unit = {
    Logger.logger.info("Updating lambda code.")
    val updateCodeReq = mkUpdateCodeReq(config)
    val updateResult = lambda.updateFunctionCode(updateCodeReq)
    lambda.tagResource(mkTagReq(updateResult.getFunctionArn()))
    Logger.logger.info(s"Succesfully update ${config.name.value}")
  }

  private def mkUpdateCodeReq(config: LambdaConfig): UpdateFunctionCodeRequest =
    new UpdateFunctionCodeRequest()
      .withFunctionName(config.name.value)
      .withS3Bucket(config.bucket.value)
      .withS3Key(config.prefix.value)

  private def mkTagReq(arn: String): TagResourceRequest =
    new TagResourceRequest()
      .withResource(arn)
      .withTags(Map("deploy.timestamp" -> Instant.now().toString()).asJava)
}
