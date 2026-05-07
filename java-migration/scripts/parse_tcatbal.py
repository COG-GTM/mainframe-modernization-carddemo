#!/usr/bin/env python3
"""
Seed data parser for Transaction Category Balance (TCATBAL) records.

Parses the fixed-length COBOL data file (app/data/ASCII/tcatbal.txt) based on
the record layout defined in copybook CVTRA01Y.cpy and generates SQL INSERT
statements for the Flyway seed migration.

Record layout (RECLN = 50 bytes):
    TRANCAT-ACCT-ID   PIC 9(11)      Offset 0,  Length 11  -> Long
    TRANCAT-TYPE-CD   PIC X(02)      Offset 11, Length 2   -> String
    TRANCAT-CD        PIC 9(04)      Offset 13, Length 4   -> Integer
    TRAN-CAT-BAL      PIC S9(09)V99  Offset 17, Length 11  -> BigDecimal(11,2)
    FILLER            PIC X(22)      Offset 28, Length 22  (ignored)

Zoned decimal sign overpunch (last character of TRAN-CAT-BAL):
    Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
    Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
"""

import os
import sys
from decimal import Decimal

# Zoned decimal sign overpunch mapping
POSITIVE_OVERPUNCH = {
    '{': '0', 'A': '1', 'B': '2', 'C': '3', 'D': '4',
    'E': '5', 'F': '6', 'G': '7', 'H': '8', 'I': '9',
}

NEGATIVE_OVERPUNCH = {
    '}': '0', 'J': '1', 'K': '2', 'L': '3', 'M': '4',
    'N': '5', 'O': '6', 'P': '7', 'Q': '8', 'R': '9',
}


def decode_signed_zoned_decimal(raw: str, decimal_places: int = 2) -> Decimal:
    """
    Decode a COBOL zoned decimal field with sign overpunch on the last character.

    PIC S9(09)V99 means 9 integer digits + 2 implied decimal digits = 11 characters.
    The last character encodes both the final digit and the sign.

    Args:
        raw: The raw string from the data file (11 characters for PIC S9(09)V99).
        decimal_places: Number of implied decimal places (V99 = 2).

    Returns:
        A Decimal value with the correct sign and scale.
    """
    if not raw:
        return Decimal('0.00')

    last_char = raw[-1]
    digits_before = raw[:-1]

    if last_char in POSITIVE_OVERPUNCH:
        sign = 1
        last_digit = POSITIVE_OVERPUNCH[last_char]
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = -1
        last_digit = NEGATIVE_OVERPUNCH[last_char]
    elif last_char.isdigit():
        sign = 1
        last_digit = last_char
    else:
        raise ValueError(f"Invalid overpunch character: '{last_char}' in field '{raw}'")

    all_digits = digits_before + last_digit

    if decimal_places > 0:
        integer_part = all_digits[:-decimal_places]
        decimal_part = all_digits[-decimal_places:]
        numeric_str = f"{integer_part}.{decimal_part}"
    else:
        numeric_str = all_digits

    return Decimal(numeric_str) * sign


def parse_record(line: str) -> dict:
    """
    Parse a single 50-byte TRAN-CAT-BAL-RECORD.

    Args:
        line: A single line from tcatbal.txt (at least 28 characters).

    Returns:
        Dictionary with parsed field values.
    """
    acct_id_raw = line[0:11]       # TRANCAT-ACCT-ID PIC 9(11)
    type_cd_raw = line[11:13]      # TRANCAT-TYPE-CD PIC X(02)
    cat_cd_raw = line[13:17]       # TRANCAT-CD PIC 9(04)
    balance_raw = line[17:28]      # TRAN-CAT-BAL PIC S9(09)V99

    return {
        'account_id': int(acct_id_raw),
        'type_code': type_cd_raw,
        'category_code': int(cat_cd_raw),
        'balance': decode_signed_zoned_decimal(balance_raw, decimal_places=2),
    }


def generate_sql(records: list) -> str:
    """
    Generate SQL INSERT statements for all parsed records.

    Args:
        records: List of parsed record dictionaries.

    Returns:
        A string containing all INSERT statements.
    """
    lines = [
        "-- Flyway seed migration: Transaction Category Balance data",
        "-- Source: app/data/ASCII/tcatbal.txt (50 records)",
        "-- Parsed from COBOL copybook CVTRA01Y (RECLN 50)",
        "",
        "INSERT INTO transaction_category_balances (account_id, type_code, category_code, balance) VALUES",
    ]

    value_lines = []
    for record in records:
        value_lines.append(
            f"    ({record['account_id']}, "
            f"'{record['type_code']}', "
            f"{record['category_code']}, "
            f"{record['balance']:.2f})"
        )

    lines.append(",\n".join(value_lines) + ";")
    return "\n".join(lines) + "\n"


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(os.path.dirname(script_dir))
    default_input = os.path.join(repo_root, 'app', 'data', 'ASCII', 'tcatbal.txt')

    input_file = sys.argv[1] if len(sys.argv) > 1 else default_input

    if not os.path.exists(input_file):
        print(f"Error: Input file not found: {input_file}", file=sys.stderr)
        sys.exit(1)

    records = []
    with open(input_file, 'r') as f:
        for line_num, line in enumerate(f, 1):
            line = line.rstrip('\n\r')
            if not line:
                continue
            if len(line) < 28:
                print(f"Warning: Line {line_num} too short ({len(line)} chars), skipping",
                      file=sys.stderr)
                continue
            try:
                record = parse_record(line)
                records.append(record)
            except (ValueError, IndexError) as e:
                print(f"Warning: Error parsing line {line_num}: {e}", file=sys.stderr)
                continue

    print(f"Parsed {len(records)} records from {input_file}", file=sys.stderr)

    sql = generate_sql(records)

    output_file = os.path.join(
        script_dir, '..', 'src', 'main', 'resources', 'db', 'migration',
        'V1.1__seed_transaction_category_balances.sql'
    )
    output_file = os.path.normpath(output_file)

    with open(output_file, 'w') as f:
        f.write(sql)

    print(f"Generated SQL written to {output_file}", file=sys.stderr)
    print(sql)


if __name__ == '__main__':
    main()
