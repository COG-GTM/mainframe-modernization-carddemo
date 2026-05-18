#!/usr/bin/env python3
"""
MBA-1765: Data File Integrity Validation Script
Validates that all ASCII data files have been correctly updated
for the 17-digit card number migration.

Checks:
1. carddata.txt - Record length 150, card number at pos 0-16 (17 digits)
2. cardxref.txt - Card number at pos 0-16 (17 digits), followed by cust/acct
3. dailytran.txt - Record length 350, card number at pos 262-278 (17 digits)
4. Cross-referential integrity between card data and xref files
"""

import os
import sys

DATA_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "app", "data", "ASCII")

PASS_COUNT = 0
FAIL_COUNT = 0


def assert_check(condition, description):
    global PASS_COUNT, FAIL_COUNT
    if condition:
        PASS_COUNT += 1
        print(f"  PASS: {description}")
    else:
        FAIL_COUNT += 1
        print(f"  FAIL: {description}")


def validate_carddata():
    """Validate carddata.txt against CVACT02Y.cpy layout (150 bytes)."""
    print("\n--- Validating carddata.txt (CVACT02Y layout) ---")
    filepath = os.path.join(DATA_DIR, "carddata.txt")

    assert_check(os.path.exists(filepath), "carddata.txt exists")
    if not os.path.exists(filepath):
        return

    lines = [l.rstrip('\n') for l in open(filepath) if l.strip()]
    assert_check(len(lines) == 50, f"Expected 50 records, got {len(lines)}")

    all_correct_length = True
    all_17_digit_cards = True
    all_numeric_cards = True
    card_numbers = set()

    for i, line in enumerate(lines):
        if len(line) != 150:
            all_correct_length = False
            print(f"    Line {i+1}: expected 150 bytes, got {len(line)}")

        card_num = line[0:17]
        if len(card_num) != 17:
            all_17_digit_cards = False
        if not card_num.isdigit():
            all_numeric_cards = False
        card_numbers.add(card_num)

        # Validate account ID field (positions 17-27, 11 digits)
        acct_id = line[17:28]
        if not acct_id.isdigit():
            print(f"    Line {i+1}: account ID not numeric: '{acct_id}'")

    assert_check(all_correct_length, "All records are 150 bytes")
    assert_check(all_17_digit_cards, "All card numbers are 17 characters")
    assert_check(all_numeric_cards, "All card numbers are numeric")
    assert_check(len(card_numbers) == len(lines),
                 "All card numbers are unique")

    return card_numbers


def validate_cardxref():
    """Validate cardxref.txt against CVACT03Y.cpy layout."""
    print("\n--- Validating cardxref.txt (CVACT03Y layout) ---")
    filepath = os.path.join(DATA_DIR, "cardxref.txt")

    assert_check(os.path.exists(filepath), "cardxref.txt exists")
    if not os.path.exists(filepath):
        return set()

    lines = [l.rstrip('\n') for l in open(filepath) if l.strip()]
    assert_check(len(lines) == 50, f"Expected 50 records, got {len(lines)}")

    all_17_digit_cards = True
    all_numeric_cards = True
    all_valid_cust = True
    all_valid_acct = True
    xref_card_numbers = set()

    for i, line in enumerate(lines):
        # Card number: positions 0-16 (17 bytes)
        card_num = line[0:17]
        if len(card_num) != 17:
            all_17_digit_cards = False
        if not card_num.isdigit():
            all_numeric_cards = False
        xref_card_numbers.add(card_num)

        # Customer ID: positions 17-25 (9 bytes)
        cust_id = line[17:26]
        if not cust_id.isdigit():
            all_valid_cust = False
            print(f"    Line {i+1}: cust ID not numeric: '{cust_id}'")

        # Account ID: positions 26-36 (11 bytes)
        acct_id = line[26:37]
        if len(line) >= 37 and not acct_id.isdigit():
            all_valid_acct = False
            print(f"    Line {i+1}: acct ID not numeric: '{acct_id}'")

    assert_check(all_17_digit_cards, "All XREF card numbers are 17 characters")
    assert_check(all_numeric_cards, "All XREF card numbers are numeric")
    assert_check(all_valid_cust, "All customer IDs are valid")
    assert_check(all_valid_acct, "All account IDs are valid")
    assert_check(len(xref_card_numbers) == len(lines),
                 "All XREF card numbers are unique")

    return xref_card_numbers


def validate_dailytran():
    """Validate dailytran.txt against CVTRA05Y.cpy layout (350 bytes)."""
    print("\n--- Validating dailytran.txt (CVTRA05Y layout) ---")
    filepath = os.path.join(DATA_DIR, "dailytran.txt")

    assert_check(os.path.exists(filepath), "dailytran.txt exists")
    if not os.path.exists(filepath):
        return set()

    lines = [l.rstrip('\n') for l in open(filepath) if l.strip()]
    assert_check(len(lines) == 300, f"Expected 300 records, got {len(lines)}")

    # Card number offset: 16+2+4+10+100+11+9+50+50+10 = 262
    card_offset = 262
    all_correct_length = True
    all_17_digit_cards = True
    all_numeric_cards = True
    tran_card_numbers = set()

    for i, line in enumerate(lines):
        if len(line) != 350:
            all_correct_length = False
            if i < 5:
                print(f"    Line {i+1}: expected 350 bytes, got {len(line)}")

        card_num = line[card_offset:card_offset + 17]
        if len(card_num) != 17:
            all_17_digit_cards = False
        if not card_num.isdigit():
            all_numeric_cards = False
        tran_card_numbers.add(card_num)

        # Transaction ID (positions 0-15, 16 bytes) should still be 16 digits
        tran_id = line[0:16]
        if not tran_id.isdigit():
            if i < 3:
                print(f"    Line {i+1}: tran ID not numeric: '{tran_id}'")

    assert_check(all_correct_length, "All records are 350 bytes")
    assert_check(all_17_digit_cards,
                 "All transaction card numbers are 17 characters")
    assert_check(all_numeric_cards,
                 "All transaction card numbers are numeric")

    return tran_card_numbers


def validate_cross_references(card_nums, xref_nums, tran_nums):
    """Validate cross-referential integrity between files."""
    print("\n--- Cross-Reference Integrity ---")

    if card_nums and xref_nums:
        match = card_nums == xref_nums
        assert_check(match,
                     "Card numbers in carddata.txt match cardxref.txt")
        if not match:
            only_card = card_nums - xref_nums
            only_xref = xref_nums - card_nums
            if only_card:
                print(f"    In carddata but not xref: {len(only_card)}")
            if only_xref:
                print(f"    In xref but not carddata: {len(only_xref)}")

    if tran_nums and card_nums:
        tran_in_card = tran_nums.issubset(card_nums)
        assert_check(tran_in_card,
                     "All transaction card numbers exist in card master")
        if not tran_in_card:
            orphans = tran_nums - card_nums
            print(f"    Orphan transaction cards: {len(orphans)}")


def validate_field_lengths():
    """Validate that key fields use 17-digit card numbers in copybooks."""
    print("\n--- Copybook Field Length Validation ---")
    cpy_dir = os.path.join(os.path.dirname(os.path.dirname(
        os.path.abspath(__file__))), "app", "cpy")

    copybooks_to_check = {
        "CVACT02Y.cpy": ("CARD-NUM", "PIC X(17)"),
        "CVACT03Y.cpy": ("XREF-CARD-NUM", "PIC X(17)"),
        "CVTRA05Y.cpy": ("TRAN-CARD-NUM", "PIC X(17)"),
        "CVTRA06Y.cpy": ("DALYTRAN-CARD-NUM", "PIC X(17)"),
        "CVCRD01Y.cpy": ("CC-CARD-NUM", "PIC X(17)"),
        "COCOM01Y.cpy": ("CDEMO-CARD-NUM", "PIC 9(17)"),
    }

    for cpy_file, (field_name, expected_pic) in copybooks_to_check.items():
        filepath = os.path.join(cpy_dir, cpy_file)
        if os.path.exists(filepath):
            content = open(filepath).read()
            found = expected_pic in content
            assert_check(found,
                         f"{cpy_file}: {field_name} is {expected_pic}")
        else:
            assert_check(False, f"{cpy_file} not found")


def validate_error_messages():
    """Validate that error messages reference 17-digit (not 16-digit)."""
    print("\n--- Error Message Validation ---")
    cbl_dir = os.path.join(os.path.dirname(os.path.dirname(
        os.path.abspath(__file__))), "app", "cbl")

    programs_to_check = ["COCRDLIC.cbl", "COCRDSLC.cbl", "COCRDUPC.cbl"]

    for prog in programs_to_check:
        filepath = os.path.join(cbl_dir, prog)
        if os.path.exists(filepath):
            content = open(filepath).read()
            has_old_msg = "16 DIGIT NUMBER" in content.upper()
            has_new_msg = "17 DIGIT NUMBER" in content.upper()
            assert_check(not has_old_msg,
                         f"{prog}: No references to '16 digit' remain")
            assert_check(has_new_msg,
                         f"{prog}: Contains '17 digit' error message")
        else:
            assert_check(False, f"{prog} not found")


def main():
    print("=" * 60)
    print("MBA-1765: 17-DIGIT CARD NUMBER DATA INTEGRITY VALIDATION")
    print("=" * 60)

    card_nums = validate_carddata()
    xref_nums = validate_cardxref()
    tran_nums = validate_dailytran()
    validate_cross_references(card_nums or set(), xref_nums, tran_nums)
    validate_field_lengths()
    validate_error_messages()

    print("\n" + "=" * 60)
    print("VALIDATION SUMMARY")
    print("=" * 60)
    print(f"  CHECKS PASSED: {PASS_COUNT}")
    print(f"  CHECKS FAILED: {FAIL_COUNT}")
    print("=" * 60)

    if FAIL_COUNT > 0:
        print("OVERALL: *** FAILURES DETECTED ***")
        sys.exit(1)
    else:
        print("OVERALL: ALL CHECKS PASSED")
        sys.exit(0)


if __name__ == "__main__":
    main()
