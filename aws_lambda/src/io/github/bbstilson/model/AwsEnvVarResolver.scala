package io.github.bbstilson.model

import AwsEnvVar._

import AwsEnvVarResolver.Env

trait AwsEnvVarResolver[T] {
  def getWithFallback(env: Env, opt: Option[T], awsEnvVar: AwsEnvVar): T
}

object AwsEnvVarResolver {
  type Env = Map[String, String]
  def apply[T](implicit resolver: AwsEnvVarResolver[T]): AwsEnvVarResolver[T] = resolver

  implicit val StringAwsEnvVarResolver = new AwsEnvVarResolver[String] {

    def getWithFallback(env: Env, opt: Option[String], awsEnvVar: AwsEnvVar): String = {
      opt
        .orElse(env.get(awsEnvVar.toString))
        // TODO: make this safer. ask user for input? fail safely?
        .getOrElse { throw new Exception(s"Could not find $awsEnvVar") }
    }
  }

  implicit val IntAwsEnvVarResolver = new AwsEnvVarResolver[Int] {

    def getWithFallback(env: Env, opt: Option[Int], awsEnvVar: AwsEnvVar): Int = {
      opt
        .orElse(env.get(awsEnvVar.toString).map(_.toInt))
        // TODO: make this safer. ask user for input? fail safely?
        .getOrElse { throw new Exception(s"Could not find $awsEnvVar") }
    }
  }
}
