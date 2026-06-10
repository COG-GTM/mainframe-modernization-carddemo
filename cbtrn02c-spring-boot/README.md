# CBTRN02C — Spring Boot Port

Java Spring Boot (Spring Batch + Spring Data JPA) port of the COBOL batch program
[`app/cbl/CBTRN02C.cbl`](../app/cbl/CBTRN02C.cbl), which posts records from the daily
transaction file to the transaction, account, and transaction-category-balance master files.

## Stack

- Spring Boot 3.2.x, Java 17
- Spring Batch, Spring Data JPA
- H2 in-memory database (runtime + tests)
- Maven

## Build & Test

```bash
mvn test        # run the JUnit suite
mvn package     # build the jar
```

## Mapping from COBOL

| COBOL artifact | Java artifact |
|---|---|
| `CVTRA06Y` (DALYTRAN-RECORD) | `entity/DailyTransaction` |
| `CVACT03Y` (CARD-XREF-RECORD) | `entity/CardXref` |
| `CVACT01Y` (ACCOUNT-RECORD) | `entity/Account` |
| `CVTRA05Y` (TRAN-RECORD) | `entity/Transaction` |
| `CVTRA01Y` (TRAN-CAT-BAL-RECORD) | `entity/TranCatBalance` (+ `TranCatBalanceId`) |
| inline REJECT-RECORD | `entity/TransactionReject` |
| `1500-VALIDATE-TRAN` + `2000-POST-TRANSACTION` | `service/TransactionPostingService` |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `service/Db2TimestampFormatter` |
| main PERFORM UNTIL loop / RETURN-CODE | `service/BatchResult` + `batch/BatchConfig` |

## Business rules preserved

- **Validation order**: XREF lookup (reject 100) → account lookup (reject 101) →
  credit-limit check (reject 102) → expiration check (reject 103).
- **Credit-limit formula**: `currCycCredit - currCycDebit + amount` compared to `creditLimit`
  (not `currBal + amount`).
- **Last-writer-wins**: the credit-limit (102) and expiration (103) checks run independently
  and sequentially; expiration can overwrite an overlimit rejection — exactly as in the COBOL.
- **Posting**: TCATBAL upsert, account balance update (`currCycCredit` for amount ≥ 0, else
  `currCycDebit`), and transaction write are wrapped in a single `@Transactional` method
  (an improvement over the COBOL, which has no two-phase commit).
- **Exit status**: `0` when all transactions post, `4` when any are rejected (COBOL RETURN-CODE).
- **Timestamp**: `procTimestamp` formatted as `YYYY-MM-DD-HH.MM.SS.mm0000` (26 chars).
