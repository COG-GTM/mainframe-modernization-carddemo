"""Account data model mirroring copybook ``app/cpy/CVACT01Y.cpy``.

The COBOL ``ACCOUNT-RECORD`` is a 300-byte fixed-width layout shared by many
CardDemo programs, so it lives in its own module.

Field / type mapping (see CVACT01Y.cpy):

    ACCT-ID                PIC 9(11)       -> int (primary key)
    ACCT-ACTIVE-STATUS     PIC X(01)       -> str (single char)
    ACCT-CURR-BAL          PIC S9(10)V99   -> Decimal, NUMERIC(12, 2)
    ACCT-CREDIT-LIMIT      PIC S9(10)V99   -> Decimal, NUMERIC(12, 2)
    ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   -> Decimal, NUMERIC(12, 2)
    ACCT-OPEN-DATE         PIC X(10)       -> str
    ACCT-EXPIRAION-DATE    PIC X(10)       -> str  (COBOL spelling kept)
    ACCT-REISSUE-DATE      PIC X(10)       -> str
    ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   -> Decimal, NUMERIC(12, 2)
    ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   -> Decimal, NUMERIC(12, 2)
    ACCT-ADDR-ZIP          PIC X(10)       -> str
    ACCT-GROUP-ID          PIC X(10)       -> str
    FILLER                 PIC X(178)      -> dropped

Monetary fields use ``decimal.Decimal`` backed by ``NUMERIC(12, 2)`` so that
exact cents are preserved -- never floating point.
"""

from __future__ import annotations

from decimal import Decimal

from sqlalchemy import Integer, Numeric, String
from sqlalchemy.orm import (
    DeclarativeBase,
    Mapped,
    MappedAsDataclass,
    mapped_column,
)

# Two decimal places, matching PIC S9(10)V99 / NUMERIC(12, 2).
MONEY = Numeric(12, 2)


class Base(MappedAsDataclass, DeclarativeBase):
    """Declarative base that also makes mapped models Python dataclasses."""


class Account(Base):
    """A single row of the account master file (copybook CVACT01Y.cpy).

    Because ``Base`` mixes in :class:`~sqlalchemy.orm.MappedAsDataclass`, this
    class is simultaneously a plain dataclass (``__init__``/``__eq__``/``__repr__``)
    and a SQLAlchemy ORM model mapped to the ``accounts`` table.
    """

    __tablename__ = "accounts"

    # ACCT-ID PIC 9(11) -> integer primary key (not auto-incremented; the id
    # comes from the source data just like the VSAM record key).
    acct_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=False)

    acct_active_status: Mapped[str] = mapped_column(String(1), default="")

    acct_curr_bal: Mapped[Decimal] = mapped_column(MONEY, default=Decimal("0.00"))
    acct_credit_limit: Mapped[Decimal] = mapped_column(MONEY, default=Decimal("0.00"))
    acct_cash_credit_limit: Mapped[Decimal] = mapped_column(MONEY, default=Decimal("0.00"))

    acct_open_date: Mapped[str] = mapped_column(String(10), default="")
    acct_expiraion_date: Mapped[str] = mapped_column(String(10), default="")
    acct_reissue_date: Mapped[str] = mapped_column(String(10), default="")

    acct_curr_cyc_credit: Mapped[Decimal] = mapped_column(MONEY, default=Decimal("0.00"))
    acct_curr_cyc_debit: Mapped[Decimal] = mapped_column(MONEY, default=Decimal("0.00"))

    acct_addr_zip: Mapped[str] = mapped_column(String(10), default="")
    acct_group_id: Mapped[str] = mapped_column(String(10), default="")
