"""COMEN01C (main menu, CM00) and COADM01C (admin menu, CA00)."""
import pytest

from harness.cics import commarea


def menu(cics, program, mapname, option=None, aid="ENTER", user_type="U"):
    fields = {} if option is None else {"option": option}
    return cics.send(program, mapname, aid, commarea(user_type=user_type, context=1), **fields)


@pytest.mark.parametrize("program,mapname,first,count", [
    ("COMEN01C", "COMEN1A", "01. Account View", 10),
    ("COADM01C", "COADM1A", "01. User List (Security)", 4),
])
def test_first_entry_lists_options(cics, program, mapname, first, count):
    r = cics.run(program, commarea=commarea(context=0))
    s = r.screen
    assert s.map == mapname
    assert s["OPTN001O"] == first
    assert sum(1 for i in range(1, 13) if s.fields.get(f"OPTN{i:03d}O")) == count
    assert r.common()["CDEMO-PGM-CONTEXT"] == 1


@pytest.mark.parametrize("program", ["COMEN01C", "COADM01C"])
def test_no_commarea_returns_to_signon(cics, program):
    assert cics.run(program).xctl == "COSGN00C"


@pytest.mark.parametrize("program,mapname", [("COMEN01C", "COMEN1A"),
                                             ("COADM01C", "COADM1A")])
def test_pf3_returns_to_signon(cics, program, mapname):
    assert menu(cics, program, mapname, aid="PF3").xctl == "COSGN00C"


@pytest.mark.parametrize("program,mapname", [("COMEN01C", "COMEN1A"),
                                             ("COADM01C", "COADM1A")])
def test_invalid_key(cics, program, mapname):
    assert menu(cics, program, mapname, aid="PF9").message.startswith("Invalid key pressed")


@pytest.mark.parametrize("option,target", [
    ("1", "COACTVWC"), ("2", "COACTUPC"), ("3", "COCRDLIC"), ("4", "COCRDSLC"),
    ("5", "COCRDUPC"), ("6", "COTRN00C"), ("7", "COTRN01C"), ("8", "COTRN02C"),
    ("9", "CORPT00C"), ("10", "COBIL00C"), (" 3", "COCRDLIC"),
])
def test_main_menu_option_transfers(cics, option, target):
    r = menu(cics, "COMEN01C", "COMEN1A", option)
    assert r.xctl == target
    assert r.common()["CDEMO-FROM-PROGRAM"] == "COMEN01C"
    assert r.common()["CDEMO-PGM-CONTEXT"] == 0


@pytest.mark.parametrize("option,target", [("1", "COUSR00C"), ("2", "COUSR01C"),
                                           ("3", "COUSR02C"), ("4", "COUSR03C")])
def test_admin_menu_option_transfers(cics, option, target):
    r = menu(cics, "COADM01C", "COADM1A", option, user_type="A")
    assert r.xctl == target
    assert r.common()["CDEMO-FROM-PROGRAM"] == "COADM01C"


@pytest.mark.parametrize("program,mapname,option", [
    ("COMEN01C", "COMEN1A", "99"), ("COMEN01C", "COMEN1A", "0"), ("COMEN01C", "COMEN1A", "X"),
    ("COADM01C", "COADM1A", "5"), ("COADM01C", "COADM1A", ""),
])
def test_invalid_option(cics, program, mapname, option):
    r = menu(cics, program, mapname, option)
    assert r.message == "Please enter a valid option number..."
    assert r.xctl is None
