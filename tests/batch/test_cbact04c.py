"""CBACT04C: monthly interest calculation (INTCALC job)."""
from decimal import Decimal

from harness.builders import account, tcatbal, xref
from harness.cobol import Cobol
from harness.fixtures import ascii_records
from harness.records import ACCOUNT, DISCGRP, TCATBAL, TRAN, XREF

RUN_TS = "2024031512345678"
PARM = "2022071800"


def discgrp(group, type_cd, cat_cd, rate):
    return DISCGRP.build(**{"DIS-ACCT-GROUP-ID": group, "DIS-TRAN-TYPE-CD": type_cd,
                            "DIS-TRAN-CAT-CD": cat_cd, "DIS-INT-RATE": rate})


def run_interest(cobol, tmp_path, balances, accounts, xrefs, groups, parm=PARM):
    dds = {
        "TCATBALF": cobol.load_indexed(tmp_path / "tcatbal.dat", TCATBAL, balances),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, xrefs),
        "DISCGRP": cobol.load_indexed(tmp_path / "discgrp.dat", DISCGRP, groups),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, accounts),
        "TRANSACT": tmp_path / "systran.dat",
    }
    result = cobol.run("CBACT04C", dds, parm=parm, current_date=RUN_TS)
    if result.abended:
        return result, None, None
    trans = [TRAN.parse(r) for r in Cobol.read_sequential(dds["TRANSACT"], TRAN.rec_len)]
    accts = {int(a["ACCT-ID"]): a for a in
             map(ACCOUNT.parse, cobol.dump_indexed(dds["ACCTFILE"], ACCOUNT))}
    return result, trans, accts


def test_interest_transactions_and_account_update(cobol, tmp_path):
    balances = [tcatbal(1, "01", 1, "1200.00"), tcatbal(1, "01", 2, "600.00"),
                tcatbal(2, "01", 1, "300.00")]
    accounts = [account(1, curr_bal="1000", cyc_credit="50", cyc_debit="-20", group="GOLD"),
                account(2, curr_bal="300", cyc_credit="5", group="GOLD")]
    groups = [discgrp("GOLD", "01", 1, "12.00"), discgrp("GOLD", "01", 2, "24.00")]
    result, trans, accts = run_interest(cobol, tmp_path, balances, accounts,
                                        [xref("4000000000000001", 1), xref("4000000000000002", 2)],
                                        groups)

    assert result.returncode == 0, result.stdout
    assert result.lines[0] == "START OF EXECUTION OF PROGRAM CBACT04C"
    assert [(t["TRAN-ID"], t["TRAN-AMT"], t["TRAN-CARD-NUM"]) for t in trans] == [
        ("2022071800000001", Decimal("12.00"), "4000000000000001"),   # 1200 * 12% / 12
        ("2022071800000002", Decimal("12.00"), "4000000000000001"),   # 600 * 24% / 12
        ("2022071800000003", Decimal("3.00"), "4000000000000002"),
    ]
    first = trans[0]
    assert (first["TRAN-TYPE-CD"], first["TRAN-CAT-CD"], first["TRAN-SOURCE"]) == \
        ("01", Decimal(5), "System")
    assert first["TRAN-DESC"] == "Int. for a/c 00000000001"
    assert first["TRAN-MERCHANT-ID"] == 0 and first["TRAN-MERCHANT-NAME"] == ""
    assert first["TRAN-ORIG-TS"] == first["TRAN-PROC-TS"] == "2024-03-15-12.34.56.780000"

    # account 1 is rewritten when the account number changes
    assert accts[1]["ACCT-CURR-BAL"] == Decimal("1024.00")
    assert accts[1]["ACCT-CURR-CYC-CREDIT"] == 0 and accts[1]["ACCT-CURR-CYC-DEBIT"] == 0
    # the last account is never rewritten: the loop ends before 1050-UPDATE-ACCOUNT
    assert accts[2] == ACCOUNT.parse(accounts[1])


def test_falls_back_to_default_disclosure_group(cobol, tmp_path):
    result, trans, _ = run_interest(
        cobol, tmp_path, [tcatbal(1, "02", 3, "-500.00")], [account(1, group="NOGROUP")],
        [xref("4000000000000001", 1)], [discgrp("DEFAULT", "02", 3, "18.00")])
    assert result.returncode == 0
    assert "DISCLOSURE GROUP RECORD MISSING" in result.lines
    assert "TRY WITH DEFAULT GROUP CODE" in result.lines
    assert [t["TRAN-AMT"] for t in trans] == [Decimal("-7.50")]


def test_zero_rate_writes_no_transaction(cobol, tmp_path):
    result, trans, _ = run_interest(
        cobol, tmp_path, [tcatbal(1, "01", 1, "999.99")], [account(1, group="FREE")],
        [xref("4000000000000001", 1)], [discgrp("FREE", "01", 1, "0")])
    assert result.returncode == 0
    assert trans == []


def test_missing_default_disclosure_group_abends(cobol, tmp_path):
    result, _, _ = run_interest(
        cobol, tmp_path, [tcatbal(1, "01", 1, "100")], [account(1, group="NOGROUP")],
        [xref("4000000000000001", 1)], [discgrp("OTHER", "01", 1, "10")])
    assert result.abended
    assert "ERROR READING DEFAULT DISCLOSURE GROUP" in result.lines
    assert "FILE STATUS IS: NNNN0023" in result.lines


def test_missing_account_abends(cobol, tmp_path):
    result, _, _ = run_interest(
        cobol, tmp_path, [tcatbal(5, "01", 1, "100")], [account(1)],
        [xref("4000000000000001", 1)], [discgrp("DEFAULT", "01", 1, "10")])
    assert result.abended
    assert "ACCOUNT NOT FOUND: 00000000005" in result.lines
    assert "ERROR READING ACCOUNT FILE" in result.lines


def test_missing_xref_abends(cobol, tmp_path):
    result, _, _ = run_interest(
        cobol, tmp_path, [tcatbal(1, "01", 1, "100")], [account(1)],
        [xref("4000000000000009", 9)], [discgrp("DEFAULT", "01", 1, "10")])
    assert result.abended
    assert "ERROR READING XREF FILE" in result.lines


def test_sample_data(cobol, tmp_path):
    balances = ascii_records("tcatbal.txt", TCATBAL.rec_len)
    result, trans, _ = run_interest(
        cobol, tmp_path, balances, ascii_records("acctdata.txt", ACCOUNT.rec_len),
        ascii_records("cardxref.txt", XREF.rec_len), ascii_records("discgrp.txt", DISCGRP.rec_len))
    assert result.returncode == 0, result.stdout[-2000:]
    assert len(trans) <= len(balances)
    assert [t["TRAN-ID"] for t in trans] == [f"{PARM}{n:06d}" for n in range(1, len(trans) + 1)]


def test_missing_tcatbal_file_abends(cobol, tmp_path):
    result = cobol.run("CBACT04C", {"TCATBALF": tmp_path / "missing.dat"}, parm=PARM)
    assert result.abended
    assert "FILE STATUS IS: NNNN0035" in result.lines
