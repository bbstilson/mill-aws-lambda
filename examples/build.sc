import $ivy.`io.github.bbstilson::mill-aws-lambda:0.2.0`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`

import mill._
import mill.scalalib._
import io.github.bbstilson.AwsLambdaModule
import io.github.davidgregory084.TpolecatModule

object example extends AwsLambdaModule with TpolecatModule {
  def scalaVersion = "2.13.4"

  def s3Bucket = "some-bucket"
  def s3KeyPrefix = "lambda-test"
  def lambdaName = "test"
  def lambdaHandler = "Main::getSourceBuckets"

  def ivyDeps = Agg(
    ivy"com.amazonaws:aws-lambda-java-core:1.0.0",
    ivy"com.amazonaws:aws-lambda-java-events:1.0.0"
  )
}
