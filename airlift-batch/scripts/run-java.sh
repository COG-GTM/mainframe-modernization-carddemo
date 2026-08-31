#!/usr/bin/env bash
#
# Run the right side: the Spring Batch mirror over the same fixtures the COBOL
# read, writing the same fixed-width datasets.
#
#   run-java.sh <jar> <out-dir>
#
# The two Spring Batch jobs correspond to the JCL steps one for one:
#   posttran-intcalc  POSTTRAN STEP15 (CBTRN02C) then INTCALC STEP15 (CBACT04C)
#   readacct          READACCT STEP05 (CBACT01C), the reference slice
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="${1:-$ROOT/mirror/target/airlift-batch-mirror.jar}"
OUT="${2:-$ROOT/build/java}"
FIX="$ROOT/build/fixtures"
PARM_DATE="${AIRLIFT_PARM_DATE:-2022071800}"
CLOCK="${AIRLIFT_CLOCK:?AIRLIFT_CLOCK must be set}"

rm -rf "$OUT"
mkdir -p "$OUT"

common=(
  --airlift.copybook-dir="$ROOT/../app/cpy"
  --airlift.in="$FIX"
  --airlift.out="$OUT"
  --airlift.clock="$CLOCK"
  --airlift.parm-date="$PARM_DATE"
)

java -jar "$JAR" --spring.batch.job.name=posttran-intcalc "${common[@]}" \
  | tee "$OUT/posttran-intcalc.log" | grep -E 'TRANSACTIONS|CATEGORY BALANCES'
java -jar "$JAR" --spring.batch.job.name=readacct "${common[@]}" > "$OUT/readacct.log"
