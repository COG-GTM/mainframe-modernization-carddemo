from sqlalchemy import BigInteger, Column, Numeric, String

from app.database import Base


class Account(Base):
    __tablename__ = "accounts"

    acct_id = Column(BigInteger, primary_key=True)
    active_status = Column(String(1))
    curr_bal = Column(Numeric(12, 2), default=0)
    credit_limit = Column(Numeric(12, 2), default=0)
    cash_credit_limit = Column(Numeric(12, 2), default=0)
    open_date = Column(String(10))
    expiration_date = Column(String(10))
    reissue_date = Column(String(10))
    curr_cyc_credit = Column(Numeric(12, 2), default=0)
    curr_cyc_debit = Column(Numeric(12, 2), default=0)
    addr_zip = Column(String(10))
    group_id = Column(String(10))
