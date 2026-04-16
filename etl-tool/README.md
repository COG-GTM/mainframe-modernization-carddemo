# CardDemo EBCDIC ETL Migration Tool

A Spring Boot command-line tool that migrates EBCDIC-encoded data files from the CardDemo mainframe application into PostgreSQL.

## Overview

This ETL tool parses fixed-length EBCDIC records from the CardDemo sample data files, converts them to their Java equivalents, and loads them into a PostgreSQL database using JDBC batch inserts.

### Supported Data Files

| File | Record Layout | RECLN | Description |
|------|--------------|-------|-------------|
| `AWS.M2.CARDDEMO.ACCTDATA.PS` | CVACT01Y | 300 | Account records |
| `AWS.M2.CARDDEMO.CARDDATA.PS` | CVACT02Y | 150 | Card records |
| `AWS.M2.CARDDEMO.CARDXREF.PS` | CVACT03Y | 50 | Card cross-reference records |
| `AWS.M2.CARDDEMO.CUSTDATA.PS` | CVCUS01Y | 500 | Customer records |
| `AWS.M2.CARDDEMO.USRSEC.PS` | CSUSR01Y | 80 | User security records |
| `AWS.M2.CARDDEMO.DALYTRAN.PS` | CVTRA06Y | 350 | Daily transaction records |
| `AWS.M2.CARDDEMO.TCATBALF.PS` | CVTRA01Y | 50 | Category balance records |
| `AWS.M2.CARDDEMO.DISCGRP.PS` | CVTRA02Y | 50 | Disclosure group records |
| `AWS.M2.CARDDEMO.TRANTYPE.PS` | CVTRA03Y | 60 | Transaction type records |
| `AWS.M2.CARDDEMO.TRANCATG.PS` | CVTRA04Y | 60 | Transaction category records |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or use H2 profile for testing)

## Configuration

Edit `src/main/resources/application.yml` or pass as command-line arguments:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/carddemo
    username: carddemo
    password: carddemo

etl:
  source-dir: app/data/EBCDIC        # Path to EBCDIC data files
  truncate-before-load: true          # Idempotent re-run support
  validate-xref: true                 # Cross-reference integrity check
```

## Build

```bash
cd etl-tool
mvn clean package
```

## Run

### With PostgreSQL
```bash
java -jar target/carddemo-etl-tool-1.0.0-SNAPSHOT.jar \
  --etl.source-dir=../app/data/EBCDIC
```

### With H2 (no PostgreSQL required)
```bash
java -jar target/carddemo-etl-tool-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=h2 \
  --etl.source-dir=../app/data/EBCDIC
```

## Features

- **EBCDIC Parsing**: Handles EBCDIC character encoding (IBM037), zoned decimal numerics, and signed display fields (PIC S9(n)V99)
- **Idempotent Migration**: Truncate-and-reload strategy ensures the migration can be re-run safely
- **Cross-Reference Validation**: Verifies that card_xref records link to valid accounts, customers, and cards
- **Migration Report**: Logs record counts per file, conversion errors, and validation results
- **Schema Management**: Flyway migrations auto-create the target schema on startup

## EBCDIC Field Handling

| COBOL PIC Clause | Java Type | Conversion |
|-----------------|-----------|------------|
| `PIC X(n)` | `String` | IBM037 charset decode + trim |
| `PIC 9(n)` | `long` / `int` | Zoned decimal digit extraction |
| `PIC S9(n)V99` | `BigDecimal` | Sign nibble detection + decimal point placement |

## Database Schema

The tool creates 10 tables via Flyway migration:

- `accounts` - Credit card accounts
- `customers` - Customer master data
- `cards` - Card information
- `card_xref` - Card-to-account-to-customer cross-reference
- `user_security` - User authentication records
- `daily_transactions` - Daily transaction log
- `transactions` - Transaction history
- `tran_cat_balance` - Transaction category balances
- `disclosure_group` - Disclosure group interest rates
- `tran_type` / `tran_category` - Reference data
