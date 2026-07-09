# CS-12 — Batch statements (CBSTM03A / CBSTM03B / CREASTMT)

WAVE 3 (BATCH) of the CardDemo COBOL→Java migration. This document maps the legacy account
**statement generation** job (`CREASTMT`) to a Spring Batch job, `creastmtJob`.

Legacy sources (read-only, under [`../../../app/`](../../../app/)):

| Artifact | Role |
|----------|------|
| [`app/cbl/CBSTM03A.CBL`](../../../app/cbl/CBSTM03A.CBL) | Driver — reads accounts/customers/transactions and prints each statement in **plain text** and **HTML** |
| [`app/cbl/CBSTM03B.CBL`](../../../app/cbl/CBSTM03B.CBL) | File-handling subroutine (open/read/close of TRNXFILE / XREFFILE / CUSTFILE / ACCTFILE) |
| [`app/cpy/COSTM01.CPY`](../../../app/cpy/COSTM01.CPY) | `TRNX-RECORD` statement/transaction layout used for reporting |

All conventions follow [`java/README.md`](../../README.md#cobol-java-mapping): ids → `String`
(leading zeros preserved), monetary amounts → `java.math.BigDecimal` at the copybook's implied
scale, and **all money math is done in `BigDecimal`**.

## Artifacts

| Layer | Location |
|-------|----------|
| Job / step / reader beans | `carddemo-app/.../batch/statement/CreateStatementJobConfig.java` |
| ItemProcessor | `carddemo-app/.../batch/statement/CreateStatementItemProcessor.java` |
| ItemWriter (STMTFILE + HTMLFILE) | `carddemo-app/.../batch/statement/StatementItemWriter.java` |
| Layout renderer (COSTM01 / `ST-LINEnn` + HTML) | `carddemo-app/.../batch/statement/StatementFormatter.java` |
| Processed statement model | `carddemo-app/.../batch/statement/AccountStatement.java` |
| Test | `carddemo-app/src/test/java/com/carddemo/batch/statement/CreateStatementJobTest.java` |

No schema change: the job **reads only** existing tables (`account`, `customer`, `card_xref`,
`card_transaction`) created by CS-1 (`V2__data_model.sql`). No Flyway migration is added.

## Program → Spring Batch mapping

The COBOL program is one monolithic paragraph-driven flow using `ALTER`/`GO TO` and an in-memory
`WS-TRNX-TABLE` (transactions grouped by card). It is decomposed into the standard Spring Batch
reader → processor → writer chunk step:

| COBOL (CBSTM03A) | Java |
|------------------|------|
| `8400-ACCTFILE-OPEN` + driving loop `1000-MAINLINE` | `RepositoryItemReader<Account>` over `AccountRepository.findAll` (ordered by `acctId`) |
| `1000-XREFFILE-GET-NEXT` (XREF: card→cust→acct) | `CardXrefRepository.findByXrefAcctId` |
| `2000-CUSTFILE-GET` (`READ-K` by cust id) | `CustomerRepository.findById` |
| `3000-ACCTFILE-GET` (`READ-K` by acct id) | account is the reader item (already in hand) |
| `WS-TRNX-TABLE` build (`8100`/`8500-READTRNX-READ`) + `4000-TRNXFILE-GET` match loop | `TransactionRepository.findByTranCardNum` per card, in `CreateStatementItemProcessor` |
| `ADD TRNX-AMT TO WS-TOTAL-AMT` (COMP-3 `S9(9)V99`) | `BigDecimal` accumulation at scale 2 (`AccountStatement.totalAmount`) |
| `5000-CREATE-STATEMENT` / `6000-WRITE-TRANS` (write `ST-LINEnn`) | `StatementFormatter.toTextLines` |
| `5100`/`5200`/HTML writes | `StatementFormatter.toHtmlLines` |
| `STMT-FILE` (`STMTFILE`), `HTML-FILE` (`HTMLFILE`) | `StatementItemWriter` → `statements.txt`, `statements.html` |
| `CBSTM03B` (VSAM open/read/close) | Spring Data repositories (no separate I/O subroutine needed) |

`CBSTM03B` is a pure file-access shim over VSAM KSDS files; in Java the repositories provide the
equivalent keyed/sequential access, so it has no standalone Java counterpart.

### Driving granularity

The legacy driver emits one statement per **XREF record** (per card). CS-12 drives the reader
over **accounts** (per the task spec) and, in the processor, aggregates the transactions of all
of the account's cards into a single statement — one statement per account. With the seed data
(one card per account) the two are equivalent.

## Field mapping (COSTM01 `TRNX-RECORD` → statement)

| COBOL field | PIC | Source (Java) | Rendered field |
|-------------|-----|---------------|----------------|
| `TRNX-ID` | `X(16)` | `Transaction.tranId` | `ST-TRANID` (16) |
| `TRNX-DESC` | `X(100)` | `Transaction.tranDesc` | `ST-TRANDT` (truncated to 49) |
| `TRNX-AMT` | `S9(09)V99` | `Transaction.tranAmt` (`BigDecimal`) | `ST-TRANAMT` |
| `ACCT-ID` | `9(11)` | `Account.acctId` | `ST-ACCT-ID` (20) |
| `ACCT-CURR-BAL` | `S9(10)V99` | `Account.acctCurrBal` (`BigDecimal`) | `ST-CURR-BAL` |
| `CUST-FIRST/MIDDLE/LAST-NAME` | `X(25)` each | `Customer` | `ST-NAME` (75) |
| `CUST-ADDR-LINE-1/2/3` | `X(50)` | `Customer` | `ST-ADD1/2/3` |
| `CUST-ADDR-STATE-CD/COUNTRY-CD/ZIP` | `X(2)/X(3)/X(10)` | `Customer` | appended to `ST-ADD3` |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | `Customer.custFicoCreditScore` | `ST-FICO-SCORE` (20) |

### Numeric edited pictures

The plain-text output is byte-for-byte faithful to the COBOL edited pictures, rendered by
`StatementFormatter.editZoned`:

- `ST-CURR-BAL` — **`PIC 9(9).99-`**: 9 integer digits with leading zeros kept, `.`, 2 decimals,
  trailing sign position (space for `+`, `-` for negative). E.g. `194.00` → `000000194.00 `.
- `ST-TRANAMT` / `ST-TOTAL-TRAMT` — **`PIC Z(9).99-`**: leading zeros suppressed to spaces,
  trailing sign. E.g. `100.00` → `      100.00 `, `0.00` → `         .00 ` (with a `$` prefix in
  the line, as in COBOL).

Every plain-text line is exactly 80 columns (`FD-STMTFILE-REC PIC X(80)`). Text `MOVE`s into
`X(n)` fields are left-justified/space-padded; the `ST-NAME`/`ST-ADD3` `STRING ... DELIMITED BY
' '` concatenations are reproduced by taking each field's content up to its first space.

The HTML output reproduces the same table structure, labels, colours and "Bank of XYZ" / "Basic
Details" / "Transaction Summary" bands as the COBOL `HTML-*` lines, emitted as clean markup
(without the legacy 100-byte record padding).

## Output

`StatementItemWriter` writes both formats to a configurable directory
(`carddemo.batch.statement.output-dir`, default `target/statements`):

- `statements.txt` — the `STMTFILE` equivalent (all accounts concatenated)
- `statements.html` — the `HTMLFILE` equivalent

## Validation

`CreateStatementJobTest` (H2, `test` profile) runs `dataLoadJob` to load the seed data, adds two
transactions (`100.00` + `250.50`) for account `00000000001`'s card, runs `creastmtJob`, then
asserts the generated statement:

- total = `350.50` rendered as `$      350.50 ` (`Z(9).99-`);
- current balance `194.00` rendered as `000000194.00 ` (`9(9).99-`);
- FICO, cardholder name and both transaction lines present;
- every text line is exactly 80 columns;
- HTML contains the account header and a transaction row.
