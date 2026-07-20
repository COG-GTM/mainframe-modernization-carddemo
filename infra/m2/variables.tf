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

# --- Consumed from infra/network ---
variable "private_subnet_ids" {
  description = "Private subnet ids for the M2 environment and ECS run-tasks."
  type        = list(string)
  default     = []
}

variable "batch_security_group_id" {
  description = "Security group id for M2/batch tasks."
  type        = string
  default     = ""
}

# --- M2 application (online CO* programs, kept as-is) ---
variable "engine_type" {
  description = "AWS Mainframe Modernization engine (microfocus | bluage)."
  type        = string
  default     = "microfocus"
}

variable "m2_environment_instance_type" {
  description = "M2 runtime environment instance type."
  type        = string
  default     = "M2.m5.large"
}

variable "runtime_definition_key" {
  description = "S3 key of the M2 application definition JSON (references CardDemo_runtime.zip)."
  type        = string
  default     = "carddemo/app-definition.json"
}

# --- Consumed from infra/compute (for the batch scheduler) ---
variable "ecs_cluster_arn" {
  description = "ECS cluster ARN running the batch containers."
  type        = string
  default     = ""
}

# Map of batch job step -> ECS task definition ARN. Steps map to the posting
# cycle: POSTTRAN=CBTRN02C, INTCALC=CBACT04C, CREASTMT=CBSTM03A.
variable "batch_task_definition_arns" {
  description = "Map of job step name -> ECS task definition ARN (from infra/compute)."
  type        = map(string)
  default = {
    POSTTRAN = "arn:aws:ecs:us-east-1:000000000000:task-definition/carddemo-dev-cbtrn02c"
    INTCALC  = "arn:aws:ecs:us-east-1:000000000000:task-definition/carddemo-dev-cbact04c"
    CREASTMT = "arn:aws:ecs:us-east-1:000000000000:task-definition/carddemo-dev-cbstm03a"
  }
}

variable "schedule_expression" {
  description = "EventBridge Scheduler expression for the nightly batch cycle."
  type        = string
  default     = "cron(0 2 * * ? *)"
}

variable "schedule_enabled" {
  description = "Whether the batch schedule is enabled."
  type        = bool
  default     = true
}

variable "tags" {
  description = "Extra tags merged into the default tag set."
  type        = map(string)
  default     = {}
}
