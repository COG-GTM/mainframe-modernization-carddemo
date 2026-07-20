variable "project" {
  description = "Project name used as a resource name prefix."
  type        = string
  default     = "carddemo"
}

variable "environment" {
  description = "Deployment environment (dev/test/prod)."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "az_count" {
  description = "Number of Availability Zones to spread subnets across."
  type        = number
  default     = 2

  validation {
    condition     = var.az_count >= 2 && var.az_count <= 3
    error_message = "az_count must be 2 or 3 for multi-AZ resilience."
  }
}

variable "enable_nat_gateway" {
  description = "Create a NAT gateway so private subnets have egress (image pulls, AWS APIs)."
  type        = bool
  default     = true
}

variable "programs" {
  description = "Containerized CB* programs to create ECR repositories for (lowercased image names)."
  type        = list(string)
  default     = ["cbtrn02c"]
}

variable "ecr_image_tag_mutability" {
  description = "ECR image tag mutability."
  type        = string
  default     = "IMMUTABLE"
}

variable "tags" {
  description = "Extra tags merged into the default tag set."
  type        = map(string)
  default     = {}
}
