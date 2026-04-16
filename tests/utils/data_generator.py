"""Random test-data generators for CardDemo record types.

Generators produce *valid* or *invalid* records suitable for property-
based and fuzz-style testing.  All randomness uses a seed-able
``random.Random`` so tests stay reproducible.
"""

from __future__ import annotations

import random
import string
from datetime import date, timedelta

from tests.utils.record_builder import (
    build_account_record,
    build_card_record,
    build_card_xref_record,
    build_customer_record,
    build_daily_transaction_record,
    build_transaction_record,
    build_user_security_record,
)

# ---------------------------------------------------------------------------
# Shared randomisation helpers
# ---------------------------------------------------------------------------

_DEFAULT_RNG = random.Random(42)

VALID_TRAN_TYPES = ["SA", "CA", "PR", "CR", "BA", "FE"]
VALID_USER_TYPES = ["A", "U"]
US_STATE_CODES = [
    "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
    "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
    "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
    "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
    "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY",
]


def _rand_digits(rng: random.Random, n: int) -> str:
    return "".join(rng.choices(string.digits, k=n))


def _rand_alpha(rng: random.Random, n: int) -> str:
    return "".join(rng.choices(string.ascii_uppercase, k=n))


def _rand_date(rng: random.Random, start_year: int = 2000, end_year: int = 2030) -> str:
    start = date(start_year, 1, 1)
    end = date(end_year, 12, 31)
    delta = (end - start).days
    d = start + timedelta(days=rng.randint(0, delta))
    return d.isoformat()


def _rand_timestamp(rng: random.Random) -> str:
    d = _rand_date(rng)
    h, m, s = rng.randint(0, 23), rng.randint(0, 59), rng.randint(0, 59)
    micro = rng.randint(0, 999999)
    return f"{d}-{h:02d}.{m:02d}.{s:02d}.{micro:06d}"


# ---------------------------------------------------------------------------
# Valid-record generators
# ---------------------------------------------------------------------------

def generate_valid_account(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_account_record(
        acct_id=int(_rand_digits(rng, 11)),
        active_status=rng.choice(["Y", "N"]),
        curr_bal=rng.uniform(-10000, 50000),
        credit_limit=rng.uniform(1000, 100000),
        cash_credit_limit=rng.uniform(500, 50000),
        open_date=_rand_date(rng, 2000, 2020),
        expiration_date=_rand_date(rng, 2025, 2035),
        reissue_date=_rand_date(rng, 2020, 2025),
        curr_cyc_credit=rng.uniform(0, 5000),
        curr_cyc_debit=rng.uniform(0, 5000),
        addr_zip=_rand_digits(rng, 5) + "-" + _rand_digits(rng, 4),
        group_id="GRP" + _rand_digits(rng, 5),
    )


def generate_valid_card(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_card_record(
        card_num=_rand_digits(rng, 16),
        acct_id=int(_rand_digits(rng, 11)),
        cvv=int(_rand_digits(rng, 3)),
        embossed_name=_rand_alpha(rng, 20),
        expiration_date=_rand_date(rng, 2025, 2035),
        active_status=rng.choice(["Y", "N"]),
    )


def generate_valid_customer(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_customer_record(
        cust_id=int(_rand_digits(rng, 9)),
        first_name=_rand_alpha(rng, 10),
        middle_name=_rand_alpha(rng, 5),
        last_name=_rand_alpha(rng, 12),
        addr_line_1=_rand_digits(rng, 4) + " " + _rand_alpha(rng, 15) + " ST",
        addr_line_2="APT " + _rand_digits(rng, 3),
        addr_state_cd=rng.choice(US_STATE_CODES),
        addr_country_cd="USA",
        addr_zip=_rand_digits(rng, 5),
        phone_num_1=_rand_digits(rng, 10),
        ssn=int(_rand_digits(rng, 9)),
        dob_yyyy_mm_dd=_rand_date(rng, 1950, 2000),
        eft_account_id=_rand_digits(rng, 10),
        pri_card_holder_ind=rng.choice(["Y", "N"]),
        fico_credit_score=rng.randint(300, 850),
    )


def generate_valid_transaction(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_transaction_record(
        tran_id=_rand_digits(rng, 16),
        type_cd=rng.choice(VALID_TRAN_TYPES),
        cat_cd=rng.randint(1000, 9999),
        source=rng.choice(["ONLINE", "POS", "ATM", "MOBILE", "PHONE"]),
        desc=_rand_alpha(rng, 40),
        amt=rng.uniform(1.00, 9999.99),
        merchant_id=int(_rand_digits(rng, 9)),
        merchant_name=_rand_alpha(rng, 20),
        merchant_city=_rand_alpha(rng, 15),
        merchant_zip=_rand_digits(rng, 5),
        card_num=_rand_digits(rng, 16),
        orig_ts=_rand_timestamp(rng),
        proc_ts=_rand_timestamp(rng),
    )


def generate_valid_daily_transaction(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_daily_transaction_record(
        tran_id=_rand_digits(rng, 16),
        type_cd=rng.choice(VALID_TRAN_TYPES),
        cat_cd=rng.randint(1000, 9999),
        source=rng.choice(["ONLINE", "POS", "ATM", "MOBILE", "PHONE"]),
        desc=_rand_alpha(rng, 40),
        amt=rng.uniform(1.00, 9999.99),
        merchant_id=int(_rand_digits(rng, 9)),
        merchant_name=_rand_alpha(rng, 20),
        merchant_city=_rand_alpha(rng, 15),
        merchant_zip=_rand_digits(rng, 5),
        card_num=_rand_digits(rng, 16),
        orig_ts=_rand_timestamp(rng),
        proc_ts=_rand_timestamp(rng),
    )


def generate_valid_user_security(rng: random.Random | None = None) -> str:
    rng = rng or _DEFAULT_RNG
    return build_user_security_record(
        user_id=("USER" + _rand_digits(rng, 4))[:8],
        first_name=_rand_alpha(rng, 10),
        last_name=_rand_alpha(rng, 12),
        password=_rand_alpha(rng, 8),
        user_type=rng.choice(VALID_USER_TYPES),
    )


# ---------------------------------------------------------------------------
# Invalid-record generators (for negative testing)
# ---------------------------------------------------------------------------

def generate_invalid_account_negative_credit_limit(rng: random.Random | None = None) -> str:
    """Account with a negative credit limit (business rule violation)."""
    rng = rng or _DEFAULT_RNG
    return build_account_record(
        acct_id=int(_rand_digits(rng, 11)),
        active_status="Y",
        credit_limit=-5000.0,
    )


def generate_invalid_card_expired(rng: random.Random | None = None) -> str:
    """Card with an expiration date in the past."""
    rng = rng or _DEFAULT_RNG
    return build_card_record(
        card_num=_rand_digits(rng, 16),
        acct_id=int(_rand_digits(rng, 11)),
        cvv=int(_rand_digits(rng, 3)),
        embossed_name=_rand_alpha(rng, 20),
        expiration_date="2019-01-01",
        active_status="Y",
    )


def generate_invalid_card_bad_status(rng: random.Random | None = None) -> str:
    """Card with an unrecognised active-status code."""
    rng = rng or _DEFAULT_RNG
    return build_card_record(
        card_num=_rand_digits(rng, 16),
        acct_id=int(_rand_digits(rng, 11)),
        cvv=int(_rand_digits(rng, 3)),
        embossed_name=_rand_alpha(rng, 20),
        active_status="X",
    )


def generate_invalid_customer_missing_name(rng: random.Random | None = None) -> str:
    """Customer with blank first and last names."""
    rng = rng or _DEFAULT_RNG
    return build_customer_record(
        cust_id=int(_rand_digits(rng, 9)),
        first_name="",
        last_name="",
    )


def generate_invalid_customer_bad_fico(rng: random.Random | None = None) -> str:
    """Customer with FICO score outside the valid 300-850 range."""
    rng = rng or _DEFAULT_RNG
    return build_customer_record(
        cust_id=int(_rand_digits(rng, 9)),
        first_name=_rand_alpha(rng, 10),
        last_name=_rand_alpha(rng, 12),
        fico_credit_score=999,
    )


def generate_invalid_transaction_zero_amount(rng: random.Random | None = None) -> str:
    """Transaction with a zero amount."""
    rng = rng or _DEFAULT_RNG
    return build_transaction_record(
        tran_id=_rand_digits(rng, 16),
        type_cd="SA",
        amt=0.0,
        card_num=_rand_digits(rng, 16),
    )


def generate_invalid_transaction_bad_type(rng: random.Random | None = None) -> str:
    """Transaction with an invalid type code."""
    rng = rng or _DEFAULT_RNG
    return build_transaction_record(
        tran_id=_rand_digits(rng, 16),
        type_cd="ZZ",
        amt=100.0,
        card_num=_rand_digits(rng, 16),
    )


def generate_invalid_user_bad_type(rng: random.Random | None = None) -> str:
    """User security record with an unrecognised user-type code."""
    rng = rng or _DEFAULT_RNG
    return build_user_security_record(
        user_id="BADUSER1",
        first_name="Bad",
        last_name="User",
        password="pass1234",
        user_type="Z",
    )
