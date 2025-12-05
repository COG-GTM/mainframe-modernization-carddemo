# CardDemo Java Backend - Phase 1

This directory contains the Phase 1 Java backend implementation for the CardDemo mainframe modernization project. It implements the **Strangler Fig Pattern** to gradually migrate functionality from the legacy COBOL/CICS mainframe application to modern Java microservices.

## Overview

Phase 1 focuses on modernizing the **Authentication** and **User Management** services, which are the least dependent services in the CardDemo application. These services replace the following COBOL programs:

| Java Service | COBOL Program | Transaction | Function |
|-------------|---------------|-------------|----------|
| Authentication Service | COSGN00C | CC00 | User login/logout, credential validation |
| User Management Service | COUSR00C | CU00 | List all users |
| User Management Service | COUSR01C | CU01 | Add new user |
| User Management Service | COUSR02C | CU02 | Update user |
| User Management Service | COUSR03C | CU03 | Delete user |

## Architecture

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (Port 8080)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
    ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
    │ Authentication  │  │ User Management │  │    Mainframe    │
    │    Service      │  │    Service      │  │    (Legacy)     │
    │  (Port 8081)    │  │  (Port 8082)    │  │                 │
    └─────────────────┘  └─────────────────┘  └─────────────────┘
              │                   │
              ▼                   ▼
    ┌─────────────────────────────────────────┐
    │              Database (H2/PostgreSQL)    │
    │         (Replaces USRSEC VSAM file)      │
    └─────────────────────────────────────────┘
```

## Project Structure

```
java-backend/
├── pom.xml                          # Parent POM with common dependencies
├── README.md                        # This file
├── authentication-service/          # JWT authentication (replaces COSGN00C)
│   ├── pom.xml
│   └── src/main/java/com/carddemo/auth/
│       ├── AuthenticationServiceApplication.java
│       ├── config/                  # Security, OpenAPI configuration
│       ├── controller/              # REST endpoints
│       ├── dto/                     # Request/Response DTOs
│       ├── model/                   # JPA entities
│       ├── repository/              # Data access layer
│       ├── security/                # JWT token handling
│       └── service/                 # Business logic
├── user-management-service/         # User CRUD (replaces COUSR00C-03C)
│   ├── pom.xml
│   └── src/main/java/com/carddemo/user/
│       ├── UserManagementServiceApplication.java
│       ├── config/                  # Security, OpenAPI configuration
│       ├── controller/              # REST endpoints
│       ├── dto/                     # Request/Response DTOs
│       ├── model/                   # JPA entities
│       ├── repository/              # Data access layer
│       ├── security/                # JWT validation
│       └── service/                 # Business logic
└── api-gateway/                     # Spring Cloud Gateway
    ├── pom.xml
    └── src/main/java/com/carddemo/gateway/
        ├── ApiGatewayApplication.java
        ├── config/                  # Route configuration
        ├── controller/              # Fallback endpoints
        └── filter/                  # Global filters
```

## Technology Stack

- **Java 17** - Runtime
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **Spring Cloud Gateway** - API routing
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **H2 Database** - Development database (in-memory)
- **PostgreSQL** - Production database
- **Maven** - Build and dependency management
- **SpringDoc OpenAPI** - API documentation

## Building the Project

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher

### Build Commands

```bash
# Build all modules
cd java-backend
mvn clean install

# Build specific module
mvn clean install -pl authentication-service

# Skip tests
mvn clean install -DskipTests
```

## Running the Services

### Development Mode (H2 Database)

Start each service in a separate terminal:

```bash
# Terminal 1: Start Authentication Service
cd java-backend/authentication-service
mvn spring-boot:run

# Terminal 2: Start User Management Service
cd java-backend/user-management-service
mvn spring-boot:run

# Terminal 3: Start API Gateway
cd java-backend/api-gateway
mvn spring-boot:run
```

### Service Ports

| Service | Port | Swagger UI |
|---------|------|------------|
| API Gateway | 8080 | http://localhost:8080/swagger-ui.html |
| Authentication Service | 8081 | http://localhost:8081/swagger-ui.html |
| User Management Service | 8082 | http://localhost:8082/swagger-ui.html |

### Default Users

The following users are pre-loaded (matching mainframe USRSEC data):

| User ID | Password | Type | Description |
|---------|----------|------|-------------|
| USER0001 | PASSWORD | USER | Regular user |
| ADMIN001 | PASSWORD | ADMIN | Administrator |

## API Endpoints

### Authentication Service (Port 8081)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/logout` | User logout | Yes |
| GET | `/api/auth/validate` | Validate token | No |

### User Management Service (Port 8082)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | List all users | Yes (ADMIN) |
| GET | `/api/users/{id}` | Get user by ID | Yes (ADMIN) |
| POST | `/api/users` | Create new user | Yes (ADMIN) |
| PUT | `/api/users/{id}` | Update user | Yes (ADMIN) |
| DELETE | `/api/users/{id}` | Delete user | Yes (ADMIN) |

### API Gateway (Port 8080)

All requests through the gateway are routed to the appropriate service:
- `/api/auth/**` → Authentication Service
- `/api/users/**` → User Management Service

## Example Usage

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId": "ADMIN001", "password": "PASSWORD"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": "ADMIN001",
  "userType": "ADMIN",
  "firstName": "Admin",
  "lastName": "User"
}
```

### List Users (Admin Only)

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"
```

### Create User (Admin Only)

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USER0002",
    "firstName": "Jane",
    "lastName": "Smith",
    "password": "PASSWORD",
    "userType": "USER"
  }'
```

## Migration Strategy

This implementation follows the **Strangler Fig Pattern**:

1. **Phase 1 (Current)**: Authentication and User Management
   - New Java services handle all auth and user operations
   - API Gateway routes these requests to new services
   - Other requests can be forwarded to mainframe (when configured)

2. **Future Phases**:
   - Phase 2: Customer Service
   - Phase 3: Account Service
   - Phase 4: Card Service
   - Phase 5: Transaction Service
   - Phase 6: Batch Processing

As each phase completes, more routes are added to the API Gateway to redirect traffic from the mainframe to the new Java services.

## Data Model Mapping

The User entity maps directly to the USRSEC VSAM file structure:

| COBOL Field | Java Field | Type | Description |
|-------------|------------|------|-------------|
| SEC-USR-ID | userId | String(8) | Primary key |
| SEC-USR-FNAME | firstName | String(20) | First name |
| SEC-USR-LNAME | lastName | String(20) | Last name |
| SEC-USR-PWD | password | String | BCrypt hashed |
| SEC-USR-TYPE | userType | Enum | ADMIN or USER |

## Configuration

### Environment Variables (Production)

```bash
# Database
DB_USERNAME=carddemo
DB_PASSWORD=<secure-password>

# JWT
JWT_SECRET=<base64-encoded-256-bit-key>
JWT_EXPIRATION_MS=86400000

# Service URLs (for gateway)
AUTH_SERVICE_URL=http://authentication-service:8081
USER_SERVICE_URL=http://user-management-service:8082
```

### Running with Production Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

## Health Checks

Each service exposes health endpoints:

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Authentication service health
curl http://localhost:8081/actuator/health

# User management service health
curl http://localhost:8082/actuator/health
```

## Security Considerations

1. **JWT Tokens**: All services share the same JWT secret for token validation
2. **Password Hashing**: BCrypt is used instead of mainframe's plain text storage
3. **Role-Based Access**: User management requires ADMIN role
4. **HTTPS**: Should be enabled in production via reverse proxy

## Troubleshooting

### Common Issues

1. **Port already in use**: Change port in `application.yml` or stop conflicting service
2. **JWT validation fails**: Ensure all services use the same `jwt.secret` value
3. **Database connection issues**: Check H2 console at `http://localhost:808x/h2-console`

### Logging

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.carddemo: DEBUG
    org.springframework.security: DEBUG
```

## Contributing

See the main repository's [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../LICENSE) file for details.
