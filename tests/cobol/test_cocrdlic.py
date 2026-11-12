"""COCRDLIC: credit card list (CCLI) - paging, filters, row selection."""
import pytest

from harness.cics import commarea
from harness.records import CARD

CARDS = [CARD.build(**{"CARD-NUM": f"4{i:015d}", "CARD-ACCT-ID": 1 + i % 2,
                       "CARD-EMBOSSED-NAME": f"HOLDER {i}", "CARD-ACTIVE-STATUS": "Y",
                       "CARD-EXPIRAION-DATE": "2027-05-31"}) for i in range(1, 11)]


@pytest.fixture
def region(cics):
    cics.dataset("CARDDAT", CARD, CARDS)
    return cics


def ca(context=1, **kw):
    return commarea(user_type="A", context=context, **kw)


def rows(r):
    return [r.screen[f"CRDNUM{i}O"] for i in range(1, 8)]


def first(cics, **ca_kw):
    return cics.run("COCRDLIC", commarea=ca(context=0, **ca_kw))


def test_first_page_lists_seven_cards(region):
    r = first(region)
    assert rows(r) == [f"4{i:015d}" for i in range(1, 8)]
    assert r.screen["ACCTNO1O"] == "00000000002" and r.screen["CRDSTS1O"] == "Y"
    assert r.screen["PAGENOO"].strip() == "1"
    assert r.return_transid == "CCLI"


def test_page_forward_and_back(region):
    r = first(region)
    r2 = region.send("COCRDLIC", "CCRDLIA", "PF8", r.commarea)
    assert rows(r2)[:3] == [f"4{i:015d}" for i in (8, 9, 10)] and rows(r2)[3] == ""
    r3 = region.send("COCRDLIC", "CCRDLIA", "PF8", r2.commarea)
    assert r3.screen["INFOMSGO"].strip() or r3.screen["ERRMSGO"].strip()
    r4 = region.send("COCRDLIC", "CCRDLIA", "PF7", r2.commarea)
    assert rows(r4)[0] == f"4{1:015d}"
    r5 = region.send("COCRDLIC", "CCRDLIA", "PF7", r4.commarea)
    assert "NO PREVIOUS PAGES TO DISPLAY" in r5.screen["ERRMSGO"] + r5.screen["INFOMSGO"]


def test_account_filter(region):
    r = region.send("COCRDLIC", "CCRDLIA", "ENTER", first(region).commarea, acctsid="00000000001")
    assert [x for x in rows(r) if x] == [f"4{i:015d}" for i in (2, 4, 6, 8, 10)]


def test_card_filter(region):
    r = region.send("COCRDLIC", "CCRDLIA", "ENTER", first(region).commarea, cardsid=f"4{5:015d}")
    assert [x for x in rows(r) if x] == [f"4{5:015d}"]


def test_no_match(region):
    r = region.send("COCRDLIC", "CCRDLIA", "ENTER", first(region).commarea, cardsid="4" * 16)
    assert "NO RECORDS FOUND FOR THIS SEARCH CONDITION." in r.screen["ERRMSGO"]


@pytest.mark.parametrize("fields,message", [
    ({"acctsid": "ABC"}, "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER"),
    ({"cardsid": "41X1"}, "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER"),
    ({"crdsel1": "S", "crdsel2": "U"}, "PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE"),
    ({"crdsel1": "X"}, "INVALID ACTION CODE"),
])
def test_input_errors(region, fields, message):
    r = region.send("COCRDLIC", "CCRDLIA", "ENTER", first(region).commarea, **fields)
    assert r.screen["ERRMSGO"].strip() == message


@pytest.mark.parametrize("sel,target", [("S", "COCRDSLC"), ("U", "COCRDUPC")])
def test_select_row_transfers(region, sel, target):
    r = region.send("COCRDLIC", "CCRDLIA", "ENTER", first(region).commarea, crdsel3=sel)
    assert r.xctl == target
    c = r.common()
    assert str(c["CDEMO-CARD-NUM"]) == f"4{3:015d}" and c["CDEMO-ACCT-ID"] == 2
    assert c["CDEMO-FROM-PROGRAM"] == "COCRDLIC"


def test_read_errors(region):
    r = region.run("COCRDLIC", commarea=ca(context=0), faults=["STARTBR CARDDAT 17"])
    assert r.screen["ERRMSGO"].startswith("File Error:") or r.texts
    r = region.run("COCRDLIC", commarea=ca(context=0), faults=["READNEXT CARDDAT 17"])
    assert "File Error:" in r.screen["ERRMSGO"]


def test_pf3_and_no_commarea(region):
    assert region.send("COCRDLIC", "CCRDLIA", "PF3", first(region).commarea).xctl == "COMEN01C"
    assert region.run("COCRDLIC").screen.map == "CCRDLIA"
