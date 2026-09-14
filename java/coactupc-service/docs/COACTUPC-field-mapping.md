# COACTUPC field mapping

## 1. Overview

`COACTUPC` implements transaction `CAUP`, reading and updating `ACCTDAT`,
`CUSTDAT`, and the `CARDDAT` card cross-reference path `CXACAIX`. The original
screen uses mapset `COACTUP` and map `CACTUPA`. RESP 13 / REAS 0 not-found
conditions are represented by HTTP 404 and the user-facing WS-RETURN-MSG
text rather than exposing CICS response numbers.

## 2. Copybook and Java mappings

### CVACT01Y / ACCOUNT-RECORD -> AccountRecord / ACCTDAT

| COBOL field | PIC | Byte offset | Java field | Type / column | Notes |
|---|---|---:|---|---|---|
| ACCT-ID | 9(11) | 1 | acctId | Long | primary key |
| ACCT-ACTIVE-STATUS | X(1) | 12 | activeStatus | String(1) | |
| ACCT-CURR-BAL | S9(10)V99 | 13 | currBal | BigDecimal(12,2) | |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 25 | creditLimit | BigDecimal(12,2) | |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 37 | cashCreditLimit | BigDecimal(12,2) | |
| ACCT-OPEN-DATE | X(10) | 49 | openDate | String(10) | YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | X(10) | 59 | expirationDate | String(10) | Java corrects COBOL spelling |
| ACCT-REISSUE-DATE | X(10) | 69 | reissueDate | String(10) | |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 79 | currCycCredit | BigDecimal(12,2) | |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 91 | currCycDebit | BigDecimal(12,2) | |
| ACCT-ADDR-ZIP | X(10) | 103 | addrZip | String(10) | preserved by Java update |
| ACCT-GROUP-ID | X(10) | 113 | groupId | String(10) | |

### CVCUS01Y / CUSTOMER-RECORD -> CustomerRecord / CUSTDAT

Fields map in copybook order: `CUST-ID` 9(09) -> `custId` Long primary key;
names X(25), address lines X(50), state X(2), country X(3), zip X(10),
phones X(15), SSN 9(09), government ID X(20), DOB X(10), EFT X(10),
primary-holder X(1), and FICO 9(03), with the corresponding Java names
`firstName`, `middleName`, `lastName`, `addrLine1`-`3`, `addrStateCd`,
`addrCountryCd`, `addrZip`, `phoneNum1`-`2`, `ssn`, `govtIssuedId`,
`dobYyyyMmDd`, `eftAccountId`, `priCardHolderInd`, and `ficoCreditScore`.

### CVACT03Y / CARD-XREF-RECORD -> CardXrefRecord / CARDXREF

`XREF-CARD-NUM` X(16) -> `cardNum` String primary key, `XREF-CUST-ID`
9(09) -> `custId` Long, and `XREF-ACCT-ID` 9(11) -> `acctId` Long with
the `CXACAIX` secondary index.

### ACUP-OLD/NEW-DETAILS -> AccountUpdateDetails

All screen values are strings. Account money is edited by `1250-EDIT-SIGNED-9V2`;
date year/month/day triples by `EDIT-DATE-CCYYMMDD`; account status and primary
holder by `1220-EDIT-YESNO`; names/address/country/city by the alpha routines;
SSN parts by `1265-EDIT-US-SSN`; phones by `1260-EDIT-US-PHONE-NUM`; state/zip
by `1270`/`1280`; FICO by `1275`; and EFT ID by `1245-EDIT-NUM-REQD`.
The screen's City label maps to `addrLine3`; the read-only card number is
`cardNum`.

## 3. Paragraph to method

| COBOL paragraph | Java |
|---|---|
| 1210-EDIT-ACCOUNT, 9000/9200/9300/9400/9500 | `readAccount` |
| 1200-EDIT-MAP-INPUTS | `editMapInputs` |
| 1205-COMPARE-OLD-NEW | `compareOldNew` |
| 1215/1220/1225/1235/1245/1250 and date/phone routines | `FieldEditor` |
| 9600-WRITE-PROCESSING / 9700-CHECK-CHANGE-IN-REC | `writeProcessing` |
| 9500-STORE-FETCHED-DATA | `from` |

## 4. Actions, messages, and PF keys

` ` = `DETAILS_NOT_FETCHED`, `S` = `SHOW_DETAILS`, `E` =
`CHANGES_NOT_OK`, `N` = `CHANGES_OK_NOT_CONFIRMED`, `C` =
`CHANGES_OKAYED_AND_DONE`, `L` = `CHANGES_OKAYED_LOCK_ERROR`, and `F` =
`CHANGES_OKAYED_BUT_FAILED`. WS-INFO-MSG values are in `Messages`, along with
the WS-RETURN-MSG texts. GET maps to fetch, POST validate maps to ENTER, and
PUT maps to PF5. PF12 re-issues GET; PF3 is client navigation.

## 5. CICS to Spring

Normal `READ` maps to repository `findById`; `READ UPDATE` maps to
`findByIdForUpdate` with `PESSIMISTIC_WRITE`; `REWRITE` maps to JPA save/flush;
SYNCPOINT ROLLBACK maps to `@Transactional` rollback; NOTFND maps to 404;
record-change detection maps to 409; and lock failures map to 423.

## 6. Known deviations / preserved quirks

* Numeric integer overflow beyond ten digits is rejected rather than silently
  truncated by a COBOL move.
* `ACCT-UPDATE-RECORD` omits `ACCT-ADDR-ZIP`; its group field occupies the
  original address-zip offset (102 in the update layout), causing the legacy
  REWRITE defect to overwrite address zip and blank group id. Java intentionally
  writes groupId to its correct column and preserves addrZip.
* The COBOL spelling `ACCT-EXPIRAION-DATE` is recorded as Java `expirationDate`.
* RESP/REAS values are not included in simplified user-facing not-found text.
* The COBOL phone blank check repeats NUMA; translation treats all three parts
  blank as the intended valid blank phone.
* The requested example treats area code `999` as invalid, but the selected
  COBOL `VALID-GENERAL-PURP-CODE` 88 explicitly includes `999`; the translation
  follows the copybook and accepts it.
* FILLER bytes are dropped.
