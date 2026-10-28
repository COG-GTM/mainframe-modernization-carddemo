"""CBACT01C/CBACT02C/CBACT03C/CBCUS01C: sequential KSDS dump utilities."""
import pytest

from harness.fixtures import ascii_records
from harness.records import ACCOUNT, CARD, CUSTOMER, XREF

READERS = [
    # program, DD name, layout, fixture, field label printed per record or the
    # number of times the raw record is DISPLAYed (CBACT03C/CBCUS01C print it
    # both in the read paragraph and in the main loop).
    ("CBACT01C", "ACCTFILE", ACCOUNT, "acctdata.txt", "ACCT-ID"),
    ("CBACT02C", "CARDFILE", CARD, "carddata.txt", 1),
    ("CBACT03C", "XREFFILE", XREF, "cardxref.txt", 2),
    ("CBCUS01C", "CUSTFILE", CUSTOMER, "custdata.txt", 2),
]
IDS = [r[0] for r in READERS]


@pytest.mark.parametrize("program,dd,layout,fixture,label", READERS, ids=IDS)
def test_reads_every_sample_record_in_key_order(cobol, tmp_path, program, dd, layout,
                                                 fixture, label):
    records = ascii_records(fixture, layout.rec_len)
    ksds = cobol.load_indexed(tmp_path / f"{dd}.dat", layout, records)

    result = cobol.run(program, {dd: ksds})

    assert result.returncode == 0, result.stdout + result.stderr
    assert result.lines[0] == f"START OF EXECUTION OF PROGRAM {program}"
    assert result.lines[-1] == f"END OF EXECUTION OF PROGRAM {program}"
    keys = sorted(layout.key(r) for r in records)
    if isinstance(label, str):
        printed = [line.split(":", 1)[1] for line in result.lines if line.startswith(label + " ")]
        assert printed == keys
    else:
        body = [line for line in result.lines[1:-1] if line]
        assert [layout.key(line.ljust(layout.rec_len)) for line in body] == \
            [k for k in keys for _ in range(label)]


def test_cbact01c_formats_account_fields(cobol, tmp_path):
    record = ACCOUNT.build(**{
        "ACCT-ID": 42, "ACCT-ACTIVE-STATUS": "Y", "ACCT-CURR-BAL": "-12.34",
        "ACCT-CREDIT-LIMIT": "5000", "ACCT-CASH-CREDIT-LIMIT": "1500",
        "ACCT-OPEN-DATE": "2020-01-31", "ACCT-EXPIRAION-DATE": "2030-01-31",
        "ACCT-REISSUE-DATE": "2025-01-31", "ACCT-CURR-CYC-CREDIT": "10.5",
        "ACCT-CURR-CYC-DEBIT": "7.25", "ACCT-GROUP-ID": "GOLD"})
    ksds = cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [record])

    result = cobol.run("CBACT01C", {"ACCTFILE": ksds})

    shown = {k.strip(): v for k, _, v in (l.partition(":") for l in result.lines if ":" in l)}
    assert shown["ACCT-ID"] == "00000000042"
    assert shown["ACCT-ACTIVE-STATUS"] == "Y"
    assert shown["ACCT-CURR-BAL"] == "000000001234-"
    assert shown["ACCT-CREDIT-LIMIT"] == "000000500000+"
    assert shown["ACCT-EXPIRAION-DATE"] == "2030-01-31"
    assert shown["ACCT-GROUP-ID"] == "GOLD"


def test_empty_file_prints_no_records(cobol, tmp_path):
    ksds = cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [])
    result = cobol.run("CBACT01C", {"ACCTFILE": ksds})
    assert result.returncode == 0
    assert result.lines == ["START OF EXECUTION OF PROGRAM CBACT01C",
                            "END OF EXECUTION OF PROGRAM CBACT01C"]


@pytest.mark.parametrize("program,dd,layout,fixture,label", READERS, ids=IDS)
def test_missing_dataset_abends_with_file_status(cobol, tmp_path, program, dd, layout,
                                                 fixture, label):
    result = cobol.run(program, {dd: tmp_path / "does-not-exist.dat"})

    assert result.abended
    assert "ABENDING PROGRAM" in result.lines
    assert "FILE STATUS IS: NNNN0035" in result.lines
    assert any(line.startswith("ERROR OPENING") for line in result.lines)
