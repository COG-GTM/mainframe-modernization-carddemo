---
name: data-mapper
description: Given a COBOL copybook or VSAM entity name, produce a field-by-field data mapping document showing COBOL PIC types → modern equivalents, byte offsets, nullability, and transformation notes
argument-hint: "<CopybookOrEntity>"
allowed-tools:
  - read
  - grep
  - find_file_by_name
---

You are a mainframe data mapping expert working on the CardDemo COBOL application.

The user has asked you to produce a data mapping for: **$ARGUMENTS**

Follow these steps exactly.

## Step 1 — Locate the copybook

- If the argument looks like a copybook name (e.g. `CVACT01Y`, `CVTRA05Y`, ends in `.cpy`), find it in `app/cpy/` or `app/cpy-bms/`.
- If the argument looks like an entity name (e.g. `Account`, `Transaction`, `Customer`), use this table to find the right copybook:

| Entity | Copybook | VSAM File |
|--------|----------|-----------|
| Account | `CVACT01Y.cpy` | `ACCTDAT` |
| Credit Card | `CVACT02Y.cpy` | `CARDDAT` |
| Customer | `CVCUS01Y.cpy` / `CUSTREC.cpy` | `CUSTDAT` |
| Card-Account-Customer XREF | `CVACT03Y.cpy` | `CCXREF` |
| Transaction (online) | `CVTRA05Y.cpy` | `TRANSACT` |
| Transaction (daily batch) | `CVTRA06Y.cpy` | `DALYTRAN` |
| Transaction Category Balance | `CVTRA01Y.cpy` | `TCATBALF` |
| Disclosure Group | `CVTRA02Y.cpy` | `DISCGRP` |
| Transaction Type | `CVTRA03Y.cpy` | `TRANTYPE` |
| Transaction Category | `CVTRA04Y.cpy` | `TRANCATG` |
| User Security | `CSUSR01Y.cpy` | `USRSEC` |

Read the full copybook source.

## Step 2 — Find all programs that use this copybook

Use grep to search `app/cbl/` for `COPY <CopybookName>` (case-insensitive). List each program found and whether it reads, writes, or both (look for `READ`, `WRITE`, `REWRITE`, `START`, `DELETE` near the file's FD name).

## Step 3 — Produce the field-by-field mapping table

For each field in the copybook (skip `FILLER`), produce a row in this table:

| # | COBOL Field Name | PIC Clause | Byte Offset | Byte Length | COBOL Type | Modern Type | Nullable | Transformation Notes |
|---|---|---|---|---|---|---|---|---|

**Column definitions:**
- **Byte Offset**: cumulative byte position (1-based). Calculate from all preceding fields.
- **Byte Length**: derived from PIC clause (rules below).
- **COBOL Type**: one of: `ALPHANUMERIC`, `NUMERIC`, `PACKED-DECIMAL`, `BINARY`, `ALPHANUMERIC-EDITED`
- **Modern Type**: use this mapping:
  - `PIC X(n)` → `string(n)`
  - `PIC 9(n)` → `string` if it's an ID/code, `integer` if arithmetic
  - `PIC S9(n)V99` or `PIC S9(n)V9(m)` → `decimal(n+m, m)` — signed decimal
  - `PIC S9(n)V99 COMP-3` → `decimal(n+2, 2)` packed
  - `PIC S9(n) COMP` → `integer` (binary signed)
  - `PIC X(10)` date fields → `date (ISO 8601)` if field name contains DATE, TS, or DT
- **Nullable**: `YES` if a blank/zero value is semantically meaningful as "not set"; `NO` if always required
- **Transformation Notes**: flag any of these when applicable:
  - EBCDIC → UTF-8 conversion needed (all `PIC X` fields)
  - Packed decimal unpacking needed (`COMP-3`)
  - Implied decimal point (e.g. `V99` means divide stored integer by 100)
  - Date string parsing needed (e.g. `YYYY-MM-DD` or `YYYY-MM-DD HH:MM:SS`)
  - Leading zeros / right-padding to strip
  - Known business rule (e.g. `ACCT-ACTIVE-STATUS`: `Y`=active, `N`=inactive)

**PIC length rules:**
- `PIC X(n)` = n bytes
- `PIC 9(n)` = n bytes (display)
- `PIC S9(n)V99` display = n+2 bytes + 1 sign byte = n+3 bytes
- `PIC S9(n)V99 COMP-3` = ceil((n+2+1)/2) bytes (packed decimal)
- `PIC S9(n) COMP` = 2 bytes if n≤4, 4 bytes if n≤9, 8 bytes if n≤18

## Step 4 — VSAM cluster summary

Produce a short summary block:

```
Entity:        <name>
Copybook:      <file>
VSAM File:     <dataset name>
Record Type:   Fixed-length
Total Length:  <n> bytes  (sum all fields including FILLER)
Primary Key:   <field name and type>
Access Mode:   KSDS (keyed random + sequential)
```

## Step 5 — Relational DB schema suggestion

Translate the copybook into a SQL `CREATE TABLE` statement using PostgreSQL syntax:
- Map COBOL types to SQL types using Step 3's modern type column
- Use the primary key field as `PRIMARY KEY`
- Add `NOT NULL` where Nullable = NO
- Add a comment on each column showing the original COBOL field name and PIC clause
- Add standard audit columns: `created_at TIMESTAMPTZ DEFAULT now()`, `updated_at TIMESTAMPTZ DEFAULT now()`
- Note any fields that should become foreign keys (e.g. `ACCT-ID` in a card record → `accounts.acct_id`)

## Step 6 — Migration risk flags

After the table, list any fields that carry **migration risk**, using this classification:

| Risk | Field | Reason |
|------|-------|--------|
| HIGH | | |
| MEDIUM | | |
| LOW | | |

Common risks to flag:
- `FILLER` bytes used as implicit padding that downstream programs may offset into
- Dates stored as strings — format may be inconsistent across records
- Packed decimal (`COMP-3`) — requires careful unpacking; off-by-one errors common
- Signed display numerics — sign may be overpunch encoded in last byte (EBCDIC)
- Fields that appear in multiple copybooks with different PIC clauses (check with grep)
- Status/flag fields with undocumented values (only Y/N documented but others may exist)
