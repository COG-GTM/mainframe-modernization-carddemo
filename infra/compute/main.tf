locals {
  name_prefix = "${var.project}-${var.environment}"

  tags = merge(
    {
      Project     = var.project
      Environment = var.environment
      ManagedBy   = "terraform"
      Component   = "compute"
      Application = "CardDemo"
    },
    var.tags,
  )

  # Resolve each program's image; placeholder keeps validate/plan working before
  # infra/network ECR outputs are wired in.
  images = {
    for p in var.programs : p => "${lookup(var.ecr_repository_urls, p, "000000000000.dkr.ecr.${var.aws_region}.amazonaws.com/${var.project}/${p}")}:${var.image_tag}"
  }
}

resource "aws_ecs_cluster" "this" {
  name = "${local.name_prefix}-batch"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = { Name = "${local.name_prefix}-batch" }
}

resource "aws_cloudwatch_log_group" "program" {
  for_each = toset(var.programs)

  name              = "/ecs/${local.name_prefix}/${each.value}"
  retention_in_days = var.log_retention_days
}

resource "aws_ecs_task_definition" "program" {
  for_each = toset(var.programs)

  family                   = "${local.name_prefix}-${each.value}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  volume {
    name = "carddemo-data"

    efs_volume_configuration {
      file_system_id     = var.efs_file_system_id
      transit_encryption = "ENABLED"

      authorization_config {
        access_point_id = var.efs_access_point_id
        iam             = "ENABLED"
      }
    }
  }

  container_definitions = jsonencode([
    {
      name      = each.value
      image     = local.images[each.value]
      essential = true

      environment = [
        { name = "PROGRAM", value = upper(each.value) },
        { name = "DATA_DIR", value = var.data_root },
        { name = "HEALTH_PORT", value = tostring(var.health_port) },
        { name = "MODE", value = "service" },
      ]

      mountPoints = [
        { sourceVolume = "carddemo-data", containerPath = var.data_root, readOnly = false },
      ]

      portMappings = [
        { containerPort = var.health_port, protocol = "tcp" },
      ]

      # In-container liveness probe against the health sidecar (python3 is in the image).
      healthCheck = {
        command     = ["CMD-SHELL", "python3 -c \"import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://localhost:${var.health_port}/health').status==200 else 1)\" || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 30
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.program[each.value].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    },
  ])

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  tags = { Name = "${local.name_prefix}-${each.value}" }
}

resource "aws_ecs_service" "program" {
  for_each = toset(var.programs)

  name            = "${local.name_prefix}-${each.value}"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.program[each.value].arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.batch_security_group_id]
    assign_public_ip = false
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  tags = { Name = "${local.name_prefix}-${each.value}" }
}
