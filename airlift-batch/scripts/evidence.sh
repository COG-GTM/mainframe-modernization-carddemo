#!/usr/bin/env bash
#
# Regenerate the codec evidence: byte vectors and arithmetic results produced by
# the GnuCOBOL compiler itself, so neither codec's expectations are an assumption.
#
#   build/evidence/packvec.bin  one AIRPACK-RECORD per case (the bytes)
#   build/evidence/packvec.txt  the same values as COBOL renders them
#   build/evidence/arith.txt    (balance * rate) / 1200 truncated and ROUNDED
#
# The committed copies under mirror/src/test/resources and parity/vectors are
# checked against this output by scripts/verify-encoding.sh.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BIN="$ROOT/build/bin"
EVIDENCE="$ROOT/build/evidence"

mkdir -p "$EVIDENCE"

DD_OUTFILE="$EVIDENCE/packvec.bin" DD_TEXTFILE="$EVIDENCE/packvec.txt" "$BIN/PACKGEN"
DD_TEXTFILE="$EVIDENCE/arith.txt" "$BIN/ARITHCHK" >/dev/null

echo "== $EVIDENCE"
ls -l "$EVIDENCE"
