# Bill Payment — Screen Recording Checklist

Verification cases exercised on the running Angular UI (`http://localhost:4200`) with the
Spring Boot backend on `:8080` (`demo` profile seed data). Each case maps to a functional
requirement and to the COBOL behavior in `COBIL00C.cbl`.

| # | Case | Steps | Expected result | FR |
|---|------|-------|-----------------|----|
| RC-1 | Open screen | Load the app | Bill Payment screen renders: header (Tran CB00 / Prog COBIL00C / date / time), Acct ID + Confirm inputs, footer `ENTER=Continue  F3=Back  F4=Clear` | FR-1 |
| RC-2 | Blank account | Leave Acct ID empty, press ENTER | RED message `Acct ID can NOT be empty...` | FR-4 |
| RC-3 | Unknown account | Acct ID `99999999999`, ENTER | RED message `Account ID NOT found...` | FR-6 |
| RC-4 | Balance inquiry | Acct ID `11`, ENTER (confirm blank) | Balance `+0000000123.45` shown, message `Confirm to make a bill payment...` | FR-2 / FR-7 |
| RC-5 | Nothing to pay | Acct ID `12`, ENTER | RED message `You have nothing to pay...` | FR-5 |
| RC-6 | Invalid confirm | Acct ID `11`, Confirm `X`, ENTER | RED message `Invalid value. Valid values are (Y/N)...` | FR-8 |
| RC-7 | Decline (N) | Acct ID `11`, Confirm `N`, ENTER | Screen cleared, no message, balance unchanged | FR-9 |
| RC-8 | Confirmed payment | Acct ID `11`, Confirm `Y`, ENTER | GREEN message `Payment successful.  Your Transaction ID is <id>.`, balance `+0000000000.00` | FR-3 |
| RC-9 | Clear (F4) | Enter some data, click F4 | All fields cleared | FR-10 |
| RC-10 | Back (F3) | Click F3 | Returns to previous menu (message shown) | FR-11 |

## Persistence proof (asserted in `BillPaymentE2ETest` + DB2 round-trip)
- Confirmed payment inserts exactly one `TRANSACTION` row.
- `TRAN_AMT` = pre-payment balance; `TRAN_TYPE_CD='02'`, `TRAN_CAT_CD=2`,
  `TRAN_DESC='BILL PAYMENT - ONLINE'`, `TRAN_MERCHANT_ID=999999999`, card from xref.
- `ACCT_CURR_BAL` becomes `0.00`.
- New `TRAN_ID` = numeric max + 1 (16-char zero padded).
