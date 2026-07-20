output "vpc_id" {
  description = "VPC id."
  value       = aws_vpc.this.id
}

output "vpc_cidr" {
  description = "VPC CIDR block."
  value       = aws_vpc.this.cidr_block
}

output "public_subnet_ids" {
  description = "Public subnet ids."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet ids (batch tasks / storage mount targets)."
  value       = aws_subnet.private[*].id
}

output "batch_security_group_id" {
  description = "Security group id for batch/service container tasks."
  value       = aws_security_group.batch.id
}

output "ecr_repository_urls" {
  description = "Map of program name -> ECR repository URL."
  value       = { for name, repo in aws_ecr_repository.program : name => repo.repository_url }
}
