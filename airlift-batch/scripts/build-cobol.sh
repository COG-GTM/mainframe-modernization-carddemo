#!/usr/bin/env bash
#
# Compile the left side: the staged app/cbl programs plus the harness modules.
#
# Flags, and why each one is here:
#   -x                  build an executable (the JCL EXEC PGM= entry point)
#   -std=ibm            IBM dialect, so the estate's syntax compiles as written
#   -fbinary-truncate   truncate USAGE COMP values to their PICTURE digits,
#                       which is the TRUNC(STD) behaviour of Enterprise COBOL
# WORKING-STORAGE items without a VALUE clause are left at the cobc default
# (initialise to PICTURE: zeros for numeric, spaces for alphanumeric), which is
# deterministic; the slice does not depend on it, since every field it reads is
# either given a VALUE or assigned before use.
# The copybook path points at the staged copy of app/cpy; the harness copy
# directory carries only AIRPACK.cpy.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="$ROOT/build"
SRC="$BUILD/src"
BIN="$BUILD/bin"
COPY="$BUILD/copy"

FLAGS=(-x -std=ibm -fbinary-truncate)

mkdir -p "$BIN"
python3 "$ROOT/tools/preclock.py" --out "$BUILD"

# Harness support modules, linked into every executable that needs them.
SUPPORT=("$ROOT/cobol/AIRCLOCK.cbl" "$ROOT/cobol/CEE3ABD.cbl")

echo "== compiling the slice programs from staged app/cbl sources"
cobc "${FLAGS[@]}" -I "$COPY" -o "$BIN/CBTRN02C" "$SRC/CBTRN02C.cbl" "${SUPPORT[@]}"
cobc "${FLAGS[@]}" -I "$COPY" -o "$BIN/CBACT01C" "$SRC/CBACT01C.cbl" "${SUPPORT[@]}"
cobc "${FLAGS[@]}" -I "$COPY" -o "$BIN/INTDRV" \
  "$ROOT/cobol/INTDRV.cbl" "$SRC/CBACT04C.cbl" "${SUPPORT[@]}"

echo "== compiling the harness utilities"
cobc "${FLAGS[@]}" -o "$BIN/AIRLOAD" "$ROOT/cobol/AIRLOAD.cbl"
cobc "${FLAGS[@]}" -o "$BIN/AIRUNLD" "$ROOT/cobol/AIRUNLD.cbl"
cobc "${FLAGS[@]}" -I "$ROOT/cobol/copy" -o "$BIN/PACKGEN" "$ROOT/cobol/PACKGEN.cbl"
cobc "${FLAGS[@]}" -o "$BIN/ARITHCHK" "$ROOT/cobol/ARITHCHK.cbl"

echo "== cobc version"
cobc --version | head -1
ls -l "$BIN"
