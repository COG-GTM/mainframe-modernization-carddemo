# CardDemo User Management Service

## Overview

The User Management Service is part of the Phase 1 Java backend modernization for the CardDemo mainframe application. This service replaces the COBOL programs that handle user CRUD operations on the USRSEC VSAM file.

## Original COBOL Program Mapping

| COBOL Program | Transaction | Java Endpoint | Description |
|---------------|-------------|---------------|-------------|
| COUSR00C.cbl | CU00 | GET /api/users | List all users |
| COUSR01C.cbl | CU01 | POST /api/users | Add new user |
| COUSR02C.cbl | CU02 | PUT /api/users/{id} | Update user |
| COUSR03C.cbl | CU03 | DELETE /api/users/{id} | Delete user |

## API Endpoints

### GET /api/users
Lists all users with pagination (default 10 per page, matching COBOL).

**Query Parameters:**
- `page` - Page number (0-based)
- `size` - Page size (default: 10)
- `search` - Search term for user ID, first name, or last name

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": "USER0001",
        "firstName": "John",
        "lastName": "Doe",
        "userType": "U",
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### GET /api/users/{userId}
Gets a single user by ID.

### POST /api/users
Creates a new user.

**Request:**
```json
{
  "userId": "USER0002",
  "firstName": "Jane",
  "lastName": "Smith",
  "password": "PASS1234",
  "userType": "U"
}
```

### PUT /api/users/{userId}
Updates an existing user.

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "password": "NEWPASS1",
  "userType": "A"
}
```

### DELETE /api/users/{userId}
Soft deletes a user (sets isActive to false).

## Data Model

The User entity maps to the CSUSR01Y.cpy copybook:

| COBOL Field | Java Field | Type | Max Length |
|-------------|------------|------|------------|
| SEC-USR-ID | userId | String | 8 |
| SEC-USR-FNAME | firstName | String | 20 |
| SEC-USR-LNAME | lastName | String | 20 |
| SEC-USR-PWD | password | String | 255 (hashed) |
| SEC-USR-TYPE | userType | String | 1 |

## User Types

| Type | Description |
|------|-------------|
| A | Admin user |
| U | Regular user |

## Configuration

Key configuration properties in `application.yml`:

- `server.port`: Service port (default: 8082)
- `spring.datasource.*`: Database connection settings

## Running the Service

### Local Development
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker
```bash
docker-compose up user-management-service
```

## Security Notes

- Passwords are hashed using BCrypt
- Soft delete preserves audit trail
- In production, write operations should require admin role
