# CardDemo — Agent Guide

CardDemo is a classic mainframe credit card management application built with COBOL, CICS, VSAM, JCL, and RACF. It exists as a reference for mainframe modernization use-cases: discovery, migration, transformation, test harness creation, and service extraction.

> Note: Coding style is intentionally non-uniform across the application — this is by design to exercise analysis and transformation tooling.

---

## Directory Map

```
app/
  cbl/        COBOL source programs (batch: CB*, online CICS: CO*)
  cpy/        Copybooks — shared data structures (entity schemas)
  cpy-bms/    BMS-generated symbolic copybooks (screen field definitions)
  bms/        BMS map source (screen layouts)
  jcl/        JCL jobs (data setup, batch processing, compile samples)
  proc/       Cataloged PROCs for compiling COBOL and assembling BMS
  data/       Seed data in ASCII and EBCDIC formats
  csd/        CICS resource definitions (DFHCSDUP input)
  catlg/      VSAM LISTCAT output (reference)
diagrams/     Application flow diagrams and data model (drawio + png)
samples/      Sample JCL for compile and runtime
```

---

## Program Naming Conventions

| Prefix | Type | Example |
|--------|------|---------|
| `CB*`  | Batch COBOL program | `CBTRN02C`, `CBACT04C` |
| `CO*`  | Online CICS COBOL program | `COACTVWC`, `COBIL00C` |

Program names are exactly 8 characters — CICS resource definitions reference these exact names. **Do not rename programs.**

---

## Entity → Copybook → VSAM File Reference

| Entity | Copybook | VSAM File | Record Length |
|--------|----------|-----------|---------------|
| Account | `CVACT01Y.cpy` | `ACCTDAT` | 300 bytes |
| Credit Card | `CVACT02Y.cpy` | `CARDDAT` | 150 bytes |
| Customer | `CVCUS01Y.cpy` / `CUSTREC.cpy` | `CUSTDAT` | 500 bytes |
| Card-Account-Customer XREF | `CVACT03Y.cpy` | `CCXREF` | 50 bytes |
| Transaction (online VSAM) | `CVTRA05Y.cpy` | `TRANSACT` | 350 bytes |
| Transaction (daily batch flat file) | `CVTRA06Y.cpy` | `DALYTRAN` | 350 bytes |
| Transaction Category Balance | `CVTRA01Y.cpy` | `TCATBALF` | 50 bytes |
| Disclosure Group | `CVTRA02Y.cpy` | `DISCGRP` | 50 bytes |
| Transaction Type | `CVTRA03Y.cpy` | `TRANTYPE` | 60 bytes |
| Transaction Category | `CVTRA04Y.cpy` | `TRANCATG` | 60 bytes |
| User Security | `CSUSR01Y.cpy` | `USRSEC` | 80 bytes |

VSAM files are **fixed-length**. Changing copybook field lengths or total record size will break the VSAM cluster definition and all programs that use it.

---

## Online Program Inventory (CICS)

| Transaction | Program | BMS Map | Function |
|-------------|---------|---------|----------|
| CC00 | COSGN00C | COSGN00 | Signon |
| CM00 | COMEN01C | COMEN01 | Main Menu |
| CAVW | COACTVWC | COACTVW | Account View |
| CAUP | COACTUPC | COACTUP | Account Update |
| CCLI | COCRDLIC | COCRDLI | Credit Card List |
| CCDL | COCRDSLC | COCRDSL | Credit Card View |
| CCUP | COCRDUPC | COCRDUP | Credit Card Update |
| CT00 | COTRN00C | COTRN00 | Transaction List |
| CT01 | COTRN01C | COTRN01 | Transaction View |
| CT02 | COTRN02C | COTRN02 | Transaction Add |
| CR00 | CORPT00C | CORPT00 | Transaction Reports |
| CB00 | COBIL00C | COBIL00 | Bill Payment |
| CA00 | COADM01C | COADM01 | Admin Menu |
| CU00 | COUSR00C | COUSR00 | List Users |
| CU01 | COUSR01C | COUSR01 | Add User |
| CU02 | COUSR02C | COUSR02 | Update User |
| CU03 | COUSR03C | COUSR03 | Delete User |

Online programs follow the **pseudo-conversational** CICS pattern: each screen interaction is a separate task; state is passed via COMMAREA.

---

## Batch Program Inventory

| JCL Job | Program | Function |
|---------|---------|----------|
| POSTTRAN | CBTRN02C | Post daily transactions to accounts |
| INTCALC | CBACT04C | Calculate interest and fees |
| CREASTMT | CBSTM03A | Generate account statements |
| TRANREPT | CBTRN03C | Produce transaction reports |
| READACCT | CBACT01C | Read and print account data |

---

## Build Process

Three PROCs in `app/proc/` handle compilation:

| PROC | Purpose |
|------|---------|
| `BUILDBAT.prc` | Compile batch COBOL programs |
| `BUILDONL.prc` | Compile online CICS COBOL programs (includes CICS translator step) |
| `BUILDBMS.prc` | Assemble BMS maps into physical mapsets and symbolic copybooks |

Sample compile JCLs are in `samples/jcl/` and `samples/proc/`.

---

## Data Setup JCL Order (Initial Load)

Run these jobs in order to initialize all VSAM files from seed data:

```
DUSRSECJ → CLOSEFIL → ACCTFILE → CARDFILE → CUSTFILE → XREFFILE
→ TRANFILE → DISCGRP → TCATBALF → TRANCATG → TRANTYPE → OPENFIL → DEFGDGB
```

Seed data files are in `app/data/ASCII/` (text) and `app/data/EBCDIC/` (binary, upload to mainframe in binary mode).

---

## Daily Batch Processing Order

```
CLOSEFIL → ACCTFILE → CARDFILE → XREFFILE → CUSTFILE → TRANBKP
→ DISCGRP → TCATBALF → TRANTYPE → DUSRSECJ → POSTTRAN → INTCALC
→ TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

---

## CICS Resource Setup

CICS resources are defined in `app/csd/CARDDEMO.CSD`. Apply them with:

```jcl
// EXEC PGM=DFHCSDUP   (see app/jcl/CBADMCDJ.jcl)
```

Or interactively via `CEDA`. After loading, run `CEMT SET PROG(...) NEWCOPY` for any updated programs/mapsets.

**Default credentials:**
- Admin: `ADMIN001` / `PASSWORD`
- User: `USER0001` / `PASSWORD`

---

## Key Conventions for AI Agents

- **Never rename programs** — CICS transaction definitions, `CEDA` commands, and JCL PROCs reference exact 8-character program names.
- **Never change copybook record lengths** — VSAM clusters are defined with fixed record sizes; changing field layouts breaks the cluster.
- **Copybooks are shared schemas** — A change to a copybook field propagates to every program that copies it in. Always search for all users of a copybook before modifying it.
- **CICS programs are pseudo-conversational** — Do not introduce CICS WAIT or conversational patterns; state must flow via COMMAREA between transactions.
- **PIC clauses define storage** — Field lengths in COBOL PIC clauses directly map to byte offsets in VSAM records. Use `COMP-3` for packed decimal, `COMP` for binary.
- **Batch vs. online separation** — `CLOSEFIL`/`OPENFIL` JCL jobs manage VSAM file sharing between CICS and batch. Batch jobs should only run when CICS has released the files.
