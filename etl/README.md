# CardDemo Account ETL

A Java ETL that extracts CardDemo **Account** records (copybook
[`CVACT01Y`](../app/cpy/CVACT01Y.cpy) / VSAM file `ACCTDAT`), transforms the mainframe
fixed-length data into a modern relational shape, validates business rules, and loads the result
into a PostgreSQL `accounts` table.

## What it does

| Stage | Detail |
|-------|--------|
| **Extract** | Reads fixed-length 300-byte Account records from either the ASCII seed file (`app/data/ASCII/acctdata.txt`, newline-delimited) or the EBCDIC VSAM unload (`app/data/EBCDIC/*.ACCTDATA.PS`, contiguous fixed-length, decoded via `Cp037`). |
| **Transform** | Decodes EBCDIC sign-overpunch signed numerics (`{`=+0 … `I`=+9, `}`=-0 … `R`=-9), applies implied decimal points (`PIC S9(10)V99` → `NUMERIC(12,2)`), parses ISO dates, and trims alphanumeric fields. |
| **Validate** | Enforces business rules: `ACCT-ID > 0`, `ACCT-ACTIVE-STATUS ∈ {Y,N}`, credit limit ≥ cash credit limit, non-negative limits, expiration not before open date. |
| **Load** | Batched, idempotent upserts (`INSERT … ON CONFLICT (acct_id) DO UPDATE`). |

Invalid or unparseable records are skipped and counted; they do not abort the run.

## Field mapping (CVACT01Y → accounts)

| COBOL field | PIC | Bytes | Column | Type |
|-------------|-----|-------|--------|------|
| ACCT-ID | 9(11) | 1–11 | `acct_id` | `BIGINT` PK |
| ACCT-ACTIVE-STATUS | X(01) | 12 | `active_status` | `CHAR(1)` |
| ACCT-CURR-BAL | S9(10)V99 | 13–24 | `curr_bal` | `NUMERIC(12,2)` |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 25–36 | `credit_limit` | `NUMERIC(12,2)` |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 37–48 | `cash_credit_limit` | `NUMERIC(12,2)` |
| ACCT-OPEN-DATE | X(10) | 49–58 | `open_date` | `DATE` |
| ACCT-EXPIRAION-DATE | X(10) | 59–68 | `expiration_date` | `DATE` |
| ACCT-REISSUE-DATE | X(10) | 69–78 | `reissue_date` | `DATE` |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 79–90 | `curr_cyc_credit` | `NUMERIC(12,2)` |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 91–102 | `curr_cyc_debit` | `NUMERIC(12,2)` |
| ACCT-ADDR-ZIP | X(10) | 103–112 | `addr_zip` | `VARCHAR(10)` |
| ACCT-GROUP-ID | X(10) | 113–122 | `group_id` | `VARCHAR(10)` |
| FILLER | X(178) | 123–300 | — | (ignored) |

## Build

```bash
cd etl
mvn -q clean package          # builds target/account-etl.jar (unit tests run)
mvn -q verify                 # also runs Testcontainers integration tests (needs Docker)
```

Requires JDK 17+ and Maven. The integration tests (`*IT`) require a running Docker daemon.

## Run

```bash
# Load the ASCII seed file into PostgreSQL, creating the table if needed
java -jar target/account-etl.jar \
  --format ASCII \
  --input ../app/data/ASCII/acctdata.txt \
  --jdbc-url jdbc:postgresql://localhost:5432/carddemo \
  --db-user carddemo --db-password carddemo \
  --create-schema

# Load the EBCDIC VSAM unload
java -jar target/account-etl.jar \
  --format EBCDIC \
  --input ../app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS \
  --jdbc-url jdbc:postgresql://localhost:5432/carddemo \
  --db-user carddemo --db-password carddemo

# Parse + validate only, no database
java -jar target/account-etl.jar --format ASCII --input ../app/data/ASCII/acctdata.txt --dry-run
```

| Option | Description |
|--------|-------------|
| `-f, --format` | `ASCII` or `EBCDIC` (required) |
| `-i, --input` | path to the data file (required) |
| `--jdbc-url` | PostgreSQL JDBC URL (required unless `--dry-run`) |
| `--db-user`, `--db-password` | database credentials (`--db-password` prompts if no value) |
| `--batch-size` | records per insert batch (default 500) |
| `--create-schema` | create the `accounts` table from `schema.sql` if absent |
| `--dry-run` | parse + validate only; no DB connection or writes |

Exit code is `0` on success, `1` if any record failed to parse, `2` on a usage/input error.
