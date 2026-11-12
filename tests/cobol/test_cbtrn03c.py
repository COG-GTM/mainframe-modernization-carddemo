"""CBTRN03C: transaction detail report (TRANREPT job)."""
from decimal import Decimal

from harness.builders import tran, xref
from harness.cobol import Cobol
from harness.fixtures import ascii_records
from harness.records import TRAN, TRANCATG, TRANTYPE, XREF

REPORT_LEN = 133
CARD_A, CARD_B = "4000000000000001", "4000000000000002"
DASHES = "-" * REPORT_LEN


def run_report(cobol, tmp_path, trans, start="2024-01-01", end="2024-12-31", xrefs=None,
               types=None, cats=None):
    xrefs = xrefs if xrefs is not None else [xref(CARD_A, 11), xref(CARD_B, 22)]
    types = types if types is not None else ascii_records("trantype.txt", TRANTYPE.rec_len)
    cats = cats if cats is not None else ascii_records("trancatg.txt", TRANCATG.rec_len)
    dds = {
        "TRANFILE": Cobol.write_sequential(tmp_path / "trans.dat", trans, TRAN.rec_len),
        "TRANREPT": tmp_path / "report.dat",
        "CARDXREF": cobol.load_indexed(tmp_path / "xref.dat", XREF, xrefs),
        "TRANTYPE": cobol.load_indexed(tmp_path / "trantype.dat", TRANTYPE, types),
        "TRANCATG": cobol.load_indexed(tmp_path / "trancatg.dat", TRANCATG, cats),
        "DATEPARM": Cobol.write_sequential(tmp_path / "dateparm.dat",
                                           [f"{start} {end}".ljust(80)], 80),
    }
    result = cobol.run("CBTRN03C", dds)
    report = Cobol.read_sequential(dds["TRANREPT"], REPORT_LEN) if dds["TRANREPT"].exists() else []
    return result, [line.rstrip() for line in report]


def edited(amount, plus):
    """-ZZZ,ZZZ,ZZZ.ZZ / +ZZZ,ZZZ,ZZZ.ZZ: a fixed sign position, then zero suppression."""
    value = Decimal(amount)
    sign = "-" if value < 0 else ("+" if plus else " ")
    return sign + f"{abs(value):,.2f}".rjust(14)


def detail(tran_id, acct, type_cd, type_desc, cat, cat_desc, source, amount):
    return (f"{tran_id:<16} {acct:011d} {type_cd}-{type_desc:<15.15} {cat:04d}-"
            f"{cat_desc:<29.29} {source:<10}    {edited(amount, False)}")


def total(label, amount):
    width = 13 if label == "Account Total" else 11
    return label.ljust(width) + "." * (97 - width) + edited(amount, True)


def ts(day):
    return f"2024-03-{day:02d}-10.00.00.000000"


def test_report_layout_headers_details_and_totals(cobol, tmp_path):
    trans = [tran("T000000000000001", CARD_A, "10.00", type_cd="01", cat_cd=1, proc_ts=ts(1)),
             tran("T000000000000002", CARD_A, "-2.50", type_cd="03", cat_cd=1, proc_ts=ts(2)),
             tran("T000000000000003", CARD_B, "1234.56", type_cd="01", cat_cd=2, proc_ts=ts(3))]
    result, report = run_report(cobol, tmp_path, trans)

    assert result.returncode == 0, result.stdout
    assert "Reporting from 2024-01-01 to 2024-12-31" in result.lines
    assert report[0] == ("DALYREPT".ljust(38) + "Daily Transaction Report".ljust(41)
                         + "Date Range: 2024-01-01 to 2024-12-31")
    assert report[1] == ""
    assert report[2].startswith("Transaction ID   Account ID  Transaction Type   Tran Category")
    assert report[3] == DASHES
    assert report[4] == detail("T000000000000001", 11, "01", "Purchase", 1,
                               "Regular Sales Draft", "POS TERM", "10.00")
    assert report[5] == detail("T000000000000002", 11, "03", "Credit", 1,
                               "Credit to Account", "POS TERM", "-2.50")
    # card change closes the previous account
    assert report[6] == total("Account Total", "7.50")
    assert report[7] == DASHES
    assert report[8] == detail("T000000000000003", 22, "01", "Purchase", 2,
                               "Regular Cash Advance", "POS TERM", "1234.56")
    # at EOF the last record's amount is added a second time and the final
    # account total is never written (existing CBTRN03C behaviour)
    assert report[9:] == [total("Page Total", "2476.62"), DASHES,
                          total("Grand Total", "2476.62")]


def test_page_break_every_twenty_lines(cobol, tmp_path):
    trans = [tran(f"P{n:015d}", CARD_A, "1.00", proc_ts=ts(1)) for n in range(1, 21)]
    result, report = run_report(cobol, tmp_path, trans)

    assert result.returncode == 0
    page_totals = [i for i, line in enumerate(report) if line.startswith("Page Total")]
    # 4 header lines + 16 details fill page one; then page total, dash line and new headers
    assert page_totals[0] == 20
    assert report[20] == total("Page Total", "16.00")
    assert report[21] == DASHES
    assert report[22].startswith("DALYREPT")
    assert sum(1 for line in report if line.startswith("P0")) == 20
    assert report[-1] == total("Grand Total", "21.00")


def test_out_of_range_record_ends_processing(cobol, tmp_path):
    # NEXT SENTENCE leaves the main PERFORM loop: later records and totals are skipped
    trans = [tran("D000000000000001", CARD_A, "5.00", proc_ts=ts(1)),
             tran("D000000000000002", CARD_A, "6.00", proc_ts="2023-12-31-23.59.59.000000"),
             tran("D000000000000003", CARD_A, "7.00", proc_ts=ts(3))]
    result, report = run_report(cobol, tmp_path, trans)

    assert result.returncode == 0
    assert [line[:16] for line in report if line.startswith("D0")] == ["D000000000000001"]
    assert not any(line.startswith(("Page Total", "Grand Total")) for line in report)
    assert result.lines[-1] == "END OF EXECUTION OF PROGRAM CBTRN03C"


def test_date_range_is_inclusive(cobol, tmp_path):
    trans = [tran("E000000000000001", CARD_A, "1.00", proc_ts="2024-03-01-00.00.00.000000"),
             tran("E000000000000002", CARD_A, "2.00", proc_ts="2024-03-31-23.59.59.999999")]
    _, report = run_report(cobol, tmp_path, trans, start="2024-03-01", end="2024-03-31")
    assert [line[:16] for line in report if line.startswith("E0")] == \
        ["E000000000000001", "E000000000000002"]


def test_empty_transaction_file_writes_empty_report(cobol, tmp_path):
    result, report = run_report(cobol, tmp_path, [])
    assert result.returncode == 0
    assert report == []


def test_empty_dateparm_file_skips_processing(cobol, tmp_path):
    dds = {
        "TRANFILE": Cobol.write_sequential(tmp_path / "trans.dat",
                                           [tran("F000000000000001", CARD_A, "1.00",
                                                 proc_ts=ts(1))], TRAN.rec_len),
        "TRANREPT": tmp_path / "report.dat",
        "CARDXREF": cobol.load_indexed(tmp_path / "xref.dat", XREF, [xref(CARD_A, 1)]),
        "TRANTYPE": cobol.load_indexed(tmp_path / "tt.dat", TRANTYPE, []),
        "TRANCATG": cobol.load_indexed(tmp_path / "tc.dat", TRANCATG, []),
        "DATEPARM": Cobol.write_sequential(tmp_path / "dateparm.dat", [], 80),
    }
    result = cobol.run("CBTRN03C", dds)
    assert result.returncode == 0
    assert not any(line.startswith("Reporting from") for line in result.lines)
    assert (tmp_path / "report.dat").read_bytes() == b""


def test_unknown_card_abends(cobol, tmp_path):
    trans = [tran("G000000000000001", "4999999999999999", "1.00", proc_ts=ts(1))]
    result, _ = run_report(cobol, tmp_path, trans)
    assert result.abended
    assert "INVALID CARD NUMBER : 4999999999999999" in result.lines
    assert "FILE STATUS IS: NNNN0023" in result.lines


def test_unknown_transaction_type_abends(cobol, tmp_path):
    trans = [tran("G000000000000002", CARD_A, "1.00", type_cd="99", proc_ts=ts(1))]
    result, _ = run_report(cobol, tmp_path, trans)
    assert result.abended
    assert "INVALID TRANSACTION TYPE : 99" in result.lines


def test_unknown_category_abends(cobol, tmp_path):
    trans = [tran("G000000000000003", CARD_A, "1.00", type_cd="01", cat_cd=9999, proc_ts=ts(1))]
    result, _ = run_report(cobol, tmp_path, trans)
    assert result.abended
    assert "INVALID TRAN CATG KEY : 019999" in result.lines


def test_missing_transaction_file_abends(cobol, tmp_path):
    result = cobol.run("CBTRN03C", {"TRANFILE": tmp_path / "missing.dat",
                                    "TRANREPT": tmp_path / "r.dat"})
    assert result.abended
    assert "FILE STATUS IS: NNNN0035" in result.lines
