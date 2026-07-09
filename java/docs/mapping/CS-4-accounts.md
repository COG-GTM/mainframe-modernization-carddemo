# CS-4 — Accounts online (WAVE 3)

Ports the online **account view** (`COACTVWC`) and **account update** (`COACTUPC`) programs,
their BMS maps and copybooks to a session-backed REST service that plugs into the CS-3
navigation framework. All code lives under `com.carddemo.web.account` (controllers, DTOs) and
`com.carddemo.service.account` (service, validation) so it does not collide with sibling
WAVE-3 waves.

## Source artifacts

| Artifact | Path | Role |
|----------|------|------|
| Program  | `app/cbl/COACTVWC.cbl` | Account view — read account + associated customer. |
| Program  | `app/cbl/COACTUPC.cbl` | Account update — edit + rewrite account and customer. |
| BMS map  | `app/bms/COACTVW.bms`  | View screen (`CACTVWA`), all fields protected/output. |
| BMS map  | `app/bms/COACTUP.bms`  | Update screen (`CACTUPA`), 43 unprotected input fields. |
| Copybook | `app/cpy/CVACT01Y.cpy` | `ACCOUNT-RECORD` (account master). |
| Copybook | `app/cpy/CVCUS01Y.cpy` | `CUSTOMER-RECORD` (customer master). |
| Copybook | `app/cpy/CVACT03Y.cpy` | `CARD-XREF-RECORD` (card/account/customer xref). |
| Copybook | `app/cpy/CSLKPCDY.cpy` | US area codes / state codes / state+zip combos (lookup edits). |

The account/customer/xref JPA entities and repositories already exist (CS-1); this wave reuses
them unchanged and adds **no** Flyway migration.

## Programs → Java

| COBOL | Java | Notes |
|-------|------|-------|
| `COACTVWC` `PROCEDURE DIVISION` | `AccountService.view(acctId)` | `9000-READ-ACCT` read chain. |
| `COACTUPC` `PROCEDURE DIVISION` | `AccountService.update(acctId, request)` | edits + `9600-WRITE-PROCESSING`. |
| `1200-EDIT-MAP-INPUTS` orchestration | `AccountValidator.validate(request)` | every edit, in COBOL order. |
| `1205-COMPARE-OLD-NEW` | `AccountService.hasChanges(...)` | runs *before* edits; short-circuits to the no-change notice. |
| `2210-EDIT-ACCOUNT` / `1210-EDIT-ACCOUNT` | `AccountService.validateAndNormalizeAcctId` | 11-digit non-zero filter, zero-padded. |
| Screen dispatch (`XCTL`/`EIBAID`) | `AccountViewScreenHandler`, `AccountUpdateScreenHandler` | registered against `CardDemoProgram.ACCOUNT_VIEW`/`ACCOUNT_UPDATE`. |

### REST endpoints (`AccountController`, `/api/accounts`)

- `GET /api/accounts/{acctId}` → `AccountViewResponse` (account + associated customer).
- `PUT /api/accounts/{acctId}` (`AccountUpdateRequest`) → `AccountUpdateResponse`.
- Local `@ExceptionHandler`s only (no global handler): `AccountNotFoundException` → `404`,
  `AccountInputException` → `400`, both bodied as `AccountErrorResponse`
  (`message` + `fieldErrors[]`). Authentication is enforced by the CS-2 security layer.

## BMS maps → DTOs

Fields are mapped field-for-field, preserving lengths. Fixed-width numeric identifiers and the
split date/phone/ssn subfields stay `String` (they are display/edit values, never arithmetic);
money fields (`PIC S9(n)V99`) are `BigDecimal` scale 2 on the response.

### `COACTVW` → `AccountViewResponse` (output)

`acctId`, `acctActiveStatus`, `currentBalance`, `creditLimit`, `cashCreditLimit`,
`currentCycleCredit`, `currentCycleDebit`, `openDate`, `expirationDate`, `reissueDate`,
`accountGroupId`, `custId`, `firstName`, `middleName`, `lastName`, `addressLine1`,
`addressLine2`, `city`, `stateCode`, `zipCode`, `countryCode`, `phoneNum1`, `phoneNum2`,
`ssn`, `governmentIssuedId`, `dateOfBirth`, `eftAccountId`, `primaryCardHolderIndicator`,
`ficoScore`. Dates render `yyyy-MM-dd`, phones `(999)999-9999`, SSN `999-99-9999`.

### `COACTUP` → `AccountUpdateRequest` (input, 43 unprotected fields, raw `String`)

`acctActiveStatus`; open date `openYear/openMonth/openDay`; `creditLimit`; expiry
`expiryYear/expiryMonth/expiryDay`; `cashCreditLimit`; reissue
`reissueYear/reissueMonth/reissueDay`; `currentBalance`; `currentCycleCredit`;
`currentCycleDebit`; `accountGroupId`; SSN `ssnPart1/ssnPart2/ssnPart3`; DOB
`dobYear/dobMonth/dobDay`; `ficoScore`; `firstName`; `middleName`; `lastName`;
`addressLine1`; `addressLine2`; `city`; `stateCode`; `zipCode`; `countryCode`; phone 1
`phone1Area/phone1Prefix/phone1Line`; phone 2 `phone2Area/phone2Prefix/phone2Line`;
`governmentIssuedId`; `eftAccountId`; `primaryCardHolderIndicator`.

Input strings are kept unparsed so that (a) `1205-COMPARE-OLD-NEW` can compare exactly what
was keyed against the fetched record and (b) the field edits reproduce the COBOL error text.

## Copybooks → entities (CS-1, reused)

| Copybook field | Entity.field |
|----------------|--------------|
| `ACCT-ID`, `ACCT-ACTIVE-STATUS`, `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT`, `ACCT-GROUP-ID` | `Account.*` |
| `CUST-ID`, `CUST-FIRST/MIDDLE/LAST-NAME`, `CUST-ADDR-LINE-1/2/3`, `CUST-ADDR-STATE-CD`, `CUST-ADDR-ZIP`, `CUST-ADDR-COUNTRY-CD`, `CUST-PHONE-NUM-1/2`, `CUST-SSN`, `CUST-GOVT-ISSUED-ID`, `CUST-DOB-YYYY-MM-DD`, `CUST-EFT-ACCOUNT-ID`, `CUST-PRI-CARD-HOLDER-IND`, `CUST-FICO-CREDIT-SCORE` | `Customer.*` |
| `XREF-ACCT-ID`, `XREF-CUST-ID`, `XREF-CARD-NUM` | `CardXref.*` |

`view`/`update` resolve the customer via `CardXrefRepository.findByXrefAcctId(acctId)` then
`AccountRepository.findById` / `CustomerRepository.findById`, mirroring the COBOL
xref → acctdat → custdat read chain.

## Field edits ported (`1200-EDIT-MAP-INPUTS`)

Every edit runs in the COBOL order; the first failure is the `WS-RETURN-MSG` the screen would
show, and messages are reproduced verbatim.

| COBOL paragraph | Field(s) | Rule / message |
|-----------------|----------|----------------|
| `1215-EDIT-YESNO` | Account Status, Primary Card Holder | `Y`/`N` required. |
| `EDIT-DATE-CCYYMMDD` | Open / Expiry / Reissue / DOB dates | century 19/20, month 1-12, day-in-month + leap year. |
| `EDIT-DATE-OF-BIRTH` | Date of Birth | must not be today or in the future. |
| `1250-EDIT-SIGNED-9V2` | Credit Limit, Cash Credit Limit, Current Balance, Current Cycle Credit/Debit | optional sign, digits, comma/decimal; parsed to `BigDecimal`. |
| `1265-EDIT-US-SSN` | SSN parts | numeric; part 1 not `000`/`666`/`900-999`. |
| `1275-EDIT-FICO-SCORE` | FICO Score | numeric, `300`–`850`. |
| `1225-EDIT-ALPHA-REQD` / `1220-EDIT-ALPHA-OPT` | First/Last/City/Country (reqd), Middle (opt) | alphabetic + spaces. |
| `1235-EDIT-MANDATORY` | Address Line 1 | non-blank. |
| `1245-EDIT-NUM-REQD` | Zip, EFT Account Id | numeric, non-zero. |
| `1270-EDIT-US-STATE-CD` | State | valid 2-letter US state (56 codes). |
| `1260-EDIT-US-PHONE-NUM` | Phone 1 / Phone 2 | optional; when present, valid area code (410 codes), non-zero prefix/line. |
| `1280-EDIT-US-STATE-ZIP-CD` | State + Zip | first 2 zip digits must be a valid combo for the state (240 combos). |

Lookup tables extracted from `CSLKPCDY.cpy` live in
`carddemo-app/src/main/resources/cs4/` (`us-area-codes.txt`, `us-state-codes.txt`,
`us-state-zip2.txt`) and are loaded by `LookupCodes`.

## Output messages (verbatim)

`Changes committed to database`, `No change detected with respect to values fetched.`,
`Did not find this account in account card xref file`,
`Did not find this account in account master file`,
`Did not find associated customer in master file`,
`Account Filter must  be a non-zero 11 digit number` (view),
`Account Number if supplied must be a 11 digit Non-Zero Number` (update).

## Navigation integration

`AccountViewScreenHandler` / `AccountUpdateScreenHandler` implement `ScreenHandler`,
returning `ScreenResult.stay(message, model)` with the view/update model or the COBOL message.
PF3 returns to the caller. Registering these handlers means a launch of `CAVW`/`CAUP` now
dispatches to CS-4 (the first entry runs and leaves the context `REENTER`); the CS-3
`NavigationControllerTest` re-enter demonstration was moved to an unhandled program (`CR00`)
to keep exercising the no-handler bookkeeping path.

## Tests

- `AccountServiceTest` — view (known account, unpadded id, unknown account 404, zero-id
  reject), valid update commit, invalid FICO / invalid state-zip combo rejects, no-change
  detection. Driven against the real seed data (`carddemo.seed.enabled=true`); account
  `00000000050` → customer `000000050`.
- `AccountControllerTest` — MockMvc `GET`/`PUT` with an authenticated principal
  (`@WithMockUser`): successful view, unknown account 404, valid update commit, invalid-field
  400 with the COBOL message.
