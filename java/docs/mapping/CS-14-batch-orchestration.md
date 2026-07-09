# CS-14 — Batch orchestration (JCL → Spring Batch jobs/steps + scheduling)

WAVE 4 of the CardDemo COBOL→Java migration. This document maps the legacy JCL **processing
pipelines** (under [`../../../app/jcl/`](../../../app/jcl/) and the procedures in
[`../../../app/proc/`](../../../app/proc/)) to a Spring Batch **orchestration layer** that
sequences the already-built CS-10/CS-11/CS-12 jobs and replaces the mainframe utility steps
(IDCAMS, SORT, IEBGENER/REPRO, IEFBR14) with Java glue steps.

It builds on:

* **CS-10** — `intcalcJob`/`intcalcStep` (CBACT04C / INTCALC) and the print jobs. See
  [`CS-10-batch-account-interest.md`](CS-10-batch-account-interest.md).
* **CS-11** — `postTranJob`/`postTranStep` (CBTRN01C/CBTRN02C) and
  `transactionReportJob`/`transactionReportStep` (CBTRN03C). See
  [`CS-11-batch-posting.md`](CS-11-batch-posting.md).
* **CS-12** — `creastmtJob`/`createStatementStep` (CBSTM03A / CREASTMT). See
  [`CS-12-batch-statements.md`](CS-12-batch-statements.md).

All financial arithmetic is **delegated** to those jobs, so `BigDecimal` exactness is preserved
end-to-end; the orchestration layer adds no money math.

## Scope & conventions

Everything CS-14 adds lives in the **`com.carddemo.batch.orchestration`** package (module
`carddemo-app`). No changes were made under `app/` (COBOL/JCL/BMS/copybooks), to `pom.xml`,
`application.yml`, the scaffolding, the data model, or the existing CS-10/11/12 batch packages.
The pipeline jobs reuse the existing step beans by name (`@Qualifier`); Spring Batch allows a
step to participate in more than one job.

| Artifact | Location |
|----------|----------|
| Pipeline `Job` beans | `.../batch/orchestration/BatchPipelineConfig.java` |
| Glue `Step` beans (utility replacements) | `.../batch/orchestration/OrchestrationSteps.java` |
| Pipeline catalog (name → job bean → legacy JCL) | `.../batch/orchestration/BatchPipeline.java` |
| `JobLauncher` service | `.../batch/orchestration/BatchJobLauncherService.java` |
| REST launcher (`POST /api/batch/jobs/{name}`, `ROLE_ADMIN`) | `.../batch/orchestration/BatchJobController.java` |
| CLI runner | `.../batch/orchestration/BatchPipelineCommandLineRunner.java` |
| Cron scheduler (disabled by default) | `.../batch/orchestration/BatchPipelineScheduler.java` |
| Config properties (`carddemo.batch.orchestration.*`) | `.../batch/orchestration/BatchOrchestrationProperties.java` |
| Tests | `carddemo-app/src/test/java/com/carddemo/batch/orchestration/` |

## Auto-run behaviour

The application already boots with `spring.batch.job.enabled=false` (base
[`application.yml`](../../carddemo-app/src/main/resources/application.yml)), so **no job is
auto-run on startup**. CS-14 does not change that; the existing `CardDemoApplicationTests`
smoke test asserts the context starts cleanly. Pipelines run **only** when explicitly launched
(REST / CLI / scheduler). The CLI runner and the scheduler are each guarded by a
`@ConditionalOnProperty`, so they are absent unless opted in.

## Pipeline → Spring Batch job mapping

Each pipeline `Job` sequences its steps with `JobBuilder.start(...).next(...)`.

### POSTTRAN daily pipeline — `postTranPipelineJob`

Replaces POSTTRAN.jcl and its supporting jobs (TRANBKP.jcl, DALYREJS.jcl, TRANREPT.jcl),
sequenced **backup → post → reject handling → report**:

| # | Step (bean) | Legacy JCL step | Legacy PGM / utility |
|---|-------------|-----------------|----------------------|
| 1 | `backupTransactionsStep` | TRANBKP `STEP05R` (REPROC.prc) | IDCAMS REPRO (unload TRANSACT KSDS → backup) |
| 2 | `defineRejectsStep` | DALYREJS `STEP05` | IDCAMS DEFINE GENERATIONDATAGROUP (reject GDG) |
| 3 | **`postTranStep`** (CS-11) | POSTTRAN `STEP15` | **CBTRN02C** — post daily transactions |
| 4 | `handleRejectsStep` | (DALYREJS reject file consumption) | reads `posting_transaction_reject` count |
| 5 | **`transactionReportStep`** (CS-11) | TRANREPT `STEP10R` | **CBTRN03C** — daily transaction report |

### INTCALC monthly pipeline — `intcalcPipelineJob`

Replaces INTCALC.jcl + COMBTRAN.jcl:

| # | Step (bean) | Legacy JCL step | Legacy PGM / utility |
|---|-------------|-----------------|----------------------|
| 1 | **`intcalcStep`** (CS-10) | INTCALC `STEP15` | **CBACT04C** — compute & apply interest, write system transactions |
| 2 | `combineTransactionsStep` | COMBTRAN `STEP05R` + `STEP10` | SORT (merge BKUP + SYSTRAN) + IDCAMS REPRO (load into master) |

### CREASTMT monthly statement pipeline — `createStatementPipelineJob`

Replaces CREASTMT.JCL:

| # | Step (bean) | Legacy JCL step | Legacy PGM / utility |
|---|-------------|-----------------|----------------------|
| 1 | `initStatementFilesStep` | `DELDEF01` + `STEP030` | IDCAMS DELETE/DEFINE (TRXFL work KSDS) + IEFBR14 (delete prior reports) |
| 2 | **`createStatementStep`** (CS-12) | `STEP040` | **CBSTM03A** — statement generation (text + HTML) |

`STEP010`/`STEP020` (SORT + REPRO of TRANSACT into the `TRXFL` work KSDS keyed on card+tranid)
have **no Java step**: the CS-12 reader drives directly off the JPA repositories, so the work
file is unnecessary (documented no-op).

### TRANREPT report pipeline — `transactionReportPipelineJob`

Replaces the stand-alone TRANREPT.jcl:

| # | Step (bean) | Legacy JCL step | Legacy PGM / utility |
|---|-------------|-----------------|----------------------|
| 1 | `backupTransactionsStep` | `STEP05R` (REPROC.prc) | IDCAMS REPRO (unload) |
| 2 | **`transactionReportStep`** (CS-11) | `STEP10R` | **CBTRN03C** — report (the `SORT` filter/sort is folded into the report's date-range filtering) |

### PRTCATBL pipeline — `printCategoryBalancePipelineJob`

Replaces PRTCATBL.jcl:

| # | Step (bean) | Legacy JCL step | Legacy PGM / utility |
|---|-------------|-----------------|----------------------|
| 1 | `printCategoryBalanceStep` | `DELDEF` + `STEP05R` + `STEP10R` | IEFBR14 (delete) + REPRO (unload) + SORT/OUTREC (print TCATBAL sorted by acct/type/cat) |

## Mainframe utility → Java replacement

| Mainframe utility | Java replacement | Where |
|-------------------|------------------|-------|
| `IDCAMS DELETE` / `DEFINE CLUSTER` (VSAM lifecycle) | no-op — Flyway owns the relational schema; documented | `defineRejectsStep`, `initStatementFilesStep`, `combineTransactionsStep` |
| `IDCAMS DEFINE GENERATIONDATAGROUP` (reject GDG) | no-op — rejects persisted to `posting_transaction_reject` (CS-11 Flyway `V1110`) | `defineRejectsStep` |
| `IEBGENER` / `IDCAMS REPRO` (copy/unload) | read via repository → write flat file | `backupTransactionsStep`, `printCategoryBalanceStep` |
| `SORT` (order/filter) | sorted read (`Comparator`) / delegated date-range filter | `printCategoryBalanceStep`, `transactionReportStep` |
| `IEFBR14` (delete dataset) | delete output file if present | `initStatementFilesStep` |
| `EXEC PGM=CBxxxxC` (COBOL batch pgm) | reuse the CS-10/11/12 Spring Batch step | `postTranStep`, `transactionReportStep`, `intcalcStep`, `createStatementStep` |

## Launching pipelines

Pipelines are addressed by the short name in `BatchPipeline`: `posttran`, `intcalc`,
`creastmt`, `tranrept`, `prtcatbl`.

### REST (admin only)

```
GET  /api/batch/jobs            → list pipelines + the JCL each replaces
POST /api/batch/jobs/{name}     → launch synchronously; optional JSON body of job parameters
```

Both require `ROLE_ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`; method security is enabled
app-wide by the CS-9 `@EnableMethodSecurity`). Unauthenticated → `401`, `ROLE_USER` → `403`,
unknown pipeline → `404`. Example:

```
POST /api/batch/jobs/posttran
{ "startDate": "2024-01-01", "endDate": "2024-12-31" }
```

`BatchJobLauncherService` adds a unique identifying `run.id` parameter per launch (so a pipeline
can be re-run), and seeds `startDate`/`endDate`/`outputFile`/`parmDate` from the request or the
`BatchOrchestrationProperties` defaults.

### CLI

Set `carddemo.batch.orchestration.run=<name>` to run one pipeline at startup:

```
mvn -pl carddemo-app spring-boot:run \
    -Dspring-boot.run.arguments=--carddemo.batch.orchestration.run=intcalc
```

### Scheduling (disabled by default)

`BatchPipelineScheduler` (`@EnableScheduling` + `@Scheduled`) is only registered when
`carddemo.batch.orchestration.scheduling.enabled=true`. Cron expressions are configurable
(`post-tran-cron`, `intcalc-cron`, `create-statement-cron`); defaults are POSTTRAN 01:00 daily,
INTCALC and CREASTMT at 02:00/03:00 on the 1st of the month.

## Configuration properties (`carddemo.batch.orchestration.*`)

| Property | Default | Purpose |
|----------|---------|---------|
| `backup-dir` | `target/batch/backup` | TRANBKP unload output |
| `report-dir` | `target/batch/report` | TRANREPT / PRTCATBL output |
| `statement-dir` | `target/statements` | matches the CS-12 statement writer dir |
| `default-report-start-date` / `default-report-end-date` | `0000-01-01` / `9999-12-31` | report range when not supplied |
| `run` | (unset) | CLI: pipeline to run at startup |
| `scheduling.enabled` | `false` | master switch for the cron scheduler |
| `scheduling.post-tran-cron` / `intcalc-cron` / `create-statement-cron` | see above | cron expressions |

## Tests

* `PostTranPipelineJobTest` — launches `postTranPipelineJob` via `BatchJobLauncherService` on an
  isolated H2 DB with seeded accounts/cards/xrefs/daily transactions; asserts `COMPLETED`, two
  transactions posted, account `curr_bal = 270.00` / `cyc_credit = 250.00` / `cyc_debit = -10.00`,
  category balance `170.00`, and one reject with reason `100`.
* `IntcalcPipelineJobTest` — launches `intcalcPipelineJob`; asserts `COMPLETED`, interest applied
  (`110.00`, cycle buckets reset) and the one system interest transaction (`10.00`, source
  `System`) merged into `card_transaction`.
* `BatchJobControllerTest` — MockMvc: `401` unauthenticated, `403` for `ROLE_USER`, admin sees
  the pipeline catalog, and `404` for an unknown pipeline name.
