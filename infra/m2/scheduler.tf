# Batch scheduler reproducing the manual JCL posting cycle:
#   CLOSEFIL -> POSTTRAN(CBTRN02C) -> INTCALC(CBACT04C) -> CREASTMT(CBSTM03A) -> OPENFIL
# CLOSEFIL/OPENFIL run as M2 batch jobs (file open/close); the three posting
# steps run the GnuCOBOL containers via ECS RunTask. EventBridge Scheduler
# triggers the Step Functions state machine on a cron.

locals {
  # ECS RunTask parameters shared by the containerized posting steps.
  ecs_network_config = {
    AwsvpcConfiguration = {
      Subnets        = var.private_subnet_ids
      SecurityGroups = [var.batch_security_group_id]
      AssignPublicIp = "DISABLED"
    }
  }

  ecs_step = { for step, taskdef in var.batch_task_definition_arns : step => {
    Type     = "Task"
    Resource = "arn:aws:states:::ecs:runTask.sync"
    Parameters = {
      Cluster              = var.ecs_cluster_arn
      TaskDefinition       = taskdef
      LaunchType           = "FARGATE"
      NetworkConfiguration = local.ecs_network_config
    }
  } }

  m2_step = { for step in ["CLOSEFIL", "OPENFIL"] : step => {
    Type     = "Task"
    Resource = "arn:aws:states:::aws-sdk:m2:startBatchJob"
    Parameters = {
      ApplicationId = aws_m2_application.this.id
      BatchJobIdentifier = {
        FileBatchJobIdentifier = { FileName = step }
      }
    }
  } }

  state_machine_definition = {
    Comment = "CardDemo nightly posting cycle"
    StartAt = "CLOSEFIL"
    States = {
      CLOSEFIL = merge(local.m2_step["CLOSEFIL"], { Next = "POSTTRAN" })
      POSTTRAN = merge(local.ecs_step["POSTTRAN"], { Next = "INTCALC" })
      INTCALC  = merge(local.ecs_step["INTCALC"], { Next = "CREASTMT" })
      CREASTMT = merge(local.ecs_step["CREASTMT"], { Next = "OPENFIL" })
      OPENFIL  = merge(local.m2_step["OPENFIL"], { End = true })
    }
  }
}

resource "aws_sfn_state_machine" "batch_cycle" {
  name       = "${local.name_prefix}-batch-cycle"
  role_arn   = aws_iam_role.sfn.arn
  definition = jsonencode(local.state_machine_definition)
  tags       = { Name = "${local.name_prefix}-batch-cycle" }
}

resource "aws_scheduler_schedule" "batch_cycle" {
  name = "${local.name_prefix}-batch-cycle"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression = var.schedule_expression
  state               = var.schedule_enabled ? "ENABLED" : "DISABLED"

  target {
    arn      = aws_sfn_state_machine.batch_cycle.arn
    role_arn = aws_iam_role.scheduler.arn
  }
}
