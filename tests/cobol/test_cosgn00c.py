"""COSGN00C: sign-on screen (transaction CC00)."""
import pytest

from harness.cics import commarea
from harness.records import USRSEC


def user(uid, pwd, typ, first="Ann", last="Lee"):
    return USRSEC.build(**{"SEC-USR-ID": uid, "SEC-USR-FNAME": first, "SEC-USR-LNAME": last,
                           "SEC-USR-PWD": pwd, "SEC-USR-TYPE": typ})


@pytest.fixture
def region(cics):
    cics.dataset("USRSEC", USRSEC, [user("ADMIN001", "PASSWORD", "A"),
                                    user("USER0001", "SECRET", "U")])
    return cics


def sign_on(cics, aid="ENTER", **fields):
    return cics.send("COSGN00C", "COSGN0A", aid, commarea(), **fields)


def test_first_entry_sends_blank_signon_screen(cics):
    r = cics.run("COSGN00C")
    s = r.screen
    assert (s.map, s.mapset) == ("COSGN0A", "COSGN00")
    assert "ER" in s.flags
    assert s["TRNNAMEO"] == "CC00" and s["PGMNAMEO"] == "COSGN00C"
    assert s["CURDATEO"] == "03/15/24" and s["CURTIMEO"] == "10:11:12"
    assert s["APPLIDO"] == "CARDDEMO" and s["SYSIDO"] == "CICS"
    assert s["ERRMSGO"] == ""
    assert r.return_transid == "CC00"


@pytest.mark.parametrize("uid,typ,target", [("admin001", "A", "COADM01C"),
                                            ("USER0001", "U", "COMEN01C")])
def test_valid_signon_routes_by_user_type(region, uid, typ, target):
    r = sign_on(region, userid=uid, passwd="password" if typ == "A" else "secret")
    assert r.xctl == target
    common = r.common()
    assert common["CDEMO-USER-ID"] == uid.upper()
    assert common["CDEMO-USER-TYPE"] == typ
    assert common["CDEMO-FROM-PROGRAM"] == "COSGN00C"
    assert common["CDEMO-PGM-CONTEXT"] == 0


@pytest.mark.parametrize("fields,message", [
    ({}, "Please enter User ID ..."),
    ({"userid": "USER0001"}, "Please enter Password ..."),
    ({"userid": "USER0001", "passwd": "WRONG"}, "Wrong Password. Try again ..."),
    ({"userid": "NOBODY", "passwd": "X"}, "User not found. Try again ..."),
])
def test_signon_errors_redisplay_screen(region, fields, message):
    r = sign_on(region, **fields)
    assert r.message == message
    assert r.xctl is None
    assert r.return_transid == "CC00"


def test_user_file_error_is_reported(region):
    r = region.send("COSGN00C", "COSGN0A", "ENTER", commarea(),
                    faults=["READ USRSEC 17"], userid="USER0001", passwd="SECRET")
    assert r.message == "Unable to verify the User ..."


def test_pf3_says_goodbye(region):
    r = sign_on(region, aid="PF3")
    assert r.screens == []
    assert "Thank you" in r.texts[0]


def test_invalid_key(region):
    r = sign_on(region, aid="PF5")
    assert r.message == "Invalid key pressed. Please see below...         "[:50].rstrip()
