import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`

import mill._
import mill.scalalib._
import mill.scalalib.publish._
import io.github.davidgregory084.TpolecatModule

lazy val crossScalaVersions = Seq("2.13.2", "2.13.1", "2.12.11", "2.12.10")

object aws_lambda extends Cross[AwsLambdaModule](crossScalaVersions: _*)

class AwsLambdaModule(val crossScalaVersion: String)
    extends CrossScalaModule
    with PublishModule
    with TpolecatModule {
  def artifactName = T { "mill-aws-lambda" }

  def publishVersion = "0.1.0"

  def pomSettings = PomSettings(
    description = "Mill plugin to deploy code to AWS Lambda",
    organization = "io.github.bbstilson",
    url = "https://github.com/bbstilson/mill-aws-lambda",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("bbstilson", "mill-aws-lambda"),
    developers = Seq(Developer("bbstilson", "Brandon Stilson", "https://github.com/bbstilson"))
  )

  val AwsVersion = "2.13.23"

  lazy val millVersion = millVersionFor(crossScalaVersion)

  def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-scalalib:$millVersion",
    ivy"software.amazon.awssdk:iam:$AwsVersion",
    ivy"software.amazon.awssdk:lambda:$AwsVersion",
    ivy"software.amazon.awssdk:s3:$AwsVersion"
  )

}

def millVersionFor(scalaVersion: String): String = {
  if (scalaVersion.startsWith("2.13")) "0.7.3" else "0.6.3"
}
