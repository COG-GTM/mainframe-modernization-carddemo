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

# --- Consumed from infra/network (remote state or -var) ---
variable "private_subnet_ids" {
  description = "Private subnet ids the ECS tasks run in."
  type        = list(string)
  default     = []
}

variable "batch_security_group_id" {
  description = "Security group id for batch/service tasks (from infra/network)."
  type        = string
  default     = ""
}

# --- Consumed from infra/storage (remote state or -var) ---
variable "efs_file_system_id" {
  description = "EFS file system id holding AWS.M2.CARDDEMO.* datasets."
  type        = string
  default     = ""
}

variable "efs_access_point_id" {
  description = "EFS access point id (dataset root)."
  type        = string
  default     = ""
}

variable "data_root" {
  description = "Container mount path for datasets (matches entrypoint DATA_DIR)."
  type        = string
  default     = "/data"
}

# --- Program / image config ---
variable "programs" {
  description = "Containerized CB* programs to run (lowercased image names)."
  type        = list(string)
  default     = ["cbtrn02c"]
}

variable "ecr_repository_urls" {
  description = "Map of program -> ECR repo URL (from infra/network). Falls back to a placeholder when empty for offline validation."
  type        = map(string)
  default     = {}
}

variable "image_tag" {
  description = "Container image tag to deploy."
  type        = string
  default     = "latest"
}

variable "health_port" {
  description = "Container health endpoint port (matches HEALTH_PORT)."
  type        = number
  default     = 8080
}

# --- Sizing / autoscaling ---
variable "task_cpu" {
  description = "Fargate task CPU units."
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate task memory (MiB)."
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Baseline running task count per program."
  type        = number
  default     = 1
}

variable "min_capacity" {
  description = "Autoscaling minimum tasks."
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Autoscaling maximum tasks."
  type        = number
  default     = 5
}

variable "cpu_target" {
  description = "Target-tracking average CPU utilization (%) for scaling."
  type        = number
  default     = 60
}

variable "scale_on_queue_depth" {
  description = "Also scale on SQS backlog per task (requires queue_name)."
  type        = bool
  default     = false
}

variable "queue_name" {
  description = "SQS queue name feeding the batch service (for queue-depth scaling)."
  type        = string
  default     = ""
}

variable "queue_backlog_target" {
  description = "Target visible messages per running task."
  type        = number
  default     = 100
}

variable "log_retention_days" {
  description = "CloudWatch log retention."
  type        = number
  default     = 30
}

variable "tags" {
  description = "Extra tags merged into the default tag set."
  type        = map(string)
  default     = {}
}
