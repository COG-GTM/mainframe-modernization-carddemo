#!/usr/bin/env bash
# Reproduces app/jcl/POSTTRAN.jcl STEP15 (EXEC PGM=CBTRN02C) under GnuCOBOL.
# DD wiring mirrors POSTTRAN.jcl lines 28-42; no PARM is passed (the JCL has none).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="${1:-$ROOT/parity/work}"
IN="$WORK/in"
OUT="$WORK/out/legacy"

rm -rf "$OUT"; mkdir -p "$OUT"

# ---- decode the delivered EBCDIC extracts ----------------------------------
python3 "$ROOT/parity/tools/decode_ebcdic.py" "$IN"

# ---- compile ---------------------------------------------------------------
mkdir -p "$WORK/bin"
cobc -x -std=ibm -fsign=EBCDIC -I "$ROOT/app/cpy" -o "$WORK/bin/CBTRN02C" "$ROOT/app/cbl/CBTRN02C.cbl"
cobc -x -std=ibm -o "$WORK/bin/VSAMLOAD" "$ROOT/parity/tools/VSAMLOAD.cbl"
cobc -x -std=ibm -o "$WORK/bin/VSAMUNLD" "$ROOT/parity/tools/VSAMUNLD.cbl"
# CEE3ABD is an LE service, not a delivered member: stub it so the abend path of
# app/cbl/CBTRN02C.cbl:707-711 can be reached under the emulator instead of failing to resolve.
cobc -m -std=ibm -o "$WORK/bin/CEE3ABD.so" "$ROOT/parity/tools/CEE3ABD.cbl"
export COB_LIBRARY_PATH="$WORK/bin"

# ---- build the KSDS stand-ins from the delivered sequential seeds -----------
export COB_FILE_FORMAT=b32
export DD_SEQXREF="$IN/CARDXREF.dat" DD_XREFFILE="$OUT/CARDXREF.KSDS"
export DD_SEQACCT="$IN/ACCTDATA.dat" DD_ACCTFILE="$OUT/ACCTDATA.KSDS"
export DD_SEQTCAT="$IN/TCATBALF.dat" DD_TCATBALF="$OUT/TCATBALF.KSDS"
"$WORK/bin/VSAMLOAD" XREF
"$WORK/bin/VSAMLOAD" ACCT
"$WORK/bin/VSAMLOAD" TCAT

# ---- STEP15 ----------------------------------------------------------------
export DD_DALYTRAN="$IN/DALYTRAN.dat"          # POSTTRAN.jcl:30 DALYTRAN.PS
export DD_TRANFILE="$OUT/TRANSACT.KSDS"        # POSTTRAN.jcl:28 TRANSACT KSDS (OPEN OUTPUT)
export DD_DALYREJS="$OUT/DALYREJS.dat"         # POSTTRAN.jcl:34 GDG(+1)
set +e
"$WORK/bin/CBTRN02C" > "$OUT/SYSOUT.txt" 2>&1
rc=$?
set -e
echo "CBTRN02C RC=$rc" | tee -a "$OUT/SYSOUT.txt"

# ---- unload the after-images to flat files for comparison ------------------
export DD_SEQACCT="$OUT/ACCTDATA.after" DD_SEQTCAT="$OUT/TCATBALF.after"
export DD_SEQTRAN="$OUT/TRANSACT.after"
"$WORK/bin/VSAMUNLD" ACCT
"$WORK/bin/VSAMUNLD" TCAT
"$WORK/bin/VSAMUNLD" TRAN

echo "outputs in $OUT"
exit 0
