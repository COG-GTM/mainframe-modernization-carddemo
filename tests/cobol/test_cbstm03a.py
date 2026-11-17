"""CBSTM03A: account statements (text + HTML) from the CREASTMT job.

The program is built by ``Cobol.mvs_program`` (TAB-expanded copybooks and a fake
PSA/TCB/TIOT from support/mvspsa.c) and calls the real CBSTM03B for file access.
"""
import pytest

from harness.builders import account, customer, tran, xref
from harness.cobol import Cobol
from harness.records import ACCOUNT, CUSTOMER, TRNX, XREF

CARD_A, CARD_B, CARD_C = "4000000000000001", "4000000000000002", "4000000000000003"
DDS = ["TRNXFILE", "XREFFILE", "CUSTFILE", "ACCTFILE", "STMTFILE", "HTMLFILE"]


def trnx(card, tran_id, amount, desc="Purchase"):
    return card + tran(tran_id, card, amount, desc=desc)[:330] + "    "


DEFAULT_TRNX = [trnx(CARD_A, "S000000000000001", "10.25", "Coffee"),
                trnx(CARD_A, "S000000000000002", "4.75", "Bagel"),
                trnx(CARD_B, "S000000000000003", "100.00", "Groceries")]


def run(cobol, tmp_path, trnx_records=None, xrefs=None, mvs_dds=None, faults=None):
    cobol.mvs_program("CBSTM03A")
    dds = {
        "TRNXFILE": cobol.load_indexed(tmp_path / "trnx.dat", TRNX,
                                       DEFAULT_TRNX if trnx_records is None else trnx_records),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, xrefs or [
            xref(CARD_A, 1, 1), xref(CARD_B, 2, 2)]),
        "CUSTFILE": cobol.load_indexed(tmp_path / "cust.dat", CUSTOMER, [
            customer(1), customer(2, first="Grace", last="Hopper"), customer(3)]),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [
            account(1, curr_bal="1234.56"), account(2, curr_bal="-20.00"), account(3)]),
        "STMTFILE": tmp_path / "stmt.txt",
        "HTMLFILE": tmp_path / "stmt.html",
    }
    env = {"MVS_JOB": "CREASTMT", "MVS_STEP": "STEP040",
           "MVS_DDS": ",".join(mvs_dds if mvs_dds is not None else DDS)}
    return cobol.run("CBSTM03A", dds, env=env, faults=faults)


def statements(tmp_path):
    lines = [l.rstrip() for l in Cobol.read_sequential(tmp_path / "stmt.txt", 80)]
    out, current = [], None
    for line in lines:
        if "START OF STATEMENT" in line:
            current = []
        current.append(line)
        if "END OF STATEMENT" in line:
            out.append(current)
    return out


def html(tmp_path):
    return [l.rstrip() for l in Cobol.read_sequential(tmp_path / "stmt.html", 100)]


def test_job_step_and_tiot_dd_names_are_listed(cobol, tmp_path):
    result = run(cobol, tmp_path, mvs_dds=["STEPLIB", "SYSPRINT:NULL", *DDS])
    assert result.returncode == 0, result.stdout + result.stderr
    assert result.lines[:2] == ["Running JCL : CREASTMT Step STEP040", "DD Names from TIOT:"]
    assert result.lines[2:] == [": STEPLIB  -- valid UCB", ": SYSPRINT --  null UCB"] + [
        f": {dd} -- valid UCB" for dd in DDS]


def test_last_tiot_entry_with_null_ucb(cobol, tmp_path):
    """The last entry is shown after the loop, with its own '-- null  UCB' wording."""
    result = run(cobol, tmp_path, mvs_dds=[*DDS, "SYSOUT:NULL"])
    assert result.returncode == 0
    assert result.lines[-1] == ": SYSOUT   -- null  UCB"


def test_one_statement_per_xref_with_its_transactions(cobol, tmp_path):
    assert run(cobol, tmp_path).returncode == 0
    first, second = statements(tmp_path)

    assert first[0].strip("*") == "START OF STATEMENT"
    assert first[1].split() == ["Ada", "Lovelace"]
    assert first[2] == "1 Main St"
    assert first[4].split() == ["NY", "USA", "10001"]
    assert "Account ID         :00000000001" in first
    assert "Current Balance    :000001234.56" in first
    tran_lines = [l for l in first if l.startswith("S0000")]
    assert [l.split() for l in tran_lines] == [["S000000000000001", "Coffee", "$", "10.25"],
                                               ["S000000000000002", "Bagel", "$", "4.75"]]
    assert next(l for l in first if l.startswith("Total EXP:")).split()[-1] == "15.00"
    assert first[-1].strip("*") == "END OF STATEMENT"

    assert second[1].split() == ["Grace", "Hopper"]
    assert "Account ID         :00000000002" in second
    assert "Current Balance    :000000020.00-" in second
    assert [l.split()[0] for l in second if l.startswith("S0000")] == ["S000000000000003"]
    assert next(l for l in second if l.startswith("Total EXP:")).split()[-1] == "100.00"


def test_card_without_transactions_gets_empty_statement(cobol, tmp_path):
    xrefs = [xref(CARD_A, 1, 1), xref(CARD_B, 2, 2), xref(CARD_C, 3, 3)]
    assert run(cobol, tmp_path, xrefs=xrefs).returncode == 0
    third = statements(tmp_path)[2]
    assert not [l for l in third if l.startswith("S0000")]
    assert next(l for l in third if l.startswith("Total EXP:")).split()[-1] == ".00"


def test_transactions_for_unknown_cards_are_ignored(cobol, tmp_path):
    records = [trnx(CARD_A, "S000000000000001", "1.00"),
               trnx(CARD_C, "S000000000000009", "9.00")]
    assert run(cobol, tmp_path, trnx_records=records).returncode == 0
    texts = ["\n".join(s) for s in statements(tmp_path)]
    assert len(texts) == 2 and not any("S000000000000009" in t for t in texts)


def test_html_statement(cobol, tmp_path):
    assert run(cobol, tmp_path).returncode == 0
    lines = html(tmp_path)
    assert lines[0] == "<!DOCTYPE html>"
    assert lines.count("<!DOCTYPE html>") == 2 and lines.count("</html>") == 2
    assert lines[-1] == "</html>"
    assert "<h3>Statement for Account Number: 00000000001         </h3>" in lines
    assert "<h3>Statement for Account Number: 00000000002         </h3>" in lines
    assert "<p>Current Balance    : 000001234.56 </p>" in lines
    assert lines[lines.index("<p>S000000000000001</p>") + 3:][:1] == [
        "<p>Coffee                                           </p>"]
    assert "<p>       10.25 </p>" in lines and "<p>S000000000000003</p>" in lines
    # The name line is STRINGed DELIMITED BY '  ', and ST-NAME separates first and
    # last name with two spaces when there is no middle name: only the first name shows.
    assert '<p style="font-size:16px">Ada  </p>' in lines
    assert '<p style="font-size:16px">Grace  </p>' in lines


@pytest.mark.parametrize("fault,message", [
    ("OPEN TRNX-FILE 35", "ERROR OPENING TRNXFILE"),
    ("READ TRNX-FILE 30@1", "ERROR READING TRNXFILE"),
    ("READ TRNX-FILE 30@2", "ERROR READING TRNXFILE"),
    ("OPEN XREF-FILE 35", "ERROR OPENING XREFFILE"),
    ("READ XREF-FILE 30", "ERROR READING XREFFILE"),
    ("OPEN CUST-FILE 35", "ERROR OPENING CUSTFILE"),
    ("READ CUST-FILE 23", "ERROR READING CUSTFILE"),
    ("OPEN ACCT-FILE 35", "ERROR OPENING ACCTFILE"),
    ("READ ACCT-FILE 23", "ERROR READING ACCTFILE"),
    ("CLOSE TRNX-FILE 42", "ERROR CLOSING TRNXFILE"),
    ("CLOSE XREF-FILE 42", "ERROR CLOSING XREFFILE"),
    ("CLOSE CUST-FILE 42", "ERROR CLOSING CUSTFILE"),
    ("CLOSE ACCT-FILE 42", "ERROR CLOSING ACCTFILE"),
])
def test_file_errors_abend(cobol, tmp_path, fault, message):
    result = run(cobol, tmp_path, faults=[fault])
    assert result.abended, result.stdout + result.stderr
    i = result.lines.index(message)
    assert result.lines[i + 1:] == [f"RETURN CODE: {fault.split()[2][:2]}",
                                    "ABENDING PROGRAM", "CEE3ABD: ABEND U0999"]


@pytest.mark.parametrize("dd", ["TRNX", "XREF", "CUST", "ACCT"])
def test_open_and_close_status_04_are_accepted(cobol, tmp_path, dd):
    result = run(cobol, tmp_path, faults=[f"OPEN {dd}-FILE 04", f"CLOSE {dd}-FILE 04"])
    assert result.returncode == 0, result.stdout + result.stderr
    assert len(statements(tmp_path)) == 2
