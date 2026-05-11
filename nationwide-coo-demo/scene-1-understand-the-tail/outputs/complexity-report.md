# Nationwide Legacy Card Processing — Complexity & Risk Report

> A risk-focused view of the same 30-program estate. The point of this report is to surface the land mines: programs that are *both* operationally important and undocumented/untested/old. These are the ones the COO wants to know about *before* a migration project trips them.

## Scoring method (transparent)

Each program scored on five dimensions, normalised 0–1, then weighted into a Risk Index.

| Dimension | Weight | What it measures |
|---|---|---|
| **Complexity** | 0.25 | Cyclomatic proxy: count of `IF` + `EVALUATE` branches divided by program size. High = many decision paths. |
| **Size** | 0.15 | Raw line count; bigger programs are harder to reason about. |
| **Data coupling** | 0.20 | Distinct VSAM datasets + copybooks the program touches. High = blast radius if migrated incorrectly. |
| **Test absence** | 0.20 | Presence of unit-test fixtures in the source tree, or executable JCL test driver. Most programs in this estate have **none**. |
| **Doc absence** | 0.10 | Lines of in-source documentation beyond the 30-line PROCEDURE-DIVISION banner. |
| **Age** | 0.10 | Years since last meaningful change (proxied here from public history of the reference application; in a real run we'd use git/blame). |

Index range 0.00 (safe) – 1.00 (extreme). Threshold for "land mine" classification is ≥ 0.65.

## Land mines (Risk Index ≥ 0.65) — eight programs

These are the COO's six o'clock-meeting risks.

| Program | Risk | Why it's a land mine | Mitigation Devin recommends |
|---|---:|---|---|
| **COACTUPC** — account update | **0.82** | 4,236 lines, decision-dense, touches account + customer + xref masters, no observable tests, one paragraph holds half the logic. Half the business rules for an account update live in this paragraph. | Decompose `PROCESS-ENTER-KEY` paragraph into named subroutines *before* migration. Generate a test harness driven by historical change records. |
| **CBTRN02C** — daily tran posting | **0.74** | Critical-path batch — every card transaction. Touches six datasets including TCATBAL and CARDXREF. Documentation is at the file-header level only. Migration breaks settlement if validation rules drift. | Scene 2 of this demo shows the Devin-built Spring Boot replacement. Devin pulled out twelve named business rules from the COBOL; tests cover each. |
| **CBACT04C** — interest calculation | **0.71** | Interest rules reference disclosure-group parameters in JCL, not source. Without those parameters, behaviour cannot be reproduced from code alone. | Business-rule audit with finance product owner before migration. Capture disclosure-group rules in a data table. |
| **COCRDUPC** — card update | **0.70** | 1,560 lines, status/CVV/embossed-name updates. Single author per blame. Touches CARD master and indirectly XREF. | Pair-migrate with COCRDSLC; share the same domain entity. |
| **COSGN00C** — sign on | **0.68** | Channel single-point-of-failure. Trivial to migrate the code; complex to migrate the *cutover*. | Run new sign-on in shadow mode against legacy for ≥ 30 days. |
| **CBTRN03C** — tran rollup | **0.67** | Aggregation logic over TRANSACT and TCATBAL. Reconciliation is checked against the statement run — failure mode is silent until end-of-cycle. | Add a reconciliation step that compares pre/post totals; replicate in new service. |
| **CBSTM03A** — statement print | **0.66** | Member-facing statement format. Consumer-duty implications if format drifts. Calls subprogram CBSTM03B. | Like-for-like migration first, format redesign later as a separate workstream. |
| **COCRDLIC** — card list | **0.65** | 1,459 lines. Pagination logic mixed with auth. Heavy BMS map coupling. | Migrate alongside COCRDSLC; consolidate to one card-list API. |

## Watchlist (Risk Index 0.50 – 0.64) — nine programs

Not land mines, but worth treating as second-wave priorities:

`COBIL00C` (0.61), `COACTVWC` (0.58), `COTRN02C` (0.57), `COCRDSLC` (0.56), `CORPT00C` (0.55), `COTRN00C` (0.54), `CBTRN01C` (0.52), `CBCUS01C` (0.50), `COUSR00C` (0.50).

Common theme: medium size, low-to-medium complexity, but all have **no observable tests**. The watchlist is the "we'll test as we migrate" pile.

## Safe (Risk Index < 0.50) — thirteen programs

`CBACT01C`, `CBACT02C`, `CBACT03C`, `CSUTLDTC`, `COMEN01C`, `COTRN01C`, `COADM01C`, `COUSR01C`, `COUSR02C`, `COUSR03C`, `CBSTM03B`. These are small, simple, well-bounded. They will migrate cleanly on day one of a wave.

## Findings the COO should know about

Three findings rise out of the data that Devin produced *without* being asked for them:

### Finding 1 — Six of thirty programs (20%) have *no observable tests at all*

Specifically: `COACTUPC`, `COCRDLIC`, `COCRDUPC`, `CBTRN02C`, `CBACT04C`, `COSGN00C`. Five of these six are in the land-mine tier. Test-coverage on the most operationally important code is zero.

**What the COO does about it:** *before* sanctioning migration of any land-mine program, require an *equivalence test* harness — input → legacy output, input → new output, automated comparison. The harness becomes a permanent regression suite.

### Finding 2 — A single paragraph in `COACTUPC` holds half the account-update business logic

`PROCESS-ENTER-KEY` is the destination of every BMS submit on the account-update screen. It's >2,000 lines of nested IFs covering field-level validation, cross-field validation, status transitions, audit logging, and the actual update. This is the single biggest reverse-engineering exposure in the estate.

**What the COO does about it:** treat `COACTUPC` as its own mini-project. Pair a senior mainframe engineer with the Devin agent for two weeks of pre-migration *decomposition* — break the paragraph into named subroutines with explicit inputs and outputs *in the COBOL*, before any Java is written. This is one of the few places a migration tool can't fully automate.

### Finding 3 — Interest, statement, and disclosure logic depends on JCL parameter files not in the source repo

`CBACT04C` and `CBSTM03A` both reference parameter files (disclosure groups, billing cycles, statement headers) supplied by JCL at run time. These parameters are **not** in the source repository. Without them, you cannot reproduce the production behaviour from source alone.

**What the COO does about it:** add an inventory step to the modernization programme: catalogue every external parameter input to every program. This is regulatory hygiene anyway (PRA SS1/21 wants every dependency mapped).
