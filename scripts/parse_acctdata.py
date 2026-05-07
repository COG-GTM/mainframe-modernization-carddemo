#!/usr/bin/env python3
"""
Parse COBOL fixed-width account data (acctdata.txt) and generate SQL INSERT statements.

Handles zoned decimal sign overpunch encoding for signed numeric fields:
  Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
  Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9

Record layout (CVACT01Y.cpy, RECLN 300):
  Offset  0, Length 11: ACCT-ID              PIC 9(11)
  Offset 11, Length  1: ACCT-ACTIVE-STATUS   PIC X(01)
  Offset 12, Length 12: ACCT-CURR-BAL        PIC S9(10)V99
  Offset 24, Length 12: ACCT-CREDIT-LIMIT    PIC S9(10)V99
  Offset 36, Length 12: ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
  Offset 48, Length 10: ACCT-OPEN-DATE       PIC X(10)
  Offset 58, Length 10: ACCT-EXPIRAION-DATE  PIC X(10)
  Offset 68, Length 10: ACCT-REISSUE-DATE    PIC X(10)
  Offset 78, Length 12: ACCT-CURR-CYC-CREDIT PIC S9(10)V99
  Offset 90, Length 12: ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
  Offset102, Length 10: ACCT-ADDR-ZIP        PIC X(10)
  Offset112, Length 10: ACCT-GROUP-ID        PIC X(10)
  Offset122, Length178: FILLER               PIC X(178)
"""

import os
import sys

POSITIVE_OVERPUNCH = {
    '{': 0, 'A': 1, 'B': 2, 'C': 3, 'D': 4,
    'E': 5, 'F': 6, 'G': 7, 'H': 8, 'I': 9,
}
NEGATIVE_OVERPUNCH = {
    '}': 0, 'J': 1, 'K': 2, 'L': 3, 'M': 4,
    'N': 5, 'O': 6, 'P': 7, 'Q': 8, 'R': 9,
}


def decode_zoned_decimal(raw: str, decimal_places: int = 2) -> str:
    """Decode a COBOL zoned decimal field with sign overpunch on the last character."""
    if not raw:
        return '0.00'

    last_char = raw[-1]
    digits = raw[:-1]

    if last_char in POSITIVE_OVERPUNCH:
        sign = ''
        last_digit = str(POSITIVE_OVERPUNCH[last_char])
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = '-'
        last_digit = str(NEGATIVE_OVERPUNCH[last_char])
    elif last_char.isdigit():
        sign = ''
        last_digit = last_char
    else:
        raise ValueError(f"Unknown overpunch character: '{last_char}' in '{raw}'")

    full_digits = digits + last_digit
    integer_part = full_digits[:-decimal_places] if decimal_places > 0 else full_digits
    fractional_part = full_digits[-decimal_places:] if decimal_places > 0 else ''

    integer_val = integer_part.lstrip('0') or '0'

    if fractional_part:
        return f"{sign}{integer_val}.{fractional_part}"
    else:
        return f"{sign}{integer_val}"


def parse_record(line: str) -> dict:
    """Parse a single fixed-width account record."""
    acct_id = int(line[0:11])
    active_status = line[11:12]
    curr_bal = decode_zoned_decimal(line[12:24])
    credit_limit = decode_zoned_decimal(line[24:36])
    cash_credit_limit = decode_zoned_decimal(line[36:48])
    open_date = line[48:58].strip()
    expiration_date = line[58:68].strip()
    reissue_date = line[68:78].strip()
    curr_cyc_credit = decode_zoned_decimal(line[78:90])
    curr_cyc_debit = decode_zoned_decimal(line[90:102])
    addr_zip = line[102:112].strip()
    group_id = line[112:122].strip()

    return {
        'acct_id': acct_id,
        'active_status': active_status,
        'curr_bal': curr_bal,
        'credit_limit': credit_limit,
        'cash_credit_limit': cash_credit_limit,
        'open_date': open_date,
        'expiration_date': expiration_date,
        'reissue_date': reissue_date,
        'curr_cyc_credit': curr_cyc_credit,
        'curr_cyc_debit': curr_cyc_debit,
        'addr_zip': addr_zip,
        'group_id': group_id,
    }


def to_sql_insert(record: dict) -> str:
    """Generate a SQL INSERT statement from a parsed record."""
    group_id = f"'{record['group_id']}'" if record['group_id'] else 'NULL'
    return (
        f"INSERT INTO accounts (id, active_status, current_balance, credit_limit, "
        f"cash_credit_limit, open_date, expiration_date, reissue_date, "
        f"current_cycle_credit, current_cycle_debit, address_zip, group_id) VALUES ("
        f"{record['acct_id']}, "
        f"'{record['active_status']}', "
        f"{record['curr_bal']}, "
        f"{record['credit_limit']}, "
        f"{record['cash_credit_limit']}, "
        f"'{record['open_date']}', "
        f"'{record['expiration_date']}', "
        f"'{record['reissue_date']}', "
        f"{record['curr_cyc_credit']}, "
        f"{record['curr_cyc_debit']}, "
        f"'{record['addr_zip']}', "
        f"{group_id});"
    )


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(script_dir)
    input_file = os.path.join(repo_root, 'app', 'data', 'ASCII', 'acctdata.txt')
    output_file = os.path.join(
        repo_root, 'java-migration', 'src', 'main', 'resources', 'db', 'migration',
        'V1.1__seed_accounts.sql'
    )

    if not os.path.exists(input_file):
        print(f"Error: Input file not found: {input_file}", file=sys.stderr)
        sys.exit(1)

    records = []
    with open(input_file, 'r') as f:
        for line_num, line in enumerate(f, 1):
            line = line.rstrip('\n').rstrip('\r')
            if not line.strip():
                continue
            try:
                record = parse_record(line)
                records.append(record)
            except Exception as e:
                print(f"Error parsing line {line_num}: {e}", file=sys.stderr)
                sys.exit(1)

    print(f"Parsed {len(records)} records from {input_file}")

    with open(output_file, 'w') as f:
        f.write("-- Seed data: migrated from app/data/ASCII/acctdata.txt\n")
        f.write("-- Generated by scripts/parse_acctdata.py\n")
        f.write(f"-- Total records: {len(records)}\n\n")
        for record in records:
            f.write(to_sql_insert(record) + '\n')

    print(f"Generated {len(records)} INSERT statements in {output_file}")


if __name__ == '__main__':
    main()
