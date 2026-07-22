---
name: modernizing-cobol-to-java
description: >-
  General methodology for modernizing a COBOL program (batch or CICS online)
  into idiomatic Java, using the CardDemo application as the reference codebase.
  Use when asked to migrate, rewrite, convert, or modernize COBOL source
  (e.g. app/cbl/*.cbl) and its copybooks (app/cpy/*.cpy) to Java, or to explain
  how such a migration should be approached.
---

# Modernizing a COBOL Program to Java

This skill describes a repeatable, faithful approach for converting a COBOL
program in this repository into maintainable Java. The goal is **behavioral
parity first, idiomatic modernization second**: the Java must reproduce the
COBOL's observable outputs before it is refactored into clean OO code.

CardDemo intentionally uses non-uniform coding styles, VSAM files, CICS online
transactions, BMS screens, and JCL-driven batch. Treat every program as a small
migration project and follow the phases below in order.

## Guiding principles

- **Preserve behavior exactly before improving it.** COBOL arithmetic, padding,
  rounding, and truncation rules differ from Java defaults. Match them first.
- **Migrate one program at a time**, but analyze its whole dependency graph
  (copybooks, called sub-programs, files, maps) before writing code.
- **Copybooks become the domain model.** They are shared contracts across many
  programs — model them once and reuse.
- **Never invent business rules.** If the COBOL is ambiguous, capture the
  ambiguity as a test and flag it; do not guess.
- **Keep a parity test harness.** Capture representative COBOL inputs/outputs
  and assert the Java matches them.

## Phase 1 — Discover and classify

1. Read the program header comment block to find its **Type**
   (`BATCH COBOL Program` vs `CICS COBOL Program`) and **Function**.
2. Classify the program — this drives the target Java shape:
   - **Batch** (e.g. `CBACT01C`, `CBTRN02C`, `CBACT04C`): sequential/keyed file
     processing driven by JCL. → Target: a plain Java class with a `main` /
     service method, streaming records; orchestrated by a scheduler or Spring
     Batch job, not JCL.
   - **Online / CICS** (e.g. `COSGN00C`, `COACTVWC`, `COMEN01C`): screen-driven
     transactions using BMS maps and `EXEC CICS` + `COMMAREA`. → Target: a
     stateless service + controller (e.g. Spring MVC/REST), with the screen
     becoming a web/API request-response and COMMAREA becoming session/request
     state.
   - **Utility / date subroutines** (e.g. `CSUTLDTC`): called sub-programs. →
     Target: reusable Java utility/service methods.
3. Build the dependency inventory for the program:
   - `COPY` statements → copybooks in `app/cpy/` (data structures & shared
     working storage).
   - `CALL 'PROGRAM'` → other COBOL programs to stub or migrate.
   - `SELECT ... ASSIGN TO` and `FD` → the VSAM/QSAM files it reads/writes.
   - `EXEC CICS` verbs and `MAP/MAPSET` → BMS maps in `app/bms/`.
   - The JCL in `app/jcl/` that runs the program → DD names, file bindings,
     PARM values, run order.

## Phase 2 — Model the data (copybooks → Java types)

Copybooks such as `CVACT01Y` (account record) are fixed-layout records. Convert
each `01` level into a Java class and each elementary item into a typed field.

Map PIC clauses deliberately — this is where parity bugs hide:

| COBOL PIC                         | Meaning                          | Java type / handling |
| --------------------------------- | -------------------------------- | -------------------- |
| `PIC X(n)`                        | fixed-length text, space-padded  | `String` (trim on read, right-pad to `n` on write) |
| `PIC 9(n)`                        | unsigned integer digits          | `int`/`long` (or `String` if leading zeros matter) |
| `PIC S9(n)`                       | signed integer                   | `int`/`long` |
| `PIC S9(p)V99` / `V9(s)`          | implied decimal, fixed scale     | **`BigDecimal`** with fixed scale (never `double`) |
| `COMP` / `COMP-4` / `BINARY`      | binary integer                   | `int`/`long` |
| `COMP-3` (packed decimal)         | packed BCD                       | `BigDecimal`; decode packed bytes if reading raw files |
| `88` level                        | condition name (enum of values)  | `enum` or named boolean/constant |
| `OCCURS n`                        | fixed array / table              | `List<T>` or `T[]` of size `n` |
| `REDEFINES`                       | overlapping memory view          | separate accessor methods over the same bytes, or distinct DTOs |
| `FILLER`                          | reserved padding                 | omit from the model but preserve its byte width in fixed-layout I/O |

Rules that matter for parity:

- **Money is `BigDecimal`, always**, with the exact scale from the PIC
  (`S9(10)V99` → scale 2). Set an explicit `RoundingMode` matching the COBOL
  `ROUNDED`/truncation behavior.
- **Fixed-width fields keep their width.** When reading/writing flat files,
  slice by offsets derived from the copybook; when writing, right-pad text with
  spaces and zero-pad numerics exactly as the record length requires (the record
  length is documented in the copybook header, e.g. "RECLN 300").
- Turn `88`-level condition names into an `enum` or predicate methods
  (e.g. `ACCT-ACTIVE-STATUS` values → `AccountStatus`).

Because copybooks are shared across programs, put these classes in a common
`model`/`domain` package and reuse them; do not re-model the same record per
program.

## Phase 3 — Translate control flow (PROCEDURE DIVISION → methods)

1. Each COBOL **paragraph / section** (e.g. `1000-ACCTFILE-GET-NEXT`,
   `0000-ACCTFILE-OPEN`) becomes a **private Java method** with an intention-
   revealing name (`readNextAccount()`, `openAccountFile()`).
2. `PERFORM para` → a method call. `PERFORM para UNTIL cond` → a `while`/`for`
   loop. `PERFORM para N TIMES` → a counted loop. `PERFORM THRU` ranges → a
   single method covering the whole range.
3. `GO TO` is rare here but, if present, restructure into loops/conditionals or
   early returns — do not emulate `GO TO` in Java.
4. `EVALUATE` → `switch` / `if-else`; `EVALUATE TRUE WHEN <cond>` → an
   `if-else if` chain.
5. `MOVE` → assignment, but respect COBOL move semantics (truncation, padding,
   numeric editing). `COMPUTE`/`ADD`/`SUBTRACT` with `ROUNDED` → `BigDecimal`
   ops with the matching `RoundingMode`.
6. Replace the COBOL file-status idiom (checking `FILE STATUS`, `APPL-RESULT`,
   `88 APPL-EOF`, and calling `9999-ABEND-PROGRAM` / `CEE3ABD`) with Java
   control flow: iterate until end-of-data, and throw a specific exception where
   COBOL would ABEND. Log where COBOL used `DISPLAY`.

### Worked micro-example (batch)

The `CBACT01C` main loop:

```cobol
PERFORM 0000-ACCTFILE-OPEN.
PERFORM UNTIL END-OF-FILE = 'Y'
    IF END-OF-FILE = 'N'
        PERFORM 1000-ACCTFILE-GET-NEXT
        IF END-OF-FILE = 'N'
            DISPLAY ACCOUNT-RECORD
        END-IF
    END-IF
END-PERFORM.
PERFORM 9000-ACCTFILE-CLOSE.
```

becomes (behavior-preserving Java):

```java
try (AccountFileReader reader = accountFile.open()) {   // 0000-ACCTFILE-OPEN
    AccountRecord record;
    while ((record = reader.readNext()) != null) {      // 1000-...-GET-NEXT + EOF check
        log.info(record.display());                     // DISPLAY ACCOUNT-RECORD
    }
}                                                       // 9000-ACCTFILE-CLOSE (try-with-resources)
```

`readNext()` encapsulates the `READ ... INTO`, the `'00'/'10'` status handling,
and mapping the record bytes into the `AccountRecord` model. A non-`00`/`10`
status becomes a thrown exception (the ABEND equivalent).

## Phase 4 — Migrate I/O and integrations

- **VSAM / flat files:** map each `SELECT/FD` to a repository/DAO. Sequential
  reads → an `Iterator`/stream; keyed (KSDS) reads → a keyed lookup. For the
  target system, prefer a relational table or key-value store; keep the record
  model identical so parity tests still pass. Preserve key ordering when the
  COBOL relies on it (`ACCESS MODE IS SEQUENTIAL` over an indexed file reads in
  key order).
- **CICS online programs:**
  - `EXEC CICS RECEIVE MAP / SEND MAP` (BMS) → request/response DTOs; the BMS
    map fields (`*I`/`*O` fields, attributes) become form/JSON fields.
  - `COMMAREA` / `DFHCOMMAREA` → request-scoped or session-scoped state object
    passed between handlers (`CARDDEMO-COMMAREA` → a `CardDemoContext` bean).
  - `EXEC CICS RETURN TRANSID(...)` (pseudo-conversational looping) → normal
    stateless request handling; the "next transid" becomes the next endpoint.
  - `EIBAID` / `DFHENTER` / `DFHPF3` (AID keys) → explicit user actions
    (submit / back / cancel buttons or API operations).
  - `EXEC CICS READ/WRITE/REWRITE FILE(...)` → repository calls.
- **Called subprograms** (`CALL 'CSUTLDTC'` etc.): migrate or wrap as Java
  services; keep the same inputs/outputs so callers are unaffected.
- **JCL:** the job step that runs the program (DD statements, PARM, run order in
  README "Running full batch") defines the Java entrypoint's arguments and how
  jobs chain. Model multi-step jobs as an orchestrated pipeline.

## Phase 5 — Verify parity, then modernize

1. **Golden-master tests:** capture representative inputs (sample data in
   `app/data/`) and the COBOL program's outputs (its `DISPLAY` output, produced
   files, or screen contents). Assert the Java produces byte/value-equivalent
   results, paying attention to numeric formatting, padding, and rounding.
2. **Edge cases:** empty files, max field widths, signed/negative amounts,
   zero-padded IDs, high/low-values, and every `88`-level condition.
3. Only after parity holds, refactor toward idiomatic Java: dependency
   injection, immutable value objects, streams, `Optional`, layered
   architecture (controller → service → repository), and unit tests per method.
4. Do **not** modify the original COBOL to make Java pass, and do not change
   tests to hide a discrepancy — investigate and reconcile the difference.

## Program-type quick reference

| COBOL construct | Batch target | Online (CICS) target |
| --------------- | ------------ | -------------------- |
| Program body | `main`/service method | controller + stateless service |
| Paragraph | private method | private method |
| Copybook `01` | domain/DTO class (shared) | domain/DTO class (shared) |
| `FILE`/`FD` (VSAM) | repository / DAO | repository / DAO |
| `COMMAREA` | n/a | request/session context object |
| BMS map | n/a | request/response DTO (form/JSON) |
| `EXEC CICS RETURN TRANSID` | n/a | next endpoint / redirect |
| `CALL 'SUBPGM'` | injected service | injected service |
| ABEND (`CEE3ABD`) | thrown exception | thrown exception / error response |

## Checklist before calling a migration done

- [ ] Program classified (batch / online / utility) and dependency inventory built.
- [ ] Every referenced copybook modeled as reusable Java types with correct
      PIC → type mapping (money = `BigDecimal`, fixed widths preserved).
- [ ] Every paragraph translated; `PERFORM`/`EVALUATE`/`GO TO` restructured.
- [ ] File and CICS/BMS/COMMAREA I/O mapped to repositories and DTOs.
- [ ] Called subprograms migrated or wrapped with identical contracts.
- [ ] Parity tests pass on sample data and edge cases (padding, rounding, signs).
- [ ] Code refactored to idiomatic, layered Java only after parity holds.
- [ ] Any ambiguous COBOL behavior flagged, not guessed.
