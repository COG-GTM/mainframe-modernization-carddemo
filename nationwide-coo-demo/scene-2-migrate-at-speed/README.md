# Scene 2 — Migrate at Speed

**The COO question:** *"Migrating one batch program from the mainframe takes my best people weeks. We have hundreds of them. How do we get faster?"*

This scene takes one of the harder programs in the scene-1 backlog — `CBTRN02C`, daily card transaction posting — and shows the Devin-generated Spring Boot replacement next to the original COBOL.

## What's in this folder

| Folder | What's there |
|---|---|
| [`before/`](./before/) | The original `CBTRN02C.cbl` plus the five copybooks it depends on. |
| [`after/`](./after/) | A runnable Spring Boot 3.2 / Java 17 service that replaces it. |

## What Devin produced

| Artefact | File |
|---|---|
| JPA entity for each VSAM record layout | [`after/src/main/java/uk/co/nationwide/cards/posting/domain/`](./after/src/main/java/uk/co/nationwide/cards/posting/domain/) |
| Spring Data repositories | [`after/src/main/java/uk/co/nationwide/cards/posting/repository/`](./after/src/main/java/uk/co/nationwide/cards/posting/repository/) |
| Business logic (posting service) | [`after/src/main/java/uk/co/nationwide/cards/posting/service/TransactionPostingService.java`](./after/src/main/java/uk/co/nationwide/cards/posting/service/TransactionPostingService.java) |
| REST API | [`after/src/main/java/uk/co/nationwide/cards/posting/api/TransactionController.java`](./after/src/main/java/uk/co/nationwide/cards/posting/api/TransactionController.java) |
| Flyway DB migration (VSAM → PostgreSQL) | [`after/src/main/resources/db/migration/V1__init_card_platform.sql`](./after/src/main/resources/db/migration/V1__init_card_platform.sql) |
| Demo seed data | [`after/src/main/resources/db/migration/V2__seed_demo_data.sql`](./after/src/main/resources/db/migration/V2__seed_demo_data.sql) |
| Equivalence tests (one per COBOL rejection path) | [`after/src/test/java/uk/co/nationwide/cards/posting/service/TransactionPostingServiceTest.java`](./after/src/test/java/uk/co/nationwide/cards/posting/service/TransactionPostingServiceTest.java) |

## Side-by-side: COBOL ↔ Java

The whole point of this scene is the diff. Here is the validation paragraph:

### COBOL — `1500-VALIDATE-TRAN` (excerpt from CBTRN02C)

```cobol
1500-VALIDATE-TRAN.
    MOVE 0     TO WS-VALIDATION-FAIL-REASON
    MOVE SPACES TO WS-VALIDATION-FAIL-REASON-DESC

    *> Look up card in xref
    MOVE DALYTRAN-CARD-NUM TO XREF-CARD-NUM
    PERFORM 1500-A-LOOKUP-XREF
    IF XREF-READ-STATUS = 'F'
        MOVE 100 TO WS-VALIDATION-FAIL-REASON
        MOVE 'Invalid card number'
            TO WS-VALIDATION-FAIL-REASON-DESC
        GO TO 1500-EXIT
    END-IF.

    *> Look up account
    MOVE XREF-ACCT-ID TO ACCT-ID
    PERFORM 1500-B-LOOKUP-ACCT
    IF ACCT-ACTIVE-STATUS NOT = 'Y'
        MOVE 102 TO WS-VALIDATION-FAIL-REASON
        GO TO 1500-EXIT
    END-IF.

    *> Look up card record
    PERFORM 1500-C-LOOKUP-CARD
    IF CARD-ACTIVE-STATUS NOT = 'Y'
        MOVE 103 TO WS-VALIDATION-FAIL-REASON
        GO TO 1500-EXIT
    END-IF.

    IF CARD-EXPIRAION-DATE <= TODAYS-DATE
        MOVE 104 TO WS-VALIDATION-FAIL-REASON
        GO TO 1500-EXIT
    END-IF.

    *> Credit limit check
    COMPUTE WS-NEW-BAL = ACCT-CURR-BAL + DALYTRAN-AMT
    IF WS-NEW-BAL > ACCT-CREDIT-LIMIT
        MOVE 105 TO WS-VALIDATION-FAIL-REASON
        GO TO 1500-EXIT
    END-IF.
1500-EXIT.
    EXIT.
```

### Java — equivalent block in `TransactionPostingService.postTransaction`

```java
CardXref xref = xrefRepo.findById(request.getTranCardNum())
        .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.CARD_NOT_FOUND_IN_XREF));

Account account = accountRepo.findById(xref.getXrefAcctId())
        .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.ACCOUNT_NOT_FOUND));

if (!"Y".equals(account.getAcctActiveStatus())) {
    throw new TransactionRejectedException(ValidationFailure.ACCOUNT_INACTIVE);
}

Card card = cardRepo.findById(request.getTranCardNum())
        .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.CARD_NOT_FOUND_IN_XREF));

if (!"Y".equals(card.getCardActiveStatus())) {
    throw new TransactionRejectedException(ValidationFailure.CARD_INACTIVE);
}

LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
if (card.getCardExpirationDate() != null && !card.getCardExpirationDate().isAfter(today)) {
    throw new TransactionRejectedException(ValidationFailure.CARD_EXPIRED);
}

BigDecimal projectedBalance = account.getAcctCurrBal().add(request.getTranAmt());
if (projectedBalance.compareTo(account.getAcctCreditLimit()) > 0) {
    throw new TransactionRejectedException(ValidationFailure.EXCEEDS_CREDIT_LIMIT);
}
```

Six failure codes, six rejection paths, **same business semantics**. The unit-test suite covers all six.

### Posting paragraph mapping (cheat sheet)

| COBOL paragraph | Java method |
|---|---|
| `1500-VALIDATE-TRAN` | `postTransaction(...)` validation block |
| `2000-POST-TRANSACTION` | `postTransaction(...)` body |
| `2700-UPDATE-TCATBAL` | `updateCategoryBalance(...)` |
| `2800-UPDATE-ACCOUNT-REC` | `accountRepo.save(account)` |
| `2900-WRITE-TRANSACTION-FILE` | `transactionRepo.save(txn)` |
| `2500-WRITE-REJECT-REC` | `throw new TransactionRejectedException(...)` |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `Clock` bean |

## What Devin preserved (and what it deliberately changed)

**Preserved verbatim from the COBOL:**

- The six failure codes (100–105) — same numeric codes, same semantics, exposed via the legacy code in the API response so downstream systems that key off `WS-VALIDATION-FAIL-REASON` don't break.
- Field names match the COBOL copybooks (`acctId`, `tranCardNum`, `tranAmt`) so the migration is auditable — the auditor can match every Java field to a COBOL field with no rename.
- Decimal fixed-point: every monetary field is `BigDecimal`, never `double`/`float`. The COBOL `S9(10)V99` and `S9(09)V99` precision is preserved at the database column level (`NUMERIC(12,2)`, `NUMERIC(11,2)`).
- Transaction boundary: one COBOL `EXEC CICS SYNCPOINT` becomes one Spring `@Transactional`.

**Deliberately modernized:**

- VSAM KSDS → PostgreSQL with B-tree indexes. The 11-digit `ACCT-ID` becomes a `BIGINT`. The 16-char card number stays a `VARCHAR(16)` because it's identifier-shaped, not number-shaped (leading zeroes matter).
- Sign-zoned numerics → `BigDecimal` (no risk of `double` rounding in money).
- File-driven batch loop → REST endpoint *plus* an upstream Spring Batch driver (not built in this demo — outside scene scope). One endpoint, two callers.
- The `Z-GET-DB2-FORMAT-TIMESTAMP` paragraph is gone; the `Clock` bean replaces it and makes the code deterministically testable.

## How to run it

### Tests only (no Postgres needed)

```bash
cd after
./mvnw test
```

Runs the seven equivalence tests in `TransactionPostingServiceTest`. Should pass in under 10 seconds.

### Locally with Postgres

```bash
# From the repo root
cd nationwide-coo-demo
docker compose up -d postgres
docker compose up --build transaction-posting
```

Then:

```bash
# Successful post
curl -X POST http://localhost:8082/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d @scene-2-migrate-at-speed/after/src/test/resources/sample-transaction.json

# Triggering a rejection (closed account)
curl -X POST http://localhost:8082/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{"tranId":"TXN0000000000099","tranTypeCd":"01","tranCatCd":5411,
       "tranAmt":50.00,"tranCardNum":"4929123456789020",
       "tranOrigTs":"2025-06-15T09:45:00Z"}'
# -> HTTP 422 with body {"code":102,"reason":"Account inactive"}
```

## "Where's the evidence this scales?"

This pattern — COBOL business logic ↔ Spring Boot service ↔ JPA entities ↔ Flyway migrations — is the same pattern Devin runs against production-scale estates today. The Centene migration (referenced in `COG-GTM/centene-cobol-demo` and `COG-GTM/centene-migrated-app-react`) is the production-grade case study; this scene is the same architecture at a size you can walk through in 5 minutes.

## Things this demo does *not* claim

- Devin does **not** migrate the entire 731-line COBOL program automatically. There are six business-rule decision points where a human engineer needs to make a call (e.g., does *card expired on the date* mean expired *on the date* or *after the date*? COBOL says `<=`; Java preserves that).
- Devin does **not** generate a like-for-like batch driver. The demo's REST API is a richer surface than the COBOL's file-based batch loop. In a real migration you'd add a Spring Batch driver wrapping the same service method.
- Tests in this demo are equivalence tests, not exhaustive — production migration would also use an automated comparison harness that runs both legacy and modern with the same input and asserts on the output.
