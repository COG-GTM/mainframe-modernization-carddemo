from sqlalchemy import Column, ForeignKey, Numeric, String

from app.database import Base


class CardXref(Base):
    __tablename__ = "card_xrefs"

    card_num = Column(String(16), primary_key=True)
    cust_id = Column(Numeric(9))
    acct_id = Column(Numeric(11), ForeignKey("accounts.acct_id"))
