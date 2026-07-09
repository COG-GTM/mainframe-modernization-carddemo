# CS-9 — User administration (COUSR00C / COUSR01C / COUSR02C / COUSR03C)

WAVE 3 (online) of the CardDemo COBOL→Java migration. This document maps the four online
user-maintenance programs to a Spring REST CRUD API over the USRSEC store, guarded by
`ROLE_ADMIN`, plus the CS-3 screen handlers that plug the programs into the navigation
framework.

Source artifacts (read-only, under [`../../../app/`](../../../app/)):

| Program | Function | BMS map | TRANSID |
|---------|----------|---------|---------|
| [`COUSR00C.cbl`](../../../app/cbl/COUSR00C.cbl) | List users | `COUSR00.bms` | `CU00` |
| [`COUSR01C.cbl`](../../../app/cbl/COUSR01C.cbl) | Add user | `COUSR01.bms` | `CU01` |
| [`COUSR02C.cbl`](../../../app/cbl/COUSR02C.cbl) | Update user | `COUSR02.bms` | `CU02` |
| [`COUSR03C.cbl`](../../../app/cbl/COUSR03C.cbl) | Delete user | `COUSR03.bms` | `CU03` |
| [`CSUSR01Y.cpy`](../../../app/cpy/CSUSR01Y.cpy) | USRSEC record (`SEC-USER-DATA`, RECLN 80) | — | — |

All four are reached only from the admin menu `COADM01C` (see `CardDemoProgram`
`USER_LIST`/`USER_ADD`/`USER_UPDATE`/`USER_DELETE`, all `AccessLevel.ADMIN`) — they are
admin-only.

## Scope & package ownership

Everything CS-9 adds lives under the two packages it owns (module `carddemo-app`):

| Layer | Location |
|-------|----------|
| Service | `com.carddemo.service.useradmin.UserAdminService` |
| Messages | `com.carddemo.service.useradmin.UserAdminMessages` |
| Error signalling | `com.carddemo.service.useradmin.UserAdminException` |
| REST controller | `com.carddemo.web.useradmin.UserAdminController` |
| Method-security enable | `com.carddemo.web.useradmin.UserAdminMethodSecurityConfig` |
| Screen handlers | `com.carddemo.web.useradmin.User{List,Add,Update,Delete}ScreenHandler` |
| Request/response DTOs | `com.carddemo.web.useradmin.dto.*` |
| Tests | `carddemo-app/src/test/java/com/carddemo/web/useradmin/*` |

It reuses the CS-1 `SecurityUser` entity + `SecurityUserRepository` and the CS-2 role model
(`ROLE_ADMIN`/`ROLE_USER`, `PasswordEncoder`). No changes were made under `app/`, to
`pom.xml`/`application.yml`, to the CS-2 `SecurityConfig`, or to any shared session class; no
new Flyway migration is needed (the `sec_user` table already exists).

## `CSUSR01Y` → `SecurityUser` fields used

| COBOL field | PIC | `SecurityUser` | API field |
|-------------|-----|----------------|-----------|
| `SEC-USR-ID` | X(08) | `secUsrId` (`@Id`) | `userId` |
| `SEC-USR-FNAME` | X(20) | `secUsrFname` | `firstName` |
| `SEC-USR-LNAME` | X(20) | `secUsrLname` | `lastName` |
| `SEC-USR-PWD` | X(08) | `secUsrPwd` | `password` (write-only) |
| `SEC-USR-TYPE` | X(01) | `secUsrType` | `userType` ('A'/'U') |

Per the repo conventions, `PIC X(n)` → trimmed `String`; the id is a fixed-width `String`
(never used in arithmetic).

## REST API

| Method & path | COBOL | Success | Errors |
|---------------|-------|---------|--------|
| `GET /api/admin/users?startUserId=&page=&size=` | `COUSR00C` | `200` page of `UserSummary` | — |
| `POST /api/admin/users` | `COUSR01C` | `201` + confirmation | `400` validation, `409` duplicate |
| `PUT /api/admin/users/{userId}` | `COUSR02C` | `200` + confirmation / no-op | `400` validation, `404` not-found |
| `DELETE /api/admin/users/{userId}` | `COUSR03C` | `200` + confirmation | `404` not-found |

### List (`COUSR00C`) — BMS paging

`COUSR00C` browses USRSEC (`STARTBR`/`READNEXT` on `SEC-USR-ID`) filling the
`USER-REC OCCURS 10 TIMES` array, tracking `CDEMO-CU00-PAGE-NUM` and the PF8 next-page flag
`CDEMO-CU00-NEXT-PAGE-FLG`. The service reproduces this over `SecurityUserRepository.findAll`
sorted by `secUsrId` ascending:

- `startUserId` positions the window like the `STARTBR RIDFLD(SEC-USR-ID)` browse key
  (records with id ≥ the start key); blank starts at the first record.
- `size` defaults to `10` (the `OCCURS 10`); `page` is 1-based (`CDEMO-CU00-PAGE-NUM`).
- `moreAvailable` ⇔ `NEXT-PAGE-YES` (a further page exists → PF8 works).

The COBOL per-row `'U'`/`'D'` selection (transfer to `COUSR02C`/`COUSR03C`) is reproduced in
`UserListScreenHandler` (see below), not on the REST list.

### Add (`COUSR01C` `PROCESS-ENTER-KEY` + `WRITE-USER-SEC-FILE`)

Validation is applied in the exact COBOL `EVALUATE TRUE` order — first-name, last-name,
user-id, password, user-type — each emitting the verbatim message. Then `existsById` reproduces
the `WRITE` `DUPKEY/DUPREC` branch. Success mirrors the COBOL
`STRING 'User ' … SEC-USR-ID DELIMITED BY SPACE … ' has been added ...'`.

### Update (`COUSR02C` `UPDATE-USER-INFO`)

Required-field validation (same messages) → `READ` (`NOTFND` → `User ID NOT found...`) →
per-field diff (`IF field NOT = SEC-USR-…` sets `USR-MODIFIED-YES`) → `REWRITE`. When nothing
differs the COBOL shows `Please modify to update ...` and does not rewrite; the service returns
`200` with that message and the unchanged record (a no-op, not an error). The id is the VSAM
key and is taken from the path — it is never modified.

### Delete (`COUSR03C` `DELETE-USER-INFO`)

`READ` first (`NOTFND` → `User ID NOT found...`) then `DELETE`; success mirrors
`… ' has been deleted ...'`.

### Messages (`UserAdminMessages`)

Verbatim from the COBOL: `First Name can NOT be empty...`, `Last Name can NOT be empty...`,
`User ID can NOT be empty...`, `Password can NOT be empty...`, `User Type can NOT be empty...`,
`User ID already exist...`, `User ID NOT found...`, `Please modify to update ...`, and the
`User <id> has been added/updated/deleted ...` builders.

**Extensions (no direct COBOL message, implied by the copybook / domain).** The 3270 fields are
fixed width and `SEC-USR-TYPE` is a 1-char `'A'`/`'U'` domain, so CS-9 additionally rejects
over-width input and an out-of-domain type with `400` and a descriptive message
(`User ID must be 8 characters or less...`, `… 20 characters or less...`,
`User Type must be 'A' (admin) or 'U' (user)...`). These guard the DB column widths that the
BMS map enforced implicitly.

## Security

`UserAdminController` is annotated `@PreAuthorize("hasRole('ADMIN')")`; CS-9 enables method
security with an additive `@EnableMethodSecurity` configuration
(`UserAdminMethodSecurityConfig`) rather than editing the CS-2 filter chain. Net effect:

- unauthenticated → `401` (CS-2 `HttpStatusEntryPoint`);
- authenticated `ROLE_USER` → `403` (method-security `AccessDeniedException`);
- `ROLE_ADMIN` → allowed.

### Password handling

USRSEC stores 8-char **plaintext** passwords; `CardDemoUserDetails.getPassword()` prefixes the
stored value with `{noop}` for the delegating `PasswordEncoder` (CS-2 decision). New/updated
passwords are therefore persisted as plaintext to match the seed convention — running them
through `PasswordEncoder.encode` (bcrypt by default) would break both the 8-char column and the
`{noop}` comparison path, i.e. it would be inconsistent with the existing `SecurityUser` +
sign-on flow. Re-hashing the credential store remains the CS-2 follow-up.

## Screen handlers (CS-3 integration)

One `ScreenHandler` bean per program, registered with `ScreenRegistry` via `CardDemoProgram`:

- `UserListScreenHandler` (`USER_LIST`) — page model + PF7/PF8 paging; `action=U`/`D`
  transfers to update/delete; other flags → `Invalid selection. Valid values are U and D`.
- `UserAddScreenHandler` (`USER_ADD`) — first entry presents the form; ENTER submits to
  `add`; PF4 clears.
- `UserUpdateScreenHandler` (`USER_UPDATE`) — ENTER with only `userId` loads/displays the
  record; ENTER with edits saves (`update`); PF4 clears; PF12 returns.
- `UserDeleteScreenHandler` (`USER_DELETE`) — ENTER displays for confirmation; ENTER with
  `confirm=Y` deletes; PF4 clears; PF12 returns.

Note: the CS-3 `PfKey` enum has no `PF5`, so the COBOL "PF5 = save/delete" action is represented
by ENTER-with-edits (update) / a `confirm=Y` field (delete). PF3 (`BACK`) is handled by the
`NavigationController` before dispatch.

## Tests

`carddemo-app/src/test/java/com/carddemo/web/useradmin/`:

- `UserAdminControllerTest` — end-to-end against the real USRSEC seed users: list (ordered,
  paged), add (valid `201`, duplicate `409`, empty first-name `400`, bad type `400`), update
  (`200`), update not-found (`404`), delete existing (`200`), delete not-found (`404`),
  `ROLE_USER` → `403`, unauthenticated → `401`.
- `UserAdminScreenHandlerTest` — the four programs have handlers registered with
  `ScreenRegistry`.

`cd java && mvn -B verify` is green.
