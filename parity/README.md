# Parity harness — POSTTRAN / CBTRN02C

Runs the unmodified legacy COBOL and the migrated Java on the same inputs and diffs every output
byte for byte. Analysis and citations: `docs/POSTTRAN-modernization-phase1.md`.

```bash
./parity/run_parity.sh        # baseline + migration + diff
./parity/run_legacy.sh        # baseline only, into parity/work/out/legacy
```

Requirements: GnuCOBOL 3.1.2 (`cobc`), Java 17, Maven, Python 3.

| File | Role |
|---|---|
| `run_legacy.sh` | compiles `app/cbl/CBTRN02C.cbl` unchanged and runs it under GnuCOBOL with the DD wiring of `app/jcl/POSTTRAN.jcl` STEP15 |
| `run_parity.sh` | runs the baseline, builds and runs the Java step harness, then diffs |
| `tools/decode_ebcdic.py` | CP037 → single-byte ASCII for the delivered `app/data/EBCDIC` extracts |
| `tools/VSAMLOAD.cbl` | flat sequential → GnuCOBOL INDEXED stand-ins for the three KSDS inputs |
| `tools/VSAMUNLD.cbl` | INDEXED → flat sequential after-images, in key order |
| `tools/CEE3ABD.cbl` | stub for the LE abend service called at `app/cbl/CBTRN02C.cbl:711` |
| `tools/diff_outputs.py` | record-count and byte-level comparison of all four outputs plus SYSOUT |

`parity/work/` is generated and gitignored. Nothing under `app/` is modified by any of this.

## Notes

* `-fsign=EBCDIC` is mandatory on the legacy compile. Without it the run produces 43 rejects /
  257 posted instead of 38 / 262 — the sign convention changes results, not just bytes.
* `TRAN-PROC-TS` is the only masked field in the diff: it comes from `FUNCTION CURRENT-DATE`
  (`app/cbl/CBTRN02C.cbl:693`), so it is checked for format rather than equality.
* GnuCOBOL INDEXED files stand in for VSAM KSDS. This reproduces keyed read/write/rewrite
  semantics and key ordering, not VSAM's CI/CA behaviour or alternate-index upgrade.
