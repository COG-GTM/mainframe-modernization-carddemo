#!/usr/bin/env bash
# Compiles and runs the original COBOL CBTRN02C under GnuCOBOL (BDB indexed
# files standing in for VSAM) against the ASCII sample data, then unloads the
# results to line-sequential files. Used to (re)generate test/golden/.
#
# TRAN-PROC-TS comes from FUNCTION CURRENT-DATE (wall clock) and is not
# reproducible; test/golden.test.ts masks it when comparing transact.txt.
#
# Usage: tools/gnucobol/run-cobol-posttran.sh [DATA_DIR] [OUT_DIR]
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../../../../.." && pwd)"
DATA="${1:-$REPO/app/data/ASCII}"
OUT="${2:-$HERE/../../test/golden}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$OUT"

cobc -x -std=ibm -fsign=EBCDIC -I "$REPO/app/cpy" -o "$WORK/CBTRN02C" "$REPO/app/cbl/CBTRN02C.cbl"
cobc -x -fsign=EBCDIC -o "$WORK/LOADKSDS" "$HERE/LOADKSDS.cbl"
cobc -x -fsign=EBCDIC -o "$WORK/UNLDKSDS" "$HERE/UNLDKSDS.cbl"

export DD_XREFIN="$DATA/cardxref.txt" DD_ACCTIN="$DATA/acctdata.txt" DD_TCATIN="$DATA/tcatbal.txt"
export DD_XREFFILE="$WORK/xref.ksds" DD_ACCTFILE="$WORK/acct.ksds"
export DD_TCATBALF="$WORK/tcatbal.ksds" DD_TRANFILE="$WORK/transact.ksds"
"$WORK/LOADKSDS"

# DALYTRAN / DALYREJS are fixed-length RECFM=F datasets: strip/add newlines.
tr -d '\n' < "$DATA/dailytran.txt" > "$WORK/dalytran.ps"
export DD_DALYTRAN="$WORK/dalytran.ps" DD_DALYREJS="$WORK/dalyrejs.ps"
set +e
"$WORK/CBTRN02C" > "$OUT/sysout.txt"
RC=$?
set -e
echo "CBTRN02C RC=$RC"

export DD_ACCTOUT="$OUT/acctdata.txt" DD_TCATOUT="$OUT/tcatbal.txt" DD_TRANOUT="$OUT/transact.txt"
"$WORK/UNLDKSDS"
fold -w 430 "$WORK/dalyrejs.ps" > "$OUT/dalyrejs.txt"; echo >> "$OUT/dalyrejs.txt"
echo "$RC" > "$OUT/returncode.txt"
