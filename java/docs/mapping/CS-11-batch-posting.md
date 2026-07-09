# CS-11 — Batch transaction posting (CBTRN01-03C / POSTTRAN)

WAVE 3 (BATCH) of the CardDemo COBOL→Java migration. This document maps the **POSTTRAN** batch
job — COBOL programs
[`CBTRN01C`](../../../app/cbl/CBTRN01C.cbl),
[`CBTRN02C`](../../../app/cbl/CBTRN02C.cbl) and
[`CBTRN03C`](../../../app/cbl/CBTRN03C.cbl) — to a Spring Batch 5 job (`postTranJob`) and a
report job (`transactionReportJob`).

It builds on CS-1 (data model): the entities and repositories for `DailyTransaction`
(CVTRA06Y), `Transaction` (CVTRA05Y), `Account` (CVACT01Y), `Card`/`CardXref`
(CVACT02Y/CVACT03Y) and `TransactionCategoryBalance` (CVTRA01Y) already exist. See
[`CS-1-data-model.md`](CS-1-data-model.md).

## Scope

Everything added by CS-11 lives in the `com.carddemo.batch.posting` package (module
`carddemo-app`), which this wave owns, plus one Flyway migration for the reject store. No
changes were made under `app/` (COBOL/JCL/copybooks), to shared config/scaffolding, or to other
batch packages.

## Artifacts

| Layer | Location |
|-------|----------|
| Job / step wiring | `carddemo-app/.../batch/posting/PostTranJobConfig.java` |
| ItemProcessor (validate + compute) | `carddemo-app/.../batch/posting/TransactionPostingProcessor.java` |
| ItemWriter (persist + reject) | `carddemo-app/.../batch/posting/PostingItemWriter.java` |
| Processor outcome | `carddemo-app/.../batch/posting/PostingResult.java` |
| Rejection reasons | `carddemo-app/.../batch/posting/RejectReason.java` |
| Report (CBTRN03C) | `carddemo-app/.../batch/posting/TransactionReportService.java` |
| Reject table (Flyway) | `carddemo-domain/src/main/resources/db/migration/V1110__cs11_transaction_reject.sql` |
| Integration test | `carddemo-app/src/test/java/com/carddemo/batch/posting/PostTranJobTest.java` |

## Program → Java mapping

| COBOL program | Legacy role | Java |
|---------------|-------------|------|
| CBTRN01C | Open DALYTRAN, XREF, CARDXREF, ACCT files; read & validate each daily record | Subsumed by `postTranJob`'s reader + the processor's XREF/account lookups (its file opens/reads are the reader; its lookups are the processor). |
| CBTRN02C | POST each daily transaction: validate card→xref→account, credit-limit & expiration checks, write TRANSACT, update ACCT + TCATBAL, write rejects | `postTranJob` chunk step: `TransactionPostingProcessor` (validate + compute) → `PostingItemWriter` (persist / reject). |
| CBTRN03C | Daily transaction detail report over TRANSACT for a DATEPARM date range | `transactionReportJob` → `TransactionReportService`. |

## `postTranJob` — pipeline

```
RepositoryItemReader<DailyTransaction>   (findAll, order by dalytranId asc)
        │
        ▼
TransactionPostingProcessor              (CBTRN02C 1500-VALIDATE-TRAN + 2000-POST-TRANSACTION)
        │  PostingResult (posted | rejected)
        ▼
PostingItemWriter                        (CBTRN02C 2700/2800/2900 + 2500-WRITE-REJECT-REC)
```

Chunk size is **1**: each posted transaction commits before the next daily record is read, so
the account and category balances read for record *n+1* already include the effects of records
*1..n*. This reproduces the record-by-record accumulation of the sequential COBOL loop (a larger
chunk would let multiple in-flight updates to the same account clobber one another).

## Validation rules & rejection reasons (faithful port)

Evaluated in the order below (`1500-A-LOOKUP-XREF`, `1500-B-LOOKUP-ACCT`). `RejectReason` holds
the exact `WS-VALIDATION-FAIL-REASON` code and `...-DESC` literal.

| # | COBOL condition | Reason code | Description |
|---|-----------------|-------------|-------------|
| 1 | XREF read on `DALYTRAN-CARD-NUM` returns INVALID KEY | 100 | `INVALID CARD NUMBER FOUND` |
| 2 | ACCT read on `XREF-ACCT-ID` returns INVALID KEY | 101 | `ACCOUNT RECORD NOT FOUND` |
| 3 | `ACCT-CREDIT-LIMIT < WS-TEMP-BAL` | 102 | `OVERLIMIT TRANSACTION` |
| 4 | `ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)` | 103 | `TRANSACTION RECEIVED AFTER ACCT EXPIRATION` |

where, exactly as in COBOL:

```
WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT − ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
```

The credit-limit check (102) and expiration check (103) are evaluated one after the other on the
same record, so — matching the COBOL — a record that is both overlimit and expired is rejected
with **103** (the later assignment wins). The date comparison is a lexicographic compare of the
`yyyy-MM-dd` strings, valid because both are fixed-format ISO dates.

> Reason 109 (`ACCOUNT RECORD NOT FOUND` on the ACCT *rewrite*) cannot occur here: the same
> account is read at validation time (101) within the same run, so a missing account is always
> caught up front.

## Balance-update arithmetic (BigDecimal, exact scale 2)

All monetary values are `BigDecimal` with scale 2 (copybook `V99`); only `ADD`/`SUBTRACT` are
used (no COBOL `ROUNDED`), so results are exact.

**`2800-UPDATE-ACCOUNT-REC`**
```
ACCT-CURR-BAL              += DALYTRAN-AMT
IF DALYTRAN-AMT >= 0:  ACCT-CURR-CYC-CREDIT += DALYTRAN-AMT
ELSE:                  ACCT-CURR-CYC-DEBIT  += DALYTRAN-AMT
```

**`2700-UPDATE-TCATBAL`** — keyed on `(XREF-ACCT-ID, DALYTRAN-TYPE-CD, DALYTRAN-CAT-CD)`
```
found     → TRAN-CAT-BAL += DALYTRAN-AMT      (2700-B)
not found → TRAN-CAT-BAL  = 0 + DALYTRAN-AMT  (2700-A, create)
```

**`2000-POST-TRANSACTION`** copies every `DALYTRAN-*` field to the `TRAN-*` record (same 350-byte
layout, `TRAN-ID = DALYTRAN-ID`) and stamps `TRAN-PROC-TS` with the current DB2-format timestamp
(`yyyy-MM-dd-HH.mm.ss.SSSSSS`, 26 chars, per `Z-GET-DB2-FORMAT-TIMESTAMP`).

## Rejects

The legacy `2500-WRITE-REJECT-REC` writes the 350-byte transaction image plus an 80-byte trailer
(numeric reason + description) to the `DALYREJS` sequential file. The Java writer persists the
same information to a new relational table (queryable, no flat file):

`V1110__cs11_transaction_reject.sql` → `posting_transaction_reject`
`(reject_id, run_id, dalytran_id, tran_card_num, tran_type_cd, tran_cat_cd, tran_amt,
reject_reason_code, reject_reason_desc)`. `run_id` is the batch job-execution id.

## `transactionReportJob` — CBTRN03C

`TransactionReportService.generate(startDate, endDate)` reads `card_transaction` in id order,
keeps rows whose `TRAN-PROC-TS(1:10)` is within the inclusive date range, resolves the account id
via XREF (`1500-A`), the type description via `transaction_type` (`1500-B`) and the category
description via `transaction_category` (`1500-C`), then emits:

- a detail line per transaction (layout after `CVTRA07Y` `TRANSACTION-DETAIL-REPORT`),
- a **page total** every `PAGE-SIZE = 20` detail lines (`1110-WRITE-PAGE-TOTALS`),
- an **account total** on each card-number change (`1120-WRITE-ACCOUNT-TOTALS`),
- a final **grand total** (`1110-WRITE-GRAND-TOTALS`).

The step writes the rendered report to the `outputFile` job parameter and stashes the grand total
/ transaction count in the step execution context.

**Deviation:** the COBOL EOF branch adds the *stale* `TRAN-AMT` of the sentinel read into the
page/account totals and skips the last account total. This port flushes the final account total
cleanly and never double-counts, so the numeric totals are arithmetically correct; the report
column layout is reproduced approximately (labels/widths of `CVTRA07Y`) while the totals are
exact.

## Running

Both jobs are launched explicitly (the app boots with `spring.batch.job.enabled=false`); they are
not auto-started. `transactionReportJob` takes `startDate`, `endDate` and `outputFile` job
parameters.

## Tests

`PostTranJobTest` (H2, isolated in-memory DB) seeds accounts, cards, cross-references and daily
transactions, runs `postTranJob`, and asserts:

- valid transactions are posted to `card_transaction`; invalid ones are not;
- account A ends at `curr_bal = 270.00`, `cyc_credit = 250.00`, `cyc_debit = -10.00` after
  posting `+200.00` then `-30.00` (exact `BigDecimal`);
- category balance `(A, "01", 1000)` accumulates to `170.00` (created then updated);
- the four invalid records are rejected with reason codes **100 / 101 / 102 / 103**;
- `transactionReportJob` renders the report and produces the exact grand total `170.00`.
