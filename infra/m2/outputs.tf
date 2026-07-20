output "m2_environment_id" {
  description = "AWS M2 environment id."
  value       = aws_m2_environment.this.id
}

output "m2_application_id" {
  description = "AWS M2 application id."
  value       = aws_m2_application.this.id
}

output "m2_artifacts_bucket" {
  description = "S3 bucket for M2 deployment artifacts (runtime package + app definition)."
  value       = aws_s3_bucket.artifacts.bucket
}

output "batch_state_machine_arn" {
  description = "Step Functions state machine ARN for the posting cycle."
  value       = aws_sfn_state_machine.batch_cycle.arn
}

output "batch_schedule_name" {
  description = "EventBridge Scheduler name for the nightly batch cycle."
  value       = aws_scheduler_schedule.batch_cycle.name
}
