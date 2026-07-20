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

# Consumed from infra/network outputs (remote state or -var).
variable "vpc_id" {
  description = "VPC id the EFS mount targets live in."
  type        = string
  default     = ""
}

variable "vpc_cidr" {
  description = "VPC CIDR allowed to reach EFS over NFS (2049)."
  type        = string
  default     = "10.20.0.0/16"
}

variable "private_subnet_ids" {
  description = "Private subnet ids to create EFS mount targets in."
  type        = list(string)
  default     = []
}

variable "performance_mode" {
  description = "EFS performance mode."
  type        = string
  default     = "generalPurpose"
}

variable "throughput_mode" {
  description = "EFS throughput mode."
  type        = string
  default     = "elastic"
}

variable "transition_to_ia" {
  description = "Move files to Infrequent Access after this period."
  type        = string
  default     = "AFTER_30_DAYS"
}

# The AWS.M2.CARDDEMO.* datasets, mirrored as files under the shared data root.
# Batch containers and the M2 runtime mount this path; names are never changed.
variable "datasets" {
  description = "Dataset file names (kept as AWS.M2.CARDDEMO.*) that live on the shared volume."
  type        = list(string)
  default = [
    "AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS",
    "AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS",
    "AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS",
    "AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS",
    "AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS",
    "AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS",
    "AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS",
    "AWS.M2.CARDDEMO.DALYTRAN.PS",
  ]
}

variable "data_root" {
  description = "Directory (EFS access point root) the datasets live under."
  type        = string
  default     = "/carddemo/data"
}

variable "tags" {
  description = "Extra tags merged into the default tag set."
  type        = map(string)
  default     = {}
}
