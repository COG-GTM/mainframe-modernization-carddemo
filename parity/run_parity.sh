#!/usr/bin/env bash
# Parity loop for POSTTRAN STEP15: GnuCOBOL baseline (app/cbl/CBTRN02C.cbl, unmodified) vs the
# Java migration, same inputs, value-level diff of every output and after-image.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="$ROOT/parity/work"
IN="$WORK/in"
LEG="$WORK/out/legacy"
MIG="$WORK/out/java"

"$ROOT/parity/run_legacy.sh" >/dev/null

( cd "$ROOT/migration/posttran-java" && mvn -B -q -DskipTests package >/dev/null )

rm -rf "$MIG"; mkdir -p "$MIG"
JAR=$(ls "$ROOT"/migration/posttran-java/target/posttran-*.jar | head -1)
set +e
DD_DALYTRAN="$IN/DALYTRAN.dat" \
DD_XREFFILE="$IN/CARDXREF.dat" \
DD_ACCTFILE="$IN/ACCTDATA.dat" \
DD_TCATBALF="$IN/TCATBALF.dat" \
OUT_DIR="$MIG" java -jar "$JAR" >/dev/null 2>&1
set -e

python3 "$ROOT/parity/tools/diff_outputs.py" "$LEG" "$MIG"
