# CardDemo Transaction ETL

An extract / transform / load pipeline that migrates the COBOL `TRANSACT` VSAM
file (copybook `CVTRA05Y`) into a modern PostgreSQL `transactions` table.

It is the direct implementation of the field mapping in [`MAPPING.md`](MAPPING.md),
which was produced with the `data-mapper` skill
(`.devin/skills/data-mapper/SKILL.md`).

## Pipeline

```
source file ──▶ extract ──▶ transform ──▶ load ──▶ sink
(ASCII/EBCDIC)   (fixed-     (typed,        (postgres / csv / json)
                 width slice) overpunch,
                              ts parse,
                              PAN mask)
```

| Stage | Module | Responsibility |
|-------|--------|----------------|
| Layout | `carddemo_etl/layout.py` | Field definitions + byte offsets (single source of truth) |
| Extract | `carddemo_etl/extract.py` | Read fixed-width records (ASCII newline-framed or EBCDIC fixed-length) |
| Transform | `carddemo_etl/transform.py`, `transforms.py` | Overpunch sign decode, implied decimals, timestamp parsing, PAN masking |
| Load | `carddemo_etl/load.py` | PostgreSQL UPSERT (`psycopg2`), or CSV / JSON sinks |
| CLI | `carddemo_etl/cli.py` | `run` and `schema` commands |

## Usage

```bash
# Dry-run against the ASCII seed data → masked JSON (no DB, stdlib only)
PYTHONPATH=etl python -m carddemo_etl.cli run \
    --source app/data/ASCII/dailytran.txt \
    --sink json --out /tmp/transactions.json --mask-pan

# Load the EBCDIC source into PostgreSQL
PYTHONPATH=etl python -m carddemo_etl.cli run \
    --source app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS \
    --encoding ebcdic \
    --sink postgres \
    --dsn postgresql://carddemo:carddemo@localhost:5432/carddemo

# Print the DDL
PYTHONPATH=etl python -m carddemo_etl.cli schema
```

The `--encoding ebcdic` and `--encoding ascii` paths are validated to produce
identical output, giving an equivalence check between the mainframe physical
file and the converted seed data.

## Setup

```bash
cd etl
pip install -r requirements.txt      # psycopg2-binary (postgres sink) + pytest
psql "$DSN" -f schema.sql            # create the transactions table
```

The extract/transform stages and the CSV/JSON sinks need **no third-party
dependencies** — `psycopg2` is only imported (lazily) for the PostgreSQL sink.

## Tests

```bash
cd etl
PYTHONPATH=. python -m pytest -q
```

Tests cover overpunch sign decoding (full alphabet), timestamp parsing, PAN
masking, byte-offset contiguity, and a full pipeline run over the 300-record
`dailytran.txt` fixture.

## Notes / known mainframe gotchas

- **`TRAN-AMT` overpunch sign** — the trailing byte encodes both the last digit
  and the sign (`G` → `7` positive, `}` → `0` negative). See `MAPPING.md`.
- **`TRAN-PROC-TS`** is blank for unprocessed records and maps to `NULL`.
- **`TRAN-CARD-NUM`** is a plaintext PAN (PCI). Use `--mask-pan` for non-prod.
