# CardDemo — Current State Analysis

> Wave 0 / PR #1 deliverable. This is the shared baseline for the CardDemo
> mainframe modernization effort. Every downstream PR (containers, CI/CD, IaC,
> per-program rollout) links back to this document.
>
> Index: [`README.md`](./README.md) · Playbook: [`MIGRATION_PLAYBOOK.md`](./MIGRATION_PLAYBOOK.md) (PR #2)

## 1. Scope and confirmed decisions

CardDemo is a z/OS credit-card management application written in COBOL, running
online transactions under CICS and batch jobs under JCL, persisting data in VSAM
and sequential (PS) datasets. There is currently **no CI/CD, no IaC, no
containerization, no health checks, and no autoscaling** — build and deploy are
manual JCL/CICS operations.

Two migration tracks were agreed up front:

| Track | Programs | Target | Rationale |
| :---- | :------- | :----- | :-------- |
| **Online (keep as-is)** | `CO*` (3270/BMS/CICS) | AWS Mainframe Modernization (M2) managed runtime, reusing `samples/m2/mf/CardDemo_runtime.zip` | 3270/BMS online transactions run unchanged on the M2 managed runtime; not containerized. |
| **Batch / service (containerize)** | `CB*` | GnuCOBOL (`cobc`, open source) in Linux containers on ECS/EKS | Batch/service programs are re-platformed to open-source COBOL and orchestrated as containers with health checks + autoscaling. |

**Hard constraints (apply to all PRs):**
- Do **not** modify existing COBOL business logic — wrap and re-platform only.
- Keep the `AWS.M2.CARDDEMO.*` dataset naming so existing JCL and the M2 runtime
  package stay compatible.
- Validate `COMP-3`/packed-decimal and EBCDIC behavior during migration
  (GnuCOBOL defaults to ASCII/native; the batch data in `app/data/EBCDIC` is
  EBCDIC-encoded and record layouts use packed-decimal).
- Everything parameterized/templated so the playbook parallelizes migration of
  many additional on-prem COBOL services.
- Every PR small and single-purpose; `main` stays green after each merge.

## 2. Program inventory

27 COBOL programs in `app/cbl` (+ `CSUTLDTC` date utility). Classification is by
name prefix and cross-referenced against `app/csd/CARDDEMO.CSD` (CICS
program/transaction/file definitions) and the batch job table in `README.md`.

### 2.1 Online programs — `CO*` (keep as-is on M2)

Cross-referenced with CICS `DEFINE PROGRAM` / `DEFINE TRANSACTION` in
`app/csd/CARDDEMO.CSD` and the README "Online" inventory table.

| Program | Transaction | BMS Map | Function |
| :------ | :---------- | :------ | :------- |
| `COSGN00C` | CC00 | COSGN00 | Signon screen |
| `COMEN01C` | CM00 | COMEN01 | Main menu |
| `COADM01C` | CA00 | COADM01 | Admin menu |
| `COACTVWC` | CAVW | COACTVW | Account view |
| `COACTUPC` | CAUP | COACTUP | Account update |
| `COCRDLIC` | CCLI | COCRDLI | Credit card list |
| `COCRDSLC` | CCDL | COCRDSL | Credit card view |
| `COCRDUPC` | CCUP | COCRDUP | Credit card update |
| `COTRN00C` | CT00 | COTRN00 | Transaction list |
| `COTRN01C` | CT01 | COTRN01 | Transaction view |
| `COTRN02C` | CT02 | COTRN02 | Transaction add |
| `CORPT00C` | CR00 | CORPT00 | Transaction reports |
| `COBIL00C` | CB00 | COBIL00 | Bill payment |
| `COUSR00C` | CU00 | COUSR00 | List users |
| `COUSR01C` | CU01 | COUSR01 | Add user |
| `COUSR02C` | CU02 | COUSR02 | Update user |
| `COUSR03C` | CU03 | COUSR03 | Delete user |

`app/csd/CARDDEMO.CSD` contains 18 `DEFINE PROGRAM`, 18 `DEFINE TRANSACTION`, and
8 `DEFINE FILE` entries (files listed in §3.2).

### 2.2 Batch / service programs — `CB*` (containerize with GnuCOBOL)

| Program | Driving job | Function | LOC | Notes |
| :------ | :---------- | :------- | --: | :---- |
| `CBTRN02C` | POSTTRAN | Daily transaction posting (core cycle) | 731 | **PR #3 reference program.** Reads DALYTRAN PS, updates ACCT/TCATBAL/TRANSACT VSAM, writes DALYREJS. |
| `CBACT04C` | INTCALC | Interest calculation | 652 | Reads TCATBAL/XREF/ACCT/DISCGRP, writes SYSTRAN GDG. Takes date PARM. First per-program rollout PR (#10). |
| `CBSTM03A` | CREASTMT | Statement generation | 924 | Calls `CBSTM03B` (I/O subroutine, 230 LOC). Produces PS + HTML statements. Rollout PR (#11). |
| `CBTRN01C` | (read/report) | Daily transaction read/validate | 491 | |
| `CBTRN03C` | (report) | Transaction detail report | 649 | |
| `CBACT01C` | (report) | Account master print | 193 | |
| `CBACT02C` | (report) | Card master print | 178 | |
| `CBACT03C` | (report) | Xref print | 178 | |
| `CBCUS01C` | (report) | Customer master print | 178 | |
| `CBSTM03B` | (subroutine) | I/O helper called by `CBSTM03A` | 230 | Compiled/linked with `CBSTM03A`. |

### 2.3 Utility subroutine — `CS*`

| Program | Function | Notes |
| :------ | :------- | :---- |
| `CSUTLDTC` | Date validation/conversion (calls `CEEDAYS`/`CEEDATE` LE services) | Called as a subroutine by online and batch programs. Under GnuCOBOL the LE date intrinsics must be replaced/stubbed — flagged as a migration risk (§7). |

## 3. Data inventory

### 3.1 Copybook → dataset → layout map

Copybooks in `app/cpy` define the record layouts. FB record lengths taken from
the README data table and confirmed against `DEFINE CLUSTER RECORDSIZE` in
`app/jcl`.

| Copybook | Entity | Rec len | Backing dataset (KSDS unless noted) |
| :------- | :----- | ------: | :---------------------------------- |
| `CVACT01Y` | Account master | 300 | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` |
| `CVACT02Y` | Card master | 150 | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` |
| `CVACT03Y` | Card/account xref | 50 | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` |
| `CVCUS01Y` | Customer master | 500 | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` |
| `CVTRA05Y` | Transaction (online) | 350 | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` |
| `CVTRA06Y` | Daily transaction | 350 | `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential) |
| `CVTRA01Y` | Tran category balance | 50 | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` |
| `CVTRA02Y` | Disclosure group | 50 | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` |
| `CVTRA03Y` | Transaction type | 60 | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` |
| `CVTRA04Y` | Transaction category | 60 | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` |
| `CSUSR01Y` | User security | 80 | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` |

### 3.2 VSAM clusters (from IDCAMS `DEFINE CLUSTER` in `app/jcl`)

`KEYS(len offset)` and `RECORDSIZE(avg max)` are as coded in the DEFINE
statements. All are `INDEXED` (KSDS).

| Cluster (`AWS.M2.CARDDEMO.*`) | Defined in JCL | Key len / offset | Rec size | Share |
| :---------------------------- | :------------- | :--------------- | -------: | :---- |
| `ACCTDATA.VSAM.KSDS` | `ACCTFILE.jcl` | 11 / 0 | 300 | (2 3) |
| `CARDDATA.VSAM.KSDS` (+AIX key 11/16) | `CARDFILE.jcl` | 16 / 0 | 150 | (2 3) |
| `CUSTDATA.VSAM.KSDS` | `CUSTFILE.jcl` | 9 / 0 | 500 | (2 3) |
| `CARDXREF.VSAM.KSDS` (+AIX key 11/25) | `XREFFILE.jcl` | 16 / 0 | 50 | (2 3) |
| `TRANSACT.VSAM.KSDS` (+AIX key 26/304) | `TRANFILE.jcl`, `TRANBKP.jcl` | 16 / 0 | 350 | (2 3) |
| `DISCGRP.VSAM.KSDS` | `DISCGRP.jcl` | 16 / 0 | 50 | (2 3) |
| `TCATBALF.VSAM.KSDS` | `TCATBALF.jcl` | 17 / 0 | 50 | (2 3) |
| `TRANCATG.VSAM.KSDS` | `TRANCATG.jcl` | 6 / 0 | 60 | (2 3) |
| `TRANTYPE.VSAM.KSDS` | `TRANTYPE.jcl` | 2 / 0 | 60 | (1 4) |
| `USRSEC.VSAM.KSDS` | `DUSRSECJ.jcl` | 8 / 0 | 80 | — |
| `TRXFL.VSAM.KSDS` (statement work file) | `CREASTMT.JCL` | 32 / 0 | 350 | (2 3) |

Alternate indexes (AIX + PATH): `TRANSACT.VSAM.AIX` (`TRANIDX.jcl`),
`CARDDATA.VSAM.AIX` (`CARDFILE.jcl`), `CARDXREF.VSAM.AIX` (`XREFFILE.jcl`).

### 3.3 Sequential (PS) datasets and GDGs

- PS input: `AWS.M2.CARDDEMO.DALYTRAN.PS` (+ `.INIT`), plus the seed files in
  `app/data/EBCDIC/AWS.M2.CARDDEMO.*.PS` (EBCDIC) and their ASCII equivalents in
  `app/data/ASCII/*.txt`.
- GDG bases (`DEFGDGB.jcl`, `REPTFILE.jcl`, `DALYREJS.jcl`):
  `TRANSACT.BKUP`, `TRANSACT.DALY`, `TRANSACT.COMBINED`, `TRANREPT`,
  `TCATBALF.BKUP`, `SYSTRAN`, `DALYREJS`.

## 4. Batch orchestration

The full batch cycle (README "Running full batch") runs these jobs in order.
The core posting cycle to reproduce in the cloud scheduler
(EventBridge/Step Functions, PR #8) is:

```
CLOSEFIL → POSTTRAN(CBTRN02C) → INTCALC(CBACT04C) → CREASTMT(CBSTM03A) → OPENFIL
```

CLOSEFIL/OPENFIL are CICS file open/close (IEFBR14 markers on M2); the compute
steps in between are the containerized GnuCOBOL programs. Full documented order
including load/backup steps: `CLOSEFIL → ACCTFILE → CARDFILE → XREFFILE →
CUSTFILE → TRANBKP → DISCGRP → TCATBALF → TRANTYPE → DUSRSECJ → POSTTRAN →
INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL`.

### 4.1 DD → dataset mapping for the core containerized programs

These map the mainframe DD names to the volume paths the container entrypoint
will mount (mirrors the M2 EFS mapping for `AWS.M2.CARDDEMO.*`).

**`CBTRN02C` (POSTTRAN)** — SELECT/ASSIGN in `app/cbl/CBTRN02C.cbl`:

| DD (ASSIGN) | Dataset | Access |
| :---------- | :------ | :----- |
| `DALYTRAN` | `DALYTRAN.PS` | SEQUENTIAL (in) |
| `TRANFILE` | `TRANSACT.VSAM.KSDS` | INDEXED RANDOM (key `FD-TRANS-ID`) |
| `XREFFILE` | `CARDXREF.VSAM.KSDS` | INDEXED RANDOM (key `FD-XREF-CARD-NUM`) |
| `ACCTFILE` | `ACCTDATA.VSAM.KSDS` | INDEXED RANDOM (key `FD-ACCT-ID`) |
| `TCATBALF` | `TCATBALF.VSAM.KSDS` | INDEXED RANDOM (key `FD-TRAN-CAT-KEY`) |
| `DALYREJS` | `DALYREJS(+1)` GDG | SEQUENTIAL (out) |

**`CBACT04C` (INTCALC):** `TCATBALF`, `XREFFILE` (+AIX PATH), `ACCTFILE`,
`DISCGRP` in; `TRANSACT` → `SYSTRAN(+1)` GDG out; date PARM `'YYYYMMDDHH'`.

**`CBSTM03A` (CREASTMT):** `TRNXFILE` (`TRXFL.VSAM.KSDS`), `XREFFILE`,
`ACCTFILE`, `CUSTFILE` in; `STMTFILE` (PS) + `HTMLFILE` out; calls `CBSTM03B`.

## 5. Current build chain

| Artifact type | Compile JCL | Compile PROC | Toolchain |
| :------------ | :---------- | :----------- | :-------- |
| Batch COBOL | `samples/jcl/BATCMP.jcl` | `samples/proc/BUILDBAT.prc` | `IGYCRCTL` (IBM Enterprise COBOL) compile → `HEWL` link-edit into LOADLIB |
| CICS online | `samples/jcl/CICCMP.jcl` | `samples/proc/BUILDONL.prc` | CICS translator (`DFHECP1$`) → COBOL compile → link |
| BMS maps | `samples/jcl/BMSCMP.jcl` | `samples/proc/BUILDBMS.prc` | BMS macro assembly → mapset load module + copybook |

`BUILDBAT.prc` is already parameterized by `MEM` (program name) and `HLQ`
(`AWS.M2`), copybooks from `&HLQ..CARDDEMO.CPY`, output to
`&HLQ..CARDDEMO.LOADLIB`. This one-program-parameter shape is what the
containerized build (`Dockerfile.batch`, PR #3) and CI (`build.yml`, PR #4)
reproduce with GnuCOBOL.

**Current manual deploy:** compile via JCL → `DFHCSDUP` to load
`app/csd/CARDDEMO.CSD` → `CEDA INSTALL` group/transactions/files →
`CEMT SET PROG(...) NEWCOPY` to activate. No automation, no rollback, no
environment promotion.

## 6. Gap analysis

| Capability | Current state | Target |
| :--------- | :------------ | :----- |
| CI / build | Manual JCL compile per program | `build.yml` compiles all `CB*` with GnuCOBOL on push/PR (PR #4) |
| CD / deploy | Manual DFHCSDUP/CEDA/CEMT NEWCOPY | `deploy.yml` publishes images to ECR + updates M2 app (PR #9) |
| Containerization | None | `containers/Dockerfile.batch` + `entrypoint.sh`, parameterized by program (PR #3, then #10..N) |
| Health checks | None | HTTP liveness/readiness wrapper in the container (PR #3) |
| Autoscaling | Fixed capacity | ECS/EKS target-tracking (CPU / queue depth) (PR #7) |
| IaC | None | Terraform under `infra/`: network+ECR (PR #5), storage (PR #6), compute (PR #7), M2+scheduler (PR #8) |
| Batch scheduling | Manual JCL submission | EventBridge / Step Functions reproducing the ordered cycle (PR #8) |
| Data platform | z/OS VSAM/PS | EFS/FSx for containerized batch; M2-managed datasets for online (PR #6) |

## 7. Migration risks / validation focus

- **Packed-decimal (`COMP-3`) & binary (`COMP`)**: verify GnuCOBOL byte layout
  matches the seed data; compare posting/interest/statement output against
  known-good baselines.
- **EBCDIC vs ASCII**: `app/data/EBCDIC/*` is EBCDIC; GnuCOBOL runs native
  (ASCII). Either convert seed data to ASCII (equivalents already exist in
  `app/data/ASCII`) or configure EBCDIC codepage handling. Must be decided
  per-service and validated.
- **VSAM emulation**: GnuCOBOL maps `ORGANIZATION INDEXED` to an ISAM-style
  file handler; alternate indexes and share options need explicit mapping.
- **LE date services**: `CSUTLDTC` calls `CEEDAYS`/`CEEDATE`; no LE under
  GnuCOBOL — provide a replacement date routine.
- **GDG semantics**: `(+1)` generation datasets become versioned files/paths on
  EFS; the scheduler/entrypoint must emulate generation rollover.

## 8. PR strategy and delivery waves

Work ships as small, independently reviewable PRs. **No PR mixes docs with
infra, and each new program gets its own PR.** `build.yml` must pass on every PR;
each PR cross-links to this document and the relevant playbook step.

| Wave | PR | Deliverable | Depends on |
| :--- | :- | :---------- | :--------- |
| 0 | #1 | This analysis + `README.md` index skeleton (docs only) | — |
| 0 | #2 | `MIGRATION_PLAYBOOK.md` + Devin web-app Playbook (docs / non-code) | #1 |
| 1 | #3 | `containers/` Dockerfile.batch + entrypoint + health check, wired to `CBTRN02C` | #1 |
| 1 | #4 | `.github/workflows/build.yml` GnuCOBOL compile + validation | #1 |
| 2 | #5 | `infra/` Terraform: VPC/networking + ECR | #3/#4 patterns |
| 2 | #6 | `infra/` Terraform: EFS/FSx storage for VSAM datasets | base module |
| 2 | #7 | `infra/` Terraform: ECS/EKS compute + target-tracking autoscaling | #5/#6 outputs |
| 2 | #8 | `infra/` Terraform: M2 app + EventBridge/Step Functions scheduler | base module |
| 3 | #9 | `.github/workflows/deploy.yml` (publish ECR + deploy M2) | #4, #5 |
| 3 | #10..N | One PR per additional containerized program (`CBACT04C`, `CBSTM03A`, …) | #3, #9 |

### 8.1 Parallelization callouts

- **Wave 0 is sequential** (playbook depends on this analysis); everything after
  Wave 0 fans out.
- **PR #3 (containers) and PR #4 (CI skeleton) are independent** — assign to
  different people.
- **IaC PRs #5–#8** share a base module but cover independent resource domains
  (network/registry, storage, compute, M2+scheduler) and can be authored
  concurrently.
- **Biggest win — per-program PRs #10..N are mutually independent** once the
  container pattern (#3) and CD (#9) exist. Many on-prem COBOL services can each
  be migrated by a separate agent/engineer simultaneously, each running the same
  Devin Playbook with a different program-name parameter.
- **Within a single service migration**: source extraction + copybook extraction
  + test-data prep run in parallel; compile → containerize → deploy → validate
  are sequential.
