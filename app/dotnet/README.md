# CardDemo Batch — .NET / PostgreSQL Modernization

Modernized C#/.NET 8 implementations of the CardDemo batch COBOL programs
**CBACT01C** (print account data) and **CBACT02C** (print card data).

The original programs read fixed-width records sequentially from VSAM KSDS
datasets and printed each record. This modernization keeps the same batch
behavior but sources the data from a **relational database (PostgreSQL)** and
runs as standalone console applications on a **virtual machine / on-prem** host.

| Legacy COBOL | Modern replacement | Source |
| --- | --- | --- |
| `CBACT01C` (ACCTFILE KSDS) | `CardDemo.AccountReader` | `account` table |
| `CBACT02C` (CARDFILE KSDS) | `CardDemo.CardReader` | `card` table |
| `ACCTFILE.jcl` / `CARDFILE.jcl` REPRO load | `CardDemo.DataMigration` | flat files → DB |

## Solution layout

```
app/dotnet/
├── CardDemo.Batch.sln
├── db/schema.sql                 # PostgreSQL DDL (account, card)
├── docker-compose.yml            # local PostgreSQL for dev/test
├── src/
│   ├── CardDemo.Domain/          # AccountRecord, CardRecord (copybook models)
│   ├── CardDemo.Data/            # zoned-decimal + fixed-width parsing, Dapper repos, formatting
│   ├── CardDemo.AccountReader/   # CBACT01C replacement (console)
│   ├── CardDemo.CardReader/      # CBACT02C replacement (console)
│   └── CardDemo.DataMigration/   # loads acctdata.txt / carddata.txt into PostgreSQL
└── tests/
    └── CardDemo.Tests/           # unit tests (overpunch, parsing, formatting)
```

## Data mapping

Record layouts come from the copybooks `CVACT01Y` (account, RECLN 300) and
`CVACT02Y` (card, RECLN 150).

- **Signed numerics** — COBOL `PIC S9(10)V99` DISPLAY fields store the value as
  zoned decimal with a trailing **sign overpunch** on the units byte
  (`{ A B C … I` = positive 0–9, `} J K L … R` = negative 0–9). These are decoded
  to `decimal` and stored as `NUMERIC(12,2)`. See
  `CardDemo.Data/Cobol/ZonedDecimal.cs`.
- **Unsigned numerics** — `PIC 9(n)` → `BIGINT` / `INTEGER`.
- **Text** — `PIC X(n)` → `VARCHAR(n)` (dates kept as `YYYY-MM-DD` text to
  preserve the original representation exactly).
- The trailing `FILLER` bytes in each record are ignored.

> Note: the legacy programs `DISPLAY` signed numerics as their raw zoned bytes
> (e.g. `00000019400{`). The modern readers render the decimal value
> (e.g. `194.00`), which carries the same business meaning.

## Prerequisites

- [.NET SDK 8.0+](https://dotnet.microsoft.com/download)
- PostgreSQL 14+ (a `docker-compose.yml` is provided for local use)

## Quick start (local)

```bash
cd app/dotnet

# 1. Start PostgreSQL (or point appsettings.json at your own instance)
docker compose up -d

# 2. Build
dotnet build

# 3. Create schema + load the sample flat files
dotnet run --project src/CardDemo.DataMigration -- \
    --init-schema \
    --accounts ../data/ASCII/acctdata.txt \
    --cards    ../data/ASCII/carddata.txt

# 4. Run the batch readers (equivalent to READACCT.jcl / READCARD.jcl)
dotnet run --project src/CardDemo.AccountReader
dotnet run --project src/CardDemo.CardReader
```

## Configuration

Each executable reads its PostgreSQL connection string from `appsettings.json`:

```json
{
  "Database": {
    "ConnectionString": "Host=localhost;Port=5432;Database=carddemo;Username=carddemo;Password=carddemo"
  }
}
```

Override without editing the file via an environment variable (double underscore
separates the section and key, prefixed with `CARDDEMO_`):

```bash
export CARDDEMO_Database__ConnectionString="Host=db.internal;Database=carddemo;Username=svc_carddemo;Password=****"
```

`CardDemo.DataMigration` also accepts `--connection "<npgsql-conn-string>"`.

## Data migration tool

```
CardDemo.DataMigration [--init-schema] [--accounts <file>] [--cards <file>] [--connection <conn>]
```

- `--init-schema` — create the `account`/`card` tables if they do not exist.
- `--accounts <file>` — load an account flat file (300-byte records).
- `--cards <file>` — load a card flat file (150-byte records).

Loads are **idempotent** (`INSERT … ON CONFLICT DO UPDATE`), so the tool can be
re-run safely.

## Testing

```bash
cd app/dotnet
dotnet test
```

Unit tests cover the zoned-decimal overpunch codec (both directions),
fixed-width field extraction, record parsing against the real sample records,
and the display formatting.

## VM / on-prem deployment runbook

1. **Provision** a VM with the .NET 8 runtime and network access to PostgreSQL.
2. **Publish** the executables:
   ```bash
   dotnet publish src/CardDemo.AccountReader -c Release -o /opt/carddemo/accountreader
   dotnet publish src/CardDemo.CardReader    -c Release -o /opt/carddemo/cardreader
   dotnet publish src/CardDemo.DataMigration -c Release -o /opt/carddemo/migration
   ```
3. **Configure** the connection string via `CARDDEMO_Database__ConnectionString`
   (recommended: a secrets manager / systemd `EnvironmentFile`), or edit each
   published `appsettings.json`.
4. **Initialize** the database once: run `db/schema.sql` (or the migration tool
   with `--init-schema`) against the target instance.
5. **Load** data with `CardDemo.DataMigration` (equivalent to the JCL REPRO
   steps that loaded the VSAM files).
6. **Schedule** the readers with cron / systemd timers (equivalent to submitting
   `READACCT.jcl` / `READCARD.jcl`). Both return exit code `0` on success and a
   non-zero code on failure so they can be wired into batch orchestration.
