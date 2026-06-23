# Modernization Notes — COBIL00C → TypeScript

## COBOL idioms that don't translate directly

- **`REDEFINES` (BMS symbolic map).** `COBIL00.CPY` defines the input map
  `COBIL0AI` and the output map `COBIL0AO` as the *same* storage via
  `REDEFINES`, plus per-field length/attribute/flag bytes (`...L`, `...F`,
  `...A`). None of that survives in a REST world: there is no shared 3270
  buffer, so the input map collapses to a small request DTO
  (`acctId`, `confirm`) and the output map to a response DTO.
- **`MOVE -1 TO ...L` (cursor positioning).** The COBOL repeatedly sets a
  field's length to `-1` to place the 3270 cursor on the field in error. This is
  pure terminal UX with no server-side meaning and is dropped; the API instead
  returns a typed error `code` so a modern UI can focus the right field.
- **Level-88 condition names** (`ERR-FLG-ON`, `CONF-PAY-YES`, `CDEMO-PGM-REENTER`)
  become booleans / string comparisons. The `WS-ERR-FLG` + re-send pattern (set
  flag, `PERFORM SEND-BILLPAY-SCREEN`, fall through) becomes thrown
  `BillPaymentError`s caught by the controller.
- **Pseudo-conversational COMMAREA flow.** COBIL00C is re-entered on every
  keystroke and rebuilds state from the COMMAREA (`CDEMO-PGM-REENTER`,
  `CDEMO-CB00-TRN-SELECTED`). REST is stateless per request, so the screen
  lifecycle (first-display vs. re-entry, PF3 back, PF4 clear) is not modeled;
  only the business action (Enter → pay) is. PF3/PF4/menu navigation belong to
  the front-end.
- **`STARTBR` + `READPREV` + `ENDBR` to find the max key.** Browsing the VSAM
  KSDS backwards from `HIGH-VALUES` to get the last transaction id becomes
  `TransactionRepository.maxTransactionId()`. In a real database this should be
  a sequence / `IDENTITY` column or `MAX(id)+1` inside a transaction — see
  concurrency below.

## Data integrity concerns

- **Fixed-point money.** `ACCT-CURR-BAL` is `PIC S9(10)V99` and `TRAN-AMT` is
  `PIC S9(09)V99` — exact base-10 decimals. The `Money` class keeps integer
  cents so we never introduce binary floating-point drift (e.g. `0.1 + 0.2`).
  In a database, use `NUMERIC(12,2)` / `DECIMAL`, never `FLOAT`/`DOUBLE`.
- **Fixed-length VSAM → relational.** Copybook `FILLER` bytes and fixed widths
  (`ACCTDAT` 300, `CCXREF` 50, `TRANSACT` 350) are storage artifacts; modern
  columns are variable-length. IDs that are `PIC 9(n)` are kept as zero-padded
  **strings** (`acctId`, `transactionId`) to preserve leading zeros and avoid
  precision loss for 16-digit values.
- **No transactional atomicity in the original.** COBIL00C does
  `WRITE TRANSACT` then `REWRITE ACCTDAT` as two separate CICS calls (CICS
  syncpoint covers them at task end). The modern equivalent should wrap the
  transaction insert + account update in a single DB transaction so a failure
  can't leave a written transaction with an unchanged balance.
- **Concurrency.** The COBOL reads `ACCTDAT` `UPDATE` (record lock) and derives
  the next id by browsing. Under load, `MAX(id)+1` races; prefer a DB sequence,
  and use `SELECT ... FOR UPDATE` / optimistic versioning on the account row.

## Security gaps in the original

- **No authentication / authorization** in COBIL00C itself — it trusts the
  signed-on CICS user passed via COMMAREA (`CDEMO-USER-ID`). The REST service
  needs real auth middleware (JWT/OAuth) and an ownership check that the caller
  may pay *this* account.
- **No idempotency.** Re-submitting Enter could create duplicate payments; the
  COBOL only guards the (auto-incremented) primary key. Add an idempotency key
  on the POST endpoint.
- **CardDemo stores user passwords in plaintext** in `USRSEC` (copybook
  `CSUSR01Y`). Out of scope here, but any modernization must hash credentials.

## Suggested next steps

1. Replace the in-memory repositories with a real database (Postgres) using the
   table mappings above; move id generation to a sequence.
2. Wrap `insert transaction` + `update account` in one DB transaction.
3. Add authentication + per-account authorization middleware and an idempotency
   key on the payment endpoint.
4. Add input validation at the edge (e.g. zod) and request/response logging.
5. Add HTTP-level integration tests (supertest) on top of the existing unit
   tests, and equivalence tests comparing outputs against the COBOL program for
   a sample of accounts.
