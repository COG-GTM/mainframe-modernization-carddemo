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

Keep the top-level table in [`../../README.md`](../../README.md#coboljava-mapping) as the
canonical summary of the general rules; use this directory for the exhaustive detail.

## Index

- [`CS-1-data-model.md`](CS-1-data-model.md) — WAVE 1: relational data model (JPA entities,
  Flyway schema, seed loader) for the data copybooks (accounts, cards, customers, xref,
  transactions, reference/lookup tables, security users).
- [`CS-3-session-navigation.md`](CS-3-session-navigation.md) — WAVE 2: session/navigation
  framework mapping the `COCOM01Y` COMMAREA and the CICS pseudo-conversational flow to a
  session-backed REST navigation framework (`com.carddemo.session`).
- [`CS-9-user-admin.md`](CS-9-user-admin.md) — WAVE 3 (online): admin user maintenance
  (`COUSR00C`/`COUSR01C`/`COUSR02C`/`COUSR03C`, `CSUSR01Y`) as a `ROLE_ADMIN` REST CRUD over
  the USRSEC store (`com.carddemo.web.useradmin` / `com.carddemo.service.useradmin`).
