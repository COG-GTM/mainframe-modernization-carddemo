# CardDemo batch/service containers

GnuCOBOL containerization for the CardDemo **batch/service** programs (`CB*`).
Online `CO*` (CICS/BMS) programs are **not** containerized — they stay on the AWS
Mainframe Modernization (M2) managed runtime.

See [`../docs/modernization/CURRENT_STATE_ANALYSIS.md`](../docs/modernization/CURRENT_STATE_ANALYSIS.md)
and the migration procedure in
[`../docs/modernization/MIGRATION_PLAYBOOK.md`](../docs/modernization/MIGRATION_PLAYBOOK.md).

## Files

| File | Purpose |
| :--- | :------ |
| `Dockerfile.batch` | Parameterized image; compiles one `CB*` program with GnuCOBOL (`cobc`), selected by the `PROGRAM` build arg. |
| `entrypoint.sh` | Maps mainframe DD/ASSIGN names to files on the mounted data volume, runs the program, reports exit status to the health endpoint. |
| `healthserver.py` | Sidecar HTTP health endpoint (`/health` liveness, `/ready` readiness) for ECS/EKS probes. |
| `ddmap/<PROGRAM>.env` | Per-program DD → `AWS.M2.CARDDEMO.*` dataset mapping. Onboarding a new program adds only this file. |

The pattern is deliberately program-agnostic: `Dockerfile.batch`, `entrypoint.sh`
and `healthserver.py` are shared, and each additional program (PR #10..N) adds
just a `ddmap/<PROGRAM>.env`. Reference program (PR #3): **`CBTRN02C`** (POSTTRAN).

## Build

```bash
# From the repo root:
docker build -f containers/Dockerfile.batch \
  --build-arg PROGRAM=CBTRN02C \
  -t carddemo-batch:CBTRN02C .
```

Programs that call subprograms pass them via `SUBPROGRAMS` (e.g. `CBSTM03A`):

```bash
docker build -f containers/Dockerfile.batch \
  --build-arg PROGRAM=CBSTM03A --build-arg SUBPROGRAMS="CBSTM03B" \
  -t carddemo-batch:CBSTM03A .
```

## Run

Mount the datasets (locally a bind mount; in AWS an EFS/FSx volume) at `/data`,
keeping the `AWS.M2.CARDDEMO.*` file names:

```bash
docker run --rm -p 8080:8080 -v "$PWD/app/data/EBCDIC:/data" carddemo-batch:CBTRN02C
# liveness / readiness
curl -fsS localhost:8080/health
curl -fsS localhost:8080/ready
```

## Build args / environment

| Name | Default | Meaning |
| :--- | :------ | :------ |
| `PROGRAM` (build arg) | — (required) | COBOL program to compile, e.g. `CBTRN02C`. |
| `SUBPROGRAMS` (build arg) | `""` | Space-separated called subprograms to compile/link. |
| `COBC_DIALECT` (build arg) | `ibm` | `cobc -std` dialect (IBM Enterprise COBOL). |
| `COBC_FORMAT` (build arg) | `fixed` | Source format (`fixed`/`free`). |
| `DATA_DIR` | `/data` | Mount point for `AWS.M2.CARDDEMO.*` datasets. |
| `HEALTH_PORT` | `8080` | Health endpoint port. |
| `MODE` | `oneshot` | `oneshot` (batch: exit with program code) or `service` (stay up for scaling). |
| `PROGRAM_PARM` | `""` | Maps to the JCL `PARM=` value (e.g. `CBACT04C` interest date). |

## Data / encoding note

The `app/data/EBCDIC/*` seed files are EBCDIC and record layouts use `COMP-3`
packed decimal; ASCII equivalents exist in `app/data/ASCII`. GnuCOBOL runs
native (ASCII), and VSAM KSDS files need loading into GnuCOBOL's indexed format.
Choosing the encoding strategy and validating packed-decimal/output equivalence
is a **per-program** step in the migration playbook (Phases 2 & 4), not part of
this shared scaffolding.
