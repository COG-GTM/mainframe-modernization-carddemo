from sqlalchemy import Boolean, Column, Integer, Numeric, String

from app.database import Base


class DailyTransaction(Base):
    __tablename__ = "daily_transactions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    tran_id = Column(String(16), nullable=False)
    type_cd = Column(String(2))
    cat_cd = Column(Numeric(4))
    source = Column(String(10))
    description = Column(String(100))
    amount = Column(Numeric(11, 2))
    merchant_id = Column(Numeric(9))
    merchant_name = Column(String(50))
    merchant_city = Column(String(50))
    merchant_zip = Column(String(10))
    card_num = Column(String(16))
    orig_ts = Column(String(26))
    proc_ts = Column(String(26), nullable=True)
    processed = Column(Boolean, default=False)
