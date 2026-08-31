#!/usr/bin/env bash
#
# The second half of the demo: apply parity/fix-traps.patch to a copy of the
# mirror, rebuild it, run it against the same fixtures and compare again. The
# patch is the whole fix - four semantics in three files, plus the three test
# cases the trapped suite did not cover - and nothing else is touched, so the
# 100% figure cannot come from a changed fixture or a changed comparator.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# Sits beside mirror/ rather than under build/ so the relative copybook paths
# the module's tests use resolve the same way they do in mirror/.
WORK="$ROOT/mirror-fixed"

rm -rf "$WORK"
mkdir -p "$WORK"
cp -r "$ROOT/mirror/pom.xml" "$ROOT/mirror/src" "$WORK/"
patch -p1 -d "$WORK" --no-backup-if-mismatch < "$ROOT/parity/fix-traps.patch"

echo "== the fixed mirror's own tests"
mvn -q -f "$WORK/pom.xml" test

echo "== the fixed mirror against the COBOL golden output"
mvn -q -f "$WORK/pom.xml" -DskipTests package
"$ROOT/scripts/run-java.sh" "$WORK/target/airlift-batch-mirror.jar" "$ROOT/build/java-fixed"
python3 "$ROOT/parity/compare.py" "$ROOT/build/golden" "$ROOT/build/java-fixed"
python3 "$ROOT/parity/compare.py" "$ROOT/build/golden" "$ROOT/build/java-fixed" --slice readacct
