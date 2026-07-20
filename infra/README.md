# CardDemo Modernization — Infrastructure as Code (Terraform)

Terraform for the CardDemo cloud target: the batch/service (`CB*`) programs run
as GnuCOBOL containers on ECS with health checks + autoscaling; the online
(`CO*`) programs stay on the AWS Mainframe Modernization (M2) managed runtime.

See [`../docs/modernization/CURRENT_STATE_ANALYSIS.md`](../docs/modernization/CURRENT_STATE_ANALYSIS.md)
(§6 gaps, §8 PR strategy) and
[`../docs/modernization/MIGRATION_PLAYBOOK.md`](../docs/modernization/MIGRATION_PLAYBOOK.md).

## Layout (one reviewable root per resource domain)

Each directory is a **self-contained Terraform root** that `terraform validate`s
independently and follows the same naming/tagging convention
(`local.name_prefix = "${var.project}-${var.environment}"`, common `local.tags`).
This keeps the four IaC PRs independent and authorable in parallel, while sharing
one consistent pattern.

| Directory | Domain | PR |
| :-------- | :----- | :- |
| `network/` | VPC, subnets, NAT/IGW, security group, **ECR** registry | #5 |
| `storage/` | EFS/FSx for the `AWS.M2.CARDDEMO.*` VSAM/PS datasets | #6 |
| `compute/` | ECS cluster + services for batch containers, **health checks + target-tracking autoscaling** | #7 |
| `m2/` | AWS M2 environment/application (online, as-is) + EventBridge/Step Functions batch scheduler | #8 |

`compute/` and `m2/` consume `network/` and `storage/` outputs (VPC/subnet ids,
security groups, file-system ids). In a real deployment wire them via remote
state (`terraform_remote_state`) or `-var` inputs; the roots expose the needed
`outputs.tf` values and accept the consumed ids as variables so they stay
decoupled.

## Conventions

- **Backend/providers**: `versions.tf` pins Terraform + AWS provider. No backend
  block is committed (configure `-backend-config` per environment) so the roots
  validate without cloud credentials.
- **Parameterization**: `var.project`, `var.environment` (dev/test/prod),
  `var.aws_region`, and `var.programs` (list of containerized `CB*` programs) so
  new services onboard by config, not new resource code.
- **Dataset naming**: preserved as `AWS.M2.CARDDEMO.*` end to end.

## Validate

```bash
cd infra/network && terraform init -backend=false && terraform validate
terraform fmt -check -recursive ..
```
