# Modernized Interest Calculator (CBACT04C → Java 21 / Spring Boot 3)

This module is a Java 21 / Spring Boot 3.x migration of the COBOL batch program
[`app/cbl/CBACT04C.cbl`](../app/cbl/CBACT04C.cbl) — the CardDemo monthly interest
calculation batch. It is implemented as a standalone `CommandLineRunner` module that
reads the original ASCII flat files from `app/data/ASCII/`, computes monthly interest
per transaction category balance, updates account balances, and writes interest
transactions to a fixed-width `transact.txt` output file (350-byte CVTRA05Y records).

## How to run

```bash
cd modernized
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64   # any JDK 21
mvn clean test            # 48 equivalence unit tests
mvn spring-boot:run       # runs the batch against ../app/data/ASCII
```

Configuration (env vars or `application.yml`):

| Property | Env var | Default |
|---|---|---|
| `carddemo.data-dir` | `CARDDEMO_DATA_DIR` | `../app/data/ASCII` |
| `carddemo.transact-output` | `CARDDEMO_TRANSACT_OUTPUT` | `target/transact.txt` |
| `carddemo.parm-date` | `CARDDEMO_PARM_DATE` | `2022-07-19` (COBOL `PARM-DATE` from the DALYDATE JCL parm) |

## COBOL paragraph → Java method mapping

All business logic lives in
`src/main/java/com/carddemo/interestcalc/service/InterestCalculationService.java`
(structured 1:1 against the COBOL paragraphs) and
`service/InterestCalculator.java` (the arithmetic core).

| CBACT04C paragraph | Java method | Notes |
|---|---|---|
| `MAIN-PARA` (open/loop/close) | `InterestCalculationJob.run` + `InterestCalculationService.run` | File OPENs become file loads; the `PERFORM UNTIL END-OF-FILE` loop is the `for` loop in `run` |
| `0000-TCATBALF-OPEN` | `AsciiFileLoader.loadTranCatBal` | |
| `0100-XREFFILE-OPEN` | `AsciiFileLoader.loadCardXrefs` | |
| `0200-DISCGRP-OPEN` | `AsciiFileLoader.loadDisclosureGroups` | |
| `0300-ACCTFILE-OPEN` | `AsciiFileLoader.loadAccounts` | |
| `0400-TRANFILE-OPEN` | `FlatFileTransactionWriter` constructor | |
| `1000-TCATBALF-GET-NEXT` | loop iteration over `List<TranCatBalRecord>` | Sequential READ |
| `1050-UPDATE-ACCOUNT` | `updateAccount()` | `ACCT-CURR-BAL += WS-TOTAL-INT`; cycle credit/debit reset to 0; REWRITE → `AccountRepository.update` |
| `1100-GET-ACCT-DATA` | `getAccountData(long)` | Keyed READ; not-found abends (`IllegalStateException`) |
| `1110-GET-XREF-DATA` | `getXrefData(long)` | Keyed READ via AIX (account id) |
| `1200-GET-INTEREST-RATE` | `getInterestRate(...)` | Keyed READ on (group, type, category) |
| `1200-A-GET-DEFAULT-INT-RATE` | fallback inside `getInterestRate` | File status `'23'` (not found) retries with group `'DEFAULT'` |
| `1300-COMPUTE-INTEREST` | `computeInterest(...)` + `InterestCalculator.monthlyInterest` | See arithmetic semantics below |
| `1300-B-WRITE-TX` | `writeTransaction(...)` | Builds the CVTRA05Y record |
| `1400-COMPUTE-FEES` | `computeFees()` | Intentional no-op (placeholder in COBOL too) |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `db2FormatTimestamp()` | `YYYY-MM-DD-HH.MM.SS.NNNNNN` |
| `9000-*-CLOSE` paragraphs | try-with-resources on `FlatFileTransactionWriter` | |
| `9999-ABEND-PROGRAM` / `Z-ABEND-PROGRAM` | exceptions (`IllegalStateException`, `UncheckedIOException`) | |

## Copybook → Java entity mapping

Each domain class documents its copybook fields in Javadoc. Byte offsets are the
0-based positions used by `AsciiFileLoader` against the ASCII seed files.

| Copybook | Record | Java class | Length |
|---|---|---|---|
| `CVTRA01Y` | `TRAN-CAT-BAL-RECORD` | `domain/TranCatBalRecord` | 50 |
| `CVTRA02Y` | `DIS-GROUP-RECORD` | `domain/DisclosureGroupRecord` | 50 |
| `CVACT01Y` | `ACCOUNT-RECORD` | `domain/AccountRecord` | 300 |
| `CVACT03Y` | `CARD-XREF-RECORD` | `domain/CardXrefRecord` | 50 |
| `CVTRA05Y` | `TRAN-RECORD` | `domain/TransactionRecord` | 350 |

Example — `CVTRA01Y` → `TranCatBalRecord`:

| COBOL field | PIC | Offset | Java field |
|---|---|---|---|
| `TRANCAT-ACCT-ID` | `9(11)` | 0–10 | `accountId: long` |
| `TRANCAT-TYPE-CD` | `X(02)` | 11–12 | `typeCode: String` |
| `TRANCAT-CD` | `9(04)` | 13–16 | `categoryCode: int` |
| `TRAN-CAT-BAL` | `S9(09)V99` | 17–27 | `balance: BigDecimal(scale=2)` |
| `FILLER` | `X(22)` | 28–49 | (not mapped) |

(See the Javadoc in each `domain/*.java` for the full per-field tables of the other
copybooks, including the five `S9(10)V99` money fields of `CVACT01Y`.)

Signed zoned decimal fields (`S9(n)V99`) use trailing sign overpunch in the ASCII data
(`{`/`A`–`I` positive, `}`/`J`–`R` negative); parsing/formatting is implemented and
unit-tested in `util/ZonedDecimal`.

## Arithmetic semantics (COBOL equivalence)

- All money values are `BigDecimal` with explicit scale 2 — no `float`/`double` anywhere.
- The interest formula is exactly CBACT04C's
  `COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200`.
  The COMPUTE has **no `ROUNDED` clause**, so COBOL truncates the result toward zero
  when storing into `S9(09)V99`. The Java equivalent is
  `balance.multiply(rate).divide(BigDecimal.valueOf(1200), 2, RoundingMode.DOWN)`
  (`RoundingMode.DOWN` = truncation toward zero, matching for negative values too).
- `WS-TOTAL-INT` accumulates per account and is added to `ACCT-CURR-BAL` once per
  control break on account id (and once at end-of-file for the final account).

## Behavioral notes / intentional divergences

1. **Final-account flush.** In CBACT04C the main loop's
   `ELSE PERFORM 1050-UPDATE-ACCOUNT` branch is unreachable dead code (the IF condition
   re-tests the control break inside the same iteration). The evident intent — rewriting
   the *last* account's balance after end-of-file — is implemented explicitly in Java as
   the post-loop `updateAccount()` call. This is the only deliberate divergence and is
   flagged with a comment in `InterestCalculationService.run`.
2. **Storage.** VSAM KSDS files are replaced by in-memory repositories
   (`repository/InMemory*`) loaded from the ASCII seed files; `REWRITE` maps to
   `AccountRepository.update`. The repository interfaces allow swapping in a database
   implementation later without touching the business logic.
3. **Timestamps.** `Z-GET-DB2-FORMAT-TIMESTAMP` is reproduced as
   `yyyy-MM-dd-HH.mm.ss.SS0000` from an injectable `java.time.Clock`
   (fixed in tests for determinism).
4. **Transaction IDs.** `TRAN-ID = PARM-DATE || WS-TRANID-SUFFIX(9(06))`; the suffix is
   global (never reset between accounts), matching the COBOL working-storage counter.

## Tests

`mvn clean test` runs 48 tests:

- `InterestCalculatorTest` — formula equivalence incl. truncation edge cases
  (`100.00 × 11.99 / 1200 → 0.99`, negative truncation toward zero, PIC-limit values).
- `InterestCalculationServiceTest` — end-to-end paragraph flow: control break,
  total-interest accumulation, DEFAULT disclosure-group fallback, zero-rate skip,
  cycle credit/debit reset, transaction field population, abend conditions.
- `ZonedDecimalTest` — sign-overpunch parse/format round-trips.
- `AsciiFileLoaderTest` — byte-offset verification against real seed records.
