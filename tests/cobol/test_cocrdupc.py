"""COCRDUPC: card update (CCUP) - fetch, validate, confirm with PF5, optimistic locking."""
import pytest

from harness.cics import commarea
from harness.records import CARD

NUM = "4111111111111111"


@pytest.fixture
def region(cics):
    cics.dataset("CARDDAT", CARD, [CARD.build(**{
        "CARD-NUM": NUM, "CARD-ACCT-ID": 1, "CARD-CVV-CD": 123,
        "CARD-EMBOSSED-NAME": "ADA LOVELACE", "CARD-EXPIRAION-DATE": "2027-05-31",
        "CARD-ACTIVE-STATUS": "Y"})])
    return cics


def from_list(cics, faults=None):
    return cics.run("COCRDUPC", faults=faults, commarea=commarea(
        context=0, from_program="COCRDLIC", from_tranid="CCLI",
        **{"CDEMO-ACCT-ID": 1, "CDEMO-CARD-NUM": NUM}))


def edit(cics, prev, aid="ENTER", faults=None, **changes):
    fields = dict(acctsid="00000000001", cardsid=NUM, crdname="ADA LOVELACE", crdstcd="Y",
                  expmon="05", expyear="2027", expday="31")
    fields.update(changes)
    return cics.send("COCRDUPC", "CCRDUPA", aid, prev.commarea, faults, **fields)


def card(cics):
    return CARD.parse(cics.records("CARDDAT")[0])


def test_entry_from_list_shows_card(region):
    r = from_list(region)
    s = r.screen
    assert (s["CRDNAMEO"], s["CRDSTCDO"], s["EXPMONO"], s["EXPYEARO"]) == \
        ("ADA LOVELACE", "Y", "05", "2027")
    assert s["INFOMSGO"].strip() == "Details of selected card shown above"
    assert r.return_transid == "CCUP"


def test_update_validate_then_commit(region):
    r1 = edit(region, from_list(region), crdname="ADA KING", crdstcd="N", expyear="2028")
    assert r1.screen["ERRMSGO"].strip() == ""
    r2 = edit(region, r1, "PF5", crdname="ADA KING", crdstcd="N", expyear="2028")
    assert r2.screen["INFOMSGO"].strip() == "Changes committed to database"
    c = card(region)
    assert (c["CARD-EMBOSSED-NAME"], c["CARD-ACTIVE-STATUS"]) == ("ADA KING", "N")
    assert c["CARD-EXPIRAION-DATE"].startswith("2028-05")


def test_no_change_detected(region):
    r = edit(region, from_list(region))
    assert r.screen["ERRMSGO"].strip() == "No change detected with respect to values fetched."


@pytest.mark.parametrize("changes,message", [
    ({"crdname": ""}, "Card name not provided"),
    ({"crdname": "ADA 1"}, "Card name can only contain alphabets and spaces"),
    ({"crdstcd": "X"}, "Card Active Status must be Y or N"),
    ({"expmon": "13"}, "Card expiry month must be between 1 and 12"),
    ({"expyear": "1900"}, "Invalid card expiry year"),
])
def test_validation(region, changes, message):
    assert edit(region, from_list(region), **changes).screen["ERRMSGO"].strip() == message


@pytest.mark.parametrize("fault,message", [
    ("READ CARDDAT 17", "Could not lock record for update"),
    ("REWRITE CARDDAT 17", "Update of record failed"),
])
def test_commit_failures(region, fault, message):
    r1 = edit(region, from_list(region), crdname="ADA KING")
    r2 = edit(region, r1, "PF5", faults=[fault], crdname="ADA KING")
    assert r2.screen["ERRMSGO"].strip() == message
    assert card(region)["CARD-EMBOSSED-NAME"] == "ADA LOVELACE"


def test_concurrent_change_detected(region):
    r1 = edit(region, from_list(region), crdname="ADA KING")
    region.dataset("CARDDAT", CARD, [CARD.build(**{
        "CARD-NUM": NUM, "CARD-ACCT-ID": 1, "CARD-CVV-CD": 123, "CARD-EMBOSSED-NAME": "SOMEONE",
        "CARD-EXPIRAION-DATE": "2027-05-31", "CARD-ACTIVE-STATUS": "Y"})])
    r2 = edit(region, r1, "PF5", crdname="ADA KING")
    assert r2.screen["ERRMSGO"].strip() == "Record changed by some one else. Please review"
    assert card(region)["CARD-EMBOSSED-NAME"] == "SOMEONE"


def test_fresh_search_errors(region):
    r = region.send("COCRDUPC", "CCRDUPA", "ENTER", commarea(context=1), acctsid="00000000001",
                    cardsid="4" * 16)
    msgs = r.screen["ERRMSGO"] + r.screen["INFOMSGO"]
    assert msgs.strip()
    r = from_list(region, faults=["READ CARDDAT 17"])
    assert r.screen["ERRMSGO"].startswith("File Error")


def test_keys(region):
    assert from_list(region).return_transid == "CCUP"
    r = edit(region, from_list(region), "PF3")
    assert r.xctl == "COCRDLIC"
    r = edit(region, edit(region, from_list(region), crdname="ADA KING"), "PF12")
    assert r.screen["CRDNAMEO"] == "ADA LOVELACE"
