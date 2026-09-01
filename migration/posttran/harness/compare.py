#!/usr/bin/env python3
"""Diff a legacy CBTRN02C run against the migrated run, under explicit normalisation rules.

  usage: compare.py <legacy-out-dir> <migrated-out-dir>

Exactly one field in the step's output is not reproducible: TRAN-PROC-TS, stamped from
FUNCTION CURRENT-DATE (CBTRN02C.cbl:692-705, written at :438). It is blanked before the diff on
both sides and checked separately for shape, so a wrong format still fails. Everything else --
posted transactions, account after-images, category balances, the reject file, SYSOUT and the
return code -- is compared byte for byte.
"""
from __future__ import annotations

import pathlib
import re
import sys

TRAN_PROC_TS = slice(304, 330)
DB2_TS = re.compile(r"^\d{4}-\d{2}-\d{2}-\d{2}\.\d{2}\.\d{2}\.\d{2}0000$")


def records(path: pathlib.Path, length: int) -> list[str]:
    data = path.read_bytes().decode("latin1")
    if len(data) % length:
        raise SystemExit(f"{path}: length {len(data)} is not a multiple of {length}")
    return [data[i:i + length] for i in range(0, len(data), length)]


def compare_records(name: str, legacy: list[str], migrated: list[str]) -> list[str]:
    problems = []
    if len(legacy) != len(migrated):
        problems.append(f"{name}: record count {len(legacy)} (legacy) vs {len(migrated)} (migrated)")
    for i, (a, b) in enumerate(zip(legacy, migrated)):
        if a != b:
            cols = [c for c in range(len(a)) if a[c] != b[c]]
            problems.append(
                f"{name}[{i}] differs at columns {cols[0]}-{cols[-1]}:\n"
                f"    legacy   {a[cols[0]:cols[-1] + 1]!r}\n"
                f"    migrated {b[cols[0]:cols[-1] + 1]!r}")
    return problems


def main() -> int:
    legacy_dir, migrated_dir = (pathlib.Path(p) for p in sys.argv[1:3])
    problems: list[str] = []
    checks: list[str] = []

    # --- posted transactions, with TRAN-PROC-TS normalised out ------------------------------
    legacy_tran = records(legacy_dir / "TRANFILE.seq", 350)
    migrated_tran = records(migrated_dir / "TRANFILE.seq", 350)
    for side, recs in (("legacy", legacy_tran), ("migrated", migrated_tran)):
        for i, r in enumerate(recs):
            if not DB2_TS.match(r[TRAN_PROC_TS]):
                problems.append(f"{side} TRANFILE[{i}] TRAN-PROC-TS is not a DB2 timestamp: "
                                f"{r[TRAN_PROC_TS]!r}")
    if not problems:
        checks.append(f"TRAN-PROC-TS well-formed on both sides ({len(legacy_tran)} records)")

    blank = lambda r: r[:TRAN_PROC_TS.start] + " " * 26 + r[TRAN_PROC_TS.stop:]
    problems += compare_records("TRANFILE", [blank(r) for r in legacy_tran],
                                [blank(r) for r in migrated_tran])
    checks.append(f"TRANFILE: {len(legacy_tran)} posted transactions, "
                  "byte-identical outside TRAN-PROC-TS")

    # --- everything else is compared byte for byte ------------------------------------------
    for name, length in (("ACCTFILE.seq", 300), ("TCATBALF.seq", 50), ("DALYREJS", 430)):
        legacy = records(legacy_dir / name, length)
        migrated = records(migrated_dir / name, length)
        problems += compare_records(name, legacy, migrated)
        checks.append(f"{name}: {len(legacy)} records, byte-identical")

    # --- SYSOUT and the step return code -----------------------------------------------------
    legacy_sysout = (legacy_dir / "sysout.txt").read_text().splitlines()
    migrated_sysout = (migrated_dir / "sysout.txt").read_text().splitlines()
    if legacy_sysout != migrated_sysout:
        problems.append("SYSOUT differs:\n    legacy   "
                        + "\n    legacy   ".join(legacy_sysout)
                        + "\n    migrated " + "\n    migrated ".join(migrated_sysout))
    else:
        checks.append(f"SYSOUT: {len(legacy_sysout)} lines identical")

    legacy_rc = (legacy_dir / "rc.txt").read_text().strip()
    migrated_rc = (migrated_dir / "rc.txt").read_text().strip()
    if legacy_rc != migrated_rc:
        problems.append(f"return code {legacy_rc} (legacy) vs {migrated_rc} (migrated)")
    else:
        checks.append(f"return code: {legacy_rc}")

    for line in checks:
        print(f"  OK   {line}")
    for line in problems:
        print(f"  FAIL {line}")
    print(f"\n{len(checks)} checks passed, {len(problems)} differences")
    return 1 if problems else 0


if __name__ == "__main__":
    raise SystemExit(main())
