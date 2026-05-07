#!/usr/bin/env python3
"""
Parse the COBOL fixed-width daily transaction file (dailytran.txt) and generate
Flyway SQL INSERT statements for the transactions table.

Record layout from CVTRA05Y.cpy (TRAN-RECORD, RECLN 350):
    TRAN-ID            PIC X(16)      offset 0    length 16
    TRAN-TYPE-CD       PIC X(02)      offset 16   length 2
    TRAN-CAT-CD        PIC 9(04)      offset 18   length 4
    TRAN-SOURCE        PIC X(10)      offset 22   length 10
    TRAN-DESC          PIC X(100)     offset 32   length 100
    TRAN-AMT           PIC S9(09)V99  offset 132  length 11
    TRAN-MERCHANT-ID   PIC 9(09)      offset 143  length 9
    TRAN-MERCHANT-NAME PIC X(50)      offset 152  length 50
    TRAN-MERCHANT-CITY PIC X(50)      offset 202  length 50
    TRAN-MERCHANT-ZIP  PIC X(10)      offset 252  length 10
    TRAN-CARD-NUM      PIC X(16)      offset 262  length 16
    TRAN-ORIG-TS       PIC X(26)      offset 278  length 26
    TRAN-PROC-TS       PIC X(26)      offset 304  length 26
    FILLER             PIC X(20)      offset 330  length 20  (ignored)
"""

import os
import sys
from decimal import Decimal

# Zoned decimal sign overpunch mapping for the last byte of PIC S9(09)V99.
# Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
# Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
POSITIVE_OVERPUNCH = {'{': '0', 'A': '1', 'B': '2', 'C': '3', 'D': '4',
                      'E': '5', 'F': '6', 'G': '7', 'H': '8', 'I': '9'}
NEGATIVE_OVERPUNCH = {'}': '0', 'J': '1', 'K': '2', 'L': '3', 'M': '4',
                      'N': '5', 'O': '6', 'P': '7', 'Q': '8', 'R': '9'}


def decode_signed_amount(raw: str) -> Decimal:
    """Decode a zoned decimal PIC S9(09)V99 value (11 bytes) to a Decimal.

    The last character carries the sign via overpunch encoding.
    The implied decimal point is before the last 2 digits (V99).
    """
    if len(raw) != 11:
        raise ValueError(f"Expected 11 characters for TRAN-AMT, got {len(raw)}: '{raw}'")

    last_char = raw[-1]
    digits_before = raw[:-1]  # first 10 characters are plain digits

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
        raise ValueError(f"Unknown overpunch character: '{last_char}' in amount '{raw}'")

    full_digits = digits_before + last_digit  # 11 digits total: 9 integer + 2 decimal
    integer_part = full_digits[:9]
    decimal_part = full_digits[9:]
    value = Decimal(f"{integer_part}.{decimal_part}")
    return value * sign


def sql_escape(s: str) -> str:
    """Escape single quotes for SQL by doubling them."""
    return s.replace("'", "''")


def parse_record(line: str) -> dict:
    """Parse a single fixed-width record line into a dict of fields."""
    # Strip trailing newline/carriage return but preserve internal spaces
    line = line.rstrip('\n').rstrip('\r')

    # Pad line to at least 330 chars (we ignore FILLER at 330-350)
    line = line.ljust(330)

    return {
        'tran_id':            line[0:16].strip(),
        'tran_type_cd':       line[16:18].strip(),
        'tran_cat_cd':        int(line[18:22]),
        'tran_source':        line[22:32].strip(),
        'tran_desc':          line[32:132].strip(),
        'tran_amt':           decode_signed_amount(line[132:143]),
        'tran_merchant_id':   int(line[143:152]),
        'tran_merchant_name': line[152:202].strip(),
        'tran_merchant_city': line[202:252].strip(),
        'tran_merchant_zip':  line[252:262].strip(),
        'tran_card_num':      line[262:278].strip(),
        'tran_orig_ts':       line[278:304].strip(),
        'tran_proc_ts':       line[304:330].strip(),
    }


def record_to_insert(rec: dict) -> str:
    """Convert a parsed record dict to a SQL INSERT statement."""
    proc_ts = f"'{sql_escape(rec['tran_proc_ts'])}'" if rec['tran_proc_ts'] else 'NULL'

    return (
        f"INSERT INTO transactions "
        f"(tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
        f"tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
        f"tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) VALUES ("
        f"'{sql_escape(rec['tran_id'])}', "
        f"'{sql_escape(rec['tran_type_cd'])}', "
        f"{rec['tran_cat_cd']}, "
        f"'{sql_escape(rec['tran_source'])}', "
        f"'{sql_escape(rec['tran_desc'])}', "
        f"{rec['tran_amt']}, "
        f"{rec['tran_merchant_id']}, "
        f"'{sql_escape(rec['tran_merchant_name'])}', "
        f"'{sql_escape(rec['tran_merchant_city'])}', "
        f"'{sql_escape(rec['tran_merchant_zip'])}', "
        f"'{sql_escape(rec['tran_card_num'])}', "
        f"'{sql_escape(rec['tran_orig_ts'])}', "
        f"{proc_ts});"
    )


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(os.path.dirname(script_dir))
    input_file = os.path.join(repo_root, 'app', 'data', 'ASCII', 'dailytran.txt')
    output_file = os.path.join(script_dir, '..', 'src', 'main', 'resources',
                               'db', 'migration', 'V1.1__seed_transactions.sql')
    output_file = os.path.normpath(output_file)

    if not os.path.exists(input_file):
        print(f"ERROR: Input file not found: {input_file}", file=sys.stderr)
        sys.exit(1)

    records = []
    with open(input_file, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, start=1):
            if not line.strip():
                continue
            try:
                rec = parse_record(line)
                records.append(rec)
            except Exception as e:
                print(f"ERROR on line {line_num}: {e}", file=sys.stderr)
                sys.exit(1)

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("-- Flyway migration: Seed transactions table with 300 records\n")
        f.write("-- Parsed from app/data/ASCII/dailytran.txt (COBOL fixed-width format)\n")
        f.write("-- Generated by scripts/parse_dailytran.py\n\n")
        for rec in records:
            f.write(record_to_insert(rec) + "\n")

    print(f"Generated {len(records)} INSERT statements -> {output_file}")

    # Print first record for verification
    if records:
        r = records[0]
        print(f"\nFirst record verification:")
        print(f"  TRAN-ID:            '{r['tran_id']}'")
        print(f"  TRAN-TYPE-CD:       '{r['tran_type_cd']}'")
        print(f"  TRAN-CAT-CD:        {r['tran_cat_cd']}")
        print(f"  TRAN-SOURCE:        '{r['tran_source']}'")
        print(f"  TRAN-DESC:          '{r['tran_desc']}'")
        print(f"  TRAN-AMT:           {r['tran_amt']}")
        print(f"  TRAN-MERCHANT-ID:   {r['tran_merchant_id']}")
        print(f"  TRAN-MERCHANT-NAME: '{r['tran_merchant_name']}'")
        print(f"  TRAN-MERCHANT-CITY: '{r['tran_merchant_city']}'")
        print(f"  TRAN-MERCHANT-ZIP:  '{r['tran_merchant_zip']}'")
        print(f"  TRAN-CARD-NUM:      '{r['tran_card_num']}'")
        print(f"  TRAN-ORIG-TS:       '{r['tran_orig_ts']}'")
        print(f"  TRAN-PROC-TS:       '{r['tran_proc_ts']}'")


if __name__ == '__main__':
    main()
