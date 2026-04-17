# CardDemo: Dependency Graph & Strangler-Fig Migration Sequence

## Executive Summary

This document traces the complete **XCTL/CALL graph** and **VSAM file dependency map** for the CardDemo mainframe application, then recommends a phased **strangler-fig migration order** that minimises cross-phase coupling and allows incremental cut-over from COBOL/CICS to a modern stack.

The recommended sequence is:

> **Phase 0 (Foundation)** &rarr; **Phase 1 Auth** &rarr; **Phase 2 User Admin** &rarr; **Phase 3 Account** &rarr; **Phase 4 Card** &rarr; **Phase 5 Transaction** &rarr; **Phase 6 Statements & Reports**

---

## 1. Program Inventory

### 1.1 Online Programs (CICS)

| Program | Txn ID | Function | BMS Map |
|---------|--------|----------|---------|
| `COSGN00C` | CC00 | User Sign-on | COSGN00 |
| `COMEN01C` | CM00 | Main Menu (Regular Users) | COMEN01 |
| `COADM01C` | CA00 | Admin Menu | COADM01 |
| `COACTVWC` | CAVW | Account View | COACTVW |
| `COACTUPC` | CAUP | Account Update | COACTUP |
| `COCRDLIC` | CCLI | Credit Card List | COCRDLI |
| `COCRDSLC` | CCDL | Credit Card View (Detail) | COCRDSL |
| `COCRDUPC` | CCUP | Credit Card Update | COCRDUP |
| `COTRN00C` | CT00 | Transaction List | COTRN00 |
| `COTRN01C` | CT01 | Transaction View | COTRN01 |
| `COTRN02C` | CT02 | Transaction Add | COTRN02 |
| `CORPT00C` | CR00 | Transaction Reports | CORPT00 |
| `COBIL00C` | CB00 | Bill Payment | COBIL00 |
| `COUSR00C` | CU00 | User List | COUSR00 |
| `COUSR01C` | CU01 | User Add | COUSR01 |
| `COUSR02C` | CU02 | User Update | COUSR02 |
| `COUSR03C` | CU03 | User Delete | COUSR03 |

### 1.2 Batch Programs

| Program | JCL Job | Function |
|---------|---------|----------|
| `CBTRN01C` | &mdash; | Daily transaction validation/enrichment |
| `CBTRN02C` | POSTTRAN | Core daily transaction posting |
| `CBTRN03C` | TRANREPT | Transaction reporting (batch) |
| `CBACT01C` | READACCT | Account file reader |
| `CBACT02C` | READCARD | Card file reader |
| `CBACT03C` | READXREF | Cross-reference file reader |
| `CBACT04C` | INTCALC | Interest calculation |
| `CBCUS01C` | READCUST | Customer file reader |
| `CBSTM03A` | CREASTMT | Statement generation (driver) |
| `CBSTM03B` | &mdash; | Statement file I/O subroutine |

### 1.3 Utility Programs

| Program | Function |
|---------|----------|
| `CSUTLDTC` | Date conversion utility (calls `CEEDAYS`) |

---

## 2. XCTL (Transfer-Control) Graph

The XCTL graph shows how control flows between CICS programs. An arrow `A --XCTL--> B` means program A issues `EXEC CICS XCTL PROGRAM('B')`, transferring control irrevocably (A does not resume).

```
                          +-----------+
                     +--->| COADM01C  |---+---> COUSR00C --XCTL--> COUSR02C
                     |    | (Admin    |   |                    \--> COUSR03C
                     |    |  Menu)    |   +---> COUSR01C
                     |    +-----------+   +---> COUSR02C
                     |         |          +---> COUSR03C
                     |         |
                     |    PF3: XCTL --> COSGN00C
                     |
  +----------+       |
  | COSGN00C |-------+   (Admin user: XCTL --> COADM01C)
  | (Sign-on)|
  |   CC00   |-------+   (Regular user: XCTL --> COMEN01C)
  +----------+       |
                     |    +-----------+
                     +--->| COMEN01C  |---+---> COACTVWC (Opt 1)
                          | (Main     |   +---> COACTUPC (Opt 2)
                          |  Menu)    |   +---> COCRDLIC (Opt 3)
                          +-----------+   +---> COCRDSLC (Opt 4)
                               |          +---> COCRDUPC (Opt 5)
                               |          +---> COTRN00C (Opt 6)
                               |          +---> COTRN01C (Opt 7)
                               |          +---> COTRN02C (Opt 8)
                          PF3: XCTL       +---> CORPT00C (Opt 9)
                          --> COSGN00C    +---> COBIL00C (Opt 10)
```

### 2.1 Detailed XCTL Edges

| Source Program | XCTL Target(s) | Trigger |
|---------------|-----------------|---------|
| **COSGN00C** | `COADM01C` | Admin login success (`SEC-USR-TYPE = 'A'`) |
| **COSGN00C** | `COMEN01C` | Regular user login success |
| **COMEN01C** | `COACTVWC`, `COACTUPC`, `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `COTRN00C`, `COTRN01C`, `COTRN02C`, `CORPT00C`, `COBIL00C` | Menu option selection (via `CDEMO-MENU-OPT-PGMNAME` array) |
| **COMEN01C** | `COSGN00C` | PF3 (return to sign-on) |
| **COADM01C** | `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C` | Admin menu option selection (via `CDEMO-ADMIN-OPT-PGMNAME` array) |
| **COADM01C** | `COSGN00C` | PF3 (return to sign-on) |
| **COACTVWC** | `CDEMO-TO-PROGRAM` (dynamic: returns to caller, defaults `COMEN01C`) | PF3 |
| **COACTUPC** | `CDEMO-TO-PROGRAM` (dynamic: returns to caller) | PF3 |
| **COCRDLIC** | `CDEMO-TO-PROGRAM` (defaults `COMEN01C`) | PF3 |
| **COCRDLIC** | `COCRDSLC` | Select card row for detail view |
| **COCRDLIC** | `COCRDUPC` | Select card row for update |
| **COCRDSLC** | `CDEMO-FROM-PROGRAM` (returns to caller, e.g. `COCRDLIC`) | PF3 |
| **COCRDUPC** | `CDEMO-FROM-PROGRAM` (returns to caller, e.g. `COCRDLIC`) | PF3 |
| **COTRN00C** | `COTRN01C` | Select transaction row for detail view |
| **COTRN00C** | `COSGN00C` or `COMEN01C` | PF3 |
| **COTRN01C** | `CDEMO-FROM-PROGRAM` (returns to caller, e.g. `COTRN00C`) | PF3 |
| **COTRN02C** | `CDEMO-FROM-PROGRAM` (returns to caller) or `COMEN01C` | PF3 |
| **CORPT00C** | `COMEN01C` or `COSGN00C` | PF3 |
| **COBIL00C** | `COMEN01C` or `COSGN00C` | PF3 |
| **COUSR00C** | `COUSR02C` | Select user row for update |
| **COUSR00C** | `COUSR03C` | Select user row for delete |
| **COUSR00C** | `COADM01C` or `COSGN00C` | PF3 |
| **COUSR01C** | `COADM01C` or `COSGN00C` | PF3 |
| **COUSR02C** | `CDEMO-FROM-PROGRAM` (e.g. `COUSR00C` or `COADM01C`) | PF3 |
| **COUSR03C** | `CDEMO-FROM-PROGRAM` (e.g. `COUSR00C` or `COADM01C`) | PF3 |

> **Key observation**: All programs ultimately return to `COSGN00C` as the terminal fallback. The `CDEMO-TO-PROGRAM` / `CDEMO-FROM-PROGRAM` fields in the COMMAREA (`COCOM01Y.cpy`) implement a simple call-stack convention.

---

## 3. CALL Graph (Static Calls)

Unlike XCTL (which transfers control permanently), `CALL` invocations return control to the caller.

| Caller | Callee | Purpose |
|--------|--------|---------|
| **COTRN02C** | `CSUTLDTC` | Date format validation/conversion |
| **CORPT00C** | `CSUTLDTC` | Date format validation/conversion |
| **CSUTLDTC** | `CEEDAYS` | LE date intrinsic (system call) |
| **CBSTM03A** | `CBSTM03B` | File I/O subroutine for statement generation (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE) |
| **CBTRN01C** | `CEE3ABD` | Abnormal termination (error handling) |
| **CBTRN02C** | `CEE3ABD` | Abnormal termination |
| **CBTRN03C** | `CEE3ABD` | Abnormal termination |
| **CBACT01C** | `CEE3ABD` | Abnormal termination |
| **CBACT02C** | `CEE3ABD` | Abnormal termination |
| **CBACT03C** | `CEE3ABD` | Abnormal termination |
| **CBACT04C** | `CEE3ABD` | Abnormal termination |
| **CBCUS01C** | `CEE3ABD` | Abnormal termination |
| **CBSTM03A** | `CEE3ABD` | Abnormal termination |

```
  COTRN02C ----CALL----> CSUTLDTC ----CALL----> CEEDAYS (LE intrinsic)
  CORPT00C ----CALL----> CSUTLDTC ----CALL----> CEEDAYS

  CBSTM03A ----CALL----> CBSTM03B  (file I/O: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE)

  All batch programs --CALL--> CEE3ABD  (abnormal termination handler)
```

---

## 4. VSAM File Dependency Matrix

### 4.1 VSAM File Inventory

| CICS File Name | VSAM Dataset | Copybook | Key | Description |
|----------------|-------------|----------|-----|-------------|
| `USRSEC` | USRSEC.VSAM.KSDS | `CSUSR01Y` | USER-ID (8) | User credentials & type |
| `ACCTDAT` | ACCTDATA.VSAM.KSDS | `CVACT01Y` | ACCT-ID (11) | Account master |
| `CARDDAT` | CARDDATA.VSAM.KSDS | `CVACT02Y` | CARD-NUM (16) | Card master |
| `CUSTDAT` | CUSTDATA.VSAM.KSDS | `CVCUS01Y` | CUST-ID (9) | Customer master |
| `CXACAIX` | CARDXREF.VSAM.AIX.PATH | `CVACT03Y` | ACCT-ID (via AIX) | Card-Account-Customer cross-reference (AIX by account) |
| `TRANSACT` | TRANSACT.VSAM.KSDS | `CVTRA05Y` | TRAN-ID (16) | Transaction master |
| `TRANTYPE` | TRANTYPE.VSAM.KSDS | `CVTRA03Y` | TRAN-TYPE-CD (2) | Transaction type reference |
| `TRANCATG` | TRANCATG.VSAM.KSDS | `CVTRA04Y` | TRAN-CAT-CD (4) | Transaction category reference |
| `TCATBALF` | TCATBALF.VSAM.KSDS | `CVTRA01Y` | TCAT-BAL-ID | Category balance file |
| `DISCGRP` | DISCGRP.VSAM.KSDS | `CVTRA02Y` | DISC-GRP-CD | Disclosure group reference |

Batch-only files (sequential PS or GDG):

| DD Name | Description |
|---------|-------------|
| `DALYTRAN` | Daily transaction input (PS, `CVTRA06Y`) |
| `DALYREJS` | Daily rejected transactions (output) |
| `TRANREPT` | Transaction report output |
| `STMTFILE` | Statement text output |
| `HTMLFILE` | Statement HTML output |

### 4.2 Program-to-File Access Matrix

#### Online Programs (CICS File Control)

| Program | USRSEC | ACCTDAT | CUSTDAT | CARDDAT | CXACAIX | TRANSACT | Operations |
|---------|:------:|:-------:|:-------:|:-------:|:-------:|:--------:|------------|
| **COSGN00C** | **R** | | | | | | READ user credentials |
| **COMEN01C** | | | | | | | No file I/O (menu only) |
| **COADM01C** | | | | | | | No file I/O (menu only) |
| **COUSR00C** | **R** | | | | | | STARTBR/READNEXT/READPREV users |
| **COUSR01C** | **W** | | | | | | WRITE new user record |
| **COUSR02C** | **RW** | | | | | | READ + REWRITE user record |
| **COUSR03C** | **RD** | | | | | | READ + DELETE user record |
| **COACTVWC** | | **R** | **R** | | **R** | | READ account, customer, card-xref (by acct) |
| **COACTUPC** | | **RW** | **RW** | | **R** | | READ/REWRITE account & customer; READ card-xref |
| **COCRDLIC** | | | | **R** | | | STARTBR/READNEXT/READPREV cards |
| **COCRDSLC** | | | | **R** | | | READ card by key and by acct-path |
| **COCRDUPC** | | | | **RW** | | | READ + REWRITE card record |
| **COTRN00C** | | | | | | **R** | STARTBR/READNEXT/READPREV transactions |
| **COTRN01C** | | | | | | **R** | READ transaction by key |
| **COTRN02C** | | | | | **R** | **RW** | READ card-xref; STARTBR/READPREV + WRITE transaction |
| **CORPT00C** | | | | | | | WRITEQ TD to JOBS queue (triggers batch) |
| **COBIL00C** | | **RW** | | | **R** | **RW** | READ/REWRITE account; READ xref; STARTBR/READPREV/WRITE transaction |

**Legend**: R = Read, W = Write (new), RW = Read + Rewrite (update), RD = Read + Delete, D = Delete

#### Batch Programs (Standard File I/O)

| Program | DALYTRAN | TRANSACT | ACCTFILE | CARDFILE | CUSTFILE | XREFFILE | TCATBALF | DISCGRP | TRANTYPE | TRANCATG | Output Files |
|---------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-------:|:--------:|:--------:|:-------------|
| **CBTRN01C** | R | R | R | R | R | R | | | | | (validation) |
| **CBTRN02C** | R | W | RW | | | R | RW | | | | DALYREJS (W) |
| **CBTRN03C** | | R | | | | R | | | R | R | TRANREPT (W) |
| **CBACT01C** | | | R | | | | | | | | (reader utility) |
| **CBACT02C** | | | | R | | | | | | | (reader utility) |
| **CBACT03C** | | | | | | R | | | | | (reader utility) |
| **CBACT04C** | | W | RW | | | R | R | R | | | (interest calc) |
| **CBCUS01C** | | | | | R | | | | | | (reader utility) |
| **CBSTM03A** | | | | | | | | | | | STMTFILE, HTMLFILE (W) |
| **CBSTM03B** | | R* | R* | | R* | R* | | | | | (*via CALL from CBSTM03A) |

---

## 5. Functional Domain Clustering

Based on the XCTL graph, CALL graph, and VSAM dependencies, the programs cluster into six functional domains:

```
+-------------------------------------------------------------------+
|                     FUNCTIONAL DOMAIN MAP                         |
+-------------------------------------------------------------------+

  [Auth]                    [User Admin]
  COSGN00C ----XCTL----->  COADM01C
    |  USRSEC (R)             |  (no file I/O)
    |                         +----> COUSR00C  USRSEC (R)
    +----XCTL---> COMEN01C    +----> COUSR01C  USRSEC (W)
                    |         +----> COUSR02C  USRSEC (RW)
                    |         +----> COUSR03C  USRSEC (RD)
                    |
  [Account]         |         [Card]
  COACTVWC <--------+-------> COCRDLIC
    ACCTDAT (R)     |           CARDDAT (R)
    CUSTDAT (R)     |         COCRDSLC
    CXACAIX (R)     |           CARDDAT (R)
  COACTUPC <--------+         COCRDUPC
    ACCTDAT (RW)    |           CARDDAT (RW)
    CUSTDAT (RW)    |
    CXACAIX (R)     |
                    |
  [Transaction]     |         [Reports/Statements]
  COTRN00C <--------+         CORPT00C <---+
    TRANSACT (R)    |           (WRITEQ TD) |
  COTRN01C <--------+         COBIL00C <---+
    TRANSACT (R)    |           ACCTDAT (RW)
  COTRN02C <--------+           CXACAIX (R)
    TRANSACT (RW)                TRANSACT (RW)
    CXACAIX (R)
                              [Batch Statements]
  [Batch Transaction]         CBSTM03A --CALL--> CBSTM03B
  CBTRN01C (validation)         TRANSACT (R)
  CBTRN02C (posting)            XREFFILE (R)
    DALYTRAN (R)                CUSTFILE (R)
    TRANSACT (W)                ACCTFILE (R)
    ACCTFILE (RW)
    XREFFILE (R)              [Batch Interest]
    TCATBALF (RW)             CBACT04C
    DALYREJS (W)                ACCTFILE (RW)
  CBTRN03C (reporting)          XREFFILE (R)
    TRANSACT (R)                TCATBALF (R)
    XREFFILE (R)                DISCGRP (R)
    TRANTYPE (R)                TRANSACT (W)
    TRANCATG (R)
```

---

## 6. Strangler-Fig Migration Sequence

### Migration Principles

1. **Leaf-first**: Migrate modules with the fewest downstream dependents first.
2. **Shared data isolation**: Place an anti-corruption layer (ACL) / API facade over VSAM files at each phase boundary so legacy programs can still access data.
3. **COMMAREA as contract**: The `COCOM01Y` COMMAREA structure is the inter-program contract. A modern session/context object replaces it.
4. **Dual-write during transition**: Each phase writes to both legacy VSAM and the new data store until the phase is fully cut over.

### Phase Dependency Diagram

```
Phase 0 ──> Phase 1 ──> Phase 2 ──> Phase 3 ──> Phase 4 ──> Phase 5 ──> Phase 6
Foundation    Auth       User         Account      Card       Transaction  Statements
                         Admin                                              & Reports
```

Each phase depends only on the phases before it. No forward dependencies.

---

### Phase 0: Foundation (Shared Infrastructure)

**Goal**: Establish the modern platform, COMMAREA replacement, and data-access facades.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COCOM01Y.cpy` | Copybook | Map COMMAREA fields to a modern session/context DTO (user-id, user-type, from-program, to-program, acct-id, card-num, cust-id) |
| `COTTL01Y.cpy` | Copybook | Extract screen-title constants into i18n/config |
| `CSDAT01Y.cpy` | Copybook | Replace date formatting with modern date library |
| `CSMSG01Y.cpy` | Copybook | Extract user-facing messages to resource bundle |
| `CSUTLDTC.cbl` | Utility | Rewrite date conversion (`CEEDAYS` call) as a shared utility function |
| `COMEN02Y.cpy` | Copybook | Define menu option metadata as configuration (JSON/YAML), not hard-coded arrays |
| `COADM02Y.cpy` | Copybook | Define admin menu options as configuration |

**Deliverables**:
- Modern session context object replacing COMMAREA
- Shared date utility service
- Externalized menu configuration
- Anti-corruption layer (ACL) for VSAM file access via API

**VSAM files touched**: None directly (ACL wrappers only).

**Risk**: Low. No business logic changes.

---

### Phase 1: Authentication

**Goal**: Replace `COSGN00C` with a modern auth service. This is the entry point for **all** users.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COSGN00C.cbl` | Program | Replace with modern authentication service (JWT/OAuth). Validate credentials against migrated user store instead of VSAM `USRSEC` READ |
| `COSGN00.bms` | BMS Map | Replace 3270 sign-on screen with web login form |
| `CSUSR01Y.cpy` | Copybook | Map `SEC-USER-DATA` record layout to User entity/table schema |

**VSAM files**:
- `USRSEC` &mdash; **READ** (credential validation). Dual-write from new user store during transition.

**XCTL edges cut**:
- `COSGN00C --> COADM01C`: Replace with route dispatch (admin dashboard)
- `COSGN00C --> COMEN01C`: Replace with route dispatch (user dashboard)

**Why first**: Every session starts here. Zero downstream business-logic dependents. Only reads `USRSEC`. Isolating auth first enables modern identity (JWT tokens replace COMMAREA user-id/type).

**Anti-corruption layer**: Legacy programs that XCTL back to `COSGN00C` (PF3 from all programs) are redirected to the new auth endpoint via a thin CICS wrapper that issues `EXEC CICS XCTL` to the new facade.

---

### Phase 2: User Administration

**Goal**: Replace the four user-management programs with a modern user CRUD API.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COUSR00C.cbl` | Program | Replace STARTBR/READNEXT/READPREV browse of `USRSEC` with `GET /api/v1/users?page=N` |
| `COUSR01C.cbl` | Program | Replace WRITE to `USRSEC` with `POST /api/v1/users` |
| `COUSR02C.cbl` | Program | Replace READ + REWRITE of `USRSEC` with `PUT /api/v1/users/{id}` |
| `COUSR03C.cbl` | Program | Replace READ + DELETE of `USRSEC` with `DELETE /api/v1/users/{id}` |
| `COUSR00.bms` &ndash; `COUSR03.bms` | BMS Maps | Replace 3270 screens with web UI (user admin dashboard) |

**VSAM files**:
- `USRSEC` &mdash; Full CRUD. After this phase, `USRSEC` VSAM can be **decommissioned** and replaced entirely by a modern user store (database table).

**XCTL edges cut**:
- `COADM01C --> COUSR00C/01C/02C/03C`: Replace with navigation to user admin routes
- `COUSR00C --> COUSR02C/03C`: Replace with in-app navigation

**Why second**: Depends only on Phase 1 (auth). Touches only `USRSEC` (no overlap with account/card/transaction files). After this phase, `USRSEC` is fully retired from VSAM.

**Migration gate**: Once Phase 2 completes, `COADM01C` (admin menu) can be refactored into a modern admin dashboard that routes to the new user API. The legacy `COADM01C` program can be retired.

---

### Phase 3: Account Management

**Goal**: Migrate account view and update to modern services.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COACTVWC.cbl` | Program | Replace CICS READs of `CXACAIX`, `ACCTDAT`, `CUSTDAT` with `GET /api/v1/accounts/{id}` (returns account + customer + card-xref data) |
| `COACTUPC.cbl` | Program | Replace READ/REWRITE of `ACCTDAT` and `CUSTDAT` with `PUT /api/v1/accounts/{id}`. Replace CXACAIX lookup with join query |
| `COACTUP.bms`, `COACTVW.bms` | BMS Maps | Replace 3270 screens with account management web UI |
| `CVACT01Y.cpy` | Copybook | Map `ACCOUNT-RECORD` (300 bytes) to Account entity/table |
| `CVCUS01Y.cpy` | Copybook | Map `CUSTOMER-RECORD` (500 bytes) to Customer entity/table |
| `CVACT03Y.cpy` | Copybook | Map `CARD-XREF-RECORD` (50 bytes) to foreign-key relationships in RDBMS |

**VSAM files**:
- `ACCTDAT` &mdash; READ + REWRITE (view/update)
- `CUSTDAT` &mdash; READ + REWRITE (view/update)
- `CXACAIX` &mdash; READ (cross-reference lookup by account)

**XCTL edges cut**:
- `COMEN01C --> COACTVWC`: Replace with route to account view
- `COMEN01C --> COACTUPC`: Replace with route to account update

**Why third**: Account view/update reads from `ACCTDAT`, `CUSTDAT`, and `CXACAIX`. These three files are also read by downstream Card and Transaction programs, so establishing the Account API first provides a reusable data source. No dependency on Card or Transaction modules.

**Dual-write strategy**: The ACL writes to both VSAM (`ACCTDAT`, `CUSTDAT`) and the new RDBMS during transition. Batch programs (`CBTRN02C`, `CBACT04C`) continue reading/writing VSAM until Phase 5.

---

### Phase 4: Card Management

**Goal**: Migrate credit card list, view, and update.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COCRDLIC.cbl` | Program | Replace STARTBR/READNEXT/READPREV of `CARDDAT` with `GET /api/v1/cards?accountId=X&page=N` |
| `COCRDSLC.cbl` | Program | Replace READ of `CARDDAT` (by key and by account-path) with `GET /api/v1/cards/{cardNum}` |
| `COCRDUPC.cbl` | Program | Replace READ + REWRITE of `CARDDAT` with `PUT /api/v1/cards/{cardNum}` |
| `COCRDLI.bms`, `COCRDSL.bms`, `COCRDUP.bms` | BMS Maps | Replace 3270 screens with card management web UI |
| `CVACT02Y.cpy` | Copybook | Map `CARD-RECORD` (150 bytes) to Card entity/table |
| `CVCRD01Y.cpy` | Copybook | Map card display fields |

**VSAM files**:
- `CARDDAT` &mdash; Full browse + READ + REWRITE. After this phase, `CARDDAT` online access can be **retired** from CICS.

**XCTL edges cut**:
- `COMEN01C --> COCRDLIC/COCRDSLC/COCRDUPC`: Replace with card management routes
- `COCRDLIC --> COCRDSLC`: Replace with in-app card detail navigation
- `COCRDLIC --> COCRDUPC`: Replace with in-app card edit navigation

**Why fourth**: Card programs only access `CARDDAT`. The card-xref (`CXACAIX`) was already wrapped in Phase 3's Account API. Card programs have no dependency on Transaction data. The Card domain is self-contained after Account is migrated.

**Migration gate**: After Phase 4, `COMEN01C` menu options 1-5 (Account View, Account Update, Card List, Card View, Card Update) are all served by the modern stack. The legacy main menu can route these options to the new web UI.

---

### Phase 5: Transaction Processing

**Goal**: Migrate the most complex domain &mdash; online transaction list/view/add, bill payment, and batch transaction posting.

#### Phase 5a: Online Transaction Programs

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `COTRN00C.cbl` | Program | Replace STARTBR/READNEXT/READPREV of `TRANSACT` with `GET /api/v1/transactions?page=N` |
| `COTRN01C.cbl` | Program | Replace READ of `TRANSACT` with `GET /api/v1/transactions/{id}` |
| `COTRN02C.cbl` | Program | Replace CXACAIX read + TRANSACT write with `POST /api/v1/transactions`. Replace `CALL 'CSUTLDTC'` with modern date validation |
| `COBIL00C.cbl` | Program | Replace ACCTDAT read/write + CXACAIX read + TRANSACT read/write with `POST /api/v1/payments`. Bill payment is a composite operation spanning Account + Transaction |
| `CORPT00C.cbl` | Program | Replace WRITEQ TD to JOBS queue with async job submission API. Replace `CALL 'CSUTLDTC'` with modern date validation |
| `COTRN00.bms` &ndash; `COTRN02.bms`, `COBIL00.bms`, `CORPT00.bms` | BMS Maps | Replace 3270 screens with transaction web UI |
| `CVTRA05Y.cpy` | Copybook | Map `TRAN-RECORD` (350 bytes) to Transaction entity/table |
| `CVTRA06Y.cpy` | Copybook | Map `DALYTRAN-RECORD` (350 bytes) to daily transaction input schema |

**VSAM files**:
- `TRANSACT` &mdash; Full CRUD (browse, read, write)
- `CXACAIX` &mdash; READ (card-to-account lookup for transaction add)
- `ACCTDAT` &mdash; READ + REWRITE (bill payment updates account balance)

#### Phase 5b: Batch Transaction Programs

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `CBTRN01C.cbl` | Batch | Replace file-based transaction validation with modern validation service. Reads: DALYTRAN, TRANSACT, ACCTFILE, CARDFILE, CUSTFILE, XREFFILE |
| `CBTRN02C.cbl` | Batch | Replace core posting logic with modern transaction posting service. Reads: DALYTRAN, XREFFILE, TCATBALF. Writes: TRANSACT, ACCTFILE, DALYREJS |
| `CBTRN03C.cbl` | Batch | Replace batch reporting with modern reporting service. Reads: TRANSACT, XREFFILE, TRANTYPE, TRANCATG |
| `CBACT04C.cbl` | Batch | Replace interest calculation with modern service. Reads: TCATBALF, XREFFILE, DISCGRP. Updates: ACCTFILE. Writes: TRANSACT |

**VSAM files**:
- `TRANSACT`, `ACCTFILE`, `XREFFILE`, `TCATBALF`, `DISCGRP`, `TRANTYPE`, `TRANCATG`, `DALYTRAN`, `DALYREJS`

**XCTL edges cut**:
- `COMEN01C --> COTRN00C/01C/02C/CORPT00C/COBIL00C`: Replace with transaction routes

**Why fifth**: Transaction processing has the **widest file fan-out** (touches TRANSACT, ACCTDAT, CXACAIX, TCATBALF, DALYTRAN, etc.) and depends on Account (Phase 3) and Card (Phase 4) data being available via APIs. Must migrate after Account and Card so the Transaction service can call their APIs instead of reading VSAM directly.

**COBIL00C special case**: Bill Payment is a cross-domain operation (Account + Transaction). It reads CXACAIX (Account domain), reads/updates ACCTDAT (Account domain), and reads/writes TRANSACT (Transaction domain). By Phase 5, both Account and Card APIs exist, so Bill Payment can be implemented as an orchestrating service that calls both.

---

### Phase 6: Statements & Reports (Batch)

**Goal**: Migrate the final batch programs for statement generation.

| Symbol | Type | Change Description |
|--------|------|--------------------|
| `CBSTM03A.CBL` | Batch | Replace statement generation driver with modern report-generation service. Uses `ALTER` for dynamic flow control (refactor to structured logic). Outputs HTML + text statements |
| `CBSTM03B.CBL` | Batch | Replace file I/O subroutine with data-access calls to Account, Customer, Transaction, and Card-Xref APIs |
| `COSTM01.CPY` | Copybook | Map `TRNX-RECORD` to transaction report DTO |

**VSAM files accessed (via CBSTM03B)**:
- `TRNXFILE` (TRANSACT) &mdash; READ
- `XREFFILE` (CARDXREF) &mdash; READ
- `CUSTFILE` (CUSTDATA) &mdash; READ
- `ACCTFILE` (ACCTDATA) &mdash; READ

**Why last**: Statement generation is a **read-only consumer** of every other domain's data. It depends on Account (Phase 3), Card (Phase 4 for card-xref), and Transaction (Phase 5) all being available. It has **zero upstream dependents** &mdash; nothing calls it or depends on its output for online processing. This makes it the perfect final migration target.

**Note on `ALTER` statement**: `CBSTM03A` uses the `ALTER` verb (lines 300-309) for dynamic paragraph routing, which is a legacy pattern with no modern equivalent. This must be refactored to structured `EVALUATE` or method dispatch during migration.

---

## 7. VSAM File Retirement Schedule

| Phase | VSAM File(s) Retired from Online | Still Used By (Batch) |
|-------|----------------------------------|----------------------|
| Phase 1 | `USRSEC` (read only for auth) | &mdash; |
| Phase 2 | `USRSEC` **fully retired** | &mdash; |
| Phase 3 | `ACCTDAT`, `CUSTDAT`, `CXACAIX` (online retired) | CBTRN02C, CBACT04C, CBSTM03B |
| Phase 4 | `CARDDAT` (online retired) | CBTRN01C |
| Phase 5 | `TRANSACT`, `TCATBALF` (online + batch retired) | CBSTM03B |
| Phase 6 | All remaining files fully retired | &mdash; |

---

## 8. Risk Matrix & Cross-Phase Dependencies

| Risk | Description | Mitigation |
|------|-------------|------------|
| **COMMAREA contract** | All 17 online programs share `COCOM01Y` COMMAREA structure | Phase 0 establishes session context DTO; ACL translates between COMMAREA and modern context during transition |
| **COBIL00C cross-domain** | Bill Payment spans Account + Transaction domains | Defer to Phase 5 when both Account and Transaction APIs exist; implement as orchestrating service |
| **CBSTM03A `ALTER` verb** | Dynamic paragraph routing is un-translatable | Refactor to structured control flow before or during Phase 6 |
| **Batch file contention** | CLOSEFIL/OPENFIL jobs lock files for batch windows | Migrate batch to event-driven or micro-batch to eliminate file-locking requirement |
| **CXACAIX shared index** | Card-xref AIX is read by Account, Transaction, and Bill Payment programs | Establish cross-reference API in Phase 3; all subsequent phases use the API |
| **Dual-write consistency** | Writing to both VSAM and RDBMS during transition | Implement CDC reconciliation; designate source-of-truth per phase |

---

## 9. Summary: Migration Order with Justification

| Phase | Domain | Programs | Files | Justification |
|-------|--------|----------|-------|---------------|
| **0** | Foundation | Copybooks, CSUTLDTC | &mdash; | Establish shared infrastructure, session context, ACL |
| **1** | Auth | COSGN00C | USRSEC (R) | Entry point, zero downstream deps, enables modern identity |
| **2** | User Admin | COUSR00C-03C, COADM01C | USRSEC (CRUD) | Only depends on Auth; fully retires USRSEC |
| **3** | Account | COACTVWC, COACTUPC | ACCTDAT, CUSTDAT, CXACAIX | Provides Account API for downstream Card and Transaction |
| **4** | Card | COCRDLIC, COCRDSLC, COCRDUPC | CARDDAT | Self-contained after Account; no Transaction dependency |
| **5** | Transaction | COTRN00-02C, COBIL00C, CORPT00C, CBTRN01-03C, CBACT04C | TRANSACT, TCATBALF, DALYTRAN, DISCGRP, TRANTYPE, TRANCATG | Widest fan-out; depends on Account + Card APIs |
| **6** | Statements | CBSTM03A, CBSTM03B | All (read-only) | Pure consumer of all other domains; zero upstream deps |

---

## Appendix A: COMMAREA Structure (`COCOM01Y.cpy`)

```cobol
01 CARDDEMO-COMMAREA.
   05 CDEMO-GENERAL-INFO.
      10 CDEMO-FROM-TRANID         PIC X(04).   -- Source transaction ID
      10 CDEMO-FROM-PROGRAM        PIC X(08).   -- Source program name
      10 CDEMO-TO-TRANID           PIC X(04).   -- Target transaction ID
      10 CDEMO-TO-PROGRAM          PIC X(08).   -- Target program name
      10 CDEMO-USER-ID             PIC X(08).   -- Authenticated user
      10 CDEMO-USER-TYPE           PIC X(01).   -- 'A'=Admin, 'U'=User
      10 CDEMO-PGM-CONTEXT        PIC 9(01).   -- 0=Enter, 1=Reenter
   05 CDEMO-CUSTOMER-INFO.
      10 CDEMO-CUST-ID             PIC 9(09).
      10 CDEMO-CUST-FNAME          PIC X(25).
      10 CDEMO-CUST-MNAME          PIC X(25).
      10 CDEMO-CUST-LNAME          PIC X(25).
   05 CDEMO-ACCOUNT-INFO.
      10 CDEMO-ACCT-ID             PIC 9(11).
      10 CDEMO-ACCT-STATUS         PIC X(01).
   05 CDEMO-CARD-INFO.
      10 CDEMO-CARD-NUM            PIC 9(16).
   05 CDEMO-MORE-INFO.
      10 CDEMO-LAST-MAP            PIC X(7).
      10 CDEMO-LAST-MAPSET         PIC X(7).
```

## Appendix B: Menu Option &rarr; Program Mapping

### Regular User Menu (`COMEN02Y.cpy`)

| # | Label | Program | User Type |
|---|-------|---------|-----------|
| 1 | Account View | `COACTVWC` | U |
| 2 | Account Update | `COACTUPC` | U |
| 3 | Credit Card List | `COCRDLIC` | U |
| 4 | Credit Card View | `COCRDSLC` | U |
| 5 | Credit Card Update | `COCRDUPC` | U |
| 6 | Transaction List | `COTRN00C` | U |
| 7 | Transaction View | `COTRN01C` | U |
| 8 | Transaction Add | `COTRN02C` | U |
| 9 | Transaction Reports | `CORPT00C` | U |
| 10 | Bill Payment | `COBIL00C` | U |

### Admin Menu (`COADM02Y.cpy`)

| # | Label | Program |
|---|-------|---------|
| 1 | User List (Security) | `COUSR00C` |
| 2 | User Add (Security) | `COUSR01C` |
| 3 | User Update (Security) | `COUSR02C` |
| 4 | User Delete (Security) | `COUSR03C` |
