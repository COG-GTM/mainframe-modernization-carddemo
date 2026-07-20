output "efs_file_system_id" {
  description = "EFS file system id holding the AWS.M2.CARDDEMO.* datasets."
  value       = aws_efs_file_system.data.id
}

output "efs_file_system_arn" {
  description = "EFS file system ARN."
  value       = aws_efs_file_system.data.arn
}

output "efs_access_point_id" {
  description = "EFS access point id (dataset root) for ECS task volume config."
  value       = aws_efs_access_point.data.id
}

output "efs_security_group_id" {
  description = "Security group id attached to EFS mount targets."
  value       = aws_security_group.efs.id
}

output "data_root" {
  description = "Path the datasets live under on the mounted volume."
  value       = var.data_root
}

output "datasets" {
  description = "Dataset file names hosted on the shared volume (AWS.M2.CARDDEMO.*)."
  value       = var.datasets
}
