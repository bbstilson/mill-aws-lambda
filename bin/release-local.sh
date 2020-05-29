#!/usr/bin/env bash

set -eux

mill "aws_lambda[2.13.2].publishLocal"
mill "aws_lambda[2.12.11].publishLocal"
