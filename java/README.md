# CardDemo — Java migration

Java migration of the legacy **CardDemo** mainframe application (COBOL / CICS / JCL / VSAM /
BMS). This directory (`java/`) holds the Spring Boot + Spring Batch codebase that reproduces the
legacy behaviour on modern infrastructure. The original mainframe sources under
[`../app/`](../app/) are **read-only** and remain the source of truth that every migration wave
translates into Java.

The migration was delivered in four waves across scopes **CS-0 … CS-15**:

| Wave | Scopes | Delivered |
|------|--------|-----------|
| **WAVE 0** | CS-0 | Multi-module scaffolding, conventions, bootable skeleton |
| **WAVE 1** | CS-1 | Relational data model, Flyway schema, ASCII seed loader |
| **WAVE 2** | CS-2, CS-3 | Spring Security sign-on; session/navigation (COMMAREA) framework |
| **WAVE 3** | CS-4 … CS-13 | Online functions (accounts, cards, transactions, reports/bill-pay, menus, user-admin) and batch (account/interest, posting, statements, date util) |
| **WAVE 4** | CS-14, CS-15 | JCL→Spring Batch orchestration pipelines + launchers; **integration & numeric validation + this README** |

Detailed, per-scope field mappings and design notes live under
[`docs/mapping/`](docs/mapping/) — see the [full mapping index](docs/mapping/README.md) and the
[complete program/copybook/JCL → Java table](#complete-cobol--java-mapping) below.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
  - [Module layout](#module-layout)
  - [Layers & package ownership](#layers--package-ownership)
  - [CICS pseudo-conversational model → REST/session](#cics-pseudo-conversational-model--restsession)
  - [Numeric & money conventions](#numeric--money-conventions)
- [Build](#build)
- [Run the online application](#run-the-online-application)
- [Loading the seed data](#loading-the-seed-data)
- [Running the batch pipelines](#running-the-batch-pipelines-rest--cli--scheduler)
- [Profiles](#profiles)
- [Database migrations (Flyway)](#database-migrations-flyway)
- [Testing & validation](#testing--validation)
- [COBOL→Java type mapping rules](#coboljava-type-mapping-rules)
- [Complete COBOL → Java mapping](#complete-cobol--java-mapping)

---

## Tech stack

- Java 17
- Spring Boot 3.3.x (Web, Data JPA, Security, Batch)
- Spring Batch 5.x (via `spring-boot-starter-batch`)
- Maven multi-module build (parent: `spring-boot-starter-parent`)
- H2 (in-memory, PostgreSQL compatibility mode) for `dev`/`test`; PostgreSQL for `prod`
- Flyway for schema migrations
- Base package: `com.carddemo`

---

## Architecture

### Module layout

```
java/
├── pom.xml                 # parent POM (packaging=pom); manages versions
├── carddemo-domain/        # JPA entities + Spring Data repositories + schema + seed loader
│   └── src/main/java/com/carddemo/
│       ├── domain/         # @Entity classes (one per copybook record)
│       ├── repository/     # Spring Data JPA repositories
│       ├── batch/          # dataLoadJob (ASCII seed loader) reader/processor/writer
│       └── config/         # JpaConfig — owns @EntityScan / @EnableJpaRepositories
│   └── src/main/resources/
│       ├── db/migration/   # Flyway migrations (V1__…, V2__…, …)
│       └── seed/           # ASCII fixed-width seed files (from ../app/data/ASCII/)
└── carddemo-app/           # application module; depends on carddemo-domain
    └── src/main/java/com/carddemo/
        ├── CardDemoApplication.java   # @SpringBootApplication entry point
        ├── security/       # Spring Security config + COSGN00C sign-on/off (CS-2)
        ├── session/        # CICS COMMAREA + pseudo-conversational navigation (CS-3)
        ├── web/            # REST controllers + DTOs per online screen (CS-4..CS-9)
        ├── service/        # business services backing the controllers
        ├── batch/          # Spring Batch jobs/steps (CS-10..CS-13) + orchestration (CS-14)
        └── config/         # app-level configuration
    └── src/main/resources/application.yml   # profiles: dev / test / prod
```

### Layers & package ownership

| Package | Module | Responsibility | Scope |
|---------|--------|----------------|-------|
| `com.carddemo.domain` | carddemo-domain | JPA `@Entity` classes — one per copybook record | CS-1 |
| `com.carddemo.repository` | carddemo-domain | Spring Data JPA repositories (replace VSAM KSDS reads/browses) | CS-1 |
| `com.carddemo.batch` (domain) | carddemo-domain | `dataLoadJob` — ASCII seed loader (IDCAMS REPRO loads) | CS-1 |
| `com.carddemo.config` (`JpaConfig`) | carddemo-domain | Datasource/JPA config; Flyway migrations live here | CS-1 |
| `com.carddemo.security` | carddemo-app | Session-based auth, roles, sign-on/off | CS-2 |
| `com.carddemo.session` | carddemo-app | COMMAREA state + pseudo-conversational navigation | CS-3 |
| `com.carddemo.web.*` | carddemo-app | REST controllers + DTOs, one sub-package per screen family | CS-4..CS-9 |
| `com.carddemo.service.*` | carddemo-app | Business logic behind the controllers | CS-4..CS-9 |
| `com.carddemo.batch.*` | carddemo-app | Spring Batch jobs (account/interest, posting, statements) | CS-10..CS-13 |
| `com.carddemo.batch.orchestration` | carddemo-app | JCL→pipeline jobs + REST/CLI/scheduler launchers | CS-14 |
| `com.carddemo.util` | carddemo-app | Shared utilities (e.g. `DateValidator` for `CSUTLDTC`) | CS-13 |

### CICS pseudo-conversational model → REST/session

The legacy online application is **pseudo-conversational**: each CICS transaction reads the
`COCOM01Y` COMMAREA passed from the previous screen, does one unit of work, sends a map, and
returns to CICS. State is carried across turns in the COMMAREA; screen-to-screen routing is a
program that inspects the AID (PF key) and the COMMAREA and `XCTL`s to the next program.

The Java framework (CS-3) reproduces this faithfully over REST + an HTTP session:

| CICS concept | Java analogue |
|--------------|---------------|
| `COCOM01Y` COMMAREA (`CDEMO-*` fields) | `CardDemoCommarea` object stored in the `HttpSession` |
| Pseudo-conversational turn (receive map → process → send map) | one REST request/response against `com.carddemo.session.web.NavigationController` |
| AID / PF-key handling (`DFHENTER`, `DFHPF3`, …) | the navigation request's PF-key field applied by the screen handler |
| `EXEC CICS XCTL PROGRAM(...)` routing | `ScreenHandler` dispatch keyed by the target program/transaction |
| Sign-on (`COSGN00C`) establishing the session | `POST /api/auth/signon` authenticates + `POST /api/nav/signon` seeds the COMMAREA and routes to the menu |
| Menu routing (`COMEN01C`/`COADM01C`) | `MAIN_MENU` / `ADMIN_MENU` destinations in the navigation state |
| Screen "business" transactions (`COACTVWC`, `COTRN02C`, …) | dedicated REST controllers under `com.carddemo.web.*`, callable directly or via the navigation turn |

Individual screens are also exposed as first-class REST resources (e.g.
`GET /api/accounts/{id}`) so the functions are testable and usable independently of the
conversation, while `NavigationController` preserves the end-to-end sign-on → menu → function →
back flow.

### Numeric & money conventions

All monetary and interest values use `java.math.BigDecimal` at the **exact scale** of the
copybook's implied decimal (`V99` → scale 2) — never `double`/`float`. COBOL arithmetic
semantics are reproduced precisely, including truncation where the COBOL `COMPUTE` has no
`ROUNDED` phrase. The canonical example is the interest formula (`CBACT04C`):

```
COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200    (no ROUNDED → truncate)
```

reproduced by `InterestCalculator.monthlyInterest` as
`balance.multiply(rate).divide(new BigDecimal("1200"), 2, RoundingMode.DOWN)`. See
[Testing & validation](#testing--validation) for the exact expected constants verified against
the COBOL.

---

## Build

```bash
cd java
mvn -B verify        # compiles both modules and runs the full test suite
```

Requires JDK 17. CI runs `mvn -B -f java/pom.xml verify` on Temurin 17
(see [`.github/workflows/java-ci.yml`](../.github/workflows/java-ci.yml)).

## Run the online application

```bash
cd java
mvn -pl carddemo-app spring-boot:run
```

The app starts on `http://localhost:8080`. Verify it is up:

```bash
curl http://localhost:8080/api/health      # {"status":"UP"}
```

`GET /api/health` is public; all other endpoints require an authenticated session. Sign on with
a seed user (see below), e.g.:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/signon \
  -H 'Content-Type: application/json' \
  -d '{"userId":"ADMIN001","password":"PASSWORD"}'
```

Batch jobs do **not** run on a normal boot: `spring.batch.job.enabled=false` and every runner is
guarded by an explicit `@ConditionalOnProperty`, so the online app starts without launching any
job.

## Loading the seed data

The seed data are the legacy `app/data/ASCII/` unloads, packaged as classpath resources under
`carddemo-domain/src/main/resources/seed/`. They are loaded by CS-1's `dataLoadJob`
(`SeedDataLoadRunner`), which is only registered when `carddemo.seed.enabled=true`:

```bash
# load seed data at startup (idempotent)
mvn -pl carddemo-app spring-boot:run -Dspring-boot.run.arguments=--carddemo.seed.enabled=true
# or:  java -jar carddemo-app.jar --carddemo.seed.enabled=true
```

Seed users (password `PASSWORD` for all): admins `ADMIN001`–`ADMIN005` (type `A`), users
`USER0001`–`USER0005` (type `U`). Sample account `00000000001` (customer `000000001`, card
`9680294154603697`).

## Running the batch pipelines (REST / CLI / scheduler)

CS-14 maps each batch JCL job to a Spring Batch pipeline `Job`, catalogued in the
`BatchPipeline` enum and launched by `BatchJobLauncherService`. The launchable pipelines:

| Pipeline name | Job bean | Legacy JCL |
|---------------|----------|------------|
| `posttran` | `postTranPipelineJob` | POSTTRAN.jcl (+ TRANBKP, DALYREJS, TRANREPT) |
| `intcalc` | `intcalcPipelineJob` | INTCALC.jcl (+ COMBTRAN) |
| `creastmt` | `createStatementPipelineJob` | CREASTMT.JCL |
| `tranrept` | `transactionReportPipelineJob` | TRANREPT.jcl |
| `prtcatbl` | `printCategoryBalancePipelineJob` | (category-balance report) |

Three launch surfaces, all funnelling through the same service:

- **REST** (`BatchJobController`, `ROLE_ADMIN` only):
  ```bash
  curl -b cookies.txt http://localhost:8080/api/batch/jobs                 # list pipelines
  curl -b cookies.txt -X POST http://localhost:8080/api/batch/jobs/posttran \
    -H 'Content-Type: application/json' \
    -d '{"startDate":"2000-01-01","endDate":"2099-12-31"}'
  ```
- **CLI** (`BatchPipelineCommandLineRunner`, active only when the property is set):
  ```bash
  java -jar carddemo-app.jar --carddemo.batch.orchestration.run=intcalc
  ```
- **Scheduler** (`BatchPipelineScheduler`, active only when
  `carddemo.batch.orchestration.scheduling.enabled=true`): cron-driven launches for unattended
  runs.

## Profiles

Configured in `carddemo-app/src/main/resources/application.yml`. Default active profile is
`dev`. Select a profile with `SPRING_PROFILES_ACTIVE=<profile>` (or `-Dspring-boot.run.profiles`).

| Profile | Datasource | Notes |
|---------|-----------|-------|
| `dev`   | H2 in-memory (`MODE=PostgreSQL`) | default; Flyway migrates on startup |
| `test`  | H2 in-memory (`MODE=PostgreSQL`) | used by the test suite |
| `prod`  | PostgreSQL | via `CARDDEMO_DB_URL` / `CARDDEMO_DB_USERNAME` / `CARDDEMO_DB_PASSWORD` |

H2 runs in PostgreSQL compatibility mode so Flyway migrations authored for `prod` also apply
cleanly in `dev`/`test`.

## Database migrations (Flyway)

Migrations live in `carddemo-domain/src/main/resources/db/migration`, named
`V<n>__<description>.sql`, and are **PostgreSQL-compatible** (the `prod` target) so they also run
on H2 in PostgreSQL mode. Each backs one or more copybook/VSAM records.

## Testing & validation

`mvn -B verify` runs the full suite: per-module unit/slice tests plus the CS-15 end-to-end and
numeric-validation tests under [`com.carddemo.e2e`](carddemo-app/src/test/java/com/carddemo/e2e).

- **Online golden path** (`OnlineGoldenPathE2ETest`): a single `@SpringBootTest` conversation
  with a persisted session — sign-on → menu routing → account view → card list/detail →
  add/view transaction → bill payment → report → admin user CRUD — asserting HTTP status and
  COMMAREA/session behaviour, plus a regular-user path that is denied the admin functions.
- **Batch golden path** (`BatchGoldenPathE2ETest`): launches `posttran` → `intcalc` →
  `creastmt` through `BatchJobLauncherService`, asserting each reaches `COMPLETED` and the exact
  chained DB state (balances, cycle buckets, category balances, statement totals).
- **Numeric validation** (`InterestNumericValidationTest`,
  `PostingRejectNumericValidationE2ETest`, and the batch golden path): exact `BigDecimal`
  results asserted against COBOL-derived constants — interest truncation, posting arithmetic,
  reject reason codes 100/101/102/103 and the overlimit boundary. See
  [`docs/mapping/CS-15-integration-validation.md`](docs/mapping/CS-15-integration-validation.md)
  for every expected value and its COBOL citation.

## COBOL→Java type mapping rules

These conventions are **binding for all migration waves**. The exhaustive, per-scope field
tables live under [`docs/mapping/`](docs/mapping/); this is the canonical summary of the rules.

| COBOL PIC / usage | Java type | Notes |
|-------------------|-----------|-------|
| `PIC 9(n)`, `n <= 9` (numeric, used in arithmetic) | `Integer` | |
| `PIC 9(n)`, `n > 9` (numeric, used in arithmetic) | `Long` | |
| Fixed-width numeric ID with leading zeros, **never** used in arithmetic (account/card/customer id) | `String` | Preserve leading zeros / exact width |
| `PIC X(n)` | `String` (length `n`) | Preserve the field length in a comment |
| `PIC S9(n)V99` / implied-decimal / COMP-3 monetary | `java.math.BigDecimal` | Exact scale (`V99` → 2). All money/interest arithmetic uses `BigDecimal` |
| `COMP` / binary | `Long` / `Integer` | Size per the field width |

Additional rules:

- **Never use `double`/`float`** for money or interest — `BigDecimal` with the copybook's scale
  and an explicit `RoundingMode` on every division/scaling.
- IDs (account/card/customer) are fixed-width, zero-padded identifiers — map to `String`.
- Record the source copybook field length/PIC in a comment beside each Java field, e.g.
  `// ACCT-ID PIC 9(11) — 11-char zero-padded id`.

## Complete COBOL → Java mapping

Every legacy artifact and its Java counterpart. "Scope doc" links to the detailed per-scope
mapping under [`docs/mapping/`](docs/mapping/).

### Online programs (CICS / BMS)

| COBOL program | Function | Java component(s) | Scope doc |
|---------------|----------|-------------------|-----------|
| `COSGN00C` | Sign-on / sign-off | `security.AuthController`, `security` config | [CS-2](docs/mapping/CS-2-security.md) |
| `COMEN01C` | Main menu | `web.menu.MenuController` (`GET /api/menu`) | [CS-8](docs/mapping/CS-8-menus.md) |
| `COADM01C` | Admin menu | `web.menu.MenuController` (`GET /api/menu/admin`) | [CS-8](docs/mapping/CS-8-menus.md) |
| `COACTVWC` | Account view | `web.account.AccountController` (`GET /api/accounts/{id}`) | [CS-4](docs/mapping/CS-4-accounts.md) |
| `COACTUPC` | Account update | `web.account.AccountController` (`PUT /api/accounts/{id}`) | [CS-4](docs/mapping/CS-4-accounts.md) |
| `COCRDLIC` | Card list | `web.card.CardController` (`GET /api/cards`) | [CS-5](docs/mapping/CS-5-cards.md) |
| `COCRDSLC` | Card detail | `web.card.CardController` (`GET /api/cards/{num}`) | [CS-5](docs/mapping/CS-5-cards.md) |
| `COCRDUPC` | Card update | `web.card.CardController` (`PUT /api/cards/{num}`) | [CS-5](docs/mapping/CS-5-cards.md) |
| `COTRN00C` | Transaction list | `web.transaction.TransactionController` (`GET /api/transactions`) | [CS-6](docs/mapping/CS-6-transactions-online.md) |
| `COTRN01C` | Transaction view | `web.transaction.TransactionController` (`GET /api/transactions/{id}`) | [CS-6](docs/mapping/CS-6-transactions-online.md) |
| `COTRN02C` | Transaction add | `web.transaction.TransactionController` (`POST /api/transactions`) | [CS-6](docs/mapping/CS-6-transactions-online.md) |
| `CORPT00C` | Transaction report request | `web.report.ReportController` (`POST /api/reports/transactions`) | [CS-7](docs/mapping/CS-7-report-billpay.md) |
| `COBIL00C` | Bill payment | `web.billpay.BillPayController` (`POST /api/billpay`) | [CS-7](docs/mapping/CS-7-report-billpay.md) |
| `COUSR00C` | User list | `web.useradmin.UserAdminController` (`GET /api/admin/users`) | [CS-9](docs/mapping/CS-9-user-admin.md) |
| `COUSR01C` | User add | `web.useradmin.UserAdminController` (`POST /api/admin/users`) | [CS-9](docs/mapping/CS-9-user-admin.md) |
| `COUSR02C` | User update | `web.useradmin.UserAdminController` (`PUT /api/admin/users/{id}`) | [CS-9](docs/mapping/CS-9-user-admin.md) |
| `COUSR03C` | User delete | `web.useradmin.UserAdminController` (`DELETE /api/admin/users/{id}`) | [CS-9](docs/mapping/CS-9-user-admin.md) |

### Batch programs

| COBOL program | Function | Java component(s) | Scope doc |
|---------------|----------|-------------------|-----------|
| `CBACT01C` | Account master print | account read/report steps (`batch.account`) | [CS-10](docs/mapping/CS-10-batch-account-interest.md) |
| `CBACT02C` | Card master print | card read/report steps (`batch.account`) | [CS-10](docs/mapping/CS-10-batch-account-interest.md) |
| `CBACT03C` | Xref master print | xref read/report steps (`batch.account`) | [CS-10](docs/mapping/CS-10-batch-account-interest.md) |
| `CBACT04C` | Interest calculation (INTCALC) | `batch.account.InterestCalculator`, `AccountInterestProcessor`, `intcalcStep` | [CS-10](docs/mapping/CS-10-batch-account-interest.md) |
| `CBCUS01C` | Customer master print | customer read/report step (`batch.account`) | [CS-10](docs/mapping/CS-10-batch-account-interest.md) |
| `CBTRN01C` | Daily transaction validation/report | posting validation/report steps (`batch.posting`) | [CS-11](docs/mapping/CS-11-batch-posting.md) |
| `CBTRN02C` | Transaction posting | `batch.posting.TransactionPostingProcessor`, `RejectReason`, `postTranStep` | [CS-11](docs/mapping/CS-11-batch-posting.md) |
| `CBTRN03C` | Transaction detail report | `batch.posting` report step (`transactionReportStep`) | [CS-11](docs/mapping/CS-11-batch-posting.md) |
| `CBSTM03A` | Statement generation (driver) | `batch.statement.CreateStatementItemProcessor`, `StatementFormatter`, `StatementItemWriter`, `createStatementStep` | [CS-12](docs/mapping/CS-12-batch-statements.md) |
| `CBSTM03B` | Statement I/O subprogram (XREF/CUST/TRNX reads) | repository reads inside `CreateStatementItemProcessor` | [CS-12](docs/mapping/CS-12-batch-statements.md) |
| `CSUTLDTC` | Date validation (CEEDAYS wrapper) | `util.DateValidator` | [CS-13](docs/mapping/CS-13-util-date.md) |

### Copybooks

| Copybook | Content | Java component(s) | Scope doc |
|----------|---------|-------------------|-----------|
| `CVACT01Y` | Account record | `domain.Account` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVACT02Y` | Card record | `domain.Card` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVACT03Y` | Card xref record | `domain.CardXref` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVCUS01Y` | Customer record | `domain.Customer` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA01Y` | Transaction category balance | `domain.TransactionCategoryBalance` (+ `…Id`) | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA02Y` | Disclosure group / interest rate | `domain.DisclosureGroup` (+ `…Id`) | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA03Y` | Account grouping | grouping fields on `domain.Account` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA04Y` | Daily transaction record | `domain.DailyTransaction` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA05Y` | Transaction record | `domain.Transaction` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA06Y` | Transaction type | `domain.TransactionType` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVTRA07Y` | Transaction category | `domain.TransactionCategory` | [CS-1](docs/mapping/CS-1-data-model.md) |
| `CVCRD01Y` | Online card work fields | `web.card` DTOs / state | [CS-5](docs/mapping/CS-5-cards.md) |
| `CSUSR01Y` | Security user record | `domain.SecurityUser` | [CS-1](docs/mapping/CS-1-data-model.md) / [CS-2](docs/mapping/CS-2-security.md) |
| `COCOM01Y` | COMMAREA (`CDEMO-*`) | `session.CardDemoCommarea` | [CS-3](docs/mapping/CS-3-session-navigation.md) |
| `COMEN02Y` | Main-menu options table | `web.menu` option model | [CS-8](docs/mapping/CS-8-menus.md) |
| `COADM02Y` | Admin-menu options table | `web.menu` option model (admin) | [CS-8](docs/mapping/CS-8-menus.md) |
| `COSTM01` | Statement layout | `batch.statement.StatementFormatter` (text/HTML) | [CS-12](docs/mapping/CS-12-batch-statements.md) |
| `CSUTLDPY` | Date-validation procedure copybook | `util.DateValidator` logic | [CS-13](docs/mapping/CS-13-util-date.md) |
| `CSUTLDWY` | Date-validation working storage | `util.DateValidator` fields/messages | [CS-13](docs/mapping/CS-13-util-date.md) |

### BMS maps

Each online program's BMS map (`app/bms/*.bms`) maps to the request/response **DTOs** of its
Java controller (the map's input fields → request DTO, output fields → response DTO):

| BMS map | Screen | Java DTOs (package) |
|---------|--------|---------------------|
| `COSGN00` | Sign-on | `security` sign-on request/response |
| `COMEN01` / `COADM01` | Main / admin menu | `web.menu.dto.MenuResponse`, `MenuOptionDto` |
| `COACTVW` / `COACTUP` | Account view / update | `web.account.dto.*` |
| `COCRDLI` / `COCRDSL` / `COCRDUP` | Card list / detail / update | `web.card.dto.*` |
| `COTRN00` / `COTRN01` / `COTRN02` | Transaction list / view / add | `web.transaction.dto.*` |
| `CORPT00` | Report request | `web.report.dto.*` |
| `COBIL00` | Bill payment | `web.billpay.dto.*` |
| `COUSR00` / `COUSR01` / `COUSR02` / `COUSR03` | User list / add / update / delete | `web.useradmin.dto.*` |

### JCL jobs

| JCL job | Purpose | Java pipeline / component | Scope doc |
|---------|---------|---------------------------|-----------|
| `POSTTRAN.jcl` | Post daily transactions | `posttran` → `postTranPipelineJob` | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `TRANBKP.jcl` | Backup TRANSACT (REPRO) | `backupTransactionsStep` (in `posttran`) | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `DALYREJS.jcl` | Define/handle reject file | `defineRejectsStep` / `handleRejectsStep` (in `posttran`) | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `TRANREPT.jcl` | Transaction report | `tranrept` → `transactionReportPipelineJob` / `transactionReportStep` | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `INTCALC.jcl` | Interest calculation | `intcalc` → `intcalcPipelineJob` (`CBACT04C`) | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `COMBTRAN.jcl` | Combine system transactions (SORT/REPRO) | `combineTransactionsStep` (in `intcalc`) | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| `CREASTMT.JCL` | Generate statements | `creastmt` → `createStatementPipelineJob` (`CBSTM03A`) | [CS-14](docs/mapping/CS-14-batch-orchestration.md) |
| Seed/load JCLs (IDCAMS REPRO loads) | Load VSAM master/reference files | `dataLoadJob` (`carddemo-domain` `batch`) | [CS-1](docs/mapping/CS-1-data-model.md) |
