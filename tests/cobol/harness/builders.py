"""Small record factories with sensible defaults for building test datasets."""
from __future__ import annotations

from .records import ACCOUNT, CUSTOMER, TCATBAL, TRAN, XREF, CARD


def account(acct_id, credit_limit="5000.00", cyc_credit="0", cyc_debit="0", curr_bal="0",
            expires="2030-12-31", group="DEFAULT", **extra) -> str:
    return ACCOUNT.build(**{
        "ACCT-ID": acct_id, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": curr_bal,
        "ACCT-CREDIT-LIMIT": credit_limit, "ACCT-CASH-CREDIT-LIMIT": "1000.00",
        "ACCT-OPEN-DATE": "2020-01-01", "ACCT-EXPIRAION-DATE": expires,
        "ACCT-REISSUE-DATE": "2025-01-01", "ACCT-CURR-CYC-CREDIT": cyc_credit,
        "ACCT-CURR-CYC-DEBIT": cyc_debit, "ACCT-GROUP-ID": group, **extra})


def xref(card_num, acct_id, cust_id=1) -> str:
    return XREF.build(**{"XREF-CARD-NUM": card_num, "XREF-CUST-ID": cust_id,
                         "XREF-ACCT-ID": acct_id})


def card(card_num, acct_id) -> str:
    return CARD.build(**{"CARD-NUM": card_num, "CARD-ACCT-ID": acct_id, "CARD-CVV-CD": 123,
                         "CARD-EMBOSSED-NAME": "TEST HOLDER", "CARD-EXPIRAION-DATE": "2030-12-31",
                         "CARD-ACTIVE-STATUS": "Y"})


def customer(cust_id, first="Ada", last="Lovelace") -> str:
    return CUSTOMER.build(**{"CUST-ID": cust_id, "CUST-FIRST-NAME": first,
                             "CUST-LAST-NAME": last, "CUST-ADDR-LINE-1": "1 Main St",
                             "CUST-ADDR-STATE-CD": "NY", "CUST-ADDR-COUNTRY-CD": "USA",
                             "CUST-ADDR-ZIP": "10001"})


def tcatbal(acct_id, type_cd, cat_cd, balance) -> str:
    return TCATBAL.build(**{"TRANCAT-ACCT-ID": acct_id, "TRANCAT-TYPE-CD": type_cd,
                            "TRANCAT-CD": cat_cd, "TRAN-CAT-BAL": balance})


def tran(tran_id, card_num, amount, type_cd="01", cat_cd=1,
         orig_ts="2024-03-15-10.00.00.000000", proc_ts="2024-03-15-10.00.00.000000",
         desc="Purchase", **extra) -> str:
    return TRAN.build(**{
        "TRAN-ID": tran_id, "TRAN-TYPE-CD": type_cd, "TRAN-CAT-CD": cat_cd,
        "TRAN-SOURCE": "POS TERM", "TRAN-DESC": desc, "TRAN-AMT": amount,
        "TRAN-MERCHANT-ID": 800000001, "TRAN-MERCHANT-NAME": "Test Merchant",
        "TRAN-MERCHANT-CITY": "Springfield", "TRAN-MERCHANT-ZIP": "12345",
        "TRAN-CARD-NUM": card_num, "TRAN-ORIG-TS": orig_ts, "TRAN-PROC-TS": proc_ts, **extra})
