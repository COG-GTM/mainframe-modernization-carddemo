#!/usr/bin/env python3
"""
Parse the COBOL fixed-width trantype.txt file and generate SQL INSERT statements.

Record layout (from CVTRA03Y.cpy, RECLN = 60):
  TRAN-TYPE       PIC X(02)   offset 0,  length 2   -> type_code
  TRAN-TYPE-DESC  PIC X(50)   offset 2,  length 50  -> description
  FILLER          PIC X(08)   offset 52, length 8   (ignored)

Usage:
  python parse_trantype.py <path_to_trantype.txt>

Output:
  SQL INSERT statements to stdout.
"""

import sys


def parse_trantype(filepath: str) -> list[dict[str, str]]:
    """Parse fixed-width trantype.txt and return list of records."""
    records = []
    with open(filepath, "r") as f:
        for line in f:
            if len(line.rstrip("\n")) < 2:
                continue
            type_code = line[0:2].strip()
            description = line[2:52].strip()
            if type_code:
                records.append({"type_code": type_code, "description": description})
    return records


def generate_sql(records: list[dict[str, str]]) -> str:
    """Generate SQL INSERT statements from parsed records."""
    statements = []
    for rec in records:
        code = rec["type_code"].replace("'", "''")
        desc = rec["description"].replace("'", "''")
        stmt = (
            f"INSERT INTO transaction_types (type_code, description) "
            f"VALUES ('{code}', '{desc}');"
        )
        statements.append(stmt)
    return "\n".join(statements)


def main() -> None:
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <path_to_trantype.txt>", file=sys.stderr)
        sys.exit(1)

    filepath = sys.argv[1]
    records = parse_trantype(filepath)
    print(f"-- Parsed {len(records)} records from {filepath}")
    print(generate_sql(records))


if __name__ == "__main__":
    main()
