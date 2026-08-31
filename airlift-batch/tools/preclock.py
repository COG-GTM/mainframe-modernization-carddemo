#!/usr/bin/env python3
"""Stage app/cbl sources for GnuCOBOL, substituting only the wall clock.

The left side of the harness has to be the customer's code, so the only edit
applied here is the one that makes a run reproducible: every

    MOVE FUNCTION CURRENT-DATE TO COBOL-TS

becomes

    CALL 'AIRCLOCK' USING COBOL-TS

which reads the fixed timestamp from AIRLIFT_CLOCK (see cobol/AIRCLOCK.cbl).
The staged copies are written under the build directory; app/ is never
touched. Any other difference between app/cbl and the staged file is a bug in
this tool, so the substitution count is asserted and `--diff` prints the whole
delta for review.
"""

from __future__ import annotations

import argparse
import difflib
import re
import shutil
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
CBL = REPO / "app" / "cbl"
CPY = REPO / "app" / "cpy"

CLOCK_RE = re.compile(
    r"^(?P<indent> {6,})MOVE FUNCTION CURRENT-DATE TO COBOL-TS\s*$",
)
REPLACEMENT = "CALL 'AIRCLOCK' USING COBOL-TS"
CODE_END = 72


def substitute(text: str) -> tuple[str, int]:
    out: list[str] = []
    count = 0
    for line in text.splitlines():
        match = CLOCK_RE.match(line.rstrip("\r"))
        if match is None:
            out.append(line)
            continue
        replaced = match.group("indent") + REPLACEMENT
        if len(replaced) > CODE_END:
            raise SystemExit(f"substitution exceeds column {CODE_END}: {replaced!r}")
        out.append(replaced)
        count += 1
    return "\n".join(out) + "\n", count


def stage(name: str, dest: Path, expected_clocks: int, show_diff: bool) -> None:
    source = CBL / name
    original = source.read_text(encoding="latin-1")
    staged, count = substitute(original)
    if count != expected_clocks:
        raise SystemExit(
            f"{name}: expected {expected_clocks} CURRENT-DATE substitutions, made {count}"
        )
    target = dest / name
    target.write_text(staged, encoding="latin-1")
    if show_diff:
        diff = difflib.unified_diff(
            original.splitlines(),
            staged.splitlines(),
            fromfile=f"app/cbl/{name}",
            tofile=f"staged/{name}",
            lineterm="",
        )
        for line in diff:
            print(line)
    print(f"staged {name} ({count} clock substitution(s))")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--diff", action="store_true")
    args = parser.parse_args()

    src = args.out / "src"
    copy = args.out / "copy"
    src.mkdir(parents=True, exist_ok=True)
    if copy.exists():
        shutil.rmtree(copy)
    shutil.copytree(CPY, copy)

    stage("CBTRN02C.cbl", src, 1, args.diff)
    stage("CBACT04C.cbl", src, 1, args.diff)
    stage("CBACT01C.cbl", src, 0, args.diff)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
