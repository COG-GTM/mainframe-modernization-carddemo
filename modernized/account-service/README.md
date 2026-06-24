# CardDemo Account Service — Modern Java Spring Boot Equivalent

A modern, runnable Spring Boot service that is the functional equivalent of the
CardDemo COBOL batch program `CBACT01C` (JCL job `READACCT`) — "Read and print
account data file" — and exposes the `ACCTDAT` data as a REST resource.

Generated following the `generate-modern-equivalent` skill (`.devin/skills/`),
targeting **Java + Spring Boot** per the entity-migration plan.

## What CBACT01C does

`CBACT01C` is a batch program that:

1. Opens the `ACCTDAT` VSAM KSDS (`0000-ACCTFILE-OPEN`).
2. Reads it **sequentially** by key (`1000-ACCTFILE-GET-NEXT` → `READ ... INTO
   ACCOUNT-RECORD`).
3. `DISPLAY`s every field of each account record (`1100-DISPLAY-ACCT-RECORD`).
4. Closes the file (`9000-ACCTFILE-CLOSE`).
5. On any non-zero file status, abends via `9999-ABEND-PROGRAM` (`CALL 'CEE3ABD'`).

It is a pure read-and-print job — no calculations, no updates.

## Architecture

| Layer | File | COBOL origin |
|-------|------|--------------|
| JPA entity | `model/Account.java` | copybook `CVACT01Y` (`ACCTDAT`, 300 bytes) |
| Fixed-width parser | `parser/AccountRecordParser.java` | `FD-ACCTFILE-REC` byte layout |
| Zoned-decimal decoder | `parser/ZonedDecimal.java` | `PIC S9(10)V99` sign overpunch |
| Repository | `repository/AccountRepository.java` | file control over the `ACCTDAT` KSDS |
| Batch service | `batch/AccountReportService.java` | `PROCEDURE DIVISION` + paragraphs `0000`/`1000`/`1100`/`9000`/`9999` |
| Batch runner | `batch/AccountReportRunner.java` | the `READACCT` JCL job step |
| Seed loader | `config/AccountDataLoader.java` | `ACCTFILE` initial-load JCL |
| REST controller | `web/AccountController.java` | `ACCTDAT` as a queryable resource |
| Error handling | `exception/AccountFileException.java`, `web/GlobalExceptionHandler.java` | `9999-ABEND-PROGRAM` / file-status errors |

Each method in `AccountReportService` maps one-to-one to a COBOL paragraph, so the
translation stays traceable back to `app/cbl/CBACT01C.cbl`.

## Run it

Requires JDK 17+ and Maven.

```bash
cd modernized/account-service

mvn test                                              # run the unit + integration tests
mvn spring-boot:run                                   # start the REST API on http://localhost:8080
mvn spring-boot:run -Dspring-boot.run.arguments=report  # run the CBACT01C batch report (prints all accounts)
```

The same artifact serves as both the batch job (`report` argument) and the REST
service. On startup the `account` table is seeded from `data/acctdata.txt`
(a copy of `app/data/ASCII/acctdata.txt`, 50 records), simulating the mainframe
`ACCTFILE` load job.

## API

```bash
# Sequential read of the whole file (ordered by key) — the REST face of CBACT01C
curl http://localhost:8080/api/accounts

# Keyed VSAM READ
curl http://localhost:8080/api/accounts/00000000001
```

See [`MODERNIZATION_NOTES.md`](./MODERNIZATION_NOTES.md) for translation decisions
and recommended next steps.
