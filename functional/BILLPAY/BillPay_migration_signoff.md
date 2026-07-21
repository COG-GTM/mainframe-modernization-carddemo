# Bill Payment (CB00 / COBIL00C) — Migration Sign-off

**Stream:** Bill Payment · **TransID:** CB00 · **Program:** COBIL00C · **Map:** COBIL00 (COBIL0A)
**Parity mode:** OFF (functional-requirement + integration-test + recording verification)
**Target:** Java 17 / Spring Boot 3.3 · JPA · Flyway · IBM DB2 (H2 DB2-mode for tests) · Angular 17

## Scope delivered

| Layer | Location | Notes |
|-------|----------|-------|
| DB schema | `stack/backend/src/main/resources/db/migration/V1__billpay_schema.sql` | ACCOUNT, CARD_XREF (+ acct index), TRANSACTION — from CVACT01Y/CVACT03Y/CVTRA05Y |
| Entities/repos | `stack/backend/.../domain`, `.../repository` | AccountRepository, CardXrefRepository, TransactionRepository |
| Business logic | `stack/backend/.../service/BillPaymentService.java` | 1:1 of PROCESS-ENTER-KEY (validation order, confirm dispatch, id sequencing, constants, messages) |
| REST | `stack/backend/.../web/BillPaymentController.java` | `POST /api/billpay/inquiry`, `POST /api/billpay/pay` |
| UI | `stack/frontend/src/app/bill-payment/` | 3270-style Bill Payment screen; ENTER / F3 / F4 |
| CI | `.github/workflows/billpay-ci.yml` | backend `mvn verify`, frontend `ng test` + `ng build` |

## Verification results

- **Backend tests:** `mvn test` → **13 passing** (BillPaymentServiceTest 8, BillPaymentE2ETest 1, BillPaymentResponseTest 4). Covers FR-2..FR-9, AC-1..AC-4, balance formatting.
- **Frontend tests:** `ng test` → **7 passing** (component behavior + app bootstrap).
- **Frontend build:** `ng build` → success.
- **DB2 round-trip:** app started with `db2` profile against `icr.io/db2_community/db2:11.5.9.0`; Flyway migrated schema; `POST /pay` for account 11 (balance 123.45) →
  - response: `Payment successful.  Your Transaction ID is 0000000000000101.`
  - DB2 `ACCOUNT.ACCT_CURR_BAL` for acct 11 = `0.00`
  - DB2 `TRANSACTION` row `0000000000000101`: type `02`, cat `2`, amt `123.45`, card `4111111111111111`, desc `BILL PAYMENT - ONLINE`.
- **UI recording:** see `BillPay_screen_recording_checklist.md` (RC-1..RC-10).

## Behavioral parity notes (source → target)

- Validation order preserved: empty acct → account read → non-positive balance guard → confirm dispatch → payment.
- Exact messages preserved (e.g. `Acct ID can NOT be empty...`, `You have nothing to pay...`, `Confirm to make a bill payment...`, `Account ID NOT found...`, `Invalid value. Valid values are (Y/N)...`, and the success string with two spaces after the period).
- Transaction id = numeric max + 1, zero-padded to 16 (ENDFILE → first id `0000000000000001`).
- Money movement invariant: `TRAN_AMT` = pre-payment balance; post-payment balance = 0; insert + update are one `@Transactional` unit.
- Decline (`N`) and Clear (`F4`) leave data unchanged.

## Known deltas / out of scope

- Stored procedures: none required (analysis confirmed plain JPA leaves).
- Sign-on, menu, and the other CardDemo transactions remain COBOL (outside this slice).
- CICS pseudo-conversation/commarea is replaced by stateless REST; screen state lives in the Angular component.
- Timestamp millisecond precision is emitted as `.000000` to match the COBOL `WS-TIMESTAMP` construction.
