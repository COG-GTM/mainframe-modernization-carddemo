"""COUSR00C (user list, CU00) and COTRN00C (transaction list, CT00): paging browses."""
import pytest

from harness.cics import commarea
from harness.records import TRAN, USRSEC

USERS = [USRSEC.build(**{"SEC-USR-ID": f"USER{i:04d}", "SEC-USR-FNAME": f"First{i}",
                         "SEC-USR-LNAME": f"Last{i}", "SEC-USR-PWD": "PW",
                         "SEC-USR-TYPE": "A" if i == 1 else "U"}) for i in range(1, 26)]
TRANS = [TRAN.build(**{"TRAN-ID": f"{i:016d}", "TRAN-TYPE-CD": "01", "TRAN-CAT-CD": 1,
                       "TRAN-SOURCE": "POS TERM", "TRAN-DESC": f"Purchase {i}",
                       "TRAN-AMT": i * 10 + 0.5, "TRAN-CARD-NUM": "4111111111111111",
                       "TRAN-ORIG-TS": f"2024-02-{i:02d} 10:00:00.000000"})
         for i in range(1, 26)]

# program, map, dataset, records, id width, first-row field, second id field name
SCREENS = {
    "COUSR00C": ("COUSR0A", "USRSEC", USRSEC, USERS, 8, "USRID{:02d}O", "usridin"),
    "COTRN00C": ("COTRN0A", "TRANSACT", TRAN, TRANS, 16, "TRNID{:02d}O", "trnidin"),
}


def ca(width, context=1):
    return commarea(context=context, from_program="COMEN01C") + \
        " " * (2 * width) + "00000000" + "N" + " " + " " * width


@pytest.fixture(params=list(SCREENS))
def screen(request, cics):
    program = request.param
    mapname, ds, layout, recs, width, idfield, infield = SCREENS[program]
    cics.dataset(ds, layout, recs)
    return cics, program, mapname, ds, width, idfield, infield


def ids(r, idfield):
    return [r.screen[idfield.format(i)] for i in range(1, 11)]


def test_first_entry_shows_first_page(screen):
    cics, program, mapname, ds, width, idfield, _ = screen
    r = cics.run(program, commarea=ca(width, context=0))
    rows = ids(r, idfield)
    assert rows[0].endswith("1") and rows[9].endswith("10")
    assert r.screen["PAGENUMO"].lstrip("0") == "1"
    assert r.common()["CDEMO-PGM-CONTEXT"] == 1


def test_paging_forward_and_back(screen):
    cics, program, mapname, ds, width, idfield, _ = screen
    r = cics.run(program, commarea=ca(width, context=0))
    pages = [ids(r, idfield)]
    for _ in range(2):
        r = cics.send(program, mapname, "PF8", r.commarea)
        pages.append(ids(r, idfield))
    assert pages[1][0].endswith("11") and pages[2][0].endswith("21")
    assert pages[2][5:] == [""] * 5
    assert r.screen["PAGENUMO"].lstrip("0") == "3"
    bottom = cics.send(program, mapname, "PF8", r.commarea)
    assert bottom.message == "You are already at the bottom of the page..."
    back = cics.send(program, mapname, "PF7", r.commarea)
    assert ids(back, idfield)[0].endswith("11")
    assert back.screen["PAGENUMO"].lstrip("0") == "2"
    top = cics.send(program, mapname, "PF7",
                    cics.send(program, mapname, "PF7", back.commarea).commarea)
    assert top.message == "You are already at the top of the page..."


def test_enter_with_start_key_positions_list(screen):
    cics, program, mapname, ds, width, idfield, infield = screen
    start = "USER0015" if width == 8 else f"{15:016d}"
    r = cics.send(program, mapname, "ENTER", ca(width), **{infield: start})
    assert ids(r, idfield)[0] == start
    assert ids(r, idfield)[-1].endswith("24")


def test_start_key_past_end(screen):
    cics, program, mapname, ds, width, idfield, infield = screen
    start = "ZZZZ" if width == 8 else "9" * 16
    r = cics.send(program, mapname, "ENTER", ca(width), **{infield: start})
    assert r.message in ("You are at the top of the page...",
                         "You have reached the bottom of the page...")


def test_browse_errors(screen):
    cics, program, mapname, ds, width, idfield, _ = screen
    what = "User" if width == 8 else "transaction"
    for cmd in ("STARTBR", "READNEXT"):
        r = cics.run(program, commarea=ca(width, context=0), faults=[f"{cmd} {ds} 17"])
        assert r.message == f"Unable to lookup {what}...", cmd
    r = cics.run(program, commarea=ca(width, context=0))
    r = cics.send(program, mapname, "PF8", r.commarea)
    back = cics.send(program, mapname, "PF7", r.commarea, faults=[f"READPREV {ds} 17"])
    assert back.message == f"Unable to lookup {what}..."
    back = cics.send(program, mapname, "PF7", r.commarea, faults=[f"READPREV {ds} 20"])
    assert back.message == "You have reached the top of the page..."


def test_keys(screen):
    cics, program, mapname, ds, width, idfield, _ = screen
    assert cics.run(program).xctl == "COSGN00C"
    back = cics.send(program, mapname, "PF3", ca(width))
    assert back.xctl == ("COADM01C" if width == 8 else "COMEN01C")
    assert cics.send(program, mapname, "PF2", ca(width)).message.startswith("Invalid key")


@pytest.mark.parametrize("flag,target", [("U", "COUSR02C"), ("u", "COUSR02C"),
                                         ("D", "COUSR03C"), ("d", "COUSR03C")])
def test_user_selection(cics, flag, target):
    cics.dataset("USRSEC", USRSEC, USERS)
    r = cics.send("COUSR00C", "COUSR0A", "ENTER", ca(8), sel0003=flag, usrid03="USER0003")
    assert r.xctl == target
    assert r.commarea[160 + 26:160 + 34] == b"USER0003"


@pytest.mark.parametrize("row", range(1, 11))
def test_every_selection_row(cics, row):
    cics.dataset("USRSEC", USRSEC, USERS)
    cics.dataset("TRANSACT", TRAN, TRANS)
    r = cics.send("COUSR00C", "COUSR0A", "ENTER", ca(8),
                  **{f"sel{row:04d}": "D", f"usrid{row:02d}": "USER0009"})
    assert r.xctl == "COUSR03C"
    r = cics.send("COTRN00C", "COTRN0A", "ENTER", ca(16),
                  **{f"sel{row:04d}": "S", f"trnid{row:02d}": f"{row:016d}"})
    assert r.xctl == "COTRN01C"
    assert r.commarea[160 + 42:160 + 58] == f"{row:016d}".encode()


def test_invalid_selection(cics):
    cics.dataset("USRSEC", USRSEC, USERS)
    cics.dataset("TRANSACT", TRAN, TRANS)
    r = cics.send("COUSR00C", "COUSR0A", "ENTER", ca(8), sel0001="X", usrid01="USER0001")
    assert r.message == "Invalid selection. Valid values are U and D"
    r = cics.send("COTRN00C", "COTRN0A", "ENTER", ca(16), sel0001="X",
                  trnid01="0000000000000001")
    assert r.message == "Invalid selection. Valid value is S"


def test_transaction_row_formatting_and_numeric_check(cics):
    cics.dataset("TRANSACT", TRAN, TRANS)
    r = cics.run("COTRN00C", commarea=ca(16, context=0))
    assert r.screen["TDATE01O"] == "02/01/24"
    assert r.screen["TDESC01O"] == "Purchase 1"
    assert r.screen["TAMT001O"].strip() in ("+00000010.50", "10.50", "+10.50")
    bad = cics.send("COTRN00C", "COTRN0A", "ENTER", ca(16), trnidin="ABC")
    assert bad.message == "Tran ID must be Numeric ..."
