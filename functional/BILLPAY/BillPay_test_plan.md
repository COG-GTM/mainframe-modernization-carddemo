# Bill Payment UI — Test Plan (execution)

App under test: Angular UI `http://localhost:4200` → Spring Boot `http://localhost:8080` (`demo` seed on H2 in DB2-mode).
Feature path (traced): `AppComponent` renders `<app-bill-payment>` (app.component.ts:8) → `BillPaymentComponent`
(bill-payment.component.ts). ENTER → `onEnter()` → `BillPaymentService.pay()` POST `/api/billpay/pay`
(bill-payment.service.ts:18-20). Balance/message/color from response (`applyResponse`, ts:76-89);
success clears inputs; `cleared` response wipes screen. F4 → `onClear()`; F3 → `onBack()`.

Seed: acct 11 = 123.45 (card 4111111111111111), acct 12 = 0.00, pre-existing txn 0000000000000100.

## Primary flow — confirmed payment (the money-movement proof)

| Step | Action | Pass criteria (fails if broken) |
|------|--------|--------------------------------|
| 1 | Load `localhost:4200` | Screen shows `Tran: CB00`, `Prog: COBIL00C`, title `Bill Payment`, footer `ENTER=Continue  F3=Back  F4=Clear` |
| 2 | Type `11` in Acct ID, click ENTER (confirm blank) | Balance line shows `+0000000123.45`; message `Confirm to make a bill payment...` in RED |
| 3 | Type `Y` in Confirm, click ENTER | Message turns GREEN: `Payment successful.  Your Transaction ID is 0000000000000101.`; balance shows `+0000000000.00`; inputs cleared. (Broken impl would show wrong id, red text, or non-zero balance) |
| 4 | Verify persistence (shell, DB) | `ACCOUNT` acct 11 `ACCT_CURR_BAL=0.00`; new `TRANSACTION` `0000000000000101` with `TRAN_TYPE_CD=02`, `TRAN_CAT_CD=2`, `TRAN_AMT=123.45`, `TRAN_CARD_NUM=4111111111111111`, `TRAN_DESC=BILL PAYMENT - ONLINE` |

## Guard cases (each must show the exact COBOL message, RED, and no data change)

| Step | Action | Pass criteria |
|------|--------|---------------|
| G1 | Empty Acct ID + ENTER | RED `Acct ID can NOT be empty...` |
| G2 | Acct ID `12` + ENTER | RED `You have nothing to pay...` (balance 0 account) |
| G3 | Acct ID `11`, Confirm `X` + ENTER | RED `Invalid value. Valid values are (Y/N)...` |

Adversarial note: the success case (Step 3) is the discriminator — a broken id-sequencing, balance-decrement, or color/message mapping would visibly differ (wrong id, non-zero balance, or red instead of green).
