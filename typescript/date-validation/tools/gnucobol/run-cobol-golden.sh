#!/usr/bin/env bash
# Builds DTEDRIVR with the real CSUTLDWY/CSUTLDPY copybooks under GnuCOBOL
# (apt install gnucobol) and writes test/golden/cobol-results.txt.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
PKG="$HERE/../.."
REPO="$PKG/../.."
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

cobc -x -std=ibm -I "$REPO/app/cpy" -o "$WORK/dtedrivr" "$HERE/DTEDRIVR.cbl" "$HERE/CSUTLDTC.cbl"
cp "$PKG/test/golden/cases.txt" "$WORK/DTEIN"
(cd "$WORK" && DD_DTEIN=DTEIN DD_DTEOUT=DTEOUT ./dtedrivr)
sed 's/ *$//' "$WORK/DTEOUT" > "$PKG/test/golden/cobol-results.txt"
echo "wrote $(wc -l < "$PKG/test/golden/cobol-results.txt") results"
