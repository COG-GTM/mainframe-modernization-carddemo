"""Utilities for creating fixed-length test records from COBOL copybook layouts.

Each builder function produces a fixed-length ``str`` that mirrors the
byte-level layout of the corresponding COBOL record.  The helpers accept
keyword arguments for individual fields and pad / truncate to the exact
COBOL PIC length so that consumers can parse them with simple slicing.

Record lengths are documented in the original copybooks:
    - Account   (CVACT01Y) : 300 bytes
    - Card      (CVACT02Y) : 150 bytes
    - CardXref  (CVACT03Y) :  50 bytes
    - Customer  (CVCUS01Y) : 500 bytes
    - Transaction       (CVTRA05Y) : 350 bytes
    - DailyTransaction  (CVTRA06Y) : 350 bytes
    - TransactionType   (CVTRA03Y) :  60 bytes
    - TransactionCat    (CVTRA04Y) :  60 bytes
    - TranCatBalance    (CVTRA01Y) :  50 bytes
    - DisclosureGroup   (CVTRA02Y) :  50 bytes
    - UserSecurity      (CSUSR01Y) :  80 bytes
"""

from __future__ import annotations


# ---------------------------------------------------------------------------
# Low-level helpers
# ---------------------------------------------------------------------------

def _pad(value: str, length: int, fill: str = " ") -> str:
    """Left-justify *value* in a field of *length*, truncating if needed."""
    return value[:length].ljust(length, fill)


def _zfill(value: int | str, length: int) -> str:
    """Zero-fill a numeric value to *length* digits."""
    return str(value).zfill(length)[:length]


def _signed_decimal(value: float | int | str, int_digits: int, dec_digits: int) -> str:
    """Encode a signed decimal value as a fixed-width string.

    The COBOL PIC ``S9(int_digits)V{dec_digits}`` stores the sign and
    implied decimal in a fixed character representation.  We use a simple
    text encoding: ``+`` or ``-`` followed by zero-padded digits.

    Total width = 1 (sign) + int_digits + dec_digits.
    """
    if isinstance(value, str):
        value = float(value)
    sign = "+" if value >= 0 else "-"
    abs_val = abs(value)
    int_part = int(abs_val)
    dec_part = round((abs_val - int_part) * (10 ** dec_digits))
    return f"{sign}{str(int_part).zfill(int_digits)}{str(dec_part).zfill(dec_digits)}"


# ---------------------------------------------------------------------------
# Record builders
# ---------------------------------------------------------------------------

def build_account_record(
    *,
    acct_id: int | str = 0,
    active_status: str = "Y",
    curr_bal: float = 0.0,
    credit_limit: float = 0.0,
    cash_credit_limit: float = 0.0,
    open_date: str = "2020-01-01",
    expiration_date: str = "2030-12-31",
    reissue_date: str = "2025-06-15",
    curr_cyc_credit: float = 0.0,
    curr_cyc_debit: float = 0.0,
    addr_zip: str = "00000",
    group_id: str = "GROUP001",
) -> str:
    """Build a 300-byte account record (CVACT01Y)."""
    parts = [
        _zfill(acct_id, 11),                  # ACCT-ID             PIC 9(11)
        _pad(active_status, 1),                # ACCT-ACTIVE-STATUS  PIC X(01)
        _signed_decimal(curr_bal, 10, 2),      # ACCT-CURR-BAL       PIC S9(10)V99
        _signed_decimal(credit_limit, 10, 2),  # ACCT-CREDIT-LIMIT   PIC S9(10)V99
        _signed_decimal(cash_credit_limit, 10, 2),  # ACCT-CASH-CREDIT-LIMIT
        _pad(open_date, 10),                   # ACCT-OPEN-DATE      PIC X(10)
        _pad(expiration_date, 10),             # ACCT-EXPIRAION-DATE PIC X(10)
        _pad(reissue_date, 10),                # ACCT-REISSUE-DATE   PIC X(10)
        _signed_decimal(curr_cyc_credit, 10, 2),  # ACCT-CURR-CYC-CREDIT
        _signed_decimal(curr_cyc_debit, 10, 2),   # ACCT-CURR-CYC-DEBIT
        _pad(addr_zip, 10),                    # ACCT-ADDR-ZIP       PIC X(10)
        _pad(group_id, 10),                    # ACCT-GROUP-ID       PIC X(10)
    ]
    raw = "".join(parts)
    return raw.ljust(300)[:300]                # FILLER to 300


def build_card_record(
    *,
    card_num: str = "0000000000000000",
    acct_id: int | str = 0,
    cvv: int | str = 0,
    embossed_name: str = "",
    expiration_date: str = "2030-12-31",
    active_status: str = "Y",
) -> str:
    """Build a 150-byte card record (CVACT02Y)."""
    parts = [
        _pad(str(card_num), 16),      # CARD-NUM            PIC X(16)
        _zfill(acct_id, 11),          # CARD-ACCT-ID        PIC 9(11)
        _zfill(cvv, 3),               # CARD-CVV-CD         PIC 9(03)
        _pad(embossed_name, 50),      # CARD-EMBOSSED-NAME  PIC X(50)
        _pad(expiration_date, 10),    # CARD-EXPIRAION-DATE PIC X(10)
        _pad(active_status, 1),       # CARD-ACTIVE-STATUS  PIC X(01)
    ]
    raw = "".join(parts)
    return raw.ljust(150)[:150]


def build_card_xref_record(
    *,
    card_num: str = "0000000000000000",
    cust_id: int | str = 0,
    acct_id: int | str = 0,
) -> str:
    """Build a 50-byte card cross-reference record (CVACT03Y)."""
    parts = [
        _pad(str(card_num), 16),  # XREF-CARD-NUM  PIC X(16)
        _zfill(cust_id, 9),       # XREF-CUST-ID   PIC 9(09)
        _zfill(acct_id, 11),      # XREF-ACCT-ID   PIC 9(11)
    ]
    raw = "".join(parts)
    return raw.ljust(50)[:50]


def build_customer_record(
    *,
    cust_id: int | str = 0,
    first_name: str = "",
    middle_name: str = "",
    last_name: str = "",
    addr_line_1: str = "",
    addr_line_2: str = "",
    addr_line_3: str = "",
    addr_state_cd: str = "",
    addr_country_cd: str = "",
    addr_zip: str = "",
    phone_num_1: str = "",
    phone_num_2: str = "",
    ssn: int | str = 0,
    govt_issued_id: str = "",
    dob_yyyy_mm_dd: str = "",
    eft_account_id: str = "",
    pri_card_holder_ind: str = "Y",
    fico_credit_score: int | str = 0,
) -> str:
    """Build a 500-byte customer record (CVCUS01Y)."""
    parts = [
        _zfill(cust_id, 9),              # CUST-ID                PIC 9(09)
        _pad(first_name, 25),            # CUST-FIRST-NAME        PIC X(25)
        _pad(middle_name, 25),           # CUST-MIDDLE-NAME       PIC X(25)
        _pad(last_name, 25),             # CUST-LAST-NAME         PIC X(25)
        _pad(addr_line_1, 50),           # CUST-ADDR-LINE-1       PIC X(50)
        _pad(addr_line_2, 50),           # CUST-ADDR-LINE-2       PIC X(50)
        _pad(addr_line_3, 50),           # CUST-ADDR-LINE-3       PIC X(50)
        _pad(addr_state_cd, 2),          # CUST-ADDR-STATE-CD     PIC X(02)
        _pad(addr_country_cd, 3),        # CUST-ADDR-COUNTRY-CD   PIC X(03)
        _pad(addr_zip, 10),              # CUST-ADDR-ZIP          PIC X(10)
        _pad(phone_num_1, 15),           # CUST-PHONE-NUM-1       PIC X(15)
        _pad(phone_num_2, 15),           # CUST-PHONE-NUM-2       PIC X(15)
        _zfill(ssn, 9),                  # CUST-SSN               PIC 9(09)
        _pad(govt_issued_id, 20),        # CUST-GOVT-ISSUED-ID    PIC X(20)
        _pad(dob_yyyy_mm_dd, 10),        # CUST-DOB-YYYY-MM-DD   PIC X(10)
        _pad(eft_account_id, 10),        # CUST-EFT-ACCOUNT-ID   PIC X(10)
        _pad(pri_card_holder_ind, 1),    # CUST-PRI-CARD-HOLDER-IND PIC X(01)
        _zfill(fico_credit_score, 3),    # CUST-FICO-CREDIT-SCORE PIC 9(03)
    ]
    raw = "".join(parts)
    return raw.ljust(500)[:500]


def build_transaction_record(
    *,
    tran_id: str = "",
    type_cd: str = "SA",
    cat_cd: int | str = 5001,
    source: str = "ONLINE",
    desc: str = "",
    amt: float = 0.0,
    merchant_id: int | str = 0,
    merchant_name: str = "",
    merchant_city: str = "",
    merchant_zip: str = "",
    card_num: str = "0000000000000000",
    orig_ts: str = "",
    proc_ts: str = "",
) -> str:
    """Build a 350-byte transaction record (CVTRA05Y)."""
    parts = [
        _pad(tran_id, 16),              # TRAN-ID            PIC X(16)
        _pad(type_cd, 2),               # TRAN-TYPE-CD       PIC X(02)
        _zfill(cat_cd, 4),              # TRAN-CAT-CD        PIC 9(04)
        _pad(source, 10),               # TRAN-SOURCE        PIC X(10)
        _pad(desc, 100),                # TRAN-DESC          PIC X(100)
        _signed_decimal(amt, 9, 2),     # TRAN-AMT           PIC S9(09)V99
        _zfill(merchant_id, 9),         # TRAN-MERCHANT-ID   PIC 9(09)
        _pad(merchant_name, 50),        # TRAN-MERCHANT-NAME PIC X(50)
        _pad(merchant_city, 50),        # TRAN-MERCHANT-CITY PIC X(50)
        _pad(merchant_zip, 10),         # TRAN-MERCHANT-ZIP  PIC X(10)
        _pad(str(card_num), 16),        # TRAN-CARD-NUM      PIC X(16)
        _pad(orig_ts, 26),              # TRAN-ORIG-TS       PIC X(26)
        _pad(proc_ts, 26),              # TRAN-PROC-TS       PIC X(26)
    ]
    raw = "".join(parts)
    return raw.ljust(350)[:350]


def build_daily_transaction_record(
    *,
    tran_id: str = "",
    type_cd: str = "SA",
    cat_cd: int | str = 5001,
    source: str = "ONLINE",
    desc: str = "",
    amt: float = 0.0,
    merchant_id: int | str = 0,
    merchant_name: str = "",
    merchant_city: str = "",
    merchant_zip: str = "",
    card_num: str = "0000000000000000",
    orig_ts: str = "",
    proc_ts: str = "",
) -> str:
    """Build a 350-byte daily transaction record (CVTRA06Y)."""
    parts = [
        _pad(tran_id, 16),              # DALYTRAN-ID            PIC X(16)
        _pad(type_cd, 2),               # DALYTRAN-TYPE-CD       PIC X(02)
        _zfill(cat_cd, 4),              # DALYTRAN-CAT-CD        PIC 9(04)
        _pad(source, 10),               # DALYTRAN-SOURCE        PIC X(10)
        _pad(desc, 100),                # DALYTRAN-DESC          PIC X(100)
        _signed_decimal(amt, 9, 2),     # DALYTRAN-AMT           PIC S9(09)V99
        _zfill(merchant_id, 9),         # DALYTRAN-MERCHANT-ID   PIC 9(09)
        _pad(merchant_name, 50),        # DALYTRAN-MERCHANT-NAME PIC X(50)
        _pad(merchant_city, 50),        # DALYTRAN-MERCHANT-CITY PIC X(50)
        _pad(merchant_zip, 10),         # DALYTRAN-MERCHANT-ZIP  PIC X(10)
        _pad(str(card_num), 16),        # DALYTRAN-CARD-NUM      PIC X(16)
        _pad(orig_ts, 26),              # DALYTRAN-ORIG-TS       PIC X(26)
        _pad(proc_ts, 26),              # DALYTRAN-PROC-TS       PIC X(26)
    ]
    raw = "".join(parts)
    return raw.ljust(350)[:350]


def build_tran_type_record(
    *,
    tran_type: str = "SA",
    tran_type_desc: str = "",
) -> str:
    """Build a 60-byte transaction type record (CVTRA03Y)."""
    parts = [
        _pad(tran_type, 2),       # TRAN-TYPE       PIC X(02)
        _pad(tran_type_desc, 50), # TRAN-TYPE-DESC  PIC X(50)
    ]
    raw = "".join(parts)
    return raw.ljust(60)[:60]


def build_tran_cat_record(
    *,
    type_cd: str = "SA",
    cat_cd: int | str = 5001,
    cat_type_desc: str = "",
) -> str:
    """Build a 60-byte transaction category record (CVTRA04Y)."""
    parts = [
        _pad(type_cd, 2),         # TRAN-TYPE-CD       PIC X(02)
        _zfill(cat_cd, 4),        # TRAN-CAT-CD        PIC 9(04)
        _pad(cat_type_desc, 50),  # TRAN-CAT-TYPE-DESC PIC X(50)
    ]
    raw = "".join(parts)
    return raw.ljust(60)[:60]


def build_tran_cat_balance_record(
    *,
    acct_id: int | str = 0,
    type_cd: str = "SA",
    cat_cd: int | str = 5001,
    balance: float = 0.0,
) -> str:
    """Build a 50-byte transaction category balance record (CVTRA01Y)."""
    parts = [
        _zfill(acct_id, 11),           # TRANCAT-ACCT-ID  PIC 9(11)
        _pad(type_cd, 2),              # TRANCAT-TYPE-CD  PIC X(02)
        _zfill(cat_cd, 4),             # TRANCAT-CD       PIC 9(04)
        _signed_decimal(balance, 9, 2),  # TRAN-CAT-BAL   PIC S9(09)V99
    ]
    raw = "".join(parts)
    return raw.ljust(50)[:50]


def build_disclosure_group_record(
    *,
    acct_group_id: str = "",
    tran_type_cd: str = "SA",
    tran_cat_cd: int | str = 5001,
    int_rate: float = 0.0,
) -> str:
    """Build a 50-byte disclosure group record (CVTRA02Y)."""
    parts = [
        _pad(acct_group_id, 10),         # DIS-ACCT-GROUP-ID  PIC X(10)
        _pad(tran_type_cd, 2),           # DIS-TRAN-TYPE-CD   PIC X(02)
        _zfill(tran_cat_cd, 4),          # DIS-TRAN-CAT-CD    PIC 9(04)
        _signed_decimal(int_rate, 4, 2), # DIS-INT-RATE       PIC S9(04)V99
    ]
    raw = "".join(parts)
    return raw.ljust(50)[:50]


def build_user_security_record(
    *,
    user_id: str = "",
    first_name: str = "",
    last_name: str = "",
    password: str = "",
    user_type: str = "U",
) -> str:
    """Build an 80-byte user security record (CSUSR01Y)."""
    parts = [
        _pad(user_id, 8),      # SEC-USR-ID     PIC X(08)
        _pad(first_name, 20),  # SEC-USR-FNAME  PIC X(20)
        _pad(last_name, 20),   # SEC-USR-LNAME  PIC X(20)
        _pad(password, 8),     # SEC-USR-PWD    PIC X(08)
        _pad(user_type, 1),    # SEC-USR-TYPE   PIC X(01)
    ]
    raw = "".join(parts)
    return raw.ljust(80)[:80]
