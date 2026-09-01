#!/usr/bin/env python3
"""Value-level diff of the CBTRN02C baseline outputs against the migrated outputs.

Normalization rules applied before comparison:
  * TRAN-PROC-TS (app/cpy/CVTRA05Y.cpy:48, offset 304 len 26) is set from FUNCTION CURRENT-DATE at
    run time (app/cbl/CBTRN02C.cbl:692-693), so it can never match between two runs. It is masked
    and instead checked for the DB2 format 'EEEE-MM-DD-UU.MM.SS.HH0000'.
  * Nothing else is normalized: both sides are compared byte for byte, including zoned-decimal
    sign overpunches.
"""
import re
import sys
from pathlib import Path

TS_RE = re.compile(rb"^\d{4}-\d{2}-\d{2}-\d{2}\.\d{2}\.\d{2}\.\d{2}0000$")

# file -> (record length, [(offset, length) fields to mask])
FILES = {
    "TRANSACT.after": (350, [(304, 26)]),
    "ACCTDATA.after": (300, []),
    "TCATBALF.after": (50, []),
    "DALYREJS.dat": (430, []),
}


def records(path, reclen):
    data = path.read_bytes()
    if len(data) % reclen:
        raise SystemExit(f"{path}: length {len(data)} not a multiple of {reclen}")
    return [data[i:i + reclen] for i in range(0, len(data), reclen)]


def main(legacy_dir, migrated_dir):
    legacy_dir, migrated_dir = Path(legacy_dir), Path(migrated_dir)
    failures = 0
    for name, (reclen, masks) in FILES.items():
        lp, mp = legacy_dir / name, migrated_dir / name
        if not lp.exists() or not mp.exists():
            print(f"FAIL {name}: missing ({lp.exists()=} {mp.exists()=})")
            failures += 1
            continue
        lrecs, mrecs = records(lp, reclen), records(mp, reclen)
        if len(lrecs) != len(mrecs):
            print(f"FAIL {name}: record count {len(lrecs)} vs {len(mrecs)}")
            failures += 1
            continue
        diffs = 0
        for idx, (a, b) in enumerate(zip(lrecs, mrecs)):
            for off, ln in masks:
                if not TS_RE.match(b[off:off + ln]):
                    print(f"FAIL {name} rec {idx}: bad timestamp format {b[off:off + ln]!r}")
                    diffs += 1
                a = a[:off] + b"?" * ln + a[off + ln:]
                b = b[:off] + b"?" * ln + b[off + ln:]
            if a != b:
                diffs += 1
                if diffs <= 3:
                    col = next(i for i in range(reclen) if a[i] != b[i])
                    print(f"FAIL {name} rec {idx} col {col}: "
                          f"{a[col:col + 20]!r} vs {b[col:col + 20]!r}")
        if diffs:
            print(f"FAIL {name}: {diffs} differing records of {len(lrecs)}")
            failures += 1
        else:
            print(f"PASS {name}: {len(lrecs)} records identical")

    lso = (legacy_dir / "SYSOUT.txt").read_text(errors="replace").splitlines()
    mso = (migrated_dir / "SYSOUT.txt").read_text(errors="replace").splitlines()
    if lso == mso:
        print(f"PASS SYSOUT.txt: {len(lso)} lines identical")
    else:
        failures += 1
        print("FAIL SYSOUT.txt:")
        for a, b in zip(lso, mso):
            if a != b:
                print(f"  legacy: {a}\n  java  : {b}")
        if len(lso) != len(mso):
            print(f"  line count {len(lso)} vs {len(mso)}")

    print("PARITY: " + ("PASS" if failures == 0 else f"FAIL ({failures} file(s))"))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1], sys.argv[2]))
