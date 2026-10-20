#!/usr/bin/env bash
# Runs the original app/cbl/CBTRN03C.cbl under GnuCOBOL against a test fixture and prints the
# report as line-sequential 133-byte records, for comparison with the Java golden files.
# Usage: crosscheck.sh <scenario-dir> <lookup-dir> <output-report>
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../../../.." && pwd)"
scenario="$(cd "$1" && pwd)"; lookups="$(cd "$2" && pwd)"; out="$3"
work="$(mktemp -d)"; trap 'rm -rf "$work"' EXIT

cobc -x -std=ibm -fsign=EBCDIC -I "$repo/app/cpy" -o "$work/cbtrn03c" "$repo/app/cbl/CBTRN03C.cbl"
cobc -x -o "$work/loadidx" "$here/LOADIDX.cbl"

export DD_XREFIN="$lookups/cardxref.txt" DD_TYPEIN="$lookups/trantype.txt" DD_CATGIN="$lookups/trancatg.txt"
export DD_CARDXREF="$work/cardxref.idx" DD_TRANTYPE="$work/trantype.idx" DD_TRANCATG="$work/trancatg.idx"
"$work/loadidx"

# TRANFILE / DATEPARM / TRANREPT are ORGANIZATION SEQUENTIAL (fixed-length, no newlines).
python3 - "$scenario" "$work" <<'PY'
import sys
scenario, work = sys.argv[1:]
def fixed(src, dst, n):
    with open(src, 'rb') as f:
        lines = f.read().split(b'\n')
    if lines and lines[-1] == b'':
        lines.pop()
    with open(dst, 'wb') as f:
        f.write(b''.join(l.rstrip(b'\r').ljust(n)[:n] for l in lines))
fixed(scenario + '/tranfile.txt', work + '/tranfile.dat', 350)
fixed(scenario + '/dateparm.txt', work + '/dateparm.dat', 80)
PY
export DD_TRANFILE="$work/tranfile.dat" DD_DATEPARM="$work/dateparm.dat" DD_TRANREPT="$work/tranrept.dat"
(cd "$work" && ./cbtrn03c > "$work/sysout.txt") || { cat "$work/sysout.txt"; exit 1; }

python3 - "$work/tranrept.dat" "$out" <<'PY'
import sys
data = open(sys.argv[1], 'rb').read()
assert len(data) % 133 == 0, len(data)
with open(sys.argv[2], 'wb') as f:
    f.write(b''.join(data[i:i + 133] + b'\n' for i in range(0, len(data), 133)))
PY
