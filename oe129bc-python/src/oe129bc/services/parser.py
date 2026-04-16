"""Transaction record parser — converts raw bytes into TransactionRecord objects.

Supports both EBCDIC (cp037) and ASCII-encoded fixed-length 350-byte records.
Handles COBOL PIC S9(09)V99 signed decimal in both zoned-decimal (display)
and COMP-3 (packed decimal) formats.
"""

from decimal import Decimal, InvalidOperation

from oe129bc.models.transaction import RECORD_LENGTH, TRAN_RECORD_FIELDS, TransactionRecord

# EBCDIC codepage for US (IBM037)
EBCDIC_CODEC = "cp037"


class ParseError(Exception):
    """Raised when a transaction record cannot be parsed."""

    def __init__(self, message: str, offset: int | None = None, field: str | None = None) -> None:
        self.offset = offset
        self.field = field
        super().__init__(message)


def _decode_zoned_decimal_ebcdic(raw: bytes) -> Decimal:
    """Decode an EBCDIC zoned-decimal (display format) field to Decimal.

    PIC S9(09)V99 in EBCDIC display format: 11 bytes.
    Each byte = zone nibble (high) + digit nibble (low).
    Sign is in the zone nibble of the last byte:
        0xC, 0xA, 0xE, 0xF = positive
        0xD, 0xB = negative
    The implied decimal point (V) is between positions 9 and 10 (9 integer, 2 fractional).
    """
    if len(raw) != 11:
        raise ParseError(f"Zoned decimal field must be 11 bytes, got {len(raw)}", field="tran_amt")

    digits = []
    for i, byte_val in enumerate(raw):
        zone = (byte_val >> 4) & 0x0F
        digit = byte_val & 0x0F

        if digit > 9:
            raise ParseError(
                f"Invalid digit nibble 0x{digit:X} at byte {i} in zoned decimal",
                offset=i,
                field="tran_amt",
            )

        if i < len(raw) - 1:
            # Non-last bytes: zone should be 0xF (unsigned numeric)
            if zone != 0x0F:
                raise ParseError(
                    f"Invalid zone nibble 0x{zone:X} at byte {i} (expected 0xF)",
                    offset=i,
                    field="tran_amt",
                )

        digits.append(str(digit))

    # Determine sign from last byte's zone nibble
    last_zone = (raw[-1] >> 4) & 0x0F
    is_negative = last_zone in (0x0D, 0x0B)

    # Build the decimal value: 9 integer digits + 2 fractional digits
    digit_str = "".join(digits)
    integer_part = digit_str[:9]
    fractional_part = digit_str[9:]
    sign = "-" if is_negative else ""

    return Decimal(f"{sign}{integer_part}.{fractional_part}")


def _decode_zoned_decimal_ascii(raw: bytes) -> Decimal:
    """Decode an ASCII representation of a signed decimal field.

    For ASCII records, PIC S9(09)V99 is stored as 11 ASCII digit characters.
    Sign convention: the last byte encodes the sign using the mainframe
    overpunch convention translated to ASCII:
        '{' = +0, 'A'-'I' = +1 to +9
        '}' = -0, 'J'-'R' = -1 to -9
    If the field is plain digits (no overpunch), it's treated as unsigned positive.
    """
    if len(raw) != 11:
        raise ParseError(f"Signed decimal field must be 11 bytes, got {len(raw)}", field="tran_amt")

    text = raw.decode("ascii", errors="replace")

    # Check for leading sign convention (+/- prefix in first char)
    if len(text) >= 1 and text[0] in "+-":
        sign = "-" if text[0] == "-" else ""
        digit_str = text[1:]
        if not digit_str.isdigit() or len(digit_str) != 10:
            raise ParseError(f"Invalid signed decimal (leading sign): '{text}'", field="tran_amt")
        integer_part = digit_str[:8]
        fractional_part = digit_str[8:]
        return Decimal(f"{sign}{integer_part}.{fractional_part}")

    # Check for trailing overpunch sign
    overpunch_positive = {
        "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
        "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
    }
    overpunch_negative = {
        "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
        "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
    }

    last_char = text[-1]
    prefix_digits = text[:-1]

    if last_char in overpunch_positive:
        if not prefix_digits.isdigit():
            raise ParseError(f"Invalid digits before overpunch: '{text}'", field="tran_amt")
        digit_str = prefix_digits + overpunch_positive[last_char]
        sign = ""
    elif last_char in overpunch_negative:
        if not prefix_digits.isdigit():
            raise ParseError(f"Invalid digits before overpunch: '{text}'", field="tran_amt")
        digit_str = prefix_digits + overpunch_negative[last_char]
        sign = "-"
    elif text.isdigit():
        # All digits, unsigned positive
        digit_str = text
        sign = ""
    else:
        raise ParseError(f"Cannot decode signed decimal field: '{text}'", field="tran_amt")

    integer_part = digit_str[:9]
    fractional_part = digit_str[9:]
    return Decimal(f"{sign}{integer_part}.{fractional_part}")


def decode_comp3(raw: bytes, integer_digits: int = 9, decimal_digits: int = 2) -> Decimal:
    """Decode a COMP-3 (packed decimal) field to Decimal.

    COMP-3 stores two digits per byte with a trailing sign nibble.
    For PIC S9(09)V99: 11 digits + sign = 12 nibbles = 6 bytes.
    Sign nibble: 0xC/0xA/0xE/0xF = positive, 0xD/0xB = negative.
    """
    total_digits = integer_digits + decimal_digits
    expected_len = (total_digits + 2) // 2  # +1 for sign nibble, rounded up

    if len(raw) != expected_len:
        raise ParseError(
            f"COMP-3 field expected {expected_len} bytes, got {len(raw)}",
            field="tran_amt",
        )

    nibbles = []
    for byte_val in raw:
        nibbles.append((byte_val >> 4) & 0x0F)
        nibbles.append(byte_val & 0x0F)

    # Last nibble is the sign
    sign_nibble = nibbles[-1]
    is_negative = sign_nibble in (0x0D, 0x0B)

    # Remaining nibbles are digits
    digit_nibbles = nibbles[:-1]
    # The first nibble may be a leading zero pad if total digits is even
    if len(digit_nibbles) > total_digits:
        digit_nibbles = digit_nibbles[len(digit_nibbles) - total_digits :]

    for i, n in enumerate(digit_nibbles):
        if n > 9:
            raise ParseError(f"Invalid digit nibble 0x{n:X} at position {i} in COMP-3", field="tran_amt")

    digit_str = "".join(str(d) for d in digit_nibbles)

    # Pad to total_digits if needed
    digit_str = digit_str.zfill(total_digits)

    integer_part = digit_str[:integer_digits]
    fractional_part = digit_str[integer_digits:]
    sign = "-" if is_negative else ""

    return Decimal(f"{sign}{integer_part}.{fractional_part}")


def _detect_encoding(data: bytes) -> str:
    """Heuristic to detect if data is EBCDIC or ASCII.

    Checks the first few bytes for common EBCDIC vs ASCII ranges.
    EBCDIC digits are 0xF0-0xF9; ASCII digits are 0x30-0x39.
    EBCDIC spaces are 0x40; ASCII spaces are 0x20.
    """
    if not data:
        return "ascii"

    ebcdic_indicators = 0
    ascii_indicators = 0

    for byte_val in data[:50]:
        if 0xF0 <= byte_val <= 0xF9 or byte_val == 0x40:
            ebcdic_indicators += 1
        if 0x30 <= byte_val <= 0x39 or byte_val == 0x20:
            ascii_indicators += 1

    return "ebcdic" if ebcdic_indicators > ascii_indicators else "ascii"


def parse_transaction(data: bytes, encoding: str | None = None) -> TransactionRecord:
    """Parse a single 350-byte fixed-length record into a TransactionRecord.

    Args:
        data: Raw bytes of exactly 350 bytes.
        encoding: Force encoding ("ebcdic" or "ascii"). Auto-detected if None.

    Returns:
        Parsed TransactionRecord.

    Raises:
        ParseError: If the data cannot be parsed.
    """
    if len(data) != RECORD_LENGTH:
        raise ParseError(f"Record must be exactly {RECORD_LENGTH} bytes, got {len(data)}")

    if encoding is None:
        encoding = _detect_encoding(data)

    is_ebcdic = encoding.lower() == "ebcdic"
    fields: dict[str, str | Decimal] = {}

    for spec in TRAN_RECORD_FIELDS:
        raw = data[spec.offset : spec.offset + spec.length]

        if spec.field_type == "signed_decimal":
            try:
                if is_ebcdic:
                    fields[spec.name] = _decode_zoned_decimal_ebcdic(raw)
                else:
                    fields[spec.name] = _decode_zoned_decimal_ascii(raw)
            except (ParseError, InvalidOperation) as exc:
                raise ParseError(
                    f"Failed to parse field '{spec.name}' at offset {spec.offset}: {exc}",
                    offset=spec.offset,
                    field=spec.name,
                ) from exc
        elif spec.field_type == "numeric":
            if is_ebcdic:
                text = raw.decode(EBCDIC_CODEC).strip()
            else:
                text = raw.decode("ascii", errors="replace").strip()
            fields[spec.name] = text
        else:
            # text field
            if is_ebcdic:
                text = raw.decode(EBCDIC_CODEC).strip()
            else:
                text = raw.decode("ascii", errors="replace").strip()
            fields[spec.name] = text

    return TransactionRecord(**fields)


def parse_transactions(data: bytes, encoding: str | None = None) -> list[TransactionRecord]:
    """Parse multiple concatenated 350-byte records (batch parsing).

    Args:
        data: Raw bytes containing one or more 350-byte records.
        encoding: Force encoding ("ebcdic" or "ascii"). Auto-detected if None.

    Returns:
        List of parsed TransactionRecord objects.

    Raises:
        ParseError: If the data length is not a multiple of 350, or any record fails.
    """
    if len(data) == 0:
        return []

    if len(data) % RECORD_LENGTH != 0:
        raise ParseError(
            f"Batch data length ({len(data)}) is not a multiple of {RECORD_LENGTH}"
        )

    record_count = len(data) // RECORD_LENGTH
    records: list[TransactionRecord] = []

    for i in range(record_count):
        start = i * RECORD_LENGTH
        end = start + RECORD_LENGTH
        try:
            record = parse_transaction(data[start:end], encoding=encoding)
            records.append(record)
        except ParseError as exc:
            raise ParseError(
                f"Failed to parse record {i + 1} at offset {start}: {exc}",
                offset=start,
            ) from exc

    return records
