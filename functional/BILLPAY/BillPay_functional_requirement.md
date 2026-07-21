# Bill Payment — Functional Requirements (Step 2a-FR: `!generate_fr_transaction`)

**Stream:** Bill Payment (`CB00` / `COBIL00C`). **This document is the acceptance / sign-off oracle.**
Every requirement is a *business-visible* behavior (trigger → user-visible result), cited to `COBIL00C.cbl:line`.

## Functional requirements (business sign-off set)

| ID | As a user I can… | Trigger | User-visible result | Source cite |
|----|------------------|---------|---------------------|-------------|
| FR-1 | Open the Bill Payment screen | Navigate to CB00 | Empty Bill Payment screen: Acct ID + Confirm inputs, balance & message blank, header shows date/time/prog/tran | `COBIL00C.cbl:114-122`, `289-301` |
| FR-2 | See my current balance for an account | Enter a valid Acct ID, ENTER (confirm blank) | Screen shows the account's current balance and prompt `Confirm to make a bill payment...` | `COBIL00C.cbl:170-194`, `236-240` |
| FR-3 | Pay my balance in full | Enter valid Acct ID with balance > 0, Confirm = `Y`, ENTER | One transaction is recorded, balance becomes 0, GREEN message `Payment successful.  Your Transaction ID is <id>.` | `COBIL00C.cbl:210-235`, `522-532` |
| FR-4 | Be stopped from paying an empty account id | ENTER with Acct ID blank | RED error `Acct ID can NOT be empty...` | `COBIL00C.cbl:159-164` |
| FR-5 | Be stopped from paying when nothing is owed | Valid Acct ID whose balance ≤ 0, ENTER | RED error `You have nothing to pay...`; no transaction, balance unchanged | `COBIL00C.cbl:198-205` |
| FR-6 | Be told when the account does not exist | Unknown Acct ID, ENTER | RED error `Account ID NOT found...` | `COBIL00C.cbl:359-363` |
| FR-7 | Be forced to confirm before paying | Valid Acct ID, balance > 0, Confirm blank, ENTER | RED prompt `Confirm to make a bill payment...`; no transaction, balance unchanged | `COBIL00C.cbl:236-240` |
| FR-8 | Be rejected for an invalid confirm value | Confirm not in {Y,y,N,n,blank}, ENTER | RED error `Invalid value. Valid values are (Y/N)...` | `COBIL00C.cbl:185-190` |
| FR-9 | Decline the payment | Confirm = `N`, ENTER | Screen fields cleared; no transaction, balance unchanged | `COBIL00C.cbl:178-181`, `552-566` |
| FR-10 | Clear the screen | Press PF4 | All input fields cleared | `COBIL00C.cbl:136-137`, `552-566` |
| FR-11 | Go back | Press PF3 | Return to the calling screen (menu) | `COBIL00C.cbl:128-135` |

## Screen / UI field spec (map `COBIL0A`)
| Field | Kind | Len | Notes |
|-------|------|-----|-------|
| Acct ID (`ACTIDIN`) | input | 11 | numeric account id |
| Confirm (`CONFIRM`) | input | 1 | Y/N |
| Current balance (`CURBAL`) | output | 14 | signed, 2 decimals; e.g. `+0000123.45` style |
| Message (`ERRMSG`) | output | 78 | RED for errors, GREEN on payment success |
| Header | output | — | Tran `CB00`, Prog `COBIL00C`, title, current date `mm/dd/yy`, time `hh:mm:ss` |
| Footer | static | — | `ENTER=Continue  F3=Back  F4=Clear` |

## Validation / error catalogue (exact strings — parity-critical)
- `Acct ID can NOT be empty...`
- `Invalid value. Valid values are (Y/N)...`
- `You have nothing to pay...`
- `Confirm to make a bill payment...`
- `Account ID NOT found...`
- `Payment successful.  Your Transaction ID is <TRAN-ID>.` (GREEN)
- (defensive) `Unable to lookup Account...`, `Unable to Update Account...`, `Unable to lookup XREF AIX file...`, `Tran ID already exist...`

## Acceptance criteria
- **AC-1 (money movement):** confirmed payment on balance `B>0` ⇒ exactly one new transaction with `amount == B`, account balance ⇒ `0`. (FR-3)
- **AC-2 (id sequencing):** new transaction id = `max(existing numeric id) + 1`, 16-char zero-padded; first ever = `1`. (FR-3)
- **AC-3 (transaction content):** new transaction has `typeCode='02'`, `categoryCode=2`, `source='POS TERM'`, `description='BILL PAYMENT - ONLINE'`, `merchantId=999999999`, `merchantName='BILL PAYMENT'`, `merchantCity='N/A'`, `merchantZip='N/A'`, `cardNumber` = the account's xref card. (FR-3)
- **AC-4 (guards):** empty id, balance ≤ 0, unknown account, unconfirmed, invalid confirm each block the payment with the exact message above and leave data unchanged. (FR-4..FR-8)
- **AC-5 (decline/clear/back):** N declines & clears; PF4 clears; PF3 navigates back — none mutate data. (FR-9..FR-11)

## Traceability matrix (requirement ↔ source ↔ test ↔ UI-verification case)
| FR | Source cite | Backend test | UI recording case |
|----|-------------|--------------|-------------------|
| FR-2 | 170-194 | `inquiry_returnsBalanceAndConfirmPrompt` | REC-02 |
| FR-3 | 210-235,522-532 | `pay_confirmed_insertsTxnAndZeroesBalance`, E2E `billPay_persistsRowAndBalance` | REC-03 |
| FR-4 | 159-164 | `pay_emptyAcct_error` | REC-04 |
| FR-5 | 198-205 | `pay_nonPositiveBalance_error` | REC-05 |
| FR-6 | 359-363 | `pay_unknownAcct_error` | REC-06 |
| FR-7 | 236-240 | `pay_unconfirmed_prompt` | REC-07 |
| FR-8 | 185-190 | `pay_invalidConfirm_error` | REC-08 |
| FR-9 | 178-181 | `pay_declined_noChange` | REC-09 |
| FR-10/11 | 136-137,128-135 | (UI) | REC-10/REC-11 |

Demoted (NOT functional requirements): CICS reentry/commarea mechanics, XCTL routing, timestamp formatting, browse cursor mechanics, defensive VSAM RESP handling.
