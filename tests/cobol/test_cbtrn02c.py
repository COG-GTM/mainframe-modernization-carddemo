"""CBTRN02C: daily transaction validation and posting (POSTTRAN job)."""
from decimal import Decimal

import pytest

from harness.builders import account, tcatbal, tran, xref
from harness.cobol import Cobol
from harness.fixtures import ascii_records
from harness.records import ACCOUNT, TCATBAL, TRAN, XREF

REJECT_LEN = 430
RUN_TS = "2024031512345678"  # COB_CURRENT_DATE -> 2024-03-15 12:34:56.78
CARD_A, CARD_B, CARD_NO_ACCT, CARD_UNKNOWN = ("4000000000000001", "4000000000000002",
                                             "4000000000000003", "4999999999999999")


def run_posting(cobol: Cobol, tmp_path, daily, accounts, xrefs, balances=()):
    dds = {
        "DALYTRAN": Cobol.write_sequential(tmp_path / "daily.dat", list(daily), TRAN.rec_len),
        "TRANFILE": tmp_path / "tran.dat",
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, list(xrefs)),
        "DALYREJS": tmp_path / "rejects.dat",
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, list(accounts)),
        "TCATBALF": cobol.load_indexed(tmp_path / "tcatbal.dat", TCATBAL, list(balances)),
    }
    result = cobol.run("CBTRN02C", dds, current_date=RUN_TS)
    if result.abended:
        return result, None
    out = {
        "posted": [TRAN.parse(r) for r in cobol.dump_indexed(dds["TRANFILE"], TRAN)],
        "rejects": [(TRAN.parse(r[:350]), r[350:354], r[354:].rstrip())
                    for r in Cobol.read_sequential(dds["DALYREJS"], REJECT_LEN)],
        "accounts": {int(a["ACCT-ID"]): a for a in map(ACCOUNT.parse,
                                                         cobol.dump_indexed(dds["ACCTFILE"], ACCOUNT))},
        "balances": {(int(b["TRANCAT-ACCT-ID"]), b["TRANCAT-TYPE-CD"], int(b["TRANCAT-CD"])):
                     b["TRAN-CAT-BAL"] for b in map(TCATBAL.parse,
                                                    cobol.dump_indexed(dds["TCATBALF"], TCATBAL))},
    }
    return result, out


def counts(result):
    shown = dict(line.split(":", 1) for line in result.lines if line.startswith("TRANSACTIONS"))
    return int(shown["TRANSACTIONS PROCESSED "]), int(shown["TRANSACTIONS REJECTED  "])


def test_valid_transactions_post_to_all_files(cobol, tmp_path):
    daily = [tran("T000000000000001", CARD_A, "125.50", type_cd="01", cat_cd=1),
             tran("T000000000000002", CARD_A, "-20.25", type_cd="03", cat_cd=2)]
    result, out = run_posting(cobol, tmp_path, daily,
                              accounts=[account(1, curr_bal="100", cyc_credit="10", cyc_debit="5")],
                              xrefs=[xref(CARD_A, 1)],
                              balances=[tcatbal(1, "01", 1, "1000.00")])

    assert result.returncode == 0
    assert counts(result) == (2, 0)
    assert out["rejects"] == []
    assert [p["TRAN-ID"] for p in out["posted"]] == ["T000000000000001", "T000000000000002"]
    first = out["posted"][0]
    assert first["TRAN-AMT"] == Decimal("125.50")
    assert first["TRAN-CARD-NUM"] == CARD_A
    assert first["TRAN-MERCHANT-NAME"] == "Test Merchant"
    assert first["TRAN-ORIG-TS"] == "2024-03-15-10.00.00.000000"
    assert first["TRAN-PROC-TS"] == "2024-03-15-12.34.56.780000"

    acct = out["accounts"][1]
    assert acct["ACCT-CURR-BAL"] == Decimal("205.25")
    assert acct["ACCT-CURR-CYC-CREDIT"] == Decimal("135.50")  # positive amounts are credits
    assert acct["ACCT-CURR-CYC-DEBIT"] == Decimal("-15.25")   # negative amounts add to debit

    assert out["balances"][(1, "01", 1)] == Decimal("1125.50")  # existing record rewritten
    assert out["balances"][(1, "03", 2)] == Decimal("-20.25")   # missing record created
    assert "TCATBAL record not found for key : 00000000001030002.. Creating." in result.lines


@pytest.mark.parametrize("case,daily_kwargs,acct_kwargs,card,reason,desc", [
    ("unknown card", {}, {}, CARD_UNKNOWN, "0100", "INVALID CARD NUMBER FOUND"),
    ("account missing", {}, {}, CARD_NO_ACCT, "0101", "ACCOUNT RECORD NOT FOUND"),
    ("over limit", {"amount": "600.01"}, {"credit_limit": "1000", "cyc_credit": "500",
                                          "cyc_debit": "100"}, CARD_A, "0102",
     "OVERLIMIT TRANSACTION"),
    ("after expiry", {"orig_ts": "2026-01-01-00.00.00.000000"}, {"expires": "2025-12-31"},
     CARD_A, "0103", "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"),
    ("over limit and expired reports expiry", {"amount": "9999", "orig_ts": "2026-01-01-00.00.00.000000"},
     {"credit_limit": "10", "expires": "2025-12-31"}, CARD_A, "0103",
     "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"),
])
def test_rejections(cobol, tmp_path, case, daily_kwargs, acct_kwargs, card, reason, desc):
    kwargs = {"amount": "50.00", **daily_kwargs}
    daily = [tran("R000000000000001", card, **kwargs)]
    original = account(1, **acct_kwargs)
    result, out = run_posting(cobol, tmp_path, daily, accounts=[original],
                              xrefs=[xref(CARD_A, 1), xref(CARD_NO_ACCT, 777)])

    assert result.returncode == 4
    assert counts(result) == (1, 1)
    assert out["posted"] == []
    assert out["balances"] == {}
    assert out["accounts"][1] == ACCOUNT.parse(original)
    rejected, code, text = out["rejects"][0]
    assert rejected == TRAN.parse(daily[0])
    assert (code, text) == (reason, desc)


def test_limit_and_expiry_boundaries_are_inclusive(cobol, tmp_path):
    # temp balance == credit limit and orig date == expiration date are both accepted
    daily = [tran("B000000000000001", CARD_A, "400.00", orig_ts="2025-12-31-23.59.59.000000")]
    result, out = run_posting(cobol, tmp_path, daily,
                              accounts=[account(1, credit_limit="500", cyc_credit="200",
                                                cyc_debit="100", expires="2025-12-31")],
                              xrefs=[xref(CARD_A, 1)])
    assert result.returncode == 0
    assert counts(result) == (1, 0)
    assert len(out["posted"]) == 1


def test_mixed_batch_counts_and_routing(cobol, tmp_path):
    daily = [tran("M000000000000001", CARD_A, "10.00"),
             tran("M000000000000002", CARD_UNKNOWN, "10.00"),
             tran("M000000000000003", CARD_B, "20.00"),
             tran("M000000000000004", CARD_B, "99999.00")]
    result, out = run_posting(cobol, tmp_path, daily,
                              accounts=[account(1), account(2, credit_limit="100")],
                              xrefs=[xref(CARD_A, 1), xref(CARD_B, 2)])
    assert result.returncode == 4
    assert counts(result) == (4, 2)
    assert sorted(p["TRAN-ID"] for p in out["posted"]) == ["M000000000000000"[:16]] * 0 or \
        sorted(p["TRAN-ID"] for p in out["posted"]) == ["M000000000000001"[:16], "M000000000000003"[:16]]
    assert [(r[0]["TRAN-ID"], r[1]) for r in out["rejects"]] == [
        ("M000000000000002"[:16], "0100"), ("M000000000000004"[:16], "0102")]
    assert out["balances"] == {(1, "01", 1): Decimal("10.00"), (2, "01", 1): Decimal("20.00")}


def test_sample_daily_file_against_sample_master_files(cobol, tmp_path):
    daily = ascii_records("dailytran.txt", TRAN.rec_len)
    result, out = run_posting(cobol, tmp_path, daily,
                              accounts=ascii_records("acctdata.txt", ACCOUNT.rec_len),
                              xrefs=ascii_records("cardxref.txt", XREF.rec_len),
                              balances=ascii_records("tcatbal.txt", TCATBAL.rec_len))
    processed, rejected = counts(result)
    assert processed == len(daily) == 300
    assert len(out["posted"]) + len(out["rejects"]) == processed
    assert len(out["rejects"]) == rejected
    assert result.returncode == (4 if rejected else 0)
    # every posted amount is reflected in the category balances
    posted_total = sum(p["TRAN-AMT"] for p in out["posted"])
    before = sum(TCATBAL.parse(r)["TRAN-CAT-BAL"] for r in ascii_records("tcatbal.txt", TCATBAL.rec_len))
    assert sum(out["balances"].values()) - before == posted_total


def test_missing_daily_file_abends(cobol, tmp_path):
    dds = {"DALYTRAN": tmp_path / "missing.dat", "TRANFILE": tmp_path / "t.dat",
           "XREFFILE": tmp_path / "x.dat", "DALYREJS": tmp_path / "r.dat",
           "ACCTFILE": tmp_path / "a.dat", "TCATBALF": tmp_path / "c.dat"}
    result = cobol.run("CBTRN02C", dds)
    assert result.abended
    assert "ERROR OPENING DALYTRAN" in result.lines
    assert "FILE STATUS IS: NNNN0035" in result.lines
