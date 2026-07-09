# CS-2 — Security (Spring Security roles + COSGN00C sign-on)

WAVE 2 of the CardDemo COBOL→Java migration. This document maps the sign-on program
[`app/cbl/COSGN00C.cbl`](../../../app/cbl/COSGN00C.cbl) and its security copybook
[`app/cpy/CSUSR01Y.cpy`](../../../app/cpy/CSUSR01Y.cpy) to a Spring Security 6 configuration,
a `UserDetailsService` backed by the USRSEC store, and a REST sign-on/sign-off flow.

It builds on CS-1 (data model): the `SecurityUser` entity + `SecurityUserRepository` and the
USRSEC seed users (loaded from `DUSRSECJ.jcl` inline records via `dataLoadJob`) already exist.
See [`CS-1-data-model.md`](CS-1-data-model.md).

## Scope

Everything added by CS-2 lives in the `com.carddemo.security` package (module `carddemo-app`),
which this wave owns. No changes were made under `app/` (COBOL/JCL/BMS/copybooks) and no
generic web/session config was added — reading the authenticated principal for
navigation/COMMAREA state is CS-3's responsibility.

## Artifacts

| Layer | Location |
|-------|----------|
| Security config | `com.carddemo.security.SecurityConfig` |
| User details service | `com.carddemo.security.CardDemoUserDetailsService` |
| Principal (UserDetails) | `com.carddemo.security.CardDemoUserDetails` |
| Role / routing mapping | `com.carddemo.security.CardDemoRole` |
| Sign-on service | `com.carddemo.security.AuthService` |
| Sign-on controller | `com.carddemo.security.AuthController` |
| Request/response DTOs | `SignonRequest`, `SignonResponse`, `SignonErrorResponse` |
| Error messages | `com.carddemo.security.SignonMessages` |
| Error signalling | `com.carddemo.security.SignonException` |
| Tests | `carddemo-app/src/test/java/com/carddemo/security/AuthControllerTest.java` |

## `CSUSR01Y` → `SecurityUser` (recap from CS-1) and role mapping

The copybook record (RECLN 80, VSAM key = first 8 bytes) maps to the `sec_user` table:

| COBOL field | PIC | Java (`SecurityUser`) | Used by CS-2 for |
|-------------|-----|-----------------------|-------------------|
| `SEC-USR-ID` | X(08) | `secUsrId` (`@Id`) | username / VSAM key lookup |
| `SEC-USR-FNAME` | X(20) | `secUsrFname` | response `firstName` |
| `SEC-USR-LNAME` | X(20) | `secUsrLname` | response `lastName` |
| `SEC-USR-PWD` | X(08) | `secUsrPwd` | credential (plaintext — see below) |
| `SEC-USR-TYPE` | X(01) | `secUsrType` | authority + routing |
| `SEC-USR-FILLER` | X(23) | *(unused)* | — |

### `SEC-USR-TYPE` → role → routing (`CardDemoRole`)

`COSGN00C` routes on `SEC-USR-TYPE` via `IF CDEMO-USRTYP-ADMIN … EXEC CICS XCTL PROGRAM('COADM01C')
… ELSE … PROGRAM('COMEN01C')`. Only `'A'` is admin; every other value falls through to the
regular main-menu path.

| `SEC-USR-TYPE` | Authority | `XCTL` program | Destination (API) |
|----------------|-----------|----------------|-------------------|
| `'A'` (admin) | `ROLE_ADMIN` | `COADM01C` | `ADMIN_MENU` |
| `'U'` / other | `ROLE_USER` | `COMEN01C` | `MAIN_MENU` |

## `COSGN00C` control flow → `AuthService.signon`

`PROCESS-ENTER-KEY` + `READ-USER-SEC-FILE` are reproduced in order:

| COBOL step | Java equivalent |
|------------|-----------------|
| `WHEN USERIDI = SPACES OR LOW-VALUES` | blank `userId` → `SignonException` / 400 |
| `WHEN PASSWDI = SPACES OR LOW-VALUES` | blank `password` → `SignonException` / 400 |
| `MOVE FUNCTION UPPER-CASE(USERIDI/PASSWDI)` | `toUpperCase(Locale.ROOT)` on both inputs |
| `EXEC CICS READ DATASET('USRSEC') RIDFLD(WS-USER-ID)` | `CardDemoUserDetailsService.loadUserByUsername` (repository `findById`) |
| `WHEN 13` (not found) | `UsernameNotFoundException` → `SignonException` / 401 |
| `IF SEC-USR-PWD = WS-USER-PWD` false | `BadCredentialsException` → `SignonException` / 401 |
| `WHEN OTHER` (unexpected RESP) | other `AuthenticationException` → `SignonException` / 500 |
| success + `XCTL COADM01C` / `COMEN01C` | authenticate, save `SecurityContext` to HTTP session, return `SignonResponse` with role/destination |

The record lookup is performed before delegating to the `AuthenticationManager` specifically so
the "user not found" and "wrong password" cases return *distinct* messages, exactly as the COBOL
distinguishes `WHEN 13` from the failed `IF SEC-USR-PWD = WS-USER-PWD` comparison
(`DaoAuthenticationProvider.hideUserNotFoundExceptions` is set to `false` for the same reason).

## Error-message mapping (`SignonMessages`)

Messages are reproduced verbatim from `COSGN00C` so the migrated API returns the exact wording
shown on the 3270 `ERRMSGO` field:

| Condition | Message | HTTP status |
|-----------|---------|-------------|
| Empty user id | `Please enter User ID ...` | 400 |
| Empty password | `Please enter Password ...` | 400 |
| User not found (`WHEN 13`) | `User not found. Try again ...` | 401 |
| Wrong password | `Wrong Password. Try again ...` | 401 |
| Unexpected read error (`WHEN OTHER`) | `Unable to verify the User ...` | 500 |

Failures are serialised as `SignonErrorResponse` (`{"message": "..."}`) by
`AuthController`'s `@ExceptionHandler`.

## REST endpoints

| Method + path | Purpose | Auth |
|---------------|---------|------|
| `POST /api/auth/signon` | Body `{userId, password}` → authenticate, establish session, return `{userId, firstName, lastName, userType, role, destination, program}` | public |
| `POST /api/auth/signoff` | Clear security context + invalidate HTTP session (web analogue of the `PF3` end-session path) | authenticated |

`GET /api/health` and `POST /api/auth/signon` are the only public endpoints; every other request
requires authentication and returns **401** when unauthenticated (a REST-friendly
`HttpStatusEntryPoint`, not the servlet default 403).

## Session model

Authentication is **session-based** (`HttpSessionSecurityContextRepository`,
`SessionCreationPolicy.IF_REQUIRED`) to fit the CICS pseudo-conversational model: `signon`
establishes a `SecurityContext` persisted in the `HttpSession`, and subsequent requests reuse it
via the session cookie. HTTP Basic and form login are disabled — sign-on is the REST endpoint.
CSRF is disabled because the API is driven by non-browser REST clients that do not carry a CSRF
cookie.

## Password strategy — legacy-compat decision (READ THIS)

USRSEC stores **8-character plaintext** passwords (all seed users share the password `PASSWORD`).
To validate the seed data unchanged and preserve the exact `COSGN00C` `IF SEC-USR-PWD = WS-USER-PWD`
comparison, CS-2 uses a **delegating `PasswordEncoder`**
(`PasswordEncoderFactories.createDelegatingPasswordEncoder()`) and presents each stored credential
with the `{noop}` prefix (`CardDemoUserDetails.getPassword()` returns `"{noop}" + password`).

This is a **deliberate migration-fidelity decision, explicitly documented and not silent.** It
does **not** endorse plaintext credentials. The delegating encoder was chosen precisely so the
store can be migrated to a real hash (e.g. `{bcrypt}`) transparently — each credential carries its
own `{id}` prefix, so re-hashed users validate alongside legacy `{noop}` users with no config
change.

**Follow-up (out of scope for CS-2):** re-hash USRSEC passwords to `{bcrypt}` and upgrade the seed
loader accordingly once the legacy data-parity checks no longer require plaintext.

## Exposing the principal to CS-3

CS-3 (session/navigation, COMMAREA) reads the authenticated user from the Spring Security context
(`SecurityContextHolder.getContext().getAuthentication().getPrincipal()`), which is a
`CardDemoUserDetails`. It exposes `getUserId()`, `getFirstName()`, `getLastName()` and
`getRole()` (a `CardDemoRole` carrying the authority, `XCTL` program and destination menu), so the
navigation wave can populate `CDEMO-USER-ID` / `CDEMO-USER-TYPE`-style COMMAREA state without
re-reading USRSEC.

## Tests (`AuthControllerTest`)

Driven against the real seed users (`carddemo.seed.enabled=true`):

- admin sign-on (`ADMIN001`/`PASSWORD`) → 200, `ROLE_ADMIN`, `ADMIN_MENU`, `COADM01C`;
- regular sign-on (`USER0001`/`PASSWORD`) → 200, `ROLE_USER`, `MAIN_MENU`, `COMEN01C`;
- lower-case input (`admin001`/`password`) still succeeds (upper-casing parity);
- wrong password → 401 + `Wrong Password. Try again ...`;
- unknown user → 401 + `User not found. Try again ...`;
- empty user id / empty password → 400 + the respective COBOL message;
- protected endpoint → 401 when unauthenticated, authorized (204) after sign-on reusing the
  session;
- `/api/health` remains public.
