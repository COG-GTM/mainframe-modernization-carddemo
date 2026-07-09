# CS-6 — Online Transactions (COTRN00C / COTRN01C / COTRN02C)

Wave 3 online port of the three transaction screens: **list** (`COTRN00C`), **view**
(`COTRN01C`) and **add** (`COTRN02C`). All new code lives under
`com.carddemo.web.transaction` (REST + DTOs + `ScreenHandler` beans) and
`com.carddemo.service.transaction` (business logic + exceptions); nothing else is modified.

## Source artifacts

| COBOL | BMS map | Purpose |
|-------|---------|---------|
| `app/cbl/COTRN00C.cbl` | `app/bms/COTRN00.bms` (`COTRN0A`) | Browse/list transactions, 10 rows/page, PF7/PF8 paging, row select `S` → view |
| `app/cbl/COTRN01C.cbl` | `app/bms/COTRN01.bms` (`COTRN1A`) | View one transaction by id |
| `app/cbl/COTRN02C.cbl` | `app/bms/COTRN02.bms` (`COTRN2A`) | Add a transaction with full field validation |
| copybook `CVTRA05Y` | — | `TRAN-RECORD` layout (mapped to `Transaction` / `card_transaction`) |

## REST surface (`TransactionController`, base `/api/transactions`)

| Method / path | Screen | Behaviour |
|---------------|--------|-----------|
| `GET /api/transactions?startTranId=&pageSize=` | COTRN00 | Page of transactions ordered by `TRAN-ID` starting GTEQ `startTranId` (blank = from top); `pageSize` defaults to 10. Returns `TransactionListResponse` (rows + `firstTranId`/`lastTranId`/`moreRecords` paging anchors). |
| `GET /api/transactions/{tranId}` | COTRN01 | `TransactionDetailDto`; 404 when not found. |
| `POST /api/transactions` | COTRN02 | Validate + add; `201 Created` with `TransactionAddResponse` (generated id + COBOL success line). |

All endpoints require an authenticated principal (CS-2 security). The same logic is reachable
through the CS-3 navigation framework via the registered `ScreenHandler` beans
(`TRANSACTION_LIST` = `COTRN00C`/`CT00`, `TRANSACTION_VIEW` = `COTRN01C`/`CT01`,
`TRANSACTION_ADD` = `COTRN02C`/`CT02`) dispatched by `NavigationController` at `/api/nav`.

Errors carry the **verbatim COBOL screen message**: `400` for validation
(`TransactionValidationException`), `404` for a missing transaction
(`TransactionNotFoundException`). The exception handler is controller-local — no global
`@ControllerAdvice` is added.

## Field mapping (BMS ⇄ DTO ⇄ `CVTRA05Y`)

| BMS field | Copybook (`TRAN-…`) | PIC | Java (DTO / entity) | Type |
|-----------|---------------------|-----|---------------------|------|
| `TRNID` / `TRNIDIN` | `TRAN-ID` | X(16) | `tranId` | `String` (zero-padded, no arithmetic) |
| `CARDNUM` / `CARDNIN` | `TRAN-CARD-NUM` | X(16) | `cardNum` | `String` |
| `TTYPCD` | `TRAN-TYPE-CD` | X(02) | `typeCd` | `String` |
| `TCATCD` | `TRAN-CAT-CD` | 9(04) | `categoryCd` | `Integer` |
| `TRNSRC` | `TRAN-SOURCE` | X(10) | `source` | `String` |
| `TRNAMT` | `TRAN-AMT` | S9(09)V99 | `amount` | `BigDecimal` scale 2 |
| `TDESC` | `TRAN-DESC` | X(100) | `description` | `String` |
| `TORIGDT` | `TRAN-ORIG-TS` | X(26) | `origDate` | `String` (YYYY-MM-DD input) |
| `TPROCDT` | `TRAN-PROC-TS` | X(26) | `procDate` | `String` |
| `MID` | `TRAN-MERCHANT-ID` | 9(09) | `merchantId` | `String` (zero-padded) |
| `MNAME` | `TRAN-MERCHANT-NAME` | X(50) | `merchantName` | `String` |
| `MCITY` | `TRAN-MERCHANT-CITY` | X(50) | `merchantCity` | `String` |
| `MZIP` | `TRAN-MERCHANT-ZIP` | X(10) | `merchantZip` | `String` |
| `ACTIDIN` | account key | 9(11) | `acctId` (add only) | `String` |
| `CONFIRM` | `WS-CONFIRM` | X(01) | `confirm` (add only) | `String` |

The list row (`TransactionSummaryDto`) exposes `tranId`, `date` (MM/DD/YY slice of
`TRAN-ORIG-TS`), `description` and `amount`, matching `POPULATE-TRAN-DATA`.

## Behaviour notes

### List (COTRN00C)
- Browse ordered by `TRAN-ID`, GTEQ `startTranId`; `pageSize + 1` peek sets `moreRecords`
  (`CDEMO-CT00-NEXT-PAGE-FLG`). `firstTranId`/`lastTranId` are the PF7/PF8 anchors.
- A non-blank, **non-numeric** filter → `Tran ID must be Numeric ...`.
- `TransactionListScreenHandler`: a row `selection=S` XCTLs to `TRANSACTION_VIEW`; any other
  flag re-displays with `Invalid selection. Valid value is S`. PF7 at the top /
  PF8 at the bottom emit `You are already at the top/bottom of the page...`.

### View (COTRN01C)
- Empty id → `Tran ID can NOT be empty...`; not found → `Transaction ID NOT found...`.

### Add (COTRN02C) — validation order (reproduced exactly)
1. **Key fields** (`VALIDATE-INPUT-KEY-FIELDS`): account **or** card required
   (`Account or Card Number must be entered...`); numeric checks
   (`Account ID must be Numeric...`, `Card Number must be Numeric...`); cross-reference
   existence via `CardXref` (`Account ID NOT found...`, `Card Number NOT found...`). The
   resolved 16-char card number is stamped onto the new record.
2. **Data fields** (`VALIDATE-INPUT-DATA-FIELDS`), empty checks first in field order:
   type, category, source, description, amount, orig date, proc date, merchant id, name,
   city, zip — each `... can NOT be empty...`.
3. **Format checks**: `Type CD must be Numeric...`, `Category CD must be Numeric...`,
   amount `S99999999.99` (`Amount should be in format -99999999.99`), dates `YYYY-MM-DD`
   (`Orig/Proc Date should be in format YYYY-MM-DD`), calendar validity
   (`Orig/Proc Date - Not a valid date...`), `Merchant ID must be Numeric...`.
4. **Confirm gate** (`EVALUATE CONFIRMI`): only `Y`/`y` commits; `N`/blank →
   `Confirm to add this transaction...`; anything else → `Invalid value. Valid values are (Y/N)...`.
5. **Id generation**: highest existing `TRAN-ID` + 1, zero-padded to 16 (COBOL
   STARTBR/READPREV of the last key). Success line (note the double space):
   `Transaction added successfully.  Your Tran ID is <id>.`

## Tests

- `TransactionControllerTest` — MockMvc, `@WithMockUser`, seed data: list (default page,
  `startTranId`, non-numeric reject), view (found / not-found), add (valid + id generation +
  card resolution, bad amount, missing key, unknown account, confirm required, invalid Y/N).
- `TransactionServiceTest` — validation ordering (empty-field order, invalid calendar date,
  non-numeric merchant id, unknown card), empty-id view, card-from-account resolution.
- `TransactionScreenHandlerNavTest` — `ScreenHandler` beans dispatched through `/api/nav`
  for CT00/CT01/CT02 (list model, view record, not-found message, add confirmation).

Run: `cd java && mvn -B verify`.
