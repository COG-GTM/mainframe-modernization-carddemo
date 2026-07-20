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
in-memory H2 database seeded at startup with the sample `USRSEC` users.

Once running, open <http://localhost:8080/> for the signon UI. Sample
credentials (from `app/jcl/DUSRSECJ.jcl`):

| User ID    | Password   | Routes to        |
| :--------- | :--------- | :--------------- |
| `ADMIN001` | `PASSWORD` | Admin menu (`COADM01C`) |
| `USER0001` | `PASSWORD` | Main menu (`COMEN01C`)  |

## Test layers

| Layer | Where | What it proves |
| :---- | :---- | :------------- |
| Unit | `domain/*Test`, `service/SignonServiceTest` | validation branches & copybook mapping in isolation (mocked repo) |
| Integration | `web/SignonEndpointIntegrationTest`, `domain/UserSecurityRepositoryIntegrationTest` | endpoint + real H2 datastore, `CSUSR01Y` field mapping |
| E2E parity | `e2e/SignonParityE2ETest` | full app over HTTP vs mainframe `COSGN00C` behavior |

## Seed / fixture data

`src/main/resources/usrsec-seed.txt` holds the 10 sample users as fixed-width
80-byte records (copybook `CSUSR01Y`), mirroring the in-stream data in the
mainframe `DUSRSECJ` job. `UsrsecSeeder` loads them at startup via
`UsrsecRecordMapper`.
