# Bill Payment Migration — Independent Audit

Independent review of the CB00 / COBIL00C migration against the COBOL source and the
functional requirements. Method: re-read `app/cbl/COBIL00C.cbl`, the copybooks, and the BMS
map, then trace each behavior into the Java/Angular implementation and the test assertions.

## Audit findings

| # | Check | Source ref | Target ref | Verdict |
|---|-------|-----------|-----------|---------|
| A-1 | Empty account id rejected with exact message | COBIL00C.cbl:159-164 | `BillPaymentService.process` (MSG_ACCT_EMPTY) | PASS |
| A-2 | Unknown account → `Account ID NOT found...` | READ-ACCTDAT NOTFND :361-366 | `accountRepository.findById` empty branch | PASS |
| A-3 | Invalid confirm → `Invalid value...` on CONFIRM field | EVALUATE CONFIRMI OTHER :186-190 | `switch default` → error CONFIRM | PASS |
| A-4 | Blank confirm → balance + `Confirm to make a bill payment...` | :236-240 | `!confirmYes` info branch | PASS |
| A-5 | Balance ≤ 0 → `You have nothing to pay...` (before payment) | :198-205 | non-positive guard before payment | PASS |
| A-6 | Decline (N) clears screen, no data change | EVALUATE CONFIRMI 'N' :179-182 | `BillPaymentResult.cleared()`; test asserts unchanged balance | PASS |
| A-7 | Transaction id = max numeric + 1, 16-char | STARTBR/READPREV + ADD 1 :217-223 | `findMaxNumericId()+1`, `%016d` | PASS |
| A-8 | First-ever id = 1 when file empty | ENDFILE path :460-465 | `coalesce(max,0)+1` | PASS |
| A-9 | Transaction constants (02/2/POS TERM/desc/merchant) | :224-232 | literals in `process` + asserted in tests | PASS |
| A-10 | Amount = pre-payment balance | MOVE ACCT-CURR-BAL TO TRAN-AMT :227 | `txn.setAmount(balance)` | PASS |
| A-11 | Card number from xref | READ-CXACAIX + MOVE XREF-CARD-NUM :225 | `cardXrefRepository.findFirstByAcctId...` | PASS |
| A-12 | Balance zeroed after payment | COMPUTE ... - TRAN-AMT :234 | `balance.subtract(balance)` = 0 | PASS |
| A-13 | Insert + update atomic | single CICS flow | `@Transactional` on `process` | PASS |
| A-14 | Success message exact (two spaces) | STRING :527-531 | `"Payment successful. " + " Your..."` | PASS |
| A-15 | Balance display format `+9999999999.99` | map CURBAL / WS-CURR-BAL | `BillPaymentResponse.formatBalance` + test | PASS |
| A-16 | Error RED / success GREEN | map ERRMSG colour, DFHGREEN | `messageType` → CSS class | PASS |

## Observations / recommendations (non-blocking)

- **Card xref multiplicity:** the COBOL AIX read returns the first matching record; the Java
  repo uses `findFirstByAcctIdOrderByCardNum` for determinism. If an account can have multiple
  cards and a specific one is required for billing, revisit selection criteria.
- **Concurrency:** the max-id+1 scheme mirrors the COBOL browse and is safe under the app's
  single-writer assumption. For high-concurrency use, add a DB sequence or unique-key retry
  (the duplicate-key branch already surfaces `Tran ID already exist...`).
- **Timestamp precision:** emitted as `.000000`; if downstream systems need real microseconds,
  widen the formatter. Matches COBOL today.

## Conclusion

The migration is a faithful 1:1 reproduction of the observable COBIL00C behavior for the Bill
Payment transaction. All 16 audit checks pass and are backed by automated tests plus a live
DB2 round-trip. **Audit result: APPROVED.**
