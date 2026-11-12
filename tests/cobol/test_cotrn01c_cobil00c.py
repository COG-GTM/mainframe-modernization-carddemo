"""COTRN01C (view a transaction, CT01) and COBIL00C (online bill payment, CB00)."""
import pytest

from harness.cics import commarea
from harness.records import ACCOUNT, TRAN, XREF

CARD = "4111111111111111"


def tran(i, amt=12.34, **kw):
    return TRAN.build(**{"TRAN-ID": f"{i:016d}", "TRAN-TYPE-CD": "01", "TRAN-CAT-CD": 5,
                         "TRAN-SOURCE": "POS TERM", "TRAN-DESC": f"Coffee {i}",
                         "TRAN-AMT": amt, "TRAN-MERCHANT-ID": 123456789,
                         "TRAN-MERCHANT-NAME": "Cafe", "TRAN-MERCHANT-CITY": "Austin",
                         "TRAN-MERCHANT-ZIP": "78701", "TRAN-CARD-NUM": CARD,
                         "TRAN-ORIG-TS": "2024-02-01 10:00:00.000000",
                         "TRAN-PROC-TS": "2024-02-02 10:00:00.000000", **kw})


def ca(context=1, selected="", from_program="COMEN01C"):
    return commarea(context=context, from_program=from_program) + \
        " " * 32 + "00000000" + "N" + " " + selected.ljust(16)


# ------------------------------------------------------------------ COTRN01C
@pytest.fixture
def trans(cics):
    cics.dataset("TRANSACT", TRAN, [tran(1), tran(2, amt=-5)])
    return cics


def test_view_selected_transaction(trans):
    r = trans.run("COTRN01C", commarea=ca(context=0, selected=f"{1:016d}"))
    s = r.screen
    assert s["TRNIDO"] == f"{1:016d}" and s["CARDNUMO"] == CARD
    assert (s["TTYPCDO"], s["TCATCDO"], s["TRNSRCO"]) == ("01", "0005", "POS TERM")
    assert s["TRNAMTO"] == "+00000012.34"
    assert s["TDESCO"] == "Coffee 1" and s["MNAMEO"] == "Cafe" and s["MZIPO"] == "78701"
    assert s["TORIGDTO"].startswith("2024-02-01")
    assert r.return_transid == "CT01"


def test_view_negative_amount_via_enter(trans):
    r = trans.send("COTRN01C", "COTRN1A", "ENTER", ca(), trnidin=f"{2:016d}")
    assert r.screen["TRNAMTO"] == "-00000005.00"


@pytest.mark.parametrize("fields,faults,message", [
    ({}, [], "Tran ID can NOT be empty..."),
    ({"trnidin": "9" * 16}, [], "Transaction ID NOT found..."),
    ({"trnidin": "1".rjust(16, "0")}, ["READ TRANSACT 17"], "Unable to lookup Transaction..."),
])
def test_view_errors(trans, fields, faults, message):
    assert trans.send("COTRN01C", "COTRN1A", "ENTER", ca(), faults, **fields).message == message


def test_view_keys(trans):
    assert trans.run("COTRN01C").xctl == "COSGN00C"
    assert trans.send("COTRN01C", "COTRN1A", "PF3", ca(from_program="COTRN00C")).xctl == "COTRN00C"
    assert trans.send("COTRN01C", "COTRN1A", "PF3", ca(from_program=" ")).xctl == "COMEN01C"
    assert trans.send("COTRN01C", "COTRN1A", "PF5", ca()).xctl == "COTRN00C"
    assert trans.send("COTRN01C", "COTRN1A", "PF4", ca(), trnidin="1").screen["TRNIDINO"] == ""
    assert trans.send("COTRN01C", "COTRN1A", "PF9", ca()).message.startswith("Invalid key")


# ------------------------------------------------------------------ COBIL00C
@pytest.fixture
def bank(cics):
    cics.dataset("ACCTDAT", ACCOUNT, [
        ACCOUNT.build(**{"ACCT-ID": 1, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": 250.75}),
        ACCOUNT.build(**{"ACCT-ID": 2, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": 0}),
    ])
    xrefs = [XREF.build(**{"XREF-CARD-NUM": CARD, "XREF-CUST-ID": 7, "XREF-ACCT-ID": 1})]
    cics.dataset("CXACAIX", XREF, xrefs, key=XREF.alt_key)
    cics.dataset("TRANSACT", TRAN, [tran(1), tran(41)])
    return cics


def pay(cics, acct="00000000001", confirm=None, faults=None):
    fields = {"actidin": acct}
    if confirm is not None:
        fields["confirm"] = confirm
    return cics.send("COBIL00C", "COBIL0A", "ENTER", ca(), faults, **fields)


def test_balance_shown_and_confirmation_requested(bank):
    r = pay(bank)
    assert r.screen["CURBALO"] == "+0000000250.75"
    assert r.message == "Confirm to make a bill payment..."
    assert len(bank.records("TRANSACT")) == 2


def test_preselected_account_on_first_entry(bank):
    r = bank.run("COBIL00C", commarea=ca(context=0, selected="00000000001"))
    assert r.screen["CURBALO"] == "+0000000250.75"


@pytest.mark.parametrize("confirm", ["Y", "y"])
def test_confirmed_payment_posts_transaction_and_zeroes_balance(bank, confirm):
    r = pay(bank, confirm=confirm)
    assert r.message == "Payment successful.  Your Transaction ID is 0000000000000042."
    new = TRAN.parse(bank.records("TRANSACT")[-1])
    assert new["TRAN-ID"] == "0000000000000042"
    assert (new["TRAN-TYPE-CD"], new["TRAN-CAT-CD"]) == ("02", 2)
    assert new["TRAN-DESC"] == "BILL PAYMENT - ONLINE"
    assert new["TRAN-AMT"] == pytest.approx(250.75)
    assert new["TRAN-CARD-NUM"] == CARD
    assert new["TRAN-ORIG-TS"] == "2024-03-15 10:11:12.000000"
    assert ACCOUNT.parse(bank.records("ACCTDAT")[0])["ACCT-CURR-BAL"] == 0


def test_first_ever_transaction_gets_id_1(bank):
    bank.dataset("TRANSACT", TRAN, [])
    pay(bank, confirm="Y")
    assert TRAN.key(bank.records("TRANSACT")[0]) == f"{1:016d}"


@pytest.mark.parametrize("acct,confirm,faults,message", [
    ("", None, [], "Acct ID can NOT be empty..."),
    ("00000000002", None, [], "You have nothing to pay..."),
    ("00000000009", None, [], "Account ID NOT found..."),
    ("00000000001", "X", [], "Invalid value. Valid values are (Y/N)..."),
    ("00000000001", None, ["READ ACCTDAT 17"], "Unable to lookup Account..."),
    ("00000000001", "Y", ["READ CXACAIX 13"], "Account ID NOT found..."),
    ("00000000001", "Y", ["READ CXACAIX 17"], "Unable to lookup XREF AIX file..."),
    ("00000000001", "Y", ["STARTBR TRANSACT 13"], "Transaction ID NOT found..."),
    ("00000000001", "Y", ["STARTBR TRANSACT 17"], "Unable to lookup Transaction..."),
    ("00000000001", "Y", ["READPREV TRANSACT 17"], "Unable to lookup Transaction..."),
    ("00000000001", "Y", ["WRITE TRANSACT 14"], "Tran ID already exist..."),
    ("00000000001", "Y", ["WRITE TRANSACT 17"], "Unable to Add Bill pay Transaction..."),
])
def test_payment_errors(bank, acct, confirm, faults, message):
    # Lookup errors after confirmation do not stop the payment flow (later SENDs overwrite
    # the message), so check every screen sent during the task.
    assert message in [s["ERRMSGO"] for s in pay(bank, acct, confirm, faults).screens]


@pytest.mark.parametrize("fault,message", [("REWRITE ACCTDAT 13", "Account ID NOT found..."),
                                           ("REWRITE ACCTDAT 17", "Unable to Update Account...")])
def test_rewrite_errors_after_successful_write(bank, fault, message):
    r = pay(bank, confirm="Y", faults=[fault])
    assert message in [s["ERRMSGO"] for s in r.screens]


def test_payment_declined_and_keys(bank):
    r = pay(bank, confirm="N")
    assert r.screen["ACTIDINO"] == "" and r.screen["CURBALO"] == ""
    assert bank.run("COBIL00C").xctl == "COSGN00C"
    assert bank.send("COBIL00C", "COBIL0A", "PF3", ca(from_program=" ")).xctl == "COMEN01C"
    assert bank.send("COBIL00C", "COBIL0A", "PF3", ca(from_program="COTRN00C")).xctl == "COTRN00C"
    assert bank.send("COBIL00C", "COBIL0A", "PF4", ca(), actidin="1").screen["ACTIDINO"] == ""
    assert bank.send("COBIL00C", "COBIL0A", "PF9", ca()).message.startswith("Invalid key")
