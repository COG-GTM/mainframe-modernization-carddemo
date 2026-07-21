# Bill Payment Stream — Analysis (Step 2a: `!analyze_stream`)

**Stream:** Bill Payment — TransID `CB00`, program `COBIL00C`
**SOURCE:** `app/cbl/COBIL00C.cbl` (572 lines), map `app/bms/COBIL00.bms`, DSECT `app/cpy-bms/COBIL00.CPY`
**Copybooks:** `CVACT01Y` (ACCOUNT-RECORD), `CVACT03Y` (CARD-XREF-RECORD), `CVTRA05Y` (TRAN-RECORD)

## Program inventory & leaf-first dependency DAG

Single online program with paragraph-level sub-routines. Data-access "leaves" (map to persistence layer):

```
COBIL00C (orchestrator / UI-bearing)
 ├── READ-ACCTDAT-FILE      → ACCTDAT (READ ... UPDATE)         [leaf: Account read-for-update]
 ├── READ-CXACAIX-FILE      → CXACAIX (READ)                    [leaf: Card-xref lookup by acct]
 ├── STARTBR/READPREV/ENDBR-TRANSACT-FILE → TRANSACT (browse)   [leaf: max TRAN-ID discovery]
 ├── WRITE-TRANSACT-FILE    → TRANSACT (WRITE)                  [leaf: Transaction insert]
 └── UPDATE-ACCTDAT-FILE    → ACCTDAT (REWRITE)                 [leaf: Account balance rewrite]
```

Wave grouping (leaf-first):
- **Wave A (data-access leaves):** Account repository (read/update), CardXref repository (read), Transaction repository (max-id + insert). Plain JPA — **no stored procedures** (see SP table).
- **Wave B (orchestrator, UI-bearing):** `COBIL00C` business orchestration → `BillPaymentService` + REST controller + Angular `COBIL00` screen.

## uiSurface (per program)
`COBIL00C` — **UI-bearing**. Map `COBIL0A` (mapset `COBIL00`), 24×80 screen "Bill Payment".
- Input fields: `ACTIDIN` (Acct ID, len 11), `CONFIRM` (Y/N, len 1).
- Output/protected fields: `CURBAL` (current balance, len 14), `ERRMSG` (message line, len 78, RED normally / GREEN on success), plus header (`TRNNAME`,`TITLE01/02`,`CURDATE`,`PGMNAME`,`CURTIME`).
- AID keys: `ENTER`=continue/process, `PF3`=back (to `CDEMO-FROM-PROGRAM` or `COMEN01C`), `PF4`=clear.

## Field dictionary (source copybooks → canonical types)

### ACCOUNT-RECORD (`CVACT01Y`, RECLN 300)
| COBOL field | PIC | Meaning | Canonical type |
|-------------|-----|---------|----------------|
| ACCT-ID | 9(11) | account id (PK) | long / 11-digit |
| ACCT-ACTIVE-STATUS | X(01) | active flag | char(1) |
| ACCT-CURR-BAL | S9(10)V99 | current balance | decimal(12,2) |
| ACCT-CREDIT-LIMIT | S9(10)V99 | credit limit | decimal(12,2) |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | cash limit | decimal(12,2) |
| ACCT-OPEN-DATE | X(10) | open date | char(10) |
| ACCT-EXPIRAION-DATE | X(10) | expiry date | char(10) |
| ACCT-REISSUE-DATE | X(10) | reissue date | char(10) |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | cycle credit | decimal(12,2) |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | cycle debit | decimal(12,2) |
| ACCT-ADDR-ZIP | X(10) | zip | char(10) |
| ACCT-GROUP-ID | X(10) | group id | char(10) |
| FILLER | X(178) | filler | — |

### CARD-XREF-RECORD (`CVACT03Y`, RECLN 50) — indexed by ACCT (AIX `CXACAIX`)
| COBOL field | PIC | Meaning | Canonical type |
|-------------|-----|---------|----------------|
| XREF-CARD-NUM | X(16) | card number | char(16) |
| XREF-CUST-ID | 9(09) | customer id | long |
| XREF-ACCT-ID | 9(11) | account id (AIX key) | long |
| FILLER | X(14) | filler | — |

### TRAN-RECORD (`CVTRA05Y`, RECLN 350)
| COBOL field | PIC | Meaning | Canonical type |
|-------------|-----|---------|----------------|
| TRAN-ID | X(16) | transaction id (PK, numeric string) | char(16) |
| TRAN-TYPE-CD | X(02) | type code (`'02'` for bill pay) | char(2) |
| TRAN-CAT-CD | 9(04) | category code (`2`) | int |
| TRAN-SOURCE | X(10) | source (`'POS TERM'`) | char(10) |
| TRAN-DESC | X(100) | description (`'BILL PAYMENT - ONLINE'`) | char(100) |
| TRAN-AMT | S9(09)V99 | amount | decimal(11,2) |
| TRAN-MERCHANT-ID | 9(09) | merchant id (`999999999`) | long |
| TRAN-MERCHANT-NAME | X(50) | merchant name (`'BILL PAYMENT'`) | char(50) |
| TRAN-MERCHANT-CITY | X(50) | merchant city (`'N/A'`) | char(50) |
| TRAN-MERCHANT-ZIP | X(10) | merchant zip (`'N/A'`) | char(10) |
| TRAN-CARD-NUM | X(16) | card number (from xref) | char(16) |
| TRAN-ORIG-TS | X(26) | origination timestamp | char(26) |
| TRAN-PROC-TS | X(26) | processing timestamp | char(26) |
| FILLER | X(20) | filler | — |

## Control flow (COBIL00C) — line-cited
- **Entry / reentry** (`MAIN-PARA`, lines 99–149): `EIBCALEN=0` → return to `COSGN00C`. First entry → send empty screen; if `CDEMO-CB00-TRN-SELECTED` was passed, prefill Acct ID and process. Reentry → receive map, dispatch on AID (`ENTER`→`PROCESS-ENTER-KEY`; `PF3`→back; `PF4`→clear; other→"invalid key").
- **PROCESS-ENTER-KEY** (154–244): the core business logic.
  - **V1** Acct ID empty (159–164) → `'Acct ID can NOT be empty...'`.
  - Move Acct ID to `ACCT-ID`/`XREF-ACCT-ID` (170–171).
  - **Confirm dispatch** (173–191): `Y/y`→confirm-yes, read account; `N/n`→clear + stop; blank→read account (unconfirmed); other→`'Invalid value. Valid values are (Y/N)...'` (V2).
  - Move balance to screen (193–194).
  - **V3** balance ≤ 0 with non-empty acct (198–205) → `'You have nothing to pay...'`.
  - **Payment path (confirm=Y)** (210–235): read CXACAIX (card num); browse TRANSACT descending for max TRAN-ID; `newId = maxId + 1`; build TRAN-RECORD with the fixed bill-pay constants + `TRAN-AMT = ACCT-CURR-BAL` + card num + timestamps; WRITE transaction; `ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT` (→ 0); REWRITE account.
  - **Unconfirmed path** (236–240) → `'Confirm to make a bill payment...'`.
- **WRITE-TRANSACT-FILE** (510–547): on success → clear fields + GREEN message `'Payment successful.  Your Transaction ID is <id>.'`; DUPKEY/DUPREC → `'Tran ID already exist...'`.
- **Read/Update ACCTDAT** (343–403): NOTFND → `'Account ID NOT found...'`; other → `'Unable to lookup/Update Account...'`.
- **READ-CXACAIX** (408–436): NOTFND → `'Account ID NOT found...'`; other → `'Unable to lookup XREF AIX file...'`.
- **Browse TRANSACT** (441–496): ENDFILE on READPREV → start numbering at 0 (so first id = 1).
- Header population = current date/time (`POPULATE-HEADER-INFO`, 319–338).

## Business invariants (the parity oracle)
1. A confirmed payment **inserts exactly one** TRANSACT row with `TRAN-AMT = pre-payment ACCT-CURR-BAL` and the fixed bill-pay constants.
2. New `TRAN-ID = (max existing numeric TRAN-ID) + 1`, zero-padded to 16 (first ever = 1).
3. After a confirmed payment, `ACCT-CURR-BAL` becomes `0` (balance − amount, amount == balance).
4. Payment blocked when balance ≤ 0 ("nothing to pay") and when not confirmed ("confirm...").
5. Card number on the transaction comes from the account→card xref (`CXACAIX`).
6. Success message is GREEN and echoes the new Transaction ID.

## Stored-procedure candidate table
| Data-access leaf | Physical layer | SP? | Language | Decision |
|------------------|----------------|-----|----------|----------|
| Account read-for-update / rewrite | VSAM KSDS ACCTDAT | **No** | — | plain JPA `AccountRepository` |
| Card-xref lookup by acct | VSAM AIX CXACAIX | **No** | — | plain JPA `CardXrefRepository` (query by acctId) |
| Max TRAN-ID + insert | VSAM KSDS TRANSACT | **No** | — | plain JPA `TransactionRepository` (`max(id)` + save) |

**No stored procedures** — all three leaves are simple keyed VSAM operations with no server-side procedural logic; native JPA repositories preserve behavior. No DBA registration requests required for this stream.

## Absent-module flags
- None. All source, maps and copybooks are present on `main`.

## Hard-stop boundary
This slice migrates **only** the online Bill Payment transaction (`COBIL00C`) and the three data stores it touches. It does **not** migrate the sign-on, menu routing, or the batch posting programs; navigation targets (`COSGN00C`, `COMEN01C`) are represented only as return signals, not migrated here.
