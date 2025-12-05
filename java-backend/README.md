# CardDemo Java Backend - Phase 1

## Overview

This directory contains the Phase 1 Java backend implementation for the CardDemo mainframe modernization project. The implementation follows the **Strangler Fig Pattern** to gradually migrate functionality from the legacy COBOL/CICS mainframe application to modern Java microservices.

## Phase 1 Scope: Authentication and User Management

Phase 1 focuses on migrating the least dependent services first:

| Original COBOL Program | Transaction | New Java Service | Description |
|------------------------|-------------|------------------|-------------|
| COSGN00C.cbl | CC00 | Authentication Service | User login/logout |
| COUSR00C.cbl | CU00 | User Management Service | List users |
| COUSR01C.cbl | CU01 | User Management Service | Add user |
| COUSR02C.cbl | CU02 | User Management Service | Update user |
| COUSR03C.cbl | CU03 | User Management Service | Delete user |

## Architecture

```
                         +------------------+
                         |   API Gateway    |
                         |   (Port 8080)    |
                         +--------+---------+
                                  |
                 +----------------+----------------+
                 |                                 |
                 v                                 v
    +------------+-------------+    +-------------+------------+
    | Authentication Service   |    | User Management Service  |
    | (Port 8081)              |    | (Port 8082)              |
    | - Login                  |    | - List Users             |
    | - Logout                 |    | - Create User            |
    | - Token Validation       |    | - Update User            |
    +------------+-------------+    | - Delete User            |
                 |                  +-------------+------------+
                 |                                |
                 +----------------+---------------+
                                  |
                                  v
                         +-------+--------+
                         |   PostgreSQL   |
                         | (Port 5432)    |
                         +----------------+
```

## Directory Structure

```
java-backend/
├── api-gateway/                 # Spring Cloud Gateway
│   ├── src/main/java/          # Gateway configuration
│   └── src/main/resources/     # Application config
├── authentication-service/      # Auth service (replaces COSGN00C)
│   ├── src/main/java/          # Controllers, services, models
│   └── src/main/resources/     # Application config
├── user-management-service/     # User service (replaces COUSR*)
│   ├── src/main/java/          # Controllers, services, models
│   └── src/main/resources/     # Application config
├── common/                      # Shared components
│   └── src/main/java/          # DTOs, exceptions, utilities
├── docker-compose.yml          # Container orchestration
├── init-db.sql                 # Database initialization
├── pom.xml                     # Parent Maven POM
└── README.md                   # This file
```

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.2.0 |
| Gateway | Spring Cloud Gateway | 2023.0.0 |
| Security | Spring Security + JWT | - |
| Database | PostgreSQL | 15 |
| Build Tool | Maven | 3.9+ |
| Java | OpenJDK | 17 |
| Containers | Docker | 20+ |

## Data Model Mapping

The User entity maps to the CSUSR01Y.cpy copybook:

| COBOL Field (CSUSR01Y.cpy) | Java Field | Type | Max Length |
|----------------------------|------------|------|------------|
| SEC-USR-ID | userId | String | 8 |
| SEC-USR-FNAME | firstName | String | 20 |
| SEC-USR-LNAME | lastName | String | 20 |
| SEC-USR-PWD | password | String | 255 (hashed) |
| SEC-USR-TYPE | userType | String | 1 |

## API Endpoints

### Authentication Service (Port 8081)

| Method | Endpoint | Description | COBOL Equivalent |
|--------|----------|-------------|------------------|
| POST | /api/auth/login | User login | COSGN00C PROCESS-ENTER-KEY |
| POST | /api/auth/logout | User logout | COSGN00C DFHPF3 handler |
| POST | /api/auth/validate | Validate token | COMMAREA validation |

### User Management Service (Port 8082)

| Method | Endpoint | Description | COBOL Equivalent |
|--------|----------|-------------|------------------|
| GET | /api/users | List users | COUSR00C |
| GET | /api/users/{id} | Get user | COUSR02C READ-USER-SEC-FILE |
| POST | /api/users | Create user | COUSR01C WRITE-USER-SEC-FILE |
| PUT | /api/users/{id} | Update user | COUSR02C UPDATE-USER-SEC-FILE |
| DELETE | /api/users/{id} | Delete user | COUSR03C DELETE-USER-SEC-FILE |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose

### Build

```bash
# Build all modules
cd java-backend
mvn clean install
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Run Locally (Development)

```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run Authentication Service
cd authentication-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run User Management Service (in another terminal)
cd user-management-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run API Gateway (in another terminal)
cd api-gateway
mvn spring-boot:run
```

### Test Credentials

| User ID | Password | Type | Description |
|---------|----------|------|-------------|
| ADMIN001 | PASSWORD | Admin | System administrator |
| USER0001 | PASSWORD | User | Regular user |

## API Documentation

Swagger UI is available at:
- Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html

## Strangler Fig Pattern Implementation

The API Gateway implements the Strangler Fig Pattern:

1. **Phase 1 (Current)**: Authentication and User Management routes to new Java services
2. **Future Phases**: Additional routes will be added as more services are migrated
3. **Legacy Support**: Unmigrated endpoints can be proxied to mainframe (when configured)

This allows:
- Incremental migration without disrupting existing clients
- Parallel operation of old and new systems
- Easy rollback if issues arise
- Gradual team learning and adaptation

## Future Phases

| Phase | Services | COBOL Programs |
|-------|----------|----------------|
| 2 | Customer, Account | CBCUS01C, COACTUPC, COACTVWC |
| 3 | Card Management | COCRDLIC, COCRDSLC, COCRDUPC |
| 4 | Transaction | COTRN00C, COTRN01C, COTRN02C |
| 5 | Batch Processing | CBTRN01C, CBSTM03A, CBACT04C |

## Security Considerations

- Passwords are hashed using BCrypt (original COBOL stored plain text)
- JWT tokens replace CICS session management
- Soft delete preserves audit trail
- CORS configured for API Gateway

## Contributing

See the main repository CONTRIBUTING.md for guidelines.

## License

Apache License 2.0 - See LICENSE file in the root directory.
