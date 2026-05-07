#!/usr/bin/env python3
"""
Parse COBOL fixed-width transaction category file (trancatg.txt) and generate
SQL INSERT statements for the transaction_categories table.

Record layout from CVTRA04Y.cpy (RECLN = 60):
  TRAN-TYPE-CD        PIC X(02)   offset 0,  length 2   -> type_code
  TRAN-CAT-CD         PIC 9(04)   offset 2,  length 4   -> category_code
  TRAN-CAT-TYPE-DESC  PIC X(50)   offset 6,  length 50  -> description
  FILLER              PIC X(04)   offset 56, length 4   (ignored)

Usage:
    python3 parse_trancatg.py <input_file> [output_file]

    If output_file is omitted, SQL is printed to stdout.
"""

import sys


def parse_record(line: str) -> dict | None:
    """Parse a single fixed-width record into its component fields."""
    if len(line.rstrip("\n\r")) < 6:
        return None

    type_code = line[0:2]
    category_code_str = line[2:6]
    description = line[6:56].rstrip()

    try:
        category_code = int(category_code_str)
    except ValueError:
        return None

    return {
        "type_code": type_code,
        "category_code": category_code,
        "description": description,
    }


def to_sql_insert(record: dict) -> str:
    """Convert a parsed record to a SQL INSERT statement."""
    desc = record["description"].replace("'", "''")
    return (
        f"INSERT INTO transaction_categories (type_code, category_code, description) "
        f"VALUES ('{record['type_code']}', {record['category_code']}, '{desc}');"
    )


def main():
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <input_file> [output_file]", file=sys.stderr)
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else None

    records = []
    with open(input_file, "r") as f:
        for line in f:
            record = parse_record(line)
            if record:
                records.append(record)

    sql_lines = [to_sql_insert(r) for r in records]
    output = "\n".join(sql_lines) + "\n"

    if output_file:
        with open(output_file, "w") as f:
            f.write(output)
        print(f"Generated {len(records)} INSERT statements -> {output_file}")
    else:
        print(output, end="")
        print(f"\n-- {len(records)} records parsed", file=sys.stderr)


if __name__ == "__main__":
    main()
