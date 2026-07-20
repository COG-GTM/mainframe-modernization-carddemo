# CardDemo COBOL Service Migration Playbook

> Wave 0 / PR #2 deliverable. A **parameterized, repeatable** procedure to
> migrate **one** CardDemo COBOL service at a time. Re-run it (changing only the
> program-name parameter) to parallelize migration of many on-prem COBOL
> services. Published in two identical forms: this committed markdown file and a
> Devin web-app Playbook.
>
> Baseline: [`CURRENT_STATE_ANALYSIS.md`](./CURRENT_STATE_ANALYSIS.md) · Index:
> [`README.md`](./README.md)

## Overview

Migrate a single CardDemo COBOL program off z/OS while leaving its business
logic untouched (wrap and re-platform only). Two paths, chosen by program class:

- **Online (`CO*`, CICS/BMS)** — keep AS-IS; deploy on the AWS Mainframe
  Modernization (M2) managed runtime, reusing `samples/m2/mf/CardDemo_runtime.zip`.
  **Not containerized.**
- **Batch / service (`CB*`)** — containerize with GnuCOBOL (`cobc`), wire data
  access to mounted volumes, add an HTTP health check, register in CI/CD, and
  validate behavioral equivalence against seed data.

## What's Needed From User

- **`PROGRAM_NAME`** (required) — the COBOL program, e.g. `CBTRN02C`,
  `CBACT04C`, `CBSTM03A`. This is the single parameter that makes the playbook
  reusable.
- **`PROGRAM_CLASS`** (optional; inferred from prefix) — `batch` for `CB*`,
  `online` for `CO*`.
- **AWS target** (for deploy phases) — account/region, ECR repo, ECS/EKS cluster,
  EFS/FSx file system id, and M2 environment/application ids from the `infra/`
  Terraform outputs.
- **Validation baseline** — known-good output for the program (existing seed data
  lives in `app/data/`; EBCDIC in `app/data/EBCDIC`, ASCII equivalents in
  `app/data/ASCII`).

## Parameters and conventions

| Parameter | Example | Notes |
| :-------- | :------ | :---- |
| `PROGRAM_NAME` | `CBTRN02C` | Drives every path/artifact name below. |
| Source | `app/cbl/${PROGRAM_NAME}.cbl` | Some files use `.CBL` (e.g. `CBSTM03A.CBL`). |
| Copybooks | `app/cpy/*` (+ `app/cpy-bms` for online) | Passed to `cobc -I app/cpy`. |
| Image | `${ECR_REPO}:${PROGRAM_NAME}` | Built from `containers/Dockerfile.batch` with `--build-arg PROGRAM=${PROGRAM_NAME}`. |
| Dataset naming | `AWS.M2.CARDDEMO.*` | **Never rename** — keeps existing JCL and the M2 runtime package compatible. |
| DD → path | `/data/AWS.M2.CARDDEMO.<name>` | Entrypoint maps each ASSIGN/DD name to a mounted volume path (mirrors the M2 EFS mapping). |

<phase name="Assess & Classify" id="1">
## Phase 1 — Assess & classify

1. Set `PROGRAM_NAME` and confirm the class: `CB*` → batch/containerize,
   `CO*` → online/keep-as-is on M2. Stop and use the online path (Phase 5) for
   `CO*`; otherwise continue the batch path.
2. Read `app/cbl/${PROGRAM_NAME}.cbl` and record its `SELECT … ASSIGN TO <DD>`
   clauses, file `ORGANIZATION` (SEQUENTIAL vs INDEXED), `RECORD KEY`s, any
   `PARM` inputs, and any `CALL`ed subprograms (e.g. `CBSTM03A` calls `CBSTM03B`;
   date logic calls `CSUTLDTC`).
3. Cross-reference the driving JCL in `app/jcl` to map each DD to a
   `AWS.M2.CARDDEMO.*` dataset (see `CURRENT_STATE_ANALYSIS.md` §4.1).
4. Flag migration risks for this program: `COMP-3`/packed-decimal fields,
   EBCDIC vs ASCII data, VSAM alternate indexes, GDG `(+1)` outputs, and LE date
   services (no `CEEDAYS`/`CEEDATE` under GnuCOBOL).

<verification>
- `PROGRAM_NAME` and `PROGRAM_CLASS` are set.
- All DD/ASSIGN names are mapped to `AWS.M2.CARDDEMO.*` datasets with
  organization + key length/offset noted.
- Called subprograms and PARM inputs are listed.
- Per-program risk list (COMP-3 / EBCDIC / AIX / GDG / LE date) is recorded.
</verification>
</phase>

<phase name="Extract Source, Copybooks & Test Data" id="2">
## Phase 2 — Extract source, copybooks & test data (PARALLEL)

These three sub-tasks are independent and can run concurrently:

1. **Source extraction** — collect `app/cbl/${PROGRAM_NAME}.cbl` plus every
   `CALL`ed subprogram source.
2. **Copybook extraction** — collect the copybooks the program `COPY`s from
   `app/cpy` (record layouts such as `CVACT01Y`, `CVTRA06Y`, `CSUSR01Y`).
3. **Test-data prep** — identify the seed datasets the program reads/writes in
   `app/data/`. Decide the encoding strategy: use the ASCII equivalents in
   `app/data/ASCII`, or convert EBCDIC → ASCII, honoring `COMP-3` layouts. Keep
   a copy of the expected output for the Phase 4 comparison.

<verification>
- Program source + all called subprograms are collected.
- All `COPY`d copybooks are collected.
- Seed input data and a known-good expected-output baseline are prepared with a
  documented encoding decision.
</verification>
</phase>

<phase name="Compile in the Cloud (GnuCOBOL)" id="3">
## Phase 3 — Compile in the cloud with GnuCOBOL (SEQUENTIAL)

1. Compile locally first to shorten the loop (CardDemo sources are IBM dialect,
   fixed format):
   `cobc -x -std=ibm -fixed -ftab-width=1 -I app/cpy -o build/${PROGRAM_NAME} app/cbl/${PROGRAM_NAME}.cbl`
   Notes / per-program knobs (recorded in `containers/programs/${PROGRAM_NAME}.env`):
   - Add each called subprogram to the compile/link (`SUBPROGRAMS`), e.g.
     `CBSTM03A` links `CBSTM03B`.
   - `-ftab-width=1` keeps tab-indented copybooks (e.g. `CUSTREC.cpy`) aligned in
     fixed format.
   - If the program has a `PROCEDURE DIVISION USING` main (receives a JCL `PARM`,
     e.g. `CBACT04C`), GnuCOBOL rejects `cobc -x`; compile as a module
     (`cobc -m … -o build/${PROGRAM_NAME}.so`) and run via
     `cobcrun ${PROGRAM_NAME} <parm>` (`RUN_MODE=module`).
   - Resolve remaining GnuCOBOL-specific issues from the Phase 1 risk list (e.g.
     replace LE services such as `CEE3ABD`, load VSAM KSDS into GnuCOBOL indexed
     format, verify `COMP-3` options).
2. Build the container image from the shared pattern (PR #3) — callers pass only
   `PROGRAM`; the manifest drives the rest:
   `docker build -f containers/Dockerfile.batch --build-arg PROGRAM=${PROGRAM_NAME} -t ${PROGRAM_NAME}:local .`
   The Dockerfile installs `cobc`, sources `containers/programs/${PROGRAM_NAME}.env`,
   compiles `app/cbl/${PROGRAM_NAME}.*` with copybooks from `app/cpy`, and bundles
   the health-check wrapper.
3. Register the program in CI: for `cobc -x`-clean programs, add the name to
   `ci/programs.txt` so `.github/workflows/build.yml` compiles it (matrix is
   parameterized — no new pipeline code). module-mode/special-flag programs are
   validated via their container build (CD `deploy.yml` discovers them from
   `containers/ddmap/`).

<verification>
- `cobc` compiles the program (and subprograms) with no errors.
- `docker build` succeeds for `${PROGRAM_NAME}`.
- The program is registered (in `ci/programs.txt` and/or via its `containers/`
  config) and the relevant workflow passes.
</verification>
</phase>

<phase name="Containerize, Wire Data & Health Checks" id="4">
## Phase 4 — Containerize, wire data access, health checks & validate (SEQUENTIAL)

1. Confirm `containers/entrypoint.sh` maps this program's DD/ASSIGN names to the
   mounted volume paths (`/data/AWS.M2.CARDDEMO.*`) and passes any required PARM.
   Add only the program-specific config; do not fork the shared script.
2. Verify the HTTP health endpoint reports liveness before the run and
   readiness/exit status after (used by ECS/EKS probes).
3. Run the container against the Phase 2 seed data on a mounted volume.
4. **Validate behavioral equivalence**: diff the container output against the
   known-good baseline. Pay special attention to packed-decimal totals
   (posting/interest/statement amounts) and record counts. Investigate any
   difference before proceeding — do not "fix" by editing business logic.

<verification>
- Container runs end-to-end against seed data and exits 0.
- Health endpoint returns healthy (liveness) and reports final exit status.
- Output matches the known-good baseline (amounts + record counts), with any
  COMP-3/EBCDIC differences explained and resolved.
</verification>
</phase>

<phase name="Online Path — Deploy AS-IS to M2" id="5">
## Phase 5 — Online path: deploy AS-IS to AWS M2 (only for `CO*`)

Skip for batch programs. For `CO*` (CICS/BMS) programs:

1. Do **not** containerize. Package the program with the existing M2 runtime
   (`samples/m2/mf/CardDemo_runtime.zip`); keep `app/csd/CARDDEMO.CSD`
   transaction/program/map definitions intact.
2. Deploy/update the M2 application via the CD workflow (`deploy.yml`, PR #9) /
   `infra/` M2 resources (PR #8), parameterized by environment (dev/test/prod).
3. Verify the transaction still works: connect to the M2 3270 endpoint, run the
   program's transaction (e.g. `CC00` signon → menu), and confirm the screens
   render and navigate correctly. **Record a screen recording of the interaction
   as proof the online app is unaffected**, and attach it to the PR/session.

<verification>
- No container artifacts were produced for the `CO*` program.
- M2 application deploys and the transaction runs against the M2 3270 endpoint.
- A screen recording of the working transaction is captured and attached.
</verification>
</phase>

<phase name="Register in CI/CD & Deploy" id="6">
## Phase 6 — Register in CI/CD, deploy & confirm

1. Add the one-program config for `${PROGRAM_NAME}` in its **own** PR (reusing
   the PR #3 pattern) so `build.yml` builds it and `deploy.yml` (PR #9) publishes
   its image to ECR and, for the scheduled cycle, wires it into the
   EventBridge/Step Functions orchestration (PR #8) at the correct position
   (`CLOSEFIL → POSTTRAN → INTCALC → CREASTMT → OPENFIL`).
2. Deploy to `dev`, run the containerized job (or scheduled step), and re-check
   the health endpoint + output.
3. Cross-link the PR to `CURRENT_STATE_ANALYSIS.md` and this playbook; confirm
   `main` stays green.

<verification>
- One PR per program; `build.yml` passes on it.
- Image published to ECR; job runs in `dev` with a healthy status and correct
  output.
- PR cross-links the analysis + playbook; `main` remains green.
</verification>
</phase>

## Per-service checklist

- [ ] `PROGRAM_NAME` set; class confirmed (`CB*` batch / `CO*` online).
- [ ] DD/ASSIGN → `AWS.M2.CARDDEMO.*` dataset map recorded (org, key len/offset).
- [ ] Called subprograms + PARM inputs listed.
- [ ] Risk list recorded (COMP-3 / EBCDIC / AIX / GDG / LE date).
- [ ] Source + subprograms + copybooks extracted.
- [ ] Seed input + known-good expected output prepared (encoding decided).
- [ ] `cobc` compiles clean; `docker build` succeeds.
- [ ] `build.yml` compiles the program and passes.
- [ ] `entrypoint.sh` DD→volume mapping + PARM verified.
- [ ] Health endpoint liveness/readiness verified.
- [ ] Output matches baseline (amounts + counts); differences explained.
- [ ] (Online) M2 deploy + 3270 transaction verified + screen recording attached.
- [ ] Own PR created, cross-linked to analysis + playbook; `main` green.

## Per-service PR template

```markdown
## Summary
Migrate `${PROGRAM_NAME}` (<batch|online>) following docs/modernization/MIGRATION_PLAYBOOK.md.
- Class: <CB* containerized with GnuCOBOL | CO* kept AS-IS on M2>
- Datasets touched (AWS.M2.CARDDEMO.*): <list>
- Risks handled: <COMP-3 / EBCDIC / AIX / GDG / LE date>

## Changes
- containers/config or manifest for ${PROGRAM_NAME} (reuses containers/Dockerfile.batch)
- CI: build.yml builds ${PROGRAM_NAME}
- (online only) M2 packaging/deploy config

## Validation
- cobc compile: <result>
- container run vs baseline: <amounts + record counts match?>
- health endpoint: <healthy>
- (online) 3270 transaction recording: <link>

Links: CURRENT_STATE_ANALYSIS.md · MIGRATION_PLAYBOOK.md (phase <n>)
```

## Parallel vs sequential (within one migration)

- **Parallel:** source extraction · copybook extraction · test-data prep (Phase 2).
- **Sequential:** compile (Phase 3) → containerize + wire data + health check
  (Phase 4) → deploy (Phase 6) → validate. Online path (Phase 5) replaces
  containerization for `CO*`.

## Cross-service parallelization

Once the container pattern (PR #3) and CD (PR #9) exist, **each program migration
is fully independent**. Assign a separate agent/engineer per program, each
running this playbook with a different `PROGRAM_NAME`, producing one PR per
program (#10..N). This is the concrete realization of the parallelization goal.

## Specifications (postconditions)

- Business logic unchanged; only wrapping/re-platforming performed.
- `AWS.M2.CARDDEMO.*` dataset naming preserved.
- Batch/service program: reproducible GnuCOBOL container image with a working
  health endpoint, registered in CI/CD, output validated against seed data.
- Online program: deployed AS-IS on M2 with a verified transaction + recording.
- Exactly one small, single-purpose PR per program, cross-linked to the analysis
  and this playbook; `main` stays green.

## Forbidden actions

- Do **not** modify existing COBOL business logic.
- Do **not** rename `AWS.M2.CARDDEMO.*` datasets.
- Do **not** containerize `CO*` online/3270 programs.
- Do **not** bundle multiple programs (or docs + infra) into one PR.
- Do **not** merge a program PR while `build.yml` is failing.
