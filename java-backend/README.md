# CardDemo Java Backend - Phase 1

This is the Phase 1 implementation of the CardDemo modernization project, focusing on Authentication and User Management services using the Strangler Fig Pattern.

## Architecture Overview

The backend is structured as a multi-module Gradle project with the following modules:

- **common**: Shared DTOs, exceptions, security utilities, and JWT token handling
- **api-gateway**: Spring Cloud Gateway for routing requests between new and legacy systems
- **auth-service**: Authentication service with JWT token generation and validation
- **user-service**: User management service (CRUD operations for users)

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Cloud Gateway
- Spring Security with JWT
- Spring Data JPA
- H2 Database (development) / PostgreSQL (production)
- Gradle 8.5

## Module Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |

## Environment Variables

The following environment variables must be configured:

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | Secret key for JWT signing (min 32 characters) | Yes |
| `JWT_EXPIRATION_MS` | Token expiration in milliseconds | No (default: 3600000) |
| `DATABASE_URL` | JDBC database URL | No (default: H2 in-memory) |
| `DATABASE_USERNAME` | Database username | No (default: sa) |
| `DATABASE_PASSWORD` | Database password | No (default: empty) |
| `LEGACY_MAINFRAME_URL` | URL of the legacy mainframe system | No (default: http://localhost:9080) |

## Building the Project

```bash
./gradlew build
```

## Running the Services

### Development Mode (H2 Database)

```bash
# Set required environment variables
export JWT_SECRET="your-secret-key-at-least-32-characters-long"

# Run individual services
./gradlew :api-gateway:bootRun
./gradlew :auth-service:bootRun
./gradlew :user-service:bootRun
```

### Production Mode (PostgreSQL)

```bash
export JWT_SECRET="your-production-secret-key"
export DATABASE_URL="jdbc:postgresql://localhost:5432/carddemo"
export DATABASE_USERNAME="carddemo"
export DATABASE_PASSWORD="your-password"
export DATABASE_DRIVER="org.postgresql.Driver"

./gradlew bootRun
```

## API Endpoints

### Authentication Service (Port 8081)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | Authenticate user and get JWT token | No |
| POST | `/api/v1/auth/validate` | Validate JWT token | Yes |
| GET | `/api/v1/auth/me` | Get current user info | Yes |

### User Management Service (Port 8082)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users` | List all users (paginated) | Admin |
| GET | `/api/v1/users/{userId}` | Get user by ID | Admin or Self |
| POST | `/api/v1/users` | Create new user | Admin |
| PUT | `/api/v1/users/{userId}` | Update user | Admin |
| DELETE | `/api/v1/users/{userId}` | Delete user | Admin |
| GET | `/api/v1/users/search?q={term}` | Search users | Admin |

## User Types

Based on the original COBOL application:
- `A` - Administrator
- `U` - Regular User

## Data Model

The User entity mirrors the original COBOL USRSEC file structure:

| Field | Type | Max Length | Description |
|-------|------|------------|-------------|
| userId | String | 8 | Primary key |
| firstName | String | 20 | User's first name |
| lastName | String | 20 | User's last name |
| password | String | 8 | User's password |
| userType | String | 1 | 'A' for Admin, 'U' for User |

## Phase 1 Scope

This phase implements:
- User authentication (login/logout)
- JWT token generation and validation
- User CRUD operations (list, create, update, delete)
- API Gateway for routing between new and legacy systems

## Future Phases

- Phase 2: Customer Management Service
- Phase 3: Account Management Service
- Phase 4: Card Management Service
- Phase 5: Transaction Processing Service
- Phase 6: Batch Processing Services
