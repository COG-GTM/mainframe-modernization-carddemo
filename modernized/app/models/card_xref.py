from sqlalchemy import BigInteger, Column, ForeignKey, String

from app.database import Base


class CardXref(Base):
    __tablename__ = "card_xrefs"

    card_num = Column(String(16), primary_key=True)
    cust_id = Column(BigInteger)
    acct_id = Column(BigInteger, ForeignKey("accounts.acct_id"))
