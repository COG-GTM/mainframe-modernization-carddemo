#!/usr/bin/env bash
# End-to-end parity run: legacy CBTRN02C vs the migrated Spring Batch step, over one input set.
#
#   usage: parity.sh [input-dir] [work-dir]
#
# Defaults to the branch-coverage mock set. Use the shipped drop instead with:
#   parity.sh ../testdata/shipped
set -euo pipefail

HARNESS=$(cd "$(dirname "$0")" && pwd)
IN=${1:-$HARNESS/../testdata/mock}
WORK=${2:-${TMPDIR:-/tmp}/posttran-parity}

rm -rf "$WORK"
mkdir -p "$WORK"

echo "== legacy =="
"$HARNESS/legacy_run.sh" "$IN" "$WORK/legacy" | tail -1
echo "== migrated =="
"$HARNESS/java_run.sh" "$IN" "$WORK/migrated" | tail -1
echo "== diff =="
python3 "$HARNESS/compare.py" "$WORK/legacy/out" "$WORK/migrated/out"
