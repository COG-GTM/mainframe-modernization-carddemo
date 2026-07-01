"""Decode EBCDIC-style signed overpunch used in ASCII COBOL data files.

In mainframe COBOL, PIC S9(...) fields encode the sign of the number
into the last digit using "zoned decimal" / overpunch convention.
When exported to ASCII the last byte is translated as follows:

    Positive:  { 0  A 1  B 2  C 3  D 4  E 5  F 6  G 7  H 8  I 9
    Negative:  } 0  J 1  K 2  L 3  M 4  N 5  O 6  P 7  Q 8  R 9
"""

from decimal import Decimal

_POSITIVE = {"{": 0, "A": 1, "B": 2, "C": 3, "D": 4, "E": 5, "F": 6, "G": 7, "H": 8, "I": 9}
_NEGATIVE = {"}": 0, "J": 1, "K": 2, "L": 3, "M": 4, "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9}


def decode_signed_overpunch(raw: str, decimal_places: int = 0) -> Decimal:
    """Decode an overpunch-encoded numeric string to a ``Decimal``.

    Parameters
    ----------
    raw:
        The raw fixed-width field value (e.g. ``"00000001940{"``).
    decimal_places:
        Number of implied decimal places (``V99`` → 2).

    Returns
    -------
    Decimal with the correct sign and scale.

    Raises
    ------
    ValueError
        If the last character is not a valid overpunch symbol.
    """
    if not raw:
        raise ValueError("Empty overpunch field")

    last_char = raw[-1]
    prefix = raw[:-1]

    if last_char in _POSITIVE:
        sign = 1
        last_digit = _POSITIVE[last_char]
    elif last_char in _NEGATIVE:
        sign = -1
        last_digit = _NEGATIVE[last_char]
    elif last_char.isdigit():
        sign = 1
        last_digit = int(last_char)
    else:
        raise ValueError(f"Invalid overpunch character: {last_char!r}")

    integer_value = int(prefix + str(last_digit))
    result = Decimal(integer_value)

    if decimal_places > 0:
        result = result / (Decimal(10) ** decimal_places)

    return result * sign
