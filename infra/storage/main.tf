locals {
  name_prefix = "${var.project}-${var.environment}"

  tags = merge(
    {
      Project     = var.project
      Environment = var.environment
      ManagedBy   = "terraform"
      Component   = "storage"
      Application = "CardDemo"
    },
    var.tags,
  )
}

# Shared, encrypted EFS file system holding the AWS.M2.CARDDEMO.* datasets.
# Both the GnuCOBOL batch containers (ECS) and the M2 runtime mount this so the
# JCL DD/DSN bindings resolve to the same files without renaming datasets.
resource "aws_efs_file_system" "data" {
  creation_token   = "${local.name_prefix}-carddemo-data"
  encrypted        = true
  performance_mode = var.performance_mode
  throughput_mode  = var.throughput_mode

  lifecycle_policy {
    transition_to_ia = var.transition_to_ia
  }

  tags = { Name = "${local.name_prefix}-carddemo-data" }
}

# NFS access restricted to the VPC.
resource "aws_security_group" "efs" {
  name        = "${local.name_prefix}-efs"
  description = "CardDemo EFS (NFS 2049) from within the VPC"
  vpc_id      = var.vpc_id

  ingress {
    description = "NFS from within the VPC"
    from_port   = 2049
    to_port     = 2049
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${local.name_prefix}-efs-sg" }
}

# One mount target per private subnet (one per AZ) for HA access.
resource "aws_efs_mount_target" "data" {
  count = length(var.private_subnet_ids)

  file_system_id  = aws_efs_file_system.data.id
  subnet_id       = var.private_subnet_ids[count.index]
  security_groups = [aws_security_group.efs.id]
}

# Access point pinning the dataset root so tasks mount a consistent path.
resource "aws_efs_access_point" "data" {
  file_system_id = aws_efs_file_system.data.id

  posix_user {
    uid = 1000
    gid = 1000
  }

  root_directory {
    path = var.data_root

    creation_info {
      owner_uid   = 1000
      owner_gid   = 1000
      permissions = "0755"
    }
  }

  tags = { Name = "${local.name_prefix}-carddemo-data-ap" }
}
