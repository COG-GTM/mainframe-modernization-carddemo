# CBACT01C — Python migration

A modern Python port of the CardDemo mainframe batch program
[`app/cbl/CBACT01C.cbl`](../../app/cbl/CBACT01C.cbl).

`CBACT01C` is a standalone batch program that opens the account master VSAM
KSDS file (`ACCTFILE`), reads every record sequentially in `ACCT-ID` order,
prints each field with labels, then closes. On the mainframe it is driven by the
JCL job [`app/jcl/READACCT.jcl`](../../app/jcl/READACCT.jcl); this package
**replaces that job**. The record layout comes from copybook
[`app/cpy/CVACT01Y.cpy`](../../app/cpy/CVACT01Y.cpy) (300-byte `ACCOUNT-RECORD`).

The VSAM file is replaced by a relational table (`accounts`) accessed through
**SQLAlchemy**. SQLite is used for local development/testing; because all access
goes through SQLAlchemy, the same code runs against Postgres or MySQL by pointing
at a different database URL.

## Layout

| File | Purpose |
| --- | --- |
| `account.py` | `Account` model — a dataclass **and** SQLAlchemy ORM model mirroring `CVACT01Y.cpy`. Kept in its own module because the account record is shared across many programs. |
| `copybook.py` | Fixed-width / zoned-decimal parsing for the `CVACT01Y` layout. |
| `db.py` | Engine / session helpers and schema creation. |
| `cbact01c.py` | Batch entry point reproducing the COBOL procedure flow. |
| `loader.py` | Utility that seeds the `accounts` table from the sample data file. |
| `tests/` | pytest suite. |

## Field / type mapping (`CVACT01Y.cpy`)

| COBOL | PIC | Python / SQLAlchemy |
| --- | --- | --- |
| `ACCT-ID` | `9(11)` | `int` primary key |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `str` (1 char) |
| `ACCT-CURR-BAL` | `S9(10)V99` | `Decimal` / `NUMERIC(12,2)` |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `Decimal` / `NUMERIC(12,2)` |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `Decimal` / `NUMERIC(12,2)` |
| `ACCT-OPEN-DATE` | `X(10)` | `str` |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `str` (COBOL spelling kept) |
| `ACCT-REISSUE-DATE` | `X(10)` | `str` |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `Decimal` / `NUMERIC(12,2)` |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `Decimal` / `NUMERIC(12,2)` |
| `ACCT-ADDR-ZIP` | `X(10)` | `str` |
| `ACCT-GROUP-ID` | `X(10)` | `str` |
| `FILLER` | `X(178)` | dropped |

Monetary fields use `decimal.Decimal` backed by `NUMERIC(12,2)` — **never
float** — to preserve exact cents.

## Setup

Run all commands from the `python/` directory (the parent of this package) so
that `cbact01c` is importable as a package:

```bash
cd python
python -m venv cbact01c/.venv
source cbact01c/.venv/bin/activate      # Windows: cbact01c\.venv\Scripts\activate
pip install -r cbact01c/requirements.txt
```

By default the program uses `sqlite:///cbact01c.db`. Override with the
`CBACT01C_DB_URL` environment variable, e.g.:

```bash
export CBACT01C_DB_URL="postgresql+psycopg://user:pw@localhost/carddemo"
```

## 1. Seed the database

Load the shipped ASCII sample account data (`app/data/ASCII/acctdata.txt`):

```bash
python -m cbact01c.loader ../app/data/ASCII/acctdata.txt
```

The loader creates the `accounts` table if needed and inserts/updates one row
per record. To load the EBCDIC dataset instead, pass its encoding:

```bash
python -m cbact01c.loader --encoding cp037 \
    ../app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS
```

## 2. Run the batch job

This is the equivalent of submitting `READACCT.jcl`:

```bash
python -m cbact01c
```

It prints:

```
START OF EXECUTION OF PROGRAM CBACT01C
ACCT-ID                 :00000000001
ACCT-ACTIVE-STATUS      :Y
ACCT-CURR-BAL           :194.00
...
-------------------------------------------------
END OF EXECUTION OF PROGRAM CBACT01C
```

The labels, order, and separator line match the COBOL paragraph
`1100-DISPLAY-ACCT-RECORD`; monetary fields render with two decimal places.

### Exit codes / error handling

Mirrors the COBOL file-status logic:

* Normal end of the result cursor = end-of-file → clean exit (`0`).
* An unexpected DB / read error logs `ERROR READING ACCOUNT FILE`, a file-status
  line (equivalent to `9910-DISPLAY-IO-STATUS`), and `ABENDING PROGRAM`, then
  exits non-zero — mirroring `9999-ABEND-PROGRAM` / `CEE3ABD`.

## Tests

```bash
cd python
pip install -r cbact01c/requirements-dev.txt
python -m pytest cbact01c
```
