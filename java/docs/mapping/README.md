# COBOL → Java mapping catalogue

This directory collects the detailed, per-scope mapping documentation produced by each
migration wave. WAVE 0 (scaffolding) establishes the conventions; later sessions add one
document per scope they migrate.

## How to contribute (later waves)

When you migrate a scope (a set of COBOL programs / copybooks / JCL), add a Markdown file
here named after the scope, e.g. `accounts.md`, `cards.md`, `transactions.md`,
`interest-calc.md`. Each file should document, for every copybook / record you touched:

- The source artifact(s): copybook name (e.g. `CVACT01Y.cpy`), COBOL program(s), JCL job(s).
- A field-by-field table: COBOL field name, PIC clause / usage, field length, and the chosen
  Java type + JPA/DTO field, following the type-mapping rules in [`../../README.md`](../../README.md#coboljava-mapping).
- Any behavioural notes (rounding rules, sign handling, EBCDIC/packed-decimal quirks, key
  structure of the VSAM cluster / alternate indexes).

The top-level [`../../README.md`](../../README.md) holds the canonical summary of the general
mapping rules **and** the complete program/copybook/BMS/JCL → Java table; the documents below are
the exhaustive per-scope detail.

## Index

### WAVE 1 — data model
- [`CS-1-data-model.md`](CS-1-data-model.md) — relational data model (JPA entities, Flyway
  schema, seed loader) for the data copybooks (accounts, cards, customers, xref, transactions,
  reference/lookup tables, security users).

### WAVE 2 — security & session
- [`CS-2-security.md`](CS-2-security.md) — Spring Security roles + `COSGN00C` sign-on/off
  (`com.carddemo.security`).
- [`CS-3-session-navigation.md`](CS-3-session-navigation.md) — session/navigation framework
  mapping the `COCOM01Y` COMMAREA and the CICS pseudo-conversational flow to a session-backed
  REST navigation framework (`com.carddemo.session`).

### WAVE 3 — online functions
- [`CS-4-accounts.md`](CS-4-accounts.md) — accounts online (`COACTVWC` / `COACTUPC`).
- [`CS-5-cards.md`](CS-5-cards.md) — cards online (`COCRDLIC` / `COCRDSLC` / `COCRDUPC`).
- [`CS-6-transactions-online.md`](CS-6-transactions-online.md) — online transactions
  (`COTRN00C` / `COTRN01C` / `COTRN02C`).
- [`CS-7-report-billpay.md`](CS-7-report-billpay.md) — transaction report & bill pay
  (`CORPT00C` / `COBIL00C`).
- [`CS-8-menus.md`](CS-8-menus.md) — main & admin menus (`COMEN01C` / `COADM01C`).
- [`CS-9-user-admin.md`](CS-9-user-admin.md) — admin user maintenance
  (`COUSR00C`/`01C`/`02C`/`03C`, `CSUSR01Y`) as a `ROLE_ADMIN` REST CRUD over the USRSEC store.

### WAVE 3 — batch & utilities
- [`CS-10-batch-account-interest.md`](CS-10-batch-account-interest.md) — batch account/customer
  print + interest calculation (`CBACT01–04C`, `CBCUS01C` / INTCALC).
- [`CS-11-batch-posting.md`](CS-11-batch-posting.md) — batch transaction posting
  (`CBTRN01-03C` / POSTTRAN).
- [`CS-12-batch-statements.md`](CS-12-batch-statements.md) — batch statements
  (`CBSTM03A` / `CBSTM03B` / `COSTM01` / CREASTMT).
- [`CS-13-util-date.md`](CS-13-util-date.md) — date-validation utility mapping `CSUTLDTC`
  (CEEDAYS wrapper) and the `CSUTLDPY`/`CSUTLDWY` copybooks to `com.carddemo.util.DateValidator`.

### WAVE 4 — orchestration, integration & validation
- [`CS-14-batch-orchestration.md`](CS-14-batch-orchestration.md) — JCL → Spring Batch pipelines
  (`posttran`/`intcalc`/`creastmt`/`tranrept`/`prtcatbl`) + REST/CLI/scheduler launchers.
- [`CS-15-integration-validation.md`](CS-15-integration-validation.md) — end-to-end integration
  tests (online + batch golden paths), numeric validation against COBOL-derived `BigDecimal`
  constants (posting, interest, statements), and the final authoritative `README`.
