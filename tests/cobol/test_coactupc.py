"""COACTUPC: account + customer update (CAUP) - fetch, field edits, PF5 commit."""
import pytest

from harness.cics import commarea
from harness.records import ACCOUNT, CUSTOMER, XREF

CARD = "4111111111111111"


def acct(**kw):
    return ACCOUNT.build(**{"ACCT-ID": 1, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": 100,
                            "ACCT-CREDIT-LIMIT": 5000, "ACCT-CASH-CREDIT-LIMIT": 1000,
                            "ACCT-OPEN-DATE": "2020-01-15", "ACCT-EXPIRAION-DATE": "2027-01-31",
                            "ACCT-REISSUE-DATE": "2024-01-31", "ACCT-GROUP-ID": "GOLD", **kw})


@pytest.fixture
def region(cics):
    cics.dataset("CXACAIX", XREF, [XREF.build(**{"XREF-CARD-NUM": CARD, "XREF-CUST-ID": 7,
                                                 "XREF-ACCT-ID": 1})], key=XREF.alt_key)
    cics.dataset("ACCTDAT", ACCOUNT, [acct()])
    cics.dataset("CUSTDAT", CUSTOMER, [CUSTOMER.build(**{
        "CUST-ID": 7, "CUST-FIRST-NAME": "Ada", "CUST-LAST-NAME": "Lovelace",
        "CUST-ADDR-LINE-1": "1 Main St", "CUST-ADDR-LINE-3": "Austin",
        "CUST-ADDR-STATE-CD": "TX", "CUST-ADDR-COUNTRY-CD": "USA", "CUST-ADDR-ZIP": "78701",
        "CUST-PHONE-NUM-1": "(512)555-0100", "CUST-PHONE-NUM-2": "(512)555-0101",
        "CUST-SSN": 123456789, "CUST-GOVT-ISSUED-ID": "DL123", "CUST-EFT-ACCOUNT-ID": "0000000001",
        "CUST-PRI-CARD-HOLDER-IND": "Y", "CUST-FICO-CREDIT-SCORE": 780,
        "CUST-DOB-YYYY-MM-DD": "1990-12-10"})])
    return cics


def fetch(cics, acct_id="00000000001", faults=None):
    first = cics.run("COACTUPC", commarea=commarea(context=0))
    return cics.send("COACTUPC", "CACTUPA", "ENTER", first.commarea, faults, acctsid=acct_id)


def screen_fields(r):
    """Echo every unprotected field back as the user would see it."""
    names = [n[:-1] for n in r.screen.fields if n.endswith("O")]
    skip = {"TRNNAME", "TITLE01", "TITLE02", "CURDATE", "CURTIME", "PGMNAME",
            "INFOMSG", "ERRMSG", "FKEYS", "FKEY05", "FKEY12"}
    return {n.lower(): r.screen[n + "O"] for n in names if n not in skip}


def edit(cics, prev, aid="ENTER", faults=None, **changes):
    fields = {**screen_fields(prev), **changes}
    return cics.send("COACTUPC", "CACTUPA", aid, prev.commarea, faults, **fields)


def test_entry_prompts_for_account(region):
    r = region.run("COACTUPC", commarea=commarea(context=0))
    assert r.screen["INFOMSGO"].strip() == "Enter or update id of account to update"
    assert r.return_transid == "CAUP"


def test_fetch_shows_account_and_customer(region):
    r = fetch(region)
    s = r.screen
    assert s["INFOMSGO"].strip() == "Update account details presented above."
    assert (s["ACSTTUSO"], s["OPNYEARO"], s["OPNMONO"], s["OPNDAYO"]) == ("Y", "2020", "01", "15")
    assert (s["ACSFNAMO"], s["ACSLNAMO"], s["ACSSTTEO"]) == ("Ada", "Lovelace", "TX")
    assert (s["ACTSSN1O"], s["ACTSSN2O"], s["ACTSSN3O"]) == ("123", "45", "6789")


@pytest.mark.parametrize("acct_id,faults,message", [
    ("", [], "No input received"),
    ("00000000000", [], "Account Number if supplied must be a 11 digit Non-Zero Number"),
    ("00000000009", [], "Account:00000000009 not found in Cross ref file."),
    ("00000000001", ["READ ACCTDAT 13"], "Account:00000000001 not found in Acct Master file."),
    ("00000000001", ["READ CUSTDAT 13"], "CustId:000000007 not found in customer master."),
    ("00000000001", ["READ CXACAIX 17"], "File Error: READ"),
])
def test_fetch_errors(region, acct_id, faults, message):
    r = fetch(region, acct_id, faults)
    assert (r.screen["ERRMSGO"] + r.screen["INFOMSGO"]).strip().startswith(message)


def test_change_validate_then_commit(region):
    r1 = edit(region, fetch(region), acsfnam="Augusta", acsttus="N")
    assert r1.screen["INFOMSGO"].strip() == "Changes validated.Press F5 to save"
    r2 = edit(region, r1, "PF5")
    assert r2.screen["INFOMSGO"].strip() == "Changes committed to database"
    assert CUSTOMER.parse(region.records("CUSTDAT")[0])["CUST-FIRST-NAME"] == "Augusta"
    assert ACCOUNT.parse(region.records("ACCTDAT")[0])["ACCT-ACTIVE-STATUS"] == "N"


@pytest.mark.parametrize("changes,message", [
    ({"acsttus": "X"}, "Account Status must be Y or N."),
    ({"acslnam": ""}, "Last Name must be supplied."),
    ({"acsfnam": "Ada1"}, "First Name can have alphabets only."),
    ({"opnmon": "13"}, "Open Date: Month must be a number between 1 and 12."),
    ({"acrdlim": "abc"}, "Credit Limit is not valid"),
    ({"acsstte": "ZZ"}, "State: is not a valid state code"),
    ({"actssn1": "000"}, "SSN: First 3 chars must not be zero."),
    ({"acstfco": "200"}, "FICO Score: should be between 300 and 850"),
    ({"acsph1a": "000"}, "Phone Number 1: Area code cannot be zero"),
])
def test_field_validation(region, changes, message):
    r = edit(region, fetch(region), **changes)
    assert r.screen["ERRMSGO"].strip() == message


@pytest.mark.parametrize("fault,message", [
    ("READ ACCTDAT 17", "Could not lock account record for update"),
    ("READ CUSTDAT 17", "Could not lock customer record for update"),
    ("REWRITE ACCTDAT 17", "Update of record failed"),
    ("REWRITE CUSTDAT 17", "Update of record failed"),
])
def test_commit_failures(region, fault, message):
    r1 = edit(region, fetch(region), acsfnam="Augusta", acsttus="N")
    r2 = edit(region, r1, "PF5", faults=[fault])
    assert r2.screen["ERRMSGO"].strip() == message
    # a failed customer rewrite rolls back the account rewrite that preceded it
    assert ACCOUNT.parse(region.records("ACCTDAT")[0])["ACCT-ACTIVE-STATUS"] == "Y"
    assert CUSTOMER.parse(region.records("CUSTDAT")[0])["CUST-FIRST-NAME"] == "Ada"


def test_concurrent_change_detected(region):
    r1 = edit(region, fetch(region), acsfnam="Augusta")
    region.dataset("ACCTDAT", ACCOUNT, [acct(**{"ACCT-CURR-BAL": 999})])
    assert edit(region, r1, "PF5").screen["ERRMSGO"].strip() == \
        "Record changed by some one else. Please review"


def test_keys(region):
    assert edit(region, fetch(region), "PF3").xctl == "COMEN01C"
    r = edit(region, edit(region, fetch(region), acsfnam="Augusta"), "PF12")
    assert r.screen["ACSFNAMO"] == "Ada"
