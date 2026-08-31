#!/usr/bin/env python3
"""Attribute the mismatches compare.py found to the traps declared in traps.json.

The split matters: compare.py counts differences without knowing what is supposed
to differ, and this step reads its JSON output afterwards. A mismatch in a
(dataset, field) pair that no trap claims is `unclassified`, which fails, and a
trap that claims a pair but produced no mismatch is `unwitnessed`, which also
fails - the manifest is not allowed to drift away from the code.

Usage:
    attribute.py <mismatches.json> [--manifest parity/traps.json]
"""

from __future__ import annotations

import argparse
import collections
import json
from pathlib import Path

HERE = Path(__file__).resolve().parent


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("mismatches", type=Path)
    parser.add_argument("--manifest", type=Path, default=HERE / "traps.json")
    arguments = parser.parse_args()

    report = json.loads(arguments.mismatches.read_text(encoding="utf-8"))
    manifest = json.loads(arguments.manifest.read_text(encoding="utf-8"))

    owners: dict[str, list[str]] = collections.defaultdict(list)
    for trap in manifest["traps"]:
        for pair in trap["owns"]:
            owners[pair].append(trap["id"])

    observed: collections.Counter[str] = collections.Counter()
    unclassified: collections.Counter[str] = collections.Counter()
    for mismatch in report["mismatches"]:
        pair = f"{mismatch['dataset']}/{mismatch['field']}"
        claimants = owners.get(pair)
        if not claimants:
            unclassified[pair] += 1
            continue
        for trap in claimants:
            observed[trap] += 1

    print(f"slice: {manifest['slice']}")
    for trap in manifest["traps"]:
        count = observed[trap["id"]]
        state = "observed" if count else "UNWITNESSED"
        print(f"{trap['id']} {state:11s} {count:5d} mismatches  {trap['class']}")
        print(f"   {trap['title']}")
        print(f"   contradicts {trap['contradicts']}")

    classified = sum(1 for m in report["mismatches"] if f"{m['dataset']}/{m['field']}" in owners)
    total = len(report["mismatches"])
    print(f"classified={classified} unclassified={total - classified} of {total} mismatches")
    for pair, count in sorted(unclassified.items()):
        print(f"   unclassified {pair}: {count}")

    unwitnessed = [trap["id"] for trap in manifest["traps"] if not observed[trap["id"]]]
    if unwitnessed:
        print(f"traps claiming fields that are at parity: {', '.join(unwitnessed)}")
    raise SystemExit(1 if unclassified or unwitnessed else 0)


if __name__ == "__main__":
    main()
