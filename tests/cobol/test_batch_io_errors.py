"""Batch I/O error paths: every OPEN/READ/WRITE/REWRITE/CLOSE status check that abends.

support/iofault.c forces a FILE STATUS on one file operation (see ``Cobol.run(faults=)``),
so each case runs the program on a small, otherwise valid dataset and checks the error
message, the decoded file status and the CEE3ABD abend.
"""
import pytest

from harness.builders import account, card, customer, tcatbal, tran, xref
from harness.cobol import Cobol
from harness.fixtures import ascii_records
from harness.records import (ACCOUNT, CARD, CUSTOMER, DISCGRP, TCATBAL, TRAN, TRANCATG,
                             TRANTYPE, XREF)

CARD_A, CARD_UNKNOWN = "4000000000000001", "4999999999999999"


def readers(cobol, tmp_path, program):
    dd, layout, record = {
        "CBACT01C": ("ACCTFILE", ACCOUNT, account(1)),
        "CBACT02C": ("CARDFILE", CARD, card(CARD_A, 1)),
        "CBACT03C": ("XREFFILE", XREF, xref(CARD_A, 1)),
        "CBCUS01C": ("CUSTFILE", CUSTOMER, customer(1)),
    }[program]
    return {dd: cobol.load_indexed(tmp_path / f"{dd}.dat", layout, [record])}, {}


def cbtrn01c(cobol, tmp_path, _):
    daily = [tran("V000000000000001", CARD_A, "1.00")]
    return {
        "DALYTRAN": Cobol.write_sequential(tmp_path / "daily.dat", daily, TRAN.rec_len),
        "CUSTFILE": cobol.load_indexed(tmp_path / "cust.dat", CUSTOMER, [customer(1)]),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, [xref(CARD_A, 1)]),
        "CARDFILE": cobol.load_indexed(tmp_path / "card.dat", CARD, [card(CARD_A, 1)]),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [account(1)]),
        "TRANFILE": cobol.load_indexed(tmp_path / "tran.dat", TRAN, []),
    }, {}


def cbtrn02c(cobol, tmp_path, _):
    # T1 posts against an existing category balance, T2 creates a new one, T3 is rejected.
    daily = [tran("T000000000000001", CARD_A, "10.00", type_cd="01", cat_cd=1),
             tran("T000000000000002", CARD_A, "5.00", type_cd="01", cat_cd=2),
             tran("T000000000000003", CARD_UNKNOWN, "1.00")]
    return {
        "DALYTRAN": Cobol.write_sequential(tmp_path / "daily.dat", daily, TRAN.rec_len),
        "TRANFILE": tmp_path / "tran.dat",
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, [xref(CARD_A, 1)]),
        "DALYREJS": tmp_path / "rejects.dat",
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [account(1)]),
        "TCATBALF": cobol.load_indexed(tmp_path / "tcatbal.dat", TCATBAL,
                                       [tcatbal(1, "01", 1, "100.00")]),
    }, {"current_date": "2024031512345678"}


def cbtrn03c(cobol, tmp_path, _):
    trans = [tran("R000000000000001", CARD_A, "1.00",
                  proc_ts="2024-03-15-10.00.00.000000")]
    return {
        "TRANFILE": Cobol.write_sequential(tmp_path / "trans.dat", trans, TRAN.rec_len),
        "TRANREPT": tmp_path / "report.dat",
        "CARDXREF": cobol.load_indexed(tmp_path / "xref.dat", XREF, [xref(CARD_A, 1)]),
        "TRANTYPE": cobol.load_indexed(tmp_path / "trantype.dat", TRANTYPE,
                                       ascii_records("trantype.txt", TRANTYPE.rec_len)),
        "TRANCATG": cobol.load_indexed(tmp_path / "trancatg.dat", TRANCATG,
                                       ascii_records("trancatg.txt", TRANCATG.rec_len)),
        "DATEPARM": Cobol.write_sequential(tmp_path / "dateparm.dat",
                                           ["2024-01-01 2024-12-31".ljust(80)], 80),
    }, {}


def cbact04c(cobol, tmp_path, _):
    # Two accounts: CBACT04C only rewrites an account when the next one starts.
    groups = [DISCGRP.build(**{"DIS-ACCT-GROUP-ID": "GOLD", "DIS-TRAN-TYPE-CD": "01",
                               "DIS-TRAN-CAT-CD": 1, "DIS-INT-RATE": "12.00"})]
    return {
        "TCATBALF": cobol.load_indexed(tmp_path / "tcatbal.dat", TCATBAL,
                                       [tcatbal(1, "01", 1, "1200.00"),
                                        tcatbal(2, "01", 1, "600.00")]),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF,
                                       [xref(CARD_A, 1), xref("4000000000000002", 2)]),
        "DISCGRP": cobol.load_indexed(tmp_path / "discgrp.dat", DISCGRP, groups),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT,
                                       [account(1, group="GOLD"), account(2, group="GOLD")]),
        "TRANSACT": tmp_path / "systran.dat",
    }, {"parm": "2022071800", "current_date": "2024031512345678"}


SETUP = {"CBACT01C": readers, "CBACT02C": readers, "CBACT03C": readers,
         "CBCUS01C": readers, "CBTRN01C": cbtrn01c, "CBTRN02C": cbtrn02c,
         "CBTRN03C": cbtrn03c, "CBACT04C": cbact04c}

CASES = [
    ("CBACT01C", "READ ACCTFILE-FILE 30", "ERROR READING ACCOUNT FILE"),
    ("CBACT01C", "CLOSE ACCTFILE-FILE 42", "ERROR CLOSING ACCOUNT FILE"),
    ("CBACT02C", "READ CARDFILE-FILE 30", "ERROR READING CARDFILE"),
    ("CBACT02C", "CLOSE CARDFILE-FILE 42", "ERROR CLOSING CARDFILE"),
    ("CBACT03C", "READ XREFFILE-FILE 30", "ERROR READING XREFFILE"),
    ("CBACT03C", "CLOSE XREFFILE-FILE 42", "ERROR CLOSING XREFFILE"),
    ("CBCUS01C", "READ CUSTFILE-FILE 30", "ERROR READING CUSTOMER FILE"),
    ("CBCUS01C", "CLOSE CUSTFILE-FILE 42", "ERROR CLOSING CUSTOMER FILE"),

    ("CBTRN01C", "READ DALYTRAN-FILE 30", "ERROR READING DAILY TRANSACTION FILE"),
    ("CBTRN01C", "OPEN XREF-FILE 37", "ERROR OPENING CROSS REF FILE"),
    ("CBTRN01C", "OPEN CARD-FILE 37", "ERROR OPENING CARD FILE"),
    ("CBTRN01C", "OPEN ACCOUNT-FILE 37", "ERROR OPENING ACCOUNT FILE"),
    ("CBTRN01C", "OPEN TRANSACT-FILE 37", "ERROR OPENING TRANSACTION FILE"),
    # CBTRN01C reports a DALYTRAN close failure as "CUSTOMER FILE", with CUSTFILE's status.
    ("CBTRN01C", "CLOSE DALYTRAN-FILE 42", "ERROR CLOSING CUSTOMER FILE", "00"),
    ("CBTRN01C", "CLOSE CUSTOMER-FILE 42", "ERROR CLOSING CUSTOMER FILE"),
    ("CBTRN01C", "CLOSE XREF-FILE 42", "ERROR CLOSING CROSS REF FILE"),
    ("CBTRN01C", "CLOSE CARD-FILE 42", "ERROR CLOSING CARD FILE"),
    ("CBTRN01C", "CLOSE ACCOUNT-FILE 42", "ERROR CLOSING ACCOUNT FILE"),
    ("CBTRN01C", "CLOSE TRANSACT-FILE 42", "ERROR CLOSING TRANSACTION FILE"),

    ("CBTRN02C", "OPEN TRANSACT-FILE 37", "ERROR OPENING TRANSACTION FILE"),
    ("CBTRN02C", "OPEN XREF-FILE 37", "ERROR OPENING CROSS REF FILE"),
    ("CBTRN02C", "OPEN DALYREJS-FILE 37", "ERROR OPENING DALY REJECTS FILE"),
    ("CBTRN02C", "OPEN ACCOUNT-FILE 37", "ERROR OPENING ACCOUNT MASTER FILE"),
    ("CBTRN02C", "OPEN TCATBAL-FILE 37", "ERROR OPENING TRANSACTION BALANCE FILE"),
    ("CBTRN02C", "READ DALYTRAN-FILE 30", "ERROR READING DALYTRAN FILE"),
    ("CBTRN02C", "WRITE DALYREJS-FILE 34", "ERROR WRITING TO REJECTS FILE"),
    ("CBTRN02C", "READ TCATBAL-FILE 30", "ERROR READING TRANSACTION BALANCE FILE"),
    ("CBTRN02C", "WRITE TCATBAL-FILE 24", "ERROR WRITING TRANSACTION BALANCE FILE"),
    ("CBTRN02C", "REWRITE TCATBAL-FILE 21", "ERROR REWRITING TRANSACTION BALANCE FILE"),
    ("CBTRN02C", "WRITE TRANSACT-FILE 24", "ERROR WRITING TO TRANSACTION FILE"),
    ("CBTRN02C", "CLOSE DALYTRAN-FILE 42", "ERROR CLOSING DALYTRAN FILE"),
    ("CBTRN02C", "CLOSE TRANSACT-FILE 42", "ERROR CLOSING TRANSACTION FILE"),
    ("CBTRN02C", "CLOSE XREF-FILE 42", "ERROR CLOSING CROSS REF FILE"),
    # CBTRN02C shows XREFFILE's status for a DALYREJS close failure.
    ("CBTRN02C", "CLOSE DALYREJS-FILE 42", "ERROR CLOSING DAILY REJECTS FILE", "00"),
    ("CBTRN02C", "CLOSE ACCOUNT-FILE 42", "ERROR CLOSING ACCOUNT FILE"),
    ("CBTRN02C", "CLOSE TCATBAL-FILE 42", "ERROR CLOSING TRANSACTION BALANCE FILE"),

    ("CBTRN03C", "READ DATE-PARMS-FILE 30", "ERROR READING DATEPARM FILE"),
    ("CBTRN03C", "READ TRANSACT-FILE 30", "ERROR READING TRANSACTION FILE"),
    ("CBTRN03C", "WRITE REPORT-FILE 34", "ERROR WRITING REPTFILE"),
    ("CBTRN03C", "OPEN REPORT-FILE 37", "ERROR OPENING REPTFILE"),
    ("CBTRN03C", "OPEN XREF-FILE 37", "ERROR OPENING CROSS REF FILE"),
    ("CBTRN03C", "OPEN TRANTYPE-FILE 37", "ERROR OPENING TRANSACTION TYPE FILE"),
    ("CBTRN03C", "OPEN TRANCATG-FILE 37", "ERROR OPENING TRANSACTION CATG FILE"),
    ("CBTRN03C", "OPEN DATE-PARMS-FILE 37", "ERROR OPENING DATE PARM FILE"),
    ("CBTRN03C", "CLOSE TRANSACT-FILE 42", "ERROR CLOSING POSTED TRANSACTION FILE"),
    ("CBTRN03C", "CLOSE REPORT-FILE 42", "ERROR CLOSING REPORT FILE"),
    ("CBTRN03C", "CLOSE XREF-FILE 42", "ERROR CLOSING CROSS REF FILE"),
    ("CBTRN03C", "CLOSE TRANTYPE-FILE 42", "ERROR CLOSING TRANSACTION TYPE FILE"),
    ("CBTRN03C", "CLOSE TRANCATG-FILE 42", "ERROR CLOSING TRANSACTION CATG FILE"),
    ("CBTRN03C", "CLOSE DATE-PARMS-FILE 42", "ERROR CLOSING DATE PARM FILE"),

    ("CBACT04C", "OPEN XREF-FILE 37", "ERROR OPENING CROSS REF FILE"),
    # CBACT04C labels the DISCGRP open failure as the "DALY REJECTS FILE".
    ("CBACT04C", "OPEN DISCGRP-FILE 37", "ERROR OPENING DALY REJECTS FILE"),
    ("CBACT04C", "OPEN ACCOUNT-FILE 37", "ERROR OPENING ACCOUNT MASTER FILE"),
    ("CBACT04C", "OPEN TRANSACT-FILE 37", "ERROR OPENING TRANSACTION FILE"),
    ("CBACT04C", "READ TCATBAL-FILE 30", "ERROR READING TRANSACTION CATEGORY FILE"),
    ("CBACT04C", "REWRITE ACCOUNT-FILE 21", "ERROR RE-WRITING ACCOUNT FILE"),
    ("CBACT04C", "READ DISCGRP-FILE 30", "ERROR READING DISCLOSURE GROUP FILE"),
    ("CBACT04C", "WRITE TRANSACT-FILE 34", "ERROR WRITING TRANSACTION RECORD"),
    ("CBACT04C", "CLOSE TCATBAL-FILE 42", "ERROR CLOSING TRANSACTION BALANCE FILE"),
    ("CBACT04C", "CLOSE XREF-FILE 42", "ERROR CLOSING CROSS REF FILE"),
    ("CBACT04C", "CLOSE DISCGRP-FILE 42", "ERROR CLOSING DISCLOSURE GROUP FILE"),
    ("CBACT04C", "CLOSE ACCOUNT-FILE 42", "ERROR CLOSING ACCOUNT FILE"),
    ("CBACT04C", "CLOSE TRANSACT-FILE 42", "ERROR CLOSING TRANSACTION FILE"),
]


def run_with_fault(cobol, tmp_path, program, fault):
    dds, kwargs = SETUP[program](cobol, tmp_path, program)
    return cobol.run(program, dds, faults=[fault], **kwargs)


CASES = [c if len(c) == 4 else (*c, c[1][-2:]) for c in CASES]


@pytest.mark.parametrize("program,fault,message,shown", CASES,
                         ids=[f"{p}-{f.replace(' ', '-')}" for p, f, _, _ in CASES])
def test_file_error_abends(cobol, tmp_path, program, fault, message, shown):
    result = run_with_fault(cobol, tmp_path, program, fault)

    assert result.abended, result.stdout + result.stderr
    assert any(line.startswith(message) for line in result.lines), result.stdout
    assert f"FILE STATUS IS: NNNN00{shown}" in result.lines
    assert result.lines[-2:] == ["ABENDING PROGRAM", "CEE3ABD: ABEND U0999"]


@pytest.mark.parametrize("program,fault", [
    ("CBACT01C", "READ ACCTFILE-FILE 9D"), ("CBACT02C", "READ CARDFILE-FILE 9D"),
    ("CBACT03C", "READ XREFFILE-FILE 9D"), ("CBCUS01C", "READ CUSTFILE-FILE 9D"),
    ("CBTRN01C", "READ DALYTRAN-FILE 9D"), ("CBTRN02C", "READ DALYTRAN-FILE 9D"),
    ("CBTRN03C", "READ TRANSACT-FILE 9D"), ("CBACT04C", "READ TCATBAL-FILE 9D"),
])
def test_implementor_status_is_shown_as_binary(cobol, tmp_path, program, fault):
    """Status 9x: the second byte is displayed as its binary value ('D' = 196 in EBCDIC)."""
    result = run_with_fault(cobol, tmp_path, program, fault)
    assert result.abended
    status = [line for line in result.lines if line.startswith("FILE STATUS IS: NNNN")]
    assert status == ["FILE STATUS IS: NNNN9068"] or status == ["FILE STATUS IS: NNNN9196"], status


def test_cbtrn02c_account_rewrite_invalid_key_is_not_fatal(cobol, tmp_path):
    """REWRITE ACCOUNT INVALID KEY only sets reason 109 locally; the posting still completes."""
    dds, kwargs = cbtrn02c(cobol, tmp_path, None)
    result = cobol.run("CBTRN02C", dds, faults=["REWRITE ACCOUNT-FILE 23"], **kwargs)
    assert result.returncode == 4, result.stdout  # one reject (T3)
    assert "TRANSACTIONS PROCESSED :000000003" in result.lines
