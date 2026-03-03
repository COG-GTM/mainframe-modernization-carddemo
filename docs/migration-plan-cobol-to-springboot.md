# Migration Plan: COBOL Transaction Processing to Spring Boot Microservice

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current System Analysis](#2-current-system-analysis)
3. [Functional Mapping](#3-functional-mapping)
4. [Data Layer Migration](#4-data-layer-migration)
5. [Batch Processing Strategy](#5-batch-processing-strategy)
6. [Transaction Validation Logic](#6-transaction-validation-logic)
7. [Reject Handling](#7-reject-handling)
8. [Reporting](#8-reporting)
9. [Suggested Spring Boot Project Structure](#9-suggested-spring-boot-project-structure)
10. [Migration Phases and Timeline](#10-migration-phases-and-timeline)
11. [Risk Assessment and Mitigations](#11-risk-assessment-and-mitigations)
12. [Appendix: COBOL-to-Java Data Type Mapping](#appendix-a-cobol-to-java-data-type-mapping)

---

## 1. Executive Summary

This document describes a plan to migrate three core COBOL batch transaction processing programs from a mainframe environment to a **Spring Boot microservice** implemented in Java. The programs in scope are:

| COBOL Program | Source File | Purpose |
|---------------|------------|---------|
| `CBTRN01C` | `app/cbl/CBTRN01C.cbl` | Reads daily transaction file, performs cross-reference lookups, and displays results (pre-validation/ingestion step) |
| `CBTRN02C` | `app/cbl/CBTRN02C.cbl` | Core batch transaction posting job (`POSTTRAN`) -- validates each transaction, posts accepted ones to the transaction master and updates account balances, writes rejected ones to a reject file |
| `CBTRN03C` | `app/cbl/CBTRN03C.cbl` | Reads the posted transaction file, enriches each record with cross-reference and category lookups, and produces a paginated transaction detail report |

The target architecture replaces:
- **COBOL programs** with Java Spring Boot services and Spring Batch jobs
- **VSAM KSDS files** with relational database tables accessed via Spring Data JPA
- **JCL batch scheduling** with Spring Batch jobs triggered by `@Scheduled` or an external scheduler
- **Sequential flat-file output** (rejects, reports) with database tables and downloadable report endpoints

---

## 2. Current System Analysis

### 2.1 Program-Level Summary

#### CBTRN01C -- Daily Transaction Ingestion (490 lines)

**Responsibility:** Opens the daily transaction sequential file (`DALYTRAN`) and reads records one at a time. For each record, it looks up the card number in the cross-reference file (`XREFFILE`) to resolve the associated account ID, then reads the account master (`ACCTFILE`). It is a read-only pre-validation/display step -- it does not write to any output files.

**Files accessed (all VSAM KSDS unless noted):**

| Logical Name | VSAM Dataset | Access Mode | Copybook |
|-------------|-------------|-------------|----------|
| `DALYTRAN-FILE` | `AWS.M2.CARDDEMO.DALYTRAN.PS` | Sequential / Input | `CVTRA06Y` (350-byte daily transaction record) |
| `CUSTOMER-FILE` | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | Random / Input | `CVCUS01Y` (500-byte customer record) |
| `XREF-FILE` | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | Random / Input | `CVACT03Y` (50-byte card cross-reference) |
| `CARD-FILE` | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | Random / Input | `CVACT02Y` (150-byte card record) |
| `ACCOUNT-FILE` | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | Random / Input | `CVACT01Y` (300-byte account record) |
| `TRANSACT-FILE` | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | Random / Input | `CVTRA05Y` (350-byte transaction record) |

**Key logic (lines 164-186):** Sequential read loop over `DALYTRAN-FILE`; for each record, perform `2000-LOOKUP-XREF` (card-to-account resolution) then `3000-READ-ACCOUNT` (account master read). Unresolved cards are logged; accounts not found are logged. No writes occur.

#### CBTRN02C -- Core Transaction Posting (`POSTTRAN`) (732 lines)

**Responsibility:** The central batch program. Reads the daily transaction file sequentially, validates each transaction (card cross-reference lookup, account lookup, credit-limit check, expiration-date check), then either posts the transaction (writes to transaction master, updates account balances, updates transaction category balance) or writes a reject record with a reason code.

**Files accessed:**

| Logical Name | VSAM Dataset | Access Mode | Copybook |
|-------------|-------------|-------------|----------|
| `DALYTRAN-FILE` | `AWS.M2.CARDDEMO.DALYTRAN.PS` | Sequential / Input | `CVTRA06Y` |
| `TRANSACT-FILE` | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | Random / Output (write) | `CVTRA05Y` |
| `XREF-FILE` | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | Random / Input | `CVACT03Y` |
| `DALYREJS-FILE` | `AWS.M2.CARDDEMO.DALYREJS(+1)` (GDG) | Sequential / Output | Custom 430-byte reject record (350-byte tran + 80-byte trailer) |
| `ACCOUNT-FILE` | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | Random / I-O (read + rewrite) | `CVACT01Y` |
| `TCATBAL-FILE` | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | Random / I-O (read + write/rewrite) | `CVTRA01Y` |

**Core processing loop (lines 202-219):**
```
FOR EACH daily transaction record:
  1. Read next record from DALYTRAN
  2. Increment transaction count
  3. Reset validation fail reason to 0
  4. PERFORM 1500-VALIDATE-TRAN
     a. 1500-A-LOOKUP-XREF: Card number -> XREF lookup. Fail reason 100 if not found.
     b. 1500-B-LOOKUP-ACCT: Account ID -> Account lookup. Fail reason 101 if not found.
        - Compute temp balance = current_cycle_credit - current_cycle_debit + tran_amount
        - If temp balance > credit limit -> Fail reason 102 ("OVERLIMIT")
        - If account expiration date < transaction date -> Fail reason 103 ("EXPIRED")
  5. IF validation passed (reason = 0):
     PERFORM 2000-POST-TRANSACTION
       a. Map daily tran fields to transaction record fields
       b. Generate DB2-format timestamp for TRAN-PROC-TS
       c. 2700-UPDATE-TCATBAL: Read/create transaction category balance record, add amount
       d. 2800-UPDATE-ACCOUNT-REC: Update ACCT-CURR-BAL, ACCT-CURR-CYC-CREDIT or DEBIT
       e. 2900-WRITE-TRANSACTION-FILE: Write to TRANSACT master
  6. ELSE:
     Increment reject count
     PERFORM 2500-WRITE-REJECT-REC: Write tran data + validation trailer to rejects file
```

**Validation fail reason codes:**

| Code | Description | Source Paragraph |
|------|------------|-----------------|
| 100 | Invalid card number (XREF not found) | `1500-A-LOOKUP-XREF` |
| 101 | Account record not found | `1500-B-LOOKUP-ACCT` |
| 102 | Over credit limit | `1500-B-LOOKUP-ACCT` |
| 103 | Account expired | `1500-B-LOOKUP-ACCT` |

**Exit behavior (lines 227-231):** Displays counts of processed and rejected transactions. Sets `RETURN-CODE = 4` if any rejects exist (allows JCL conditional step execution).

#### CBTRN03C -- Transaction Detail Report (650 lines)

**Responsibility:** Reads the posted transaction file sequentially (sorted by card number), enriches each record by looking up cross-reference (card -> account), transaction type description, and transaction category description. Produces a paginated flat-file report with headers, detail lines, page totals, account totals, and grand totals. Filters transactions by a date range read from a parameter file.

**Files accessed:**

| Logical Name | VSAM Dataset | Access Mode | Copybook |
|-------------|-------------|-------------|----------|
| `TRANSACT-FILE` | `AWS.M2.CARDDEMO.TRANSACT.DALY(+1)` | Sequential / Input | `CVTRA05Y` |
| `XREF-FILE` | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | Random / Input | `CVACT03Y` |
| `TRANTYPE-FILE` | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | Random / Input | `CVTRA03Y` |
| `TRANCATG-FILE` | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | Random / Input | `CVTRA04Y` |
| `REPORT-FILE` | `AWS.M2.CARDDEMO.TRANREPT(+1)` | Sequential / Output | `CVTRA07Y` (133-byte report line) |
| `DATE-PARMS-FILE` | `AWS.M2.CARDDEMO.DATEPARM` | Sequential / Input | Custom (start date + end date) |

**Report structure (from `CVTRA07Y` copybook):**

| Section | Content |
|---------|---------|
| `REPORT-NAME-HEADER` | Report short name ("DALYREPT"), long name, date range |
| `TRANSACTION-HEADER-1` | Column headers: Transaction ID, Account ID, Type, Category, Source, Amount |
| `TRANSACTION-HEADER-2` | Separator line (dashes) |
| `TRANSACTION-DETAIL-REPORT` | Detail: trans ID, account ID, type code+desc, cat code+desc, source, amount |
| `REPORT-PAGE-TOTALS` | Page total |
| `REPORT-ACCOUNT-TOTALS` | Account-level total (when card number changes) |
| `REPORT-GRAND-TOTALS` | Grand total across all transactions |

**Pagination:** Every `WS-PAGE-SIZE` (20) detail lines, write page totals and new headers.

### 2.2 Data Structures (Copybooks)

| Copybook | Record Name | Length | Key Fields | Purpose |
|----------|------------|--------|------------|---------|
| `CVTRA06Y` | `DALYTRAN-RECORD` | 350 | `DALYTRAN-ID` (16) | Daily transaction input record |
| `CVTRA05Y` | `TRAN-RECORD` | 350 | `TRAN-ID` (16) | Posted transaction master record |
| `CVACT03Y` | `CARD-XREF-RECORD` | 50 | `XREF-CARD-NUM` (16) | Card-to-account-to-customer cross-reference |
| `CVACT01Y` | `ACCOUNT-RECORD` | 300 | `ACCT-ID` (11) | Account master |
| `CVACT02Y` | `CARD-RECORD` | 150 | `CARD-NUM` (16) | Card master |
| `CVCUS01Y` | `CUSTOMER-RECORD` | 500 | `CUST-ID` (9) | Customer master |
| `CVTRA01Y` | `TRAN-CAT-BAL-RECORD` | 50 | Composite: `TRANCAT-ACCT-ID`(11) + `TRANCAT-TYPE-CD`(2) + `TRANCAT-CD`(4) | Transaction category balance |
| `CVTRA03Y` | `TRAN-TYPE-RECORD` | 60 | `TRAN-TYPE` (2) | Transaction type reference |
| `CVTRA04Y` | `TRAN-CAT-RECORD` | 60 | Composite: `TRAN-TYPE-CD`(2) + `TRAN-CAT-CD`(4) | Transaction category reference |
| `CVTRA07Y` | Various report structures | 133 | N/A | Report layout definitions |

### 2.3 Batch Job Flow (JCL)

The JCL `POSTTRAN.jcl` executes a single step:
```
//STEP15 EXEC PGM=CBTRN02C
```
It maps DD names to VSAM datasets and creates a new GDG generation for the daily rejects file (`DALYREJS(+1)`).

The JCL `TRANREPT.jcl` executes a multi-step pipeline:
1. **STEP05R**: Unload the VSAM transaction master to a sequential backup file
2. **STEP05R** (SORT): Filter transactions by date range and sort by card number
3. **STEP10R**: Execute `CBTRN03C` to produce the formatted report

---

## 3. Functional Mapping

### 3.1 COBOL Program to Spring Boot Component Mapping

| COBOL Program | COBOL Responsibility | Spring Boot Component | Description |
|---------------|---------------------|----------------------|-------------|
| `CBTRN01C` | Read daily transactions, validate card/account exist | `DailyTransactionIngestionJob` (Spring Batch `Job`) | A Spring Batch job with a `FlatFileItemReader` (or JPA reader) that reads daily transaction records, resolves card cross-references, and logs unresolvable records. This becomes Step 1 of the batch pipeline. |
| `CBTRN02C` | Validate and post transactions, write rejects | `TransactionPostingJob` (Spring Batch `Job`) | The core batch job. Uses an `ItemReader` to read daily transactions, an `ItemProcessor` for validation (replaces `1500-VALIDATE-TRAN`), and an `ItemWriter` that either posts the transaction or writes a reject record. |
| `CBTRN03C` | Generate transaction detail report | `TransactionReportJob` (Spring Batch `Job`) or `TransactionReportService` | A Spring Batch job or scheduled service that queries posted transactions within a date range, enriches them with type/category descriptions, and generates a report (PDF, CSV, or HTML). |

### 3.2 Detailed Component Mapping for CBTRN02C

The most complex program, `CBTRN02C`, maps to a Spring Batch `Job` with the following structure:

```
TransactionPostingJob (Spring Batch Job)
  |
  +-- Step 1: "postTransactions"
        |
        +-- ItemReader: DailyTransactionReader
        |     Reads from the `daily_transaction` staging table (or file)
        |
        +-- ItemProcessor: TransactionValidationProcessor
        |     Replaces COBOL paragraphs:
        |       1500-A-LOOKUP-XREF  -> CardXrefRepository.findByCardNumber()
        |       1500-B-LOOKUP-ACCT  -> AccountRepository.findById()
        |       Credit limit check  -> if (tempBal > account.getCreditLimit()) reject
        |       Expiration check    -> if (account.getExpirationDate().isBefore(tranDate)) reject
        |
        +-- ItemWriter: CompositeItemWriter
              |
              +-- TransactionPostingWriter (for valid items)
              |     Replaces COBOL paragraphs:
              |       2000-POST-TRANSACTION   -> transactionRepository.save()
              |       2700-UPDATE-TCATBAL      -> tranCatBalRepository.save/update()
              |       2800-UPDATE-ACCOUNT-REC  -> accountRepository.save()
              |
              +-- RejectRecordWriter (for rejected items)
                    Replaces COBOL paragraph:
                      2500-WRITE-REJECT-REC   -> rejectedTransactionRepository.save()
```

### 3.3 COBOL Paragraph to Java Method Mapping

| COBOL Paragraph | Java Method / Class | Notes |
|----------------|---------------------|-------|
| `1000-DALYTRAN-GET-NEXT` | `DailyTransactionReader.read()` | Spring Batch `ItemReader` handles EOF detection automatically |
| `1500-VALIDATE-TRAN` | `TransactionValidationProcessor.process()` | Returns `null` for rejects (skipped by writer) or the validated item |
| `1500-A-LOOKUP-XREF` | `CardXrefRepository.findByCardNumber(String)` | Spring Data JPA query method |
| `1500-B-LOOKUP-ACCT` | `AccountRepository.findById(Long)` + validation logic in processor | Business rules for credit limit and expiration |
| `2000-POST-TRANSACTION` | `TransactionPostingWriter.write(List<Transaction>)` | Batch insert to `transaction` table |
| `2500-WRITE-REJECT-REC` | `RejectedTransactionRepository.save(RejectedTransaction)` | Persists reject with reason code and description |
| `2700-UPDATE-TCATBAL` | `TransactionCategoryBalanceService.updateBalance()` | Upsert logic (create if not exists, update if exists) |
| `2800-UPDATE-ACCOUNT-REC` | `AccountService.updateAccountBalance()` | Adds amount to current balance, updates cycle credit/debit |
| `2900-WRITE-TRANSACTION-FILE` | `TransactionRepository.save(Transaction)` | JPA entity save |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `LocalDateTime.now()` or `Instant.now()` | Java handles timestamp formatting natively |

---

## 4. Data Layer Migration

### 4.1 VSAM to Relational Database Mapping

Each VSAM KSDS file maps to a relational database table. The VSAM primary key becomes the JPA `@Id` or a composite `@EmbeddedId`. We recommend **PostgreSQL** as the target database, though any JPA-compatible database will work.

#### 4.1.1 Table Definitions

**`daily_transaction`** (from `CVTRA06Y` -- `DALYTRAN-RECORD`, 350 bytes)

| COBOL Field | COBOL Type | Java Type | Column Name | Column Type | Notes |
|------------|-----------|-----------|-------------|-------------|-------|
| `DALYTRAN-ID` | PIC X(16) | `String` | `id` | `VARCHAR(16)` | Primary Key |
| `DALYTRAN-TYPE-CD` | PIC X(02) | `String` | `type_code` | `VARCHAR(2)` | FK to `transaction_type` |
| `DALYTRAN-CAT-CD` | PIC 9(04) | `Integer` | `category_code` | `INTEGER` | FK to `transaction_category` |
| `DALYTRAN-SOURCE` | PIC X(10) | `String` | `source` | `VARCHAR(10)` | |
| `DALYTRAN-DESC` | PIC X(100) | `String` | `description` | `VARCHAR(100)` | |
| `DALYTRAN-AMT` | PIC S9(09)V99 | `BigDecimal` | `amount` | `DECIMAL(11,2)` | Signed with 2 decimal places |
| `DALYTRAN-MERCHANT-ID` | PIC 9(09) | `Long` | `merchant_id` | `BIGINT` | |
| `DALYTRAN-MERCHANT-NAME` | PIC X(50) | `String` | `merchant_name` | `VARCHAR(50)` | |
| `DALYTRAN-MERCHANT-CITY` | PIC X(50) | `String` | `merchant_city` | `VARCHAR(50)` | |
| `DALYTRAN-MERCHANT-ZIP` | PIC X(10) | `String` | `merchant_zip` | `VARCHAR(10)` | |
| `DALYTRAN-CARD-NUM` | PIC X(16) | `String` | `card_number` | `VARCHAR(16)` | FK to `card_xref` |
| `DALYTRAN-ORIG-TS` | PIC X(26) | `LocalDateTime` | `original_timestamp` | `TIMESTAMP` | |
| `DALYTRAN-PROC-TS` | PIC X(26) | `LocalDateTime` | `processed_timestamp` | `TIMESTAMP` | |

**`transaction`** (from `CVTRA05Y` -- `TRAN-RECORD`, 350 bytes)

Same structure as `daily_transaction` but represents posted/committed transactions. The `id` column (`TRAN-ID`) is the primary key.

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `TRAN-ID` | `String` | `id` | `VARCHAR(16)` PK |
| `TRAN-TYPE-CD` | `String` | `type_code` | `VARCHAR(2)` |
| `TRAN-CAT-CD` | `Integer` | `category_code` | `INTEGER` |
| `TRAN-SOURCE` | `String` | `source` | `VARCHAR(10)` |
| `TRAN-DESC` | `String` | `description` | `VARCHAR(100)` |
| `TRAN-AMT` | `BigDecimal` | `amount` | `DECIMAL(11,2)` |
| `TRAN-MERCHANT-ID` | `Long` | `merchant_id` | `BIGINT` |
| `TRAN-MERCHANT-NAME` | `String` | `merchant_name` | `VARCHAR(50)` |
| `TRAN-MERCHANT-CITY` | `String` | `merchant_city` | `VARCHAR(50)` |
| `TRAN-MERCHANT-ZIP` | `String` | `merchant_zip` | `VARCHAR(10)` |
| `TRAN-CARD-NUM` | `String` | `card_number` | `VARCHAR(16)` |
| `TRAN-ORIG-TS` | `LocalDateTime` | `original_timestamp` | `TIMESTAMP` |
| `TRAN-PROC-TS` | `LocalDateTime` | `processed_timestamp` | `TIMESTAMP` |

**`account`** (from `CVACT01Y` -- `ACCOUNT-RECORD`, 300 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `ACCT-ID` | `Long` | `id` | `BIGINT` PK |
| `ACCT-ACTIVE-STATUS` | `String` | `active_status` | `CHAR(1)` |
| `ACCT-CURR-BAL` | `BigDecimal` | `current_balance` | `DECIMAL(12,2)` |
| `ACCT-CREDIT-LIMIT` | `BigDecimal` | `credit_limit` | `DECIMAL(12,2)` |
| `ACCT-CASH-CREDIT-LIMIT` | `BigDecimal` | `cash_credit_limit` | `DECIMAL(12,2)` |
| `ACCT-OPEN-DATE` | `LocalDate` | `open_date` | `DATE` |
| `ACCT-EXPIRAION-DATE` | `LocalDate` | `expiration_date` | `DATE` |
| `ACCT-REISSUE-DATE` | `LocalDate` | `reissue_date` | `DATE` |
| `ACCT-CURR-CYC-CREDIT` | `BigDecimal` | `current_cycle_credit` | `DECIMAL(12,2)` |
| `ACCT-CURR-CYC-DEBIT` | `BigDecimal` | `current_cycle_debit` | `DECIMAL(12,2)` |
| `ACCT-ADDR-ZIP` | `String` | `address_zip` | `VARCHAR(10)` |
| `ACCT-GROUP-ID` | `String` | `group_id` | `VARCHAR(10)` |

**`card_xref`** (from `CVACT03Y` -- `CARD-XREF-RECORD`, 50 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `XREF-CARD-NUM` | `String` | `card_number` | `VARCHAR(16)` PK |
| `XREF-CUST-ID` | `Long` | `customer_id` | `BIGINT` |
| `XREF-ACCT-ID` | `Long` | `account_id` | `BIGINT` FK to `account` |

**`card`** (from `CVACT02Y` -- `CARD-RECORD`, 150 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `CARD-NUM` | `String` | `card_number` | `VARCHAR(16)` PK |
| `CARD-ACCT-ID` | `Long` | `account_id` | `BIGINT` |
| `CARD-CVV-CD` | `Integer` | `cvv_code` | `INTEGER` |
| `CARD-EMBOSSED-NAME` | `String` | `embossed_name` | `VARCHAR(50)` |
| `CARD-EXPIRAION-DATE` | `LocalDate` | `expiration_date` | `DATE` |
| `CARD-ACTIVE-STATUS` | `String` | `active_status` | `CHAR(1)` |

**`customer`** (from `CVCUS01Y` -- `CUSTOMER-RECORD`, 500 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `CUST-ID` | `Long` | `id` | `BIGINT` PK |
| `CUST-FIRST-NAME` | `String` | `first_name` | `VARCHAR(25)` |
| `CUST-MIDDLE-NAME` | `String` | `middle_name` | `VARCHAR(25)` |
| `CUST-LAST-NAME` | `String` | `last_name` | `VARCHAR(25)` |
| `CUST-ADDR-LINE-1` | `String` | `address_line_1` | `VARCHAR(50)` |
| `CUST-ADDR-LINE-2` | `String` | `address_line_2` | `VARCHAR(50)` |
| `CUST-ADDR-LINE-3` | `String` | `address_line_3` | `VARCHAR(50)` |
| `CUST-ADDR-STATE-CD` | `String` | `state_code` | `CHAR(2)` |
| `CUST-ADDR-COUNTRY-CD` | `String` | `country_code` | `CHAR(3)` |
| `CUST-ADDR-ZIP` | `String` | `address_zip` | `VARCHAR(10)` |
| `CUST-PHONE-NUM-1` | `String` | `phone_number_1` | `VARCHAR(15)` |
| `CUST-PHONE-NUM-2` | `String` | `phone_number_2` | `VARCHAR(15)` |
| `CUST-SSN` | `Long` | `ssn` | `BIGINT` |
| `CUST-GOVT-ISSUED-ID` | `String` | `govt_issued_id` | `VARCHAR(20)` |
| `CUST-DOB-YYYY-MM-DD` | `LocalDate` | `date_of_birth` | `DATE` |
| `CUST-EFT-ACCOUNT-ID` | `String` | `eft_account_id` | `VARCHAR(10)` |
| `CUST-PRI-CARD-HOLDER-IND` | `String` | `primary_cardholder_indicator` | `CHAR(1)` |
| `CUST-FICO-CREDIT-SCORE` | `Integer` | `fico_credit_score` | `INTEGER` |

**`transaction_category_balance`** (from `CVTRA01Y` -- `TRAN-CAT-BAL-RECORD`, 50 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `TRANCAT-ACCT-ID` | `Long` | `account_id` | `BIGINT` | Composite PK part 1 |
| `TRANCAT-TYPE-CD` | `String` | `type_code` | `VARCHAR(2)` | Composite PK part 2 |
| `TRANCAT-CD` | `Integer` | `category_code` | `INTEGER` | Composite PK part 3 |
| `TRAN-CAT-BAL` | `BigDecimal` | `balance` | `DECIMAL(11,2)` | Running balance |

**`transaction_type`** (from `CVTRA03Y` -- `TRAN-TYPE-RECORD`, 60 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `TRAN-TYPE` | `String` | `type_code` | `VARCHAR(2)` PK |
| `TRAN-TYPE-DESC` | `String` | `description` | `VARCHAR(50)` |

**`transaction_category`** (from `CVTRA04Y` -- `TRAN-CAT-RECORD`, 60 bytes)

| COBOL Field | Java Type | Column Name | Column Type |
|------------|-----------|-------------|-------------|
| `TRAN-TYPE-CD` | `String` | `type_code` | `VARCHAR(2)` | Composite PK part 1 |
| `TRAN-CAT-CD` | `Integer` | `category_code` | `INTEGER` | Composite PK part 2 |
| `TRAN-CAT-TYPE-DESC` | `String` | `description` | `VARCHAR(50)` |

**`rejected_transaction`** (new table -- replaces `DALYREJS-FILE`)

| Column Name | Column Type | Notes |
|-------------|-------------|-------|
| `id` | `BIGINT` AUTO | Surrogate PK |
| `daily_transaction_id` | `VARCHAR(16)` | FK to original daily transaction |
| `transaction_data` | `TEXT` or `JSONB` | Full original transaction record (for audit) |
| `reject_reason_code` | `INTEGER` | Maps to COBOL `WS-VALIDATION-FAIL-REASON` |
| `reject_reason_description` | `VARCHAR(76)` | Maps to COBOL `WS-VALIDATION-FAIL-REASON-DESC` |
| `rejected_at` | `TIMESTAMP` | When the rejection occurred |
| `batch_job_execution_id` | `BIGINT` | Links to Spring Batch job execution for traceability |

### 4.2 DB2 Usage Mapping

The existing system uses DB2 in some optional modules (e.g., `COBTUPDT` for transaction type management with embedded `EXEC SQL` blocks). In the Spring Boot target:

- **DB2 embedded SQL** maps directly to **Spring Data JPA repositories** or **`@Query` annotated methods**
- `EXEC SQL SELECT ... INTO :host-var` becomes `repository.findById()` or a custom JPQL query
- `EXEC SQL INSERT` becomes `repository.save(entity)`
- `EXEC SQL UPDATE` becomes modifying the entity and calling `repository.save(entity)`
- DB2 `COMMIT` / `ROLLBACK` maps to Spring `@Transactional` annotation boundaries

### 4.3 Data Migration Strategy

1. **Extract** VSAM data using mainframe utilities (IDCAMS REPRO to sequential files, or use AWS M2 data export)
2. **Transform** EBCDIC-encoded fixed-width records to UTF-8 CSV/JSON using a conversion utility
3. **Load** into PostgreSQL using Spring Batch `FlatFileItemReader` + JPA `ItemWriter`, or using `COPY` commands for bulk import
4. **Validate** record counts and key field checksums between source and target

---

## 5. Batch Processing Strategy

### 5.1 JCL to Spring Batch Mapping

| JCL Job | COBOL Program | Spring Batch Job | Trigger Method |
|---------|--------------|-----------------|----------------|
| (implicit) | `CBTRN01C` | `DailyTransactionIngestionJob` | `@Scheduled` or on-demand REST endpoint |
| `POSTTRAN.jcl` | `CBTRN02C` | `TransactionPostingJob` | `@Scheduled(cron = "0 0 2 * * ?")` (e.g., daily at 2 AM) or triggered via REST |
| `TRANREPT.jcl` | `CBTRN03C` | `TransactionReportJob` | `@Scheduled` or on-demand REST endpoint |

### 5.2 Spring Batch Job Configuration for POSTTRAN

```java
@Configuration
@EnableBatchProcessing
public class TransactionPostingJobConfig {

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository,
                                      Step postTransactionsStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new TransactionPostingJobListener())  // replaces DISPLAY statements
                .start(postTransactionsStep)
                .build();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      ItemReader<DailyTransaction> reader,
                                      ItemProcessor<DailyTransaction, TransactionPostingResult> processor,
                                      ItemWriter<TransactionPostingResult> writer) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<DailyTransaction, TransactionPostingResult>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(0)  // fail on any unexpected error, matching COBOL ABEND behavior
                .listener(new PostTransactionStepListener())
                .build();
    }

    @Bean
    public ItemReader<DailyTransaction> dailyTransactionReader(
            DailyTransactionRepository repository) {
        return new RepositoryItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .repository(repository)
                .methodName("findUnprocessed")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }
}
```

### 5.3 JCL Conditional Execution Mapping

The COBOL program sets `RETURN-CODE = 4` when rejects exist. In Spring Batch, this maps to:

```java
@Component
public class TransactionPostingJobListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        long rejectCount = jobExecution.getExecutionContext()
                .getLong("rejectCount", 0);
        long totalCount = jobExecution.getExecutionContext()
                .getLong("transactionCount", 0);

        log.info("Transactions processed: {}", totalCount);
        log.info("Transactions rejected: {}", rejectCount);

        if (rejectCount > 0) {
            jobExecution.setExitStatus(new ExitStatus("COMPLETED_WITH_REJECTS"));
        }
    }
}
```

### 5.4 TRANREPT Multi-Step Job

The `TRANREPT.jcl` is a 3-step pipeline. In Spring Batch, this becomes a multi-step job:

```java
@Bean
public Job transactionReportJob(JobRepository jobRepository,
                                 Step filterAndSortStep,
                                 Step generateReportStep) {
    return new JobBuilder("transactionReportJob", jobRepository)
            .start(filterAndSortStep)     // replaces SORT step
            .next(generateReportStep)      // replaces CBTRN03C execution
            .build();
}
```

The SORT step in JCL (filter by date range, sort by card number) becomes a database query:

```java
@Query("SELECT t FROM Transaction t " +
       "WHERE t.processedTimestamp BETWEEN :startDate AND :endDate " +
       "ORDER BY t.cardNumber ASC")
List<Transaction> findByDateRangeOrderByCardNumber(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);
```

---

## 6. Transaction Validation Logic

### 6.1 COBOL Validation Flow

The COBOL `1500-VALIDATE-TRAN` paragraph performs two sequential validations, short-circuiting on the first failure:

```
1500-VALIDATE-TRAN
  |
  +-- 1500-A-LOOKUP-XREF
  |     Card number -> XREF file lookup
  |     FAIL (100): "INVALID CARD NUMBER FOUND"
  |
  +-- (only if XREF found)
  |
  +-- 1500-B-LOOKUP-ACCT
        Account ID -> Account file lookup
        FAIL (101): "ACCOUNT RECORD NOT FOUND"
        |
        +-- Credit limit check:
        |   temp_bal = curr_cyc_credit - curr_cyc_debit + tran_amt
        |   FAIL (102): "OVERLIMIT TRANSACTION" if temp_bal > credit_limit
        |
        +-- Expiration check:
            FAIL (103): "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"
            if acct_expiration_date < tran_orig_timestamp
```

### 6.2 Spring Boot Validation Implementation

The validation logic maps to a Spring Batch `ItemProcessor` that implements a chain-of-responsibility pattern:

```java
@Component
public class TransactionValidationProcessor
        implements ItemProcessor<DailyTransaction, TransactionPostingResult> {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final RejectedTransactionRepository rejectedTransactionRepository;

    @Override
    public TransactionPostingResult process(DailyTransaction dailyTran) {
        // Step 1: Card cross-reference lookup (replaces 1500-A-LOOKUP-XREF)
        Optional<CardXref> xref = cardXrefRepository
                .findByCardNumber(dailyTran.getCardNumber());

        if (xref.isEmpty()) {
            return TransactionPostingResult.rejected(dailyTran,
                    100, "INVALID CARD NUMBER FOUND");
        }

        // Step 2: Account lookup and validation (replaces 1500-B-LOOKUP-ACCT)
        Optional<Account> account = accountRepository
                .findById(xref.get().getAccountId());

        if (account.isEmpty()) {
            return TransactionPostingResult.rejected(dailyTran,
                    101, "ACCOUNT RECORD NOT FOUND");
        }

        Account acct = account.get();

        // Step 2a: Credit limit check
        BigDecimal tempBalance = acct.getCurrentCycleCredit()
                .subtract(acct.getCurrentCycleDebit())
                .add(dailyTran.getAmount());

        if (tempBalance.compareTo(acct.getCreditLimit()) > 0) {
            return TransactionPostingResult.rejected(dailyTran,
                    102, "OVERLIMIT TRANSACTION");
        }

        // Step 2b: Account expiration check
        LocalDate tranDate = dailyTran.getOriginalTimestamp().toLocalDate();
        if (acct.getExpirationDate().isBefore(tranDate)) {
            return TransactionPostingResult.rejected(dailyTran,
                    103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        // All validations passed
        return TransactionPostingResult.accepted(dailyTran, xref.get(), acct);
    }
}
```

### 6.3 Extensibility

The COBOL source includes the comment `* ADD MORE VALIDATIONS HERE` at line 377 of `CBTRN02C.cbl`. The Spring Boot implementation should be designed for extensibility:

```java
public interface TransactionValidator {
    Optional<ValidationFailure> validate(DailyTransaction tran, CardXref xref, Account account);
}

@Component @Order(1)
public class CardXrefValidator implements TransactionValidator { ... }

@Component @Order(2)
public class AccountExistsValidator implements TransactionValidator { ... }

@Component @Order(3)
public class CreditLimitValidator implements TransactionValidator { ... }

@Component @Order(4)
public class AccountExpirationValidator implements TransactionValidator { ... }

// Future validators can be added by implementing TransactionValidator
// and annotating with @Component @Order(N)
```

The processor would inject `List<TransactionValidator>` and iterate through them, short-circuiting on the first failure -- exactly matching the COBOL behavior while being open for extension.

---

## 7. Reject Handling

### 7.1 Current COBOL Reject Handling

In `CBTRN02C`, rejected transactions are written to a sequential file (`DALYREJS-FILE`) as a 430-byte record:
- **Bytes 1-350**: The original daily transaction record (copied as-is from `DALYTRAN-RECORD`)
- **Bytes 351-430**: A validation trailer containing:
  - `WS-VALIDATION-FAIL-REASON` (4-digit numeric code)
  - `WS-VALIDATION-FAIL-REASON-DESC` (76-character description)

The rejects file uses a GDG (Generation Data Group) with a limit of 5 generations, meaning the last 5 days of rejects are retained.

### 7.2 Spring Boot Reject Handling Strategy

We recommend a **multi-channel approach** for reject handling:

#### 7.2.1 Primary: Reject Database Table

All rejected transactions are persisted to the `rejected_transaction` table (defined in Section 4.1.1). This replaces the sequential file and provides:
- Queryable reject history (vs. sequential-only access in VSAM)
- Retention policy via scheduled cleanup (replaces GDG limit of 5)
- Linkage to Spring Batch job execution for auditability

```java
@Entity
@Table(name = "rejected_transaction")
public class RejectedTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dailyTransactionId;

    @Column(columnDefinition = "TEXT")
    private String transactionDataJson;  // JSON serialization of original record

    private Integer rejectReasonCode;
    private String rejectReasonDescription;
    private LocalDateTime rejectedAt;
    private Long batchJobExecutionId;
}
```

#### 7.2.2 Secondary: Dead-Letter Topic (Optional, for Event-Driven Architectures)

If the organization uses a message broker (e.g., Apache Kafka, AWS SQS), rejected transactions can also be published to a dead-letter topic for downstream alerting and reprocessing:

```java
@Component
public class RejectEventPublisher {

    private final KafkaTemplate<String, RejectedTransactionEvent> kafkaTemplate;

    public void publishReject(RejectedTransaction reject) {
        kafkaTemplate.send("transaction-rejects-dlq",
                reject.getDailyTransactionId(),
                new RejectedTransactionEvent(reject));
    }
}
```

#### 7.2.3 REST API for Reject Inquiry

A REST endpoint replaces the need to manually browse the sequential rejects file:

```java
@RestController
@RequestMapping("/api/v1/rejects")
public class RejectedTransactionController {

    @GetMapping
    public Page<RejectedTransactionDto> getRejects(
            @RequestParam(required = false) Integer reasonCode,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            Pageable pageable) { ... }

    @GetMapping("/{id}")
    public RejectedTransactionDto getReject(@PathVariable Long id) { ... }

    @GetMapping("/summary")
    public RejectSummaryDto getRejectSummary(
            @RequestParam Long batchJobExecutionId) { ... }
}
```

#### 7.2.4 Reject Reason Code Mapping

| COBOL Code | COBOL Description | Java Enum |
|-----------|------------------|-----------|
| 100 | INVALID CARD NUMBER FOUND | `RejectReason.INVALID_CARD_NUMBER` |
| 101 | ACCOUNT RECORD NOT FOUND | `RejectReason.ACCOUNT_NOT_FOUND` |
| 102 | OVERLIMIT TRANSACTION | `RejectReason.OVER_CREDIT_LIMIT` |
| 103 | TRANSACTION RECEIVED AFTER ACCT EXPIRATION | `RejectReason.ACCOUNT_EXPIRED` |

```java
public enum RejectReason {
    INVALID_CARD_NUMBER(100, "Invalid card number -- cross-reference not found"),
    ACCOUNT_NOT_FOUND(101, "Account record not found for card"),
    OVER_CREDIT_LIMIT(102, "Transaction would exceed account credit limit"),
    ACCOUNT_EXPIRED(103, "Transaction received after account expiration date");

    private final int code;
    private final String description;
    // constructor, getters
}
```

---

## 8. Reporting

### 8.1 Current COBOL Reporting (CBTRN03C)

`CBTRN03C` produces a fixed-width, paginated text report (`TRANREPT`) with:
- A header showing report name and date range
- Column headers for Transaction ID, Account ID, Type, Category, Source, and Amount
- Detail lines for each transaction
- Page totals every 20 lines
- Account totals when the card number changes (break logic)
- A grand total at the end

The JCL pipeline (`TRANREPT.jcl`) first sorts transactions by card number and filters by date range using the mainframe SORT utility, then passes the sorted/filtered file to `CBTRN03C`.

### 8.2 Spring Boot Reporting Strategy

#### 8.2.1 Option A: Spring Batch Report Job (Recommended for backward compatibility)

A Spring Batch job that queries posted transactions and generates a downloadable report file:

```java
@Bean
public Job transactionReportJob(JobRepository jobRepository,
                                 Step generateReportStep) {
    return new JobBuilder("transactionReportJob", jobRepository)
            .start(generateReportStep)
            .build();
}

@Bean
public Step generateReportStep(JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                ItemReader<Transaction> reportReader,
                                ItemProcessor<Transaction, TransactionReportLine> enricher,
                                ItemWriter<TransactionReportLine> reportWriter) {
    return new StepBuilder("generateReportStep", jobRepository)
            .<Transaction, TransactionReportLine>chunk(100, txManager)
            .reader(reportReader)
            .processor(enricher)
            .writer(reportWriter)
            .build();
}
```

The `ItemProcessor` enriches each transaction with type and category descriptions (replacing `1500-B-LOOKUP-TRANTYPE` and `1500-C-LOOKUP-TRANCATG`):

```java
@Component
public class TransactionReportEnricher
        implements ItemProcessor<Transaction, TransactionReportLine> {

    private final CardXrefRepository xrefRepository;
    private final TransactionTypeRepository typeRepository;
    private final TransactionCategoryRepository categoryRepository;

    @Override
    public TransactionReportLine process(Transaction tran) {
        CardXref xref = xrefRepository.findByCardNumber(tran.getCardNumber())
                .orElseThrow();
        TransactionType type = typeRepository.findById(tran.getTypeCode())
                .orElse(new TransactionType(tran.getTypeCode(), "UNKNOWN"));
        TransactionCategory cat = categoryRepository
                .findByTypeCodeAndCategoryCode(tran.getTypeCode(), tran.getCategoryCode())
                .orElse(new TransactionCategory(tran.getTypeCode(),
                        tran.getCategoryCode(), "UNKNOWN"));

        return new TransactionReportLine(
                tran.getId(),
                xref.getAccountId(),
                type.getTypeCode(),
                type.getDescription(),
                cat.getCategoryCode(),
                cat.getDescription(),
                tran.getSource(),
                tran.getAmount()
        );
    }
}
```

#### 8.2.2 Option B: REST Endpoint for On-Demand Reports

A REST endpoint that generates reports dynamically in multiple formats:

```java
@RestController
@RequestMapping("/api/v1/reports")
public class TransactionReportController {

    @GetMapping("/daily-transactions")
    public ResponseEntity<Resource> generateReport(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "CSV") ReportFormat format) {

        // Query transactions, enrich, generate report
        byte[] report = transactionReportService
                .generateReport(startDate, endDate, format);

        String filename = String.format("daily_transaction_report_%s_%s.%s",
                startDate, endDate, format.getExtension());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(format.getMediaType())
                .body(new ByteArrayResource(report));
    }
}
```

Supported formats:
- **CSV** -- closest to original fixed-width format, easy to import into Excel
- **PDF** -- using JasperReports or Apache PDFBox for formatted output
- **HTML** -- browser-viewable version with pagination

#### 8.2.3 Report Totals Logic

The COBOL break logic (page totals, account totals, grand total) is preserved in the report service:

```java
public class TransactionReportService {

    public ReportData buildReportData(List<TransactionReportLine> lines,
                                       int pageSize) {
        ReportData report = new ReportData();
        BigDecimal pageTotal = BigDecimal.ZERO;
        BigDecimal accountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;
        String currentCardNumber = null;
        int lineCount = 0;

        for (TransactionReportLine line : lines) {
            // Account break (when card number changes)
            if (currentCardNumber != null
                    && !currentCardNumber.equals(line.getCardNumber())) {
                report.addAccountTotal(currentCardNumber, accountTotal);
                accountTotal = BigDecimal.ZERO;
            }
            currentCardNumber = line.getCardNumber();

            // Page break every pageSize lines
            if (lineCount > 0 && lineCount % pageSize == 0) {
                report.addPageTotal(pageTotal);
                grandTotal = grandTotal.add(pageTotal);
                pageTotal = BigDecimal.ZERO;
            }

            report.addDetailLine(line);
            pageTotal = pageTotal.add(line.getAmount());
            accountTotal = accountTotal.add(line.getAmount());
            lineCount++;
        }

        // Final totals
        report.addAccountTotal(currentCardNumber, accountTotal);
        report.addPageTotal(pageTotal);
        grandTotal = grandTotal.add(pageTotal);
        report.setGrandTotal(grandTotal);

        return report;
    }
}
```

---

## 9. Suggested Spring Boot Project Structure

### 9.1 Package Layout

```
com.carddemo.transaction/
|
+-- CardDemoTransactionApplication.java          # @SpringBootApplication entry point
|
+-- config/
|   +-- BatchConfiguration.java                  # Spring Batch infrastructure beans
|   +-- DataSourceConfiguration.java             # DataSource and JPA configuration
|   +-- SchedulingConfiguration.java             # @EnableScheduling, cron definitions
|
+-- domain/
|   +-- entity/
|   |   +-- Account.java                         # @Entity (from CVACT01Y)
|   |   +-- Card.java                            # @Entity (from CVACT02Y)
|   |   +-- CardXref.java                        # @Entity (from CVACT03Y)
|   |   +-- Customer.java                        # @Entity (from CVCUS01Y)
|   |   +-- DailyTransaction.java                # @Entity (from CVTRA06Y)
|   |   +-- Transaction.java                     # @Entity (from CVTRA05Y)
|   |   +-- TransactionCategoryBalance.java      # @Entity (from CVTRA01Y)
|   |   +-- TransactionType.java                 # @Entity (from CVTRA03Y)
|   |   +-- TransactionCategory.java             # @Entity (from CVTRA04Y)
|   |   +-- RejectedTransaction.java             # @Entity (new -- replaces DALYREJS)
|   |
|   +-- enums/
|   |   +-- RejectReason.java                    # Enum mapping COBOL reject codes
|   |
|   +-- dto/
|       +-- TransactionPostingResult.java        # Result wrapper (accepted or rejected)
|       +-- TransactionReportLine.java           # Report detail line DTO
|       +-- RejectedTransactionDto.java          # API response DTO
|       +-- RejectSummaryDto.java                # Summary statistics DTO
|       +-- ReportData.java                      # Full report data model
|
+-- repository/
|   +-- AccountRepository.java                   # extends JpaRepository<Account, Long>
|   +-- CardRepository.java                      # extends JpaRepository<Card, String>
|   +-- CardXrefRepository.java                  # extends JpaRepository<CardXref, String>
|   +-- CustomerRepository.java                  # extends JpaRepository<Customer, Long>
|   +-- DailyTransactionRepository.java          # extends JpaRepository + custom queries
|   +-- TransactionRepository.java               # extends JpaRepository<Transaction, String>
|   +-- TransactionCategoryBalanceRepository.java
|   +-- TransactionTypeRepository.java
|   +-- TransactionCategoryRepository.java
|   +-- RejectedTransactionRepository.java
|
+-- service/
|   +-- AccountService.java                      # Account balance update logic
|   +-- TransactionCategoryBalanceService.java   # TCATBAL upsert logic
|   +-- TransactionReportService.java            # Report generation logic
|
+-- batch/
|   +-- posting/
|   |   +-- TransactionPostingJobConfig.java     # Job/Step bean definitions
|   |   +-- TransactionPostingJobListener.java   # Job-level listener (counts, exit status)
|   |   +-- DailyTransactionReader.java          # ItemReader (if custom beyond repository)
|   |   +-- TransactionValidationProcessor.java  # ItemProcessor (validate + classify)
|   |   +-- TransactionPostingWriter.java        # ItemWriter for accepted transactions
|   |   +-- RejectRecordWriter.java              # ItemWriter for rejected transactions
|   |
|   +-- ingestion/
|   |   +-- DailyTransactionIngestionJobConfig.java  # CBTRN01C equivalent
|   |
|   +-- report/
|       +-- TransactionReportJobConfig.java      # CBTRN03C equivalent
|       +-- TransactionReportEnricher.java       # ItemProcessor for enrichment
|       +-- TransactionReportWriter.java         # ItemWriter for report output
|
+-- validation/
|   +-- TransactionValidator.java                # Interface for pluggable validators
|   +-- CardXrefValidator.java                   # Validates card exists in XREF
|   +-- AccountExistsValidator.java              # Validates account exists
|   +-- CreditLimitValidator.java                # Validates credit limit not exceeded
|   +-- AccountExpirationValidator.java          # Validates account not expired
|
+-- controller/
|   +-- BatchJobController.java                  # REST endpoints to trigger batch jobs
|   +-- RejectedTransactionController.java       # REST endpoints for reject inquiry
|   +-- TransactionReportController.java         # REST endpoints for report generation
|
+-- exception/
    +-- GlobalExceptionHandler.java              # @ControllerAdvice for error handling
    +-- TransactionProcessingException.java      # Custom exception
    +-- BatchJobException.java                   # Custom exception for batch failures
```

### 9.2 Key Dependencies (Gradle)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.x'
    id 'io.spring.dependency-management' version '1.1.x'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Core Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Spring Batch
    implementation 'org.springframework.boot:spring-boot-starter-batch'

    // Database
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'          // Schema migration

    // Reporting (optional, for PDF generation)
    implementation 'com.opencsv:opencsv:5.9'            // CSV report generation
    implementation 'com.itextpdf:itext7-core:8.0.x'     // PDF report generation (optional)

    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.x'

    // Messaging (optional, for dead-letter queue)
    // implementation 'org.springframework.kafka:spring-kafka'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.batch:spring-batch-test'
    testImplementation 'com.h2database:h2'              // In-memory DB for tests
}
```

### 9.3 Application Configuration

```yaml
# application.yml
spring:
  application:
    name: carddemo-transaction-service

  datasource:
    url: jdbc:postgresql://localhost:5432/carddemo
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20

  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema management
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # Don't auto-run jobs on startup

  flyway:
    enabled: true
    locations: classpath:db/migration

# Batch job scheduling
carddemo:
  batch:
    posting:
      cron: "0 0 2 * * ?"      # Daily at 2:00 AM (replaces JCL scheduler)
    report:
      cron: "0 0 6 * * ?"      # Daily at 6:00 AM
    reject-retention-days: 5    # Replaces GDG LIMIT(5)

# Actuator endpoints for monitoring
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, batch
```

### 9.4 Flyway Migration Scripts

```sql
-- V1__create_core_tables.sql

CREATE TABLE account (
    id              BIGINT PRIMARY KEY,
    active_status   CHAR(1),
    current_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    credit_limit    DECIMAL(12,2) NOT NULL DEFAULT 0,
    cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0,
    open_date       DATE,
    expiration_date DATE,
    reissue_date    DATE,
    current_cycle_credit DECIMAL(12,2) NOT NULL DEFAULT 0,
    current_cycle_debit  DECIMAL(12,2) NOT NULL DEFAULT 0,
    address_zip     VARCHAR(10),
    group_id        VARCHAR(10)
);

CREATE TABLE customer (
    id              BIGINT PRIMARY KEY,
    first_name      VARCHAR(25),
    middle_name     VARCHAR(25),
    last_name       VARCHAR(25),
    address_line_1  VARCHAR(50),
    address_line_2  VARCHAR(50),
    address_line_3  VARCHAR(50),
    state_code      CHAR(2),
    country_code    CHAR(3),
    address_zip     VARCHAR(10),
    phone_number_1  VARCHAR(15),
    phone_number_2  VARCHAR(15),
    ssn             BIGINT,
    govt_issued_id  VARCHAR(20),
    date_of_birth   DATE,
    eft_account_id  VARCHAR(10),
    primary_cardholder_indicator CHAR(1),
    fico_credit_score INTEGER
);

CREATE TABLE card (
    card_number     VARCHAR(16) PRIMARY KEY,
    account_id      BIGINT REFERENCES account(id),
    cvv_code        INTEGER,
    embossed_name   VARCHAR(50),
    expiration_date DATE,
    active_status   CHAR(1)
);

CREATE TABLE card_xref (
    card_number     VARCHAR(16) PRIMARY KEY,
    customer_id     BIGINT REFERENCES customer(id),
    account_id      BIGINT REFERENCES account(id)
);

CREATE TABLE transaction_type (
    type_code       VARCHAR(2) PRIMARY KEY,
    description     VARCHAR(50)
);

CREATE TABLE transaction_category (
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    description     VARCHAR(50),
    PRIMARY KEY (type_code, category_code)
);

CREATE TABLE daily_transaction (
    id              VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2),
    category_code   INTEGER,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2),
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16),
    original_timestamp  TIMESTAMP,
    processed_timestamp TIMESTAMP,
    processing_status   VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE transaction (
    id              VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2),
    category_code   INTEGER,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2),
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16),
    original_timestamp  TIMESTAMP,
    processed_timestamp TIMESTAMP
);

CREATE TABLE transaction_category_balance (
    account_id      BIGINT NOT NULL,
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    balance         DECIMAL(11,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (account_id, type_code, category_code)
);

CREATE TABLE rejected_transaction (
    id                      BIGSERIAL PRIMARY KEY,
    daily_transaction_id    VARCHAR(16),
    transaction_data_json   TEXT,
    reject_reason_code      INTEGER NOT NULL,
    reject_reason_description VARCHAR(76),
    rejected_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    batch_job_execution_id  BIGINT
);

-- Indexes for common access patterns
CREATE INDEX idx_daily_tran_status ON daily_transaction(processing_status);
CREATE INDEX idx_daily_tran_card ON daily_transaction(card_number);
CREATE INDEX idx_transaction_card ON transaction(card_number);
CREATE INDEX idx_transaction_proc_ts ON transaction(processed_timestamp);
CREATE INDEX idx_rejected_tran_date ON rejected_transaction(rejected_at);
CREATE INDEX idx_rejected_tran_reason ON rejected_transaction(reject_reason_code);
CREATE INDEX idx_card_xref_account ON card_xref(account_id);
```

---

## 10. Migration Phases and Timeline

### Phase 1: Foundation (Weeks 1-2)

| Task | Details |
|------|---------|
| Project scaffolding | Create Spring Boot project with Gradle, configure dependencies |
| Database schema | Create Flyway migrations for all tables |
| JPA entities | Implement all `@Entity` classes with proper mappings |
| Repositories | Create Spring Data JPA repository interfaces |
| Unit tests | Write entity mapping tests and repository integration tests |

### Phase 2: Core Batch Logic (Weeks 3-5)

| Task | Details |
|------|---------|
| Transaction Posting Job | Implement `TransactionPostingJob` (CBTRN02C equivalent) |
| Validation chain | Implement all `TransactionValidator` implementations |
| Account update service | Implement balance update logic (`2800-UPDATE-ACCOUNT-REC`) |
| Category balance service | Implement TCATBAL upsert logic (`2700-UPDATE-TCATBAL`) |
| Reject handling | Implement `RejectedTransaction` persistence and reject writer |
| Integration tests | End-to-end test of the posting job with test data |

### Phase 3: Ingestion and Reporting (Weeks 5-7)

| Task | Details |
|------|---------|
| Ingestion Job | Implement `DailyTransactionIngestionJob` (CBTRN01C equivalent) |
| Report generation | Implement `TransactionReportJob` (CBTRN03C equivalent) |
| Report formats | CSV output (primary), PDF (optional) |
| REST endpoints | Batch trigger endpoints, reject inquiry, report download |
| Scheduling | Configure `@Scheduled` cron triggers |

### Phase 4: Data Migration and Testing (Weeks 7-9)

| Task | Details |
|------|---------|
| Data extraction | Extract VSAM data to sequential files |
| Data transformation | EBCDIC-to-UTF8 conversion, field mapping |
| Data loading | Bulk load into PostgreSQL |
| Parallel run | Run COBOL and Spring Boot side-by-side, compare outputs |
| Reconciliation | Compare posted transaction counts, balances, reject counts |

### Phase 5: Production Cutover (Weeks 9-10)

| Task | Details |
|------|---------|
| Performance testing | Load test with production-volume data |
| Monitoring setup | Configure Spring Boot Actuator, logging, alerting |
| Documentation | API documentation (OpenAPI/Swagger), runbook |
| Cutover | Switch batch scheduling from JCL to Spring Batch |
| Hypercare | Monitor first 5 business days of production operation |

---

## 11. Risk Assessment and Mitigations

| Risk | Severity | Mitigation |
|------|---------|------------|
| **Numeric precision loss** during COBOL `PIC S9(09)V99` to Java conversion | High | Use `BigDecimal` exclusively for all monetary fields; never use `double` or `float`. Validate with reconciliation tests comparing COBOL output to Java output for identical input. |
| **Transaction ordering differences** between sequential VSAM reads and database queries | Medium | Ensure `ORDER BY` clauses match COBOL sequential access patterns. For posting, process in `id` order to match COBOL behavior. |
| **Concurrent access conflicts** on account records during batch posting | Medium | Use `@Version` for optimistic locking on `Account` entity, or process within a Spring Batch partition scoped by account range. COBOL sequential processing was inherently single-threaded. |
| **GDG generation semantics** not directly available in relational databases | Low | Replace with timestamp-based queries and a configurable retention policy (`carddemo.batch.reject-retention-days`). Implement a scheduled cleanup job. |
| **COBOL ABEND behavior** (program termination on I/O errors) differs from Java exception handling | Medium | Configure Spring Batch `skipLimit(0)` for critical steps. Implement a `SkipListener` that logs and alerts on unexpected errors. Map COBOL file status codes to specific Java exceptions. |
| **Date/timestamp format differences** between COBOL and Java | Low | COBOL uses `PIC X(26)` for DB2-format timestamps (`YYYY-MM-DD-HH.MM.SS.HH0000`). Map to `java.time.LocalDateTime` using a custom formatter: `DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000")`. |
| **EBCDIC-to-ASCII data encoding issues** during data migration | Medium | Use IBM's `jt400` library or a dedicated EBCDIC conversion tool. Validate character data after conversion, especially for special characters and packed-decimal fields. |
| **Missing validation rules** that exist in production COBOL but not in the source code | Medium | The COBOL source includes `* ADD MORE VALIDATIONS HERE` indicating potential undocumented rules. Conduct UAT with production data to identify discrepancies. |

---

## Appendix A: COBOL-to-Java Data Type Mapping

| COBOL Picture | COBOL Type | Java Type | JPA Column Type | Notes |
|--------------|-----------|-----------|-----------------|-------|
| `PIC X(n)` | Alphanumeric | `String` | `VARCHAR(n)` | Trim trailing spaces |
| `PIC 9(n)` | Unsigned numeric | `Long` or `Integer` | `BIGINT` / `INTEGER` | Use `Long` if n > 9 |
| `PIC S9(n)V99` | Signed decimal | `BigDecimal` | `DECIMAL(n+2, 2)` | Always use `BigDecimal` for money |
| `PIC S9(n) COMP` | Binary integer | `int` or `long` | `INTEGER` / `BIGINT` | |
| `PIC 9(n) COMP-3` | Packed decimal | `BigDecimal` | `DECIMAL` | |
| `PIC X(10)` (date) | Date as string | `LocalDate` | `DATE` | Parse with `DateTimeFormatter` |
| `PIC X(26)` (timestamp) | Timestamp as string | `LocalDateTime` | `TIMESTAMP` | Custom DB2 format parser |

---

*Document prepared for the CardDemo Mainframe Modernization initiative.*
*Source repository: `ankehao-demo/aws-mainframe-modernization-carddemo`*
*Target technology: Spring Boot 3.2.x with Spring Batch, Spring Data JPA, PostgreSQL*
