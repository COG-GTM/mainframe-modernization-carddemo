# CardDemo — modernized (Java 17 / Spring Boot 3 / PostgreSQL)

Hand-crafted rewrite of the COBOL/CICS/VSAM/JCL CardDemo application in `app/` into a Spring Boot
service: VSAM files become PostgreSQL tables, CICS/BMS online programs become REST endpoints, and
JCL jobs become Spring Batch jobs. Business logic (validation messages, posting rules, interest
formula, report layouts) is ported field-for-field from the COBOL sources.

## Running it

```bash
createdb carddemo                       # role/database used by application.yml
cd modernized
mvn spring-boot:run                     # Flyway creates the schema at startup
curl -XPOST localhost:8080/api/v1/batch/loadLegacyDataJob   # IDCAMS load equivalent
curl -XPOST localhost:8080/api/v1/signon \
  -H 'Content-Type: application/json' -d '{"userId":"USER0001","password":"PASSWORD"}'
```

Configuration (`src/main/resources/application.yml`, all overridable by environment variable):

| Property | Env var | Default | Legacy equivalent |
| --- | --- | --- | --- |
| `spring.datasource.url` | `CARDDEMO_DB_URL` | `jdbc:postgresql://localhost:5432/carddemo` | VSAM cluster names |
| `spring.datasource.username` / `password` | `CARDDEMO_DB_USER` / `CARDDEMO_DB_PASSWORD` | `carddemo` | RACF ids |
| `carddemo.data-directory` | `CARDDEMO_DATA_DIR` | `../app/data/ASCII` | `//SYSUT1` inputs of the load jobs |
| `carddemo.output-directory` | `CARDDEMO_OUTPUT_DIR` | `./target/carddemo-output` | `//STMT-FILE`, `//TRANREPT` outputs |

`mvn test` runs the suite against in-memory H2 (PostgreSQL mode); no database is needed to build.

## Copybooks → tables → entities

| Copybook | Len | Legacy file | Table | Entity |
| --- | --- | --- | --- | --- |
| `CSUSR01Y` | 80 | USRSEC (KSDS) | `sec_user` | `SecUser` |
| `CVCUS01Y` | 500 | CUSTDATA | `customer` | `Customer` |
| `CVACT01Y` | 300 | ACCTDATA | `account` | `Account` |
| `CVACT02Y` | 150 | CARDDATA (+ CARDAIX) | `card` | `Card` |
| `CVACT03Y` | 50 | CARDXREF (+ CXACAIX) | `card_xref` | `CardXref` |
| `CVTRA03Y` | 60 | TRANTYPE | `transaction_type` | `TransactionType` |
| `CVTRA04Y` | 60 | TRANCATG | `transaction_category_type` | `TransactionCategoryType` |
| `CVTRA02Y` | 50 | DISCGRP | `disclosure_group` | `DisclosureGroup` |
| `CVTRA01Y` | 50 | TCATBAL | `transaction_category_balance` | `TransactionCategoryBalance` |
| `CVTRA05Y` | 350 | TRANSACT (KSDS) | `transaction` | `Transaction` |
| `CVTRA06Y` | 350 | DALYTRAN | `daily_transaction` | `DailyTransaction` |
| — | 350+80 | DALYREJS | `daily_transaction_reject` | `DailyTransactionReject` |

Type mapping rules used in `V1__carddemo_schema.sql`:

* `PIC S9(n)V99` → `NUMERIC(n+2,2)` mapped to `BigDecimal` — no binary floating point anywhere.
* `PIC 9(n)` keys/ids → `BIGINT`/`INTEGER` plus a `CHECK` that preserves the original digit width.
* `PIC X(n)` → `VARCHAR(n)`; trailing blanks are trimmed on read, so fixed-width padding does not
  leak into the API.
* VSAM primary keys become primary keys; alternate indexes (CARDAIX, CXACAIX, CXACAIX-by-customer)
  become secondary indexes plus derived repository finders.
* `FILLER` fields are dropped; they only existed to pad the record to its VSAM length.

## Online programs (CICS/BMS) → REST

| COBOL | Screen | Endpoint | Service |
| --- | --- | --- | --- |
| `COSGN00C` | COSGN00 | `POST /api/v1/signon` | `SignonService` |
| `COMEN01C` | COMEN01 | `GET /api/v1/menus/main`, `/main/{option}` | `MenuService` |
| `COADM01C` | COADM01 | `GET /api/v1/menus/admin`, `/admin/{option}` | `MenuService` |
| `COACTVWC` | COACTVW | `GET /api/v1/accounts/{accountId}` | `AccountService` |
| `COACTUPC` | COACTUP | `PUT /api/v1/accounts/{accountId}` | `AccountService`, `AccountUpdateValidator` |
| `COCRDLIC` | COCRDLI | `GET /api/v1/cards` | `CardService` |
| `COCRDSLC` | COCRDSL | `GET /api/v1/cards/{cardNumber}` | `CardService` |
| `COCRDUPC` | COCRDUP | `PUT /api/v1/cards/{cardNumber}` | `CardService` |
| `COTRN00C` | COTRN00 | `GET /api/v1/transactions` | `TransactionService` |
| `COTRN01C` | COTRN01 | `GET /api/v1/transactions/{transactionId}` | `TransactionService` |
| `COTRN02C` | COTRN02 | `POST /api/v1/transactions` | `TransactionService` |
| `CORPT00C` | CORPT00 | `POST /api/v1/reports/transactions` | `TransactionReportService` |
| `COBIL00C` | COBIL00 | `POST /api/v1/bill-payments` | `BillPaymentService` |
| `COUSR00C`–`COUSR03C` | COUSR0x | `GET/POST/PUT/DELETE /api/v1/admin/users` | `UserAdminService` |

CICS idioms are translated as follows: `EXEC CICS READ/WRITE/REWRITE/DELETE` → repository calls
inside one `@Transactional` unit of work; `STARTBR/READNEXT` paging → `PageRequest`; `XCTL`/`RETURN
TRANSID` navigation → `legacyProgram` hints on the menu/signon responses; the BMS error message line
→ `ApiError` bodies produced by `GlobalExceptionHandler` (400 business rule, 404 not found, 409
concurrent update — the "Record changed by someone else" path of `COACTUPC`/`COCRDUPC`).

## Batch programs (JCL) → Spring Batch

| JCL job | COBOL | Spring Batch job | Notes |
| --- | --- | --- | --- |
| `POSTTRAN` | `CBTRN02C` | `postTransactionsJob` | `TransactionPostingService`; rejects with reason 100–103 land in `daily_transaction_reject`, return code 4 when any reject |
| `INTCALC` | `CBACT04C` | `interestCalculationJob` | `InterestCalculationService`; `bal * rate / 1200`, writes type 01 / category 05 transactions, resets cycle totals |
| `CREASTMT` | `CBSTM03A`/`CBSTM03B` | `statementGenerationJob` | `StatementService`; plain-text and HTML statements |
| `TRANREPT` | `CBTRN03C` | `transactionReportJob` | `TransactionReportService`; monthly/yearly/custom date ranges |
| `ACCTFILE`/`CARDFILE`/`XREFFILE`/`CUSTFILE`/`TRANFILE` reads | `CBACT01C`–`CBACT03C`, `CBCUS01C`, `CBTRN01C` | `printAccountsJob`, `printCardsJob`, `printCardXrefJob`, `printCustomersJob`, `printDailyTransactionsJob` | sequential file dumps kept as read-only jobs |
| IDCAMS `REPRO` load steps | — | `loadLegacyDataJob` | `LegacyDataLoader` parses `app/data/ASCII/*.txt` with the copybook offsets |
| `CSUTLDTC` | `CSUTLDTC` | — | `CobolDateValidator` utility (same severity codes and messages) |

Jobs can also be launched over HTTP: `POST /api/v1/batch/{jobName}` (`BatchJobController`), the
replacement for submitting JCL.

## Ported business rules worth calling out

* **Signon** (`COSGN00C`): ids and passwords are upper-cased, blanks rejected with the original
  messages, admins routed to `COADM01C` and everyone else to `COMEN01C`.
* **Posting** (`CBTRN02C`): card must exist in the xref (reason 100), account must exist (101),
  `curr_bal + cycle_credit - cycle_debit + amount` must stay within the credit limit (102), and the
  transaction must fall before the account expiration date (103); accepted transactions update the
  account, the category balance and the `transaction` table in one transaction.
* **Interest** (`CBACT04C`): rate from the account's disclosure group, falling back to `DEFAULT`;
  merchant fields are blanked exactly as the COBOL `MOVE SPACES` does.
* **Bill payment** (`COBIL00C`): type 02 / category 2, source `POS TERM`, merchant `999999999`,
  balance zeroed.
* **Account update** (`COACTUPC` + `CSLKPCDY`): state, state/ZIP combination, phone area code, SSN,
  FICO 300–850, date and amount edits are all preserved, including the optimistic "record changed"
  check.

## Legacy data

`loadLegacyDataJob` reads the shipped ASCII extracts (`app/data/ASCII/acctdata.txt`, `carddata.txt`,
`custdata.txt`, `cardxref.txt`, `trantype.txt`, `trancatg.txt`, `discgrp.txt`, `tcatbal.txt`,
`dailytran.txt`) using the copybook offsets, and the ten users from `app/jcl/DUSRSECJ.jcl` (bundled
as `src/main/resources/legacy-data/usrsec.txt`). Signed display numerics — including zoned overpunch
(`{`/`}`, `A`–`R`) — are decoded by `CobolNumbers`.

Passwords are still the legacy 8-byte cleartext values so the ported signon logic matches the
original; hashing them is deliberately left as a follow-up migration rather than a silent behavior
change.
