output "cluster_name" {
  description = "ECS cluster name."
  value       = aws_ecs_cluster.this.name
}

output "cluster_arn" {
  description = "ECS cluster ARN."
  value       = aws_ecs_cluster.this.arn
}

output "service_names" {
  description = "Map of program -> ECS service name."
  value       = { for p, s in aws_ecs_service.program : p => s.name }
}

output "task_definition_arns" {
  description = "Map of program -> task definition ARN."
  value       = { for p, t in aws_ecs_task_definition.program : p => t.arn }
}

output "task_role_arn" {
  description = "ECS task role ARN."
  value       = aws_iam_role.task.arn
}

output "execution_role_arn" {
  description = "ECS execution role ARN."
  value       = aws_iam_role.execution.arn
}
