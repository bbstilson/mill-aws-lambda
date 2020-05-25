package io.github.bbstilson.model

import AwsEnvVar._
import AwsEnvVarResolver.Env

case class LambdaConfig(
  name: String,
  bucket: String,
  key: String,
  region: String,
  // handlers: String,
  roleName: String,
  timeout: Option[Int],
  memory: Option[Int],
  deadLetterArn: Option[String],
  vpcConfigSubnetIds: Option[String],
  vpcConfigSecurityGroupIds: Option[String]
)

object LambdaConfig {

  def apply(
    env: Env,
    lambdaName: Option[String],
    s3Bucket: Option[String],
    s3KeyPrefix: Option[String],
    awsRegion: Option[String],
    lambdaRoleArn: Option[String]
    // lambdaTimeout: Option[Int],
    // lamdbaMemory: Option[Int]
  ): LambdaConfig = {
    LambdaConfig(
      name = resolveAwsEnvVar(env, lambdaName, AWS_LAMBDA_NAME),
      bucket = resolveAwsEnvVar(env, s3Bucket, AWS_LAMBDA_BUCKET_ID),
      key = resolveAwsEnvVar(env, s3KeyPrefix, AWS_LAMBDA_S3_KEY_PREFIX),
      region = resolveAwsEnvVar(env, awsRegion, AWS_REGION),
      roleName = resolveAwsEnvVar(env, lambdaRoleArn, AWS_LAMBDA_IAM_ROLE_ARN),
      timeout = None, // TODO: resolveAwsEnvVar(env, lambdaTimeout, AWS_LAMBDA_TIMEOUT),
      memory = None, // TODO: resolveAwsEnvVar(env, lamdbaMemory, AWS_LAMBDA_MEMORY),
      // handlers = resolveAwsEnvVar(lambdaHandlers, AWS_LAMBDA_NAME, AWS_LAMBDA_HANDLER_NAME),
      deadLetterArn = None, // TODO
      vpcConfigSubnetIds = None, // TODO
      vpcConfigSecurityGroupIds = None // TODO
    )
  }

  private def resolveAwsEnvVar[T: AwsEnvVarResolver](
    env: Map[String, String],
    opt: Option[T],
    awsEnvVar: AwsEnvVar
  ): T = {
    AwsEnvVarResolver[T].getWithFallback(env, opt, awsEnvVar)
  }
}
