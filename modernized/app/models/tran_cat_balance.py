from sqlalchemy import BigInteger, Column, Integer, Numeric, String

from app.database import Base


class TranCatBalance(Base):
    __tablename__ = "tran_cat_balances"

    acct_id = Column(BigInteger, primary_key=True)
    type_cd = Column(String(2), primary_key=True)
    cat_cd = Column(Integer, primary_key=True)
    balance = Column(Numeric(11, 2), default=0)
