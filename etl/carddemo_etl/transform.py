"""Transform: turn a :class:`RawRecord` into typed, modern values.

The output dict is keyed by the modern (snake_case) column name and contains
native Python types (``str``, ``int``, ``Decimal``, ``datetime``, ``None``)
ready to be loaded into PostgreSQL.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from .extract import RawRecord
from .layout import CobolType, RecordLayout
from . import transforms as T


@dataclass
class TransformResult:
    line_number: int
    row: dict[str, Any]


class TransformError(Exception):
    def __init__(self, line_number: int, field: str, message: str):
        super().__init__(f"line {line_number}, field {field}: {message}")
        self.line_number = line_number
        self.field = field


def transform_record(
    record: RawRecord,
    layout: RecordLayout,
    *,
    mask_pan: bool = False,
) -> TransformResult:
    """Map a single raw record to typed column values."""
    row: dict[str, Any] = {}
    for fld in layout.fields:
        if fld.name == "FILLER":
            continue
        raw_value = record.fields[fld.name]
        try:
            row[fld.column] = _convert(fld, raw_value, mask_pan=mask_pan)
        except (ValueError, ArithmeticError) as exc:
            raise TransformError(record.line_number, fld.name, str(exc)) from exc
    return TransformResult(line_number=record.line_number, row=row)


def _convert(fld, raw_value: str, *, mask_pan: bool) -> Any:
    if fld.name == "TRAN-CARD-NUM" and mask_pan:
        return T.mask_card_number(raw_value)

    # Timestamp fields are alphanumeric in COBOL but semantically datetimes.
    if fld.name in ("TRAN-ORIG-TS", "TRAN-PROC-TS"):
        return T.parse_timestamp(raw_value)

    if fld.cobol_type is CobolType.SIGNED_DISPLAY:
        return T.parse_signed_decimal(raw_value, fld.decimals)

    if fld.cobol_type is CobolType.NUMERIC:
        # Merchant id is used as a numeric identifier; category code is a code.
        if fld.name == "TRAN-MERCHANT-ID":
            return T.parse_int(raw_value)
        return T.parse_numeric_string(raw_value)

    # ALPHANUMERIC
    value = T.clean_text(raw_value)
    if value is None and not fld.nullable:
        return ""
    return value
