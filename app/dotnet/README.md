# CBACT01C — C# / .NET Migration

Modernized replacement for the COBOL batch program **`CBACT01C.CBL`** (see `app/cbl/CBACT01C.cbl`),
which reads the VSAM KSDS account master and prints each account record. This port targets
**.NET 8**, runs on a VM / on-prem host, and reads accounts from a **relational database**
(PostgreSQL) instead of VSAM.

## What it does

- **`CBACT01C` report (default):** streams every account ordered by `acct_id` (the VSAM key
  order) and prints the per-field report, byte-for-byte identical to the COBOL
  `1100-DISPLAY-ACCT-RECORD` paragraph. Exits `0` on success and non-zero on any read error,
  mirroring the original `CEE3ABD` ABEND so a scheduler halts the job.
- **`load` sub-command:** one-time / repeatable ETL that reads the legacy fixed-width account
  flat file (ASCII, signed-overpunch numerics) and bulk-loads it into the `account` table.

## Layout

| Path | Purpose |
| --- | --- |
| `CardDemo.Batch.Cbact01c/Domain/AccountRecord.cs` | Modern account record (copybook `CVACT01Y`) |
| `CardDemo.Batch.Cbact01c/Legacy/ZonedDecimalCodec.cs` | Signed-overpunch decode/encode |
| `CardDemo.Batch.Cbact01c/Legacy/FixedWidthAccountParser.cs` | 300-byte fixed-width record parser |
| `CardDemo.Batch.Cbact01c/Formatting/AccountFormatter.cs` | Reproduces the COBOL DISPLAY layout |
| `CardDemo.Batch.Cbact01c/Data/PostgresAccountRepository.cs` | Streaming (forward-only) read of `account` |
| `CardDemo.Batch.Cbact01c/Etl/*` | Flat-file reader + COPY bulk loader + reconciliation count |
| `CardDemo.Batch.Cbact01c/AccountBatchProcessor.cs` | Port of the PROCEDURE DIVISION |
| `db/001_create_account.sql` | Relational schema for the account master |
| `CardDemo.Batch.Cbact01c.Tests/` | Unit + golden-master parity tests |

## Data mapping (copybook `CVACT01Y`, RECLN 300)

Signed numerics (`PIC S9(10)V99`) are stored on the mainframe as **zoned decimal with a signed
overpunch** on the last digit (`{`/`A`–`I` = positive 0–9, `}`/`J`–`R` = negative 0–9). For
example `00000001940{` decodes to **194.00**. Money columns use `NUMERIC(12,2)` and `decimal`
in code — never `double`.

## Configuration

Replaces the `ACCTFILE` DD of `app/jcl/READACCT.jcl`. Provide a connection string via
either `appsettings.json`:

```json
{ "Batch": { "ConnectionString": "Host=localhost;Database=carddemo;Username=carddemo;Password=..." } }
```

or the environment variable `CARDDEMO_Batch__ConnectionString`.

## Run

```bash
# 0. one-time schema
psql "$CONN" -f db/001_create_account.sql

# 1. ETL: load the legacy flat file into the relational table
export CARDDEMO_Batch__ConnectionString="Host=...;Database=carddemo;Username=...;Password=..."
dotnet run --project CardDemo.Batch.Cbact01c -- load ../data/ASCII/acctdata.txt

# 2. run the batch report (the CBACT01C equivalent)
dotnet run --project CardDemo.Batch.Cbact01c
```

Schedule step 2 with cron / Task Scheduler / Control-M as the replacement for `READACCT.jcl`.

## Test

```bash
dotnet test CardDemo.Batch.sln
```

Includes overpunch decode/encode round-trips, fixed-width parsing, an error-path check, and a
**golden-master** test asserting the report matches the legacy `1100-DISPLAY-ACCT-RECORD` output
byte-for-byte (fixtures in `CardDemo.Batch.Cbact01c.Tests/Fixtures/`).

## Notes / scope

- This program is read-only. The original main loop also emits a raw 300-byte
  `DISPLAY ACCOUNT-RECORD` dump in addition to the per-field block; the migration reproduces the
  human-readable per-field report and intentionally omits the redundant raw dump.
- `open_date` / `expiration_date` / `reissue_date` are kept as `VARCHAR(10)` text to preserve the
  exact source representation; convert to `DATE` in a later phase if downstream consumers need it.
