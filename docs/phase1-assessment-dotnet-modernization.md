# CardDemo .NET Modernization - Phase 1: Assessment & Foundation

| Field | Value |
|---|---|
| **Application** | CardDemo - Mainframe Credit Card Management System |
| **Repository** | `ankehao-demo/aws-mainframe-modernization-carddemo` |
| **Assessment Date** | 2026-02-19 |
| **Target Platform** | .NET 8 / ASP.NET Core |
| **Phase** | 1 - Assessment & Foundation |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Application Overview](#2-application-overview)
3. [Component Inventory](#3-component-inventory)
   - 3.1 [COBOL Programs](#31-cobol-programs)
   - 3.2 [CICS Transactions](#32-cics-transactions)
   - 3.3 [BMS Maps](#33-bms-maps)
   - 3.4 [VSAM Data Files](#34-vsam-data-files)
   - 3.5 [Copybooks (Data Structures)](#35-copybooks-data-structures)
   - 3.6 [JCL Batch Jobs](#36-jcl-batch-jobs)
   - 3.7 [CSD Resource Definitions](#37-csd-resource-definitions)
   - 3.8 [Security (RACF)](#38-security-racf)
   - 3.9 [Assembler Programs](#39-assembler-programs)
   - 3.10 [Optional Modules](#310-optional-modules)
4. [Data Model Analysis](#4-data-model-analysis)
5. [.NET Target Architecture](#5-net-target-architecture)
6. [Technology Mapping](#6-technology-mapping)
7. [Database Schema Design](#7-database-schema-design)
8. [Risk Assessment](#8-risk-assessment)
9. [Tooling & Framework Recommendations](#9-tooling--framework-recommendations)
10. [Migration Roadmap](#10-migration-roadmap)

---

## 1. Executive Summary

CardDemo is a mainframe credit card management application built with COBOL, CICS, VSAM, JCL, and RACF. It provides online transaction processing for account management, credit card operations, transaction handling, bill payment, and user administration, along with batch processing for transaction posting, interest calculation, and statement generation.

**Scope Summary:**

| Category | Count |
|---|---|
| COBOL Programs (Online) | 16 |
| COBOL Programs (Batch) | 7 |
| COBOL Utility Programs | 1 |
| CICS Transactions | 17 |
| BMS Mapsets | 17 |
| VSAM KSDS Clusters | 7 |
| VSAM Alternate Indexes | 2 |
| Copybooks | 28 |
| JCL Jobs | 29 |
| Data Files (Flat + VSAM) | 12 |
| Optional Modules (Planned) | 3 |

**Estimated Complexity:** Medium-High. The core application is well-structured with clear separation between online (CICS) and batch processing. The pseudo-conversational CICS model, VSAM indexed file access patterns, and batch job orchestration require careful mapping to .NET equivalents.

---

## 2. Application Overview

### 2.1 Core Technologies

| Technology | Role |
|---|---|
| COBOL | Primary programming language for all business logic |
| CICS | Online transaction processing (pseudo-conversational model) |
| VSAM (KSDS + AIX) | Primary data storage using Key-Sequenced Data Sets with Alternate Indexes |
| JCL | Batch job control and orchestration |
| RACF | Security and access control |
| BMS | Basic Mapping Support for 3270 screen definitions |

### 2.2 User Roles

| Role | Entry Transaction | Menu Program | Capabilities |
|---|---|---|---|
| Regular User | CC00 | COMEN01C | Account view/update, credit card operations, transactions, reports, bill payment |
| Admin User | CC00 | COADM01C | User list, add, update, delete (security management) |

### 2.3 Application Flow

**User Flow:** Signon (CC00) -> Main Menu (CM00) -> Account View/Update, Credit Card List/View/Update, Transaction List/View/Add, Reports, Bill Payment

**Admin Flow:** Signon (CC00) -> Admin Menu (CA00) -> User List/Add/Update/Delete

**Batch Flow:** Close CICS files -> Load data -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> Reopen CICS files

---

## 3. Component Inventory

### 3.1 COBOL Programs

#### 3.1.1 Online Programs (CICS)

| # | Program | Source File | Function | Transaction | BMS Map | LOC |
|---|---|---|---|---|---|---|
| 1 | COSGN00C | `app/cbl/COSGN00C.cbl` | Signon Screen | CC00 | COSGN00 | 261 |
| 2 | COMEN01C | `app/cbl/COMEN01C.cbl` | Main Menu | CM00 | COMEN01 | ~300 |
| 3 | COADM01C | `app/cbl/COADM01C.cbl` | Admin Menu | CA00 | COADM01 | ~250 |
| 4 | COACTVWC | `app/cbl/COACTVWC.cbl` | Account View | CAVW | COACTVW | ~400 |
| 5 | COACTUPC | `app/cbl/COACTUPC.cbl` | Account Update | CAUP | COACTUP | ~500 |
| 6 | COCRDLIC | `app/cbl/COCRDLIC.cbl` | Credit Card List | CCLI | COCRDLI | ~400 |
| 7 | COCRDSLC | `app/cbl/COCRDSLC.cbl` | Credit Card View | CCDL | COCRDSL | ~350 |
| 8 | COCRDUPC | `app/cbl/COCRDUPC.cbl` | Credit Card Update | CCUP | COCRDUP | ~500 |
| 9 | COTRN00C | `app/cbl/COTRN00C.cbl` | Transaction List | CT00 | COTRN00 | ~400 |
| 10 | COTRN01C | `app/cbl/COTRN01C.cbl` | Transaction View | CT01 | COTRN01 | ~350 |
| 11 | COTRN02C | `app/cbl/COTRN02C.cbl` | Transaction Add | CT02 | COTRN02 | ~400 |
| 12 | CORPT00C | `app/cbl/CORPT00C.cbl` | Transaction Reports | CR00 | CORPT00 | ~350 |
| 13 | COBIL00C | `app/cbl/COBIL00C.cbl` | Bill Payment | CB00 | COBIL00 | ~400 |
| 14 | COUSR00C | `app/cbl/COUSR00C.cbl` | List Users (Admin) | CU00 | COUSR00 | ~350 |
| 15 | COUSR01C | `app/cbl/COUSR01C.cbl` | Add User (Admin) | CU01 | COUSR01 | ~300 |
| 16 | COUSR02C | `app/cbl/COUSR02C.cbl` | Update User (Admin) | CU02 | COUSR02 | ~350 |
| 17 | COUSR03C | `app/cbl/COUSR03C.cbl` | Delete User (Admin) | CU03 | COUSR03 | ~300 |

**CICS API Usage Patterns Identified:**
- `EXEC CICS SEND MAP` / `EXEC CICS RECEIVE MAP` - Screen I/O
- `EXEC CICS READ DATASET` / `REWRITE` / `WRITE` / `DELETE` - VSAM file operations
- `EXEC CICS STARTBR` / `READNEXT` / `ENDBR` - VSAM browse operations
- `EXEC CICS XCTL` - Transfer control between programs
- `EXEC CICS RETURN TRANSID` - Pseudo-conversational return
- `EXEC CICS ASSIGN` - System information retrieval
- COMMAREA-based state passing between transactions

#### 3.1.2 Batch Programs

| # | Program | Source File | Function | LOC |
|---|---|---|---|---|
| 1 | CBTRN02C | `app/cbl/CBTRN02C.cbl` | Post daily transactions to master file | 732 |
| 2 | CBACT04C | `app/cbl/CBACT04C.cbl` | Interest calculation on transaction balances | 653 |
| 3 | CBSTM03A | `app/cbl/CBSTM03A.CBL` | Generate account statements (text + HTML) | 924 |
| 4 | CBSTM03B | `app/cbl/CBSTM03B.CBL` | Subroutine for CBSTM03A (file I/O helper) | ~300 |
| 5 | CBTRN01C | `app/cbl/CBTRN01C.cbl` | Transaction report generation | ~400 |
| 6 | CBTRN03C | `app/cbl/CBTRN03C.cbl` | Transaction data processing | ~350 |
| 7 | CBACT01C | `app/cbl/CBACT01C.cbl` | Account data processing | ~300 |
| 8 | CBACT02C | `app/cbl/CBACT02C.cbl` | Account data processing | ~300 |
| 9 | CBACT03C | `app/cbl/CBACT03C.cbl` | Account data processing | ~300 |
| 10 | CBCUS01C | `app/cbl/CBCUS01C.cbl` | Customer data processing | ~300 |

**Batch Processing Patterns Identified:**
- Sequential file reading (DALYTRAN)
- Indexed file random access (VSAM KSDS)
- Alternate index access (XREF by account ID)
- File validation and reject handling
- Account balance updates
- Interest rate computation using disclosure groups
- Statement generation with CALL to subroutine (CBSTM03B)
- PSA/TCB/TIOT control block addressing (CBSTM03A)
- ALTER/GO TO flow control (CBSTM03A)
- COMP and COMP-3 variables

#### 3.1.3 Utility Programs

| # | Program | Source File | Function | LOC |
|---|---|---|---|---|
| 1 | CSUTLDTC | `app/cbl/CSUTLDTC.cbl` | Date validation using CEEDAYS API (Lillian date conversion) | 158 |

### 3.2 CICS Transactions

Defined in `app/csd/CARDDEMO.CSD`:

| # | Transaction ID | Program | Description | Profile |
|---|---|---|---|---|
| 1 | CC00 | COSGN00C | Signon Screen (entry point) | DFHCICST |
| 2 | CM00 | COMEN01C | Main Menu | DFHCICST |
| 3 | CA00 | COADM01C | Admin Menu | DFHCICST |
| 4 | CAVW | COACTVWC | Account View | DFHCICST |
| 5 | CAUP | COACTUPC | Account Update | DFHCICST |
| 6 | CCLI | COCRDLIC | Credit Card List | DFHCICST |
| 7 | CCDL | COCRDSLC | Credit Card View | DFHCICST |
| 8 | CCUP | COCRDUPC | Credit Card Update | DFHCICST |
| 9 | CT00 | COTRN00C | Transaction List | DFHCICST |
| 10 | CT01 | COTRN01C | Transaction View | DFHCICST |
| 11 | CT02 | COTRN02C | Transaction Add | DFHCICST |
| 12 | CR00 | CORPT00C | Transaction Reports | DFHCICST |
| 13 | CB00 | COBIL00C | Bill Payment | DFHCICST |
| 14 | CU00 | COUSR00C | List Users | DFHCICST |
| 15 | CU01 | COUSR01C | Add User | DFHCICST |
| 16 | CU02 | COUSR02C | Update User | DFHCICST |
| 17 | CU03 | COUSR03C | Delete User | DFHCICST |

Additional CSD entry: `CDV1` (Developer Transaction) -> `COCRDSEC` (Credit Card Search - development/test)

### 3.3 BMS Maps

| # | Mapset | Source File | Associated Program | Screen Function |
|---|---|---|---|---|
| 1 | COSGN00 | `app/bms/COSGN00.bms` | COSGN00C | Signon |
| 2 | COMEN01 | `app/bms/COMEN01.bms` | COMEN01C | Main Menu |
| 3 | COADM01 | `app/bms/COADM01.bms` | COADM01C | Admin Menu |
| 4 | COACTVW | `app/bms/COACTVW.bms` | COACTVWC | Account View |
| 5 | COACTUP | `app/bms/COACTUP.bms` | COACTUPC | Account Update |
| 6 | COCRDLI | `app/bms/COCRDLI.bms` | COCRDLIC | Credit Card List |
| 7 | COCRDSL | `app/bms/COCRDSL.bms` | COCRDSLC | Credit Card View |
| 8 | COCRDUP | `app/bms/COCRDUP.bms` | COCRDUPC | Credit Card Update |
| 9 | COTRN00 | `app/bms/COTRN00.bms` | COTRN00C | Transaction List |
| 10 | COTRN01 | `app/bms/COTRN01.bms` | COTRN01C | Transaction View |
| 11 | COTRN02 | `app/bms/COTRN02.bms` | COTRN02C | Transaction Add |
| 12 | CORPT00 | `app/bms/CORPT00.bms` | CORPT00C | Reports |
| 13 | COBIL00 | `app/bms/COBIL00.bms` | COBIL00C | Bill Payment |
| 14 | COUSR00 | `app/bms/COUSR00.bms` | COUSR00C | User List |
| 15 | COUSR01 | `app/bms/COUSR01.bms` | COUSR01C | User Add |
| 16 | COUSR02 | `app/bms/COUSR02.bms` | COUSR02C | User Update |
| 17 | COUSR03 | `app/bms/COUSR03.bms` | COUSR03C | User Delete |

BMS-generated copybooks are in `app/cpy-bms/` with matching names.

### 3.4 VSAM Data Files

#### 3.4.1 VSAM KSDS Clusters (from CSD and LISTCAT)

| # | CSD Name | Dataset Name | Key Length | Key Offset | Record Length | Description |
|---|---|---|---|---|---|---|
| 1 | ACCTDAT | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | 11 | 0 | 300 | Account Master |
| 2 | CARDDAT | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | 16 | 0 | 150 | Card Master |
| 3 | CCXREF | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | 16 | 0 | 50 | Card-Account Cross Reference |
| 4 | CUSTDAT | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | 9 | 0 | 500 | Customer Master |
| 5 | TRANSACT | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | 16 | 0 | 350 | Transaction Master |
| 6 | USRSEC | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | 8 | 0 | 80 | User Security |
| 7 | (batch) | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | 17 | 0 | 50 | Transaction Category Balance |

#### 3.4.2 Alternate Indexes

| # | CSD Name | Dataset Name | Base Cluster | Purpose |
|---|---|---|---|---|
| 1 | CARDAIX | `AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH` | CARDDATA KSDS | Card lookup by alternate key |
| 2 | CXACAIX | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH` | CARDXREF KSDS | Cross-reference lookup by Account ID |

#### 3.4.3 Flat File Datasets (Input/Seed Data)

| # | Dataset Name | Description | Copybook | Format | LRECL |
|---|---|---|---|---|---|
| 1 | `AWS.M2.CARDDEMO.USRSEC.PS` | User Security seed data | CSUSR01Y | FB | 80 |
| 2 | `AWS.M2.CARDDEMO.ACCTDATA.PS` | Account seed data | CVACT01Y | FB | 300 |
| 3 | `AWS.M2.CARDDEMO.CARDDATA.PS` | Card seed data | CVACT02Y | FB | 150 |
| 4 | `AWS.M2.CARDDEMO.CUSTDATA.PS` | Customer seed data | CVCUS01Y | FB | 500 |
| 5 | `AWS.M2.CARDDEMO.CARDXREF.PS` | Card-Account-Customer cross reference | CVACT03Y | FB | 50 |
| 6 | `AWS.M2.CARDDEMO.DALYTRAN.PS.INIT` | Transaction DB initialization | CVTRA06Y | FB | 350 |
| 7 | `AWS.M2.CARDDEMO.DALYTRAN.PS` | Daily transactions for posting | CVTRA06Y | FB | 350 |
| 8 | `AWS.M2.CARDDEMO.DISCGRP.PS` | Disclosure Groups (interest rates) | CVTRA02Y | FB | 50 |
| 9 | `AWS.M2.CARDDEMO.TRANCATG.PS` | Transaction Category Types | CVTRA04Y | FB | 60 |
| 10 | `AWS.M2.CARDDEMO.TRANTYPE.PS` | Transaction Types | CVTRA03Y | FB | 60 |
| 11 | `AWS.M2.CARDDEMO.TCATBALF.PS` | Transaction Category Balance | CVTRA01Y | FB | 50 |

ASCII sample data files in `app/data/ASCII/`: `acctdata.txt`, `carddata.txt`, `cardxref.txt`, `custdata.txt`, `dailytran.txt`, `discgrp.txt`, `tcatbal.txt`, `trancatg.txt`, `trantype.txt`

### 3.5 Copybooks (Data Structures)

#### 3.5.1 Entity/Record Copybooks

| # | Copybook | Source File | Record Name | LRECL | Description |
|---|---|---|---|---|---|
| 1 | CVACT01Y | `app/cpy/CVACT01Y.cpy` | ACCOUNT-RECORD | 300 | Account entity |
| 2 | CVACT02Y | `app/cpy/CVACT02Y.cpy` | CARD-RECORD | 150 | Card entity |
| 3 | CVACT03Y | `app/cpy/CVACT03Y.cpy` | CARD-XREF-RECORD | 50 | Card-Account-Customer cross reference |
| 4 | CVCUS01Y | `app/cpy/CVCUS01Y.cpy` | CUSTOMER-RECORD | 500 | Customer entity |
| 5 | CSUSR01Y | `app/cpy/CSUSR01Y.cpy` | SEC-USER-DATA | 80 | User security record |
| 6 | CVTRA05Y | `app/cpy/CVTRA05Y.cpy` | TRAN-RECORD | 350 | Online transaction record |
| 7 | CVTRA06Y | `app/cpy/CVTRA06Y.cpy` | DALYTRAN-RECORD | 350 | Daily transaction record |
| 8 | CVTRA01Y | `app/cpy/CVTRA01Y.cpy` | TRAN-CAT-BAL-RECORD | 50 | Transaction category balance |
| 9 | CVTRA02Y | `app/cpy/CVTRA02Y.cpy` | DIS-GROUP-RECORD | 50 | Disclosure group (interest rates) |
| 10 | CVTRA03Y | `app/cpy/CVTRA03Y.cpy` | TRAN-TYPE-RECORD | 60 | Transaction type |
| 11 | CVTRA04Y | `app/cpy/CVTRA04Y.cpy` | TRAN-CAT-RECORD | 60 | Transaction category type |
| 12 | COSTM01 | `app/cpy/COSTM01.CPY` | TRNX-RECORD | 350 | Transaction record (reporting layout, key=card+tranID) |
| 13 | CUSTREC | `app/cpy/CUSTREC.cpy` | (Customer record) | - | Customer record variant |

#### 3.5.2 Application Framework Copybooks

| # | Copybook | Source File | Description |
|---|---|---|---|
| 14 | COCOM01Y | `app/cpy/COCOM01Y.cpy` | COMMAREA structure (inter-program communication) |
| 15 | CVCRD01Y | `app/cpy/CVCRD01Y.cpy` | Credit card work areas (AID keys, navigation, error messages) |
| 16 | COMEN02Y | `app/cpy/COMEN02Y.cpy` | Main menu options (10 options with program names and user types) |
| 17 | COADM02Y | `app/cpy/COADM02Y.cpy` | Admin menu options (4 options with program names) |
| 18 | CSDAT01Y | `app/cpy/CSDAT01Y.cpy` | Date/time working storage structures |
| 19 | COTTL01Y | `app/cpy/COTTL01Y.cpy` | Screen title constants |
| 20 | CSMSG01Y | `app/cpy/CSMSG01Y.cpy` | Application messages |
| 21 | CSMSG02Y | `app/cpy/CSMSG02Y.cpy` | Additional messages |
| 22 | CVTRA07Y | `app/cpy/CVTRA07Y.cpy` | Transaction report layout (headers, detail, totals) |
| 23 | CSSETATY | `app/cpy/CSSETATY.cpy` | Screen attribute settings |
| 24 | CSSTRPFY | `app/cpy/CSSTRPFY.cpy` | String processing functions |
| 25 | CSLKPCDY | `app/cpy/CSLKPCDY.cpy` | Lookup code definitions |
| 26 | CSUTLDPY | `app/cpy/CSUTLDPY.cpy` | Utility display parameters |
| 27 | CSUTLDWY | `app/cpy/CSUTLDWY.cpy` | Utility working storage |
| 28 | UNUSED1Y | `app/cpy/UNUSED1Y.cpy` | Unused/placeholder |

#### 3.5.3 BMS-Generated Copybooks

Located in `app/cpy-bms/`: COSGN00.CPY, COMEN01.CPY, COADM01.CPY, COACTVW.CPY, COACTUP.CPY, COCRDLI.CPY, COCRDSL.CPY, COCRDUP.CPY, COTRN00.CPY, COTRN01.CPY, COTRN02.CPY, CORPT00.CPY, COBIL00.CPY, COUSR00.CPY, COUSR01.CPY, COUSR02.CPY, COUSR03.CPY

### 3.6 JCL Batch Jobs

#### 3.6.1 Data Load/Refresh Jobs (IDCAMS-based)

| # | Job Name | Source File | Program | Function |
|---|---|---|---|---|
| 1 | DUSRSECJ | `app/jcl/DUSRSECJ.jcl` | IEBGENER | Initial load of user security file |
| 2 | DEFGDGB | `app/jcl/DEFGDGB.jcl` | IDCAMS | Setup GDG bases for versioned datasets |
| 3 | ACCTFILE | `app/jcl/ACCTFILE.jcl` | IDCAMS | Refresh account master VSAM |
| 4 | CARDFILE | `app/jcl/CARDFILE.jcl` | IDCAMS | Refresh card master VSAM |
| 5 | CUSTFILE | `app/jcl/CUSTFILE.jcl` | IDCAMS | Refresh customer master VSAM |
| 6 | XREFFILE | `app/jcl/XREFFILE.jcl` | IDCAMS | Load card-account cross reference |
| 7 | TRANFILE | `app/jcl/TRANFILE.jcl` | IDCAMS | Load transaction master |
| 8 | DISCGRP | `app/jcl/DISCGRP.jcl` | IDCAMS | Load disclosure group file |
| 9 | TCATBALF | `app/jcl/TCATBALF.jcl` | IDCAMS | Refresh transaction category balance |
| 10 | TRANCATG | `app/jcl/TRANCATG.jcl` | IDCAMS | Load transaction category types |
| 11 | TRANTYPE | `app/jcl/TRANTYPE.jcl` | IDCAMS | Load transaction types |
| 12 | TRANBKP | `app/jcl/TRANBKP.jcl` | IDCAMS | Backup/refresh transaction master |
| 13 | TRANIDX | `app/jcl/TRANIDX.jcl` | IDCAMS | Define alternate index on transaction file |

#### 3.6.2 CICS File Control Jobs

| # | Job Name | Source File | Program | Function |
|---|---|---|---|---|
| 14 | CLOSEFIL | `app/jcl/CLOSEFIL.jcl` | IEFBR14 | Close VSAM files in CICS |
| 15 | OPENFIL | `app/jcl/OPENFIL.jcl` | IEFBR14 | Open files in CICS |

#### 3.6.3 Business Processing Jobs

| # | Job Name | Source File | Program | Function |
|---|---|---|---|---|
| 16 | POSTTRAN | `app/jcl/POSTTRAN.jcl` | CBTRN02C | Post daily transactions, validate, update balances |
| 17 | INTCALC | `app/jcl/INTCALC.jcl` | CBACT04C | Compute interest and fees on transaction balances |
| 18 | COMBTRAN | `app/jcl/COMBTRAN.jcl` | SORT + IDCAMS | Sort and combine system + daily transactions |
| 19 | CREASTMT | `app/jcl/CREASTMT.JCL` | SORT + CBSTM03A | Generate account statements (text + HTML) |

#### 3.6.4 Utility/Diagnostic Jobs

| # | Job Name | Source File | Program | Function |
|---|---|---|---|---|
| 20 | READACCT | `app/jcl/READACCT.jcl` | (utility) | Read account data |
| 21 | READCARD | `app/jcl/READCARD.jcl` | (utility) | Read card data |
| 22 | READCUST | `app/jcl/READCUST.jcl` | (utility) | Read customer data |
| 23 | READXREF | `app/jcl/READXREF.jcl` | (utility) | Read cross-reference data |
| 24 | DALYREJS | `app/jcl/DALYREJS.jcl` | (utility) | Daily rejects processing |
| 25 | DEFCUST | `app/jcl/DEFCUST.jcl` | (utility) | Define customer VSAM |
| 26 | CBADMCDJ | `app/jcl/CBADMCDJ.jcl` | (utility) | Admin card processing |
| 27 | PRTCATBL | `app/jcl/PRTCATBL.jcl` | (utility) | Print category balance |
| 28 | REPTFILE | `app/jcl/REPTFILE.jcl` | (utility) | Report file processing |
| 29 | TRANREPT | `app/jcl/TRANREPT.jcl` | (utility) | Transaction report |

#### 3.6.5 Batch Execution Sequence

```
1. CLOSEFIL    - Close CICS files
2. ACCTFILE    - Refresh account master
3. CARDFILE    - Refresh card master
4. XREFFILE    - Load cross-reference
5. CUSTFILE    - Refresh customer master
6. TRANBKP     - Backup transaction master
7. DISCGRP     - Load disclosure groups
8. TCATBALF    - Load category balances
9. TRANTYPE    - Load transaction types
10. DUSRSECJ   - Load user security
11. POSTTRAN   - Core transaction posting
12. INTCALC    - Interest calculations
13. TRANBKP    - Backup updated transactions
14. COMBTRAN   - Combine transaction files
15. CREASTMT   - Generate statements
16. TRANIDX    - Define alternate index
17. OPENFIL    - Reopen CICS files
```

### 3.7 CSD Resource Definitions

From `app/csd/CARDDEMO.CSD` (506 lines):

| Resource Type | Count | Details |
|---|---|---|
| FILE definitions | 7 | ACCTDAT, CARDAIX, CARDDAT, CCXREF, CUSTDAT, CXACAIX, TRANSACT, USRSEC |
| MAPSET definitions | 17 | One per screen |
| PROGRAM definitions | 18 | 17 online + COCRDSEC (dev) |
| TRANSACTION definitions | 17 | CC00, CM00, CA00, CAVW, CAUP, CCLI, CCDL, CCUP, CT00, CT01, CT02, CR00, CB00, CU00, CU01, CU02, CU03 + CDV1 |
| LIBRARY definitions | 2 | CARDDLIB, COM2DOLL |
| TDQUEUE definitions | 1 | JOBS (for submitting batch from CICS) |

### 3.8 Security (RACF)

The application implements its own security layer:
- **User authentication**: VSAM file `USRSEC` stores user credentials (ID, first name, last name, password, user type)
- **User types**: 'A' (Admin) and 'U' (Regular User) - defined in COMMAREA field `CDEMO-USER-TYPE`
- **Password storage**: Plain text in VSAM record (8 characters)
- **Authorization**: Menu-level access control based on user type
- **RACF integration**: CSD shows `RACF(NO)` on all file definitions - RACF is not enforced at the VSAM level

### 3.9 Assembler Programs

Referenced in the task but **not present in the repository**:
- **MVSWAIT**: Timer control utility (system-level wait)
- **COBDATFT**: Date format conversion utility

These are likely system-provided utilities on the mainframe and would need .NET equivalents.

### 3.10 Optional Modules

These modules are referenced in the task description but are **not present in this repository fork**. They represent planned extensions:

#### 3.10.1 Credit Card Authorizations (IMS, DB2, MQ)

**Location:** `app/app-authorization-ims-db2-mq/` (not present)

| Program | Function |
|---|---|
| COPAUA0C | MQ-triggered authorization processor |
| COPAUS0C | Authorization summary display |
| COPAUS1C | Authorization detail display |
| COPAUS2C | Fraud marking |
| CBPAUP0C | Batch purge of authorization data |

**Technologies:**
- IBM MQ for request/response messaging (CSV format)
- IMS database segments: PAUTSUM0 (root), PAUTDTL1 (child)
- DB2 table AUTHFRDS for fraud logging
- Business rules: credit limit checks, fraud detection

#### 3.10.2 Transaction Type Management (DB2)

**Location:** `app/app-transaction-type-db2/` (not present)

- DB2 tables: `CARDDEMO.TRANSACTION_TYPE`, `CARDDEMO.TRANSACTION_TYPE_CATEGORY`
- CICS transactions for CRUD operations
- Batch job management

#### 3.10.3 Account Extractions (MQ, VSAM)

- MQ channel data transmission
- CDRD transaction (system date inquiry)
- CDRA transaction (account details inquiry)

---

## 4. Data Model Analysis

### 4.1 Entity Relationship Summary

```
                    ┌──────────────┐
                    │   CUSTOMER   │
                    │  (CVCUS01Y)  │
                    │  PK: CUST-ID │
                    └──────┬───────┘
                           │ 1:N
                    ┌──────┴───────┐
                    │  CARD-XREF   │
                    │  (CVACT03Y)  │
                    │ PK: CARD-NUM │
                    │ FK: CUST-ID  │
                    │ FK: ACCT-ID  │
                    └──┬───────┬───┘
                  1:1  │       │ 1:1
           ┌───────────┘       └───────────┐
    ┌──────┴───────┐              ┌────────┴──────┐
    │    CARD      │              │    ACCOUNT    │
    │  (CVACT02Y)  │              │   (CVACT01Y)  │
    │ PK: CARD-NUM │              │  PK: ACCT-ID  │
    │ FK: ACCT-ID  │              │ FK: GROUP-ID  │
    └──────┬───────┘              └───────┬───────┘
           │ 1:N                          │ 1:N
    ┌──────┴───────┐              ┌───────┴────────┐
    │ TRANSACTION  │              │ TRAN-CAT-BAL   │
    │  (CVTRA05Y)  │              │   (CVTRA01Y)   │
    │ PK: TRAN-ID  │              │ PK: ACCT-ID +  │
    │ FK: CARD-NUM │              │     TYPE-CD +   │
    └──────────────┘              │     CAT-CD      │
                                  └───────┬────────┘
                                          │ N:1
                                  ┌───────┴────────┐
                                  │  DISCL-GROUP   │
                                  │   (CVTRA02Y)   │
                                  │ PK: GROUP-ID + │
                                  │     TYPE-CD +  │
                                  │     CAT-CD     │
                                  └────────────────┘

    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
    │  TRAN-TYPE   │    │  TRAN-CAT    │    │   USER-SEC   │
    │  (CVTRA03Y)  │    │  (CVTRA04Y)  │    │  (CSUSR01Y)  │
    │ PK: TYPE-CD  │    │ PK: TYPE-CD  │    │ PK: USR-ID   │
    └──────────────┘    │   + CAT-CD   │    └──────────────┘
                        └──────────────┘
```

### 4.2 Key Field Analysis

| Entity | Primary Key | Key Type | Key Length | Notes |
|---|---|---|---|---|
| Account | ACCT-ID | Numeric | 11 | PIC 9(11) |
| Card | CARD-NUM | Alphanumeric | 16 | PIC X(16) |
| Customer | CUST-ID | Numeric | 9 | PIC 9(09) |
| Card Cross-Ref | XREF-CARD-NUM | Alphanumeric | 16 | PIC X(16), with AIX on ACCT-ID |
| Transaction | TRAN-ID | Alphanumeric | 16 | PIC X(16) |
| User Security | SEC-USR-ID | Alphanumeric | 8 | PIC X(08) |
| Tran Cat Balance | ACCT-ID + TYPE-CD + CAT-CD | Composite | 17 | 11 + 2 + 4 |
| Disclosure Group | GROUP-ID + TYPE-CD + CAT-CD | Composite | 16 | 10 + 2 + 4 |
| Transaction Type | TRAN-TYPE | Alphanumeric | 2 | PIC X(02) |
| Transaction Category | TYPE-CD + CAT-CD | Composite | 6 | 2 + 4 |

---

## 5. .NET Target Architecture

### 5.1 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────────┐ │
│  │  Blazor Web  │  │  REST Client │  │  Swagger / OpenAPI UI      │ │
│  │  Application │  │  (Postman)   │  │                            │ │
│  └──────┬──────┘  └──────┬───────┘  └────────────┬───────────────┘ │
└─────────┼────────────────┼───────────────────────┼─────────────────┘
          │                │                       │
          ▼                ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     WEB API LAYER (ASP.NET Core 8)                   │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    API Controllers                            │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────┐ │   │
│  │  │ Auth       │ │ Account    │ │ Card       │ │ Transact │ │   │
│  │  │ Controller │ │ Controller │ │ Controller │ │ Contrlr  │ │   │
│  │  └────────────┘ └────────────┘ └────────────┘ └──────────┘ │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐              │   │
│  │  │ Report     │ │ BillPay    │ │ UserAdmin  │              │   │
│  │  │ Controller │ │ Controller │ │ Controller │              │   │
│  │  └────────────┘ └────────────┘ └────────────┘              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────────┐  ┌────────────────────────────────────┐   │
│  │  Middleware           │  │  Cross-Cutting Concerns            │   │
│  │  - Authentication     │  │  - Logging (Serilog)               │   │
│  │  - Authorization      │  │  - Exception Handling              │   │
│  │  - Request Validation │  │  - Health Checks                   │   │
│  └──────────────────────┘  └────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SERVICE / BUSINESS LOGIC LAYER                     │
│                                                                      │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────────┐ │
│  │ Account    │ │ Card       │ │ Transaction│ │ Authorization    │ │
│  │ Service    │ │ Service    │ │ Service    │ │ Service          │ │
│  └────────────┘ └────────────┘ └────────────┘ └──────────────────┘ │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────────┐ │
│  │ Report     │ │ BillPay    │ │ UserAdmin  │ │ Interest Calc    │ │
│  │ Service    │ │ Service    │ │ Service    │ │ Service          │ │
│  └────────────┘ └────────────┘ └────────────┘ └──────────────────┘ │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Domain Models / DTOs / Validators (FluentValidation)         │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                                  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Entity Framework Core 8 (Code-First)                        │   │
│  │  - DbContext: CardDemoDbContext                               │   │
│  │  - Entities: Account, Card, Customer, Transaction, etc.      │   │
│  │  - Configurations: IEntityTypeConfiguration<T>               │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Repository Pattern (optional, for complex queries)          │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                                     │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  SQL Server / PostgreSQL                                     │   │
│  │  - Accounts, Cards, Customers, Transactions tables           │   │
│  │  - CardXref, TransactionCategoryBalance, DisclosureGroup     │   │
│  │  - TransactionType, TransactionCategory, Users               │   │
│  │  - Proper indexes replacing VSAM KSDS keys and AIX           │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    BACKGROUND PROCESSING LAYER                       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  .NET Background Services / Hangfire                         │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │   │
│  │  │ PostTran Job │ │ IntCalc Job  │ │ Statement Gen Job    │ │   │
│  │  │ (CBTRN02C)   │ │ (CBACT04C)   │ │ (CBSTM03A)           │ │   │
│  │  └──────────────┘ └──────────────┘ └──────────────────────┘ │   │
│  │  ┌──────────────┐                                            │   │
│  │  │ CombTran Job │                                            │   │
│  │  │ (SORT merge) │                                            │   │
│  │  └──────────────┘                                            │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    MESSAGING LAYER (Future - Optional Modules)        │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Azure Service Bus / RabbitMQ                                │   │
│  │  - Authorization request/response queues                     │   │
│  │  - Account extraction messages                               │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 API Endpoint Design

Replacing CICS transactions with RESTful endpoints:

| CICS Transaction | HTTP Method | API Endpoint | Description |
|---|---|---|---|
| CC00 (Signon) | POST | `/api/auth/login` | User authentication |
| - | POST | `/api/auth/logout` | Session termination |
| CAVW (Account View) | GET | `/api/accounts/{id}` | Get account details |
| CAUP (Account Update) | PUT | `/api/accounts/{id}` | Update account |
| CCLI (Card List) | GET | `/api/cards?accountId={id}` | List cards for account |
| CCDL (Card View) | GET | `/api/cards/{cardNum}` | Get card details |
| CCUP (Card Update) | PUT | `/api/cards/{cardNum}` | Update card |
| CT00 (Transaction List) | GET | `/api/transactions?accountId={id}` | List transactions |
| CT01 (Transaction View) | GET | `/api/transactions/{id}` | Get transaction details |
| CT02 (Transaction Add) | POST | `/api/transactions` | Create transaction |
| CR00 (Reports) | GET | `/api/reports/transactions` | Generate transaction report |
| CB00 (Bill Payment) | POST | `/api/payments` | Process bill payment |
| CU00 (User List) | GET | `/api/admin/users` | List users (admin) |
| CU01 (User Add) | POST | `/api/admin/users` | Create user (admin) |
| CU02 (User Update) | PUT | `/api/admin/users/{id}` | Update user (admin) |
| CU03 (User Delete) | DELETE | `/api/admin/users/{id}` | Delete user (admin) |

### 5.3 Authentication & Authorization

| Mainframe | .NET Equivalent |
|---|---|
| USRSEC VSAM file | ASP.NET Core Identity with SQL Server |
| Plain-text password in VSAM | Bcrypt/PBKDF2 hashed passwords |
| COMMAREA user type flag | JWT claims-based authorization |
| User type 'A'/'U' | Role-based: `[Authorize(Roles = "Admin")]` / `[Authorize(Roles = "User")]` |
| CICS pseudo-conversational state | Stateless JWT tokens |

---

## 6. Technology Mapping

### 6.1 Core Technology Mapping

| # | Mainframe Component | .NET Equivalent | Notes |
|---|---|---|---|
| 1 | COBOL | C# 12 | Business logic rewritten in C# |
| 2 | CICS Transaction Processing | ASP.NET Core Web API | RESTful stateless API |
| 3 | CICS Pseudo-Conversational Model | Stateless HTTP Request/Response | No session state on server |
| 4 | CICS COMMAREA | DTOs / Request-Response Models | Typed data transfer objects |
| 5 | CICS SEND/RECEIVE MAP | Blazor Components / React SPA | Modern web UI |
| 6 | BMS Maps (3270 screens) | Razor Pages / Blazor / React | Web-based UI |
| 7 | VSAM KSDS | SQL Server / PostgreSQL tables | Relational database with indexes |
| 8 | VSAM Alternate Index (AIX) | Database secondary indexes | `CREATE INDEX` |
| 9 | VSAM BROWSE (STARTBR/READNEXT) | LINQ queries with pagination | `Skip().Take()` |
| 10 | JCL Batch Jobs | .NET Background Services / Hangfire | Scheduled background processing |
| 11 | JCL SORT utility | LINQ `OrderBy()` / SQL `ORDER BY` | In-memory or database sorting |
| 12 | IDCAMS (REPRO, DEFINE) | EF Core Migrations / Seed Data | Database schema management |
| 13 | RACF | ASP.NET Core Identity | Authentication & authorization |
| 14 | IEBGENER (file copy) | File.Copy / EF Core bulk insert | Data loading |
| 15 | GDG (Generation Data Groups) | Database versioning / audit tables | Temporal tables or soft deletes |
| 16 | CICS XCTL (Transfer Control) | Controller routing / service calls | Dependency injection |
| 17 | CICS LINK | Method calls / service invocation | Direct service calls |
| 18 | CICS RETURN TRANSID | HTTP redirect / SPA routing | Client-side navigation |
| 19 | CICS TD Queue (JOBS) | Message queue / Hangfire enqueue | Background job submission |
| 20 | DFHAID (Attention IDs) | HTTP methods + UI events | Button clicks, form submissions |
| 21 | COMP / COMP-3 variables | `decimal` / `int` / `long` | Native .NET numeric types |
| 22 | PIC 9(n)V99 | `decimal` | Fixed-point decimal |
| 23 | PIC X(n) | `string` | Character fields |
| 24 | FILLER | (not needed) | Padding eliminated in RDBMS |
| 25 | COPY statement | `using` / class inheritance | Shared model classes |
| 26 | CEEDAYS (date validation) | `DateTime.TryParse()` | Built-in .NET date handling |
| 27 | PSA/TCB/TIOT addressing | `IHostEnvironment` / config | Environment information |
| 28 | ALTER/GO TO | Standard control flow | if/else, switch, methods |

### 6.2 Optional Module Technology Mapping

| # | Mainframe Component | .NET Equivalent | Notes |
|---|---|---|---|
| 29 | IBM MQ | Azure Service Bus / RabbitMQ | Message-driven architecture |
| 30 | IMS Database | SQL Server tables | Hierarchical data flattened to relational |
| 31 | DB2 Tables | SQL Server / PostgreSQL | Direct relational mapping |
| 32 | MQ-triggered programs | Azure Functions / Message handlers | Event-driven processing |
| 33 | IMS segments (root/child) | Parent-child tables with FK | One-to-many relationships |

---

## 7. Database Schema Design

### 7.1 Proposed SQL Server Schema

```sql
-- Account Master (from CVACT01Y, LRECL=300)
CREATE TABLE Accounts (
    AccountId           BIGINT          NOT NULL PRIMARY KEY,  -- ACCT-ID PIC 9(11)
    ActiveStatus        CHAR(1)         NOT NULL,              -- ACCT-ACTIVE-STATUS
    CurrentBalance      DECIMAL(12,2)   NOT NULL DEFAULT 0,    -- ACCT-CURR-BAL
    CreditLimit         DECIMAL(12,2)   NOT NULL DEFAULT 0,    -- ACCT-CREDIT-LIMIT
    CashCreditLimit     DECIMAL(12,2)   NOT NULL DEFAULT 0,    -- ACCT-CASH-CREDIT-LIMIT
    OpenDate            DATE            NULL,                   -- ACCT-OPEN-DATE
    ExpirationDate      DATE            NULL,                   -- ACCT-EXPIRAION-DATE
    ReissueDate         DATE            NULL,                   -- ACCT-REISSUE-DATE
    CurrentCycleCredit  DECIMAL(12,2)   NOT NULL DEFAULT 0,    -- ACCT-CURR-CYC-CREDIT
    CurrentCycleDebit   DECIMAL(12,2)   NOT NULL DEFAULT 0,    -- ACCT-CURR-CYC-DEBIT
    ZipCode             VARCHAR(10)     NULL,                   -- ACCT-ADDR-ZIP
    GroupId             VARCHAR(10)     NULL,                   -- ACCT-GROUP-ID
    CreatedAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT GETDATE()
);

-- Card Master (from CVACT02Y, LRECL=150)
CREATE TABLE Cards (
    CardNumber          VARCHAR(16)     NOT NULL PRIMARY KEY,   -- CARD-NUM
    AccountId           BIGINT          NOT NULL,               -- CARD-ACCT-ID
    CvvCode             SMALLINT        NOT NULL,               -- CARD-CVV-CD
    EmbossedName        VARCHAR(50)     NULL,                   -- CARD-EMBOSSED-NAME
    ExpirationDate      DATE            NULL,                   -- CARD-EXPIRAION-DATE
    ActiveStatus        CHAR(1)         NOT NULL,               -- CARD-ACTIVE-STATUS
    CreatedAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Cards_Accounts FOREIGN KEY (AccountId) REFERENCES Accounts(AccountId)
);
CREATE INDEX IX_Cards_AccountId ON Cards(AccountId);

-- Customer Master (from CVCUS01Y, LRECL=500)
CREATE TABLE Customers (
    CustomerId          INT             NOT NULL PRIMARY KEY,   -- CUST-ID PIC 9(09)
    FirstName           VARCHAR(25)     NOT NULL,               -- CUST-FIRST-NAME
    MiddleName          VARCHAR(25)     NULL,                   -- CUST-MIDDLE-NAME
    LastName            VARCHAR(25)     NOT NULL,               -- CUST-LAST-NAME
    AddressLine1        VARCHAR(50)     NULL,                   -- CUST-ADDR-LINE-1
    AddressLine2        VARCHAR(50)     NULL,                   -- CUST-ADDR-LINE-2
    AddressLine3        VARCHAR(50)     NULL,                   -- CUST-ADDR-LINE-3
    StateCode           CHAR(2)         NULL,                   -- CUST-ADDR-STATE-CD
    CountryCode         CHAR(3)         NULL,                   -- CUST-ADDR-COUNTRY-CD
    ZipCode             VARCHAR(10)     NULL,                   -- CUST-ADDR-ZIP
    Phone1              VARCHAR(15)     NULL,                   -- CUST-PHONE-NUM-1
    Phone2              VARCHAR(15)     NULL,                   -- CUST-PHONE-NUM-2
    Ssn                 INT             NULL,                   -- CUST-SSN
    GovernmentId        VARCHAR(20)     NULL,                   -- CUST-GOVT-ISSUED-ID
    DateOfBirth         DATE            NULL,                   -- CUST-DOB-YYYY-MM-DD
    EftAccountId        VARCHAR(10)     NULL,                   -- CUST-EFT-ACCOUNT-ID
    PrimaryCardHolder   CHAR(1)         NULL,                   -- CUST-PRI-CARD-HOLDER-IND
    FicoCreditScore     SMALLINT        NULL,                   -- CUST-FICO-CREDIT-SCORE
    CreatedAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT GETDATE()
);

-- Card Cross Reference (from CVACT03Y, LRECL=50)
CREATE TABLE CardCrossReferences (
    CardNumber          VARCHAR(16)     NOT NULL PRIMARY KEY,   -- XREF-CARD-NUM
    CustomerId          INT             NOT NULL,               -- XREF-CUST-ID
    AccountId           BIGINT          NOT NULL,               -- XREF-ACCT-ID
    CONSTRAINT FK_CardXref_Customers FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId),
    CONSTRAINT FK_CardXref_Accounts FOREIGN KEY (AccountId) REFERENCES Accounts(AccountId)
);
CREATE INDEX IX_CardXref_AccountId ON CardCrossReferences(AccountId);
CREATE INDEX IX_CardXref_CustomerId ON CardCrossReferences(CustomerId);

-- Transaction Master (from CVTRA05Y, LRECL=350)
CREATE TABLE Transactions (
    TransactionId       VARCHAR(16)     NOT NULL PRIMARY KEY,   -- TRAN-ID
    TypeCode            CHAR(2)         NOT NULL,               -- TRAN-TYPE-CD
    CategoryCode        SMALLINT        NOT NULL,               -- TRAN-CAT-CD
    Source              VARCHAR(10)     NULL,                   -- TRAN-SOURCE
    Description         VARCHAR(100)    NULL,                   -- TRAN-DESC
    Amount              DECIMAL(11,2)   NOT NULL,               -- TRAN-AMT
    MerchantId          INT             NULL,                   -- TRAN-MERCHANT-ID
    MerchantName        VARCHAR(50)     NULL,                   -- TRAN-MERCHANT-NAME
    MerchantCity        VARCHAR(50)     NULL,                   -- TRAN-MERCHANT-CITY
    MerchantZip         VARCHAR(10)     NULL,                   -- TRAN-MERCHANT-ZIP
    CardNumber          VARCHAR(16)     NOT NULL,               -- TRAN-CARD-NUM
    OriginalTimestamp   DATETIME2       NULL,                   -- TRAN-ORIG-TS
    ProcessedTimestamp  DATETIME2       NULL,                   -- TRAN-PROC-TS
    CONSTRAINT FK_Transactions_Cards FOREIGN KEY (CardNumber) REFERENCES Cards(CardNumber)
);
CREATE INDEX IX_Transactions_CardNumber ON Transactions(CardNumber);
CREATE INDEX IX_Transactions_TypeCode ON Transactions(TypeCode);

-- User Security (from CSUSR01Y, LRECL=80)
CREATE TABLE Users (
    UserId              VARCHAR(8)      NOT NULL PRIMARY KEY,   -- SEC-USR-ID
    FirstName           VARCHAR(20)     NOT NULL,               -- SEC-USR-FNAME
    LastName            VARCHAR(20)     NOT NULL,               -- SEC-USR-LNAME
    PasswordHash        VARCHAR(256)    NOT NULL,               -- Hashed (was SEC-USR-PWD)
    UserType            CHAR(1)         NOT NULL,               -- SEC-USR-TYPE ('A'/'U')
    CreatedAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT GETDATE()
);

-- Transaction Category Balance (from CVTRA01Y, LRECL=50)
CREATE TABLE TransactionCategoryBalances (
    AccountId           BIGINT          NOT NULL,               -- TRANCAT-ACCT-ID
    TypeCode            CHAR(2)         NOT NULL,               -- TRANCAT-TYPE-CD
    CategoryCode        SMALLINT        NOT NULL,               -- TRANCAT-CD
    Balance             DECIMAL(11,2)   NOT NULL DEFAULT 0,     -- TRAN-CAT-BAL
    CONSTRAINT PK_TranCatBal PRIMARY KEY (AccountId, TypeCode, CategoryCode),
    CONSTRAINT FK_TranCatBal_Accounts FOREIGN KEY (AccountId) REFERENCES Accounts(AccountId)
);

-- Disclosure Groups (from CVTRA02Y, LRECL=50)
CREATE TABLE DisclosureGroups (
    AccountGroupId      VARCHAR(10)     NOT NULL,               -- DIS-ACCT-GROUP-ID
    TransactionTypeCode CHAR(2)         NOT NULL,               -- DIS-TRAN-TYPE-CD
    TransactionCatCode  SMALLINT        NOT NULL,               -- DIS-TRAN-CAT-CD
    InterestRate        DECIMAL(6,2)    NOT NULL DEFAULT 0,     -- DIS-INT-RATE
    CONSTRAINT PK_DisclosureGroups PRIMARY KEY (AccountGroupId, TransactionTypeCode, TransactionCatCode)
);

-- Transaction Types (from CVTRA03Y, LRECL=60)
CREATE TABLE TransactionTypes (
    TypeCode            CHAR(2)         NOT NULL PRIMARY KEY,   -- TRAN-TYPE
    Description         VARCHAR(50)     NOT NULL                -- TRAN-TYPE-DESC
);

-- Transaction Categories (from CVTRA04Y, LRECL=60)
CREATE TABLE TransactionCategories (
    TypeCode            CHAR(2)         NOT NULL,               -- TRAN-TYPE-CD
    CategoryCode        SMALLINT        NOT NULL,               -- TRAN-CAT-CD
    Description         VARCHAR(50)     NOT NULL,               -- TRAN-CAT-TYPE-DESC
    CONSTRAINT PK_TranCategories PRIMARY KEY (TypeCode, CategoryCode),
    CONSTRAINT FK_TranCat_Types FOREIGN KEY (TypeCode) REFERENCES TransactionTypes(TypeCode)
);
```

### 7.2 Index Strategy (Replacing VSAM KSDS + AIX)

| VSAM Structure | SQL Equivalent |
|---|---|
| ACCTDAT KSDS (key: ACCT-ID) | `Accounts` PK on `AccountId` |
| CARDDAT KSDS (key: CARD-NUM) | `Cards` PK on `CardNumber` |
| CARDAIX (alternate index on CARDDAT) | `IX_Cards_AccountId` |
| CCXREF KSDS (key: CARD-NUM) | `CardCrossReferences` PK on `CardNumber` |
| CXACAIX (alternate index on CCXREF by ACCT-ID) | `IX_CardXref_AccountId` |
| CUSTDAT KSDS (key: CUST-ID) | `Customers` PK on `CustomerId` |
| TRANSACT KSDS (key: TRAN-ID) | `Transactions` PK on `TransactionId` |
| USRSEC KSDS (key: USR-ID) | `Users` PK on `UserId` |

---

## 8. Risk Assessment

### 8.1 Risk Matrix

| # | Risk | Likelihood | Impact | Severity | Mitigation |
|---|---|---|---|---|---|
| 1 | **Data type precision loss** during COBOL PIC to C# decimal conversion | Medium | High | **High** | Create comprehensive data type mapping; use `decimal` for all monetary fields; validate with sample data |
| 2 | **EBCDIC to ASCII encoding** issues in data migration | Medium | Medium | **Medium** | Use proper encoding libraries; validate all character data post-migration |
| 3 | **Pseudo-conversational model** mismatch with stateless REST | Low | Medium | **Medium** | Design stateless API from scratch; use JWT for session context; no direct COMMAREA translation |
| 4 | **Batch job orchestration** complexity (17-step sequence) | Medium | High | **High** | Implement job dependency graph in Hangfire; add monitoring and retry logic; test with production-volume data |
| 5 | **VSAM browse operations** performance in SQL | Low | Medium | **Low** | Use proper pagination with keyset pagination; add covering indexes |
| 6 | **Interest calculation** business logic accuracy | Medium | High | **High** | Extract exact formulas from CBACT04C; create unit tests with known inputs/outputs from mainframe |
| 7 | **Statement generation** (CBSTM03A) uses PSA/TCB/TIOT control blocks | High | Medium | **High** | Rewrite completely; replace control block addressing with .NET configuration; replace ALTER/GO TO with structured code |
| 8 | **Security model upgrade** from plain-text passwords | Low | Low | **Low** | Implement proper password hashing; plan user password reset during migration |
| 9 | **Optional modules** (IMS/DB2/MQ) not in current repo | Medium | Medium | **Medium** | Plan as Phase 2; design messaging interfaces now for future integration |
| 10 | **SORT utility** replacement for COMBTRAN job | Low | Low | **Low** | Use LINQ OrderBy or SQL ORDER BY; straightforward replacement |
| 11 | **GDG (Generation Data Groups)** versioning pattern | Medium | Medium | **Medium** | Implement audit tables or temporal tables; design backup/versioning strategy |
| 12 | **CEEDAYS date validation** in CSUTLDTC | Low | Low | **Low** | Replace with `DateTime.TryParseExact()`; well-understood mapping |
| 13 | **Concurrent access patterns** differ between CICS and web | Medium | High | **High** | Implement optimistic concurrency with EF Core; add row versioning |
| 14 | **Report generation** format compatibility | Medium | Medium | **Medium** | Generate PDF/HTML reports; may need to match existing statement format |

### 8.2 Complexity Assessment by Component

| Component | Complexity | Effort (Story Points) | Notes |
|---|---|---|---|
| Signon/Auth (COSGN00C) | Low | 3 | Standard ASP.NET Core Identity |
| Menu Navigation (COMEN01C, COADM01C) | Low | 2 | SPA routing |
| Account View/Update (COACTVWC, COACTUPC) | Medium | 5 | CRUD with validation |
| Credit Card CRUD (COCRDLIC, COCRDSLC, COCRDUPC) | Medium | 8 | List/view/update with cross-reference lookups |
| Transaction CRUD (COTRN00C, COTRN01C, COTRN02C) | Medium | 8 | List/view/add with validation |
| Reports (CORPT00C) | Medium | 5 | Report generation |
| Bill Payment (COBIL00C) | Medium | 5 | Payment processing logic |
| User Admin (COUSR00C-03C) | Low | 5 | Standard CRUD |
| POSTTRAN batch (CBTRN02C) | High | 13 | Complex validation, multi-file updates |
| INTCALC batch (CBACT04C) | High | 13 | Interest computation with disclosure groups |
| CREASTMT batch (CBSTM03A) | High | 13 | Statement generation, control block addressing, subroutine calls |
| COMBTRAN batch (SORT) | Low | 2 | Simple sort/merge |
| Database schema + migration | Medium | 8 | Schema design + data migration scripts |
| **Total Estimated** | | **~90 SP** | |

---

## 9. Tooling & Framework Recommendations

### 9.1 Development Stack

| Category | Recommendation | Version | Rationale |
|---|---|---|---|
| **Runtime** | .NET 8 (LTS) | 8.0 | Long-term support, latest performance improvements |
| **Web Framework** | ASP.NET Core Web API | 8.0 | Industry standard for REST APIs |
| **ORM** | Entity Framework Core | 8.0 | Code-first, migrations, LINQ support |
| **Database** | SQL Server 2022 or PostgreSQL 16 | Latest | Enterprise-grade RDBMS; SQL Server for Azure alignment |
| **Authentication** | ASP.NET Core Identity + JWT | 8.0 | Built-in user management, token-based auth |
| **Validation** | FluentValidation | 11.x | Declarative validation rules |
| **Logging** | Serilog | 3.x | Structured logging with multiple sinks |
| **API Documentation** | Swashbuckle (Swagger/OpenAPI) | 6.x | Auto-generated API docs |
| **Background Jobs** | Hangfire or .NET BackgroundService | 1.8.x | Scheduled job execution with dashboard |
| **Testing** | xUnit + Moq + FluentAssertions | Latest | Comprehensive testing framework |
| **UI (optional)** | Blazor Server or React | 8.0 / 18.x | Modern web UI replacing 3270 screens |

### 9.2 Migration Tooling

| Tool | Purpose | When to Use |
|---|---|---|
| **COBOL Analyzer** (e.g., Micro Focus, AWS Blu Age) | Automated COBOL code analysis | Phase 1 - deeper analysis |
| **Data Migration Tool** (SSIS, Azure Data Factory) | EBCDIC-to-SQL data migration | Phase 2 - data migration |
| **EF Core Migrations** | Database schema versioning | Phase 2+ - ongoing |
| **Postman / REST Client** | API testing | Phase 3 - testing |
| **SonarQube** | Code quality analysis | Phase 3+ - ongoing |
| **Azure DevOps / GitHub Actions** | CI/CD pipeline | Phase 2+ - ongoing |

### 9.3 Project Structure Recommendation

```
CardDemo.Modernized/
├── src/
│   ├── CardDemo.Api/                    # ASP.NET Core Web API project
│   │   ├── Controllers/                 # API controllers (one per domain)
│   │   ├── Middleware/                   # Auth, error handling, logging
│   │   ├── Program.cs                   # Application entry point
│   │   └── appsettings.json
│   ├── CardDemo.Core/                   # Business logic layer
│   │   ├── Services/                    # Service classes
│   │   ├── Models/                      # Domain models and DTOs
│   │   ├── Validators/                  # FluentValidation validators
│   │   └── Interfaces/                  # Service interfaces
│   ├── CardDemo.Infrastructure/         # Data access layer
│   │   ├── Data/                        # DbContext, configurations
│   │   ├── Entities/                    # EF Core entity classes
│   │   ├── Migrations/                  # EF Core migrations
│   │   └── Repositories/               # Repository implementations
│   ├── CardDemo.BackgroundJobs/         # Batch processing
│   │   ├── Jobs/                        # PostTran, IntCalc, Statement jobs
│   │   └── Scheduling/                  # Job scheduling configuration
│   └── CardDemo.Messaging/             # Future: MQ replacement
│       ├── Handlers/                    # Message handlers
│       └── Models/                      # Message DTOs
├── tests/
│   ├── CardDemo.Api.Tests/
│   ├── CardDemo.Core.Tests/
│   ├── CardDemo.Infrastructure.Tests/
│   └── CardDemo.Integration.Tests/
├── docs/
│   └── phase1-assessment-dotnet-modernization.md
├── CardDemo.sln
└── README.md
```

---

## 10. Migration Roadmap

### Phase 1: Assessment & Foundation (Current - Complete)
- Application inventory and analysis
- Target architecture design
- Technology mapping
- Risk assessment
- Database schema design

### Phase 2: Core Infrastructure (Weeks 1-4)
- Set up .NET solution structure
- Implement database schema with EF Core migrations
- Migrate seed data from VSAM flat files to SQL Server
- Implement authentication (ASP.NET Core Identity + JWT)
- Set up CI/CD pipeline

### Phase 3: Online Transaction Migration (Weeks 5-10)
- Implement Account domain (View, Update)
- Implement Card domain (List, View, Update)
- Implement Transaction domain (List, View, Add)
- Implement Bill Payment
- Implement Reports
- Implement User Administration (Admin)
- Unit and integration testing

### Phase 4: Batch Processing Migration (Weeks 11-14)
- Implement POSTTRAN (transaction posting) as background job
- Implement INTCALC (interest calculation) as background job
- Implement CREASTMT (statement generation) as background job
- Implement COMBTRAN (transaction merge) as background job
- Job scheduling and orchestration

### Phase 5: UI Development (Weeks 15-18)
- Implement web UI (Blazor or React)
- Replace all 17 BMS screens with modern web pages
- User acceptance testing

### Phase 6: Optional Modules (Weeks 19-22)
- Implement messaging layer (Azure Service Bus / RabbitMQ)
- Migrate authorization module (IMS/DB2/MQ)
- Migrate transaction type management (DB2)
- Migrate account extraction module

### Phase 7: Testing & Cutover (Weeks 23-26)
- End-to-end testing
- Performance testing
- Data migration validation
- Parallel run with mainframe
- Production cutover

**Total Estimated Duration: 26 weeks (6 months)**

---

## Appendix A: File Inventory Summary

| Directory | File Type | Count |
|---|---|---|
| `app/cbl/` | COBOL programs (.cbl/.CBL) | 28 |
| `app/cpy/` | Copybooks (.cpy/.CPY) | 28 |
| `app/cpy-bms/` | BMS-generated copybooks (.CPY) | 17 |
| `app/bms/` | BMS map sources (.bms) | 17 |
| `app/jcl/` | JCL jobs (.jcl/.JCL) | 29 |
| `app/csd/` | CSD definitions | 1 (506 lines) |
| `app/catlg/` | LISTCAT output | 1 (3957 lines) |
| `app/data/ASCII/` | Sample data files (.txt) | 9 |
| `app/data/EBCDIC/` | EBCDIC data files | (binary) |
| `app/proc/` | JCL procedures (.prc) | 2 |
| `app/ctl/` | Control files (.ctl) | 1 |
| `diagrams/` | Application diagrams (.png/.drawio) | 6 |
| **Total** | | **~139 files** |

## Appendix B: COMMAREA Structure

The COMMAREA (`COCOM01Y`) is the central state-passing mechanism between CICS programs:

```
CARDDEMO-COMMAREA (total ~170 bytes)
├── CDEMO-GENERAL-INFO
│   ├── CDEMO-FROM-TRANID      PIC X(04)   - Source transaction
│   ├── CDEMO-FROM-PROGRAM     PIC X(08)   - Source program
│   ├── CDEMO-TO-TRANID        PIC X(04)   - Target transaction
│   ├── CDEMO-TO-PROGRAM       PIC X(08)   - Target program
│   ├── CDEMO-USER-ID          PIC X(08)   - Authenticated user
│   ├── CDEMO-USER-TYPE        PIC X(01)   - 'A'=Admin, 'U'=User
│   └── CDEMO-PGM-CONTEXT      PIC 9(01)   - 0=Enter, 1=Reenter
├── CDEMO-CUSTOMER-INFO
│   ├── CDEMO-CUST-ID          PIC 9(09)
│   ├── CDEMO-CUST-FNAME       PIC X(25)
│   ├── CDEMO-CUST-MNAME       PIC X(25)
│   └── CDEMO-CUST-LNAME       PIC X(25)
├── CDEMO-ACCOUNT-INFO
│   ├── CDEMO-ACCT-ID          PIC 9(11)
│   └── CDEMO-ACCT-STATUS      PIC X(01)
├── CDEMO-CARD-INFO
│   └── CDEMO-CARD-NUM         PIC 9(16)
└── CDEMO-MORE-INFO
    ├── CDEMO-LAST-MAP          PIC X(7)
    └── CDEMO-LAST-MAPSET       PIC X(7)
```

In the .NET architecture, this maps to JWT claims (user info) + request/response DTOs (business data) + client-side state (navigation context).
