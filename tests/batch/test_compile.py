"""Every batch program in app/cbl compiles with GnuCOBOL (IBM dialect).

The online programs (CO*.cbl) contain EXEC CICS and need a CICS translator,
so they are out of scope here.
"""
import subprocess

import pytest

from harness.cobol import APP_CBL, COBC_FLAGS, cobc_available

BATCH = sorted(p for p in APP_CBL.iterdir()
               if p.suffix.lower() == ".cbl" and not p.name.upper().startswith("CO"))
KNOWN_FAILURES = {
    "CBSTM03A.CBL": "copybook CUSTREC.cpy is indented with TAB characters, which GnuCOBOL "
                    "does not expand in fixed-format source",
}

pytestmark = pytest.mark.skipif(not cobc_available(), reason="GnuCOBOL (cobc) is not installed")


@pytest.mark.parametrize("source", [
    pytest.param(p, id=p.name, marks=[pytest.mark.xfail(reason=KNOWN_FAILURES[p.name], strict=True)]
                 if p.name in KNOWN_FAILURES else [])
    for p in BATCH])
def test_batch_program_compiles(source):
    proc = subprocess.run(["cobc", "-fsyntax-only", *COBC_FLAGS, str(source)],
                          capture_output=True, text=True)
    assert proc.returncode == 0, proc.stdout + proc.stderr


def test_all_batch_programs_are_listed():
    assert {p.stem.upper() for p in BATCH} >= {
        "CBACT01C", "CBACT02C", "CBACT03C", "CBACT04C", "CBCUS01C", "CBSTM03A", "CBSTM03B",
        "CBTRN01C", "CBTRN02C", "CBTRN03C", "CSUTLDTC"}
