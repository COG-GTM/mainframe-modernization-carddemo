# Target-tracking autoscaling per program service.
resource "aws_appautoscaling_target" "program" {
  for_each = toset(var.programs)

  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.program[each.value].name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.min_capacity
  max_capacity       = var.max_capacity
}

# CPU utilization target tracking (always on).
resource "aws_appautoscaling_policy" "cpu" {
  for_each = aws_appautoscaling_target.program

  name               = "${local.name_prefix}-${each.key}-cpu"
  policy_type        = "TargetTrackingScaling"
  service_namespace  = each.value.service_namespace
  resource_id        = each.value.resource_id
  scalable_dimension = each.value.scalable_dimension

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value       = var.cpu_target
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}

# Optional queue-depth (backlog per task) target tracking for work-queue driven
# batch services. Enabled when scale_on_queue_depth = true and queue_name set.
resource "aws_appautoscaling_policy" "queue" {
  for_each = var.scale_on_queue_depth && var.queue_name != "" ? aws_appautoscaling_target.program : {}

  name               = "${local.name_prefix}-${each.key}-queue"
  policy_type        = "TargetTrackingScaling"
  service_namespace  = each.value.service_namespace
  resource_id        = each.value.resource_id
  scalable_dimension = each.value.scalable_dimension

  target_tracking_scaling_policy_configuration {
    customized_metric_specification {
      metric_name = "ApproximateNumberOfMessagesVisible"
      namespace   = "AWS/SQS"
      statistic   = "Average"

      dimensions {
        name  = "QueueName"
        value = var.queue_name
      }
    }

    target_value       = var.queue_backlog_target
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
