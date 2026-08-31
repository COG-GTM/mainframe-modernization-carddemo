#!/usr/bin/env python3
"""Field-level comparison of the COBOL and Java outputs of the airlift slice.

The comparator is deliberately neutral: it knows the record layouts and the keys
of the datasets, and nothing about the differences it is expected to find. Trap
attribution is a separate step (`attribute.py`) that reads the JSON this writes,
so the count of mismatches cannot be influenced by the manifest.

Every value is compared after decoding through the copybook, so a difference is
reported as the field it belongs to rather than as a byte offset:

    transact.dat 0000000000000042 TRAN-AMT: 100.00 != 100.0

Usage:
    compare.py <cobol-dir> <java-dir> [--slice posttran-intcalc|readacct]
               [--json <path>]
"""

from __future__ import annotations

import argparse
import json
import sys
from decimal import Decimal
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "tools"))

from codec import decode_record, read_records  # noqa: E402
from copybook import Field, load_record  # noqa: E402

CPY = HERE.parent.parent / "app" / "cpy"
HARNESS_CPY = HERE.parent / "cobol" / "copy"

MAX_REPORTED = 25


class Segment:
    """One copybook layout mapped onto a slice of the record image."""

    def __init__(
        self,
        copybook: str,
        record: str,
        offset: int = 0,
        directory: Path | None = None,
        only: tuple[str, ...] | None = None,
    ) -> None:
        self.layout = load_record(directory or CPY, copybook, record)
        self.offset = offset
        self.only = only

    def decode(self, image: bytes) -> dict[str, str | Decimal]:
        values = decode_record(self.layout, image[self.offset : self.offset + self.layout.size])
        if self.only is not None:
            values = {name: value for name, value in values.items() if name in self.only}
        return values


class Dataset:
    """A pair of files to compare, and the key that lines their records up."""

    def __init__(self, name: str, segments: list[Segment], key: tuple[str, ...]) -> None:
        self.name = name
        self.segments = segments
        self.key = key
        self.length = max(segment.offset + segment.layout.size for segment in segments)

    def read(self, directory: Path) -> dict[str, dict[str, str | Decimal]]:
        path = directory / self.name
        record = Field(name=self.name, level=1, picture=None, usage="DISPLAY", size=self.length)
        records: dict[str, dict[str, str | Decimal]] = {}
        for index, image in enumerate(read_records(path, record)):
            values: dict[str, str | Decimal] = {}
            for segment in self.segments:
                values.update(segment.decode(image))
            key = "|".join(str(values[part]) for part in self.key)
            if key in records:
                raise SystemExit(f"{path}: duplicate key {key!r} at record {index}")
            records[key] = values
        return records


def datasets() -> list[Dataset]:
    """The outputs of POSTTRAN STEP15 and INTCALC STEP15."""
    return [
        Dataset(
            "transact.dat",
            [Segment("CVTRA05Y", "TRAN-RECORD")],
            ("TRAN-ID",),
        ),
        Dataset(
            "systran.dat",
            [Segment("CVTRA05Y", "TRAN-RECORD")],
            ("TRAN-ID",),
        ),
        Dataset(
            "acctdata.dat",
            [Segment("CVACT01Y", "ACCOUNT-RECORD")],
            ("ACCT-ID",),
        ),
        Dataset(
            "tcatbal.dat",
            [Segment("CVTRA01Y", "TRAN-CAT-BAL-RECORD")],
            (
                "TRAN-CAT-KEY.TRANCAT-ACCT-ID",
                "TRAN-CAT-KEY.TRANCAT-TYPE-CD",
                "TRAN-CAT-KEY.TRANCAT-CD",
            ),
        ),
        # CBTRN02C:176-182 writes the 350-byte daily transaction followed by an
        # 80-byte validation trailer, so the reject file needs both layouts.
        Dataset(
            "dalyrejs.dat",
            [
                Segment("CVTRA06Y", "DALYTRAN-RECORD"),
                Segment(
                    "AIRREJ",
                    "AIRREJ-RECORD",
                    directory=HARNESS_CPY,
                    only=("AIRREJ-FAIL-REASON", "AIRREJ-FAIL-REASON-DESC"),
                ),
            ],
            ("DALYTRAN-ID",),
        ),
    ]


def report_lines(path: Path) -> dict[str, dict[str, str]]:
    """READACCT's output is a report, so its 'fields' are its labelled lines."""
    records: dict[str, dict[str, str]] = {}
    key = "header"
    for number, line in enumerate(path.read_text(encoding="latin-1").splitlines()):
        label, separator, value = line.partition(":")
        if not separator:
            records.setdefault(key, {})[f"line-{number}"] = line
            continue
        label = label.strip()
        if label == "ACCT-ID":
            key = value.strip()
        records.setdefault(key, {})[label] = value
    return records


def compare_records(
    left: dict[str, dict[str, str | Decimal]],
    right: dict[str, dict[str, str | Decimal]],
) -> tuple[int, int, list[dict[str, str]]]:
    """Field counts and mismatches over the union of the two sides' keys."""
    fields = 0
    mismatches: list[dict[str, str]] = []
    for key in sorted(set(left) | set(right)):
        left_record = left.get(key)
        right_record = right.get(key)
        names = sorted(set(left_record or {}) | set(right_record or {}))
        fields += len(names)
        if left_record is None or right_record is None:
            mismatches.append(
                {
                    "key": key,
                    "field": "<record>",
                    "cobol": "<missing>" if left_record is None else "<present>",
                    "java": "<missing>" if right_record is None else "<present>",
                }
            )
            continue
        for name in names:
            cobol = left_record.get(name)
            java = right_record.get(name)
            if cobol != java:
                mismatches.append(
                    {"key": key, "field": name, "cobol": str(cobol), "java": str(java)}
                )
    return len(set(left) | set(right)), fields, mismatches


def compare(cobol: Path, java: Path, which: str) -> tuple[int, list[dict[str, str]]]:
    total_fields = 0
    total_mismatches: list[dict[str, str]] = []
    if which == "readacct":
        pairs = [("acctreport.txt", report_lines(cobol / "acctreport.txt"), report_lines(java / "acctreport.txt"))]
    else:
        pairs = [
            (dataset.name, dataset.read(cobol), dataset.read(java)) for dataset in datasets()
        ]
    for name, left, right in pairs:
        records, fields, mismatches = compare_records(left, right)
        for mismatch in mismatches:
            mismatch["dataset"] = name
        total_fields += fields
        total_mismatches.extend(mismatches)
        print(
            f"{name:16s} records cobol={len(left):6d} java={len(right):6d} "
            f"fields={fields:7d} mismatched={len(mismatches):6d}"
        )
    return total_fields, total_mismatches


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("cobol", type=Path)
    parser.add_argument("java", type=Path)
    parser.add_argument("--slice", default="posttran-intcalc", choices=["posttran-intcalc", "readacct"])
    parser.add_argument("--json", type=Path)
    arguments = parser.parse_args()

    fields, mismatches = compare(arguments.cobol, arguments.java, arguments.slice)
    for mismatch in mismatches[:MAX_REPORTED]:
        print(
            f"{mismatch['dataset']} {mismatch['key']} {mismatch['field']}: "
            f"{mismatch['cobol']} != {mismatch['java']}"
        )
    if len(mismatches) > MAX_REPORTED:
        print(f"... and {len(mismatches) - MAX_REPORTED} more")
    print(f"{fields - len(mismatches)}/{fields} fields at parity")

    if arguments.json:
        arguments.json.parent.mkdir(parents=True, exist_ok=True)
        arguments.json.write_text(
            json.dumps({"fields": fields, "mismatches": mismatches}, indent=2) + "\n",
            encoding="utf-8",
        )
    raise SystemExit(1 if mismatches else 0)


if __name__ == "__main__":
    main()
