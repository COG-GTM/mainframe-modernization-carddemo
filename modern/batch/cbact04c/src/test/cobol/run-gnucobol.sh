#!/usr/bin/env bash
# Cross-check for the CBACT04C golden files: runs the original app/cbl/CBACT04C.cbl under
# GnuCOBOL (BDB indexed files) on a fixture directory and writes systran.txt / acctdata.txt in
# the same line-sequential layout the Java job produces.
#
# usage: run-gnucobol.sh <fixture-dir> <output-dir> [parm-date] [current-date]
#   e.g. run-gnucobol.sh ../../../../../app/data/ASCII /tmp/full 2022071800 "2022/07/18 10:30:00"
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(cd "$here/../../../../../.." && pwd)"
in="$(cd "$1" && pwd)"
mkdir -p "$2"
out="$(cd "$2" && pwd)"
parm="${3:-2022071800}"
export COB_CURRENT_DATE="${4:-2022/07/18 10:30:00}"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

flags=(-std=ibm -fsign=EBCDIC -I "$repo/app/cpy")
cobc -x "${flags[@]}" -o "$work/c4load" "$here/C4LOAD.cbl"
cobc -x "${flags[@]}" -o "$work/c4unload" "$here/C4UNLOAD.cbl"
cobc -x "${flags[@]}" -o "$work/c4driver" "$here/C4DRIVER.cbl"
cobc -m "${flags[@]}" -o "$work/CBACT04C.so" "$repo/app/cbl/CBACT04C.cbl"

export TCATTXT="$in/tcatbal.txt" XREFTXT="$in/cardxref.txt" ACCTTXT="$in/acctdata.txt" DISCTXT="$in/discgrp.txt"
export TCATBALF="$work/tcatbal.dat" XREFFILE="$work/xref.dat" ACCTFILE="$work/acct.dat" DISCGRP="$work/discgrp.dat"
export TRANSACT="$work/transact.dat" ACCTOUT="$work/acctout.dat"
export COB_LIBRARY_PATH="$work"

"$work/c4load"
"$work/c4driver" "$parm" > "$out/sysout.txt"
"$work/c4unload"

# Record sequential (fixed, no delimiter) -> line sequential. COB_CURRENT_DATE fixes the date and
# time to the second only, so the hundredths of TRAN-ORIG-TS/TRAN-PROC-TS (DB2-MIL) are set to
# '00' to match the Java tests' fixed Clock.
python3 - "$work/transact.dat" "$out/systran.txt" 350 "$work/acctout.dat" "$out/acctdata.txt" 300 <<'PY'
import sys
a = sys.argv[1:]
for src, dst, n in ((a[0], a[1], int(a[2])), (a[3], a[4], int(a[5]))):
    data = open(src, 'rb').read()
    assert len(data) % n == 0, (src, len(data))
    with open(dst, 'wb') as f:
        for i in range(0, len(data), n):
            rec = bytearray(data[i:i + n])
            if n == 350:
                rec[298:300] = b'00'
                rec[324:326] = b'00'
            f.write(bytes(rec) + b'\n')
PY
