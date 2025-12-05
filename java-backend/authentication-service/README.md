# CardDemo Authentication Service

## Overview

The Authentication Service is part of the Phase 1 Java backend modernization for the CardDemo mainframe application. This service replaces the COBOL program **COSGN00C** (transaction CC00) which handles user login/logout and validates credentials against the USRSEC VSAM file.

## Original COBOL Program Mapping

| COBOL Component | Java Component |
|-----------------|----------------|
| COSGN00C.cbl | AuthenticationService.java |
| Transaction CC00 | POST /api/auth/login |
| USRSEC VSAM file | PostgreSQL users table |
| CICS COMMAREA | JWT Token |
| SEC-USER-DATA copybook | User.java entity |

## API Endpoints

### POST /api/auth/login
Authenticates user credentials and returns a JWT token.

**Request:**
```json
{
  "userId": "USER0001",
  "password": "PASSWORD"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "USER0001",
    "firstName": "John",
    "lastName": "Doe",
    "userType": "U",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### POST /api/auth/logout
Logs out the current user.

### POST /api/auth/validate
Validates a JWT token and returns user information.

## User Types

| Type | Description | Original COBOL |
|------|-------------|----------------|
| A | Admin user | Routes to COADM01C |
| U | Regular user | Routes to COMEN01C |

## Configuration

Key configuration properties in `application.yml`:

- `server.port`: Service port (default: 8081)
- `jwt.secret`: Secret key for JWT signing
- `jwt.expiration`: Token expiration time in milliseconds

## Running the Service

### Local Development
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker
```bash
docker-compose up authentication-service
```

## Security Notes

- Passwords are hashed using BCrypt (original COBOL stored plain text)
- JWT tokens replace CICS session management
- Stateless authentication for scalability
