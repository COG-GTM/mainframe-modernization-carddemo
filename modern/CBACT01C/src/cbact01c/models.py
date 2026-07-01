"""SQLAlchemy model for the account record, mirroring CVACT01Y copybook layout."""

from sqlalchemy import Column, Date, Integer, Numeric, String
from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    pass


class AccountRecord(Base):
    """Maps to COBOL ACCOUNT-RECORD (CVACT01Y copybook, RECLN 300).

    Column names mirror the COBOL field names for traceability.
    """

    __tablename__ = "account_record"

    acct_id = Column(Integer, primary_key=True, comment="PIC 9(11)")
    acct_active_status = Column(String(1), nullable=False, comment="PIC X(01)")
    acct_curr_bal = Column(Numeric(12, 2), nullable=False, comment="PIC S9(10)V99")
    acct_credit_limit = Column(Numeric(12, 2), nullable=False, comment="PIC S9(10)V99")
    acct_cash_credit_limit = Column(Numeric(12, 2), nullable=False, comment="PIC S9(10)V99")
    acct_open_date = Column(Date, nullable=True, comment="PIC X(10) YYYY-MM-DD")
    acct_expiraion_date = Column(Date, nullable=True, comment="PIC X(10) YYYY-MM-DD")
    acct_reissue_date = Column(Date, nullable=True, comment="PIC X(10) YYYY-MM-DD")
    acct_curr_cyc_credit = Column(Numeric(12, 2), nullable=False, comment="PIC S9(10)V99")
    acct_curr_cyc_debit = Column(Numeric(12, 2), nullable=False, comment="PIC S9(10)V99")
    acct_addr_zip = Column(String(10), nullable=True, comment="PIC X(10)")
    acct_group_id = Column(String(10), nullable=True, comment="PIC X(10)")
