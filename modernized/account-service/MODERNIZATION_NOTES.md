# Modernization Notes — CBACT01C → Java Spring Boot

## COBOL idioms that don't translate directly

- **Sequential VSAM read + `FILE STATUS` driving control flow.** CBACT01C opens
  the `ACCTDAT` KSDS, then `PERFORM UNTIL END-OF-FILE = 'Y'` reading one record
  at a time, mapping file statuses (`'00'` ok, `'10'` EOF, anything else error)
  onto `APPL-RESULT` level-88s (`APPL-AOK`, `APPL-EOF`). In Java this becomes an
  ordered repository read (`findAllByOrderByAcctIdAsc`) iterated to exhaustion;
  EOF is simply `Iterator.hasNext() == false`, and a data-access failure throws
  instead of setting a status byte. The paragraph structure
  (`0000`/`1000`/`1100`/`9000`/`9999`) is preserved as private methods for
  traceability.
- **`9999-ABEND-PROGRAM` → `CALL 'CEE3ABD'`.** A hard task abend becomes a typed
  `AccountFileException`. We do not kill the JVM; the batch runner propagates the
  exception (non-zero exit) and the web layer maps it to HTTP 500 via
  `@RestControllerAdvice`.
- **`DISPLAY` of a signed numeric.** COBOL `DISPLAY ACCT-CURR-BAL` emits the raw
  zoned-decimal representation with a trailing sign overpunch (e.g. `0000019400{`).
  The modern report logs a human-readable decimal (`194.00`) instead. The literal
  field labels and the `1100-DISPLAY-ACCT-RECORD` field ordering (which prints
  `ACCT-GROUP-ID` but not `ACCT-ADDR-ZIP`) are kept identical.
- **`REDEFINES` / binary status plumbing.** `TWO-BYTES-BINARY` redefined as
  `TWO-BYTES-ALPHA` plus `IO-STATUS-04` is pure file-status formatting for the
  `9910-DISPLAY-IO-STATUS` routine. None of it has meaning once file status is an
  exception, so it is dropped.

## Data integrity concerns

- **Fixed-point money.** `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`,
  `ACCT-CASH-CREDIT-LIMIT`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT` are all
  `PIC S9(10)V99` — exact base-10 decimals. They map to `BigDecimal` (scale 2),
  **never** `double`/`float`, and to `NUMERIC(12,2)` columns. `ZonedDecimal`
  decodes the IBM sign overpunch used in the ASCII seed file
  (`{`=+0…`I`=+9, `}`=-0…`R`=-9).
- **Fixed-length VSAM → relational.** The 300-byte record includes a 178-byte
  `FILLER` that is a storage artifact and is not mapped to a column. `ACCT-ID`
  (`PIC 9(11)`) is kept as a zero-padded `String` to preserve leading zeros and
  avoid any chance of numeric precision loss. The copybook record length (300) is
  **not** changed — per the CardDemo constraint, the VSAM cluster layout is left
  intact; the parser reads against those exact byte offsets.
- **Date fields are strings.** `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE` (original
  misspelling preserved), and `ACCT-REISSUE-DATE` are `PIC X(10)` text. They are
  carried as `String` rather than `LocalDate` to stay byte-faithful to the source;
  promote to `LocalDate` once the data is validated.

## Security gaps in the original

- **No authentication / authorization.** CBACT01C is a batch job with no access
  control. The REST endpoints exposing the same data need real auth (JWT/OAuth)
  and authorization before they leave a trusted network.
- **Reads all account data unconditionally.** A `GET /api/accounts` that returns
  every account is fine for a 50-row demo but is a data-exposure and performance
  risk at scale — add pagination and field-level authorization.

## Suggested next steps

1. Replace H2 with a real database (e.g. Postgres) using `NUMERIC(12,2)` for money
   columns; load seed data via a migration tool (Flyway/Liquibase) instead of the
   startup loader.
2. Add pagination (`Pageable`) to `GET /api/accounts` and a streaming/`Slice`
   variant for the batch report so it scales beyond the sample dataset.
3. Add authentication + per-resource authorization and request/response logging.
4. Add equivalence tests that compare this service's output against the COBOL
   program's `DISPLAY` output for a sample of accounts (value-for-value, allowing
   for the zoned-decimal vs. decimal formatting difference).
5. Promote text date fields to `LocalDate` and add input validation once upstream
   data quality is confirmed.
