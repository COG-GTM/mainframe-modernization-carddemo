# POSTTRAN STEP15 — `CBTRN02C` migrated to Java 21 / Spring Batch

First migration slice of the CardDemo mainframe modernization. Scope, alternatives considered and
the source map are in [`docs/phase1-posttran-inventory.md`](../../docs/phase1-posttran-inventory.md);
this directory is the Phase 2 implementation and its evidence.

**The legacy source is untouched.** `app/cbl/CBTRN02C.cbl` is the reference oracle: it is compiled
and run here, and the migrated step is required to reproduce its output byte for byte.

```
migration/posttran/
  java/       Java 21 + Spring Batch implementation of STEP15
  harness/    mock-data generator, legacy runner, migrated runner, comparator
  testdata/   inputs (shipped + mock) and the recorded legacy baselines
```

## Status: byte parity on both input sets

| | shipped sample (300 records) | generated mock set (11 records) |
|---|---|---|
| posted transactions (TRANFILE) | 262, identical outside `TRAN-PROC-TS` | 6, identical outside `TRAN-PROC-TS` |
| account after-images (ACCTFILE) | 50, byte-identical | 50, byte-identical |
| category balances (TCATBALF) | 100, byte-identical | 51, byte-identical |
| rejects (DALYREJS) | 38, byte-identical | 5, byte-identical |
| SYSOUT / return code | identical / 4 | identical / 4 |

One field is normalised out of the diff: `TRAN-PROC-TS`, stamped from `FUNCTION CURRENT-DATE`
(`CBTRN02C.cbl:692-705`), which cannot agree across two runs. It is asserted separately for format,
and the migrated side takes its clock by injection so a run can be pinned.

Reproduce, needing only GnuCOBOL, Java 21 and Maven:

```bash
harness/parity.sh                      # generated mock set
harness/parity.sh testdata/shipped     # the drop's own sample
cd java && mvn test                    # same comparison, against the recorded baselines
```

## Why a generated mock set exists

The shipped 300-record sample only ever fires **one** of the four reject reasons: all 38 of its
rejects are `0102 OVERLIMIT`. Reasons 100, 101 and 103, the TCATBAL create branch, the negative
amount branch and both `>=` boundaries are never reached, so parity on the shipped sample alone
proves very little about them.

`harness/gen_mock_data.py` builds inputs that reach all of them, derived from the drop's own account,
card and category-balance data rather than invented — the accounts, cards and limits are real
records from `app/data/ASCII/`, and the boundary amounts are computed from those records' actual
figures. Each generated transaction targets one branch:

| record | branch | cited at |
|---|---|---|
| 1, 2 | accepted, TCATBAL REWRITE, accumulating across records | `:526-542` |
| 3 | accepted, unseen type/category → TCATBAL WRITE | `:502-524` |
| 4 | accepted, negative amount → `ACCT-CURR-CYC-DEBIT` | `:551` |
| 5 | reject 100, card absent from XREF | `:384-386` |
| 6 | reject 101, XREF hit but no account | `:396-398` |
| 7 | reject 102, over the credit limit | `:403-412` |
| 8 | reject 103, dated after expiration | `:414-419` |
| 9 | overlimit **and** expired → reported as 103 | `:407-419` |
| 10 | expiration date exactly equal → accepted | `:414` |
| 11 | credit limit exactly reached → accepted | `:407` |

Record 9 is the one worth knowing about: the expiration check runs after the limit check and
assigns unconditionally, so it overwrites a reason 102 that was already set. A transaction that
violates both is reported as expired, and the overlimit condition leaves no trace.

## Notes for reviewers

- **Sign handling decides whether the money is right.** Amounts are zoned decimal with an
  overpunched sign. The legacy program must be compiled `cobc -x -fsign=EBCDIC`; without it
  GnuCOBOL does not recognise the `}J..R` overpunch and every negative amount reads as positive —
  which is how an earlier run of the shipped sample produced 43 rejects instead of 38. `Zoned`
  implements the same convention on the Java side and `BigDecimal` carries every amount.
- **Records keep their FILLER and their untouched bytes.** Parity is byte-level, so the migrated
  records are edited in place rather than rebuilt from parsed fields.
- **VSAM stand-in.** The four KSDS datasets are `KeyedStore`, an in-memory keyed map unloaded in key
  order. That is what makes a byte-for-byte diff against the legacy unload possible; moving to a
  database is the next refactor and is confined to that one class.
- **Chunk size is 1.** The step validates each transaction against account state it has just
  rewritten, so records are not independent and a larger chunk would change results.
- **Maven, not Gradle.** Our Spring Boot convention is Gradle; Gradle is not installed on this
  machine and Maven is. Worth switching if this module grows.
- **Not covered by parity, by construction:** the `CEE3ABD` abend path (`:707-711`), the
  `TRANFILE` `OPEN OUTPUT` / `DISP=SHR` question raised in Phase 1 §4.3, and everything outside
  this single step — other JCL steps, the utility layer and the CICS programs.
