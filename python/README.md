# CardDemo Bill Payment Module - Python Conversion

This is a Python conversion of the COBIL00C mainframe COBOL bill payment module from the CardDemo application.

## Overview

The bill payment module processes full balance payments for credit card accounts. It replicates the exact business logic from the COBOL program including:

- **Account Validation**: Validates account existence and checks for available balance
- **Transaction ID Generation**: Generates unique sequential transaction IDs
- **Transaction Creation**: Creates transaction records with proper timestamps
- **Account Balance Updates**: Updates account balance to zero after successful payment

## COBOL Source Reference

This Python implementation replicates the logic from:
- **Program**: `app/cbl/COBIL00C.cbl` (lines 1-573)
- **Copybooks**: 
  - `app/cpy/CVACT01Y.cpy` - Account record structure
  - `app/cpy/CVACT03Y.cpy` - Card cross-reference structure
  - `app/cpy/CVTRA05Y.cpy` - Transaction record structure
  - `app/cpy/COCOM01Y.cpy` - Communication area structure
- **BMS Screen**: `app/bms/COBIL00.bms` - Bill payment screen layout

## Architecture

### Components

- **`models.py`**: Pydantic data models mapping COBOL copybooks to Python classes
  - `AccountRecord` - Maps to CVACT01Y (300 bytes)
  - `CardXrefRecord` - Maps to CVACT03Y (50 bytes)
  - `TransactionRecord` - Maps to CVTRA05Y (350 bytes)

- **`services.py`**: Business logic layer replicating COBOL PROCESS-ENTER-KEY section
  - `BillPaymentService.validate_account()` - Maps to READ-ACCTDAT-FILE
  - `BillPaymentService.process_payment()` - Maps to payment processing logic

- **`api.py`**: REST API endpoints using FastAPI
  - `POST /api/v1/bill-payment/validate` - Validate account
  - `POST /api/v1/bill-payment/process` - Process payment

- **`data_store.py`**: In-memory storage simulating VSAM files
  - Simulates ACCTDAT, CXACAIX, and TRANSACT files
  - Includes sample data for testing

### COBOL to Python Mapping

| COBOL Construct | Python Equivalent |
|----------------|-------------------|
| COPY CVACT01Y | `class AccountRecord(BaseModel)` |
| EXEC CICS READ ACCTDAT | `data_store.get_account()` |
| EXEC CICS REWRITE ACCTDAT | `data_store.update_account()` |
| EXEC CICS READ CXACAIX | `data_store.get_card_xref()` |
| EXEC CICS STARTBR/READPREV | `data_store.get_highest_transaction_id()` |
| EXEC CICS WRITE TRANSACT | `data_store.write_transaction()` |
| WS-ERR-FLG / PERFORM SEND | Return dict with `success`/`error` |
| PIC S9(10)V99 | `Decimal` type for precision |
| PIC X(26) timestamp | ISO format datetime string |

## Installation

```bash
cd python
pip install -r requirements.txt
```

## Running the API

Start the development server:

```bash
cd python
uvicorn bill_payment.api:app --reload
```

The API will be available at http://localhost:8000

## API Documentation

Interactive API documentation (Swagger UI): http://localhost:8000/docs

### Example API Calls

**Validate Account**:
```bash
curl -X POST http://localhost:8000/api/v1/bill-payment/validate \
  -H "Content-Type: application/json" \
  -d '{"acct_id": "12345678901"}'
```

Response:
```json
{
  "success": true,
  "current_balance": 1500.50
}
```

**Process Payment**:
```bash
curl -X POST http://localhost:8000/api/v1/bill-payment/process \
  -H "Content-Type: application/json" \
  -d '{"acct_id": "12345678901", "confirm": "Y"}'
```

Response:
```json
{
  "success": true,
  "message": "Payment successful. Your Transaction ID is 0000000000000001.",
  "transaction_id": "0000000000000001",
  "amount_paid": 1500.50
}
```

## Testing

Run all tests:
```bash
cd python
pytest tests/ -v
```

Run tests with coverage:
```bash
pytest tests/ --cov=bill_payment --cov-report=html
```

### Test Coverage

- **`test_models.py`**: Tests Pydantic model validation and data structure
- **`test_services.py`**: Tests business logic including all error conditions
- **`test_api.py`**: Tests REST API endpoints and HTTP responses

## Sample Data

The application includes sample data for testing (loaded automatically on startup):

| Account ID  | Balance  | Card Number      |
|-------------|----------|------------------|
| 12345678901 | $1500.50 | 4111111111111111 |
| 98765432109 | $2500.75 | 4222222222222222 |
| 00000000000 | $0.00    | 4333333333333333 |

## Business Logic Validation

The Python implementation maintains exact parity with the COBOL business rules:

1. ✅ Empty account ID rejection
2. ✅ Non-existent account rejection  
3. ✅ Zero/negative balance rejection
4. ✅ Confirmation requirement (Y/N validation)
5. ✅ Sequential transaction ID generation
6. ✅ Timestamp generation (YYYY-MM-DD-HH:MM:SS.000000 format)
7. ✅ Transaction type code '02' for bill payments
8. ✅ Transaction category code 2
9. ✅ Account balance zeroing after payment
10. ✅ Transaction record creation with all required fields

## Error Messages

The implementation replicates the exact COBOL error messages:

- `"Acct ID can NOT be empty..."` - Empty account ID
- `"Account ID NOT found..."` - Invalid account ID
- `"You have nothing to pay..."` - Zero or negative balance
- `"Confirm to make a bill payment..."` - Missing or invalid confirmation
- `"Account ID NOT found in cross-reference..."` - Missing card cross-reference

## Future Enhancements

- Replace in-memory storage with PostgreSQL/MySQL database
- Add authentication and authorization (JWT tokens)
- Add transaction history query endpoints
- Add payment scheduling capabilities
- Add partial payment support (not just full balance)
- Add email/SMS notifications for successful payments
- Add audit logging for compliance
- Add rate limiting and DDoS protection
- Add Docker containerization
- Add Kubernetes deployment manifests
- Add CI/CD pipeline integration

## Design Decisions

### In-Memory Storage
Using Python dictionaries for proof-of-concept. Production deployment would use:
- PostgreSQL for transactional data
- Redis for caching and session management
- Message queue (RabbitMQ/Kafka) for async processing

### Decimal Precision
Using Python's `Decimal` type to match COBOL COMP-3 precision for financial calculations.

### Type Safety
Using Pydantic for data validation matching COBOL PIC clauses:
- `PIC 9(11)` → `str` with length validation
- `PIC S9(10)V99` → `Decimal` with precision
- `PIC X(n)` → `str` with max_length

### Error Handling
Matching COBOL RESP code evaluation patterns with HTTP status codes:
- DFHRESP(NORMAL) → 200 OK
- DFHRESP(NOTFND) → 400 Bad Request
- Other errors → 400 Bad Request with descriptive message

## License

This project inherits the Apache 2.0 license from the parent CardDemo repository.

## Contributing

See the main repository CONTRIBUTING.md for guidelines.
