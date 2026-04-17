# CardDemo Microservices Architecture

This directory contains the modernized Java/Spring Boot microservices implementation of the CardDemo COBOL mainframe application. The original 28 COBOL programs have been decomposed into 7 domain microservices, an API gateway, and a shared library.

## Architecture Overview

```
                                    +------------------+
                                    |   API Gateway    |
                                    | (Spring Cloud)   |
                                    |   Port: 8080     |
                                    +--------+---------+
                                             |
                 +---------------------------+---------------------------+
                 |           |           |           |           |       |
          +------+---+ +----+-----+ +---+------+ +--+-------+ +-+------++ +--------+
          |  Auth    | | Account  | |  Card    | |Transaction| |Billing | |Statement|
          | Service  | | Service  | | Service  | | Service   | |Service | |Service  |
          | :8081    | | :8082    | | :8083    | | :8084     | | :8087  | | :8085   |
          +------+---+ +----+-----+ +---+------+ +--+-------+ +-+------+ +--------+
                 |           |           |           |           |
          +------+-----------+-----------+-----------+-----------+--------+
          |                      PostgreSQL :5432                         |
          +--------------------------------------------------------------+
                                         |
          +------------------------------+-------------------------------+
          |                      RabbitMQ :5672                          |
          +-------------------------------------------------------------+
                                                          +-------------+
                                                          | User Admin  |
                                                          | Service     |
                                                          | :8086       |
                                                          +-------------+
```

## COBOL-to-Microservice Mapping

| Microservice | COBOL Programs | Description |
|---|---|---|
| **Auth Service** | `COSGN00C` | User authentication, JWT token generation |
| **Account Service** | `COACTVWC`, `COACTUPC`, `CBACT01C`, `CBACT04C` | Account view, update, interest calculation |
| **Card Service** | `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `CBACT02C`, `CBACT03C` | Card list, view, update, cross-reference lookup |
| **Transaction Service** | `COTRN00C`, `COTRN01C`, `COTRN02C`, `CBTRN01C`, `CBTRN02C`, `CBTRN03C` | Transaction CRUD, batch posting |
| **Statement Service** | `CBSTM03A`, `CBSTM03B` | Statement generation (text + HTML) |
| **User Admin Service** | `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C` | User CRUD (admin only) |
| **Billing Service** | `COBIL00C`, `CORPT00C` | Bill payment (saga), report submission |
| **API Gateway** | `COMEN01C`, `COADM01C` | Route mapping from menu navigation |
| **Shared Library** | `CSUTLDTC` + copybooks | Date validation, shared DTOs |

## Services

### Auth Service (port 8081)
Translates `COSGN00C.cbl` sign-on logic. Authenticates users and issues JWT tokens with user type (admin/regular) in claims.

**Endpoints:**
- `POST /auth/login` - Authenticate and receive JWT token
- `GET /actuator/health` - Health check

### Account Service (port 8082)
Translates `COACTVWC`, `COACTUPC`, `CBACT01C`, `CBACT04C`. Manages account data with cross-service calls to Card Service.

**Endpoints:**
- `GET /accounts/{id}` - View account
- `PUT /accounts/{id}` - Update account
- `POST /accounts/{id}/calculate-interest` - Run interest calculation
- `GET /actuator/health` - Health check

### Card Service (port 8083)
Translates `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `CBACT02C`, `CBACT03C`. Manages credit cards and the critical cross-reference lookup.

**Endpoints:**
- `GET /cards` - List cards
- `GET /cards/{cardNum}` - View card details
- `PUT /cards/{cardNum}` - Update card
- `GET /cards/xref/{accountId}` - Cross-reference lookup
- `GET /actuator/health` - Health check

### Transaction Service (port 8084)
Translates `COTRN00C`, `COTRN01C`, `COTRN02C`, `CBTRN01C-03C`. Publishes balance-update events to RabbitMQ consumed by Account Service.

**Endpoints:**
- `GET /transactions` - List transactions
- `GET /transactions/{id}` - View transaction
- `POST /transactions` - Create transaction
- `POST /transactions/batch-post` - Batch post daily transactions
- `GET /actuator/health` - Health check

### Statement Service (port 8085)
Translates `CBSTM03A` + `CBSTM03B` (batch statement generation). Calls Account, Card, and Transaction services to assemble statements.

**Endpoints:**
- `POST /statements/generate` - Generate account statement
- `GET /actuator/health` - Health check

### User Admin Service (port 8086)
Translates `COUSR00C-03C`. Cleanest bounded context -- only touches the user security file.

**Endpoints:**
- `GET /users` - List users
- `POST /users` - Create user
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user
- `GET /actuator/health` - Health check

### Billing Service (port 8087)
Translates `COBIL00C` (bill payment) and `CORPT00C` (report submission). Bill payment uses the saga pattern across Transaction and Account services.

**Endpoints:**
- `POST /billing/pay/{accountId}` - Pay bill (saga)
- `POST /billing/reports/submit` - Submit report
- `GET /actuator/health` - Health check

### API Gateway (port 8080)
Spring Cloud Gateway translating the menu navigation from `COMEN01C` (regular) and `COADM01C` (admin). Routes requests based on JWT role claims.

**Routes:**
- `/api/auth/**` -> Auth Service
- `/api/accounts/**` -> Account Service
- `/api/cards/**` -> Card Service
- `/api/transactions/**` -> Transaction Service
- `/api/billing/**` -> Billing Service
- `/api/statements/**` -> Statement Service
- `/api/users/**` -> User Admin Service (admin only)

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker and Docker Compose

### Build All Services

```bash
# Build shared library first (dependency for other services)
cd shared-lib && mvn clean install -DskipTests && cd ..

# Build each service
for svc in auth-service account-service card-service transaction-service statement-service user-admin-service billing-service api-gateway; do
  echo "Building $svc..."
  (cd $svc && mvn clean package -DskipTests)
done
```

### Run with Docker Compose

```bash
# From the microservices/ directory
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f api-gateway
```

### Run Individual Service (Development)

```bash
cd auth-service
mvn spring-boot:run
# Service starts on http://localhost:8080 with H2 in-memory database
```

## Integration Test Flow

The key cross-boundary flow exercises the core user journey:

1. **Login** (`POST /api/auth/login`) - Get JWT token
2. **View Account** (`GET /api/accounts/{id}`) - Retrieve account details
3. **List Cards** (`GET /api/cards`) - View cards for account
4. **Pay Bill** (`POST /api/billing/pay/{accountId}`) - Execute bill payment saga

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| API Gateway | Spring Cloud Gateway |
| Database | PostgreSQL 16 (H2 for dev/test) |
| Migrations | Flyway |
| Message Queue | RabbitMQ |
| Authentication | JWT (JSON Web Tokens) |
| Build Tool | Maven |
| Containerization | Docker |
| API Spec | OpenAPI 3.x |

## Database Schema

Each service owns its database (database-per-service pattern):

| Service | Database | Source Copybook |
|---|---|---|
| Auth Service | `carddemo_auth` | `CSUSR01Y.cpy` |
| Account Service | `carddemo_accounts` | `CVACT01Y.cpy` |
| Card Service | `carddemo_cards` | `CVACT02Y.cpy`, `CVACT03Y.cpy` |
| Transaction Service | `carddemo_transactions` | `CVTRA05Y.cpy`, `CVTRA06Y.cpy` |
| User Admin Service | `carddemo_users` | `CSUSR01Y.cpy` |

## Event-Driven Architecture

Services communicate asynchronously via RabbitMQ for cross-boundary updates:

- **Transaction Service** publishes `transaction.created` events when transactions are posted
- **Account Service** consumes these events to update account balances (translating `CBTRN02C.cbl` lines 547-554: `ADD DALYTRAN-AMT TO ACCT-CURR-BAL`)
- **Billing Service** uses the saga pattern for bill payments, coordinating between Transaction and Account services with compensating transactions on failure
