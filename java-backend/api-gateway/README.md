# CardDemo API Gateway

## Overview

The API Gateway is the entry point for all CardDemo API requests. It implements the **Strangler Fig Pattern** for gradual mainframe migration, routing requests between legacy mainframe systems and new Java microservices.

## Strangler Fig Pattern

The Strangler Fig Pattern allows incremental migration from mainframe to microservices:

```
                    +------------------+
                    |   API Gateway    |
                    |   (Port 8080)    |
                    +--------+---------+
                             |
            +----------------+----------------+
            |                |                |
            v                v                v
    +-------+------+  +------+-------+  +-----+------+
    | Auth Service |  | User Service |  |  Legacy    |
    | (Port 8081)  |  | (Port 8082)  |  | Mainframe  |
    +--------------+  +--------------+  +------------+
         Phase 1          Phase 1        Future Phases
```

## Phase 1 Routes

| Route | Service | Replaces COBOL |
|-------|---------|----------------|
| /api/auth/** | Authentication Service (8081) | COSGN00C (CC00) |
| /api/users/** | User Management Service (8082) | COUSR00C/01C/02C/03C |

## Future Phase Routes (Planned)

| Phase | Route | Service | Replaces COBOL |
|-------|-------|---------|----------------|
| 2 | /api/customers/** | Customer Service | CBCUS01C |
| 2 | /api/accounts/** | Account Service | COACTUPC, COACTVWC |
| 3 | /api/cards/** | Card Service | COCRDLIC, COCRDUPC |
| 4 | /api/transactions/** | Transaction Service | COTRN00C, COTRN01C |
| 5 | /api/batch/** | Batch Service | CBTRN01C, CBSTM03A |

## Features

### Request Routing
Routes requests to appropriate backend services based on URL path.

### CORS Support
Enables cross-origin requests for web clients.

### Request/Response Logging
Logs all requests for debugging and audit purposes.

### Health Checks
Exposes health endpoints for monitoring.

## Configuration

Key configuration in `application.yml`:

- `server.port`: Gateway port (default: 8080)
- `spring.cloud.gateway.routes`: Route definitions
- `spring.cloud.gateway.globalcors`: CORS settings

## Running the Gateway

### Local Development
```bash
mvn spring-boot:run
```

### Docker
```bash
docker-compose up api-gateway
```

## API Documentation

Swagger UI aggregates documentation from all services:
- Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html

## Monitoring

Actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Gateway Routes: http://localhost:8080/actuator/gateway/routes
- Metrics: http://localhost:8080/actuator/metrics
