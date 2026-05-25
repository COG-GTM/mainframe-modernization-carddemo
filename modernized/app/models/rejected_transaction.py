from sqlalchemy import Column, DateTime, Integer, JSON, Numeric, String, func

from app.database import Base


class RejectedTransaction(Base):
    __tablename__ = "rejected_transactions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    tran_id = Column(String(16))
    card_num = Column(String(16))
    amount = Column(Numeric(11, 2))
    orig_ts = Column(String(26))
    rejection_code = Column(Integer)
    rejection_reason = Column(String(76))
    rejected_at = Column(DateTime, default=func.now())
    original_data = Column(JSON)
