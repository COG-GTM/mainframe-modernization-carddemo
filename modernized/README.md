# CardDemo Interest Calculator — Modernized (CBACT04C → Spring Boot)

A Spring Boot 3 / Java 17 transpilation of the CardDemo batch program
[`app/cbl/CBACT04C.cbl`](../app/cbl/CBACT04C.cbl), the monthly **interest calculator**. It reproduces
the COBOL business logic — reading transaction-category balances, looking up disclosure-group
interest rates, computing interest, writing interest transactions, and updating account balances —
on a relational database via Spring Data JPA, wrapped in a Spring Batch job that replaces
[`app/jcl/INTCALC.jcl`](../app/jcl/INTCALC.jcl).

## What was modernized and from where

| Mainframe artifact | Modern artifact |
|:-------------------|:----------------|
| `CBACT04C.cbl` (PROCEDURE DIVISION) | `service/InterestCalculationService.java` |
| `INTCALC.jcl` (`EXEC PGM=CBACT04C,PARM='2022071800'`) | `batch/InterestCalculationJobConfig.java` |
| `CVTRA01Y` TRAN-CAT-BAL-RECORD (TCATBALF) | `model/TranCatBalance.java` + `tran_cat_balance` |
| `CVACT03Y` CARD-XREF-RECORD (XREFFILE) | `model/CardXref.java` + `card_xref` (AIX → index) |
| `CVTRA02Y` DIS-GROUP-RECORD (DISCGRP) | `model/DisclosureGroup.java` + `disclosure_group` |
| `CVACT01Y` ACCOUNT-RECORD (ACCTFILE) | `model/Account.java` + `account` |
| `CVTRA05Y` TRAN-RECORD (TRANSACT) | `model/Transaction.java` + `transaction` |
| VSAM file I/O (READ/REWRITE/WRITE) | Spring Data JPA repositories (`repository/`) |
| `9999-ABEND-PROGRAM` (`CALL 'CEE3ABD'`) | `InterestCalculationException` (rolls back the step) |

## Paragraph-by-paragraph mapping

The COBOL paragraph name is preserved in each Java method's Javadoc for traceability.

| CBACT04C paragraph (lines) | Java method | Notes |
|:---------------------------|:------------|:------|
| PROCEDURE DIVISION main loop (180–222) | `calculateInterest(String)` | sequential TCATBALF scan, account grouping |
| `1000-TCATBALF-GET-NEXT` (325–348) | `TranCatBalanceRepository.findAllByOrderBy…` | ordered read of the driver file |
| `1050-UPDATE-ACCOUNT` (350–370) | `updateAccount` | `ACCT-CURR-BAL += WS-TOTAL-INT`; cycle credit/debit → 0; rewrite |
| `1100-GET-ACCT-DATA` (372–391) | `getAcctData` | random read of ACCTFILE by id |
| `1110-GET-XREF-DATA` (393–413) | `getXrefData` | xref read via account-id alternate index (CXACAIX) |
| `1200-GET-INTEREST-RATE` (415–440) | `getInterestRate` | disclosure-group read |
| `1200-A-GET-DEFAULT-INT-RATE` (443–460) | `getInterestRate` (fallback branch) | retry with `'DEFAULT'` group on status `'23'` |
| `1300-COMPUTE-INTEREST` (462–470) | `computeInterest` | `(TRAN-CAT-BAL * DIS-INT-RATE) / 1200` |
| `1300-B-WRITE-TX` (473–515) | `writeTransaction` | builds + writes the interest transaction |
| `1400-COMPUTE-FEES` (518–520) | `computeFees` | intentional no-op (COBOL stub) |
| `9999-ABEND-PROGRAM` | `abend` | throws to roll back the batch transaction |

### Key business rules preserved

- **Interest formula:** `monthlyInterest = (tranCatBal * intRate) / 1200`.
- **Truncation, not rounding:** COBOL `COMPUTE` has no `ROUNDED`, so the result is truncated to the
  2-decimal scale of `WS-MONTHLY-INT PIC S9(09)V99` (`RoundingMode.DOWN`).
- **Zero-rate skip:** `IF DIS-INT-RATE NOT = 0` — a zero rate writes no transaction.
- **DEFAULT fallback:** a missing disclosure group (COBOL file status `'23'`) retries with group
  `'DEFAULT'`.
- **Account flush:** interest accumulates per account (`WS-TOTAL-INT`) and is added to
  `ACCT-CURR-BAL` once, with `ACCT-CURR-CYC-CREDIT` / `ACCT-CURR-CYC-DEBIT` reset to 0, when the
  account changes and after the final record (the main loop's EOF `1050-UPDATE-ACCOUNT`).
- **Transaction id:** `PARM-DATE` (10) + a 6-digit running suffix, e.g. `2022071800000001`.
- **Interest transaction fields:** type `01`, category `0005`, source `System`, description
  `Int. for a/c <11-digit acctId>`, card number from the xref, merchant id `0`.

## COBOL → Java construct comparison

| COBOL construct | Java equivalent |
|:----------------|:----------------|
| `COPY <copybook>` | `import` of a `model` class |
| Record layout (`01`/`05` levels, PIC) | JPA `@Entity` with typed fields |
| `PIC S9(i)V99` (zoned decimal) | `BigDecimal` (scale 2) — never `double`/`float` |
| `PIC 9(n)` integer | `Long` / `Integer` |
| `PIC X(n)` | `String` |
| Multi-field `RECORD KEY` | `@IdClass` composite key |
| VSAM KSDS | table with primary key |
| Alternate index (AIX/PATH) | secondary index + finder method |
| `SELECT … ORGANIZATION INDEXED` + sequential READ | `repository.findAllByOrderBy…` |
| `READ … RECORD KEY` | `repository.findById(...)` |
| `REWRITE` / `WRITE` | `repository.save(...)` |
| `PERFORM <paragraph>` | method call |
| `COMPUTE … / 1200` | `BigDecimal.multiply(...).divide(1200, 2, DOWN)` |
| `STRING a b INTO c` | string concatenation / `String.format` |
| `CALL 'CEE3ABD'` (ABEND) | throw `InterestCalculationException` (tx rollback) |
| JCL `EXEC PGM=…,PARM=` | Spring Batch `Job` + `processingDate` job parameter |
| JCL step `COND=` sequencing | Spring Batch step flow |

## Project layout

```
modernized/
├── pom.xml
└── src
    ├── main
    │   ├── java/com/carddemo
    │   │   ├── CardDemoInterestApplication.java   # Spring Boot entry point
    │   │   ├── model/        # JPA entities (5 copybook record layouts)
    │   │   ├── repository/   # Spring Data JPA repositories (VSAM → tables)
    │   │   ├── service/      # InterestCalculationService (CBACT04C logic)
    │   │   └── batch/        # InterestCalculationJobConfig (INTCALC.jcl)
    │   └── resources
    │       ├── application.yml   # H2 (default) + PostgreSQL (postgres profile)
    │       └── schema.sql        # PostgreSQL DDL + sample data
    └── test/java/com/carddemo    # unit + batch-job tests
```

## How to run

Requires **JDK 17+** and Maven.

### Run the tests

```bash
cd modernized
mvn test
```

All tests use an in-memory H2 database; no external setup is needed.

### Run the batch job (H2, default profile)

The job runs automatically on startup; pass the processing date as a job parameter:

```bash
cd modernized
mvn spring-boot:run -Dspring-boot.run.arguments="processingDate=2022071800"
```

### Run against PostgreSQL (production profile)

1. Create the database and load schema + sample data:

   ```bash
   createdb carddemo
   psql -d carddemo -f src/main/resources/schema.sql
   ```

2. Build and run with the `postgres` profile (override connection via env vars if needed):

   ```bash
   mvn clean package
   java -jar target/carddemo-interest-calculator-1.0.0.jar \
       --spring.profiles.active=postgres \
       processingDate=2022071800
   # JDBC_URL / JDBC_USER / JDBC_PASSWORD environment variables override the defaults
   ```

Under the `postgres` profile, Hibernate is set to `validate` and `schema.sql` provides the schema
(`spring.sql.init.mode=always`); under the default H2 profile Hibernate creates the schema.

## Notes & scope

- Only `CBACT04C` is transpiled. The entity classes model the full copybook layouts used by the
  program; the other CardDemo programs are documented in [`../docs/`](../docs/) but not ported.
- `1400-COMPUTE-FEES` is a stub in the COBOL ("To be implemented") and is a no-op here.
- The sample data in `schema.sql` is derived from `app/data/ASCII/` with zoned-decimal signs decoded
  and a few non-zero demo balances/rates so a run produces visible interest.
