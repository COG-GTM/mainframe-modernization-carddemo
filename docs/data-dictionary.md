# Data Dictionary

This document parses every copybook in [`app/cpy/`](../app/cpy/) (29 files) and
[`app/cpy-bms/`](../app/cpy-bms/) (17 files), documenting record layouts, field names, `PIC`
clauses, sizes, and any `COMP`/`COMP-3`, `REDEFINES`, or `OCCURS` clauses.

### Notation & conventions

- **Offset** is the 1-based byte position within the record; **Len** is the field length in bytes.
- **`PIC X(n)`** → alphanumeric, `n` bytes.
- **`PIC 9(n)`** → unsigned zoned decimal (USAGE DISPLAY), `n` bytes.
- **`PIC S9(i)V99`** → signed zoned decimal, `i+2` bytes; `V` is an *implied* decimal point (no
  stored byte); `S` is an embedded sign (overpunch on the last byte, no extra byte).
- **COMP-3 (packed decimal):** **None** of the persistent record copybooks use COMP-3. All numeric
  amount fields are USAGE DISPLAY signed zoned decimal (e.g. `S9(09)V99`). The only binary fields in
  the repo are `COMP`/`BINARY` work fields inside `CSUTLDWY` and the generated BMS length fields
  (`...L COMP PIC S9(4)`), noted in their sections below.

> **FD cross-reference:** The persistent records (`CVTRA01Y`, `CVACT01Y`, `CVACT03Y`, `CVTRA02Y`,
> `CVTRA05Y`, …) are all present as standalone copybooks in `app/cpy/` and are pulled into the
> programs via `COPY` inside the `FD`/`WORKING-STORAGE`. For example `CBACT04C` declares them with
> `01 FD-… RECORD` FDs and `COPY` of the record into `WORKING-STORAGE` (see
> [program-inventory.md](program-inventory.md) for which program uses which copybook).

---

## A. Persistent record layouts (VSAM / sequential data)

### CVACT01Y — `ACCOUNT-RECORD` (300 bytes) — Account master (KSDS, key = ACCT-ID)

| Offset | Len | Field                    | PIC          | Notes |
|-------:|----:|:-------------------------|:-------------|:------|
| 1      | 11  | ACCT-ID                  | 9(11)        | Primary key |
| 12     | 1   | ACCT-ACTIVE-STATUS       | X(01)        | Y/N |
| 13     | 12  | ACCT-CURR-BAL            | S9(10)V99    | Current balance |
| 25     | 12  | ACCT-CREDIT-LIMIT        | S9(10)V99    | |
| 37     | 12  | ACCT-CASH-CREDIT-LIMIT   | S9(10)V99    | |
| 49     | 10  | ACCT-OPEN-DATE           | X(10)        | YYYY-MM-DD |
| 59     | 10  | ACCT-EXPIRAION-DATE      | X(10)        | YYYY-MM-DD (sic, copybook spelling) |
| 69     | 10  | ACCT-REISSUE-DATE        | X(10)        | YYYY-MM-DD |
| 79     | 12  | ACCT-CURR-CYC-CREDIT     | S9(10)V99    | Reset by interest run |
| 91     | 12  | ACCT-CURR-CYC-DEBIT      | S9(10)V99    | Reset by interest run |
| 103    | 10  | ACCT-ADDR-ZIP            | X(10)        | |
| 113    | 10  | ACCT-GROUP-ID            | X(10)        | Disclosure-group key part |
| 123    | 178 | FILLER                   | X(178)       | |

### CVACT02Y — `CARD-RECORD` (150 bytes) — Card master (KSDS, key = CARD-NUM; AIX on CARD-ACCT-ID)

| Offset | Len | Field               | PIC    | Notes |
|-------:|----:|:--------------------|:-------|:------|
| 1      | 16  | CARD-NUM            | X(16)  | Primary key |
| 17     | 11  | CARD-ACCT-ID        | 9(11)  | AIX (CARDAIX) |
| 28     | 3   | CARD-CVV-CD         | 9(03)  | |
| 31     | 50  | CARD-EMBOSSED-NAME  | X(50)  | |
| 81     | 10  | CARD-EXPIRAION-DATE | X(10)  | |
| 91     | 1   | CARD-ACTIVE-STATUS  | X(01)  | |
| 92     | 59  | FILLER              | X(59)  | |

### CVACT03Y — `CARD-XREF-RECORD` (50 bytes) — Card/Account/Customer xref (KSDS, key = XREF-CARD-NUM; AIX on XREF-ACCT-ID = CXACAIX)

| Offset | Len | Field          | PIC    | Notes |
|-------:|----:|:---------------|:-------|:------|
| 1      | 16  | XREF-CARD-NUM  | X(16)  | Primary key |
| 17     | 9   | XREF-CUST-ID   | 9(09)  | |
| 26     | 11  | XREF-ACCT-ID   | 9(11)  | AIX path (CXACAIX) used by CBACT04C |
| 37     | 14  | FILLER         | X(14)  | |

### CVCUS01Y — `CUSTOMER-RECORD` (500 bytes) — Customer master (KSDS, key = CUST-ID)

| Offset | Len | Field                    | PIC    | Notes |
|-------:|----:|:-------------------------|:-------|:------|
| 1      | 9   | CUST-ID                  | 9(09)  | Primary key |
| 10     | 25  | CUST-FIRST-NAME          | X(25)  | |
| 35     | 25  | CUST-MIDDLE-NAME         | X(25)  | |
| 60     | 25  | CUST-LAST-NAME           | X(25)  | |
| 85     | 50  | CUST-ADDR-LINE-1         | X(50)  | |
| 135    | 50  | CUST-ADDR-LINE-2         | X(50)  | |
| 185    | 50  | CUST-ADDR-LINE-3         | X(50)  | |
| 235    | 2   | CUST-ADDR-STATE-CD       | X(02)  | |
| 237    | 3   | CUST-ADDR-COUNTRY-CD     | X(03)  | |
| 240    | 10  | CUST-ADDR-ZIP            | X(10)  | |
| 250    | 15  | CUST-PHONE-NUM-1         | X(15)  | |
| 265    | 15  | CUST-PHONE-NUM-2         | X(15)  | |
| 280    | 9   | CUST-SSN                 | 9(09)  | |
| 289    | 20  | CUST-GOVT-ISSUED-ID      | X(20)  | |
| 309    | 10  | CUST-DOB-YYYY-MM-DD      | X(10)  | |
| 319    | 10  | CUST-EFT-ACCOUNT-ID      | X(10)  | |
| 329    | 1   | CUST-PRI-CARD-HOLDER-IND | X(01)  | |
| 330    | 3   | CUST-FICO-CREDIT-SCORE   | 9(03)  | |
| 333    | 168 | FILLER                   | X(168) | |

> **`CUSTREC.cpy`** is an identical 500-byte `CUSTOMER-RECORD` (used by the statement batch
> `CBSTM03A`); the only difference is the DOB field is named `CUST-DOB-YYYYMMDD` instead of
> `CUST-DOB-YYYY-MM-DD`.

### CVTRA01Y — `TRAN-CAT-BAL-RECORD` (50 bytes) — Transaction category balance (KSDS)

Group key `TRAN-CAT-KEY` spans the first 17 bytes (composite key).

| Offset | Len | Field           | PIC          | Notes |
|-------:|----:|:----------------|:-------------|:------|
| 1      | 11  | TRANCAT-ACCT-ID | 9(11)        | part of TRAN-CAT-KEY |
| 12     | 2   | TRANCAT-TYPE-CD | X(02)        | part of TRAN-CAT-KEY |
| 14     | 4   | TRANCAT-CD      | 9(04)        | part of TRAN-CAT-KEY |
| 18     | 11  | TRAN-CAT-BAL    | S9(09)V99    | Balance the interest run reads |
| 29     | 22  | FILLER          | X(22)        | |

### CVTRA02Y — `DIS-GROUP-RECORD` (50 bytes) — Disclosure group / interest rates (KSDS)

Group key `DIS-GROUP-KEY` spans the first 16 bytes (composite key).

| Offset | Len | Field             | PIC          | Notes |
|-------:|----:|:------------------|:-------------|:------|
| 1      | 10  | DIS-ACCT-GROUP-ID | X(10)        | part of DIS-GROUP-KEY |
| 11     | 2   | DIS-TRAN-TYPE-CD  | X(02)        | part of DIS-GROUP-KEY |
| 13     | 4   | DIS-TRAN-CAT-CD   | 9(04)        | part of DIS-GROUP-KEY |
| 17     | 6   | DIS-INT-RATE      | S9(04)V99    | Annual % rate; 0 ⇒ skip interest |
| 23     | 28  | FILLER            | X(28)        | |

### CVTRA03Y — `TRAN-TYPE-RECORD` (60 bytes) — Transaction type lookup (KSDS, key = TRAN-TYPE)

| Offset | Len | Field          | PIC   | Notes |
|-------:|----:|:---------------|:------|:------|
| 1      | 2   | TRAN-TYPE      | X(02) | Primary key |
| 3      | 50  | TRAN-TYPE-DESC | X(50) | |
| 53     | 8   | FILLER         | X(08) | |

### CVTRA04Y — `TRAN-CAT-RECORD` (60 bytes) — Transaction category lookup (KSDS)

Group key `TRAN-CAT-KEY` spans the first 6 bytes (composite key).

| Offset | Len | Field             | PIC   | Notes |
|-------:|----:|:------------------|:------|:------|
| 1      | 2   | TRAN-TYPE-CD      | X(02) | part of TRAN-CAT-KEY |
| 3      | 4   | TRAN-CAT-CD       | 9(04) | part of TRAN-CAT-KEY |
| 7      | 50  | TRAN-CAT-TYPE-DESC| X(50) | |
| 57     | 4   | FILLER            | X(04) | |

### CVTRA05Y — `TRAN-RECORD` (350 bytes) — Transaction master (KSDS, key = TRAN-ID)

| Offset | Len | Field              | PIC          | Notes |
|-------:|----:|:-------------------|:-------------|:------|
| 1      | 16  | TRAN-ID            | X(16)        | Primary key |
| 17     | 2   | TRAN-TYPE-CD       | X(02)        | Interest tx = `01` |
| 19     | 4   | TRAN-CAT-CD        | 9(04)        | Interest tx = `0005` |
| 23     | 10  | TRAN-SOURCE        | X(10)        | Interest tx = `System` |
| 33     | 100 | TRAN-DESC          | X(100)       | Interest tx = `Int. for a/c …` |
| 133    | 11  | TRAN-AMT           | S9(09)V99    | Computed interest amount |
| 144    | 9   | TRAN-MERCHANT-ID   | 9(09)        | |
| 153    | 50  | TRAN-MERCHANT-NAME | X(50)        | |
| 203    | 50  | TRAN-MERCHANT-CITY | X(50)        | |
| 253    | 10  | TRAN-MERCHANT-ZIP  | X(10)        | |
| 263    | 16  | TRAN-CARD-NUM      | X(16)        | From card xref |
| 279    | 26  | TRAN-ORIG-TS       | X(26)        | Timestamp |
| 305    | 26  | TRAN-PROC-TS       | X(26)        | Timestamp |
| 331    | 20  | FILLER             | X(20)        | |

### CVTRA06Y — `DALYTRAN-RECORD` (350 bytes) — Daily (incoming) transaction

Identical layout to `TRAN-RECORD` above, with every field prefixed `DALYTRAN-` instead of `TRAN-`
(`DALYTRAN-ID`, `DALYTRAN-TYPE-CD`, … `DALYTRAN-PROC-TS`, `FILLER X(20)`). Read by `CBTRN01C`/`CBTRN02C`.

### COSTM01 — `TRNX-RECORD` (350 bytes) — Statement transaction (used by CBSTM03A/B)

Group key `TRNX-KEY` spans the first 32 bytes (card number + transaction id).

| Offset | Len | Field              | PIC          | Notes |
|-------:|----:|:-------------------|:-------------|:------|
| 1      | 16  | TRNX-CARD-NUM      | X(16)        | part of TRNX-KEY |
| 17     | 16  | TRNX-ID            | X(16)        | part of TRNX-KEY |
| 33     | 2   | TRNX-TYPE-CD       | X(02)        | |
| 35     | 4   | TRNX-CAT-CD        | 9(04)        | |
| 39     | 10  | TRNX-SOURCE        | X(10)        | |
| 49     | 100 | TRNX-DESC          | X(100)       | |
| 149    | 11  | TRNX-AMT           | S9(09)V99    | |
| 160    | 9   | TRNX-MERCHANT-ID   | 9(09)        | |
| 169    | 50  | TRNX-MERCHANT-NAME | X(50)        | |
| 219    | 50  | TRNX-MERCHANT-CITY | X(50)        | |
| 269    | 10  | TRNX-MERCHANT-ZIP  | X(10)        | |
| 279    | 26  | TRNX-ORIG-TS       | X(26)        | |
| 305    | 26  | TRNX-PROC-TS       | X(26)        | |
| 331    | 20  | FILLER             | X(20)        | |

### CSUSR01Y — `SEC-USER-DATA` (80 bytes) — User security (USRSEC KSDS, key = SEC-USR-ID)

| Offset | Len | Field          | PIC   | Notes |
|-------:|----:|:---------------|:------|:------|
| 1      | 8   | SEC-USR-ID     | X(08) | Primary key |
| 9      | 20  | SEC-USR-FNAME  | X(20) | |
| 29     | 20  | SEC-USR-LNAME  | X(20) | |
| 49     | 8   | SEC-USR-PWD    | X(08) | Plain-text password (legacy) |
| 57     | 1   | SEC-USR-TYPE   | X(01) | `A`=admin, `U`=user |
| 58     | 23  | SEC-USR-FILLER | X(23) | |

> **`UNUSED1Y.cpy`** defines `UNUSED-DATA`, a byte-for-byte clone of `SEC-USER-DATA` (80 bytes)
> with `UNUSED-` prefixes. It is not referenced by any program.

---

## B. Report & date/work copybooks

### CVTRA07Y — Report layout group (used by CBTRN03C)

Defines several report line `01` levels (all USAGE DISPLAY):

- `REPORT-NAME-HEADER` — report title block (`REPT-SHORT-NAME X(38)`, `REPT-LONG-NAME X(41)`,
  `REPT-DATE-HEADER X(12)`, `REPT-START-DATE X(10)`, `FILLER ' to '`, `REPT-END-DATE X(10)`).
- `TRANSACTION-DETAIL-REPORT` — one detail line: `TRAN-REPORT-TRANS-ID X(16)`,
  `TRAN-REPORT-ACCOUNT-ID X(11)`, `TRAN-REPORT-TYPE-CD X(02)`, `TRAN-REPORT-TYPE-DESC X(15)`,
  `TRAN-REPORT-CAT-CD 9(04)`, `TRAN-REPORT-CAT-DESC X(29)`, `TRAN-REPORT-SOURCE X(10)`,
  `TRAN-REPORT-AMT PIC -ZZZ,ZZZ,ZZZ.ZZ` (edited numeric), interspersed with literal FILLERs.
- `TRANSACTION-HEADER-1`, `TRANSACTION-HEADER-2 (PIC X(133) VALUE ALL '-')` — column headings.
- `REPORT-PAGE-TOTALS`, `REPORT-ACCOUNT-TOTALS`, `REPORT-GRAND-TOTALS` — each with a
  `PIC +ZZZ,ZZZ,ZZZ.ZZ` edited total field.

### CODATECN — `CODATECN-REC` — date conversion work area

Input/output date structure with multiple `REDEFINES` over a 20-byte date string and 88-level
condition names selecting the format:

- `CODATECN-IN-REC` → `CODATECN-TYPE X` (88: `YYYYMMDD-IN`/`YYYY-MM-DD-IN`), `CODATECN-INP-DATE X(20)`
  with `CODATECN-1INP`/`CODATECN-2INP` **REDEFINES** splitting into `YYYY/MM/DD` (+ separators).
- `CODATECN-OUT-REC` → `CODATECN-OUTTYPE X`, `CODATECN-0UT-DATE X(20)` with `CODATECN-1OUT`/
  `CODATECN-2OUT` **REDEFINES**.
- `CODATECN-ERROR-MSG X(38)`.

### CSDAT01Y — `WS-DATE-TIME` — current date/time work area

Editing structure (no persistence). Highlights: `WS-CURDATE` (`YEAR 9(4)`, `MONTH 9(2)`, `DAY 9(2)`)
with `WS-CURDATE-N REDEFINES … PIC 9(08)`; `WS-CURTIME` (`HOURS/MINUTE/SECOND/MILSEC` each `9(02)`)
with `WS-CURTIME-N REDEFINES … 9(08)`; formatted variants `WS-CURDATE-MM-DD-YY`,
`WS-CURTIME-HH-MM-SS`, and a `WS-TIMESTAMP` (`YYYY-MM-DD HH:MM:SS.NNNNNN`) built from numeric pieces
plus literal separators.

### CSUTLDWY — date-edit work area (used with CSUTLDPY)

Contains the only **binary** fields in the copybook set:

- `WS-EDIT-DATE-CCYYMMDD` broken into `CC/YY/MM/DD` text fields, each with `…-N REDEFINES … PIC 9(n)`
  numeric redefinitions and 88-levels (`THIS-CENTURY VALUE 20`, `WS-VALID-MONTH VALUES 1 THRU 12`,
  `WS-31-DAY-MONTH`, `WS-FEBRUARY`, `WS-VALID-DAY 1 THRU 31`, `WS-VALID-FEB-DAY 1 THRU 28`, …).
- `WS-EDIT-DATE-BINARY PIC S9(9) BINARY` and `WS-CURRENT-DATE-BINARY PIC S9(9) BINARY` — **COMP/BINARY**.
- Result/flag fields (`WS-EDIT-DATE-FLGS` with `WS-EDIT-YEAR-FLG`/`WS-EDIT-MONTH`/`WS-EDIT-DAY` flags),
  `WS-DATE-VALIDATION-RESULT` formatted message.

---

## C. CICS framework & navigation copybooks

### COCOM01Y — `CARDDEMO-COMMAREA` — inter-program context (passed on XCTL)

| Group / Field | PIC | Notes |
|:--------------|:----|:------|
| CDEMO-FROM-TRANID | X(04) | |
| CDEMO-FROM-PROGRAM | X(08) | |
| CDEMO-TO-TRANID | X(04) | |
| CDEMO-TO-PROGRAM | X(08) | XCTL target program |
| CDEMO-USER-ID | X(08) | |
| CDEMO-USER-TYPE | X(01) | 88: `CDEMO-USRTYP-ADMIN 'A'`, `CDEMO-USRTYP-USER 'U'` |
| CDEMO-PGM-CONTEXT | 9(01) | 88: `CDEMO-PGM-ENTER 0`, `CDEMO-PGM-REENTER 1` |
| CDEMO-CUST-ID | 9(09) | |
| CDEMO-CUST-FNAME / MNAME / LNAME | X(25) each | |
| CDEMO-ACCT-ID | 9(11) | |
| CDEMO-ACCT-STATUS | X(01) | |
| CDEMO-CARD-NUM | 9(16) | |
| CDEMO-LAST-MAP / CDEMO-LAST-MAPSET | X(7) each | |

### COMEN02Y — `CARDDEMO-MAIN-MENU-OPTIONS` (main-menu dispatch table)

- `CDEMO-MENU-OPT-COUNT PIC 9(02) VALUE 11`.
- `CDEMO-MENU-OPTIONS-DATA` — 11 inline entries, each `num 9(02)`, `name X(35)`, `pgmname X(08)`,
  `usrtype X(01)`.
- `CDEMO-MENU-OPTIONS REDEFINES … OCCURS 12 TIMES` → `CDEMO-MENU-OPT-NUM 9(02)`,
  `CDEMO-MENU-OPT-NAME X(35)`, `CDEMO-MENU-OPT-PGMNAME X(08)`, `CDEMO-MENU-OPT-USRTYPE X(01)`.
- **Targets:** COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C,
  CORPT00C, COBIL00C, COPAUS0C.

### COADM02Y — `CARDDEMO-ADMIN-MENU-OPTIONS` (admin-menu dispatch table)

- `CDEMO-ADMIN-OPT-COUNT PIC 9(02) VALUE 6`.
- 6 inline entries (`num 9(02)`, `name X(35)`, `pgmname X(08)`).
- `CDEMO-ADMIN-OPTIONS REDEFINES … OCCURS 9 TIMES` → `CDEMO-ADMIN-OPT-NUM 9(02)`,
  `CDEMO-ADMIN-OPT-NAME X(35)`, `CDEMO-ADMIN-OPT-PGMNAME X(08)`.
- **Targets:** COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC (Db2), COTRTUPC (Db2).

### CVCRD01Y — `CC-WORK-AREAS` — credit-card screen work area

`CCARD-AID X(5)` with 88-levels for every AID key (`CCARD-AID-ENTER`, `…-CLEAR`, `…-PA1/PA2`,
`…-PFK01`…`PFK12`); `CCARD-NEXT-PROG X(8)`, `CCARD-NEXT-MAPSET X(7)`, `CCARD-NEXT-MAP X(7)`,
`CCARD-ERROR-MSG X(75)`, `CCARD-RETURN-MSG X(75)` (88 `CCARD-RETURN-MSG-OFF VALUE LOW-VALUES`),
and account/card/customer id fields each with an alphanumeric definition plus a numeric
**REDEFINES** (`CC-ACCT-ID X(11)` / `CC-ACCT-ID-N REDEFINES … 9(11)`, similarly `CC-CARD-NUM`,
`CC-CUST-ID`).

### COTTL01Y — `CCDA-SCREEN-TITLE`

Three `PIC X(40)` literal title lines (`CCDA-TITLE01`, `CCDA-TITLE02`, `CCDA-THANK-YOU`).

### CSMSG01Y — `CCDA-COMMON-MESSAGES`
`CCDA-MSG-THANK-YOU PIC X(50)`, `CCDA-MSG-INVALID-KEY PIC X(50)`.

### CSMSG02Y — `ABEND-DATA`
`ABEND-CODE X(4)`, `ABEND-CULPRIT X(8)`, `ABEND-REASON X(50)`, `ABEND-MSG X(72)`.

### CSLKPCDY — lookup-code validation tables
`WS-US-PHONE-AREA-CODE-TO-EDIT PIC XXX` plus large 88-level `VALUES` lists of valid US phone area
codes, state codes, and US/Canada zip-state combinations. Pure validation data (no record layout).

### Procedural (code) copybooks — no data layout

These contain `PROCEDURE DIVISION` statements copied inline, not record definitions:

- **CSUTLDPY** — date-edit paragraphs (`EDIT-DATE-CCYYMMDD`, `EDIT-YEAR-CCYY`, …) used with `CSUTLDWY`.
- **CSSETATY** — sets BMS attribute/colour for a field in error (template with `(TESTVAR)` substitution).
- **CSSTRPFY** — `YYYY-STORE-PFKEY` paragraph translating `EIBAID` to `CCARD-AID-*` flags.

---

## D. BMS symbolic map copybooks (`app/cpy-bms/`, 17 files)

These are the **CICS BMS-generated symbolic maps** (the COBOL view of each screen). They are
auto-generated from the matching `app/bms/*.bms` source (see [screen-maps.md](screen-maps.md) for the
screen layouts and transaction ids).

**Generated structure (per map):** an input record `01 <map>I` and an output record
`01 <map>O REDEFINES <map>I`. For every screen field `XXX` the map contains a fixed 5-part group:

| Suffix | PIC | Meaning |
|:-------|:----|:--------|
| `XXXL` | `COMP PIC S9(4)` | field length (binary half-word) on input |
| `XXXF` / `XXXA` (redefines) | `PIC X` | input flag / attribute byte |
| `XXXI` | `PIC X(n)` | **input** value (in the `…I` record) |
| `XXXC`,`XXXP`,`XXXH`,`XXXV`,`XXXO` | `PIC X` / `PIC X(n)` | colour, highlight, etc. and **output** value (in the `…O` record) |

The data-bearing fields are the `XXXI`/`XXXO` pairs. Their widths per map:

| Copybook (map record) | Data input fields (`name` width) |
|:----------------------|:---------------------------------|
| **COSGN00** (`COSGN0AI`) | TRNNAME 4, TITLE01 40, CURDATE 8, PGMNAME 8, TITLE02 40, CURTIME 9, APPLID 8, SYSID 8, USERID 8, PASSWD 8, ERRMSG 78 |
| **COMEN01** (`COMEN1AI`) | header(6) + OPTN001..OPTN012 ×40, OPTION 2, ERRMSG 78 |
| **COADM01** (`COADM1AI`) | header(6) + OPTN001..OPTN012 ×40, OPTION 2, ERRMSG 78 |
| **COACTVW** (`CACTVWAI`) | header(6) + ACSTTUS 1, ADTOPEN 10, ACRDLIM 15, AEXPDT 10, ACSHLIM 15, AREISDT 10, ACURBAL 15, ACRCYCR 15, AADDGRP 10, ACRCYDB 15, ACSTNUM 9, ACSTSSN 12, ACSTDOB 10, ACSTFCO 3, ACSFNAM/ACSMNAM/ACSLNAM 25, ACSADL1 50, ACSSTTE 2, ACSADL2 50, ACSZIPC 5, ACSCITY 50, ACSCTRY 3, ACSPHN1 13, ACSGOVT 20, ACSPHN2 13, ACSEFTC 10, ACSPFLG 1, INFOMSG 45, ERRMSG 78 |
| **COACTUP** (`CACTUPAI`) | header(6) + ACCTSID 11, ACSTTUS 1, OPN/EXP/RIS YEAR 4/MON 2/DAY 2, ACRDLIM/ACSHLIM/ACURBAL/ACRCYCR/ACRCYDB 15, AADDGRP 10, ACSTNUM 9, ACTSSN1 3/ACTSSN2 2/ACTSSN3 4, DOB YEAR 4/MON 2/DAY 2, ACSTFCO 3, names 25, addr 50/2/50, ACSZIPC 5, ACSCITY 50, ACSCTRY 3, phone parts 3/3/4 (×2), ACSGOVT 20, ACSEFTC 10, ACSPFLG 1, INFOMSG 45, ERRMSG 78, FKEYS 21, FKEY05 7, FKEY12 10 |
| **COCRDLI** (`CCRDLIAI`) | header(6) + PAGENO 3, ACCTSID 11, CARDSID 16, then 7 rows of CRDSELn 1 / CRDSTPn 1 / ACCTNOn 11 / CRDNUMn 16 / CRDSTSn 1, INFOMSG 45, ERRMSG 78 |
| **COCRDSL** (`CCRDSLAI`) | header(6) + ACCTSID 11, CARDSID 16, CRDNAME 50, CRDSTCD 1, EXPMON 2, EXPYEAR 4, INFOMSG 40, ERRMSG 80, FKEYS 75 |
| **COCRDUP** (`CCRDUPAI`) | header(6) + ACCTSID 11, CARDSID 16, CRDNAME 50, CRDSTCD 1, EXPMON 2, EXPYEAR 4, EXPDAY 2, INFOMSG 40, ERRMSG 80, FKEYS 21, FKEYSC 18 |
| **COTRN00** (`COTRN0AI`) | header(6) + PAGENUM 8, TRNIDIN 16, then 10 rows of SEL000n 1 / TRNIDn 16 / TDATEn 8 / TDESCn 20 / TAMTn 12, ERRMSG 78 |
| **COTRN01** (`COTRN1AI`) | header(6) + TRNIDIN 16, TRNID 16, CARDNUM 16, TTYPCD 2, TCATCD 4, TRNSRC 10, TDESC 100, TRNAMT 12, TORIGDT 26, TPROCDT 26, MID 9, MNAME 50, MCITY 50, MZIP 10, ERRMSG 78 |
| **COTRN02** (`COTRN2AI`) | header(6) + ACTIDIN 11, CARDNIN 16, TTYPCD 2, TCATCD 4, TRNSRC 10, TDESC 100, TRNAMT 12, TORIGDT 26, TPROCDT 26, MID 9, MNAME 50, MCITY 50, MZIP 10, CONFIRM 1, ERRMSG 78 |
| **CORPT00** (`CORPT0AI`) | header(6) + MONTHLY 1, YEARLY 1, CUSTOM 1, SDTMM 2, SDTDD 2, SDTYYYY 4, EDTMM 2, EDTDD 2, EDTYYYY 4, CONFIRM 1, ERRMSG 78 |
| **COBIL00** (`COBIL0AI`) | header(6) + ACTIDIN 11, CURBAL 14, CONFIRM 1, ERRMSG 78 |
| **COUSR00** (`COUSR0AI`) | header(6) + PAGENUM 8, USRIDIN 16, then 10 rows of SEL000n 1 / USRIDn 8 / FNAMEn 20 / LNAMEn 20 / UTYPEn 1, ERRMSG 78 |
| **COUSR01** (`COUSR1AI`) | header(6) + FNAME 20, LNAME 20, USERID 8, PASSWD 8, USRTYPE 1, ERRMSG 78 |
| **COUSR02** (`COUSR2AI`) | header(6) + USRIDIN 8, FNAME 20, LNAME 20, PASSWD 8, USRTYPE 1, ERRMSG 78 |
| **COUSR03** (`COUSR3AI`) | header(6) + USRIDIN 8, FNAME 20, LNAME 20, USRTYPE 1, ERRMSG 78 |

*"header(6)" = the common `TRNNAME 4, TITLE01 40, CURDATE 8, PGMNAME 8, TITLE02 40, CURTIME 8/9`
field group present on every screen.*
