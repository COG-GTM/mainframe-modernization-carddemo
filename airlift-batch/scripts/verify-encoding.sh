#!/usr/bin/env bash
#
# The committed codec evidence must be exactly what the compiler just produced.
# scripts/evidence.sh writes build/evidence; the copies under parity/vectors and
# mirror/src/test/resources/evidence are what the two codecs are tested against,
# so if the two ever diverge the tests are asserting against stale bytes.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EVIDENCE="$ROOT/build/evidence"

cmp "$EVIDENCE/packvec.bin" "$ROOT/parity/vectors/packvec.bin"
cmp "$EVIDENCE/packvec.txt" "$ROOT/parity/vectors/packvec.txt"
cmp "$EVIDENCE/packvec.bin" "$ROOT/mirror/src/test/resources/evidence/packvec.bin"
cmp "$EVIDENCE/packvec.txt" "$ROOT/mirror/src/test/resources/evidence/packvec.txt"
cmp "$EVIDENCE/arith.txt"   "$ROOT/mirror/src/test/resources/evidence/arith.txt"

echo "codec evidence matches the compiler output"
