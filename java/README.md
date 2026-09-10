# CardDemo — Java / Spring Boot

Modernized version of the CardDemo mainframe application. The COBOL, CICS, VSAM and JCL
artifacts under `app/` are left untouched; this directory is a standalone Maven module that
reproduces the same business behavior on Spring Boot.

## Build and run

```bash
cd java
mvn clean verify          # compile + unit tests
mvn spring-boot:run       # starts on http://localhost:8080 with an in-memory H2 database
```

On startup the sample VSAM extracts in `app/data/ASCII` are loaded into the database
(`carddemo.data.load-on-startup`, `carddemo.data.directory`). The H2 console is available at
`/h2-console`. For a persistent database run with `--spring.profiles.active=postgres` and set
`CARDDEMO_DB_URL`, `CARDDEMO_DB_USER`, `CARDDEMO_DB_PASSWORD`.

Demo sign-on credentials (seeded from `app/jcl/DUSRSECJ.jcl`): `ADMIN001` / `ADMIN001` (admin)
and `USER0001` / `USER0001` (regular user).

## Architecture mapping

| Mainframe construct | Java equivalent |
| :------------------ | :-------------- |
| VSAM KSDS files | JPA entities + Spring Data repositories (H2 dev, PostgreSQL prod) |
| Copybook record layouts | `com.carddemo.model.entity` / `com.carddemo.model.dto` classes |
| CICS COMMAREA (`COCOM01Y`) | `CardDemoCommarea` DTO held in the HTTP session |
| BMS maps (`app/bms`) | REST endpoints (JSON) |
| JCL batch jobs (`app/jcl`) | Spring Batch jobs |
| Zoned decimal / overpunch signs | `com.carddemo.util.CobolUtils` |

## Copybook → Java model

| Copybook | VSAM/PS file | Java class |
| :------- | :----------- | :--------- |
| `CSUSR01Y` | USRSEC | `model.entity.SecurityUser` |
| `CVACT01Y` | ACCTDATA | `model.entity.Account` |
| `CVACT02Y` | CARDDATA | `model.entity.Card` |
| `CVACT03Y` | CARDXREF | `model.entity.CardXref` |
| `CVCUS01Y` | CUSTDATA | `model.entity.Customer` |
| `CVTRA05Y` | TRANSACT | `model.entity.Transaction` |
| `CVTRA06Y` | DALYTRAN | `model.entity.DailyTransaction` |
| `CVTRA01Y` | TCATBALF | `model.entity.TransactionCategoryBalance` (+ `...Id`) |
| `CVTRA02Y` | DISCGRP | `model.entity.DisclosureGroup` (+ `...Id`) |
| `CVTRA03Y` | TRANTYPE | `model.entity.TransactionType` |
| `CVTRA04Y` | TRANCATG | `model.entity.TransactionCategory` (+ `...Id`) |
| `COCOM01Y` | CICS COMMAREA | `model.dto.CardDemoCommarea` |

PIC clause mapping: `PIC X(n)` → `String` (length preserved on the column), `PIC 9(n)` →
`Integer`/`Long` by digit count, `PIC S9(m)V99` → `BigDecimal` with matching precision/scale.

### Note on the sample account extract

`acctdata.txt` carries the disclosure group value (`A000000000`) in the `ACCT-ADDR-ZIP` slot and
leaves `ACCT-GROUP-ID` blank. The Java loader follows the `CVACT01Y` offsets exactly, so the
interest calculation falls back to the `DEFAULT` disclosure group just as `CBACT04C` does.

## Program → Java mapping

Batch programs (`app/cbl/CB*`) and online CICS programs (`app/cbl/CO*`) are migrated in
follow-up changes stacked on this foundation; each Java class carries a Javadoc reference to
its originating COBOL program.

| COBOL program | JCL | Java class | Batch job name |
| :------------ | :-- | :--------- | :------------- |
| `CBACT01C` (read account master) | `READACCT.jcl` | `batch.readers.ReadAccountJobConfig` | `readAccountJob` |
| `CBACT02C` (read card master) | `READCARD.jcl` | `batch.readers.ReadCardJobConfig` | `readCardJob` |
| `CBACT03C` (read card xref) | `READXREF.jcl` | `batch.readers.ReadXrefJobConfig` | `readXrefJob` |
| `CBCUS01C` (read customer master) | `READCUST.jcl` | `batch.readers.ReadCustomerJobConfig` | `readCustomerJob` |

Jobs never start on their own (`spring.batch.job.enabled=false`);
`batch.readers.SequentialReaderJobLauncher.launch("readAccountJob")` submits one by name, the
way a JCL `EXEC PGM=` step does. Each job streams its table in key order and logs every record
under the COBOL program name exactly as the program `DISPLAY`s it (labelled
`1100-DISPLAY-ACCT-RECORD` lines for CBACT01C, the raw fixed-width record for the others,
twice for CBACT03C/CBCUS01C). A failed read logs the program's read-error message, the
`9910-DISPLAY-IO-STATUS` line and `ABENDING PROGRAM`, and the job ends `FAILED` in place of
the CEE3ABD abend.
