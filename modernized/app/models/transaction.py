from sqlalchemy import BigInteger, Column, Integer, Numeric, String

from app.database import Base


class Transaction(Base):
    __tablename__ = "transactions"

    tran_id = Column(String(16), primary_key=True)
    type_cd = Column(String(2))
    cat_cd = Column(Integer)
    source = Column(String(10))
    description = Column(String(100))
    amount = Column(Numeric(11, 2))
    merchant_id = Column(BigInteger)
    merchant_name = Column(String(50))
    merchant_city = Column(String(50))
    merchant_zip = Column(String(10))
    card_num = Column(String(16))
    orig_ts = Column(String(26))
    proc_ts = Column(String(26))
