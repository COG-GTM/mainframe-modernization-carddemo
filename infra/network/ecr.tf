# One ECR repository per containerized CB* program. Parameterized by var.programs
# so onboarding a new service is a config change, not new resource code.
resource "aws_ecr_repository" "program" {
  for_each = toset(var.programs)

  name                 = "${var.project}/${each.value}"
  image_tag_mutability = var.ecr_image_tag_mutability
  force_delete         = var.environment != "prod"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = { Name = "${local.name_prefix}-${each.value}" }
}

# Keep the last 10 images per repo; expire older untagged ones.
resource "aws_ecr_lifecycle_policy" "program" {
  for_each = aws_ecr_repository.program

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = { type = "expire" }
      },
    ]
  })
}
