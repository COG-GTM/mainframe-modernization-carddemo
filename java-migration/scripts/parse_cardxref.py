#!/usr/bin/env python3
"""
Parse fixed-width Card XREF records from the COBOL seed data file
and generate SQL INSERT statements for Flyway migration.

Record layout (CVACT03Y.cpy, RECLN 50):
    XREF-CARD-NUM   PIC X(16)   offset 0,  length 16
    XREF-CUST-ID    PIC 9(09)   offset 16, length 9
    XREF-ACCT-ID    PIC 9(11)   offset 25, length 11
    FILLER           PIC X(14)   offset 36, length 14  (ignored)
"""

import os
import sys


def parse_line(line: str) -> tuple[str, int, int] | None:
    """Parse a single fixed-width record line and return (card_num, cust_id, acct_id)."""
    stripped = line.rstrip("\n\r")
    if not stripped:
        return None
    card_num = stripped[0:16]
    cust_id = int(stripped[16:25])
    acct_id = int(stripped[25:36])
    return card_num, cust_id, acct_id


def main() -> None:
    input_path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
        os.path.dirname(__file__), "..", "..", "app", "data", "ASCII", "cardxref.txt"
    )
    output_path = sys.argv[2] if len(sys.argv) > 2 else os.path.join(
        os.path.dirname(__file__), "..", "src", "main", "resources", "db", "migration",
        "V1.1__seed_card_xrefs.sql",
    )

    records: list[tuple[str, int, int]] = []
    with open(input_path, "r") as f:
        for line in f:
            parsed = parse_line(line)
            if parsed:
                records.append(parsed)

    with open(output_path, "w") as out:
        out.write("-- Flyway migration: seed card_xrefs from COBOL data file cardxref.txt\n")
        out.write(f"-- Generated from {os.path.basename(input_path)} ({len(records)} records)\n\n")
        for card_num, cust_id, acct_id in records:
            out.write(
                f"INSERT INTO card_xrefs (card_num, customer_id, account_id) "
                f"VALUES ('{card_num}', {cust_id}, {acct_id});\n"
            )

    print(f"Generated {len(records)} INSERT statements -> {output_path}")


if __name__ == "__main__":
    main()
