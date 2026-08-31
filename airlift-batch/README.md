# airlift-batch: proving a batch air-lift is faithful

Their question: *we have mainframe programs and JOBS running today — can they be
air-lifted to a new environment, and how would you PROVE the lift is faithful?*

This directory answers the second half. It takes one real CardDemo nightly job
chain, runs **the estate's own COBOL** off-mainframe under GnuCOBOL to produce a
golden dataset, runs a **Java 21 / Spring Boot 3 / Spring Batch** mirror over the
same fixtures, and compares the two **field by field**. The comparator knows
nothing about what is supposed to differ, so its verdict is not self-fulfilling.

Nothing under `app/` is modified: the legacy estate stays pristine. Everything
here is additive, and every number in this file is produced by a command in it.

## What is in the box

| Path | What it is |
| --- | --- |
| `tools/inventory.py` | Parses `app/jcl`, `app/proc`, `app/cbl`, `app/cpy` and emits `INVENTORY.md`. `--check` fails if the committed file is stale. |
| `INVENTORY.md` | Job chain, step -> program -> dataset bindings, copybook usage, CALL graph, LOC, lift-difficulty ranking. Generated, never hand-written. |
| `fixtures/generate.py` | Deterministic fixtures (seed 20220718): 300 accounts, 604 daily transactions, 450 category balances, disclosure groups. |
| `cobol/` | Harness-owned modules only: `AIRCLOCK` (fixed clock), `AIRLOAD`/`AIRUNLD` (IDCAMS REPRO stand-ins), `INTDRV` (JCL PARM driver), `CEE3ABD` stub, `PACKGEN`/`ARITHCHK` (codec evidence). |
| `tools/preclock.py` | Stages `app/cbl` into `build/src` and replaces exactly one statement, `MOVE FUNCTION CURRENT-DATE`, with `CALL 'AIRCLOCK'`. Nothing else is rewritten. |
| `mirror/` | The Spring Batch mirror: copybook parser, fixed-width codec (PIC X, zoned DISPLAY, COMP, COMP-3 with scale), H2-backed keyed store standing in for VSAM KSDS, and the two jobs. |
| `parity/compare.py` | Neutral field-level comparator. Exits non-zero on any mismatch. |
| `parity/traps.json` | The audit trail: what differs on purpose, and the `app/cbl` line each trap contradicts. |
| `parity/attribute.py` | Maps mismatches onto the manifest. Anything unclaimed is `unclassified`. |
| `parity/fix-traps.patch` | The fix. Three source files, plus the three test cases the trapped suite did not cover. |

## The slice, and why

`POSTTRAN.jcl STEP15 -> CBTRN02C` then `INTCALC.jcl STEP15 -> CBACT04C`, both
verified against the source in `app/jcl` and `app/cbl` (see `INVENTORY.md`
sections 2 and 6). Between them these two steps:

* read a sequential daily-transaction file and four VSAM KSDS clusters
  (ACCTDATA, CARDXREF, DISCGRP, TCATBALF), one of them through an alternate
  index path (`XREFFIL1`), and write two GDG generations (DALYREJS, SYSTRAN);
* update two KSDS clusters in place (`OPEN I-O` + `REWRITE`);
* carry the money arithmetic — `COMPUTE` into `PIC S9(09)V99` fields, COMP-3
  category balances, an unrounded interest computation;
* have a real reject path with its own reason codes.

That is the hardest combination of surfaces in the batch estate outside the
statement generator (`CBSTM03A`, which needs four more files and an HTML
formatter), so it is the honest slice to prove a method on. `CBTRN01C` is a
read-and-print job over the same records and proves nothing extra.

`READACCT.jcl STEP05 -> CBACT01C` is carried as the **reference slice**: same
copybooks, same codec, same comparator, no traps. It is the control that stops
"verify fails" being dismissed as a broken harness.

## Presenter runbook

Prerequisites: GnuCOBOL 3.1.2 (`cobc`), JDK 21, Maven 3.6+, Python 3.11+. Run
everything from this directory.

**1. The estate, counted by a tool rather than by hand.**

```bash
make inventory-check      # regenerates INVENTORY.md and diffs it: byte-identical
```

31 JCL/PROC members, 83 steps, 262 DD statements, 28 COBOL programs (11 batch,
17 online), 28 copybooks, 18 VSAM clusters, 7 GDG bases.

**2. Their COBOL, running off-mainframe, producing the golden dataset.**

```bash
make setup
```

Compiles the staged `app/cbl` programs with `cobc -x -std=ibm -fbinary-truncate`,
loads the KSDS clusters, and runs the two steps exactly as their JCL does. Read
aloud:

```
TRANSACTIONS PROCESSED :000000604
TRANSACTIONS REJECTED  :000000086
CBTRN02C RC=4
AIRUNLD TRAN     RECORDS UNLOADED: 000000518
```

**3. The Java mirror is green on its own terms.**

```bash
make test
```

14 tests pass: copybook parser, codec vectors, interest arithmetic, and an
end-to-end Spring Batch run. This is the state most modernisation programmes
call "done". (The fix patch takes it to 17 — three of the four defects live in
behaviour the mirror's own suite never asserted on, which is the point.)

**4. Parity says otherwise.**

```bash
make verify
```

```
transact.dat     records cobol=   518 java=   504 fields=   6734 mismatched=    14
systran.dat      records cobol=   359 java=   442 fields=   5746 mismatched=  1142
acctdata.dat     records cobol=   300 java=   300 fields=   3600 mismatched=   295
tcatbal.dat      records cobol=   450 java=   442 fields=   1800 mismatched=     8
dalyrejs.dat     records cobol=    86 java=   100 fields=   1500 mismatched=    22
17899/19380 fields at parity
```

Exit code 1. Note the record counts: the mirror posts 14 fewer transactions,
rejects 14 more, and writes 83 extra interest transactions.

**Why the denominator moves between this run and step 7 (19380 -> 18091).** The
comparator aligns on the *union* of both sides' record keys, and counts a field
on a record only one side produced as a mismatch against `<missing>`. So the 83
invented interest transactions and 14 extra rejects bring 1289 extra field slots
with them (83 x 13 + 14 x 15), and those slots disappear when the mirror stops
inventing records. That makes the two percentages non-comparable, and in the
harsher direction — a defect that fabricates records inflates its own
denominator. The figures to quote are the mismatch counts, **1481 -> 0**, against
the fixed denominator of 18091, which is exactly the COBOL record set.

**5. Every difference is accounted for, by a manifest the comparator cannot see.**

```bash
make traps
```

```
T1 observed      644 mismatches  packed-decimal rounding vs truncation
T2 observed     1427 mismatches  blank padding of a fixed-width key
T3 observed     1473 mismatches  inclusive vs exclusive date boundary
T4 observed        8 mismatches  error-path ordering
classified=1481 unclassified=0 of 1481 mismatches
```

(The per-trap counts overlap, because one wrong rate changes an amount, a
description and a balance at once; 1481 is the number of distinct field
mismatches.) `unclassified=0` is the claim that matters: nothing is differing
that we cannot explain, and a trap the manifest declares but the run never
witnesses fails just as loudly.

**6. The harness is sound.**

```bash
make verify-reference
```

```
acctreport.txt   records cobol=   301 java=   301 fields=   3902 mismatched=     0
3902/3902 fields at parity
```

Same comparator, same codec, untrapped slice: 100%. So step 4 is a finding about
the mirror, not about the harness.

**7. Fix the four defects; parity goes to 100%.**

```bash
make verify-fixed
```

Applies `parity/fix-traps.patch` to a copy of the mirror, rebuilds it, reruns it
against the same fixtures and the same golden dataset:

```
transact.dat     records cobol=   518 java=   518 fields=   6734 mismatched=     0
systran.dat      records cobol=   359 java=   359 fields=   4667 mismatched=     0
acctdata.dat     records cobol=   300 java=   300 fields=   3600 mismatched=     0
tcatbal.dat      records cobol=   450 java=   450 fields=   1800 mismatched=     0
dalyrejs.dat     records cobol=    86 java=    86 fields=   1290 mismatched=     0
18091/18091 fields at parity
```

**8. The whole story in one command** (this is what CI runs):

```bash
make demo
```

## The four traps

Each is drawn only from semantics actually present in this estate, and each cites
the line it contradicts. Attribution lives in `parity/traps.json`, never in
`compare.py`.

| Trap | Defect | Contradicts |
| --- | --- | --- |
| T1 | Monthly interest rounded to the nearest cent instead of truncated | `app/cbl/CBACT04C.cbl:464-465` — `COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200` with no `ROUNDED`, target `PIC S9(09)V99` (`CBACT04C:168`) |
| T2 | `ACCT-GROUP-ID` trimmed before the DISCGRP read, so every account silently falls through to the DEFAULT rate | `app/cbl/CBACT04C.cbl:415-440` — the key is `PIC X(10)` in `app/cpy/CVTRA02Y.cpy` and `app/cpy/CVACT01Y.cpy`, and `CBACT04C:437` moves the literal `'DEFAULT'` into that same fixed-width field |
| T3 | A transaction dated exactly on the account expiry date is rejected instead of posted | `app/cbl/CBTRN02C.cbl:414` — `IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)` |
| T4 | First validation failure wins, so an over-limit *and* expired transaction is rejected `0102` instead of `0103` | `app/cbl/CBTRN02C.cbl:407-420` — both `IF`s run unconditionally and the second `MOVE` overwrites `WS-VALIDATION-FAIL-REASON` |

T1 is the one to dwell on: it is a one-word difference (`ROUNDED`), the Java code
looks more correct than the COBOL, the unit tests pass, and it moves money on
644 fields. This is exactly the class of defect that survives code review and
surfaces in production reconciliation.

## How both sides are kept honest

* **Determinism.** Fixture seed 20220718; `AIRLIFT_CLOCK=2022071810300012+0000`
  feeds both `AIRCLOCK` and the Java clock; `INTCALC` runs with the JCL's own
  `PARM='2022071800'`. Reruns are byte-identical on both sides.
* **Codec evidence, not assumption.** `PACKGEN` and `ARITHCHK` are COBOL programs
  compiled by the same `cobc`; they emit the byte images for zoned/COMP/COMP-3
  values and the truncated-vs-`ROUNDED` results of the interest computation. The
  Java codec tests assert against those files, and `scripts/verify-encoding.sh`
  fails if the committed copies drift from what the compiler just produced.
  `arith.txt` (balance, rate, truncated, `ROUNDED`) is the evidence behind T1:

  ```
  01 +000001000.00 +0012.99 +000000010.82 +000000010.83
  04 +000004567.89 +0024.99 +000000095.12 +000000095.13
  05 -000002500.55 +0018.49 -000000038.52 -000000038.53
  ```

* **VSAM.** KSDS clusters become GnuCOBOL indexed files on the left and keyed H2
  tables (primary key + alternate key column, record image as `VARBINARY`) on the
  right; the alternate index path `XREFFIL1` becomes an alternate-key lookup.
* **The comparator is neutral.** `compare.py` decodes both sides through the
  estate's copybooks, aligns records by their logical key, reports missing
  records and fields as `<missing>`, and counts. It contains no trap names.

## Not verified

Stated plainly, because a parity claim is only worth its boundaries.

* **IBM Enterprise COBOL equivalence.** Everything on the left is **GnuCOBOL
  3.1.2.0** with `-std=ibm -fbinary-truncate`. `-fbinary-truncate` is GnuCOBOL's
  equivalent of `TRUNC(STD)` for `USAGE COMP`, and the truncation in `arith.txt`
  is what an unrounded `COMPUTE` into `PIC S9(09)V99` produces *under GnuCOBOL*.
  We have not executed these programs under Enterprise COBOL on z/OS, so
  byte-equality with a real z/OS run is **not established** — for a customer
  engagement the left side should be re-run on their compiler and the golden
  dataset regenerated there. The method does not change; the reference bytes may.
* **EBCDIC.** Both sides run in ASCII. Zoned-decimal sign nibbles and collation
  differ under EBCDIC. Sign handling is asserted against compiler output, but
  only in ASCII.
* **Fixture realism.** The fixtures are generated, not production extracts. They
  exercise the code paths this slice contains (including rejects and a zero-rate
  disclosure group) but they are not a claim about their data distribution.
* **CICS.** Nothing online is covered here; that is the sibling `cics-genapp`
  demo. `CEE3ABD` is a stub, and `CBACT04C` runs under `INTDRV` because a JCL
  `PARM` has no equivalent on a bare executable.
* **Scale and runtime.** 300 accounts / 604 transactions. No statement about
  throughput on production volumes, and Spring Batch chunking/restart semantics
  are used but not stress-tested.
* **The rest of the estate.** 8 of the 28 COBOL programs are invoked by a JCL
  step; this slice covers 3 of them. The other jobs are inventoried, not lifted.
