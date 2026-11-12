"""COACTVWC: account view (CAVW) - account, cross-reference and customer lookups."""
import pytest

from harness.cics import commarea
from harness.records import ACCOUNT, CUSTOMER, XREF

CARD = "4111111111111111"


@pytest.fixture
def region(cics):
    cics.dataset("CXACAIX", XREF, [XREF.build(**{"XREF-CARD-NUM": CARD, "XREF-CUST-ID": 7,
                                                 "XREF-ACCT-ID": 1})], key=XREF.alt_key)
    cics.dataset("ACCTDAT", ACCOUNT, [ACCOUNT.build(**{
        "ACCT-ID": 1, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": 1234.56,
        "ACCT-CREDIT-LIMIT": 5000, "ACCT-CASH-CREDIT-LIMIT": 1000,
        "ACCT-OPEN-DATE": "2020-01-15", "ACCT-EXPIRAION-DATE": "2027-01-31",
        "ACCT-REISSUE-DATE": "2024-01-31", "ACCT-GROUP-ID": "GOLD"})])
    cics.dataset("CUSTDAT", CUSTOMER, [CUSTOMER.build(**{
        "CUST-ID": 7, "CUST-FIRST-NAME": "Ada", "CUST-LAST-NAME": "Lovelace",
        "CUST-ADDR-LINE-1": "1 Main St", "CUST-ADDR-LINE-3": "Austin",
        "CUST-ADDR-STATE-CD": "TX", "CUST-ADDR-ZIP": "78701", "CUST-SSN": 123456789,
        "CUST-FICO-CREDIT-SCORE": 780, "CUST-DOB-YYYY-MM-DD": "1990-12-10"})])
    return cics


def view(cics, acct=None, aid="ENTER", faults=None):
    fields = {} if acct is None else {"acctsid": acct}
    return cics.send("COACTVWC", "CACTVWA", aid, commarea(context=1), faults, **fields)


def test_first_entry_prompts_for_account(region):
    r = region.run("COACTVWC", commarea=commarea(context=0))
    assert r.screen.map == "CACTVWA" and r.return_transid == "CAVW"
    assert r.screen["ACCTSIDO"] == ""


def test_view_account_with_customer(region):
    s = view(region, "00000000001").screen
    assert s["ERRMSGO"] == ""
    assert (s["ACSTTUSO"], s["AADDGRPO"], s["ADTOPENO"]) == ("Y", "GOLD", "2020-01-15")
    assert s["ACSTNUMO"] == "000000007"
    assert (s["ACSFNAMO"], s["ACSLNAMO"], s["ACSCITYO"]) == ("Ada", "Lovelace", "Austin")
    assert s["ACSTSSNO"] == "123-45-6789"
    assert s["ACSTFCOO"] == "780"


@pytest.mark.parametrize("acct,faults,message", [
    (None, [], "No input received"),
    ("00000000000", [], "Account Filter must  be a non-zero 11 digit number"),
    ("ABC", [], "Account Filter must  be a non-zero 11 digit number"),
    ("00000000009", [], "Account:00000000009 not found in Cross ref file.  Resp:000000013"),
    ("00000000001", ["READ CXACAIX 17"], "File Error: READ     on CXACAIX   returned RESP 000000017"),
    ("00000000001", ["READ ACCTDAT 13"], "Account:00000000001 not found in Acct Master file.Resp:000000013"),
    ("00000000001", ["READ ACCTDAT 17"], "File Error: READ     on ACCTDAT   returned RESP 000000017"),
    ("00000000001", ["READ CUSTDAT 13"], "CustId:000000007 not found in customer master.Resp: 000000013"),
    ("00000000001", ["READ CUSTDAT 17"], "File Error: READ     on CUSTDAT   returned RESP 000000017"),
])
def test_view_errors(region, acct, faults, message):
    assert view(region, acct, faults=faults).screen["ERRMSGO"].startswith(message)


def test_keys(region):
    assert view(region, aid="PF3").xctl == "COMEN01C"
    r = region.send("COACTVWC", "CACTVWA", "PF3",
                    commarea(context=1, from_program="COCRDLIC", from_tranid="CCLI"))
    assert r.xctl == "COCRDLIC"
    assert view(region, aid="PF9").screen["ERRMSGO"] == "No input received"
