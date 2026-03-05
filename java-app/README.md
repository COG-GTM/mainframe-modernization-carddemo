# CardDemo - Java 21 Spring Boot Migration

Migrated from the AWS CardDemo mainframe COBOL/CICS/VSAM credit card management application to Java 21 with Spring Boot 3.2.5.

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose (for PostgreSQL and ActiveMQ)

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d postgres activemq
```

This starts:
- **PostgreSQL 16** on port 5432 (database: `carddemo`, user: `carddemo`, password: `carddemo`)
- **ActiveMQ 6.1** on port 61616 (AMQP) and 8161 (web console, admin/admin)

### 2. Build

```bash
mvn clean package -DskipTests
```

### 3. Run the Online Module

```bash
java -jar carddemo-online/target/carddemo-online-1.0.0-SNAPSHOT.jar --spring.profiles.active=seed
```

The `seed` profile loads sample data from the original mainframe data files on first startup.

The REST API is available at `http://localhost:8080`.

### 4. Run Batch Jobs

```bash
java -jar carddemo-batch/target/carddemo-batch-1.0.0-SNAPSHOT.jar --spring.batch.job.name=nightlyCycleJob
```

### 5. Run with Docker Compose (Full Stack)

```bash
docker-compose up --build
```

## Project Structure

```
java-app/
├── pom.xml                     — Parent POM (Java 21, Spring Boot 3.2.5)
├── carddemo-common/            — Shared entities, repositories, DTOs, enums, utilities
├── carddemo-online/            — REST controllers and online services (port 8080)
├── carddemo-batch/             — Spring Batch jobs
├── carddemo-authorization/     — Authorization module with JMS
├── carddemo-messaging/         — MQ/JMS integration (port 8082)
├── Dockerfile                  — Multi-stage build for carddemo-online
└── docker-compose.yml          — Full stack (PostgreSQL, ActiveMQ, app)
```

## API Endpoints

### Authentication
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| POST | `/api/auth/login` | Login | COSGN00C |
| POST | `/api/auth/logout` | Logout | COSGN00C |

### Account Management
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/accounts/{acctId}` | View account | COACTVWC |
| PUT | `/api/accounts/{acctId}` | Update account | COACTUPC |

### Card Management
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/accounts/{acctId}/cards?page=0&size=10` | List cards | COCRDLIC |
| GET | `/api/cards/{cardNum}` | Card detail | COCRDSLC |
| PUT | `/api/cards/{cardNum}` | Update card | COCRDUPC |

### Transaction Management
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/accounts/{acctId}/transactions?page=0&size=10` | List transactions | COTRN00C |
| GET | `/api/transactions/{transactionId}` | Transaction detail | COTRN01C |
| POST | `/api/transactions` | Add transaction | COTRN02C |

### Bill Payment
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| POST | `/api/accounts/{acctId}/bill-payment` | Pay bill | COBIL00C |

### Reports
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| POST | `/api/reports/transaction-report` | Submit report | CORPT00C |
| GET | `/api/reports/{reportId}` | Get report status | CORPT00C |

### User Administration (Admin only)
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/admin/users?page=0&size=10` | List users | COUSR00C |
| GET | `/api/admin/users/{userId}` | View user | COUSR01C |
| POST | `/api/admin/users` | Add user | COUSR02C |
| PUT | `/api/admin/users/{userId}` | Update user | COUSR03C |
| DELETE | `/api/admin/users/{userId}` | Delete user | COUSR03C |

### Transaction Type Management (Admin only)
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/admin/transaction-types?page=0&size=10` | List types | COTRTLIC |
| POST | `/api/admin/transaction-types` | Add type | COTRTUPC |
| PUT | `/api/admin/transaction-types/{typeCode}` | Update type | COTRTUPC |
| DELETE | `/api/admin/transaction-types/{typeCode}` | Delete type | COTRTUPC |

### Authorization
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/authorizations?cardNum={cardNum}&page=0&size=10` | List authorizations | COPAUS0C |
| GET | `/api/authorizations/{authId}` | Authorization detail | COPAUS1C |
| PUT | `/api/authorizations/{authId}/fraud` | Mark fraud | COPAUS2C |
| POST | `/api/authorizations` | Process authorization | COPAUA0C |

### External/Messaging
| Method | Path | Description | COBOL Source |
|--------|------|-------------|--------------|
| GET | `/api/external/accounts/{acctId}` | External account query | COACCT01 |
| GET | `/api/external/system-date` | System date | CODATE01 |

## Batch Jobs

| Job Name | Description | COBOL/JCL Source |
|----------|-------------|------------------|
| `postTransactionsJob` | Post daily transactions, validate cards/accounts/limits | CBTRN02C / POSTTRAN.jcl |
| `interestCalculationJob` | Calculate monthly interest per category balance | CBACT04C / INTCALC.jcl |
| `statementGenerationJob` | Generate account statements (text + HTML) | CBSTM03A+B / CREASTMT.JCL |
| `transactionReportJob` | Generate transaction reports | CBTRN03C / TRANREPT.jcl |
| `nightlyCycleJob` | Orchestrate all batch jobs in sequence | JCL job chain |
| `authorizationPurgeJob` | Purge expired authorizations (>90 days) | CBPAUP0C |

Run a specific job:
```bash
java -jar carddemo-batch/target/carddemo-batch-1.0.0-SNAPSHOT.jar \
  --spring.batch.job.name=postTransactionsJob
```

## COBOL Program to Java Class Mapping

| COBOL Program | Java Class | Module |
|---------------|------------|--------|
| COSGN00C.cbl | AuthenticationService, AuthenticationController | online |
| COACTVWC.cbl | AccountService.getAccount() | online |
| COACTUPC.cbl | AccountService.updateAccount() | online |
| COCRDLIC.cbl | CardService.listCards() | online |
| COCRDSLC.cbl | CardService.getCard() | online |
| COCRDUPC.cbl | CardService.updateCard() | online |
| COTRN00C.cbl | TransactionService.listTransactions() | online |
| COTRN01C.cbl | TransactionService.getTransaction() | online |
| COTRN02C.cbl | TransactionService.addTransaction() | online |
| COBIL00C.cbl | BillPaymentService.payBill() | online |
| CORPT00C.cbl | ReportService.submitTransactionReport() | online |
| COUSR00C.cbl | UserAdminService.listUsers() | online |
| COUSR01C.cbl | UserAdminService.getUser() | online |
| COUSR02C.cbl | UserAdminService.addUser() | online |
| COUSR03C.cbl | UserAdminService.updateUser/deleteUser() | online |
| CBTRN02C.cbl | TransactionPostingProcessor | batch |
| CBACT04C.cbl | InterestCalculationProcessor | batch |
| CBSTM03A.CBL | StatementGenerationProcessor | batch |
| CBSTM03B.CBL | StatementWriter | batch |
| CBTRN03C.cbl | transactionReportJob (BatchConfig) | batch |
| COPAUA0C.cbl | AuthorizationProcessor | authorization |
| COPAUS0C.cbl | AuthorizationController (list) | authorization |
| COPAUS1C.cbl | AuthorizationController (detail) | authorization |
| COPAUS2C.cbl | AuthorizationController (fraud) | authorization |
| CBPAUP0C.cbl | AuthorizationPurgeConfig | authorization |
| COTRTLIC.cbl | TransactionTypeService (list) | online |
| COTRTUPC.cbl | TransactionTypeService (CRUD) | online |
| COACCT01.cbl | AccountQueryListener, AccountExternalController | messaging |
| CODATE01.cbl | AccountExternalController.getSystemDate() | messaging |
| CSUTLDTC.cbl | DateUtils | common |
| COBDATFT.asm | DateUtils | common |
| CEE3ABD | ApplicationAbendException | common |
| MVSWAIT | WaitUtils | common |

## Data Migration

The seed data loader (`SeedDataLoader.java`) reads the original mainframe data files from `app/data/ASCII/` and loads them into PostgreSQL:

| VSAM File | Data File | JPA Entity | Table |
|-----------|-----------|------------|-------|
| ACCTDATA | ACCTDATA.txt | Account | account |
| CARDDATA | CARDDATA.txt | Card | card |
| CUSTDATA | CUSTDATA.txt | Customer | customer |
| CARDXREF | CARDXREF.txt | CardXref | card_xref |
| DALYTRAN | DALYTRAN.txt | DailyTransaction | daily_transaction |
| TCATBALF | TCATBALF.txt | TransactionCategoryBalance | transaction_category_balance |
| DISCGRP | DISCGRP.txt | DisclosureGroup | disclosure_group |
| TRTEFIL | TRTEFIL.txt | TransactionType | transaction_type |
| TRNXFILE | n/a | Transaction | transaction |

## Technology Stack

| Component | Mainframe | Java Migration |
|-----------|-----------|----------------|
| Language | COBOL | Java 21 |
| Runtime | CICS | Spring Boot 3.2.5 |
| Database | VSAM / DB2 | PostgreSQL 16 |
| ORM | Native I/O | Spring Data JPA / Hibernate 6 |
| Security | RACF | Spring Security + BCrypt |
| Batch | JCL | Spring Batch 5.x |
| Messaging | MQ Series | Spring JMS + ActiveMQ |
| Transactions | CICS SYNCPOINT | Spring @Transactional |
| Session | COMMAREA | HTTP Session (@SessionScope) |
| DB Migrations | DDL scripts | Flyway |
| Deployment | z/OS | Docker + docker-compose |

## Design Decisions

1. **CLOSEFIL/OPENFIL removed**: The original mainframe batch locked VSAM files before batch runs and unlocked after. This is unnecessary with an RDBMS that handles concurrency natively.

2. **z/OS control block addressing removed**: Statement generation (CBSTM03A) used PSA/TCB/TIOT addressing to discover DD names. Replaced with Spring `@ConfigurationProperties`.

3. **ALTER...GO TO replaced**: Dynamic dispatch patterns in COBOL replaced with simple if/else logic.

4. **IMS PSB scheduling removed**: IMS DL/I calls replaced with Spring Data JPA repository methods.

5. **Two-phase commit simplified**: IMS+DB2 distributed transactions replaced with single-database `@Transactional`.

6. **Interest rounding**: Uses `BigDecimal` with `RoundingMode.HALF_UP` to match COBOL COMP-3 behavior.

7. **Signed decimal parsing**: `SeedDataLoader.parseSignedDecimal()` handles COBOL trailing sign format (e.g., `{` = +0, `}` = -0, `A`-`I` = +1 to +9, `J`-`R` = -1 to -9).
