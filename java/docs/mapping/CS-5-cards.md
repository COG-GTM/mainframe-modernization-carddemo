# CS-5 — Cards online (WAVE 3)

Ports the three online **card** programs to a Spring service + REST layer, plugged into the
CS-3 navigation framework:

| COBOL program | BMS map | Function | Java |
|---------------|---------|----------|------|
| `app/cbl/COCRDLIC.cbl` | `app/bms/COCRDLI.bms` | Card list (filter + page) | `CardListScreenHandler`, `GET /api/cards` |
| `app/cbl/COCRDSLC.cbl` | `app/bms/COCRDSL.bms` | Card detail / select | `CardViewScreenHandler`, `GET /api/cards/{cardNumber}` |
| `app/cbl/COCRDUPC.cbl` | `app/bms/COCRDUP.bms` | Card update | `CardUpdateScreenHandler`, `PUT /api/cards/{cardNumber}` |

All new code lives under `com.carddemo.web.card` (controller, DTOs, screen handlers) and
`com.carddemo.service.card` (service, messages, exceptions). Nothing in the shared
scaffolding, security (CS-2) or navigation (CS-3) packages was modified.

## Source artifacts

- Programs (read-only): `COCRDLIC.cbl`, `COCRDSLC.cbl`, `COCRDUPC.cbl`.
- Copybooks: `CVACT02Y` (`CARD-RECORD`), `CVACT03Y` (`CARD-XREF-RECORD`),
  `CVCRD01Y` (`WS-CARD-RIDFLD` / card work area).
- Entity/repository (CS-1): `com.carddemo.domain.Card`, `com.carddemo.repository.CardRepository`.

## 1. Record → DTO mapping (`CVACT02Y` `CARD-RECORD`)

Per the project rules (`java/README.md`): fixed-width numeric identifiers that are never
used in arithmetic map to `String` to preserve leading zeros; `PIC X(n)` maps to `String`.

| COBOL field | PIC | `Card` entity field | DTO field |
|-------------|-----|---------------------|-----------|
| `CARD-NUM`             | X(16) | `cardNum`             | `cardNumber` |
| `CARD-ACCT-ID`         | 9(11) | `cardAcctId`          | `accountId` |
| `CARD-CVV-CD`          | 9(03) | `cardCvvCd`           | `cvvCode` |
| `CARD-EMBOSSED-NAME`   | X(50) | `cardEmbossedName`    | `embossedName` |
| `CARD-EXPIRAION-DATE`  | X(10) | `cardExpirationDate`  | `expirationDate` (+ derived `expiryYear`/`expiryMonth`/`expiryDay`) |
| `CARD-ACTIVE-STATUS`   | X(01) | `cardActiveStatus`    | `activeStatus` |

DTOs (`com.carddemo.web.card.dto`): `CardSummaryResponse` (one list row — account, card,
status), `CardListResponse` (page envelope), `CardDetailResponse`, `CardUpdateRequest`
(editable fields only), `CardUpdateResponse`.

## 2. Card list — `COCRDLIC`

`GET /api/cards?accountId=&cardNumber=&page=`

- **Filters** (`1000-EDIT-MAPINPUTS`): a supplied account filter must be an 11-digit number,
  a supplied card filter a 16-digit number; blank means "no restriction". Both are ANDed
  (`9500-FILTER-RECORDS`). Invalid filters raise the verbatim messages
  `ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER` /
  `CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER`.
- **Paging** (`9000-READ-FORWARD` / `9100-READ-BACKWARDS`): the VSAM browse keyed ascending
  by `CARD-NUM` with `WS-MAX-SCREEN-LINES = 7` rows per page and the look-ahead next/prev
  flags is reproduced as an ordered, filtered slice — `pageSize = 7`, `hasNextPage` /
  `hasPreviousPage`. Empty result → `NO RECORDS FOUND FOR THIS SEARCH CONDITION.`
- **Access**: `CardController.resolveAccountFilter` reproduces COCRDLIC's admin rule — an
  admin (`ROLE_ADMIN`) filters freely, a regular user is pinned to the account in the
  session COMMAREA ("Only the ones associated with ACCT in COMMAREA if user is not admin").
- **Selection** (screen handler): the list handler moves the selected card into
  `CDEMO-CARD-NUM` on the COMMAREA and transfers to the detail (`S`) or update (`U`) screen;
  any other action code → `INVALID ACTION CODE`.

> **Fidelity note.** The card file is small (one card per account in the seed set), so the
> streaming `STARTBR`/`READNEXT` browse is realised as a load-then-slice in `CardService`
> rather than a stateful cursor. The externally observable behaviour (key order, 7-row
> pages, next/prev flags, filter semantics) is identical.

## 3. Card detail — `COCRDSLC`

`GET /api/cards/{cardNumber}` (optional `?accountId=`)

- `2220-EDIT-CARD`: card number required (`Card number not provided`) and a 16-digit number
  (`Card number if supplied must be a 16 digit number`).
- `2210-EDIT-ACCOUNT`: when an account is supplied it must be a non-zero 11-digit number
  (`Account number must be a non zero 11 digit number`).
- `9100-GETCARD-BYACCTCARD`: read by card number; not found (or account mismatch) →
  HTTP 404 `Did not find cards for this search condition`.

## 4. Card update — `COCRDUPC`

`PUT /api/cards/{cardNumber}` with body `{embossedName, activeStatus, expiryMonth, expiryYear}`

Only the embossed name, active status and expiry month/year are editable; the expiry **day**,
CVV and account are carried unchanged from the fetched record (as the COCRDUP map does).

| Edit paragraph | Rule | Message |
|----------------|------|---------|
| `1230-EDIT-NAME`         | required; letters and spaces only | `Card name not provided` / `Card name can only contain alphabets and spaces` |
| `1240-EDIT-CARDSTATUS`   | `Y` or `N`                        | `Card Active Status must be Y or N` |
| `1250-EDIT-EXPIRY-MON`   | numeric `1`..`12`                 | `Card expiry month must be between 1 and 12` |
| `1260-EDIT-EXPIRY-YEAR`  | numeric `1950`..`2099`            | `Invalid card expiry year` |

- `9300-CHECK-CHANGE-IN-REC`: if the submitted values equal the fetched record (name
  compared case-insensitively, as the COBOL upper-cases before comparing) →
  `No change detected with respect to values fetched.` and no write.
- `9200-WRITE-PROCESSING`: on success the record is rewritten and
  `Changes committed to database` is returned with the refreshed detail.

> **Fidelity note.** COCRDUPC's optimistic-lock re-read (`READ ... UPDATE` +
> concurrent-change detection before `REWRITE`) is handled by the JPA transaction; the
> multi-step confirm state machine (`CCUP-CHANGE-ACTION`) is collapsed to a single validated
> `PUT`. Because the shared `PfKey` enum has no PF5 (the COBOL "save" key), the update screen
> handler drives the save on ENTER carrying the edited fields.

## 5. Errors

Field-edit failures raise `CardValidationException` → HTTP 400; not-found raises
`CardNotFoundException` → HTTP 404. Both are translated by controller-local
`@ExceptionHandler` methods carrying the verbatim COBOL message (`{"message": "..."}`); no
global exception handler is introduced. All literals live in `CardMessages`.

## 6. Tests

`cd java && mvn -B verify` (green):

- `CardServiceTest` — filter edits, 7-row paging + ordering, detail found/not-found/edits,
  update field validations, no-change detection.
- `CardControllerTest` — `@SpringBootTest` + MockMvc against the real seed cards with an
  authenticated principal: unauthenticated 401, admin list paging + card filter, non-admin
  COMMAREA-account restriction, detail found/404/bad-key, update persist + invalid + 404.
- `CardScreenHandlerTest` — list paging/selection/back, detail fetch/not-found, update
  fetch/apply for the three `ScreenHandler` beans.
