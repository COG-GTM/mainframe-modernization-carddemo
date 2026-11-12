"""CORPT00C: submit the transaction report batch job through the JOBS TD queue (CR00)."""
import pytest

from harness.cics import commarea

CUSTOM = dict(custom="X", sdtmm="01", sdtdd="01", sdtyyyy="2024",
              edtmm="02", edtdd="29", edtyyyy="2024")


def rpt(cics, aid="ENTER", faults=None, env=None, **fields):
    return cics.send("CORPT00C", "CORPT0A", aid, commarea(context=1), faults, env, **fields)


def jcl(r):
    return [t.rstrip() for t in r.texts]


def test_first_entry(cics):
    r = cics.run("CORPT00C", commarea=commarea(context=0))
    assert r.screen.map == "CORPT0A" and r.return_transid == "CR00"


@pytest.mark.parametrize("fields,name,start,end", [
    ({"monthly": "X"}, "Monthly", "2024-03-01", "2024-03-31"),
    ({"yearly": "X"}, "Yearly", "2024-01-01", "2024-12-31"),
    (CUSTOM, "Custom", "2024-01-01", "2024-02-29"),
])
def test_confirmed_report_writes_jcl_to_jobs_queue(cics, fields, name, start, end):
    r = rpt(cics, confirm="Y", **fields)
    assert r.message == f"{name} report submitted for printing ..."
    lines = jcl(r)
    assert lines and all(e[2] == "JOBS" for e in r.called("WRITEQ"))
    assert any(start in line for line in lines) and any(end in line for line in lines)


@pytest.mark.parametrize("fields", [{"monthly": "X"}, {"yearly": "X"}, CUSTOM])
def test_report_needs_confirmation(cics, fields):
    r = rpt(cics, **fields)
    assert r.message.startswith("Please confirm to print the ")
    assert r.texts == []


def test_declined_and_invalid_confirmation(cics):
    assert rpt(cics, confirm="N", monthly="X").texts == []
    r = rpt(cics, confirm="Q", monthly="X")
    assert r.message == '"Q" is not a valid value to confirm...'


@pytest.mark.parametrize("field,value,message", [
    ("sdtmm", None, "Start Date - Month can NOT be empty..."),
    ("sdtdd", None, "Start Date - Day can NOT be empty..."),
    ("sdtyyyy", None, "Start Date - Year can NOT be empty..."),
    ("edtmm", None, "End Date - Month can NOT be empty..."),
    ("edtdd", None, "End Date - Day can NOT be empty..."),
    ("edtyyyy", None, "End Date - Year can NOT be empty..."),
    ("sdtmm", "13", "Start Date - Not a valid Month..."),
    ("sdtdd", "32", "Start Date - Not a valid Day..."),
    ("edtmm", "13", "End Date - Not a valid Month..."),
    ("edtdd", "32", "End Date - Not a valid Day..."),
])
def test_custom_date_validation(cics, field, value, message):
    fields = {**CUSTOM, field: value}
    if value is None:
        del fields[field]
    assert message in [s["ERRMSGO"] for s in rpt(cics, confirm="Y", **fields).screens]


def test_custom_dates_rejected_by_ceedays(cics):
    r = rpt(cics, confirm="Y", env={"CEEDAYS_RESULT": "BAD-DATE-VALUE"}, **CUSTOM)
    msgs = [s["ERRMSGO"] for s in r.screens]
    assert "Start Date - Not a valid date..." in msgs or "End Date - Not a valid date..." in msgs
    assert r.texts == []


def test_no_report_selected_and_tdq_error(cics):
    assert rpt(cics).message == "Select a report type to print report..."
    r = rpt(cics, faults=["WRITEQ JOBS 17"], confirm="Y", monthly="X")
    assert "Unable to Write TDQ (JOBS)..." in [s["ERRMSGO"] for s in r.screens]


def test_keys(cics):
    assert cics.run("CORPT00C").xctl == "COSGN00C"
    assert rpt(cics, aid="PF3").xctl == "COMEN01C"
    assert rpt(cics, aid="PF9").message.startswith("Invalid key")
