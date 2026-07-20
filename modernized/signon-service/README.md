# CardDemo Signon Service

Cloud-deployable modernization of the CardDemo CICS COBOL program
**`COSGN00C`** (transaction **`CC00`**, the Signon screen). It preserves the
observable behavior of the mainframe program: the same validation rules, the
same success/failure outcomes, and equivalent routing to the **admin menu**
(`COADM01C`) vs the **main menu** (`COMEN01C`) flows.

This is the first migration produced with the CardDemo migration playbook
(`docs/modernization-playbook.md`). It is built as a sequence of small,
independently reviewable pull requests.

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Data JPA + H2 (replaces the `USRSEC` VSAM KSDS)
- JUnit 5 (unit, integration and end-to-end parity tests)
- Maven build; GitHub Actions CI (`.github/workflows/ci.yml`)

## Layout

```
modernized/signon-service/
  src/main/java/com/carddemo/signon/   application code
  src/test/java/com/carddemo/signon/   unit / integration / e2e tests
  src/main/resources/                  configuration and seed data
```

## Build & test

From `modernized/signon-service`:

```bash
mvn verify        # compile + run unit, integration and e2e tests
mvn spring-boot:run   # start the service on http://localhost:8080
```

Requires JDK 17. No external services are needed; the datastore is an
in-memory H2 database seeded with the sample `USRSEC` users.
