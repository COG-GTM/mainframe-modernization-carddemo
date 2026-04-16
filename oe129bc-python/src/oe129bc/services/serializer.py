"""Transaction record serializer — converts TransactionRecord objects back to bytes.

Supports both EBCDIC (cp037) and ASCII encoding for round-trip compatibility
with the COBOL TRAN-RECORD layout (CVTRA05Y.cpy, 350 bytes).
"""

from decimal import Decimal

from oe129bc.models.transaction import RECORD_LENGTH, TRAN_RECORD_FIELDS, TransactionRecord
from oe129bc.services.parser import EBCDIC_CODEC


class SerializeError(Exception):
    """Raised when a transaction record cannot be serialized."""

    def __init__(self, message: str, field: str | None = None) -> None:
        self.field = field
        super().__init__(message)


def _encode_zoned_decimal_ebcdic(value: Decimal, length: int = 11, decimal_digits: int = 2) -> bytes:
    """Encode a Decimal value as EBCDIC zoned decimal (display format).

    PIC S9(09)V99: 11 bytes, 9 integer digits + 2 fractional digits.
    Each byte = zone nibble (0xF for non-last) + digit nibble.
    Last byte zone: 0xC = positive, 0xD = negative.
    """
    integer_digits = length - decimal_digits
    is_negative = value < 0
    abs_value = abs(value)

    # Split into integer and fractional parts
    int_part = int(abs_value)
    frac_part = abs(abs_value - int_part)

    # Format the digit string
    int_str = str(int_part).zfill(integer_digits)
    if len(int_str) > integer_digits:
        raise SerializeError(
            f"Integer part '{int_str}' exceeds {integer_digits} digits",
            field="tran_amt",
        )

    # Get fractional digits by multiplying and truncating
    frac_int = int(round(frac_part * (10**decimal_digits)))
    frac_str = str(frac_int).zfill(decimal_digits)
    if len(frac_str) > decimal_digits:
        frac_str = frac_str[:decimal_digits]

    digit_str = int_str + frac_str

    # Encode each digit as zoned decimal byte
    result = bytearray(length)
    for i in range(length):
        digit = int(digit_str[i])
        if i < length - 1:
            result[i] = 0xF0 | digit  # Zone = 0xF
        else:
            # Last byte: sign in zone nibble
            sign_zone = 0xD0 if is_negative else 0xC0
            result[i] = sign_zone | digit

    return bytes(result)


def _encode_zoned_decimal_ascii(value: Decimal, length: int = 11, decimal_digits: int = 2) -> bytes:
    """Encode a Decimal value as ASCII zoned decimal with trailing overpunch.

    Uses the standard mainframe-to-ASCII overpunch convention:
        Positive: '{' = 0, 'A'-'I' = 1-9
        Negative: '}' = 0, 'J'-'R' = 1-9
    """
    integer_digits = length - decimal_digits
    is_negative = value < 0
    abs_value = abs(value)

    int_part = int(abs_value)
    frac_part = abs(abs_value - int_part)

    int_str = str(int_part).zfill(integer_digits)
    if len(int_str) > integer_digits:
        raise SerializeError(
            f"Integer part '{int_str}' exceeds {integer_digits} digits",
            field="tran_amt",
        )

    frac_int = int(round(frac_part * (10**decimal_digits)))
    frac_str = str(frac_int).zfill(decimal_digits)
    if len(frac_str) > decimal_digits:
        frac_str = frac_str[:decimal_digits]

    digit_str = int_str + frac_str

    overpunch_positive = "{ABCDEFGHI"
    overpunch_negative = "}JKLMNOPQR"

    last_digit = int(digit_str[-1])
    if is_negative:
        last_char = overpunch_negative[last_digit]
    else:
        last_char = overpunch_positive[last_digit]

    return (digit_str[:-1] + last_char).encode("ascii")


def encode_comp3(value: Decimal, integer_digits: int = 9, decimal_digits: int = 2) -> bytes:
    """Encode a Decimal value as COMP-3 (packed decimal).

    For PIC S9(09)V99: 11 digits + sign = 12 nibbles = 6 bytes.
    Sign nibble: 0xC = positive, 0xD = negative.
    """
    is_negative = value < 0
    abs_value = abs(value)

    int_part = int(abs_value)
    frac_part = abs(abs_value - int_part)

    int_str = str(int_part).zfill(integer_digits)
    if len(int_str) > integer_digits:
        raise SerializeError(
            f"Integer part '{int_str}' exceeds {integer_digits} digits",
            field="tran_amt",
        )

    frac_int = int(round(frac_part * (10**decimal_digits)))
    frac_str = str(frac_int).zfill(decimal_digits)
    if len(frac_str) > decimal_digits:
        frac_str = frac_str[:decimal_digits]

    digit_str = int_str + frac_str
    sign_nibble = 0x0D if is_negative else 0x0C

    # Build nibble list: digits + sign
    nibbles = [int(d) for d in digit_str]
    nibbles.append(sign_nibble)

    # Pad to even number of nibbles
    if len(nibbles) % 2 != 0:
        nibbles.insert(0, 0)

    # Pack into bytes
    result = bytearray()
    for i in range(0, len(nibbles), 2):
        byte_val = (nibbles[i] << 4) | nibbles[i + 1]
        result.append(byte_val)

    return bytes(result)


def _pad_text(value: str, length: int, pad_char: str = " ") -> str:
    """Left-justify a text value and pad to exact field length."""
    return value.ljust(length, pad_char)[:length]


def _pad_numeric(value: str, length: int) -> str:
    """Right-justify a numeric value with leading zeros to exact field length."""
    return value.zfill(length)[:length]


def serialize_transaction(record: TransactionRecord, encoding: str = "ascii") -> bytes:
    """Serialize a TransactionRecord to a 350-byte fixed-length record.

    Args:
        record: The TransactionRecord to serialize.
        encoding: Output encoding ("ebcdic" or "ascii"). Defaults to "ascii".

    Returns:
        Exactly 350 bytes representing the fixed-length record.

    Raises:
        SerializeError: If the record cannot be serialized.
    """
    is_ebcdic = encoding.lower() == "ebcdic"
    buffer = bytearray(RECORD_LENGTH)

    for spec in TRAN_RECORD_FIELDS:
        value = getattr(record, spec.name)

        if spec.field_type == "signed_decimal":
            if is_ebcdic:
                encoded = _encode_zoned_decimal_ebcdic(value, spec.length)
            else:
                encoded = _encode_zoned_decimal_ascii(value, spec.length)
            buffer[spec.offset : spec.offset + spec.length] = encoded

        elif spec.field_type == "numeric":
            padded = _pad_numeric(str(value), spec.length)
            if is_ebcdic:
                encoded = padded.encode(EBCDIC_CODEC)
            else:
                encoded = padded.encode("ascii")
            buffer[spec.offset : spec.offset + spec.length] = encoded

        else:
            # text field
            padded = _pad_text(str(value), spec.length)
            if is_ebcdic:
                encoded = padded.encode(EBCDIC_CODEC)
            else:
                encoded = padded.encode("ascii")
            buffer[spec.offset : spec.offset + spec.length] = encoded

    return bytes(buffer)
