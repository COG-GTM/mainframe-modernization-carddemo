"""Fixed-width record layout for the CardDemo Transaction entity.

The layout is derived directly from the ``CVTRA05Y`` copybook
(``app/cpy/CVTRA05Y.cpy``) via the ``data-mapper`` skill.  The same physical
layout is shared by ``CVTRA06Y`` (DALYTRAN), so the ASCII seed file
``app/data/ASCII/dailytran.txt`` can be used as a test fixture.

Each :class:`Field` records the COBOL name, PIC clause and the COBOL data
category.  Byte offsets are computed automatically from the field order so the
layout stays the single source of truth for both the extractor and the loader.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum


class CobolType(str, Enum):
    """COBOL data categories relevant to this layout."""

    ALPHANUMERIC = "ALPHANUMERIC"
    NUMERIC = "NUMERIC"
    SIGNED_DISPLAY = "SIGNED_DISPLAY"  # zoned decimal with overpunch sign


@dataclass(frozen=True)
class Field:
    """A single fixed-width field within a copybook record."""

    name: str
    pic: str
    cobol_type: CobolType
    length: int
    decimals: int = 0
    nullable: bool = False
    # The modern column name used when loading into PostgreSQL.
    column: str = ""

    def __post_init__(self) -> None:
        if not self.column:
            # TRAN-MERCHANT-ID -> tran_merchant_id
            object.__setattr__(self, "column", self.name.lower().replace("-", "_"))


@dataclass
class RecordLayout:
    """An ordered collection of :class:`Field` with computed byte offsets."""

    name: str
    fields: list[Field] = field(default_factory=list)

    @property
    def record_length(self) -> int:
        return sum(f.length for f in self.fields)

    def offsets(self) -> list[tuple[Field, int, int]]:
        """Return ``(field, start, end)`` 0-based half-open slices."""
        result: list[tuple[Field, int, int]] = []
        pos = 0
        for fld in self.fields:
            result.append((fld, pos, pos + fld.length))
            pos += fld.length
        return result


# --- CVTRA05Y / TRANSACT (RECLN = 350) ------------------------------------
#
# COBOL field            PIC            bytes  modern type
# TRAN-ID                X(16)          16     string(16)  (PK)
# TRAN-TYPE-CD           X(02)           2     string(2)
# TRAN-CAT-CD            9(04)           4     string(4)
# TRAN-SOURCE            X(10)          10     string(10)
# TRAN-DESC              X(100)        100     string(100)
# TRAN-AMT               S9(09)V99      11     decimal(11,2)  overpunch sign
# TRAN-MERCHANT-ID       9(09)           9     bigint
# TRAN-MERCHANT-NAME     X(50)          50     string(50)
# TRAN-MERCHANT-CITY     X(50)          50     string(50)
# TRAN-MERCHANT-ZIP      X(10)          10     string(10)
# TRAN-CARD-NUM          X(16)          16     string(16)  (PCI sensitive)
# TRAN-ORIG-TS           X(26)          26     timestamp
# TRAN-PROC-TS           X(26)          26     timestamp (nullable)
# FILLER                 X(20)          20     (dropped)
# -------------------------------------------------------------------------

TRANSACTION_LAYOUT = RecordLayout(
    name="TRAN-RECORD",
    fields=[
        Field("TRAN-ID", "X(16)", CobolType.ALPHANUMERIC, 16),
        Field("TRAN-TYPE-CD", "X(02)", CobolType.ALPHANUMERIC, 2),
        Field("TRAN-CAT-CD", "9(04)", CobolType.NUMERIC, 4),
        Field("TRAN-SOURCE", "X(10)", CobolType.ALPHANUMERIC, 10, nullable=True),
        Field("TRAN-DESC", "X(100)", CobolType.ALPHANUMERIC, 100, nullable=True),
        Field("TRAN-AMT", "S9(09)V99", CobolType.SIGNED_DISPLAY, 11, decimals=2),
        Field("TRAN-MERCHANT-ID", "9(09)", CobolType.NUMERIC, 9),
        Field("TRAN-MERCHANT-NAME", "X(50)", CobolType.ALPHANUMERIC, 50, nullable=True),
        Field("TRAN-MERCHANT-CITY", "X(50)", CobolType.ALPHANUMERIC, 50, nullable=True),
        Field("TRAN-MERCHANT-ZIP", "X(10)", CobolType.ALPHANUMERIC, 10, nullable=True),
        Field("TRAN-CARD-NUM", "X(16)", CobolType.ALPHANUMERIC, 16),
        Field("TRAN-ORIG-TS", "X(26)", CobolType.ALPHANUMERIC, 26),
        Field("TRAN-PROC-TS", "X(26)", CobolType.ALPHANUMERIC, 26, nullable=True),
        Field("FILLER", "X(20)", CobolType.ALPHANUMERIC, 20),
    ],
)
