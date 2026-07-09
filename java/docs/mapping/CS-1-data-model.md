# CS-1 — Data model (JPA entities, schema, seed loader)

WAVE 1 of the CardDemo COBOL→Java migration. This document maps every **data** copybook in
[`../../../app/cpy/`](../../../app/cpy/) to a JPA entity and a relational table, records the
field-by-field type mapping (COBOL `PIC` → Java type → SQL column), and documents the keys,
relationships and the seed-data loader that replaces the legacy IDCAMS load JCLs.

All conventions follow [`java/README.md`](../../README.md#cobol-java-mapping):
fixed-width, zero-padded numeric **identifiers** (account/card/customer/merchant ids, SSN, CVV)
→ `String`; `PIC 9(n)` used numerically → `Integer`/`Long`; monetary/interest
(`PIC S9(n)Vnn`) → `java.math.BigDecimal` with the copybook's implied scale.

## Artifacts

| Layer | Location |
|-------|----------|
| Entities | `carddemo-domain/src/main/java/com/carddemo/domain/` |
| Repositories | `carddemo-domain/src/main/java/com/carddemo/repository/` |
| Schema (Flyway) | `carddemo-domain/src/main/resources/db/migration/V2__data_model.sql` |
| Seed files (classpath copy of `app/data/ASCII/`) | `carddemo-domain/src/main/resources/seed/` |
| Seed loader (Spring Batch) | `carddemo-app/src/main/java/com/carddemo/batch/` |

## Copybook → entity → table summary

| Copybook | RECLN | Entity | Table | Primary key | Seed file |
|----------|-------|--------|-------|-------------|-----------|
| CVACT01Y | 300 | `Account` | `account` | `acct_id` | acctdata.txt |
| CVACT02Y | 150 | `Card` | `card` | `card_num` | carddata.txt |
| CVACT03Y | 50 | `CardXref` | `card_xref` | `xref_card_num` | cardxref.txt |
| CVCUS01Y | 500 | `Customer` | `customer` | `cust_id` | custdata.txt |
| CVTRA05Y | 350 | `Transaction` | `card_transaction` | `tran_id` | *(none — runtime)* |
| CVTRA06Y | 350 | `DailyTransaction` | `daily_transaction` | `dalytran_id` | dailytran.txt |
| CVTRA01Y | 50 | `TransactionCategoryBalance` | `tran_cat_balance` | (acct_id, type_cd, cat_cd) | tcatbal.txt |
| CVTRA02Y | 50 | `DisclosureGroup` | `disclosure_group` | (group_id, type_cd, cat_cd) | discgrp.txt |
| CVTRA03Y | 60 | `TransactionType` | `transaction_type` | `tran_type` | trantype.txt |
| CVTRA04Y | 60 | `TransactionCategory` | `transaction_category` | (type_cd, cat_cd) | trancatg.txt |
| CSUSR01Y | 80 | `SecurityUser` | `sec_user` | `sec_usr_id` | usrsec.txt (from DUSRSECJ.jcl) |

> The table for `CVTRA05Y` is named `card_transaction` (not `transaction`) because
> `TRANSACTION` is a SQL keyword in PostgreSQL/H2.

### Not persisted (documented, no table)

| Copybook | Reason |
|----------|--------|
| CVCRD01Y | `CC-WORK-AREAS` — a CICS COMMAREA/working-storage layout (AID keys, next program/mapset, error/return messages, and account/card/customer work fields with `REDEFINES`). It is transient screen/session state, not a persistent VSAM record, so it is **not** mapped to a table. It will be revisited by the online (CICS→web) wave as a session/DTO structure. |
| CVTRA07Y | Report layout group (`REPORT-NAME-HEADER`, `TRANSACTION-DETAIL-REPORT`, headers, page/account/grand totals with edited `PIC -ZZZ,ZZZ,ZZZ.ZZ` masks). It is a print-formatting structure for the daily transaction report, not a stored record, so it is **skipped** here; report formatting belongs to the reporting wave. |

The remaining copybooks in `app/cpy/` (e.g. `COADM02Y`, `COMEN02Y`, `CSMSG0*Y`, `CSDAT01Y`,
`CSUTLD*Y`, `COTTL01Y`, `CSSTRPFY`, `CUSTREC`, `UNUSED1Y`, BMS symbolic maps, literals and
utility copybooks) are **not data records** and are out of scope for CS-1.

## Keys & relationships

Primary keys were taken from the `RECORD KEY` clauses of the `SELECT` statements in the batch
programs:

- `CBACT01C` — `ACCTFILE` `RECORD KEY IS FD-ACCT-ID` → `account.acct_id`
- `CBACT02C` — `CARDFILE` `RECORD KEY IS FD-CARD-NUM` → `card.card_num`
- `CBACT03C` — `XREFFILE` `RECORD KEY IS FD-XREF-CARD-NUM` → `card_xref.xref_card_num`
- `CBCUS01C` — `CUSTFILE` `RECORD KEY IS FD-CUST-ID` → `customer.cust_id`
- `CBTRN02C` — `TRANFILE` `RECORD KEY IS FD-TRANS-ID` → `card_transaction.tran_id`;
  `TCATBALF` `RECORD KEY IS FD-TRAN-CAT-KEY` → composite `tran_cat_balance` key.
- `DUSRSECJ.jcl` — `DEFINE CLUSTER … KEYS(8,0)` → `sec_user.sec_usr_id` (first 8 bytes).

Composite keys mirror the group-level `…-KEY` items in the copybooks and are modelled with
JPA `@EmbeddedId` classes (`TransactionCategoryBalanceId`, `DisclosureGroupId`,
`TransactionCategoryId`).

Foreign keys (enforced in `V2__data_model.sql`) capture the customer↔account↔card chain that
the cross-reference (`CVACT03Y`, built by `XREFFILE`/read by `CBTRN02C`) makes explicit:

- `card.card_acct_id → account.acct_id`
- `card_xref.xref_card_num → card.card_num`
- `card_xref.xref_acct_id → account.acct_id`
- `card_xref.xref_cust_id → customer.cust_id`

**Assumptions / decisions**

- The transaction tables (`card_transaction`, `daily_transaction`) keep `tran_card_num`,
  `tran_type_cd`/`tran_cat_cd` as plain columns **without** DB foreign keys. VSAM enforced no
  referential integrity, daily input can legitimately reference not-yet-posted or rejected
  keys (`CBTRN02C` writes rejects to `DALYREJS`), and constraining them would make the seed
  load order-dependent and brittle. The relationships are exposed through repository finders
  (`findByTranCardNum`, `findByCardAcctId`, `findByXrefAcctId`, `findByXrefCustId`) instead.
- The reference tables (`transaction_type`, `transaction_category`, `disclosure_group`) are
  not FK targets of the transaction tables for the same reason.
- `TRAN-CAT-CD` (`PIC 9(04)`) is modelled as `Integer` (a small numeric category code); all
  other zero-padded numeric identifiers stay `String`.
- The online-transaction seed file does not exist (online transactions are produced at
  runtime by posting), so `card_transaction` is created empty in CS-1.

## Seed data loader

`dataLoadJob` (Spring Batch, `com.carddemo.batch.DataLoadJobConfig`) replaces the IDCAMS load
JCLs (`ACCTFILE`/`CARDFILE`/`CUSTFILE`/`XREFFILE`/`DISCGRP`/`TCATBALF`/`TRANCATG`/`TRANTYPE`/
`DUSRSECJ`) and the `DALYTRAN` input. One chunk-oriented step per file:
`FlatFileItemReader` (raw line) → `ItemProcessor` (fixed-width field mapping per copybook,
`SeedFieldParser`) → `RepositoryItemWriter` (`save`, i.e. upsert). Steps are ordered
customer → account → card → card_xref → reference tables → tcatbal → dailytran → sec_user so
that FK targets exist first.

**Running it.** Disabled by default (`spring.batch.job.enabled=false`). `SeedDataLoadRunner`
launches it at startup when `carddemo.seed.enabled=true` and skips if accounts already exist
(idempotent):

```bash
cd java
mvn -pl carddemo-app spring-boot:run -Dspring-boot.run.arguments=--carddemo.seed.enabled=true
```

**Numeric encoding.** The ASCII files store `PIC S9(n)V99` money as zoned decimal with a
**trailing sign over-punch** on the last byte (`{`=+0…`I`=+9, `}`=-0…`R`=-9).
`SeedFieldParser.signedDecimal` decodes it to `BigDecimal` at the copybook scale — e.g.
account `00000000001` current balance `00000001940{` → `194.00`.

### Field-level notes on encoding

| PIC clause | Java | SQL | Notes |
|------------|------|-----|-------|
| `PIC 9(11)` id (acct/card-acct/xref-acct) | `String` | `VARCHAR(11)` | zero-padded id, no arithmetic |
| `PIC 9(09)` id (cust/xref-cust/ssn/merchant) | `String` | `VARCHAR(9)` | zero-padded id |
| `PIC X(16)` (card/tran id) | `String` | `VARCHAR(16)` | |
| `PIC 9(03)` (cvv) | `String` | `VARCHAR(3)` | leading zeros preserved |
| `PIC 9(03)` (fico score) | `Integer` | `INTEGER` | numeric value |
| `PIC 9(04)` (cat cd) | `Integer` | `INTEGER` | numeric code |
| `PIC S9(10)V99` (account money) | `BigDecimal` scale 2 | `NUMERIC(12,2)` | over-punch decode |
| `PIC S9(09)V99` (tran amt / cat bal) | `BigDecimal` scale 2 | `NUMERIC(11,2)` | over-punch decode |
| `PIC S9(04)V99` (interest rate) | `BigDecimal` scale 2 | `NUMERIC(6,2)` | over-punch decode |
| `PIC X(n)` | `String` | `VARCHAR(n)` | trailing spaces trimmed |

## Validation

`cd java && mvn -B verify` is green:

- `@DataJpaTest` repository tests (`AccountRepositoryTest`, `TransactionCategoryRepositoryTest`)
  cover a single-column PK entity (with monetary scale) and a composite-key entity.
- `SeedDataLoadJobTest` runs `dataLoadJob` against H2, asserts each table's row count equals
  the number of records in the corresponding seed file, and checks a sample monetary field
  (`account 00000000001` → `194.00`, credit limit `2020.00`).
- Flyway `V1`/`V2` apply cleanly on H2 (PostgreSQL mode) during the app context tests.
