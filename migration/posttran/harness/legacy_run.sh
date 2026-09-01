#!/usr/bin/env bash
# Run the unmodified legacy CBTRN02C (app/cbl/CBTRN02C.cbl) under GnuCOBOL and
# capture every output and side effect of POSTTRAN STEP15 as flat, diffable files.
#
#   usage: legacy_run.sh <input-dir> <work-dir>
#
# <input-dir> holds newline-delimited fixed-width text in the app/data/ASCII
# convention: dalytran.txt (350), cardxref.txt (50), acctdata.txt (300),
# tcatbal.txt (50).
#
# Produces in <work-dir>/out:
#   TRANFILE.seq   posted transactions, key order        (350 x n)
#   ACCTFILE.seq   account after-images, key order       (300 x n)
#   TCATBALF.seq   category-balance after-images         ( 50 x n)
#   DALYREJS       reject file exactly as written        (430 x n)
#   sysout.txt     the step's SYSOUT (all DISPLAYs)
#   rc.txt         the step return code
#
# The DD names below are the ones in POSTTRAN.jcl:28-42; GnuCOBOL resolves
# `ASSIGN TO <name>` through the environment variable DD_<name>.
set -euo pipefail

IN=$(cd "$1" && pwd)
WORK=$2
HARNESS=$(cd "$(dirname "$0")" && pwd)
REPO=$(cd "$HARNESS/../../.." && pwd)
LEGACY_SRC=$REPO/app/cbl/CBTRN02C.cbl
CPY=$REPO/app/cpy

mkdir -p "$WORK"/{bin,data,out}
WORK=$(cd "$WORK" && pwd)

# ---------------------------------------------------------------- code gen
# Emit and compile a sequential->indexed loader or indexed->sequential unloader
# for a given record/key length. Kept generic so record layouts stay in the
# copybooks and are not duplicated here.
gen_util () {
  local kind=$1 name=$2 reclen=$3 keylen=$4
  local datalen=$((reclen - keylen))
  local in_org out_org in_extra out_extra keyed
  keyed="                  ACCESS MODE IS SEQUENTIAL"$'\n'"                  RECORD KEY IS"
  if [ "$kind" = load ]; then
    in_org=SEQUENTIAL; out_org=INDEXED; in_extra=""; out_extra="$keyed OUT-KEY"
  else
    in_org=INDEXED; out_org=SEQUENTIAL; in_extra="$keyed IN-KEY"; out_extra=""
  fi
  cat > "$WORK/bin/$name.cbl" <<EOF
       IDENTIFICATION DIVISION.
       PROGRAM-ID. $name.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
                  ORGANIZATION IS $in_org
$in_extra
                  FILE STATUS IS IN-ST.
           SELECT OUT-FILE ASSIGN TO OUTFILE
                  ORGANIZATION IS $out_org
$out_extra
                  FILE STATUS IS OUT-ST.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC.
           05 IN-KEY       PIC X($keylen).
           05 IN-DATA      PIC X($datalen).
       FD  OUT-FILE.
       01  OUT-REC.
           05 OUT-KEY      PIC X($keylen).
           05 OUT-DATA     PIC X($datalen).
       WORKING-STORAGE SECTION.
       01  IN-ST           PIC XX.
       01  OUT-ST          PIC XX.
       01  WS-CNT          PIC 9(6) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE
           OPEN OUTPUT OUT-FILE
           PERFORM UNTIL 1 = 2
              READ IN-FILE
                 AT END EXIT PERFORM
              END-READ
              MOVE IN-REC TO OUT-REC
              WRITE OUT-REC
              IF OUT-ST NOT = '00'
                 DISPLAY '$name WRITE ERROR ' OUT-ST
                 MOVE 12 TO RETURN-CODE
                 STOP RUN
              END-IF
              ADD 1 TO WS-CNT
           END-PERFORM
           CLOSE IN-FILE OUT-FILE
           DISPLAY '$name ' WS-CNT
           GOBACK.
EOF
  cobc -x -o "$WORK/bin/$name" "$WORK/bin/$name.cbl"
}

gen_util load   LOADXREF  50 16
gen_util load   LOADACCT 300 11
gen_util load   LOADTCB   50 17
gen_util unload UNLDTRAN 350 16
gen_util unload UNLDACCT 300 11
gen_util unload UNLDTCB   50 17

# The legacy program itself is compiled unmodified, straight from app/cbl.
cobc -x -fsign=EBCDIC -I "$CPY" -o "$WORK/bin/CBTRN02C" "$LEGACY_SRC"

# ------------------------------------------------------------- input staging
# RECFM=F on the mainframe: no record delimiters. Pad and concatenate.
pad () { awk -v n="$2" '{printf "%-*s", n, $0}' "$1"; }
pad "$IN/dalytran.txt" 350 > "$WORK/data/DALYTRAN"
pad "$IN/cardxref.txt"  50 > "$WORK/data/CARDXREF.seq"
pad "$IN/acctdata.txt" 300 > "$WORK/data/ACCTDATA.seq"
pad "$IN/tcatbal.txt"   50 > "$WORK/data/TCATBAL.seq"

rm -f "$WORK"/data/{XREFFILE,ACCTFILE,TCATBALF,TRANFILE,DALYREJS}
INFILE=$WORK/data/CARDXREF.seq OUTFILE=$WORK/data/XREFFILE "$WORK/bin/LOADXREF"
INFILE=$WORK/data/ACCTDATA.seq OUTFILE=$WORK/data/ACCTFILE "$WORK/bin/LOADACCT"
INFILE=$WORK/data/TCATBAL.seq  OUTFILE=$WORK/data/TCATBALF "$WORK/bin/LOADTCB"

# ------------------------------------------------------------------ the step
set +e
DD_DALYTRAN=$WORK/data/DALYTRAN \
DD_TRANFILE=$WORK/data/TRANFILE \
DD_XREFFILE=$WORK/data/XREFFILE \
DD_DALYREJS=$WORK/data/DALYREJS \
DD_ACCTFILE=$WORK/data/ACCTFILE \
DD_TCATBALF=$WORK/data/TCATBALF \
  "$WORK/bin/CBTRN02C" > "$WORK/out/sysout.txt" 2>&1
RC=$?
set -e
echo "$RC" > "$WORK/out/rc.txt"

# ------------------------------------------------------- after-image capture
INFILE=$WORK/data/TRANFILE OUTFILE=$WORK/out/TRANFILE.seq "$WORK/bin/UNLDTRAN"
INFILE=$WORK/data/ACCTFILE OUTFILE=$WORK/out/ACCTFILE.seq "$WORK/bin/UNLDACCT"
INFILE=$WORK/data/TCATBALF OUTFILE=$WORK/out/TCATBALF.seq "$WORK/bin/UNLDTCB"
cp "$WORK/data/DALYREJS" "$WORK/out/DALYREJS"

echo "legacy run complete: RC=$RC, outputs in $WORK/out"
