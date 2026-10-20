#!/usr/bin/env bash
# Regenerates src/test/resources/golden/* by running the original COBOL program
# app/cbl/CBTRN02C.cbl under GnuCOBOL (BDB indexed files) against each fixture.
#
#   modern/batch/cbtrn02c/src/test/cobol/regen-golden.sh
#
# Requires cobc (GnuCOBOL 3.x with an indexed file handler) and python3.
# FUNCTION CURRENT-DATE is pinned with COB_CURRENT_DATE to the same instant as
# Cbtrn02cGoldenTest.CLOCK (2022-06-11 03:15:30.45 UTC).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
MODULE="$(cd "$HERE/../../.." && pwd)"
REPO="$(cd "$MODULE/../../.." && pwd)"
RES="$MODULE/src/test/resources"
WORK="$MODULE/target/cobol-golden"
COBC=(cobc -x -std=ibm -fsign=EBCDIC)

rm -rf "$WORK" && mkdir -p "$WORK/bin"

"${COBC[@]}" -I "$REPO/app/cpy" -o "$WORK/bin/CBTRN02C" "$REPO/app/cbl/CBTRN02C.cbl"

# gen <name> <reclen> <keylen> <direction: load|unload>
gen() {
  local name=$1 len=$2 key=$3 dir=$4 in out
  local ls='ORGANIZATION IS LINE SEQUENTIAL'
  local idx="ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL RECORD KEY IS @SIDE@-KEY"
  if [[ $dir == load ]]; then in=$ls; out=${idx//@SIDE@/OUT}; out=${out/SEQUENTIAL RECORD/RANDOM RECORD}
  else in=${idx//@SIDE@/IN}; out=$ls; fi
  sed -e "s/@PGM@/${name}/" -e "s/@LEN@/${len}/" -e "s/@KEY@/${key}/" \
      -e "s/@REST@/$((len - key))/" -e "s/@IN-ORG@/${in}/" -e "s/@OUT-ORG@/${out}/" \
      "$HERE/IDXCOPY.cbl.in" > "$WORK/${name}.cbl"
  "${COBC[@]}" -free -o "$WORK/bin/${name}" "$WORK/${name}.cbl"
}
gen LXREF 50 16 load
gen LACCT 300 11 load
gen LTCAT 50 17 load
gen UACCT 300 11 unload
gen UTCAT 50 17 unload
gen UTRAN 350 16 unload

copy() { INFILE="$2" OUTFILE="$3" COB_LS_FIXED=TRUE "$WORK/bin/$1"; }

# Line-sequential <-> record-sequential (fixed length, no delimiters).
to_fixed() { python3 - "$1" "$2" "$3" <<'PY'
import sys
src, dst, n = sys.argv[1], sys.argv[2], int(sys.argv[3])
recs = [l.rstrip(b'\r') for l in open(src, 'rb').read().split(b'\n') if l]
open(dst, 'wb').write(b''.join(r[:n].ljust(n, b' ') for r in recs))
PY
}
to_lines() { python3 - "$1" "$2" "$3" <<'PY'
import sys
src, dst, n = sys.argv[1], sys.argv[2], int(sys.argv[3])
data = open(src, 'rb').read()
assert len(data) % n == 0, (src, len(data))
open(dst, 'wb').write(b''.join(data[i:i + n] + b'\n' for i in range(0, len(data), n)))
PY
}

run_case() {
  local name=$1 in=$2 dir="$WORK/$1" gold="$RES/golden/$1"
  mkdir -p "$dir" "$gold"
  to_fixed "$in/dailytran.txt" "$dir/DALYTRAN" 350
  copy LXREF "$in/cardxref.txt" "$dir/XREFFILE"
  copy LACCT "$in/acctdata.txt" "$dir/ACCTFILE"
  copy LTCAT "$in/tcatbal.txt" "$dir/TCATBALF"
  set +e
  (cd "$dir" && DALYTRAN="$dir/DALYTRAN" TRANFILE="$dir/TRANFILE" XREFFILE="$dir/XREFFILE" \
     DALYREJS="$dir/DALYREJS" ACCTFILE="$dir/ACCTFILE" TCATBALF="$dir/TCATBALF" \
     COB_CURRENT_DATE="2022/06/11 03:15:30.45" "$WORK/bin/CBTRN02C" > "$dir/sysout.txt")
  local rc=$?
  set -e
  echo "$name: RC=$rc"
  echo "$rc" > "$gold/rc.txt"
  cp "$dir/sysout.txt" "$gold/sysout.txt"
  copy UTRAN "$dir/TRANFILE" "$gold/tranfile.txt"
  copy UACCT "$dir/ACCTFILE" "$gold/acctdata.txt"
  copy UTCAT "$dir/TCATBALF" "$gold/tcatbal.txt"
  to_lines "$dir/DALYREJS" "$gold/dalyrejs.txt" 430
}

run_case full "$REPO/app/data/ASCII"
for f in "$RES"/fixtures/*/; do
  run_case "$(basename "$f")" "${f%/}"
done
