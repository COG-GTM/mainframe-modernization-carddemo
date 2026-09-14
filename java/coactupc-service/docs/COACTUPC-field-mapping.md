# COACTUPC field mapping

## 1. Overview and constants

`COACTUPC` is the CAUP account-update transaction. The source mapset is
`COACTUP`, map `CACTUPA`. The transaction reads account master `ACCTDAT`
(`CVACT01Y`), customer master `CUSTDAT` (`CVCUS01Y`), and card
cross-reference `CARDDAT` (`CVACT03Y`) through alternate index `CXACAIX`.

The Java service uses `AccountRecord`, `CustomerRecord`, and `CardXrefRecord`
for copybook records, and `AccountUpdateDetails` for the string-valued screen
state. Record lengths are 300 bytes, 500 bytes, and 50 bytes respectively.
Offsets below are one-based COBOL byte offsets. FILLER is intentionally not
represented in Java.

## 2. Copybook-to-entity mappings

### CVACT01Y / ACCOUNT-RECORD -> AccountRecord / ACCTDAT (300 bytes)

| COBOL field | PIC | Offset | Java field | Java type / JPA width | Notes |
|---|---|---:|---|---|---|
| ACCT-ID | 9(11) | 1 | acctId | Long / id | |
| ACCT-ACTIVE-STATUS | X(01) | 12 | activeStatus | String / 1 | |
| ACCT-CURR-BAL | S9(10)V99 | 13 | currBal | BigDecimal / 12,2 | |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 25 | creditLimit | BigDecimal / 12,2 | |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 37 | cashCreditLimit | BigDecimal / 12,2 | |
| ACCT-OPEN-DATE | X(10) | 49 | openDate | String / 10 | YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | X(10) | 59 | expirationDate | String / 10 | COBOL spelling preserved in this document |
| ACCT-REISSUE-DATE | X(10) | 69 | reissueDate | String / 10 | |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 79 | currCycCredit | BigDecimal / 12,2 | |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 91 | currCycDebit | BigDecimal / 12,2 | |
| ACCT-ADDR-ZIP | X(10) | 103 | addrZip | String / 10 | preserved by Java update |
| ACCT-GROUP-ID | X(10) | 113 | groupId | String / 10 | |
| FILLER | X(178) | 123 | — | — | dropped |

### CVCUS01Y / CUSTOMER-RECORD -> CustomerRecord / CUSTDAT (500 bytes)

| COBOL field | PIC | Offset | Java field | Java type / JPA width |
|---|---|---:|---|---|
| CUST-ID | 9(09) | 1 | custId | Long / id |
| CUST-FIRST-NAME | X(25) | 10 | firstName | String / 25 |
| CUST-MIDDLE-NAME | X(25) | 35 | middleName | String / 25 |
| CUST-LAST-NAME | X(25) | 60 | lastName | String / 25 |
| CUST-ADDR-LINE-1 | X(50) | 85 | addrLine1 | String / 50 |
| CUST-ADDR-LINE-2 | X(50) | 135 | addrLine2 | String / 50 |
| CUST-ADDR-LINE-3 | X(50) | 185 | addrLine3 | String / 50 |
| CUST-ADDR-STATE-CD | X(02) | 235 | addrStateCd | String / 2 |
| CUST-ADDR-COUNTRY-CD | X(03) | 237 | addrCountryCd | String / 3 |
| CUST-ADDR-ZIP | X(10) | 240 | addrZip | String / 10 |
| CUST-PHONE-NUM-1 | X(15) | 250 | phoneNum1 | String / 15 |
| CUST-PHONE-NUM-2 | X(15) | 265 | phoneNum2 | String / 15 |
| CUST-SSN | 9(09) | 280 | ssn | Long |
| CUST-GOVT-ISSUED-ID | X(20) | 289 | govtIssuedId | String / 20 |
| CUST-DOB | X(10) | 309 | dobYyyyMmDd | String / 10 |
| CUST-EFT-ACCOUNT-ID | X(10) | 319 | eftAccountId | String / 10 |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 329 | priCardHolderInd | String / 1 |
| CUST-FICO-CREDIT-SCORE | 9(03) | 330 | ficoCreditScore | Integer |
| FILLER | X(168) | 333 | — | — |

### CVACT03Y / CARD-XREF-RECORD -> CardXrefRecord / CARDXREF (50 bytes)

The calculated length is 16 + 9 + 11 + 14 = **50 bytes**, confirming the
copybook record length.

| COBOL field | PIC | Offset | Java field | Java type / index |
|---|---|---:|---|---|
| XREF-CARD-NUM | X(16) | 1 | cardNum | String / primary key |
| XREF-CUST-ID | 9(09) | 17 | custId | Long |
| XREF-ACCT-ID | 9(11) | 26 | acctId | Long / CXACAIX index |
| FILLER | X(14) | 37 | — | — |

## 3. ACUP screen field mapping

All DTO fields are `String` because the map carries edited display values.
`cardNum` is read-only context copied from the card cross-reference.

| Screen field / COBOL concept | DTO field | Edit routine | Message label |
|---|---|---|---|
| Account number | acctId | 1210 EDIT-ACCOUNT | Account number |
| Account status | activeStatus | 1220 EDIT-YESNO | Account Status |
| Current balance | currBal | 1250 EDIT-SIGNED-9V2 | Current Balance |
| Credit limit | creditLimit | 1250 EDIT-SIGNED-9V2 | Credit Limit |
| Cash credit limit | cashCreditLimit | 1250 EDIT-SIGNED-9V2 | Cash Credit Limit |
| Open year | openYear | date editor | Open Date |
| Open month | openMon | date editor | Open Date |
| Open day | openDay | date editor | Open Date |
| Expiry year | expYear | date editor | Expiry Date |
| Expiry month | expMon | date editor | Expiry Date |
| Expiry day | expDay | date editor | Expiry Date |
| Reissue year | reissueYear | date editor | Reissue Date |
| Reissue month | reissueMon | date editor | Reissue Date |
| Reissue day | reissueDay | date editor | Reissue Date |
| Current cycle credit | currCycCredit | 1250 EDIT-SIGNED-9V2 | Current Cycle Credit Limit |
| Current cycle debit | currCycDebit | 1250 EDIT-SIGNED-9V2 | Current Cycle Debit Limit |
| Group ID | groupId | comparison / write | Group ID |
| Customer number | custId | fetched context | Customer ID |
| SSN first three | ssn1 | 1245 / 1265 | SSN: First 3 chars |
| SSN fourth and fifth | ssn2 | 1245 / 1265 | SSN 4th & 5th chars |
| SSN last four | ssn3 | 1245 / 1265 | SSN Last 4 chars |
| Date of birth year | dobYear | date / DOB editor | Date of Birth |
| Date of birth month | dobMon | date / DOB editor | Date of Birth |
| Date of birth day | dobDay | date / DOB editor | Date of Birth |
| FICO score | ficoScore | 1245 / 1275 | FICO Score |
| First name | firstName | 1225 EDIT-ALPHA-REQD | First Name |
| Middle name | middleName | 1235 EDIT-ALPHA-OPT | Middle Name |
| Last name | lastName | 1225 EDIT-ALPHA-REQD | Last Name |
| Address line 1 | addrLine1 | 1215 EDIT-MANDATORY | Address Line 1 |
| Address line 2 | addrLine2 | 1215 EDIT-MANDATORY | Address Line 2 |
| City / address line 3 | addrLine3 | 1225 EDIT-ALPHA-REQD | City |
| State | addrStateCd | 1225 / 1270 | State |
| Country | addrCountryCd | 1225 EDIT-ALPHA-REQD | Country |
| Customer ZIP | addrZip | 1245 / 1280 | Zip |
| Phone 1 area | phone1A | 1260 EDIT-US-PHONE-NUM | Phone 1 area |
| Phone 1 prefix | phone1B | 1260 EDIT-US-PHONE-NUM | Phone 1 prefix |
| Phone 1 line | phone1C | 1260 EDIT-US-PHONE-NUM | Phone 1 line |
| Phone 2 area | phone2A | 1260 EDIT-US-PHONE-NUM | Phone 2 area |
| Phone 2 prefix | phone2B | 1260 EDIT-US-PHONE-NUM | Phone 2 prefix |
| Phone 2 line | phone2C | 1260 EDIT-US-PHONE-NUM | Phone 2 line |
| Government-issued ID | govtIssuedId | comparison / write | Government ID |
| EFT account ID | eftAccountId | 1245 EDIT-NUM-REQD | EFT Account Id |
| Primary card-holder indicator | priHolderInd | 1220 EDIT-YESNO | Primary Card Holder |
| Card number (read-only) | cardNum | fetched context | Card Number |

## 4. COBOL paragraph to Java method

| COBOL paragraph | Java method / class |
|---|---|
| 1200-EDIT-MAP-INPUTS | `AccountUpdateService.editMapInputs` |
| 1205-COMPARE-OLD-NEW | `AccountUpdateService.compareOldNew` |
| 1210-EDIT-ACCOUNT | `AccountUpdateService.readAccount` input validation |
| 1215-EDIT-MANDATORY | `FieldEditor.editMandatory` |
| 1220-EDIT-YESNO | `FieldEditor.editYesNo` |
| 1225-EDIT-ALPHA-REQD | `FieldEditor.editAlphaReqd` |
| 1235-EDIT-ALPHA-OPT | `FieldEditor.editAlphaOpt` |
| 1245-EDIT-NUM-REQD | `FieldEditor.editNumReqd` |
| 1250-EDIT-SIGNED-9V2 | `FieldEditor.editSigned9V2` |
| 1260-EDIT-US-PHONE-NUM | phone editing in `FieldEditor` and service |
| 1265-EDIT-US-SSN | SSN editing in `editMapInputs` |
| 1270-EDIT-US-STATE-CD | `FieldEditor.stateValid` |
| 1275-EDIT-FICO-SCORE | FICO editing in `editMapInputs` |
| 1280-EDIT-US-STATE-ZIP-CD | `FieldEditor.zipStateValid` |
| 9000-READ-ACCT | `AccountUpdateService.readAccount` |
| 9200-GETCARDXREF-BYACCT | `CardXrefRepository.findFirstByAcctId` |
| 9300-GETACCTDATA-BYACCT | `AccountRepository.findById` |
| 9400-GETCUSTDATA-BYCUST | `CustomerRepository.findById` |
| 9500-STORE-FETCHED-DATA | `AccountUpdateService.from` |
| 9600-WRITE-PROCESSING | `AccountUpdateService.writeProcessing` |
| 9700-CHECK-CHANGE-IN-REC | `accountUnchanged` / `customerUnchanged` |

## 5. Actions, messages, and transport

### ACUP-CHANGE-ACTION

| COBOL code | Java action |
|---|---|
| blank | DETAILS_NOT_FETCHED |
| S | SHOW_DETAILS |
| E | CHANGES_NOT_OK |
| N | CHANGES_OK_NOT_CONFIRMED |
| C | CHANGES_OKAYED_AND_DONE |
| L | CHANGES_OKAYED_LOCK_ERROR |
| F | CHANGES_OKAYED_BUT_FAILED |

### WS-INFO-MSG and WS-RETURN-MSG

| COBOL meaning | Java constant / value |
|---|---|
| PROMPT-FOR-SEARCH-KEYS | `ENTER_ACCOUNT`: Enter or update id of account to update |
| DETAILS-SHOWN | `DETAILS_SHOWN`: Details of selected account shown above |
| PROMPT-FOR-CHANGES | `UPDATE_PRESENTED`: Update account details presented above. |
| CHANGES-VALIDATED | `CHANGES_VALIDATED`: Changes validated.Press F5 to save |
| COMMITTED | `COMMITTED`: Changes committed to database |
| INFORM-FAILURE | `UNSUCCESSFUL`: Changes unsuccessful. Please try again |
| account absent | `ACCOUNT_NOT_PROVIDED` |
| account invalid | `ACCOUNT_INVALID` |
| no change | `NO_CHANGE` |
| cross-reference absent | `XREF_NOT_FOUND` |
| account master absent | `ACCOUNT_NOT_FOUND` |
| customer master absent | `CUSTOMER_NOT_FOUND` |
| lock account/customer | `LOCK_ACCOUNT` / `LOCK_CUSTOMER` |
| stale record | `DATA_CHANGED` |
| persistence failure | `UPDATE_FAILED` |

### PF keys and HTTP

| Screen action | HTTP mapping |
|---|---|
| ENTER | `POST /api/v1/accounts/{acctId}/validate`, always 200 screen response |
| F5 | `PUT /api/v1/accounts/{acctId}` |
| PF3 | client-side navigation, not implemented server-side |
| PF12 | client re-issues `GET /api/v1/accounts/{acctId}` |
| SHOW_DETAILS / successful fetch | 200 |
| invalid account input | 400 |
| not found | 404 |
| stale data | 409 |
| lock failure | 423 |
| persistence failure | 500 |

## 6. CICS-to-Spring translation

| CICS / COBOL behavior | Spring implementation |
|---|---|
| `READ` | repository `findById` |
| `READ ... UPDATE` | `findByIdForUpdate` with `PESSIMISTIC_WRITE` |
| `REWRITE` | mutate entity and repository `save` / flush |
| `SYNCPOINT ROLLBACK` | `@Transactional` rollback-only handling |
| RESP/REAS NOTFND | response action plus HTTP 404 |
| map redisplay | JSON response with action, messages, flags, and details |
| COMMAREA old/new details | `AccountUpdateRequest.original` and `.updated` |
| CICS map field attributes | `FieldFlag` values in the response |

## 7. Known deviations and preserved quirks

* The COBOL `ACCT-UPDATE-RECORD` omits `ACCT-ADDR-ZIP`. Consequently,
  `ACCT-UPDATE-GROUP-ID` is at byte offset 102 in that update layout, the
  offset used by `ACCT-ADDR-ZIP` in the full account record. A COBOL REWRITE
  can therefore overwrite address ZIP and blank GROUP-ID. Java preserves the
  address ZIP and writes `groupId` to its correct entity column.
* `ACCT-EXPIRAION-DATE` is misspelled in the copybook. Java uses
  `expirationDate`, while this document preserves the source spelling.
* The selected `VALID-GENERAL-PURP-CODE` list contains `999`. The service
  follows the copybook and accepts `999`, despite an example that describes it
  as invalid.
* COBOL numeric fields are conceptually never null. Java treats null money as
  `0.00` for stale-data comparison and treats null strings and empty snapshots
  as equivalent where COBOL spaces would compare equal.
* Values with more than ten integer digits are rejected instead of allowing
  COBOL MOVE behavior to truncate high-order digits.
* The phone blank check in the source repeats the NUMA check. Java treats a
  phone with all three blank components as a valid blank phone.
* FILLER fields are dropped from entities and DTOs.
* Simplified REST responses expose the WS-RETURN-MSG text rather than raw
  CICS RESP and REAS values.
