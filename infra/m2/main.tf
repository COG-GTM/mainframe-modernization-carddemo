locals {
  name_prefix = "${var.project}-${var.environment}"

  tags = merge(
    {
      Project     = var.project
      Environment = var.environment
      ManagedBy   = "terraform"
      Component   = "m2"
      Application = "CardDemo"
    },
    var.tags,
  )
}

data "aws_caller_identity" "current" {}

# S3 bucket holding the M2 deployment artifacts (CardDemo_runtime.zip package +
# app-definition JSON). The CD pipeline (PR #9) uploads the runtime here.
resource "aws_s3_bucket" "artifacts" {
  bucket = "${local.name_prefix}-m2-artifacts-${data.aws_caller_identity.current.account_id}"
  tags   = { Name = "${local.name_prefix}-m2-artifacts" }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# M2 managed runtime environment for the kept-as-is online CO* (CICS/BMS) programs.
resource "aws_m2_environment" "this" {
  name               = "${local.name_prefix}-env"
  engine_type        = var.engine_type
  instance_type      = var.m2_environment_instance_type
  subnet_ids         = var.private_subnet_ids
  security_group_ids = [var.batch_security_group_id]

  tags = { Name = "${local.name_prefix}-env" }
}

# M2 application; its definition (in S3) references the CardDemo_runtime.zip package.
resource "aws_m2_application" "this" {
  name        = "${local.name_prefix}-carddemo"
  engine_type = var.engine_type

  definition {
    s3_location = "s3://${aws_s3_bucket.artifacts.bucket}/${var.runtime_definition_key}"
  }

  tags = { Name = "${local.name_prefix}-carddemo" }
}
