#!/usr/bin/env bash
# Run the migrated Spring Batch step over the same inputs as legacy_run.sh and capture the same
# set of outputs, in the same shapes, so the two directories can be diffed directly.
#
#   usage: java_run.sh <input-dir> <work-dir>
set -euo pipefail

IN=$(cd "$1" && pwd)
WORK=$2
HARNESS=$(cd "$(dirname "$0")" && pwd)
JAVA_DIR=$HARNESS/../java
JAR=$JAVA_DIR/target/posttran-1.0.0.jar

mkdir -p "$WORK"/{data,out}
WORK=$(cd "$WORK" && pwd)

if [ ! -f "$JAR" ]; then
  (cd "$JAVA_DIR" && mvn -q -B -DskipTests package)
fi

# ACCTFILE and TCATBALF are updated in place by the step, so stage writable copies.
pad () { awk -v n="$2" '{printf "%-*s", n, $0}' "$1"; }
pad "$IN/dalytran.txt" 350 > "$WORK/data/DALYTRAN"
pad "$IN/cardxref.txt"  50 > "$WORK/data/XREFFILE"
pad "$IN/acctdata.txt" 300 > "$WORK/data/ACCTFILE"
pad "$IN/tcatbal.txt"   50 > "$WORK/data/TCATBALF"
rm -f "$WORK"/data/TRANFILE "$WORK"/data/DALYREJS

set +e
java -jar "$JAR" \
  --posttran.dd.dalytran="$WORK/data/DALYTRAN" \
  --posttran.dd.tranfile="$WORK/data/TRANFILE" \
  --posttran.dd.xreffile="$WORK/data/XREFFILE" \
  --posttran.dd.dalyrejs="$WORK/data/DALYREJS" \
  --posttran.dd.acctfile="$WORK/data/ACCTFILE" \
  --posttran.dd.tcatbalf="$WORK/data/TCATBALF" \
  > "$WORK/out/sysout.txt" 2>&1
RC=$?
set -e
echo "$RC" > "$WORK/out/rc.txt"

# Same names the legacy harness produces: the keyed stores are already saved in key order.
cp "$WORK/data/TRANFILE" "$WORK/out/TRANFILE.seq"
cp "$WORK/data/ACCTFILE" "$WORK/out/ACCTFILE.seq"
cp "$WORK/data/TCATBALF" "$WORK/out/TCATBALF.seq"
cp "$WORK/data/DALYREJS" "$WORK/out/DALYREJS"

echo "migrated run complete: RC=$RC, outputs in $WORK/out"
