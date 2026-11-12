"""CBSTM03B: file-access subroutine used by the statement job (CREASTMT)."""
from harness.builders import account, customer, tran, xref
from harness.records import ACCOUNT, CUSTOMER, TRNX, XREF

CARD_A = "4000000000000001"


def op(dd, oper, key=""):
    return f"{dd:<8}{oper}{len(key):04d}{key:<25}"


def run_ops(cobol, tmp_path, ops, files=None):
    trnx = [CARD_A + tran("S000000000000001", CARD_A, "1.00")[:330] + "    ",
            CARD_A + tran("S000000000000002", CARD_A, "2.00")[:330] + "    "]
    dds = files if files is not None else {
        "TRNXFILE": cobol.load_indexed(tmp_path / "trnx.dat", TRNX, trnx),
        "XREFFILE": cobol.load_indexed(tmp_path / "xref.dat", XREF, [xref(CARD_A, 7, 3)]),
        "CUSTFILE": cobol.load_indexed(tmp_path / "cust.dat", CUSTOMER, [customer(3)]),
        "ACCTFILE": cobol.load_indexed(tmp_path / "acct.dat", ACCOUNT, [account(7)]),
    }
    (tmp_path / "ops.txt").write_text("".join(o + "\n" for o in ops))
    env = {k: str(v) for k, v in dds.items()}
    env["M03BOPS"] = str(tmp_path / "ops.txt")
    result = cobol.run_driver("M03BDRV", ["CBSTM03B"], env)
    assert result.returncode == 0, result.stdout + result.stderr
    return [tuple(line.split("|", 3)) for line in result.lines]


def test_sequential_reads_and_eof(cobol, tmp_path):
    calls = run_ops(cobol, tmp_path, [op("TRNXFILE", "O"), op("TRNXFILE", "R"),
                                      op("TRNXFILE", "R"), op("TRNXFILE", "R"),
                                      op("TRNXFILE", "C"),
                                      op("XREFFILE", "O"), op("XREFFILE", "R"),
                                      op("XREFFILE", "R"), op("XREFFILE", "C")])
    rc = [c[2] for c in calls]
    assert rc == ["00", "00", "00", "10", "00", "00", "00", "10", "00"]
    assert calls[1][3].startswith(CARD_A + "S000000000000001")
    assert calls[2][3].startswith(CARD_A + "S000000000000002")
    assert calls[6][3] == xref(CARD_A, 7, 3).rstrip()


def test_keyed_reads(cobol, tmp_path):
    calls = run_ops(cobol, tmp_path, [op("CUSTFILE", "O"), op("CUSTFILE", "K", "000000003"),
                                      op("CUSTFILE", "K", "000000009"), op("CUSTFILE", "C"),
                                      op("ACCTFILE", "O"), op("ACCTFILE", "K", "00000000007"),
                                      op("ACCTFILE", "K", "00000000008"), op("ACCTFILE", "C")])
    assert [c[2] for c in calls] == ["00", "00", "23", "00", "00", "00", "23", "00"]
    assert calls[1][3] == customer(3).rstrip()
    assert calls[5][3] == account(7).rstrip()


def test_unsupported_requests(cobol, tmp_path):
    calls = run_ops(cobol, tmp_path, [op("BADDD", "O"), op("ACCTFILE", "O"),
                                      op("ACCTFILE", "W"), op("ACCTFILE", "C")])
    assert calls[0][:3] == ("BADDD   ", "O", "XX")  # unknown DD: area untouched
    # unsupported operation: no I/O, returns the last file status
    assert calls[2][2] == "00" and calls[2][3] == ""


def test_open_missing_file_returns_status(cobol, tmp_path):
    calls = run_ops(cobol, tmp_path, [op("ACCTFILE", "O")],
                    files={"ACCTFILE": tmp_path / "missing.dat"})
    assert calls[0][2] == "35"
