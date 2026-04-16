# CardDemo PostgreSQL Schema (`carddemo-schema`)

Database schema module for the CardDemo credit card processing application. Translates all 12 VSAM file structures from the legacy mainframe COBOL application to a PostgreSQL relational schema, managed via [Flyway](https://flywaydb.org/) migrations.

## VSAM-to-PostgreSQL Mapping

| # | VSAM File | Type | Copybook | PostgreSQL Table | Primary Key |
|---|-----------|------|----------|------------------|-------------|
| 1 | ACCTDAT | KSDS | CVACT01Y (RECLN 300) | `accounts` | `acct_id` (BIGINT) |
| 2 | CARDDAT | KSDS | CVACT02Y (RECLN 150) | `cards` | `card_num` (VARCHAR 16) |
| 3 | CARDXREF | KSDS | CVACT03Y (RECLN 50) | `card_xref` | `card_num` (VARCHAR 16) |
| 4 | CUSTDAT | KSDS | CVCUS01Y (RECLN 500) | `customers` | `cust_id` (BIGINT) |
| 5 | USRSEC | KSDS | CSUSR01Y (RECLN 80) | `users` | `usr_id` (VARCHAR 8) |
| 6 | TRANSACT | KSDS | CVTRA05Y (RECLN 350) | `transactions` | `tran_id` (VARCHAR 16) |
| 7 | DALYTRAN | ESDS | CVTRA06Y (RECLN 350) | `daily_transactions` | `id` (IDENTITY, surrogate) |
| 8 | DALYREJS | ESDS | _(same as DALYTRAN)_ | `daily_rejects` | `id` (IDENTITY, surrogate) |
| 9 | TCATBALF | KSDS | CVTRA01Y (RECLN 50) | `tran_cat_balances` | `(acct_id, type_cd, cat_cd)` |
| 10 | DISCGRP | KSDS | CVTRA02Y (RECLN 50) | `disclosure_groups` | `(acct_group_id, tran_type_cd, tran_cat_cd)` |
| 11 | TRANTYPE | KSDS | CVTRA03Y (RECLN 60) | `transaction_types` | `type_cd` (CHAR 2) |
| 12 | TRANCATG | KSDS | CVTRA04Y (RECLN 60) | `transaction_categories` | `(type_cd, cat_cd)` |

## Entity Relationship Diagram

```
                    ┌──────────────────┐
                    │   customers      │
                    │ PK: cust_id      │
                    └────────┬─────────┘
                             │ 1
                             │
                             │ FK
                             ▼ N
┌────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│   accounts     │◄─│   card_xref      │  │   cards            │
│ PK: acct_id    │  │ PK: card_num     │  │ PK: card_num       │
└───┬──────┬─────┘  │ FK: cust_id      │  │ FK: acct_id ──────►│
    │      │        │ FK: acct_id      │  └────────────────────┘
    │      │        └──────────────────┘
    │      │
    │      └──────────────────────────────┐
    │ 1                                   │ 1
    │                                     │
    ▼ N                                   ▼ N
┌──────────────────┐             ┌──────────────────────┐
│ tran_cat_balances│             │ disclosure_groups     │
│ PK: (acct_id,    │             │ PK: (acct_group_id,  │
│  type_cd, cat_cd)│             │  tran_type_cd,       │
└──────────────────┘             │  tran_cat_cd)        │
                                 └──────────────────────┘

┌────────────────────┐    ┌────────────────────────────┐
│ transaction_types  │◄───│ transaction_categories     │
│ PK: type_cd        │ FK │ PK: (type_cd, cat_cd)      │
└────────────────────┘    └────────────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌──────────────┐
│ transactions       │  │ daily_transactions │  │ daily_rejects│
│ PK: tran_id        │  │ PK: id (IDENTITY)  │  │ PK: id       │
└────────────────────┘  └────────────────────┘  └──────────────┘

┌──────────────┐
│ users        │
│ PK: usr_id   │
└──────────────┘
```

## Migration Files

| File | Description |
|------|-------------|
| `V1__create_schema.sql` | Creates all 12 tables with column types, primary keys, foreign keys, check constraints, and table/column comments |
| `V2__create_indexes.sql` | Creates secondary indexes matching VSAM Alternate Index (AIX) paths and common query patterns |
| `V3__seed_reference_data.sql` | Populates `transaction_types` (10 types) and `transaction_categories` (45 categories) reference data |

## Key Design Decisions

### Data Types
- **COBOL `PIC 9(n)`** &rarr; `BIGINT` for identifiers, `SMALLINT` for small numeric codes
- **COBOL `PIC S9(n)V99`** &rarr; `NUMERIC(n+2,2)` for exact decimal arithmetic (no floating point)
- **COBOL `PIC X(n)`** &rarr; `VARCHAR(n)` for variable-length strings, `CHAR(n)` for fixed codes
- **FILLER fields** are dropped (padding not needed in relational storage)

### ESDS (Append-Only) Files
VSAM ESDS files (`DALYTRAN`, `DALYREJS`) have no natural primary key. These tables use a `BIGINT GENERATED ALWAYS AS IDENTITY` surrogate key.

### Password Storage
The `users.password` column is sized at `VARCHAR(72)` to accommodate bcrypt hashes, replacing the original `PIC X(08)` plaintext storage from the mainframe.

### Composite Keys
`tran_cat_balances` and `disclosure_groups` use composite primary keys matching the VSAM composite key structures defined in the copybooks (`CVTRA01Y`, `CVTRA02Y`).

## Indexes

Secondary indexes mirror the VSAM Alternate Index (AIX) paths:

| Index | Table | Column(s) | VSAM AIX Source |
|-------|-------|-----------|-----------------|
| `idx_cards_acct_id` | cards | acct_id | CARDDATA AIX |
| `idx_card_xref_acct_id` | card_xref | acct_id | CXACAIX |
| `idx_card_xref_cust_id` | card_xref | cust_id | Customer lookup |
| `idx_transactions_card_num` | transactions | card_num | Transaction browse by card |
| `idx_transactions_type_cat` | transactions | type_cd, cat_cd | Category reporting |
| `idx_daily_transactions_card_num` | daily_transactions | card_num | Daily processing |
| `idx_daily_transactions_tran_id` | daily_transactions | tran_id | Transaction lookup |
| `idx_daily_rejects_card_num` | daily_rejects | card_num | Reject investigation |
| `idx_daily_rejects_tran_id` | daily_rejects | tran_id | Reject tracing |
| `idx_tran_cat_balances_acct_id` | tran_cat_balances | acct_id | Account balance summary |
| `idx_accounts_group_id` | accounts | group_id | Disclosure group lookup |
| `idx_customers_last_name` | customers | last_name | Customer name search |
| `idx_customers_ssn` | customers | ssn | SSN lookup (unique, partial) |

## Prerequisites

- **Java 17** (or later)
- **PostgreSQL 14+**
- **Gradle 8+** (wrapper included)

## Usage

### Run Migrations

```bash
# Set environment variables (or use defaults: localhost:5432/carddemo)
export FLYWAY_URL=jdbc:postgresql://localhost:5432/carddemo
export FLYWAY_USER=carddemo
export FLYWAY_PASSWORD=carddemo

# Run migrations
cd carddemo-schema
../gradlew flywayMigrate
```

### Verify Migration Status

```bash
cd carddemo-schema
../gradlew flywayInfo
```

### Build

```bash
cd carddemo-schema
../gradlew build
```

## Source Copybook Reference

| Copybook | VSAM File | Record Length | Description |
|----------|-----------|--------------|-------------|
| CVACT01Y | ACCTDAT | 300 | Account record layout |
| CVACT02Y | CARDDAT | 150 | Card record layout |
| CVACT03Y | CARDXREF | 50 | Card cross-reference layout |
| CVCUS01Y | CUSTDAT | 500 | Customer record layout |
| CSUSR01Y | USRSEC | 80 | User security record layout |
| CVTRA05Y | TRANSACT | 350 | Transaction record layout |
| CVTRA06Y | DALYTRAN | 350 | Daily transaction record layout |
| CVTRA01Y | TCATBALF | 50 | Transaction category balance layout |
| CVTRA02Y | DISCGRP | 50 | Disclosure group record layout |
| CVTRA03Y | TRANTYPE | 60 | Transaction type record layout |
| CVTRA04Y | TRANCATG | 60 | Transaction category record layout |
