# Bill Payment (CB00 / COBIL00C) — Test Report

**How tested:** Ran the migrated stack locally — Angular UI (`localhost:4200`) against the Spring Boot backend (`localhost:8080`, `demo` seed on H2 in DB2-compatibility mode) — and drove the full Bill Payment flow through the browser UI, then confirmed persistence via the backend API.

**Result:** All planned assertions passed. No deviations.

## Assertions

- **Screen loads (CB00 / COBIL00C)** — passed. Header `Tran: CB00`, `Prog: COBIL00C`, title `Bill Payment`, footer `ENTER=Continue  F3=Back  F4=Clear`.
- **Blank Acct ID rejected** — passed. RED `Acct ID can NOT be empty...`.
- **Zero-balance account rejected** — passed. Acct 12 → balance `+0000000000.00`, RED `You have nothing to pay...`.
- **Invalid confirmation rejected** — passed. Confirm `X` → RED `Invalid value. Valid values are (Y/N)...`.
- **Balance inquiry** — passed. Acct 11 → balance `+0000000123.45`, RED/INFO `Confirm to make a bill payment...`.
- **Confirmed full-balance payment** — passed. `Y` + ENTER → GREEN `Payment successful.  Your Transaction ID is 0000000000000101.` (note the two spaces, matching COBOL), balance `+0000000000.00`, inputs cleared.
- **Payment persisted / not repeatable** — passed. Post-payment API inquiry for acct 11 returns balance `0.00`; a second pay attempt returns `You have nothing to pay...`, proving the balance was durably zeroed.

## Evidence

### Initial screen
![Bill Payment screen loaded](/home/ubuntu/screenshots/ss_cb258840.png)

### Guard: blank Acct ID (🔴)
![Blank account rejected](/home/ubuntu/screenshots/ss_5429a109.png)

### Guard: zero-balance account 12 (🔴)
![Nothing to pay](/home/ubuntu/screenshots/ss_9e8d2641.png)

### Guard: invalid confirm value (🔴)
![Invalid confirm](/home/ubuntu/screenshots/ss_817b827b.png)

### Inquiry: account 11 balance + confirm prompt
![Inquiry balance](/home/ubuntu/screenshots/ss_f73c9a4c.png)

### Confirmed payment (🟢 success, balance zeroed)
![Payment successful](/home/ubuntu/screenshots/ss_d29d9df6.png)

### Success message close-up (exact text, green)
![Success zoom](/home/ubuntu/screenshots/ss_zoom_c97df895.png)

## Notes / caveats

- Tested against **H2 in DB2-compatibility mode** for the live UI run (fast demo profile). A separate **real IBM DB2 11.5.9.0** round-trip was executed earlier in the session (documented in `BillPay_migration_signoff.md`) with matching results (txn `0000000000000101`, type `02`, cat `2`, amt `123.45`, card `4111111111111111`).
- The transaction id `0000000000000101` = seeded max `0000000000000100` + 1, confirming the max-plus-one sequencing.
- Backend automated suite: 13 tests passing; frontend: 7 tests passing; CI green on PR #183.
