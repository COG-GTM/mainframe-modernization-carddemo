terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # No backend block committed: configure per environment with
  # `terraform init -backend-config=...`. This lets the root validate offline.
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.tags
  }
}
