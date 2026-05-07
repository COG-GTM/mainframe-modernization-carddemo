#!/usr/bin/env python3
"""
Parse fixed-width COBOL card data (CVACT02Y.cpy layout, RECLN 150) and generate
SQL INSERT statements for Flyway migration.

Record layout:
  CARD-NUM             PIC X(16)   offset 0   length 16
  CARD-ACCT-ID         PIC 9(11)   offset 16  length 11
  CARD-CVV-CD          PIC 9(03)   offset 27  length 3
  CARD-EMBOSSED-NAME   PIC X(50)   offset 30  length 50
  CARD-EXPIRAION-DATE  PIC X(10)   offset 80  length 10
  CARD-ACTIVE-STATUS   PIC X(01)   offset 90  length 1
  FILLER               PIC X(59)   offset 91  length 59
"""

import os
import sys


def parse_card_record(line: str) -> dict:
    """Parse a single fixed-width card record into a dictionary."""
    card_num = line[0:16].strip()
    card_acct_id = int(line[16:27])
    card_cvv_cd = int(line[27:30])
    card_embossed_name = line[30:80].strip()
    card_expiration_date = line[80:90].strip()
    card_active_status = line[90:91].strip()
    return {
        "card_num": card_num,
        "card_acct_id": card_acct_id,
        "card_cvv_cd": card_cvv_cd,
        "card_embossed_name": card_embossed_name,
        "card_expiration_date": card_expiration_date,
        "card_active_status": card_active_status,
    }


def escape_sql(value: str) -> str:
    """Escape single quotes for SQL string literals."""
    return value.replace("'", "''")


def generate_insert(record: dict) -> str:
    """Generate a SQL INSERT statement for a card record."""
    return (
        f"INSERT INTO cards (card_num, card_acct_id, card_cvv_cd, "
        f"card_embossed_name, card_expiration_date, card_active_status) VALUES ("
        f"'{escape_sql(record['card_num'])}', "
        f"{record['card_acct_id']}, "
        f"{record['card_cvv_cd']}, "
        f"'{escape_sql(record['card_embossed_name'])}', "
        f"'{record['card_expiration_date']}', "
        f"'{escape_sql(record['card_active_status'])}');"
    )


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(script_dir)
    input_path = os.path.join(repo_root, "app", "data", "ASCII", "carddata.txt")
    output_path = os.path.join(
        repo_root,
        "java-migration",
        "src",
        "main",
        "resources",
        "db",
        "migration",
        "V1.1__seed_cards.sql",
    )

    records = []
    with open(input_path, "r") as f:
        for line in f:
            if len(line.rstrip("\n\r")) < 91:
                continue
            record = parse_card_record(line)
            records.append(record)

    with open(output_path, "w") as f:
        f.write("-- Seed data: 50 card records parsed from app/data/ASCII/carddata.txt\n")
        f.write("-- Source copybook: CVACT02Y.cpy (CARD-RECORD, RECLN 150)\n\n")
        for record in records:
            f.write(generate_insert(record) + "\n")

    print(f"Generated {len(records)} INSERT statements -> {output_path}")


if __name__ == "__main__":
    main()
