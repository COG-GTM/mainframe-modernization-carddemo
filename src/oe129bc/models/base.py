"""Base utilities for COBOL fixed-length record serialization."""

from __future__ import annotations

from decimal import Decimal
from typing import Any, ClassVar

from pydantic import BaseModel


class FieldSpec:
    """Describes a single COBOL PIC field for fixed-length serialization."""

    __slots__ = ("name", "pic_type", "length", "decimals", "signed")

    def __init__(
        self,
        name: str,
        pic_type: str,
        length: int,
        decimals: int = 0,
        signed: bool = False,
    ) -> None:
        self.name = name
        self.pic_type = pic_type  # "X" for alphanumeric, "9" for numeric
        self.length = length  # total display length (excluding implicit decimal)
        self.decimals = decimals  # digits after implied decimal point (V99 -> 2)
        self.signed = signed


class FixedLengthRecordMixin:
    """Mixin providing from_fixed_length / to_fixed_length for COBOL records.

    Subclasses must define a class variable ``FIELD_SPECS`` containing an ordered
    list of :class:`FieldSpec` instances that describe the COBOL record layout.
    """

    FIELD_SPECS: ClassVar[list[FieldSpec]]

    @classmethod
    def _total_length(cls) -> int:
        total = 0
        for spec in cls.FIELD_SPECS:
            if spec.pic_type == "X":
                total += spec.length
            else:
                # PIC 9(n) -> n bytes, PIC S9(n)V99 -> n + decimals + (1 if signed)
                field_len = spec.length + spec.decimals
                if spec.signed:
                    field_len += 1  # leading sign character
                total += field_len
        return total

    @classmethod
    def from_fixed_length(cls, data: str) -> Any:
        """Parse a fixed-length string into a model instance.

        Args:
            data: Fixed-length character string representing the COBOL record.

        Returns:
            An instance of the model.

        Raises:
            ValueError: If the data length does not match the expected record length.
        """
        expected = cls._total_length()
        if len(data) != expected:
            raise ValueError(
                f"Expected record length {expected}, got {len(data)}"
            )

        offset = 0
        values: dict[str, Any] = {}

        for spec in cls.FIELD_SPECS:
            if spec.name == "FILLER":
                # Skip filler bytes
                if spec.pic_type == "X":
                    offset += spec.length
                else:
                    offset += spec.length + spec.decimals + (1 if spec.signed else 0)
                continue

            if spec.pic_type == "X":
                raw = data[offset : offset + spec.length]
                values[spec.name] = raw.rstrip()
                offset += spec.length
            else:
                field_len = spec.length + spec.decimals
                if spec.signed:
                    field_len += 1
                raw = data[offset : offset + field_len]
                offset += field_len

                if spec.decimals > 0:
                    # Signed decimal: PIC S9(n)V99
                    raw_stripped = raw.strip()
                    if not raw_stripped or raw_stripped in ("", "+", "-"):
                        numeric_val = Decimal("0")
                    else:
                        sign = ""
                        digits = raw_stripped
                        if digits[0] in ("+", "-"):
                            sign = digits[0]
                            digits = digits[1:]
                        digits = digits.lstrip("0") or "0"
                        int_part = digits[: -spec.decimals] if len(digits) > spec.decimals else ""
                        dec_part = digits[-spec.decimals :].ljust(spec.decimals, "0")
                        if not int_part:
                            int_part = "0"
                        numeric_val = Decimal(f"{sign}{int_part}.{dec_part}")
                    values[spec.name] = numeric_val
                else:
                    # Unsigned integer: PIC 9(n)
                    raw_stripped = raw.strip().lstrip("0") or "0"
                    values[spec.name] = int(raw_stripped)

        return cls(**values)  # type: ignore[call-arg]

    def to_fixed_length(self) -> str:
        """Serialize the model to a fixed-length string.

        Returns:
            A fixed-length string with proper COBOL padding
            (spaces for PIC X, zeros for PIC 9).
        """
        parts: list[str] = []

        for spec in self.__class__.FIELD_SPECS:
            if spec.name == "FILLER":
                if spec.pic_type == "X":
                    parts.append(" " * spec.length)
                else:
                    field_len = spec.length + spec.decimals + (1 if spec.signed else 0)
                    parts.append("0" * field_len)
                continue

            value = getattr(self, spec.name)

            if spec.pic_type == "X":
                # PIC X(n): left-justified, space-padded
                s = str(value) if value is not None else ""
                parts.append(s.ljust(spec.length)[:spec.length])
            else:
                if spec.decimals > 0:
                    # PIC S9(n)V99: sign + integer digits + decimal digits
                    dec_val = Decimal(str(value)) if value is not None else Decimal("0")
                    sign = "-" if dec_val < 0 else "+"
                    abs_val = abs(dec_val)
                    # Separate integer and fractional parts
                    int_part = int(abs_val)
                    frac_part = abs_val - int_part
                    # Format fractional digits
                    frac_str = str(frac_part).split(".")[1] if "." in str(frac_part) else "0"
                    frac_str = frac_str[:spec.decimals].ljust(spec.decimals, "0")
                    # Format integer digits
                    int_str = str(int_part).zfill(spec.length)[-spec.length:]
                    if spec.signed:
                        parts.append(f"{sign}{int_str}{frac_str}")
                    else:
                        parts.append(f"{int_str}{frac_str}")
                else:
                    # PIC 9(n): right-justified, zero-padded
                    int_val = int(value) if value is not None else 0
                    parts.append(str(abs(int_val)).zfill(spec.length)[-spec.length:])

        return "".join(parts)
