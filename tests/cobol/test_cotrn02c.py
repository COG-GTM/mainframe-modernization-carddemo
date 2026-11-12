"""COTRN02C: add a transaction online (CT02)."""
import pytest

from harness.cics import commarea
from harness.records import TRAN, XREF

CARD = "4111111111111111"
GOOD = dict(ttypcd="01", tcatcd="0005", trnsrc="POS TERM", tdesc="Groceries",
            trnamt="+00000042.50", torigdt="2024-03-01", tprocdt="2024-03-02",
            mid="123456789", mname="Shop", mcity="Austin", mzip="78701")


def ca(context=1, selected="", from_program="COMEN01C"):
    return commarea(context=context, from_program=from_program) + \
        " " * 32 + "00000000" + "N" + " " + selected.ljust(16)


@pytest.fixture
def region(cics):
    xref = [XREF.build(**{"XREF-CARD-NUM": CARD, "XREF-CUST-ID": 7, "XREF-ACCT-ID": 1})]
    cics.dataset("CCXREF", XREF, xref)
    cics.dataset("CXACAIX", XREF, xref, key=XREF.alt_key)
    cics.dataset("TRANSACT", TRAN, [TRAN.build(**{
        "TRAN-ID": f"{7:016d}", "TRAN-TYPE-CD": "03", "TRAN-CAT-CD": 2, "TRAN-SOURCE": "WEB",
        "TRAN-DESC": "Refund", "TRAN-AMT": -9.99, "TRAN-MERCHANT-ID": 42,
        "TRAN-MERCHANT-NAME": "Store", "TRAN-MERCHANT-CITY": "Dallas",
        "TRAN-MERCHANT-ZIP": "75001", "TRAN-CARD-NUM": CARD,
        "TRAN-ORIG-TS": "2024-01-01", "TRAN-PROC-TS": "2024-01-02"})])
    return cics


def add(cics, aid="ENTER", faults=None, **fields):
    return cics.send("COTRN02C", "COTRN2A", aid, ca(), faults, **fields)


@pytest.mark.parametrize("key", [{"actidin": "00000000001"}, {"cardnin": CARD}])
def test_add_confirmed_transaction(region, key):
    r = add(region, confirm="Y", **key, **GOOD)
    assert r.message == "Transaction added successfully.  Your Tran ID is 0000000000000008."
    new = TRAN.parse(region.records("TRANSACT")[-1])
    assert new["TRAN-ID"] == "0000000000000008"
    assert new["TRAN-CARD-NUM"] == CARD
    assert new["TRAN-AMT"] == pytest.approx(42.5)
    assert (new["TRAN-TYPE-CD"], new["TRAN-CAT-CD"], new["TRAN-MERCHANT-ID"]) == ("01", 5, 123456789)
    assert new["TRAN-ORIG-TS"] == "2024-03-01"


def test_unconfirmed_asks_for_confirmation_and_resolves_keys(region):
    r = add(region, actidin="00000000001", **GOOD)
    assert r.message == "Confirm to add this transaction..."
    assert r.screen["CARDNINO"] == CARD and r.screen["ACTIDINO"] == "00000000001"
    assert r.screen["TRNAMTO"] == "+00000042.50"
    assert len(region.records("TRANSACT")) == 1


def test_first_entry_with_preselected_card(region):
    r = region.run("COTRN02C", commarea=ca(context=0, selected=CARD))
    assert r.screen["ACTIDINO"] == "00000000001"
    assert r.message == "Type CD can NOT be empty..."


@pytest.mark.parametrize("key,faults,message", [
    ({}, [], "Account or Card Number must be entered..."),
    ({"actidin": "ABC"}, [], "Account ID must be Numeric..."),
    ({"cardnin": "41X1"}, [], "Card Number must be Numeric..."),
    ({"actidin": "00000000009"}, [], "Account ID NOT found..."),
    ({"cardnin": "4" * 16}, [], "Card Number NOT found..."),
    ({"actidin": "00000000001"}, ["READ CXACAIX 17"], "Unable to lookup Acct in XREF AIX file..."),
    ({"cardnin": CARD}, ["READ CCXREF 17"], "Unable to lookup Card # in XREF file..."),
])
def test_key_field_errors(region, key, faults, message):
    assert add(region, faults=faults, **key, **GOOD).message == message


@pytest.mark.parametrize("field,value,message", [
    ("ttypcd", "", "Type CD can NOT be empty..."),
    ("tcatcd", "", "Category CD can NOT be empty..."),
    ("trnsrc", "", "Source can NOT be empty..."),
    ("tdesc", "", "Description can NOT be empty..."),
    ("trnamt", "", "Amount can NOT be empty..."),
    ("torigdt", "", "Orig Date can NOT be empty..."),
    ("tprocdt", "", "Proc Date can NOT be empty..."),
    ("mid", "", "Merchant ID can NOT be empty..."),
    ("mname", "", "Merchant Name can NOT be empty..."),
    ("mcity", "", "Merchant City can NOT be empty..."),
    ("mzip", "", "Merchant Zip can NOT be empty..."),
    ("ttypcd", "A1", "Type CD must be Numeric..."),
    ("tcatcd", "00X5", "Category CD must be Numeric..."),
    ("trnamt", "42.50", "Amount should be in format -99999999.99"),
    ("trnamt", "+0000004X.50", "Amount should be in format -99999999.99"),
    ("torigdt", "2024/03/01", "Orig Date should be in format YYYY-MM-DD"),
    ("tprocdt", "03-01-2024", "Proc Date should be in format YYYY-MM-DD"),
    ("mid", "12AB", "Merchant ID must be Numeric..."),
])
def test_data_field_validation(region, field, value, message):
    fields = {**GOOD, field: value}
    if not value:
        del fields[field]
    assert add(region, confirm="Y", actidin="00000000001", **fields).message == message
    assert len(region.records("TRANSACT")) == 1


def test_date_rejected_by_ceedays(region):
    r = region.send("COTRN02C", "COTRN2A", "ENTER", ca(), env={"CEEDAYS_RESULT": "BAD-DATE-VALUE"},
                    confirm="Y", actidin="00000000001", **GOOD)
    assert r.message == "Orig Date - Not a valid date..."
    assert len(region.records("TRANSACT")) == 1


def test_invalid_confirm_value(region):
    assert add(region, confirm="Q", actidin="00000000001", **GOOD).message == \
        "Invalid value. Valid values are (Y/N)..."


@pytest.mark.parametrize("faults,message", [
    (["STARTBR TRANSACT 13"], "Transaction ID NOT found..."),
    (["STARTBR TRANSACT 17"], "Unable to lookup Transaction..."),
    (["READPREV TRANSACT 17"], "Unable to lookup Transaction..."),
    (["WRITE TRANSACT 14"], "Tran ID already exist..."),
    (["WRITE TRANSACT 17"], "Unable to Add Transaction..."),
])
def test_add_file_errors(region, faults, message):
    r = add(region, faults=faults, confirm="Y", actidin="00000000001", **GOOD)
    assert message in [s["ERRMSGO"] for s in r.screens]


def test_first_transaction_on_empty_file(region):
    region.dataset("TRANSACT", TRAN, [])
    add(region, confirm="Y", actidin="00000000001", **GOOD)
    assert TRAN.key(region.records("TRANSACT")[0]) == f"{1:016d}"


def test_pf5_copies_last_transaction(region):
    r = add(region, aid="PF5", actidin="00000000001")
    s = r.screen
    assert (s["TTYPCDO"], s["TCATCDO"], s["TRNSRCO"], s["TDESCO"]) == ("03", "0002", "WEB", "Refund")
    assert s["TRNAMTO"] == "-00000009.99"
    assert s["MNAMEO"] == "Store"


def test_keys(region):
    assert region.run("COTRN02C").xctl == "COSGN00C"
    assert add(region, aid="PF3").xctl == "COMEN01C"
    assert region.send("COTRN02C", "COTRN2A", "PF3", ca(from_program=" ")).xctl == "COMEN01C"
    assert add(region, aid="PF4", actidin="00000000001").screen["ACTIDINO"] == ""
    assert add(region, aid="PF9").message.startswith("Invalid key")
