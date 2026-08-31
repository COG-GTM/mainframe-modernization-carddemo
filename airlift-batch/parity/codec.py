#!/usr/bin/env python3
"""Fixed-width record codec driven by the copybook parser.

Byte-level conventions implemented here were confirmed against the GnuCOBOL
build used by this harness (see airlift-batch/docs/ARITHMETIC.md for the
transcript):

* `PIC X(n)`               - left justified, blank padded.
* `PIC 9(n)` DISPLAY       - zero filled, unsigned.
* `PIC S9(n)V99` DISPLAY   - zero filled; the sign is carried in the zone of
  the last digit. In this ASCII runtime a negative value replaces the final
  digit `d` with `chr(0x70 + d)` and a positive value leaves plain digits. On
  an EBCDIC z/OS runtime the same field would carry x'D0'/x'C0' zones; the
  digits and length are identical, the sign byte is not.
* `COMP-3` / PACKED-DECIMAL - two digits per byte, sign nibble C (+) / D (-),
  F when the picture is unsigned.
* `COMP` / BINARY          - big-endian two's complement.
"""

from __future__ import annotations

import sys
from decimal import Decimal
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))

from copybook import Field  # noqa: E402

NEGATIVE_ZONE = 0x70
ASCII_ZERO = ord("0")


class CodecError(Exception):
    pass


def decode_field(field: Field, raw: bytes) -> str | Decimal:
    if not field.numeric:
        return raw.decode("latin-1")
    if field.usage in ("COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL"):
        digits = raw.hex().upper()
        sign_nibble = digits[-1]
        value = digits[:-1]
        negative = sign_nibble == "D"
        if not value.isdigit():
            raise CodecError(f"{field.name}: non-numeric packed value {digits}")
    elif field.usage in ("COMP", "COMPUTATIONAL", "BINARY", "COMP-4", "COMP-5"):
        integer = int.from_bytes(raw, "big", signed=field.signed)
        negative = integer < 0
        value = str(abs(integer)).rjust(field.digits, "0")
    else:
        text = raw.decode("latin-1")
        last = text[-1:]
        negative = False
        if field.signed and last and not last.isdigit():
            code = ord(last)
            if NEGATIVE_ZONE <= code <= NEGATIVE_ZONE + 9:
                negative = True
                text = text[:-1] + chr(ASCII_ZERO + code - NEGATIVE_ZONE)
            else:
                raise CodecError(f"{field.name}: unexpected sign byte {last!r}")
        value = text
        if not value.strip():
            value = "0" * field.digits
        if not value.isdigit():
            raise CodecError(f"{field.name}: non-numeric display value {text!r}")
    scaled = Decimal(value.lstrip("0") or "0").scaleb(-field.scale)
    return -scaled if negative else scaled


def encode_field(field: Field, value: str | Decimal | int) -> bytes:
    if not field.numeric:
        text = str(value)
        if len(text) > field.size:
            raise CodecError(f"{field.name}: {text!r} exceeds {field.size} bytes")
        return text.ljust(field.size).encode("latin-1")
    number = Decimal(value)
    if not field.signed and number < 0:
        raise CodecError(f"{field.name}: negative value in unsigned picture")
    negative = number < 0
    unscaled = int(abs(number).scaleb(field.scale).to_integral_value())
    digits = str(unscaled).rjust(field.digits, "0")
    if len(digits) > field.digits:
        raise CodecError(f"{field.name}: {value} exceeds PIC {field.picture}")
    if field.usage in ("COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL"):
        sign = "F" if not field.signed else ("D" if negative else "C")
        packed = digits.rjust(field.size * 2 - 1, "0") + sign
        return bytes.fromhex(packed)
    if field.usage in ("COMP", "COMPUTATIONAL", "BINARY", "COMP-4", "COMP-5"):
        integer = -unscaled if negative else unscaled
        return integer.to_bytes(field.size, "big", signed=field.signed)
    if negative:
        digits = digits[:-1] + chr(NEGATIVE_ZONE + int(digits[-1]))
    return digits.encode("latin-1")


def decode_record(record: Field, raw: bytes) -> dict[str, str | Decimal]:
    if len(raw) != record.size:
        raise CodecError(f"{record.name}: expected {record.size} bytes, got {len(raw)}")
    values: dict[str, str | Decimal] = {}
    for name, field in record.leaves():
        values[name] = decode_field(field, raw[field.offset : field.offset + field.size])
    return values


def encode_record(record: Field, values: dict[str, str | Decimal | int]) -> bytes:
    buffer = bytearray(b" " * record.size)
    for name, field in record.leaves():
        if name not in values:
            raise CodecError(f"{record.name}: missing value for {name}")
        chunk = encode_field(field, values[name])
        buffer[field.offset : field.offset + field.size] = chunk
    return bytes(buffer)


def read_records(path: Path, record: Field) -> list[bytes]:
    """Split a fixed-length unblocked file into records."""
    data = path.read_bytes()
    if len(data) % record.size != 0:
        raise CodecError(
            f"{path}: length {len(data)} is not a multiple of {record.size}"
        )
    return [
        data[offset : offset + record.size]
        for offset in range(0, len(data), record.size)
    ]
