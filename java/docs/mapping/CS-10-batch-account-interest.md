# CS-10 — Batch accounts & interest (CBACT01–04C, CBCUS01C / INTCALC)

WAVE 3 (BATCH) of the CardDemo COBOL→Java migration. This document maps the five batch account
programs under [`../../../app/cbl/`](../../../app/cbl/) to Spring Batch 5 jobs and records the
interest-formula mapping for the INTCALC job.

It builds on CS-1 (data model): the `Account`, `Card`, `CardXref`, `Customer`,
`TransactionCategoryBalance`, `DisclosureGroup` and `Transaction` entities + repositories already
exist. See [`CS-1-data-model.md`](CS-1-data-model.md). This wave depends **only** on the data
model.

All conventions follow [`java/README.md`](../../README.md#cobol-java-mapping): fixed-width,
zero-padded **identifiers** → `String`; monetary/interest (`PIC S9(n)Vnn`) →
`java.math.BigDecimal` with the copybook's implied scale; all interest/balance arithmetic in
`BigDecimal` with an explicit `RoundingMode`.

## Scope & conventions

Everything added by CS-10 lives in the `com.carddemo.batch.account` package (module
`carddemo-app`). No changes were made under `app/` (COBOL/JCL/BMS/copybooks), and no changes to
`pom.xml`, `application.yml`, shared scaffolding, the data model or other batch packages. No new
tables are required — the existing CS-1 tables are reused.

| Artifact | Location |
|----------|----------|
| Jobs / steps / reader / processor / writer / config | `carddemo-app/src/main/java/com/carddemo/batch/account/` |
| Tests | `carddemo-app/src/test/java/com/carddemo/batch/account/` |

## Program → job summary

| COBOL program | Purpose | Spring Batch job | Reader → Processor → Writer |
|---------------|---------|------------------|-----------------------------|
| [`CBACT01C`](../../../app/cbl/CBACT01C.cbl) | Read/print account master | `accountFileJob` | `RepositoryItemReader<Account>` (sort `acctId`) → `RecordFormatter.account` → `FlatFileItemWriter<String>` |
| [`CBACT02C`](../../../app/cbl/CBACT02C.cbl) | Read/print card file | `cardFileJob` | `RepositoryItemReader<Card>` (sort `cardNum`) → `RecordFormatter.card` → `FlatFileItemWriter<String>` |
| [`CBACT03C`](../../../app/cbl/CBACT03C.cbl) | Read/print card cross-reference | `xrefFileJob` | `RepositoryItemReader<CardXref>` (sort `xrefCardNum`) → `RecordFormatter.cardXref` → `FlatFileItemWriter<String>` |
| [`CBCUS01C`](../../../app/cbl/CBCUS01C.cbl) | Read/print customer file | `customerFileJob` | `RepositoryItemReader<Customer>` (sort `custId`) → `RecordFormatter.customer` → `FlatFileItemWriter<String>` |
| [`CBACT04C`](../../../app/cbl/CBACT04C.cbl) | **INTCALC** — monthly interest calculation | `intcalcJob` | `TransactionCategoryBalanceItemReader` → `AccountInterestProcessor` → `AccountInterestWriter` |

## Read/print jobs (CBACT01C, CBACT02C, CBACT03C, CBCUS01C)

Each COBOL program opens a VSAM KSDS with `ACCESS MODE IS SEQUENTIAL`, walks it in ascending key
order and `DISPLAY`s every record until end-of-file, printing `START`/`END OF EXECUTION` banners.

The Java equivalent is a chunk-oriented step:

* **Reader** — `RepositoryItemReader` over the CS-1 JPA repository, sorted ascending by the
  primary-key property (the VSAM key), reproducing the sequential key-order read.
* **Processor** — `RecordFormatter` renders one entity as a fixed-width **textual record image**
  (each field laid out at its copybook `PIC` width). This is a readable image of the record
  fields/order, not the raw EBCDIC/zoned-decimal bytes.
* **Writer** — `FlatFileItemWriter<String>` writes one line per record to the `outputFile` job
  parameter (default `target/cbact-output/<file>.txt`). A `StepExecutionListener` logs the
  COBOL `START`/`END OF EXECUTION OF PROGRAM …` banners and the record count.

## Interest job (CBACT04C / `intcalcJob`)

`CBACT04C` reads `TCATBAL` (transaction-category balances, a KSDS keyed on
`acct-id + type-cd + cat-cd`) sequentially. Because the file is in key order, all rows for one
account are contiguous; the program accumulates interest across an account's categories and, when
the account id changes (and at EOF), updates that account.

### Chain of paragraphs → Java

| COBOL paragraph | Java |
|-----------------|------|
| main loop reading `TCATBALF` in key order, grouping by `TRANCAT-ACCT-ID` | `TransactionCategoryBalanceItemReader` — loads `TCATBAL`, sorts by `(acctId, typeCd, catCd)`, groups consecutive rows into one `AccountInterestItem` (account + its balances) per account |
| `1100-GET-ACCT-DATA` (read `ACCTFILE` by `FD-ACCT-ID`) | `AccountRepository.findById` (missing → `IllegalStateException`, mirroring the COBOL abend) |
| `1110-GET-XREF-DATA` (read `XREFFILE` by acct id → card number) | `CardXrefRepository.findByXrefAcctId` (first match; card number stamped on each interest tx) |
| `1200-GET-INTEREST-RATE` / `1200-A-GET-DEFAULT-INT-RATE` | `DisclosureGroupRepository.findById(group, type, cat)`, falling back to group `DEFAULT` |
| `1300-COMPUTE-INTEREST` | `InterestCalculator.monthlyInterest` (see formula below) |
| `1300-B-WRITE-TX` | `AccountInterestProcessor#buildInterestTransaction` |
| `1050-UPDATE-ACCOUNT` (`ADD WS-TOTAL-INT TO ACCT-CURR-BAL`, reset cycle credit/debit, `REWRITE`) | `AccountInterestProcessor#process` tail + `AccountInterestWriter` |
| `1400-COMPUTE-FEES` | **not ported** — no-op in the COBOL source ("To be implemented") |

The processor emits an `AccountInterestResult` (updated account + interest transactions); the
`AccountInterestWriter` persists both (`REWRITE ACCOUNT-FILE` → `AccountRepository.save`,
`WRITE TRANSACT-FILE` → `TransactionRepository.saveAll`).

### Interest formula mapping

COBOL `1300-COMPUTE-INTEREST`:

```cobol
COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
ADD WS-MONTHLY-INT TO WS-TOTAL-INT
```

Java ([`InterestCalculator`](../../carddemo-app/src/main/java/com/carddemo/batch/account/InterestCalculator.java)):

```java
categoryBalance.multiply(annualRate)          // exact product (scale 4)
    .divide(new BigDecimal("1200"), 2, RoundingMode.DOWN);
```

| COBOL field | PIC | Java type / scale |
|-------------|-----|-------------------|
| `TRAN-CAT-BAL` | `S9(09)V99` | `BigDecimal` scale 2 |
| `DIS-INT-RATE` | `S9(04)V99` | `BigDecimal` scale 2 |
| `WS-MONTHLY-INT`, `WS-TOTAL-INT` | `S9(09)V99` | `BigDecimal` scale 2 |
| literal `1200` | — | `BigDecimal("1200")` (12 months × 100 for the percentage) |

**Rounding.** The `COMPUTE` has **no `ROUNDED` phrase**, so COBOL stores the quotient into the
scale-2 receiving field by **truncating** the excess fraction. This is reproduced with
`RoundingMode.DOWN` at scale 2. Truncating the exact quotient to two decimals is identical to
computing at higher precision and then storing into the `V99` field, so a single
`divide(1200, 2, DOWN)` matches the mainframe result exactly (e.g. `1234.56 × 18.00 / 1200 =
18.5184 → 18.51`, not `18.52`).

The account balance update `ACCT-CURR-BAL += WS-TOTAL-INT` is a plain scale-2 `BigDecimal.add`
(no rounding needed — both operands are scale 2). The current-cycle credit/debit are reset to
`0.00`.

### Interest transaction record (`1300-B-WRITE-TX`)

| Field | COBOL | Value |
|-------|-------|-------|
| `TRAN-ID` | `PARM-DATE` + sequence | `parmDate` job param + zero-padded 6-digit run counter |
| `TRAN-TYPE-CD` | `'01'` | `"01"` |
| `TRAN-CAT-CD` | `05` | `5` |
| `TRAN-SOURCE` | `'System'` | `"System"` |
| `TRAN-DESC` | `'Int. for a/c ' + ACCT-ID` | `"Int. for a/c " + acctId` |
| `TRAN-AMT` | `WS-MONTHLY-INT` | the category's monthly interest |
| `TRAN-MERCHANT-ID` | `0` (`9(09)`) | `"000000000"` |
| `TRAN-CARD-NUM` | from XREF | card number from `CardXref` |
| `TRAN-ORIG-TS`, `TRAN-PROC-TS` | DB2 timestamp | `YYYY-MM-DD-HH.mm.ss.SSSS00` |

A transaction is written per category **only when the disclosure-group rate is non-zero**
(COBOL `IF DIS-INT-RATE NOT = 0`); a zero balance with a non-zero rate still produces a `0.00`
transaction, matching the COBOL condition.

## Validation

`cd java && mvn -B verify` is green. Tests
([`carddemo-app/src/test/java/com/carddemo/batch/account/`](../../carddemo-app/src/test/java/com/carddemo/batch/account/)):

* **`InterestCalculatorTest`** — pins the formula and the truncation (`RoundingMode.DOWN`).
* **`InterestCalculationJobTest`** — runs `intcalcJob` against H2 with a hand-built fixture and
  asserts hand-computed `BigDecimal` results: account A (`100.00 + 18.51 + 10.00 = 128.51`, two
  interest transactions, zero-rate category skipped, cycle credit/debit reset), and account B via
  the `DEFAULT` disclosure group (`0.00 + 2.50 = 2.50`, one transaction).
* **`PrintFileJobTest`** — seeds H2 via `dataLoadJob` and asserts each read/print job writes
  exactly one output line per source record.
