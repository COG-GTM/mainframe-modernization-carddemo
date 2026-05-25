# CardDemo Modernized — Transaction Posting API

REST API migrated from the COBOL batch program **CBTRN02C.cbl** (daily transaction posting) to **Python / FastAPI / SQLAlchemy / PostgreSQL**.

---

## COBOL → Python Mapping

| COBOL Paragraph | Lines | Python Function | Description |
|---|---|---|---|
| `PROCEDURE DIVISION` (main loop) | 193-234 | `process_batch()` | Read all pending daily transactions and process them |
| `1000-DALYTRAN-GET-NEXT` | 337-369 | DB query `processed == False` | Get next unprocessed daily transaction |
| `1500-VALIDATE-TRAN` | 370-421 | `validate_transaction()` | Run all validations on a transaction |
| `1500-A-LOOKUP-XREF` | 380-392 | XREF lookup in `validate_transaction()` | Look up card number in cross-reference |
| `1500-B-LOOKUP-ACCT` | 393-421 | Account lookup + credit/expiry checks | Validate account exists, credit limit, expiration |
| `2000-POST-TRANSACTION` | 424-444 | `post_transaction()` | Post a valid transaction |
| `2500-WRITE-REJECT-REC` | 446-465 | `reject_transaction()` | Write rejected transaction record |
| `2700-UPDATE-TCATBAL` | 467-542 | `_update_tcatbal()` | Create or update transaction category balance |
| `2700-A-CREATE-TCATBAL-REC` | 503-524 | Create new `TranCatBalance` | Create category balance record |
| `2700-B-UPDATE-TCATBAL-REC` | 526-542 | Add amount to existing `balance` | Update existing category balance |
| `2800-UPDATE-ACCOUNT-REC` | 545-560 | `_update_account()` | Update account balances |
| `2900-WRITE-TRANSACTION-FILE` | 562-579 | `db.add(tran)` | Write to transaction file |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | 692-705 | `_get_db2_format_timestamp()` | Generate DB2-format timestamp |

## Copybook → Model Mapping

| Copybook | COBOL Record | SQLAlchemy Model | Table |
|---|---|---|---|
| `CVACT01Y.cpy` | `ACCOUNT-RECORD` | `Account` | `accounts` |
| `CVACT03Y.cpy` | `CARD-XREF-RECORD` | `CardXref` | `card_xrefs` |
| `CVTRA06Y.cpy` | `DALYTRAN-RECORD` | `DailyTransaction` | `daily_transactions` |
| `CVTRA05Y.cpy` | `TRAN-RECORD` | `Transaction` | `transactions` |
| `CVTRA01Y.cpy` | `TRAN-CAT-BAL-RECORD` | `TranCatBalance` | `tran_cat_balances` |
| — | `REJECT-RECORD` | `RejectedTransaction` | `rejected_transactions` |

## Rejection Codes

| Code | COBOL Constant | Reason |
|---|---|---|
| 100 | `WS-VALIDATION-FAIL-REASON` | `INVALID CARD NUMBER FOUND` — card not in XREF |
| 101 | `WS-VALIDATION-FAIL-REASON` | `ACCOUNT RECORD NOT FOUND` — account ID from XREF not in accounts |
| 102 | `WS-VALIDATION-FAIL-REASON` | `OVERLIMIT TRANSACTION` — credit limit exceeded |
| 103 | `WS-VALIDATION-FAIL-REASON` | `TRANSACTION RECEIVED AFTER ACCT EXPIRATION` — account expired |

## Processing Flow

```
┌─────────────────┐
│  Upload daily    │   POST /api/v1/transactions/upload
│  transactions    │   (equivalent to loading DALYTRAN file)
└────────┬────────┘
         ▼
┌─────────────────┐
│  Process batch   │   POST /api/v1/transactions/process-batch
│  (POSTTRAN job)  │   (equivalent to running JCL POSTTRAN)
└────────┬────────┘
         ▼
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌──────────┐
│ Valid  │ │ Invalid  │
│        │ │          │
│ → Post │ │ → Reject │
└───┬────┘ └────┬─────┘
    ▼           ▼
┌────────────┐ ┌──────────────────┐
│transactions│ │rejected_         │
│ table      │ │transactions table│
│ + update   │ └──────────────────┘
│   account  │
│ + update   │
│   tcatbal  │
└────────────┘
```

## Quick Start with Docker Compose

```bash
cd modernized/

# Start PostgreSQL + API
docker-compose up -d --build

# Run migrations
docker-compose exec api alembic upgrade head

# Seed data from COBOL ASCII files
docker-compose exec api python -m scripts.seed_data

# API is available at http://localhost:8000
# Docs at http://localhost:8000/docs
```

## Local Development

```bash
cd modernized/

# Create virtual environment
python -m venv .venv
source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Set DATABASE_URL (or use default localhost)
export DATABASE_URL=postgresql://carddemo:carddemo@localhost:5432/carddemo

# Run migrations
alembic upgrade head

# Seed data
python -m scripts.seed_data

# Start server
uvicorn app.main:app --reload --port 8000
```

## Running Tests

```bash
cd modernized/
pip install -r requirements.txt
pytest tests/ -v
```

Tests use SQLite in-memory — no PostgreSQL required.

## API Examples

### Upload transactions

```bash
curl -X POST http://localhost:8000/api/v1/transactions/upload \
  -H "Content-Type: application/json" \
  -d '{
    "transactions": [
      {
        "tran_id": "0000000000000001",
        "type_cd": "01",
        "cat_cd": 1,
        "source": "POS TERM",
        "description": "Purchase at Store ABC",
        "amount": "50.47",
        "merchant_id": 800000000,
        "merchant_name": "Store ABC",
        "merchant_city": "New York",
        "merchant_zip": "10001",
        "card_num": "4859452612877065",
        "orig_ts": "2025-06-10 19:27:53.000000"
      }
    ]
  }'
```

### Process all pending (batch)

```bash
curl -X POST http://localhost:8000/api/v1/transactions/process-batch \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Process a single transaction

```bash
curl -X POST http://localhost:8000/api/v1/transactions/process \
  -H "Content-Type: application/json" \
  -d '{
    "tran_id": "0000000000000002",
    "type_cd": "01",
    "cat_cd": 1,
    "source": "POS TERM",
    "description": "Purchase at Store XYZ",
    "amount": "25.00",
    "merchant_id": 800000001,
    "merchant_name": "Store XYZ",
    "merchant_city": "Boston",
    "merchant_zip": "02101",
    "card_num": "4859452612877065",
    "orig_ts": "2025-06-10 20:00:00.000000"
  }'
```

### List posted transactions

```bash
curl "http://localhost:8000/api/v1/transactions?page=1&per_page=10"
```

### Get a specific transaction

```bash
curl http://localhost:8000/api/v1/transactions/0000000000000001
```

### List rejected transactions

```bash
curl http://localhost:8000/api/v1/transactions/rejected
```

### Health check

```bash
curl http://localhost:8000/health
```
