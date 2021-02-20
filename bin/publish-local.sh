#!/usr/bin/env bash

set -eux

rm -rf out/

mill "aws_lambda[__].publishLocal"
