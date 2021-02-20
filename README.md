# Mill AWS Lambda

<!-- [![Build Status](https://api.travis-ci.org/bbstilson/mill-aws-lambda.svg)](https://travis-ci.org/bbstilson/mill-aws-lambda) -->
[![License](https://img.shields.io/github/license/bbstilson/mill-aws-lambda.svg)](https://opensource.org/licenses/Apache-2.0)
[![Latest Version](https://img.shields.io/maven-central/v/io.github.bbstilson/mill-aws-lambda_2.13.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.bbstilson%22%20AND%20a%3A%22mill-aws-lambda_2.13%22)


[Mill](http://www.lihaoyi.com/mill/) plugin to deploy code to AWS Lambda. This was inspired by the sbt plugin [`sbt-aws-lambda`](https://github.com/saksdirect/sbt-aws-lambda).

## Usage

Import the module in `build.sc` using mill's `$ivy` import syntax, and extend `AwsLambdaModel` in your build definition:

```scala
import $ivy.`io.github.bbstilson::mill-aws-lambda:0.1.2-SNAPSHOT`

import io.github.bbstilson.AwsLambdaModule

object project extends AwsLambdaModule {
  def scalaVersion = "2.13.4"

  def s3Bucket = "bucket"
  def s3KeyPrefix = "prefix"
  def lambdaName = "my-lambda-name"
  def lambdaHandler = "org.company.Handler::handle"
  // These two are only required if you want to create a lambda.
  def lambdaMemory = Some(512)
  def lambdaRoleArn = Some("arn:aws:iam::1234567890:role/service-role/lambda-role")
}
```

Then, to deploy:

```bash
mill project.deployLambda
```

## Example Project

Check out the project in the [`examples`](./examples) directory for a complete example.

## Configuration

| Setting  | Description |
|:----------|:---------------|
| s3Bucket | The name of an S3 bucket where the lambda code will be stored. |
| s3KeyPrefix | The prefix to the S3 key where the jar will be uploaded. |
| lambdaName | The name to use for this AWS Lambda function. |
| lambdaHandler | Class name and method to be executed. |
| lambdaMemory | The amount of memory available to the function at runtime. Increasing the function's memory also increases its CPU allocation. The default value is 128 MB. The value can be any multiple of 1 MB. |
| lambdaRoleArn | The Amazon Resource Name (ARN) of the function's execution role. |

## Scala Versions

It has been tested with:

* 2.13.4
* 2.12.13

## License

All code in this repository is licensed under the Apache License, Version 2.0.  See [LICENSE](./LICENSE).
