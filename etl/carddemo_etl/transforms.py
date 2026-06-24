"""Value-level transformations for COBOL -> modern types.

These helpers encapsulate the tricky parts of mainframe data conversion that
the data mapping flagged as migration risks: EBCDIC decoding, zoned-decimal
overpunch signs, implied decimal points, and 26-char timestamp parsing.
"""

from __future__ import annotations

from datetime import datetime
from decimal import Decimal

# Zoned-decimal overpunch sign encoding (ASCII / common code pages).
# The final byte of a signed DISPLAY numeric encodes both the last digit and
# the sign of the whole number.
_POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
_NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}

# Timestamp formats seen in TRAN-ORIG-TS / TRAN-PROC-TS (26 chars).
_TS_FORMATS = (
    "%Y-%m-%d %H:%M:%S.%f",
    "%Y-%m-%d-%H.%M.%S.%f",  # mainframe CICS ABSTIME style
    "%Y-%m-%d %H:%M:%S",
)


def decode_ebcdic(raw: bytes, codepage: str = "cp037") -> str:
    """Decode EBCDIC bytes to a Python ``str`` (UTF-8 capable)."""
    return raw.decode(codepage)


def clean_text(value: str) -> str | None:
    """Strip COBOL right-padding; return ``None`` for an all-blank field."""
    stripped = value.rstrip()
    return stripped or None


def parse_numeric_string(value: str) -> str | None:
    """Normalise an unsigned DISPLAY numeric (e.g. an ID/code).

    Leading zeros are preserved as part of the canonical string id but a fully
    blank field becomes ``None``.
    """
    stripped = value.strip()
    return stripped or None


def parse_int(value: str) -> int | None:
    """Parse an unsigned DISPLAY numeric used arithmetically."""
    stripped = value.strip()
    if not stripped:
        return None
    return int(stripped)


def parse_signed_decimal(value: str, decimals: int) -> Decimal:
    """Parse a zoned-decimal ``S9(n)V9(m)`` DISPLAY value.

    The implied decimal point (``V``) is applied using ``decimals`` and the
    overpunch sign on the final byte is decoded.
    """
    if not value:
        raise ValueError("empty signed-decimal field")

    digits = value[:-1]
    last = value[-1]
    sign = 1

    if last in _POSITIVE_OVERPUNCH:
        digits += _POSITIVE_OVERPUNCH[last]
    elif last in _NEGATIVE_OVERPUNCH:
        digits += _NEGATIVE_OVERPUNCH[last]
        sign = -1
    elif last.isdigit():
        # No overpunch -> treat as unsigned positive.
        digits += last
    else:
        raise ValueError(f"invalid overpunch byte: {last!r}")

    digits = digits.strip() or "0"
    if not digits.isdigit():
        raise ValueError(f"non-numeric signed-decimal payload: {value!r}")

    if decimals == 0:
        return Decimal(sign * int(digits))

    # Insert the implied decimal point so the result keeps its exact scale,
    # e.g. "00000091900" with 2 decimals -> Decimal("919.00").
    padded = digits.rjust(decimals + 1, "0")
    int_part, frac_part = padded[:-decimals], padded[-decimals:]
    text = f"{'-' if sign < 0 else ''}{int_part}.{frac_part}"
    return Decimal(text)


def parse_timestamp(value: str) -> datetime | None:
    """Parse a 26-char timestamp field; blank/all-zero placeholder -> ``None``."""
    stripped = value.strip()
    # Blank, or a structural placeholder like "0000-00-00 00:00:00.000000".
    if not stripped or not stripped.strip("0-:. "):
        return None
    for fmt in _TS_FORMATS:
        try:
            return datetime.strptime(stripped, fmt)
        except ValueError:
            continue
    raise ValueError(f"unparseable timestamp: {value!r}")


def mask_card_number(card: str) -> str | None:
    """Mask a PAN for non-production sinks, keeping the last 4 digits."""
    stripped = card.strip()
    if not stripped:
        return None
    if len(stripped) <= 4:
        return stripped
    return ("*" * (len(stripped) - 4)) + stripped[-4:]
