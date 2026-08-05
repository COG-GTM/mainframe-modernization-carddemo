#!/usr/bin/env bash
# Regenerate the CBACT01C equivalence baseline by compiling and running the COBOL
# program itself, so the Java migration is compared against real COBOL output.
#
# Requires GnuCOBOL (cobc). On Ubuntu: sudo apt-get install -y gnucobol
#
# Usage: modern/tools/generate_golden.sh
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
tools_dir="$repo_root/modern/tools"
golden="$repo_root/modern/src/test/resources/golden/CBACT01C.expected.txt"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

# CBACT01C reads an INDEXED (KSDS) dataset, so the ASCII extract is loaded into an
# indexed file first, mirroring the IDCAMS REPRO of the mainframe job.
cobc -x -free "$tools_dir/LOADACCT.cbl" -o "$work/loadacct"
cobc -x -I "$repo_root/app/cpy" "$repo_root/app/cbl/CBACT01C.cbl" -o "$work/cbact01c"

INFILE="$repo_root/app/data/ASCII/acctdata.txt" ACCTFILE="$work/acct.dat" "$work/loadacct"
ACCTFILE="$work/acct.dat" "$work/cbact01c" > "$golden"

echo "wrote $golden"
