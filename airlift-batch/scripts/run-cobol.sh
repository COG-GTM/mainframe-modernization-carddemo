#!/usr/bin/env bash
#
# Run the left side of the harness: the unmodified app/cbl programs under
# GnuCOBOL, wired to the same DD names their JCL uses.
#
#   POSTTRAN.jcl STEP15  EXEC PGM=CBTRN02C
#   INTCALC.jcl  STEP15  EXEC PGM=CBACT04C,PARM='2022071800'
#   READACCT.jcl STEP05  EXEC PGM=CBACT01C          (reference slice)
#
# GnuCOBOL resolves `ASSIGN TO ACCTFILE` through the environment variable
# DD_ACCTFILE, so the DD statements in the JCL map one-for-one onto the
# exports below. The VSAM KSDS clusters become GnuCOBOL indexed files loaded by
# AIRLOAD (the IDCAMS REPRO stand-in) and unloaded by AIRUNLD.
set -euo pipefail

BUILD="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/build"
BIN="$BUILD/bin"
FIX="$BUILD/fixtures"
VSAM="$BUILD/vsam"
REFVSAM="$BUILD/vsam-reference"
OUT="$BUILD/cobol"
GOLD="$BUILD/golden"
PARM_DATE="${AIRLIFT_PARM_DATE:-2022071800}"

export COB_PRE_LOAD=""
export AIRLIFT_CLOCK="${AIRLIFT_CLOCK:?AIRLIFT_CLOCK must be set}"

rm -rf "$VSAM" "$REFVSAM" "$OUT" "$GOLD"
mkdir -p "$VSAM" "$REFVSAM" "$OUT" "$GOLD"

load() { # load <KIND> <flat file> <indexed file>
  DD_INFILE="$2" DD_OUTFILE="$3" "$BIN/AIRLOAD" "$1"
}

unload() { # unload <KIND> <indexed file> <flat file>
  DD_INFILE="$2" DD_OUTFILE="$3" "$BIN/AIRUNLD" "$1"
}

echo "== IDCAMS REPRO stand-in: loading the KSDS clusters"
load ACCT "$FIX/acctdata.dat" "$VSAM/acctdata"
load XREF "$FIX/cardxref.dat" "$VSAM/cardxref"
load DISC "$FIX/discgrp.dat"  "$VSAM/discgrp"
load TCAT "$FIX/tcatbal.dat"  "$VSAM/tcatbal"
load ACCT "$FIX/acctdata.dat" "$REFVSAM/acctdata"

echo "== POSTTRAN STEP15: CBTRN02C"
set +e
DD_DALYTRAN="$FIX/dalytran.dat" \
DD_TRANFILE="$VSAM/transact" \
DD_XREFFILE="$VSAM/cardxref" \
DD_DALYREJS="$OUT/dalyrejs.dat" \
DD_ACCTFILE="$VSAM/acctdata" \
DD_TCATBALF="$VSAM/tcatbal" \
  "$BIN/CBTRN02C" | tee "$OUT/posttran.log"
rc=${PIPESTATUS[0]}
set -e
# CBTRN02C:229-231 sets RETURN-CODE 4 when it wrote rejects, which is a
# successful run of the step; anything else is a failure.
if [ "$rc" -ne 0 ] && [ "$rc" -ne 4 ]; then
  echo "CBTRN02C failed with RC=$rc" >&2
  exit "$rc"
fi
echo "CBTRN02C RC=$rc"

echo "== INTCALC STEP15: CBACT04C PARM='$PARM_DATE'"
DD_TCATBALF="$VSAM/tcatbal" \
DD_XREFFILE="$VSAM/cardxref" \
DD_ACCTFILE="$VSAM/acctdata" \
DD_DISCGRP="$VSAM/discgrp" \
DD_TRANSACT="$OUT/systran.dat" \
  "$BIN/INTDRV" "$PARM_DATE" > "$OUT/intcalc.log"
tail -3 "$OUT/intcalc.log"

echo "== READACCT STEP05: CBACT01C (reference slice)"
DD_ACCTFILE="$REFVSAM/acctdata" "$BIN/CBACT01C" > "$OUT/acctreport.txt"

echo "== IDCAMS REPRO stand-in: unloading the KSDS clusters"
unload TRAN "$VSAM/transact" "$GOLD/transact.dat"
unload ACCT "$VSAM/acctdata" "$GOLD/acctdata.dat"
unload TCAT "$VSAM/tcatbal"  "$GOLD/tcatbal.dat"
cp "$OUT/dalyrejs.dat"   "$GOLD/dalyrejs.dat"
cp "$OUT/systran.dat"    "$GOLD/systran.dat"
cp "$OUT/acctreport.txt" "$GOLD/acctreport.txt"

echo "== golden outputs"
ls -l "$GOLD"
