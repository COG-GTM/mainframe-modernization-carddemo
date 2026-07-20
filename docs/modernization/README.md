# CardDemo Modernization — Documentation Index

This directory tracks the modernization of the CardDemo mainframe application:
keeping the CICS/BMS online programs (`CO*`) on the AWS Mainframe Modernization
(M2) managed runtime, and re-platforming the batch/service programs (`CB*`) as
GnuCOBOL containers with CI/CD, IaC, health checks, and autoscaling.

## Documents

| Document | Status | Description |
| :------- | :----- | :---------- |
| [`CURRENT_STATE_ANALYSIS.md`](./CURRENT_STATE_ANALYSIS.md) | PR #1 | Program/data/JCL inventory, build chain, gap analysis, PR & parallelization strategy. |
| [`MIGRATION_PLAYBOOK.md`](./MIGRATION_PLAYBOOK.md) | PR #2 | Parameterized, repeatable per-service migration procedure + checklist + template. Also published as a Devin web-app Playbook. |

## Deliverables map

| Area | Location | PR(s) |
| :--- | :------- | :---- |
| Analysis | `docs/modernization/CURRENT_STATE_ANALYSIS.md` | #1 |
| Migration playbook (markdown) | `docs/modernization/MIGRATION_PLAYBOOK.md` | #2 |
| Migration playbook (Devin web app) | app.devin.ai Playbooks | #2 (non-code artifact) |
| Container pattern (manifest-driven) | `containers/Dockerfile.batch`, `containers/entrypoint.sh`, `containers/{ddmap,programs}/<PROGRAM>.env` | #3 (ref `CBTRN02C`) |
| CI build | `.github/workflows/build.yml` | #4 |
| IaC — network + registry | `infra/` | #5 |
| IaC — storage (VSAM datasets) | `infra/` | #6 |
| IaC — compute + autoscaling | `infra/` | #7 |
| IaC — M2 app + batch scheduler | `infra/` | #8 |
| CD deploy | `.github/workflows/deploy.yml` | #9 |
| Per-program rollout | `containers/{ddmap,programs}/<PROGRAM>.env` (two config files per program) | #10..N (`CBACT04C`, `CBSTM03A`, …) |

## PR delivery waves

- **Wave 0 (sequential):** #1 analysis → #2 playbook. Docs only; establishes the
  shared baseline. Everything after Wave 0 fans out.
- **Wave 1 (parallel):** #3 container scaffolding (ref `CBTRN02C`) and #4 CI
  skeleton are **independent** — assign to different people.
- **Wave 2 (parallel):** IaC #5 (network/ECR), #6 (storage), #7 (compute +
  autoscaling), #8 (M2 + scheduler) share a base module but cover independent
  resource domains and can be authored concurrently.
- **Wave 3 (fan-out):** #9 CD, then #10..N — **one PR per additional
  containerized program**, mutually independent once #3 and #9 exist.

## Parallelization callouts

- Wave-0 docs are sequential (playbook depends on analysis).
- PR #3 (containers) ⟂ PR #4 (CI skeleton).
- IaC PRs #5–#8 are parallelizable across resource domains.
- **Biggest win:** per-program migration PRs (#10..N) are mutually independent —
  many on-prem COBOL services can each be migrated by a separate agent/engineer
  simultaneously, each running the Devin Playbook with a different program-name
  parameter.
- Within one service migration: source + copybook extraction + test-data prep
  are parallel; compile → containerize → deploy → validate are sequential.

## Rules for every PR

1. Keep each PR small and reviewable; `main` stays green after each merge.
2. Never mix a docs change with an infra change.
3. Each new program gets its own PR.
4. `build.yml` must pass on every PR.
5. Cross-link each PR to `CURRENT_STATE_ANALYSIS.md` and the relevant playbook
   step.
6. Do not modify COBOL business logic; keep `AWS.M2.CARDDEMO.*` dataset naming.
