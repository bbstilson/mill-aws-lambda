import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`

import mill._
import mill.scalalib._
import mill.scalalib.publish._
import mill.scalalib.scalafmt._
import io.github.davidgregory084.TpolecatModule

import Dependencies._

lazy val crossScalaVersions = Seq("2.13.6", "2.12.13")

object aws_lambda extends Cross[AwsLambdaModule](crossScalaVersions: _*)

class AwsLambdaModule(val crossScalaVersion: String)
    extends CrossScalaModule
    with PublishModule
    with TpolecatModule
    with ScalafmtModule {

  def artifactName = T { "mill-aws-lambda" }

  def publishVersion = "0.2.2"

  def pomSettings = PomSettings(
    description = "Mill plugin to deploy code to AWS Lambda",
    organization = "io.github.bbstilson",
    url = "https://github.com/bbstilson/mill-aws-lambda",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("bbstilson", "mill-aws-lambda"),
    developers = Seq(Developer("bbstilson", "Brandon Stilson", "https://github.com/bbstilson"))
  )

  lazy val millVersion = millVersionFor(crossScalaVersion)

  def ivyDeps = Agg(
    Libraries.awsIam,
    Libraries.awsLambda,
    Libraries.awsS3
  )

  def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-scalalib:$millVersion"
  )
}

def millVersionFor(scalaVersion: String): String =
  if (scalaVersion.startsWith("2.12")) "0.6.3" else "0.9.8"

object Dependencies {

  object Versions {
    val aws = "1.12.12"
  }

  object Libraries {
    def aws(artifact: String) = ivy"com.amazonaws:aws-java-sdk-$artifact:${Versions.aws}"

    val awsIam = aws("iam")
    val awsLambda = aws("lambda")
    val awsS3 = aws("s3")
  }
}
