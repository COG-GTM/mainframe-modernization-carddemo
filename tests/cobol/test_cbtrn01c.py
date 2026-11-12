"""CBTRN01C: daily transaction card/account verification."""
from harness.builders import account, card, customer, tran, xref
from harness.cobol import Cobol
from harness.records import ACCOUNT, CARD, CUSTOMER, TRAN, XREF

CARD_A, CARD_B, CARD_UNKNOWN = "4000000000000001", "4000000000000002", "4999999999999999"


def run_verify(cobol, tmp_path, daily):
    dds = {
        "DALYTRAN": Cobol.write_sequential(tmp_path / "daily.dat", daily, TRAN.rec_len),
        "CUSTFILE": cobol.load_indexed(tmp_path / "cust.dat", CUSTOMER, [customer(1)]),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF,
                                       [xref(CARD_A, 1), xref(CARD_B, 404)]),
        "CARDFILE": cobol.load_indexed(tmp_path / "card.dat", CARD, [card(CARD_A, 1)]),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [account(1)]),
        "TRANFILE": cobol.load_indexed(tmp_path / "tran.dat", TRAN, []),
    }
    return cobol.run("CBTRN01C", dds)


def test_lookup_outcomes_per_transaction(cobol, tmp_path):
    daily = [tran("V000000000000001", CARD_A, "1.00"),
             tran("V000000000000002", CARD_UNKNOWN, "2.00"),
             tran("V000000000000003", CARD_B, "3.00")]
    result = run_verify(cobol, tmp_path, daily)
    out = result.lines

    assert result.returncode == 0, result.stdout
    assert out[0] == "START OF EXECUTION OF PROGRAM CBTRN01C"
    assert out[-1] == "END OF EXECUTION OF PROGRAM CBTRN01C"
    # valid card -> xref and account found
    assert "CARD NUMBER: " + CARD_A in out
    assert "ACCOUNT ID : 00000000001" in out
    assert "SUCCESSFUL READ OF ACCOUNT FILE" in out
    # unknown card -> skipped
    assert "INVALID CARD NUMBER FOR XREF" in out
    assert (f"CARD NUMBER {CARD_UNKNOWN} COULD NOT BE VERIFIED. "
            "SKIPPING TRANSACTION ID-V000000000000002") in out
    # card on file but account missing
    assert "INVALID ACCOUNT NUMBER FOUND" in out
    assert "ACCOUNT 00000000404 NOT FOUND" in out
    assert sum(line.startswith("V0000000000000") for line in out) == 3


def test_empty_daily_file(cobol, tmp_path):
    result = run_verify(cobol, tmp_path, [])
    assert result.returncode == 0
    assert "SUCCESSFUL READ OF XREF" not in result.lines


def test_missing_daily_file_abends(cobol, tmp_path):
    result = cobol.run("CBTRN01C", {"DALYTRAN": tmp_path / "missing.dat"})
    assert result.abended
    assert "ERROR OPENING DAILY TRANSACTION FILE" in result.lines
    assert "FILE STATUS IS: NNNN0035" in result.lines


def test_missing_customer_file_abends(cobol, tmp_path):
    daily = Cobol.write_sequential(tmp_path / "daily.dat", [], TRAN.rec_len)
    result = cobol.run("CBTRN01C", {"DALYTRAN": daily, "CUSTFILE": tmp_path / "missing.dat"})
    assert result.abended
    assert "ERROR OPENING CUSTOMER FILE" in result.lines
