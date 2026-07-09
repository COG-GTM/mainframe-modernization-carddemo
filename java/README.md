# CardDemo — Java migration

Java migration of the legacy CardDemo mainframe application (COBOL/CICS/JCL/VSAM/BMS).
This directory (`java/`) holds the new Spring Boot + Spring Batch codebase. The original
COBOL/CICS/JCL/VSAM/BMS sources under [`../app/`](../app/) are **read-only** and must not be
modified — they are the source of truth that each migration wave translates into Java.

This is the output of **WAVE 0 (scaffolding)**: a compiling, bootable multi-module project
with the conventions baked in. Later waves add entities, repositories, services, REST
controllers, batch jobs, security and session handling.

## Tech stack

- Java 17
- Spring Boot 3.3.x
- Spring Batch 5.x (via `spring-boot-starter-batch`)
- Maven multi-module build (parent: `spring-boot-starter-parent`)
- H2 (in-memory) for `dev`/`test`; PostgreSQL for `prod`
- Flyway for schema migrations
- Base package: `com.carddemo`

## Module layout

```
java/
├── pom.xml                 # parent POM (packaging=pom); manages versions via
│                           #   spring-boot-starter-parent + dependencyManagement
├── carddemo-domain/        # JPA entities + Spring Data repositories
│   └── src/main/java/com/carddemo/
│       ├── domain/         # @Entity classes (added by later waves)
│       ├── repository/     # Spring Data JPA repositories (added by later waves)
│       └── config/         # JpaConfig — owns @EntityScan / @EnableJpaRepositories
│   └── src/main/resources/db/migration/   # Flyway migrations (V1__baseline.sql ...)
└── carddemo-app/           # application module; depends on carddemo-domain
    └── src/main/java/com/carddemo/
        ├── CardDemoApplication.java   # @SpringBootApplication entry point
        ├── service/        # business services
        ├── web/            # REST controllers + request/response DTOs
        ├── batch/          # Spring Batch jobs/steps/reader/processor/writer
        ├── security/       # authentication / authorization
        ├── config/         # application-level configuration
        └── session/        # CICS pseudo-conversational session/state handling
    └── src/main/resources/application.yml   # profiles: dev / test / prod
```

### Package ownership

| Package                     | Module           | Responsibility |
|-----------------------------|------------------|----------------|
| `com.carddemo.domain`       | carddemo-domain  | JPA entities |
| `com.carddemo.repository`   | carddemo-domain  | Spring Data JPA repositories |
| `com.carddemo.config` (JpaConfig) | carddemo-domain | Datasource/JPA config; Flyway migrations live in this module |
| `com.carddemo.service`      | carddemo-app     | Business logic |
| `com.carddemo.web`          | carddemo-app     | REST controllers + DTOs |
| `com.carddemo.batch`        | carddemo-app     | Spring Batch jobs |
| `com.carddemo.security`     | carddemo-app     | Security |
| `com.carddemo.config`       | carddemo-app     | App-level config |
| `com.carddemo.session`      | carddemo-app     | Session / conversational state |

## Build

```bash
cd java
mvn verify        # compiles both modules and runs the tests (smoke @SpringBootTest)
```

Requires JDK 17. CI runs `mvn -B -f java/pom.xml verify` on Temurin 17
(see [`.github/workflows/java-ci.yml`](../.github/workflows/java-ci.yml)).

## Run

```bash
cd java
mvn -pl carddemo-app spring-boot:run
```

The app starts on `http://localhost:8080`. Verify it is up:

```bash
curl http://localhost:8080/api/health
# {"status":"UP"}
```

`GET /api/health` is public; all other endpoints require authentication (baseline HTTP Basic,
extended by later waves).

## Profiles

Configured in `carddemo-app/src/main/resources/application.yml`. The default active profile is
`dev`. Select a profile with `SPRING_PROFILES_ACTIVE=<profile>` (or `-Dspring-boot.run.profiles`).

| Profile | Datasource | Notes |
|---------|-----------|-------|
| `dev`   | H2 in-memory (`MODE=PostgreSQL`) | default; Flyway migrates on startup |
| `test`  | H2 in-memory (`MODE=PostgreSQL`) | used by the test suite |
| `prod`  | PostgreSQL | configured via `CARDDEMO_DB_URL` / `CARDDEMO_DB_USERNAME` / `CARDDEMO_DB_PASSWORD` |

H2 runs in PostgreSQL compatibility mode so that Flyway migrations authored for `prod`
(PostgreSQL) also apply cleanly in `dev`/`test`.

## Database migrations (Flyway)

Migrations live in `carddemo-domain/src/main/resources/db/migration` and are named
`V<n>__<description>.sql`. WAVE 0 ships only `V1__baseline.sql` (a no-op baseline).
Later waves add versioned migrations for the tables backing each copybook/VSAM file.
Author SQL to be **PostgreSQL-compatible** (the `prod` target); it will also run on H2 in
PostgreSQL mode for dev/test.

## COBOL→Java mapping

These conventions are **binding for all migration waves**. The exhaustive, per-scope field
tables live under [`docs/mapping/`](docs/mapping/); this section is the canonical summary of
the general rules. Preserve the original copybook field lengths in comments on the Java fields.

| COBOL PIC / usage | Java type | Notes |
|-------------------|-----------|-------|
| `PIC 9(n)`, `n <= 9` (numeric, used in arithmetic) | `Integer` | |
| `PIC 9(n)`, `n > 9` (numeric, used in arithmetic) | `Long` | |
| Fixed-width numeric ID with leading zeros, **never used in arithmetic** (account number, card number, customer id) | `String` | Preserve leading zeros / exact width; do **not** convert to a numeric type |
| `PIC X(n)` | `String` (length `n`) | Preserve the field length in a comment |
| `PIC S9(n)V99`, any `V`nn implied decimal, or COMP-3 monetary | `java.math.BigDecimal` | Use the **exact scale** (e.g. `V99` → scale 2). **All** monetary/interest arithmetic uses `BigDecimal` — never `double`/`float` |
| `COMP` / binary | `Long` / `Integer` | Size per the field width |

Additional rules:

- **Never use `double` or `float`** for money or interest. Use `BigDecimal` with the copybook's
  implied scale, and set an explicit `RoundingMode` on every division/scaling operation.
- IDs such as account number, card number and customer id are fixed-width, zero-padded, and
  purely identifiers — map them to `String` to preserve formatting and avoid accidental
  arithmetic.
- Always record the source copybook field length (and PIC clause where helpful) in a comment
  next to the corresponding Java field, e.g. `// ACCT-ID PIC 9(11) — 11-char zero-padded id`.

### Per-scope mapping docs

See [`docs/mapping/`](docs/mapping/). Each later wave adds a document there for the scope it
migrates. WAVE 0 leaves it with an index placeholder only.
