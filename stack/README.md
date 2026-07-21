# CardDemo — Bill Payment (CB00) modernized stack

Java Spring Boot backend + Angular frontend migrated 1:1 from the COBOL/CICS/VSAM
Bill Payment transaction (`app/cbl/COBIL00C.cbl`, map `app/bms/COBIL00.bms`, TransID `CB00`).

```
stack/
  backend/    Spring Boot + JPA + Flyway (H2 in DB2-mode for tests, real DB2 for runtime)
  frontend/   Angular standalone app (3270-style Bill Payment screen)
  docker-compose.yml   local DB2
```

## Backend

```bash
cd stack/backend

# Run tests (hermetic, H2 in DB2 compatibility mode)
mvn test

# Run against H2 with demo seed data (no DB2 needed)
mvn spring-boot:run -Dspring-boot.run.profiles=demo

# Run against real DB2 (start DB2 first, see below) + demo seed
mvn spring-boot:run -Dspring-boot.run.profiles=db2,demo
```

Endpoints (port 8080):
- `POST /api/billpay/inquiry` — `{ "acctId": "11" }` → balance + confirm prompt
- `POST /api/billpay/pay` — `{ "acctId": "11", "confirm": "Y" }` → payment

## Local DB2

```bash
docker compose -f stack/docker-compose.yml up -d   # ~2-3 min first-time setup
```

Connection defaults (overridable via `DB2_URL`/`DB2_USER`/`DB2_PASSWORD`):
`jdbc:db2://localhost:50000/CARDDEMO`, user `db2inst1`, password `carddemo123`.

## Frontend

```bash
cd stack/frontend
npm install
npm start                 # dev server on http://localhost:4200
npm run build             # production build
CHROME_BIN=<chrome> npx ng test --watch=false   # unit tests (headless)
```

The frontend calls the backend at `http://localhost:8080`. Start the backend
(with the `demo` profile for seed data) before using the UI.

## Demo seed data (`demo` profile)

| Account | Balance | Card (xref)        | Notes                         |
|---------|---------|--------------------|-------------------------------|
| 11      | 123.45  | 4111111111111111   | happy-path payment            |
| 12      | 0.00    | 4000000000000012   | triggers "nothing to pay"     |
| 13      | 5000.00 | 4222222222222222   | second payable account        |

One pre-existing transaction (`0000000000000100`) so new ids continue from the max.
