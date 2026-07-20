# CardDemo Mainframe Modernization Playbook

A repeatable, generalizable procedure for migrating CardDemo's online (CO\*) and
batch (CB\*) COBOL programs into cloud-deployable services, backed by a layered
automated test suite that proves **functional parity** with the mainframe, and
delivered as small, dependency-ordered pull requests.

This playbook was authored while performing the **first** migration —
`COSGN00C` (CICS transaction `CC00`, the Signon screen) → the Spring Boot
`modernized/signon-service`. That migration is used throughout as the worked
example. Everything here is written so a different engineer can apply it to the
next program **in parallel**, and so the same approach can be reused in other
mainframe repositories.

> **How to read this document**
> - Sections 1–6 are the repeatable steps (inventory → data → logic/UI →
>   testing → PRs → playbook maintenance).
> - Section 7 is the checklist you copy per program.
> - Section 8 is the dependency-classification scheme + parallelization plan for
>   the rest of CardDemo.
> - Section 9 marks what is **reusable across repositories** vs
>   **CardDemo-specific**.

---

## 0. Principles

1. **Parity first.** The mainframe program is the specification. Observable
   behavior — validation order, error message text, and navigation targets — is
   preserved exactly. Message strings are copied verbatim and asserted in tests.
2. **The COBOL is the source of truth for test cases.** Every `EVALUATE` /
   `IF` branch becomes at least one test.
3. **Small, independent, dependency-ordered PRs.** Each PR is independently
   reviewable, carries its own tests, avoids mixing refactors with features, and
   states its scope and dependencies. Target a few hundred lines.
4. **Layered tests + CI from day one.** Unit → integration → e2e, wired into CI
   so parity is continuously enforced.
5. **Faithful data model.** Copybook field names, order, and lengths are carried
   into the datastore model; VSAM keys become primary keys.
6. **Generalize as you go.** Anything you do more than once (COMMAREA handling,
   PF-key mapping, fixed-width parsing) becomes a documented, reusable pattern.

---

## 1. Inventory a COBOL/CICS program and its dependencies

Produce a one-page dependency map before writing any code. For CardDemo the
sources live in `app/cbl` (programs), `app/cpy` (copybooks), `app/cpy-bms`
(generated BMS symbolic maps), `app/bms` (BMS mapsets), and `app/jcl` +
`app/data` (load jobs and sample data).

### 1.1 Steps

1. **Read the program** (`app/cbl/<PROGRAM>.cbl`). Note `PROGRAM-ID`, the
   transaction id (`WS-TRANID`), and the paragraph structure of the
   `PROCEDURE DIVISION`.
2. **List `COPY` statements.** These are the data contracts. Classify each:
   - **Shared session/common** (e.g. `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`,
     `CSMSG01Y`) — reused by many programs; migrate once into a shared module.
   - **Program/record specific** (e.g. `CSUSR01Y` for USRSEC) — becomes this
     service's data model.
   - **BMS symbolic map** (`app/cpy-bms/<MAP>.CPY`) — the screen I/O contract.
   - **CICS system** (`DFHAID`, `DFHBMSCA`) — replaced by framework constructs,
     not migrated as data.
3. **Identify the BMS mapset/map** (`app/bms/<MAPSET>.bms`). Extract the
   editable fields (the modern form inputs) and the output/error fields.
4. **Identify data files.** For each `EXEC CICS READ/WRITE/REWRITE/STARTBR` or
   batch `SELECT`, record the dataset, its copybook, VSAM organization, and key.
   Cross-reference the README data table and the `app/jcl` loader (e.g.
   `DUSRSECJ.jcl`) + sample data in `app/data`.
5. **Map navigation.** Record every `EXEC CICS XCTL`/`LINK`/`RETURN TRANSID`
   target — these are the downstream programs and define the routing contract.
6. **Extract the branch table.** Transcribe each `EVALUATE`/`IF` outcome
   (condition → message/action). This is your test matrix.

### 1.2 Worked example — `COSGN00C`

| Aspect | Finding |
| --- | --- |
| Program / Tran | `COSGN00C` / `CC00` |
| Shared copybooks | `COCOM01Y` (COMMAREA), `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y` |
| Record copybook | `CSUSR01Y` → `SEC-USER-DATA` (80 bytes) |
| BMS | mapset `COSGN00`, map `COSGN0A`; inputs `USERID`, `PASSWD`; output `ERRMSG` |
| Data file | `USRSEC` VSAM **KSDS**, key `SEC-USR-ID` (8), rec 80; loaded by `DUSRSECJ` |
| Navigation | `XCTL COADM01C` (admin), `XCTL COMEN01C` (regular) |

Branch table (from `PROCESS-ENTER-KEY` / `READ-USER-SEC-FILE`), in order:

| # | Condition | Outcome | Message / target |
| --- | --- | --- | --- |
| 1 | `USERIDI` = spaces/low-values | stay on screen | `Please enter User ID ...` |
| 2 | `PASSWDI` = spaces/low-values | stay on screen | `Please enter Password ...` |
| 3 | READ resp 0 & pwd matches & type `A` | route | `XCTL COADM01C` |
| 4 | READ resp 0 & pwd matches & type ≠ `A` | route | `XCTL COMEN01C` |
| 5 | READ resp 0 & pwd differs | stay on screen | `Wrong Password. Try again ...` |
| 6 | READ resp 13 (NOTFND) | stay on screen | `User not found. Try again ...` |
| 7 | READ resp other | stay on screen | `Unable to verify the User ...` |

Non-obvious parity detail: `COSGN00C` does
`MOVE FUNCTION UPPER-CASE(...)` on **both** the user id and password before the
lookup/compare, so credential matching is **case-insensitive**. Capture details
like this — they are easy to miss and are exactly what parity tests catch.

---

## 2. Map VSAM files and copybook record layouts to a cloud data model

### 2.1 VSAM organization → storage choice

| VSAM type | Access pattern | Modern equivalent |
| --- | --- | --- |
| **KSDS** (key-sequenced) | key lookup + sequential | RDBMS table with PK (or key-value store) |
| **ESDS** (entry-sequenced) | append-only, sequential | append-only/log table, event store |
| **RRDS** (relative record) | direct by slot | table with integer PK |
| **AIX** (alternate index) | secondary key over KSDS | secondary index or lookup table |

### 2.2 Copybook record → table/entity

- Each `05`-level field → a column with an equivalent type and **the same
  length** (`PIC X(n)` → varchar/char length `n`).
- The `SELECT ... RECORD KEY` / KSDS `KEYS(len,off)` → the primary key.
- **Never** use floating-point for `PIC S9(n)V9(m)` money — use `BigDecimal` /
  `NUMERIC(p,s)` with the copybook's implied decimal scale.
- Trailing `FILLER` is record padding; it needs no column but must be preserved
  by any fixed-width reader/writer so raw records round-trip.
- Keep a **fixed-width mapper** that parses/formats the exact byte layout. It
  lets you seed the datastore deterministically from the mainframe sample data
  and is itself unit-testable.

### 2.3 Worked example — `CSUSR01Y` → `usrsec`

```
01 SEC-USER-DATA.                        UserSecurity (@Entity table "usrsec")
  05 SEC-USR-ID     PIC X(08).   ->  userId    @Id  length 8   (KSDS KEYS(8,0))
  05 SEC-USR-FNAME  PIC X(20).   ->  firstName      length 20
  05 SEC-USR-LNAME  PIC X(20).   ->  lastName       length 20
  05 SEC-USR-PWD    PIC X(08).   ->  password       length 8
  05 SEC-USR-TYPE   PIC X(01).   ->  userType       length 1   ('A' admin / 'U')
  05 SEC-USR-FILLER PIC X(23).   ->  (padding; not a column)   total = 80
```

- Repository `findById(userId)` replaces
  `EXEC CICS READ DATASET('USRSEC') RIDFLD(WS-USER-ID)`; "not found" (empty
  `Optional`) is the resp-13 branch.
- The 88-levels `CDEMO-USRTYP-ADMIN/USER` (`'A'`/`'U'`) become a `UserType`
  enum; any non-`'A'` falls through to regular user, matching the COBOL
  `IF ... ELSE`.
- `UsrsecRecordMapper` parses the 80-byte record; `UsrsecSeeder`
  (`CommandLineRunner`) loads `usrsec-seed.txt` at startup — the cloud analogue
  of the `DUSRSECJ` KSDS load.

---

## 3. Translate the CICS pseudo-conversational pattern to modern constructs

CICS online programs are **pseudo-conversational**: the program ends after each
screen interaction and is re-invoked on the next, carrying state in the COMMAREA.
Translate the recurring pieces as follows. **These patterns recur in every
online CO\* program**, so they are the highest-value things to generalize.

| CICS construct | Meaning | Modern equivalent |
| --- | --- | --- |
| `EIBCALEN = 0` | first entry (no COMMAREA) | initial `GET` of the page / no session yet |
| `EIBCALEN > 0` | re-entry | request carrying session/COMMAREA state |
| `EIBAID` | which key was pressed (`DFHENTER`, `DFHPF3`…) | an explicit `action` field or distinct endpoints/buttons |
| `DFHENTER` | submit | `POST` the form / call the endpoint |
| `DFHPF3` | exit | "cancel/exit" action returning to the caller |
| other key | invalid | `Invalid key pressed...` equivalent (map unknown actions) |
| `EXEC CICS RECEIVE MAP` | read screen inputs | deserialize request body / form |
| `EXEC CICS SEND MAP` | write screen + fields | render page / return response DTO |
| `EXEC CICS SEND TEXT` | plain-text message screen | terminal response body |
| COMMAREA (`COCOM01Y`) | cross-screen session state | session object / signed token / passed context DTO |
| `EXEC CICS XCTL PROGRAM(x)` | transfer control (no return) | route/redirect to service `x`; return the target in the response |
| `EXEC CICS LINK` | call sub-program (returns) | synchronous call to another service/method |
| `EXEC CICS RETURN TRANSID(t)` | set next transaction | keep session; next request hits endpoint `t` |

### 3.1 Rules of thumb

- **Preserve evaluation order.** Validation/branch order is observable (which
  error appears first). Keep it identical.
- **Keep messages verbatim.** Put them in one constants class; assert them.
- **Model routing explicitly.** An `XCTL` target (`COADM01C`/`COMEN01C`) becomes
  a value in the response (`destinationProgram`) so the client/UI can navigate —
  don't bury it in an HTTP redirect only.
- **Separate layers.** BMS/`RECEIVE`/`SEND` → controller + DTOs; paragraph logic
  → service; file I/O → repository. This mirrors the copybook classification in
  §1 and keeps PRs small.
- **COMMAREA → session context.** `COCOM01Y` fields (from/to program, user id,
  user type, selected account/card) become a session/context object. For the
  stateless signon step it is just the authenticated result; later screens will
  need a shared session module.

### 3.2 Worked example — `COSGN00C`

- `MAIN-PARA` first-entry/`EIBAID` dispatch → `SignonController` (`POST
  /api/signon`) plus the served page; `DFHPF3` exit is a client concern for the
  signon screen.
- `PROCESS-ENTER-KEY` + `READ-USER-SEC-FILE` → `SignonService.authenticate`,
  branch-for-branch (see §1.2 table). Outcomes are a `SignonResult` enum; HTTP
  status reflects the outcome while the body always carries the verbatim message
  and (on success) `destinationProgram` = `COADM01C`/`COMEN01C`.

---

## 4. Layered automated testing strategy (unit → integration → e2e + CI)

Build the suite from the branch table (§1). CardDemo starts with **no tests and
no CI**, so both are created from scratch.

### 4.1 Layers

| Layer | Scope | Tooling (worked example) | Proves |
| --- | --- | --- | --- |
| **Unit** | one branch/rule in isolation, dependencies mocked | JUnit 5 + Mockito | each `EVALUATE`/`IF` outcome; copybook field mapping |
| **Integration** | endpoint + real datastore | `@SpringBootTest`/`@DataJpaTest` + H2 | keyed lookup + record-field mapping through persistence |
| **E2E parity** | whole app over the wire, seeded fixtures | `@SpringBootTest(RANDOM_PORT)` + HTTP client | the README parity cases end-to-end; this is the parity gate |

Minimum unit cases for a validate-then-lookup program (from `COSGN00C`): valid
admin, valid regular, wrong password, user-not-found, empty user id, empty
password — plus any non-obvious rule (here: case-insensitive credentials, and
datastore-failure → verify-error).

### 4.2 Fixtures

- Mirror the mainframe sample data. For USRSEC, `usrsec-seed.txt` reproduces the
  10 users from `DUSRSECJ.jcl` (`ADMIN001..005`, `USER0001..005`, password
  `PASSWORD`) as fixed-width 80-byte records, loaded via the same mapper the app
  uses — so the fixture doubles as a mapping test.
- Seeding is idempotent and, for parity, the e2e layer relies on the seeded
  data rather than hand-built rows.

### 4.3 CI

- One GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push
  and PR: set up JDK 17, `mvn verify` (unit + integration + e2e), upload test
  reports. Because e2e runs the README parity cases, **every PR is gated on
  functional parity**.
- As more services are added under `modernized/`, add a job per service (or a
  matrix) so each is built/tested independently.

### 4.4 Equivalence-testing notes (watch-list)

Byte-level parity traps to assert explicitly: space/zero padding, `COMP-3`
rounding, 2-digit-year/century windowing, signed-number representation, and
upper-casing of inputs. `COSGN00C` exercises padding + upper-casing.

### 4.5 Frontend verification (required for every code-changing migration)

Automated e2e is the parity gate, but for any migration that produces or changes
a UI you must **also** verify the frontend manually and capture proof. This is a
required step, not optional:

1. Start the service (and any backend/datastore it needs), e.g.
   `mvn spring-boot:run`.
2. Open the UI in a real browser (for signon: `http://localhost:8080/`) and,
   with the browser window maximized, **record a screen recording** of the
   walkthrough.
3. Exercise the golden path and the parity cases through the UI: sign in as
   `ADMIN001/PASSWORD` (admin routing), `USER0001/PASSWORD` (regular routing),
   and at least one invalid case (wrong password / unknown user / blank field)
   to confirm the equivalent error shows and the screen does not navigate.
4. Attach the recording (and key screenshots) to the PR / deliver it in-session
   as evidence that the modernized flow works and nothing regressed.

Deliver generated artifacts (this playbook, seed files, recordings) **directly
in the session** to the requester, not only committed to the repo.

---

## 5. Small, independent, dependency-ordered PR strategy

Deliver each program as a **stack** of PRs. Each targets the previous branch as
its base, so every diff is small and reviewable in isolation, and the stack
merges bottom-up. Each PR includes its own tests and does not mix refactors with
features.

### 5.1 The per-program stack (used for `COSGN00C`)

| PR | Scope | Depends on | Example |
| --- | --- | --- | --- |
| **1** | Service scaffolding: project skeleton, build tooling, CI workflow. **No business logic.** | — | #166 |
| **2** | Data model / persistence from the record copybook + unit tests. | 1 | #167 |
| **3** | Validation/business logic (the paragraph translation) + unit tests. | 2 | #168 |
| **4** | Service endpoint + integration tests against the datastore. | 3 | #169 |
| **5** | E2E parity tests + seed fixtures + UI + CI wiring. | 4 | #170 |
| **6** | This playbook / program-specific migration notes. | — (docs) | #171 |

### 5.2 Rules

- **State scope & dependencies** in every PR description; note it's stacked and
  which diff to review.
- **Layer dependencies too:** each PR adds only the build dependencies it needs
  (PR2 adds JPA+H2, PR4 nothing, PR5 adds the test HTTP client), so the
  dependency delta is reviewable.
- **Keep each PR green.** `mvn verify` must pass at every layer; a PR never
  leaves the build red for the next one.
- **Docs PR is independent.** The playbook/doc PR depends on nothing and can
  merge any time.

### 5.3 How stacks compose across programs

Because scaffolding-type work (shared copybooks, the CI harness, the
`modernized/` layout) is shared, factor it into **foundation PRs** merged once,
then every program's stack starts at its own PR2 (data model). See §8.

---

## 6. Maintaining this playbook

Treat the playbook as living. After each program migration:

1. Add any newly-encountered CICS/COBOL construct to the §3 translation table.
2. Add new equivalence-testing traps to §4.4.
3. Update the §8 inventory status and dependency groupings.
4. Promote anything done twice into the shared module and note it in §9.

Deliverables generated by this process (this document, seed files, and the
service) should be surfaced to the requester in-session, not only committed.

---

## 7. Per-program migration checklist

Copy this block into each program's tracking issue/PR.

```
Program: __________   Transaction: ______   Target service: modernized/__________

[ ] 1. Inventory
    [ ] program, transaction id, paragraph structure read
    [ ] COPY list classified (shared / record / BMS / CICS-system)
    [ ] BMS mapset/map: inputs + output/error fields listed
    [ ] data files: dataset, copybook, VSAM org, key recorded
    [ ] navigation (XCTL/LINK/RETURN) targets recorded
    [ ] branch table transcribed (condition -> message/action)
[ ] 2. Data model
    [ ] record copybook -> entity (same field names, lengths); VSAM key -> PK
    [ ] fixed-width mapper (parse/format) + unit tests
    [ ] money as BigDecimal/NUMERIC with copybook scale (no floats)
[ ] 3. Logic + UI
    [ ] paragraphs -> service methods (traceable names), order preserved
    [ ] messages verbatim in a constants class
    [ ] navigation targets modeled explicitly in the response
    [ ] COMMAREA fields mapped to session/context (use shared module if present)
[ ] 4. Tests + CI
    [ ] unit: one test per branch + non-obvious rules
    [ ] integration: endpoint + real datastore + field mapping
    [ ] e2e: README parity cases over HTTP against seeded fixtures
    [ ] seed fixtures mirror app/data + app/jcl loader
    [ ] CI job runs unit+integration+e2e (parity gate) on every PR
[ ] 5. PRs
    [ ] stacked PRs (scaffold -> data -> logic -> endpoint -> e2e -> docs)
    [ ] each states scope + dependencies, includes its own tests, stays green
[ ] 6. Playbook
    [ ] new constructs / traps / inventory status fed back into this doc
```

---

## 8. Dependency classification & parallelization plan for CardDemo

Goal: let different engineers migrate the remaining online CO\* (and batch
CB\*) programs **in parallel** without conflicts, by grouping work around shared
scaffolding (shared copybooks, BMS mapsets, VSAM files).

### 8.1 Dependency-classification scheme

Classify every artifact a program touches into one of:

- **F — Foundation / shared:** used by many programs (shared copybooks
  `COCOM01Y`, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`; the `modernized/` layout; the
  CI harness; a future shared "session/COMMAREA" module and BMS→form helper).
  *Migrate once, up front; everything depends on it.*
- **D — Data/record:** a record copybook + its VSAM file (e.g. `CSUSR01Y`+USRSEC,
  `CVACT01Y`+ACCTDATA, `CVACT02Y`+CARDDATA, `CVCUS01Y`+CUSTDATA,
  `CVACT03Y`+CARDXREF, `CVTRA05Y`+TRANSACT, …). *Shared by all programs that read
  that file — migrate the data module once, then programs depend on it.*
- **P — Program-specific:** the program's own BMS map + business logic. *Fully
  parallelizable once its F and D deps exist.*

A program is **ready to start in parallel** when all of its F and D dependencies
are already merged (or owned by a coordinated stream).

### 8.2 CardDemo online inventory (from README) and suggested grouping

| Tran | Program | Function | Primary data (D) |
| --- | --- | --- | --- |
| CC00 | COSGN00C | Signon | USRSEC (`CSUSR01Y`) ✅ *done — reference* |
| CM00 | COMEN01C | Main Menu | — (menu/COMMAREA) |
| CA00 | COADM01C | Admin Menu | — (menu/COMMAREA) |
| CU00–CU03 | COUSR00C–03C | List/Add/Update/Delete User | USRSEC (`CSUSR01Y`) |
| CAVW/CAUP | COACTVWC/UPC | Account View/Update | ACCTDATA (`CVACT01Y`), CUSTDATA, XREF |
| CCLI/CCDL/CCUP | COCRDLIC/SLC/UPC | Card List/View/Update | CARDDATA (`CVACT02Y`), XREF |
| CT00–CT02 | COTRN00C–02C | Transaction List/View/Add | TRANSACT (`CVTRA05Y`), XREF |
| CR00 | CORPT00C | Transaction Reports | TRANSACT |
| CB00 | COBIL00C | Bill Payment | ACCTDATA, TRANSACT |

### 8.3 Work streams (parallelizable)

1. **Stream 0 — Foundation (do first, blocks the rest):** shared-copybook
   module (`COCOM01Y` session/context, titles/date/message helpers), the
   `modernized/` layout, and the CI harness. *Delivered in part by the signon
   scaffolding PR; extract the shared bits into a shared module as the second
   program starts.*
2. **Stream A — Security/User:** `COSGN00C` ✅ then `COUSR00C–03C` (all share
   USRSEC/`CSUSR01Y`, so they reuse the signon data module). One engineer/agent
   owns the USRSEC data module; the four user-admin programs then go in parallel.
3. **Stream B — Menus:** `COMEN01C`, `COADM01C` (no data file; pure COMMAREA +
   navigation) — depend only on Stream 0.
4. **Stream C — Account/Card/Customer:** account, card, customer, xref data
   modules (D) first, then `COACTVWC/UPC`, `COCRDLIC/SLC/UPC` in parallel.
5. **Stream D — Transactions/Reports/Billing:** TRANSACT data module first, then
   `COTRN00C–02C`, `CORPT00C`, `COBIL00C`.
6. **Batch (CB\*)** (`CBTRN02C`, `CBACT04C`, `CBSTM03A`): same data modules as
   the online streams; translate JCL step sequencing to a modern
   orchestrator/CI pipeline; batch file I/O → stream processing. Can proceed once
   the shared data modules exist.

**Why this avoids conflicts:** each stream owns distinct programs and, after the
shared data module for a file is merged, program PRs only touch their own
service package + tests. The per-program stacks (§5) each live on their own
branch and merge bottom-up, so parallel streams never edit the same files.

---

## 9. Reusable across repositories vs CardDemo-specific

**Reusable across any COBOL/CICS mainframe repo:**

- The whole method: inventory → data-model mapping → CICS-pattern translation →
  layered tests → stacked PRs (§1–§5).
- The VSAM→storage and copybook→table mapping rules (§2), incl. fixed-width
  mapper + no-floats-for-money.
- The CICS pseudo-conversational translation table (§3): EIBCALEN/EIBAID/
  COMMAREA/SEND/RECEIVE/XCTL/LINK.
- The unit→integration→e2e-parity strategy and equivalence-testing watch-list
  (§4), and the stacked-PR discipline (§5).
- The F/D/P dependency-classification scheme (§8.1) and "foundation first, then
  parallel streams" model (§8.3).

**CardDemo-specific (re-derive per repo):**

- The concrete inventory: program/transaction names, the specific copybooks
  (`COCOM01Y`, `CSUSR01Y`, …), BMS mapsets, VSAM datasets, and the sample data
  in `app/data` + `app/jcl` loaders.
- Exact message strings and navigation targets (`COADM01C`/`COMEN01C`) and the
  README parity fixtures (`ADMIN001`/`USER0001`, password `PASSWORD`).
- The chosen target stack (here Java 17 / Spring Boot / H2). Another repo may
  target a different language/datastore — the layering and parity discipline are
  unchanged.
- The specific work-stream groupings in §8 (they depend on CardDemo's file
  sharing).

---

### Appendix — worked-example artifacts (signon)

- Service: `modernized/signon-service/` (`com.carddemo.signon.{domain,service,web}`)
- Fixtures: `modernized/signon-service/src/main/resources/usrsec-seed.txt`
- CI: `.github/workflows/ci.yml`
- Source program: `app/cbl/COSGN00C.cbl`; copybooks in `app/cpy`,
  `app/cpy-bms`; mapset `app/bms/COSGN00.bms`; loader `app/jcl/DUSRSECJ.jcl`
- PR stack: #166 (scaffold) → #167 (data) → #168 (logic) → #169 (endpoint) →
  #170 (e2e/fixtures/CI) → #171 (this playbook)
