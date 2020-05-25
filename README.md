# Mill AWS Lambda

<!-- [![Build Status](https://api.travis-ci.org/DavidGregory084/mill-tpolecat.svg)](https://travis-ci.org/DavidGregory084/mill-tpolecat) -->
[![License](https://img.shields.io/github/license/bbstilson/mill-aws-lambda.svg)](https://opensource.org/licenses/Apache-2.0)
[![Latest Version](https://img.shields.io/maven-central/v/io.github.bbstilson/mill-aws-lambda_2.13.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.bbstilson%22%20AND%20a%3A%22mill-aws-lambda_2.13%22)


[Mill](http://www.lihaoyi.com/mill/) plugin to deploy code to AWS Lambda. This was heavily inspired by the sbt plugin [`sbt-aws-lambda`](https://github.com/saksdirect/sbt-aws-lambda).

## Usage

Import the module in `build.sc` using mill's `$ivy` import syntax, and extend `AwsLambdaModel` in your build definition:

```scala
// build.sc

import $ivy.`io.github.bbstilson::mill-aws-lambda:0.1.1`

import io.github.bbstilson.AwsLambdaModule

object project extends AwsLambdaModule {
  def scalaVersion = "2.13.2"

  def region = Some("us-west-2")
  def s3Bucket = Some("some-bucket")
  def s3KeyPrefix = Some("some-prefix")
  def lambdaName = Some("your-lambda-name")
  def lambdaHandler = Some("org.bbstilson.Handler::handle")
  def lambdaRoleArn = Some("arn:aws:iam::1234567890:role/service-role/role-7gekixvb")
}
```

Then, to package and deploy your project to an existing AWS Lambda.

```bash
mill project.deployLambda
```

Alternatively, if you have the correct environment variables set, you don't need to add anything to your build file:

```scala
object project extends AwsLambdaModule {
  def scalaVersion = "2.13.2"
}
```

## Configuration

You can configure the deploys using settings in `build.sc` or environment variables.

| sbt setting   | Environment variable      |  Description |
|:----------|:----------|:---------------|
| s3Bucket |  AWS_LAMBDA_BUCKET_ID | The name of an S3 bucket where the lambda code will be stored |
| s3KeyPrefix | AWS_LAMBDA_S3_KEY_PREFIX | The prefix to the S3 key where the jar will be uploaded |
| lambdaName |    AWS_LAMBDA_NAME   |   The name to use for this AWS Lambda function. Defaults to the project name |
| handlerName | AWS_LAMBDA_HANDLER_NAME |    Java class name and method to be executed, e.g. `com.gilt.example.Lambda::myMethod` |
| roleArn | AWS_LAMBDA_IAM_ROLE_ARN |The [ARN](http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html "AWS ARN documentation") of an [IAM](https://aws.amazon.com/iam/ "AWS IAM documentation") role to use when creating a new Lambda |
| region |  AWS_REGION | The name of the AWS region to connect to. Defaults to `us-east-1` |
| awsLambdaTimeout | AWS_LAMBDA_TIMEOUT | The Lambda timeout in seconds (1-900). Defaults to AWS default. |
| awsLambdaMemory | AWS_LAMBDA_MEMORY | The amount of memory in MB for the Lambda function (128-1536, multiple of 64). Defaults to AWS default. |
| deadLetterArn | AWS_LAMBDA_DEAD_LETTER_ARN | The [ARN](http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html "AWS ARN documentation") of the Lambda function's dead letter SQS queue or SNS topic, to receive unprocessed messages |
| vpcConfigSubnetIds | AWS_LAMBDA_VPC_CONFIG_SUBNET_IDS | Comma separated list of subnet IDs for the VPC |
| vpcConfigSecurityGroupIds | AWS_LAMBDA_VPC_CONFIG_SECURITY_GROUP_IDS | Comma separated list of security group IDs for the VPC |
| environment  |                | Seq[(String, String)] of environment variables to set in the lambda function |

## Scala Versions

It has been tested with:

* 2.13.2
* 2.13.1
* 2.12.11
* 2.12.10

## License

All code in this repository is licensed under the Apache License, Version 2.0.  See [LICENSE](./LICENSE).
