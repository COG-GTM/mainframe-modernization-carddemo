# Program Inventory

This document catalogs all **29 COBOL programs** in [`app/cbl/`](../app/cbl/) of the CardDemo
application. For each program it records the type (batch vs. CICS online), function, copybooks
used (`COPY`), files accessed (`SELECT ... ASSIGN` for batch, `EXEC CICS` file name for online),
and the programs it invokes (`CALL` / `EXEC CICS XCTL` / `LINK`).

Naming convention:

- **`CB*` — Batch** programs, driven by JCL (see [batch-flows.md](batch-flows.md)).
- **`CO*` — CICS online** programs, driven by a 4-character transaction id (see [screen-maps.md](screen-maps.md)).
- **`CS*` — Common subroutines / utilities** shared by both.

## Summary

| Program  | Type            | Transaction | Function                                             |
|:---------|:----------------|:------------|:-----------------------------------------------------|
| CBACT01C | Batch           | —           | Read the account master file and print/extract it    |
| CBACT02C | Batch           | —           | Read and print the card data file                    |
| CBACT03C | Batch           | —           | Read and print the account cross-reference file      |
| CBACT04C | Batch           | —           | **Interest calculator** (transpiled in `modernized/`) |
| CBCUS01C | Batch           | —           | Read and print the customer data file                |
| CBSTM03A | Batch           | —           | Print account statements (driver, calls CBSTM03B)    |
| CBSTM03B | Batch subroutine| —           | File-handling subroutine for statement reporting     |
| CBTRN01C | Batch           | —           | Read & validate daily transactions (dry-run post)    |
| CBTRN02C | Batch           | —           | Post daily transactions to the master / balances     |
| CBTRN03C | Batch           | —           | Print the transaction detail report                  |
| COBSWAIT | Batch utility   | —           | Wait/sleep utility (PARM in centiseconds)            |
| CSUTLDTC | Subroutine      | —           | Date validation/format utility (CEEDAYS/CEEDATE)     |
| COSGN00C | CICS online     | CC00        | Signon screen                                        |
| COMEN01C | CICS online     | CM00        | Main menu (regular users)                            |
| COADM01C | CICS online     | CA00        | Admin menu                                           |
| COACTVWC | CICS online     | CAVW        | Account view                                         |
| COACTUPC | CICS online     | CAUP        | Account update                                       |
| COCRDLIC | CICS online     | CCLI        | Credit card list                                     |
| COCRDSLC | CICS online     | CCDL        | Credit card view (detail)                            |
| COCRDUPC | CICS online     | CCUP        | Credit card update                                   |
| COTRN00C | CICS online     | CT00        | Transaction list                                     |
| COTRN01C | CICS online     | CT01        | Transaction view                                     |
| COTRN02C | CICS online     | CT02        | Transaction add                                      |
| CORPT00C | CICS online     | CR00        | Transaction reports (submits batch via internal rdr) |
| COBIL00C | CICS online     | CB00        | Bill payment                                         |
| COUSR00C | CICS online     | CU00        | List users (security)                                |
| COUSR01C | CICS online     | CU01        | Add user                                             |
| COUSR02C | CICS online     | CU02        | Update user                                          |
| COUSR03C | CICS online     | CU03        | Delete user                                          |

> The optional-module programs referenced in `README.md` (COPAUS0C, COTRTUPC, CODATE01, …) are
> **not** present in `app/cbl/` and are out of scope for this inventory.

---

## Batch Programs (`CB*`)

### CBACT01C — Account file reader
- **Type:** Batch
- **Function:** Sequentially reads the account master (`ACCTDATA` KSDS) and writes it to flat /
  array / variable-blocked output files (demonstrates several record formats).
- **Copybooks:** `CVACT01Y` (ACCOUNT-RECORD), `CODATECN`
- **Files:** `ACCTFILE` (in), `OUTFILE`, `ARRYFILE`, `VBRCFILE` (out)
- **Calls:** `CEE3ABD` (LE abend), `COBDATFT` (date format)

### CBACT02C — Card file reader
- **Type:** Batch
- **Function:** Reads and prints the card master file.
- **Copybooks:** `CVACT02Y` (CARD-RECORD)
- **Files:** `CARDFILE` (in)
- **Calls:** `CEE3ABD`

### CBACT03C — Card cross-reference reader
- **Type:** Batch
- **Function:** Reads and prints the card/account cross-reference file.
- **Copybooks:** `CVACT03Y` (CARD-XREF-RECORD)
- **Files:** `XREFFILE` (in)
- **Calls:** `CEE3ABD`

### CBACT04C — Interest calculator ⭐
- **Type:** Batch
- **Function:** Computes monthly interest per account/category from transaction-category balances,
  writes interest transactions, and updates account balances. **This is the program transpiled to
  Spring Boot under [`modernized/`](../modernized/).**
- **Copybooks:** `CVTRA01Y` (TRAN-CAT-BAL-RECORD), `CVACT03Y` (CARD-XREF-RECORD),
  `CVACT01Y` (ACCOUNT-RECORD), `CVTRA02Y` (DIS-GROUP-RECORD), `CVTRA05Y` (TRAN-RECORD)
- **Files:** `TCATBALF` (in, sequential), `XREFFILE` + `XREFFIL1` AIX (in, random),
  `ACCTFILE` (in/out, random + rewrite), `DISCGRP` (in, random), `TRANSACT` (out, sequential)
- **Calls:** `CEE3ABD`
- **Invoked by JCL:** [`INTCALC.jcl`](../app/jcl/INTCALC.jcl) (`PARM='2022071800'`)

### CBCUS01C — Customer file reader
- **Type:** Batch
- **Function:** Reads and prints the customer data file.
- **Copybooks:** `CVCUS01Y` (CUSTOMER-RECORD)
- **Files:** `CUSTFILE` (in)
- **Calls:** `CEE3ABD`

### CBSTM03A — Statement print driver
- **Type:** Batch
- **Function:** Produces account statements (plain-text + HTML) by joining transaction, xref,
  customer and account data. Delegates file I/O to subroutine `CBSTM03B`.
- **Copybooks:** `COSTM01` (TRNX-RECORD), `CUSTREC` (CUSTOMER-RECORD), `CVACT01Y`, `CVACT03Y`
- **Files:** `STMTFILE`, `HTMLFILE` (out); reads via CBSTM03B: `TRNXFILE`, `XREFFILE`, `CUSTFILE`, `ACCTFILE`
- **Calls:** `CBSTM03B`, `CEE3ABD`

### CBSTM03B — Statement file subroutine
- **Type:** Batch subroutine (called by CBSTM03A)
- **Function:** Opens/reads the transaction, xref, customer and account files on behalf of the caller.
- **Copybooks:** (record areas passed via LINKAGE)
- **Files:** `TRNXFILE`, `XREFFILE`, `CUSTFILE`, `ACCTFILE` (all in)
- **Calls:** —

### CBTRN01C — Daily transaction validator
- **Type:** Batch
- **Function:** Reads the daily transaction file and validates each record against xref / card /
  account / customer master files (a read-only precursor to posting).
- **Copybooks:** `CVTRA05Y` (TRAN-RECORD), `CVTRA06Y` (DALYTRAN-RECORD), `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`
- **Files:** `DALYTRAN` (in), `CUSTFILE`, `XREFFILE`, `CARDFILE`, `ACCTFILE` (in), `TRANSACT` (out)
- **Calls:** `CEE3ABD`

### CBTRN02C — Transaction posting
- **Type:** Batch
- **Function:** Posts daily transactions: validates, writes accepted records to the transaction
  master, rejected ones to a reject file, and updates account balances and category balances.
- **Copybooks:** `CVTRA01Y` (TRAN-CAT-BAL-RECORD), `CVTRA05Y` (TRAN-RECORD), `CVTRA06Y` (DALYTRAN-RECORD), `CVACT01Y`, `CVACT03Y`
- **Files:** `DALYTRAN` (in), `TRANSACT` (out), `XREFFILE`, `ACCTFILE`, `TCATBALF` (in/out), `DALYREJS` (out)
- **Calls:** `CEE3ABD`
- **Invoked by JCL:** [`POSTTRAN.jcl`](../app/jcl/POSTTRAN.jcl)

### CBTRN03C — Transaction detail report
- **Type:** Batch
- **Function:** Prints a transaction detail report grouped with page / account / grand totals,
  joining transaction type and category descriptions.
- **Copybooks:** `CVTRA05Y` (TRAN-RECORD), `CVTRA03Y` (TRAN-TYPE-RECORD), `CVTRA04Y` (TRAN-CAT-RECORD), `CVTRA07Y` (report layouts), `CVACT03Y`
- **Files:** `TRANFILE` (in), `CARDXREF`, `TRANTYPE`, `TRANCATG`, `DATEPARM` (in), `TRANREPT` (out)
- **Calls:** `CEE3ABD`
- **Invoked by JCL:** [`TRANREPT.jcl`](../app/jcl/TRANREPT.jcl)

### COBSWAIT — Wait utility
- **Type:** Batch utility
- **Function:** Suspends the job for a parameterized number of centiseconds (used by `WAITSTEP.jcl`).
- **Copybooks:** —
- **Files:** —
- **Calls:** `MVSWAIT`

---

## Common Subroutine (`CS*`)

### CSUTLDTC — Date utility
- **Type:** Called subroutine
- **Function:** Validates and reformats a date using LE callable services (`CEEDAYS`/`CEEDATE`).
  Used by CICS programs to validate user-entered dates.
- **Copybooks:** —
- **Files:** —
- **Called by:** `CORPT00C`, `COTRN02C`

---

## CICS Online Programs (`CO*`)

All online programs share a common set of "framework" copybooks:

- `COCOM01Y` — `CARDDEMO-COMMAREA` (navigation/context passed between screens)
- `COTTL01Y` — screen title literals
- `CSDAT01Y` — current date/time work area
- `CSMSG01Y` / `CSMSG02Y` — common message / abend literals
- `CSUSR01Y` — `SEC-USER-DATA` (signed-on user)
- `DFHAID`, `DFHBMSCA` (and sometimes `DFHATTR`) — CICS BMS attention-id / attribute constants
- `<mapname>` — the program's BMS symbolic map copybook (see [screen-maps.md](screen-maps.md))

Navigation between online programs is **dynamic**: programs issue
`EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` where the target name is supplied at runtime from the
commarea or from a menu-options table (`COMEN02Y` for the main menu, `COADM02Y` for the admin menu).
The menu-driven targets are listed in [dependency-graph.md](dependency-graph.md).

The per-program copybooks and CICS file access below are in addition to the shared framework copybooks.

### COSGN00C — Signon (CC00)
- **Function:** Authenticates a user against the `USRSEC` file and routes to the main or admin menu.
- **Map:** `COSGN00` / `COSGN0A`
- **Extra copybooks:** `COSGN00`
- **CICS files:** `USRSEC` (read)
- **Navigation:** XCTL to `COMEN01C` (user) / `COADM01C` (admin)

### COMEN01C — Main menu (CM00)
- **Function:** Presents the regular-user menu and dispatches to the selected program.
- **Map:** `COMEN01` / `COMEN1A`
- **Extra copybooks:** `COMEN01`, `COMEN02Y` (menu options table)
- **CICS files:** `USRSEC` (read)
- **Navigation:** XCTL to `CDEMO-MENU-OPT-PGMNAME(option)` — see COMEN02Y mapping in dependency-graph.

### COADM01C — Admin menu (CA00)
- **Function:** Presents the admin menu and dispatches to the selected program.
- **Map:** `COADM01` / `COADM1A`
- **Extra copybooks:** `COADM01`, `COADM02Y` (admin menu options table)
- **CICS files:** `USRSEC` (read)
- **Navigation:** XCTL to `CDEMO-ADMIN-OPT-PGMNAME(option)`.

### COACTVWC — Account view (CAVW)
- **Function:** Reads an account and its customer/xref data and displays it read-only.
- **Map:** `COACTVW` / `CACTVWA`
- **Extra copybooks:** `COACTVW`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`
- **CICS files:** `ACCTDAT`, `CARDDAT`, `CUSTDAT`, `CARDAIX`, `CXACAIX` (read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COACTUPC — Account update (CAUP)
- **Function:** Reads, edits and rewrites an account (and customer) record.
- **Map:** `COACTUP` / `CACTUPA`
- **Extra copybooks:** `COACTUP`, `CVACT01Y`, `CVACT03Y`, `CVCUS01Y`, `CVCRD01Y`, `CSLKPCDY`, `CSUSR01Y`, `CSUTLDPY`, `CSSETATY`, `CSDAT01Y`, `CSMSG01Y`/`CSMSG02Y`
- **CICS files:** `ACCTDAT` (read/rewrite), `CUSTDAT` (read/rewrite), `CARDDAT`, `CARDAIX`, `CXACAIX`
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COCRDLIC — Credit card list (CCLI)
- **Function:** Lists cards (paged) for an account / card filter.
- **Map:** `COCRDLI` / `CCRDLIA`
- **Extra copybooks:** `COCRDLI`, `COCRDSL`, `CVACT02Y`, `CVCRD01Y`
- **CICS files:** `CARDDAT`, `CARDAIX` (browse/read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COCRDSLC — Credit card view (CCDL)
- **Function:** Displays a single card's detail.
- **Map:** `COCRDSL` / `CCRDSLA`
- **Extra copybooks:** `COCRDSL`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`
- **CICS files:** `CARDDAT`, `CARDAIX` (read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COCRDUPC — Credit card update (CCUP)
- **Function:** Reads, edits and rewrites a card record.
- **Map:** `COCRDUP` / `CCRDUPA`
- **Extra copybooks:** `COCRDUP`, `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCRD01Y`, `CVCUS01Y`
- **CICS files:** `CARDDAT` (read/rewrite), `CARDAIX`
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COTRN00C — Transaction list (CT00)
- **Function:** Lists transactions (paged) from the `TRANSACT` file.
- **Map:** `COTRN00` / `COTRN0A`
- **Extra copybooks:** `COTRN00`, `CVTRA05Y`
- **CICS files:** `TRANSACT` (browse/read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COTRN01C — Transaction view (CT01)
- **Function:** Displays a single transaction's detail.
- **Map:** `COTRN01` / `COTRN1A`
- **Extra copybooks:** `COTRN01`, `CVTRA05Y`
- **CICS files:** `TRANSACT` (read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COTRN02C — Transaction add (CT02)
- **Function:** Validates input and writes a new transaction; verifies account/card via xref.
- **Map:** `COTRN02` / `COTRN2A`
- **Extra copybooks:** `COTRN02`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`
- **CICS files:** `ACCTDAT`, `CCXREF`, `CXACAIX` (read), `TRANSACT` (write)
- **Calls:** `CSUTLDTC` (date validation)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### CORPT00C — Transaction reports (CR00)
- **Function:** Builds report-request JCL (monthly/yearly/custom) and submits it through the CICS
  internal reader / spool.
- **Map:** `CORPT00` / `CORPT0A`
- **Extra copybooks:** `CORPT00`, `CVTRA05Y`
- **CICS files:** `TRANSACT` (read), spool/internal reader (write)
- **Calls:** `CSUTLDTC` (date validation)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COBIL00C — Bill payment (CB00)
- **Function:** Pays an account balance in full and writes the resulting transaction.
- **Map:** `COBIL00` / `COBIL0A`
- **Extra copybooks:** `COBIL00`, `CVACT01Y`, `CVACT03Y`, `CVTRA05Y`
- **CICS files:** `ACCTDAT` (read/rewrite), `CXACAIX` (read), `TRANSACT` (write)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COUSR00C — List users (CU00)
- **Function:** Lists security users (paged) from the `USRSEC` file.
- **Map:** `COUSR00` / `COUSR0A`
- **Extra copybooks:** `COUSR00`, `CSUSR01Y`
- **CICS files:** `USRSEC` (browse/read)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COUSR01C — Add user (CU01)
- **Function:** Adds a regular/admin user to the `USRSEC` file.
- **Map:** `COUSR01` / `COUSR1A`
- **Extra copybooks:** `COUSR01`, `CSUSR01Y`
- **CICS files:** `USRSEC` (write)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COUSR02C — Update user (CU02)
- **Function:** Updates a user record in the `USRSEC` file.
- **Map:** `COUSR02` / `COUSR2A`
- **Extra copybooks:** `COUSR02`, `CSUSR01Y`
- **CICS files:** `USRSEC` (read/rewrite)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

### COUSR03C — Delete user (CU03)
- **Function:** Deletes a user from the `USRSEC` file.
- **Map:** `COUSR03` / `COUSR3A`
- **Extra copybooks:** `COUSR03`, `CSUSR01Y`
- **CICS files:** `USRSEC` (read/delete)
- **Navigation:** XCTL to `CDEMO-TO-PROGRAM`

---

## Logical file ↔ dataset reference

The DD names above map to these logical VSAM/sequential datasets (see [batch-flows.md](batch-flows.md)
for the full DSN list):

| DD / CICS name        | Dataset (logical)                         | Record copybook |
|:----------------------|:------------------------------------------|:----------------|
| ACCTFILE / ACCTDAT    | `…CARDDEMO.ACCTDATA.VSAM.KSDS`            | CVACT01Y        |
| CARDFILE / CARDDAT    | `…CARDDEMO.CARDDATA.VSAM.KSDS`            | CVACT02Y        |
| XREFFILE / CCXREF     | `…CARDDEMO.CARDXREF.VSAM.KSDS`            | CVACT03Y        |
| XREFFIL1 / CXACAIX    | `…CARDDEMO.CARDXREF.VSAM.AIX.PATH` (AIX)  | CVACT03Y        |
| CARDAIX               | `…CARDDEMO.CARDDATA.VSAM.AIX.PATH` (AIX)  | CVACT02Y        |
| CUSTFILE / CUSTDAT    | `…CARDDEMO.CUSTDATA.VSAM.KSDS`            | CVCUS01Y/CUSTREC|
| TCATBALF              | `…CARDDEMO.TCATBALF.VSAM.KSDS`            | CVTRA01Y        |
| DISCGRP               | `…CARDDEMO.DISCGRP.VSAM.KSDS`             | CVTRA02Y        |
| TRANSACT / TRANFILE   | `…CARDDEMO.TRANSACT.VSAM.KSDS`            | CVTRA05Y        |
| TRANTYPE              | `…CARDDEMO.TRANTYPE.VSAM.KSDS`            | CVTRA03Y        |
| TRANCATG              | `…CARDDEMO.TRANCATG.VSAM.KSDS`            | CVTRA04Y        |
| DALYTRAN              | `…CARDDEMO.DALYTRAN.PS`                   | CVTRA06Y        |
| USRSEC                | `…CARDDEMO.USRSEC.VSAM.KSDS`              | CSUSR01Y        |
</content>
