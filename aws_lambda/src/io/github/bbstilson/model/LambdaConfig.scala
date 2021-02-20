package io.github.bbstilson.model

case class LambdaBucket(value: String)
case class LambdaPrefix(value: String)
case class LambdaName(value: String)
case class LambdaHandler(value: String)
case class LambdaMemory(value: Int)
case class LambdaRoleArn(value: Option[String])

case class LambdaConfig(
  bucket: LambdaBucket,
  prefix: LambdaPrefix,
  name: LambdaName,
  handler: LambdaHandler,
  memory: LambdaMemory,
  roleArn: LambdaRoleArn
)
