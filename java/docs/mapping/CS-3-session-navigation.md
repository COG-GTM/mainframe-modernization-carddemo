# CS-3 — Session & navigation framework (WAVE 2)

Maps the CICS pseudo-conversational **COMMAREA** (`app/cpy/COCOM01Y.cpy`,
`CARDDEMO-COMMAREA`) and the online screen flow documented in the root
[`README.md`](../../../README.md) to a session-backed REST navigation framework in the
`com.carddemo.session` package.

This wave builds **only** the generic framework. It authenticates nothing — the
authenticated principal comes from the Spring Security layer (CS-2) and is read (never
configured) via `CurrentUserProvider`. WAVE 3 function waves (CS-4..CS-9) plug their
screens into this framework; see [How WAVE 3 integrates](#how-wave-3-function-sessions-integrate).

## Source artifacts

- Copybook: `app/cpy/COCOM01Y.cpy` — `01 CARDDEMO-COMMAREA`.
- Flow / inventory: root `README.md` — "Application Inventory > Online" table and the
  sign-on → menu → function screen flow.
- Reference programs (pattern, read-only): `app/cbl/COSGN00C.cbl` (sign-on routing),
  `app/cbl/COMEN01C.cbl` (menu, ENTER/RE-ENTER, PF3, XCTL).

## 1. COMMAREA → `CardDemoCommarea`

`CardDemoCommarea` mirrors the copybook field-for-field. The COBOL `05`-level groups become
nested classes; the `88`-levels become enums (`UserType`, `ProgramContext`). Per the
project mapping rules, fixed-width numeric identifiers (never used in arithmetic) map to
`String` to preserve leading zeros; single-character flags map to enums via `String`
accessors.

| COBOL field | PIC | Group / nested class | Java field | Java type |
|-------------|-----|----------------------|------------|-----------|
| `CDEMO-FROM-TRANID`  | X(04) | `GeneralInfo`  | `fromTranId`     | `String` |
| `CDEMO-FROM-PROGRAM` | X(08) | `GeneralInfo`  | `fromProgram`    | `String` |
| `CDEMO-TO-TRANID`    | X(04) | `GeneralInfo`  | `toTranId`       | `String` |
| `CDEMO-TO-PROGRAM`   | X(08) | `GeneralInfo`  | `toProgram`      | `String` |
| `CDEMO-USER-ID`      | X(08) | `GeneralInfo`  | `userId`         | `String` |
| `CDEMO-USER-TYPE`    | X(01) | `GeneralInfo`  | `userType`       | `String` ↔ `UserType` (`A`=ADMIN, `U`=USER) |
| `CDEMO-PGM-CONTEXT`  | 9(01) | `GeneralInfo`  | `pgmContext`     | `int` ↔ `ProgramContext` (`0`=ENTER, `1`=REENTER) |
| `CDEMO-CUST-ID`      | 9(09) | `CustomerInfo` | `custId`         | `String` (9-char id, no arithmetic) |
| `CDEMO-CUST-FNAME`   | X(25) | `CustomerInfo` | `custFirstName`  | `String` |
| `CDEMO-CUST-MNAME`   | X(25) | `CustomerInfo` | `custMiddleName` | `String` |
| `CDEMO-CUST-LNAME`   | X(25) | `CustomerInfo` | `custLastName`   | `String` |
| `CDEMO-ACCT-ID`      | 9(11) | `AccountInfo`  | `acctId`         | `String` (11-char id, no arithmetic) |
| `CDEMO-ACCT-STATUS`  | X(01) | `AccountInfo`  | `acctStatus`     | `String` |
| `CDEMO-CARD-NUM`     | 9(16) | `CardInfo`     | `cardNum`        | `String` (16-char PAN, no arithmetic) |
| `CDEMO-LAST-MAP`     | X(7)  | `MoreInfo`     | `lastMap`        | `String` |
| `CDEMO-LAST-MAPSET`  | X(7)  | `MoreInfo`     | `lastMapset`     | `String` |

The `88`-levels map as:

| COBOL 88-level | Enum |
|----------------|------|
| `CDEMO-USRTYP-ADMIN VALUE 'A'` | `UserType.ADMIN` |
| `CDEMO-USRTYP-USER  VALUE 'U'` | `UserType.USER` |
| `CDEMO-PGM-ENTER   VALUE 0`    | `ProgramContext.ENTER` (`commarea.isEnter()`) |
| `CDEMO-PGM-REENTER VALUE 1`    | `ProgramContext.REENTER` (`commarea.isReenter()`) |

### COMMAREA persistence

`CommareaSessionStore` keeps the `CardDemoCommarea` as an `HttpSession` attribute. This is
the Java analogue of `EXEC CICS RETURN TRANSID(..) COMMAREA(..)`: the structure is
re-presented on the next terminal interaction. Across stateless REST calls the commarea is
re-loaded from the session, so selected account/card/customer context survives exactly as
it does between pseudo-conversational turns.

## 2. Pseudo-conversational flow → REST navigation framework

| CICS concept | Framework element |
|--------------|-------------------|
| TRANSID ↔ program registry (`README` online inventory) | `CardDemoProgram` enum + `ProgramRegistry` |
| `EIBAID` attention keys (`DFHENTER`, `DFHPF3`, …) | `PfKey` → `CommonAction` |
| `XCTL PROGRAM(target) COMMAREA(..)` + `MOVE ZEROS TO CDEMO-PGM-CONTEXT` | `NavigationService.transferControl(from, to)` (sets from/to, context = ENTER) |
| `IF NOT CDEMO-PGM-REENTER SET CDEMO-PGM-REENTER TO TRUE` | `NavigationService.beginTurn(commarea)` (true on first entry, then flips to RE-ENTER) |
| `EXEC CICS RETURN TRANSID(self)` (stay on screen) | `NavigationService.stay(commarea)` |
| `COSGN00C`: `IF CDEMO-USRTYP-ADMIN XCTL COADM01C ELSE XCTL COMEN01C` | `NavigationService.signon(..)` + `ProgramRegistry.menuFor(userType)` |
| `COMEN01C` PF3: return to caller, fall back to `COSGN00C` | `NavigationService.back(commarea)` |
| `COMEN01C`: "No access - Admin Only option..." | `CardDemoProgram.isAccessibleBy(userType)` (via `AccessLevel`) |
| Sign-on moves USRSEC id/type into commarea | `CurrentUserProvider` reads the authenticated principal (CS-2) |

### `CardDemoProgram` registry

Every online transaction from the README inventory is registered with its TRANSID, COBOL
program, BMS map, function label, and an `AccessLevel` (`PUBLIC`, `USER`, `ADMIN`):

`SIGNON` CC00/COSGN00C · `MAIN_MENU` CM00/COMEN01C · `ADMIN_MENU` CA00/COADM01C ·
`ACCOUNT_VIEW` CAVW · `ACCOUNT_UPDATE` CAUP · `CARD_LIST` CCLI · `CARD_VIEW` CCDL ·
`CARD_UPDATE` CCUP · `TRANSACTION_LIST` CT00 · `TRANSACTION_VIEW` CT01 · `TRANSACTION_ADD`
CT02 · `REPORTS` CR00 · `BILL_PAYMENT` CB00 · `USER_LIST` CU00 · `USER_ADD` CU01 ·
`USER_UPDATE` CU02 · `USER_DELETE` CU03.

### PF-key / common-action handling

`PfKey` maps the 3270 attention keys to logical `CommonAction`s at the framework level:
`ENTER → SUBMIT`, `PF3 → BACK`, `PF4 → CLEAR`, `PF7 → PAGE_UP`, `PF8 → PAGE_DOWN`,
`PF12 → CANCEL`. The central controller handles `PF3` (BACK) generically — returning to the
calling program in `CDEMO-FROM-PROGRAM` (or the sign-on screen when there is none) — and
passes every other key to the current screen handler.

### REST surface (`/api/nav`)

| Endpoint | Purpose |
|----------|---------|
| `POST /api/nav/signon` | Initialise the commarea for the authenticated user and route to the admin/main menu by role. |
| `GET  /api/nav`        | Return the current commarea routing snapshot (`NavigationResponse`) without changing it. |
| `POST /api/nav`        | Perform one turn: apply the PF key, launch a selected `tranId`, and/or dispatch to the current screen's handler. |

`NavigationResponse` exposes the routing state (`from*`/`to*`, `currentProgram`,
`userId`/`userType`, `context` ENTER/REENTER, selected `acctId`/`cardNum`/`custId`, plus an
optional `message`/`model` from a screen handler).

## How WAVE 3 function sessions integrate

WAVE 3 waves add one screen per online program **without modifying this framework**:

1. **Reuse the identifier.** Use the existing `CardDemoProgram` constant for the screen
   (e.g. `ACCOUNT_VIEW`). Do not invent new TRANSIDs.
2. **Implement `ScreenHandler`.** Declare a Spring `@Component` implementing
   `ScreenHandler`:
   - `program()` returns the owned `CardDemoProgram`.
   - `handle(ScreenRequest, CardDemoCommarea)`:
     - call `navigationService.beginTurn(commarea)` to tell **ENTER** (initialise: clear/seed
       screen state) from **RE-ENTER** (process submitted `request.fields()`);
     - read/write the selected-entity context on the commarea (e.g.
       `commarea.getAccountInfo().setAcctId(..)`);
     - return a `ScreenResult`: `stay()` (re-display), `transferTo(program)` (XCTL to the
       next screen), or `back()` (PF3 return).
3. **Nothing else.** `ScreenRegistry` auto-discovers the bean and the central
   `NavigationController` dispatches to it — the transfer-of-control, ENTER/RE-ENTER
   bookkeeping and session persistence are handled by the framework.

Menu waves route by returning `ScreenResult.transferTo(...)` for the chosen option (respecting
`CardDemoProgram.isAccessibleBy` for admin-only functions); leaf screens return
`ScreenResult.back()` on PF3.
