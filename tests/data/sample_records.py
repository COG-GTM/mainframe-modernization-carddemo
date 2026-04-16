"""Pre-built sample records for every CardDemo entity type.

Each constant is a fixed-length string that matches the corresponding
COBOL copybook layout.  Tests can import these directly instead of
calling the builder functions for common happy-path scenarios.
"""

from tests.utils.record_builder import (
    build_account_record,
    build_card_record,
    build_card_xref_record,
    build_customer_record,
    build_daily_transaction_record,
    build_disclosure_group_record,
    build_tran_cat_balance_record,
    build_tran_cat_record,
    build_tran_type_record,
    build_transaction_record,
    build_user_security_record,
)

# ── Accounts ────────────────────────────────────────────────────────────────

VALID_ACCOUNT_ACTIVE = build_account_record(
    acct_id=12345678901,
    active_status="Y",
    curr_bal=5250.75,
    credit_limit=25000.00,
    cash_credit_limit=5000.00,
    open_date="2018-03-15",
    expiration_date="2028-03-15",
    reissue_date="2023-03-15",
    curr_cyc_credit=1200.00,
    curr_cyc_debit=450.50,
    addr_zip="30301-0001",
    group_id="PREMIUM01",
)

VALID_ACCOUNT_INACTIVE = build_account_record(
    acct_id=98765432100,
    active_status="N",
    curr_bal=0.0,
    credit_limit=10000.00,
    cash_credit_limit=2000.00,
    open_date="2015-06-01",
    expiration_date="2025-06-01",
    reissue_date="2020-06-01",
    addr_zip="10001",
    group_id="STANDARD1",
)

VALID_ACCOUNT_HIGH_BALANCE = build_account_record(
    acct_id=55555555555,
    active_status="Y",
    curr_bal=99999.99,
    credit_limit=100000.00,
    cash_credit_limit=50000.00,
    open_date="2010-01-01",
    expiration_date="2035-12-31",
    reissue_date="2025-01-01",
    curr_cyc_credit=25000.00,
    curr_cyc_debit=15000.00,
    addr_zip="90210",
    group_id="PLATINUM1",
)

# ── Cards ───────────────────────────────────────────────────────────────────

VALID_CARD_ACTIVE = build_card_record(
    card_num="4111111111111111",
    acct_id=12345678901,
    cvv=123,
    embossed_name="JOHN A SMITH",
    expiration_date="2028-12-31",
    active_status="Y",
)

VALID_CARD_INACTIVE = build_card_record(
    card_num="5500000000000004",
    acct_id=98765432100,
    cvv=456,
    embossed_name="JANE B DOE",
    expiration_date="2025-06-30",
    active_status="N",
)

VALID_CARD_EXPIRED = build_card_record(
    card_num="3400000000000009",
    acct_id=55555555555,
    cvv=789,
    embossed_name="EXPIRED CARD HOLDER",
    expiration_date="2020-01-01",
    active_status="N",
)

# ── Card Cross-References ───────────────────────────────────────────────────

VALID_XREF_1 = build_card_xref_record(
    card_num="4111111111111111",
    cust_id=100000001,
    acct_id=12345678901,
)

VALID_XREF_2 = build_card_xref_record(
    card_num="5500000000000004",
    cust_id=200000002,
    acct_id=98765432100,
)

# ── Customers ───────────────────────────────────────────────────────────────

VALID_CUSTOMER_PRIMARY = build_customer_record(
    cust_id=100000001,
    first_name="JOHN",
    middle_name="ANDREW",
    last_name="SMITH",
    addr_line_1="123 MAIN ST",
    addr_line_2="APT 4B",
    addr_state_cd="GA",
    addr_country_cd="USA",
    addr_zip="30301",
    phone_num_1="4045551234",
    ssn=123456789,
    govt_issued_id="GA-DL-12345678",
    dob_yyyy_mm_dd="1985-07-22",
    eft_account_id="1234567890",
    pri_card_holder_ind="Y",
    fico_credit_score=750,
)

VALID_CUSTOMER_SECONDARY = build_customer_record(
    cust_id=200000002,
    first_name="JANE",
    middle_name="BETH",
    last_name="DOE",
    addr_line_1="456 OAK AVE",
    addr_state_cd="NY",
    addr_country_cd="USA",
    addr_zip="10001",
    phone_num_1="2125559876",
    ssn=987654321,
    dob_yyyy_mm_dd="1990-11-03",
    pri_card_holder_ind="N",
    fico_credit_score=680,
)

# ── Transactions ────────────────────────────────────────────────────────────

VALID_TRANSACTION_SALE = build_transaction_record(
    tran_id="0000000000000001",
    type_cd="SA",
    cat_cd=5001,
    source="POS",
    desc="GROCERY PURCHASE",
    amt=127.43,
    merchant_id=100200300,
    merchant_name="WHOLE FOODS MARKET",
    merchant_city="ATLANTA",
    merchant_zip="30301",
    card_num="4111111111111111",
    orig_ts="2025-04-15-10.30.00.000000",
    proc_ts="2025-04-15-10.30.01.123456",
)

VALID_TRANSACTION_CASH_ADVANCE = build_transaction_record(
    tran_id="0000000000000002",
    type_cd="CA",
    cat_cd=6001,
    source="ATM",
    desc="CASH WITHDRAWAL",
    amt=500.00,
    merchant_id=400500600,
    merchant_name="WELLS FARGO ATM",
    merchant_city="NEW YORK",
    merchant_zip="10001",
    card_num="5500000000000004",
    orig_ts="2025-04-15-14.00.00.000000",
    proc_ts="2025-04-15-14.00.02.654321",
)

VALID_TRANSACTION_PAYMENT = build_transaction_record(
    tran_id="0000000000000003",
    type_cd="PR",
    cat_cd=7001,
    source="ONLINE",
    desc="PAYMENT RECEIVED",
    amt=1000.00,
    merchant_id=0,
    merchant_name="",
    merchant_city="",
    merchant_zip="",
    card_num="4111111111111111",
    orig_ts="2025-04-16-08.00.00.000000",
    proc_ts="2025-04-16-08.00.00.500000",
)

VALID_TRANSACTION_CREDIT = build_transaction_record(
    tran_id="0000000000000004",
    type_cd="CR",
    cat_cd=5001,
    source="POS",
    desc="REFUND - GROCERY",
    amt=25.00,
    merchant_id=100200300,
    merchant_name="WHOLE FOODS MARKET",
    merchant_city="ATLANTA",
    merchant_zip="30301",
    card_num="4111111111111111",
    orig_ts="2025-04-16-09.15.00.000000",
    proc_ts="2025-04-16-09.15.01.000000",
)

VALID_TRANSACTION_BALANCE_INQUIRY = build_transaction_record(
    tran_id="0000000000000005",
    type_cd="BA",
    cat_cd=8001,
    source="ATM",
    desc="BALANCE INQUIRY",
    amt=0.0,
    card_num="4111111111111111",
)

VALID_TRANSACTION_FEE = build_transaction_record(
    tran_id="0000000000000006",
    type_cd="FE",
    cat_cd=9001,
    source="ONLINE",
    desc="ANNUAL FEE",
    amt=99.00,
    card_num="4111111111111111",
)

# ── Daily Transactions ──────────────────────────────────────────────────────

VALID_DAILY_TRANSACTION = build_daily_transaction_record(
    tran_id="0000000000000001",
    type_cd="SA",
    cat_cd=5001,
    source="POS",
    desc="DAILY SALE ENTRY",
    amt=250.00,
    merchant_id=100200300,
    merchant_name="TARGET STORE",
    merchant_city="CHICAGO",
    merchant_zip="60601",
    card_num="4111111111111111",
    orig_ts="2025-04-15-11.00.00.000000",
    proc_ts="2025-04-15-23.59.59.000000",
)

# ── Transaction Types ───────────────────────────────────────────────────────

VALID_TRAN_TYPE_SALE = build_tran_type_record(
    tran_type="SA", tran_type_desc="Sale",
)
VALID_TRAN_TYPE_CASH_ADVANCE = build_tran_type_record(
    tran_type="CA", tran_type_desc="Cash Advance",
)
VALID_TRAN_TYPE_PAYMENT = build_tran_type_record(
    tran_type="PR", tran_type_desc="Payment/Return",
)
VALID_TRAN_TYPE_CREDIT = build_tran_type_record(
    tran_type="CR", tran_type_desc="Credit/Refund",
)
VALID_TRAN_TYPE_BALANCE = build_tran_type_record(
    tran_type="BA", tran_type_desc="Balance Inquiry",
)
VALID_TRAN_TYPE_FEE = build_tran_type_record(
    tran_type="FE", tran_type_desc="Fee Assessment",
)

# ── Transaction Categories ──────────────────────────────────────────────────

VALID_TRAN_CAT_GROCERY = build_tran_cat_record(
    type_cd="SA", cat_cd=5001, cat_type_desc="Grocery/Supermarket",
)
VALID_TRAN_CAT_GAS = build_tran_cat_record(
    type_cd="SA", cat_cd=5002, cat_type_desc="Gas/Fuel Station",
)
VALID_TRAN_CAT_ATM_WITHDRAWAL = build_tran_cat_record(
    type_cd="CA", cat_cd=6001, cat_type_desc="ATM Cash Withdrawal",
)

# ── Transaction Category Balances ───────────────────────────────────────────

VALID_TRAN_CAT_BALANCE = build_tran_cat_balance_record(
    acct_id=12345678901,
    type_cd="SA",
    cat_cd=5001,
    balance=3250.75,
)

# ── Disclosure Groups ──────────────────────────────────────────────────────

VALID_DISCLOSURE_GROUP = build_disclosure_group_record(
    acct_group_id="PREMIUM01",
    tran_type_cd="SA",
    tran_cat_cd=5001,
    int_rate=18.99,
)

# ── User Security ──────────────────────────────────────────────────────────

VALID_USER_ADMIN = build_user_security_record(
    user_id="ADMIN001",
    first_name="SYSTEM",
    last_name="ADMIN",
    password="PASSWORD",
    user_type="A",
)

VALID_USER_REGULAR = build_user_security_record(
    user_id="USER0001",
    first_name="REGULAR",
    last_name="USER",
    password="PASSWORD",
    user_type="U",
)
