"""CSUTLDTC: date validation wrapper around the LE service CEEDAYS.

CEEDAYS is replaced by support/CEEDAYS.cbl, which returns the feedback code
selected through CEEDAYS_RESULT, so these tests cover CSUTLDTC's argument
handling and its mapping of every feedback code to a message and return code.

Note: CSUTLDTC moves the whole VSTRING group (2-byte binary length + text)
into the 10-byte TstDate field, so the message carries X'000A' followed by the
first 8 characters of the date. The tests pin that existing behaviour.
"""
import pytest


def validate(cobol, date, fmt, outcome):
    cobol.stub("CEEDAYS")
    result = cobol.run_driver("CSUDRV", ["CSUTLDTC"],
                              {"CSUDRV_DATE": date, "CSUDRV_FORMAT": fmt,
                               "CEEDAYS_RESULT": outcome})
    assert result.returncode == 0, result.stdout + result.stderr
    start = result.stdout.index("RESULT:") + len("RESULT:")
    message = result.stdout[start:start + 80]
    rc = int(result.stdout[start + 80:].split("RC:", 1)[1].split()[0])
    return result.lines, message, rc


@pytest.mark.parametrize("outcome,severity,msg_no,text", [
    ("VALID", "0000", "0000", "Date is valid"),
    ("INSUFFICIENT", "0003", "2507", "Insufficient"),
    ("BAD-DATE-VALUE", "0003", "2508", "Datevalue error"),
    ("INVALID-ERA", "0003", "2509", "Invalid Era"),
    ("UNSUPP-RANGE", "0003", "2513", "Unsupp. Range"),
    ("INVALID-MONTH", "0003", "2517", "Invalid month"),
    ("BAD-PIC-STRING", "0003", "2518", "Bad Pic String"),
    ("NON-NUMERIC", "0003", "2520", "Nonnumeric data"),
    ("YEAR-IN-ERA-0", "0003", "2521", "YearInEra is 0"),
    ("SOMETHING-ELSE", "0004", "2522", "Date is invalid"),
])
def test_feedback_code_mapping(cobol, outcome, severity, msg_no, text):
    _, message, rc = validate(cobol, "2024-02-29", "YYYY-MM-DD", outcome)
    expected = (f"{severity}Mesg Code: {msg_no} {text:<15} TstDate: \x00\n2024-02- "
                f"Mask used:YYYY-MM-DD")
    assert message == expected.ljust(80)
    assert rc == int(severity)


def test_passes_ten_byte_vstrings_to_ceedays(cobol):
    lines, _, _ = validate(cobol, "12/31/2023", "MM/DD/YYYY", "VALID")
    assert "CEEDAYS DATE:+00010:12/31/2023" in lines
    assert "CEEDAYS FMT:+00010:MM/DD/YYYY" in lines
