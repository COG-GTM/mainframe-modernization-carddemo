# CS-8 — Menus (COMEN01C / COADM01C) (WAVE 3 ONLINE)

Migrates the two CardDemo online menu programs to Spring Boot on top of the CS-3
session/navigation framework (`com.carddemo.session`, see
[`CS-3-session-navigation.md`](CS-3-session-navigation.md)):

- **Main menu** — `app/cbl/COMEN01C.cbl` (TRANSID `CM00`, BMS `app/bms/COMEN01.bms`,
  options `app/cpy/COMEN02Y.cpy`) for regular users.
- **Admin menu** — `app/cbl/COADM01C.cbl` (TRANSID `CA00`, BMS `app/bms/COADM01.bms`,
  options `app/cpy/COADM02Y.cpy`) for admin users.

No new tables. This wave only **adds** Java under `java/` and touches nothing in the shared
`com.carddemo.session` framework — it plugs in via `ScreenHandler` beans and a small read-only
controller.

## Source artifacts

| Artifact | Role |
|----------|------|
| `app/cbl/COMEN01C.cbl` | Main-menu program: `PROCESS-ENTER-KEY`, `BUILD-MENU-OPTIONS`, PF3, `XCTL`. |
| `app/cbl/COADM01C.cbl` | Admin-menu program (same shape; no per-option access check). |
| `app/cpy/COMEN02Y.cpy` | `CARDDEMO-MAIN-MENU-OPTIONS` — 10 options (num / name / pgm / usrtype). |
| `app/cpy/COADM02Y.cpy` | `CARDDEMO-ADMIN-MENU-OPTIONS` — 4 options (num / name / pgm). |
| `app/bms/COMEN01.bms`, `COADM01.bms` | Screen layout; `OPTION` is the 2-char numeric input field. |

## Package layout (added)

| Package | Class | Responsibility |
|---------|-------|----------------|
| `com.carddemo.service.menu` | `MenuCatalog` | Option lists ported from the option copybooks; TRANSIDs resolved from `ProgramRegistry`. |
| `com.carddemo.service.menu` | `MenuScreenHandler` (abstract) | Shared `PROCESS-ENTER-KEY` turn logic. |
| `com.carddemo.service.menu` | `MainMenuScreenHandler` | `ScreenHandler` for `CardDemoProgram.MAIN_MENU` (`COMEN01C`). |
| `com.carddemo.service.menu` | `AdminMenuScreenHandler` | `ScreenHandler` for `CardDemoProgram.ADMIN_MENU` (`COADM01C`). |
| `com.carddemo.web.menu` | `MenuController` | `GET /api/menu`, `GET /api/menu/admin`. |
| `com.carddemo.web.menu.dto` | `MenuResponse`, `MenuOptionDto` | Read DTOs. |

## Copybook → Java field mapping

Per `java/README.md`: fixed-width identifiers map to `String`; `PIC X(n)` → `String(n)`.

| COBOL field | PIC | `MenuOptionDto` field | Java type |
|-------------|-----|-----------------------|-----------|
| `CDEMO-*-OPT-NUM`     | 9(02) | `number`      | `String` (2-char zero-padded, e.g. `"01"`) |
| `CDEMO-*-OPT-NAME`    | X(35) | `name`        | `String` (trimmed) |
| `CDEMO-*-OPT-PGMNAME` | X(08) | `programName` | `String` (`XCTL` target) |
| (`ProgramRegistry` lookup) | — | `tranId`      | `String` (CICS TRANSID) |
| `CDEMO-MENU-OPT-USRTYPE = 'A'` | X(01) | `adminOnly` | `boolean` (admin options always `true`) |

Each option's `tranId` is resolved from `ProgramRegistry.byProgramName(...)` rather than
hard-coded, so the wave reuses the single `CardDemoProgram` registry established by CS-3.

## Behaviour mapping (`PROCESS-ENTER-KEY`)

| COBOL | Java |
|-------|------|
| `IF NOT CDEMO-PGM-REENTER … SEND-MENU-SCREEN` (first entry displays the map) | `MenuScreenHandler`: `navigation.beginTurn(...)` returns `true` → `ScreenResult.stay(options)`. |
| Parse `OPTIONI` → `WS-OPTION-X` (JUST RIGHT, blanks→`0`) → `WS-OPTION` `PIC 9(02)` | `parseOption`: trim; blank or non-digit → invalid; `0` → invalid. |
| `IF WS-OPTION NOT NUMERIC OR > count OR = ZEROS` → `"Please enter a valid option number..."` | invalid / out-of-range number → `ScreenResult.stay(INVALID_OPTION_MESSAGE, options)`. |
| `COMEN01C`: `IF CDEMO-USRTYP-USER AND …USRTYPE = 'A'` → `"No access - Admin Only option..."` | `MainMenuScreenHandler.isAdminOnlyDenied` → `ScreenResult.stay(ADMIN_ONLY_MESSAGE, options)`. `COADM01C` has no such check → disabled in `AdminMenuScreenHandler`. |
| `XCTL PROGRAM(CDEMO-*-OPT-PGMNAME(WS-OPTION)) COMMAREA(...)` + `MOVE ZEROS TO CDEMO-PGM-CONTEXT` | `ScreenResult.transferTo(target)` → framework `transferControl(from, target)` (context = ENTER). |
| `WHEN DFHPF3` → `XCTL COSGN00C` | Handled generically by the framework `NavigationController` (`PF3` → `navigation.back` → caller / sign-on). |

## REST surface

| Endpoint | Auth | Purpose |
|----------|------|---------|
| `GET /api/menu` | any authenticated user | Main-menu option list, filtered for the signed-on user type (admin-only options hidden from regular users). |
| `GET /api/menu/admin` | admin only (`403` otherwise) | Admin-menu option list. |
| `POST /api/nav` (CS-3) | authenticated | Option **selection**: while on `MAIN_MENU`/`ADMIN_MENU`, submit `{"fields":{"option":"N"}}` — the menu `ScreenHandler` validates and transfers control to the option's program. `{"pfKey":"PF3"}` returns to the caller. |

`GET` endpoints render the menu (the `BUILD-MENU-OPTIONS` analogue); **selection** flows
through the CS-3 navigation framework so the transfer-of-control, ENTER/RE-ENTER bookkeeping
and COMMAREA session persistence are reused unchanged.

Note: after sign-on the menu is in ENTER context; the first `POST /api/nav` turn renders the
menu (ENTER → RE-ENTER, option list in the response `model`) and the next turn processes the
submitted `option` — exactly the `COMEN01C` send-then-receive pseudo-conversational cycle.

## Tests (`mvn -B verify`)

- `MenuControllerTest` — main-menu options for a regular user; admin-menu options for an
  admin; admin menu denied (`403`) to a regular user; unauthenticated `401`.
- `MenuNavigationTest` — selecting a main/admin option transfers control to the right target;
  invalid (out-of-range) and non-numeric option → error, stay on menu; PF3 returns to sign-on.
