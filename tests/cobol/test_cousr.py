"""COUSR01C/02C/03C: add, update and delete users in USRSEC (admin functions)."""
import pytest

from harness.cics import commarea
from harness.records import USRSEC


def user(uid, pwd="PASSWORD", typ="U", first="Ann", last="Lee"):
    return USRSEC.build(**{"SEC-USR-ID": uid, "SEC-USR-FNAME": first, "SEC-USR-LNAME": last,
                           "SEC-USR-PWD": pwd, "SEC-USR-TYPE": typ})


def ca(context=1, selected="", from_program="COADM01C"):
    """CARDDEMO-COMMAREA followed by the CDEMO-CU0n-INFO block (selected user at +26)."""
    tail = " " * 8 + " " * 8 + "00000000" + "N" + " " + selected.ljust(8)
    return commarea(user_type="A", context=context, user_id="ADMIN001",
                    from_program=from_program) + tail


@pytest.fixture
def region(cics):
    cics.dataset("USRSEC", USRSEC, [user("ADMIN001", typ="A"), user("USER0001")])
    return cics


FULL = dict(fname="Bob", lname="Ray", userid="USER0002", passwd="PW", usrtype="U")


# ------------------------------------------------------------------ COUSR01C
def test_add_first_entry_shows_empty_form(region):
    r = region.run("COUSR01C", commarea=ca(context=0))
    assert r.screen.map == "COUSR1A"
    assert r.return_transid == "CU01"
    assert r.common()["CDEMO-PGM-CONTEXT"] == 1


def test_add_user_writes_record(region):
    r = region.send("COUSR01C", "COUSR1A", "ENTER", ca(), **FULL)
    assert r.message == "User USER0002 has been added ..."
    assert r.screen["ERRMSGC"] != ""  # DFHGREEN
    added = USRSEC.parse(region.records("USRSEC")[2])
    assert added == {"SEC-USR-ID": "USER0002", "SEC-USR-FNAME": "Bob", "SEC-USR-LNAME": "Ray",
                     "SEC-USR-PWD": "PW", "SEC-USR-TYPE": "U", "SEC-USR-FILLER": ""}


@pytest.mark.parametrize("missing,message", [
    ("fname", "First Name can NOT be empty..."), ("lname", "Last Name can NOT be empty..."),
    ("userid", "User ID can NOT be empty..."), ("passwd", "Password can NOT be empty..."),
    ("usrtype", "User Type can NOT be empty..."),
])
def test_add_requires_every_field(region, missing, message):
    fields = {k: v for k, v in FULL.items() if k != missing}
    r = region.send("COUSR01C", "COUSR1A", "ENTER", ca(), **fields)
    assert r.message == message
    assert len(region.records("USRSEC")) == 2


def test_add_duplicate_user(region):
    r = region.send("COUSR01C", "COUSR1A", "ENTER", ca(), **{**FULL, "userid": "USER0001"})
    assert r.message == "User ID already exist..."


def test_add_file_error(region):
    r = region.send("COUSR01C", "COUSR1A", "ENTER", ca(), faults=["WRITE USRSEC 17"], **FULL)
    assert r.message == "Unable to Add User..."


@pytest.mark.parametrize("program,mapname", [("COUSR01C", "COUSR1A"), ("COUSR02C", "COUSR2A"),
                                             ("COUSR03C", "COUSR3A")])
def test_keys_common_to_maintenance_screens(region, program, mapname):
    assert region.run(program).xctl == "COSGN00C"
    cleared = region.send(program, mapname, "PF4", ca(), fname="Typed")
    assert cleared.screen["FNAMEO"] == "" and cleared.message == ""
    assert region.send(program, mapname, "PF9", ca()).message.startswith("Invalid key")
    back = region.send(program, mapname, "PF3", ca(from_program=" "))
    assert back.xctl == "COADM01C"
    assert back.common()["CDEMO-FROM-PROGRAM"] == program


# ------------------------------------------------------------------ COUSR02C
def test_update_preselected_user_from_list(region):
    r = region.run("COUSR02C", commarea=ca(context=0, selected="USER0001"))
    s = r.screen
    assert s["USRIDINO"] == "USER0001"
    assert (s["FNAMEO"], s["LNAMEO"], s["USRTYPEO"]) == ("Ann", "Lee", "U")
    assert r.message == "Press PF5 key to save your updates ..."


def test_update_lookup(region):
    assert region.send("COUSR02C", "COUSR2A", "ENTER", ca(),
                       usridin="ADMIN001").screen["USRTYPEO"] == "A"
    assert region.send("COUSR02C", "COUSR2A", "ENTER", ca()).message == \
        "User ID can NOT be empty..."
    assert region.send("COUSR02C", "COUSR2A", "ENTER", ca(), usridin="NOBODY").message == \
        "User ID NOT found..."
    assert region.send("COUSR02C", "COUSR2A", "ENTER", ca(), faults=["READ USRSEC 17"],
                       usridin="USER0001").message == "Unable to lookup User..."


def test_update_pf5_rewrites_changed_user(region):
    r = region.send("COUSR02C", "COUSR2A", "PF5", ca(), usridin="USER0001", fname="Zed",
                    lname="Lee", passwd="NEWPW", usrtype="A")
    assert r.message == "User USER0001 has been updated ..."
    rec = USRSEC.parse(region.records("USRSEC")[1])
    assert (rec["SEC-USR-FNAME"], rec["SEC-USR-PWD"], rec["SEC-USR-TYPE"]) == ("Zed", "NEWPW", "A")


def test_update_without_changes(region):
    r = region.send("COUSR02C", "COUSR2A", "PF5", ca(), usridin="USER0001", fname="Ann",
                    lname="Lee", passwd="PASSWORD", usrtype="U")
    assert r.message == "Please modify to update ..."


@pytest.mark.parametrize("missing,message", [
    ("usridin", "User ID can NOT be empty..."), ("fname", "First Name can NOT be empty..."),
    ("lname", "Last Name can NOT be empty..."), ("passwd", "Password can NOT be empty..."),
    ("usrtype", "User Type can NOT be empty..."),
])
def test_update_requires_every_field(region, missing, message):
    fields = {"usridin": "USER0001", "fname": "A", "lname": "B", "passwd": "C", "usrtype": "U"}
    del fields[missing]
    assert region.send("COUSR02C", "COUSR2A", "PF5", ca(), **fields).message == message


@pytest.mark.parametrize("fault,message", [("REWRITE USRSEC 13", "User ID NOT found..."),
                                           ("REWRITE USRSEC 17", "Unable to Update User...")])
def test_update_rewrite_errors(region, fault, message):
    r = region.send("COUSR02C", "COUSR2A", "PF5", ca(), faults=[fault], usridin="USER0001",
                    fname="Zed", lname="Lee", passwd="X", usrtype="U")
    assert r.message == message


def test_update_pf3_saves_and_returns_to_caller(region):
    r = region.send("COUSR02C", "COUSR2A", "PF3", ca(from_program="COUSR00C"),
                    usridin="USER0001", fname="Zed", lname="Lee", passwd="X", usrtype="U")
    assert r.xctl == "COUSR00C"
    assert USRSEC.parse(region.records("USRSEC")[1])["SEC-USR-FNAME"] == "Zed"


@pytest.mark.parametrize("program,mapname", [("COUSR02C", "COUSR2A"), ("COUSR03C", "COUSR3A")])
def test_pf12_returns_to_admin_menu(region, program, mapname):
    assert region.send(program, mapname, "PF12", ca()).xctl == "COADM01C"


# ------------------------------------------------------------------ COUSR03C
def test_delete_preselected_user_shows_details(region):
    r = region.run("COUSR03C", commarea=ca(context=0, selected="USER0001"))
    assert r.screen["FNAMEO"] == "Ann"
    assert r.message == "Press PF5 key to delete this user ..."


def test_delete_lookup_errors(region):
    assert region.send("COUSR03C", "COUSR3A", "ENTER", ca()).message == \
        "User ID can NOT be empty..."
    assert region.send("COUSR03C", "COUSR3A", "ENTER", ca(), usridin="NOBODY").message == \
        "User ID NOT found..."
    assert region.send("COUSR03C", "COUSR3A", "ENTER", ca(), faults=["READ USRSEC 17"],
                       usridin="USER0001").message == "Unable to lookup User..."


def test_delete_pf5_removes_user(region):
    r = region.send("COUSR03C", "COUSR3A", "PF5", ca(), usridin="USER0001")
    assert r.message == "User USER0001 has been deleted ..."
    assert [USRSEC.key(x) for x in region.records("USRSEC")] == ["ADMIN001"]


def test_delete_pf5_validation_and_errors(region):
    assert region.send("COUSR03C", "COUSR3A", "PF5", ca()).message == \
        "User ID can NOT be empty..."
    assert region.send("COUSR03C", "COUSR3A", "PF5", ca(), faults=["DELETE USRSEC 13"],
                       usridin="USER0001").message == "User ID NOT found..."
    assert region.send("COUSR03C", "COUSR3A", "PF5", ca(), faults=["DELETE USRSEC 17"],
                       usridin="USER0001").message == "Unable to Update User..."
