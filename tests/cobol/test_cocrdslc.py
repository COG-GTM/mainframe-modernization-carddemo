"""COCRDSLC: card detail view (CCDL)."""
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


def view(cics, acct=None, card=None, aid="ENTER", faults=None, **ca):
    fields = {k: v for k, v in (("acctsid", acct), ("cardsid", card)) if v is not None}
    return cics.send("COCRDSLC", "CCRDSLA", aid, commarea(context=1, **ca), faults, **fields)


def test_first_entry(region):
    r = region.run("COCRDSLC", commarea=commarea(context=0))
    assert r.screen.map == "CCRDSLA" and r.return_transid == "CCDL"


def test_view_card(region):
    s = view(region, "00000000001", NUM).screen
    assert s["ERRMSGO"] == ""
    assert s["CRDNAMEO"] == "ADA LOVELACE" and s["CRDSTCDO"] == "Y"
    assert (s["EXPMONO"], s["EXPYEARO"]) == ("05", "2027")


def test_entry_from_card_list_shows_selected_card(region):
    r = region.run("COCRDSLC", commarea=commarea(
        context=0, from_program="COCRDLIC", from_tranid="CCLI",
        **{"CDEMO-ACCT-ID": 1, "CDEMO-CARD-NUM": NUM}))
    assert r.screen["CRDNAMEO"] == "ADA LOVELACE"


@pytest.mark.parametrize("acct,card,faults,message", [
    (None, None, [], "No input received"),
    (None, NUM, [], "Account number not provided"),
    ("00000000001", None, [], "Card number not provided"),
    ("ABC", NUM, [], "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER"),
    ("00000000000", NUM, [], "Account number not provided"),
    ("00000000001", "41X1", [], "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER"),
    ("00000000001", "4" * 16, [], "Did not find cards for this search condition"),
    ("00000000001", NUM, ["READ CARDDAT 17"], "File Error: READ"),
])
def test_view_errors(region, acct, card, faults, message):
    assert view(region, acct, card, faults=faults).screen["ERRMSGO"].startswith(message)


def test_keys(region):
    assert view(region, aid="PF3").xctl == "COMEN01C"
    assert view(region, aid="PF3", from_program="COCRDLIC", from_tranid="CCLI").xctl == "COCRDLIC"
