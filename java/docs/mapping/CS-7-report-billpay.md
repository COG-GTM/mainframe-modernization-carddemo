# CS-7 — Reports & bill pay (WAVE 3 ONLINE)

Ports two CICS online programs to Spring Boot service + REST endpoints, plugged into the CS-3
navigation framework as `ScreenHandler` beans:

- **`app/cbl/CORPT00C.cbl`** + map `app/bms/CORPT00.bms` — transaction **report request**
  (`POST /api/reports/transactions`).
- **`app/cbl/COBIL00C.cbl`** + map `app/bms/COBIL00.bms` — online **bill payment**
  (`POST /api/billpay`).

All code is additive and confined to `com.carddemo.web.report` / `com.carddemo.service.report`
and `com.carddemo.web.billpay` / `com.carddemo.service.billpay` (DTOs in the respective `.dto`
sub-packages). No shared class, `pom.xml`, `application.yml` or Flyway migration was changed —
the existing `card_transaction` / `account` schema (V2) already covers both features.

Conventions per `java/README.md`: fixed-width identifiers → `String` (leading zeros
preserved); `PIC X(n)` → `String`; monetary amounts → `BigDecimal` scale 2, all money maths in
`BigDecimal` with the COBOL truncating `COMPUTE` semantics (`RoundingMode.DOWN`).

---

## 1. Bill payment — `COBIL00C` → `POST /api/billpay`

Pays an account balance **in full**: creates a payment transaction for the whole current
balance and draws the balance down to zero.

### 1.1 BMS map `COBIL0A` → DTOs

| BMS field | PIC | Direction | Java (`web.billpay.dto`) |
|-----------|-----|-----------|--------------------------|
| `ACTIDIN` | X(11) | input | `BillPayRequest.accountId` |
| `CONFIRM` | X(01) | input | `BillPayRequest.confirm` (`Y`/`N`/blank) |
| `CURBAL`  | (display) | output | `BillPayResponse.currentBalance` (`BigDecimal`) |
| `ERRMSG`  | X(78) | output | `BillPayResponse.message` / `BillPayErrorResponse.message` |
| — | — | output | `newBalance`, `transactionId`, `paid`, `confirmationRequired` |

### 1.2 Logic (`BillPayService.pay`, from `PROCESS-ENTER-KEY`)

Reproduced in the exact COBOL order, so the first failing check wins:

1. `ACTIDINI = SPACES/LOW-VALUES` → **400** `Acct ID can NOT be empty...`
2. `EVALUATE CONFIRMI`
   - `'N'/'n'` → clear screen, no account read → **200** declined (no payment).
   - not `Y`/`N`/blank → **400** `Invalid value. Valid values are (Y/N)...`
3. `READ-ACCTDAT-FILE` (not found) → **404** `Account ID NOT found...`
4. `ACCT-CURR-BAL <= ZEROS` → **422** `You have nothing to pay...`
5. blank confirm (`CONF-PAY-NO`) → **200** show balance, `Confirm to make a bill payment...`
6. `'Y'/'y'` (`CONF-PAY-YES`):
   - `READ-CXACAIX-FILE` for the card (via `CardXref` by account) — not found → **404**.
   - Allocate `TRAN-ID`: highest existing numeric id **+1**, zero-padded to 16
     (`STARTBR`/`READPREV` → "max + 1"; empty file → `0000000000000001`).
   - Write the payment `Transaction` and `COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT`
     (→ `0.00`), all in one `@Transactional` unit (the CICS `WRITE TRANSACT` + `REWRITE
     ACCTDAT`).
   - **200** `Payment successful.  Your Transaction ID is <id>.` (two spaces, as the COBOL
     `STRING`).

### 1.3 Payment transaction field mapping (`COBIL00C` `MOVE`s → `Transaction`)

| COBOL | Value | `Transaction` field |
|-------|-------|---------------------|
| `TRAN-TYPE-CD` | `'02'` | `tranTypeCd` |
| `TRAN-CAT-CD` | `2` | `tranCatCd` |
| `TRAN-SOURCE` | `POS TERM` | `tranSource` |
| `TRAN-DESC` | `BILL PAYMENT - ONLINE` | `tranDesc` |
| `TRAN-AMT` | current balance | `tranAmt` |
| `TRAN-CARD-NUM` | xref card for the account | `tranCardNum` |
| `TRAN-MERCHANT-ID` | `999999999` | `tranMerchantId` |
| `TRAN-MERCHANT-NAME` | `BILL PAYMENT` | `tranMerchantName` |
| `TRAN-MERCHANT-CITY`/`-ZIP` | `N/A` | `tranMerchantCity`/`tranMerchantZip` |
| `TRAN-ORIG-TS`/`-PROC-TS` | now (`yyyy-MM-dd HH:mm:ss.SSSSSS`) | `tranOrigTs`/`tranProcTs` |

---

## 2. Transaction report request — `CORPT00C` → `POST /api/reports/transactions`

`CORPT00C` lets an operator pick a **Monthly / Yearly / Custom** report and, after a `(Y/N)`
confirmation, submits a batch job (JCL to the `JOBS` TDQ) that runs `CBTRN03C` to print the
`TRANREPT` dataset. This service reproduces the type resolution, date validations, messages and
the confirm flow, and — on confirmation — **generates the report inline** from `Transaction`
data following the same filter/group/total logic as `CBTRN03C`. CS-14 will wire the equivalent
batch scheduling.

### 2.1 BMS map `CORPT0A` → DTOs

| BMS field | Java (`web.report.dto`) |
|-----------|-------------------------|
| `MONTHLY`/`YEARLY`/`CUSTOM` radio flags | `ReportRequest.reportType` (`ReportType` enum) |
| `SDTMM`/`SDTDD`/`SDTYYYY` | `startMonth`/`startDay`/`startYear` |
| `EDTMM`/`EDTDD`/`EDTYYYY` | `endMonth`/`endDay`/`endYear` |
| `CONFIRM` | `confirm` |
| `ERRMSG` | `ReportResponse.message` / `ReportErrorResponse.message` |

### 2.2 Logic (`TransactionReportService.requestReport`)

1. No report type flagged → **400** `Select a report type to print report...`
2. Resolve the date range:
   - **Monthly**: first day of current month … its last day.
   - **Yearly**: `Jan 01` … `Dec 31` of the current year.
   - **Custom**: validate `SDT*`/`EDT*` in COBOL order, first failure wins (all **400**):
     empty field (`... Month/Day/Year can NOT be empty...`) → range/numeric
     (`... Not a valid Month/Day/Year...`, month ≤ 12, day ≤ 31) → calendar date
     (`CSUTLDTC` → `... Not a valid date...`).
3. `SUBMIT-JOB-TO-INTRDR` confirmation:
   - blank → **200** `Please confirm to print the <name> report...` (dates echoed back).
   - `'N'/'n'` → **200** declined (cleared).
   - `'Y'/'y'` → generate report, **200** `<name> report submitted for printing ...`.
   - other → **400** `"<x>" is not a valid value to confirm...`

### 2.3 Report generation vs. the batch `CBTRN03C` report

The batch report is produced by the `TRANREPT` proc: a SORT step filters `TRANSACT` on
`TRAN-PROC-TS` between `PARM-START-DATE`/`PARM-END-DATE` and orders by `TRAN-CARD-NUM`, then
`CBTRN03C` writes a 133-column flat file (copybook `CVTRA07Y`). `TransactionReportService`
reproduces the same content as structured JSON (`TransactionReport`):

| `CBTRN03C` / `CVTRA07Y` | Java |
|-------------------------|------|
| SORT `INCLUDE COND` on `TRAN-PROC-DT` ≥ start AND ≤ end | filter on `tranProcTs[0..10]` in `[start,end]` |
| `SORT FIELDS=(TRAN-CARD-NUM,A)` | order by `tranCardNum`, then `tranId` |
| `TRANSACTION-DETAIL-REPORT` line (id, acct via xref, type+desc, cat+desc, source, amount) | `TransactionReportLine` |
| `REPORT-ACCOUNT-TOTALS` (emitted when the card changes) | `accountTotals[]` (`AccountTotal` per contiguous card group) |
| `REPORT-PAGE-TOTALS` (every `WS-PAGE-SIZE` = 20 detail lines) | `pageTotals[]`, `pageSize` |
| `REPORT-GRAND-TOTALS` | `grandTotal` |
| `TRAN-TYPE-DESC` / `TRAN-CAT-TYPE-DESC` lookups (`TRANTYPE`/`TRANCATG`) | `TransactionTypeRepository` / `TransactionCategoryRepository` |
| account id from `CARDXREF` by card | `CardXrefRepository` |

**Differences to note (documented for CS-14):**

- The online endpoint returns the report **inline as JSON** instead of submitting JCL to the
  internal reader; formatting/pagination of the 133-column flat file (headers, `-` rules,
  edited `PIC` masks) is a batch-output concern and is represented structurally here.
- The seed loader populates the **daily** transaction table, not the online `card_transaction`
  table, so a freshly seeded database yields an empty report until online transactions exist
  (e.g. bill payments). CS-14 will drive report generation from the same `Transaction` data on
  a schedule.

---

## 3. Navigation integration (`ScreenHandler` beans)

| Program (`CardDemoProgram`) | Handler | Behaviour |
|-----------------------------|---------|-----------|
| `BILL_PAYMENT` (`CB00`/`COBIL00C`) | `BillPayScreenHandler` | first entry → blank screen; RE-ENTER → `BillPayService.pay`, `ScreenResult.stay(message, response)` (`RETURN TRANSID('CB00')`) |
| `REPORTS` (`CR00`/`CORPT00C`) | `ReportScreenHandler` | first entry → blank screen; RE-ENTER → `TransactionReportService.requestReport`, `ScreenResult.stay(...)` (`RETURN TRANSID('CR00')`) |

Both are `@Component` beans discovered by `ScreenRegistry`; PF3 (back to caller) is handled by
the framework's `NavigationController`.

---

## 4. Tests (`cd java && mvn -B verify`)

- `web/billpay/BillPayControllerTest` — prompt/confirm, exact balance draw-down to `0.00` +
  payment `Transaction` fields, zero-balance edge (`You have nothing to pay...`), declined,
  empty id (400), unknown account (404), invalid confirm (400), unauthenticated (401).
- `web/report/ReportControllerTest` — valid custom range (filtered records, grand/account
  totals, resolved descriptions), out-of-range → empty report, confirm prompt, monthly range,
  missing type (400), empty/invalid custom field (400), impossible date (400), invalid confirm.
- `session/Cs7ScreenHandlerTest` — both handlers registered against their programs and the
  ENTER → RE-ENTER turn.

All run against the seed data (`carddemo.seed.enabled=true`) with an authenticated principal
(`@WithMockUser`), each in a rolled-back transaction.
