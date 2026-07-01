"""Unit tests for the signed overpunch decoder."""

from decimal import Decimal

import pytest

from cbact01c.overpunch import decode_signed_overpunch


class TestDecodeSignedOverpunch:
    """Verify every overpunch symbol and edge cases."""

    @pytest.mark.parametrize(
        "raw, decimal_places, expected",
        [
            # Positive zero via '{'
            ("00000001940{", 2, Decimal("194.00")),
            # Positive digits A-I (overpunch replaces last char with its digit)
            ("00000001940A", 2, Decimal("194.01")),
            ("00000001940B", 2, Decimal("194.02")),
            ("00000001940C", 2, Decimal("194.03")),
            ("00000001940D", 2, Decimal("194.04")),
            ("00000001940E", 2, Decimal("194.05")),
            ("00000001940F", 2, Decimal("194.06")),
            ("00000001940G", 2, Decimal("194.07")),
            ("00000001940H", 2, Decimal("194.08")),
            ("00000001940I", 2, Decimal("194.09")),
            # Negative zero via '}'
            ("00000001940}", 2, Decimal("-194.00")),
            # Negative digits J-R
            ("00000001940J", 2, Decimal("-194.01")),
            ("00000001940K", 2, Decimal("-194.02")),
            ("00000001940L", 2, Decimal("-194.03")),
            ("00000001940M", 2, Decimal("-194.04")),
            ("00000001940N", 2, Decimal("-194.05")),
            ("00000001940O", 2, Decimal("-194.06")),
            ("00000001940P", 2, Decimal("-194.07")),
            ("00000001940Q", 2, Decimal("-194.08")),
            ("00000001940R", 2, Decimal("-194.09")),
            # No decimal places
            ("001{", 0, Decimal("10")),
            ("001}", 0, Decimal("-10")),
            # Plain digit fallback
            ("00100", 2, Decimal("1.00")),
            # Zero
            ("00000000000{", 2, Decimal("0.00")),
        ],
    )
    def test_decode(self, raw: str, decimal_places: int, expected: Decimal) -> None:
        assert decode_signed_overpunch(raw, decimal_places) == expected

    def test_empty_raises(self) -> None:
        with pytest.raises(ValueError, match="Empty"):
            decode_signed_overpunch("")

    def test_invalid_char_raises(self) -> None:
        with pytest.raises(ValueError, match="Invalid overpunch"):
            decode_signed_overpunch("0000Z")
