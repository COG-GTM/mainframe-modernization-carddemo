from pydantic import BaseModel, Field
from decimal import Decimal
from typing import Optional


class AccountRecord(BaseModel):
    acct_id: str = Field(..., min_length=11, max_length=11, description="11-digit account ID")
    acct_active_status: str = Field(default="Y", max_length=1)
    acct_curr_bal: Decimal = Field(default=Decimal("0.00"))
    acct_credit_limit: Decimal = Field(default=Decimal("0.00"))
    acct_cash_credit_limit: Decimal = Field(default=Decimal("0.00"))
    acct_open_date: Optional[str] = Field(default=None, max_length=10)
    acct_expiration_date: Optional[str] = Field(default=None, max_length=10)
    acct_reissue_date: Optional[str] = Field(default=None, max_length=10)
    acct_curr_cyc_credit: Decimal = Field(default=Decimal("0.00"))
    acct_curr_cyc_debit: Decimal = Field(default=Decimal("0.00"))
    acct_addr_zip: Optional[str] = Field(default=None, max_length=10)
    acct_group_id: Optional[str] = Field(default=None, max_length=10)
    
    class Config:
        json_schema_extra = {
            "example": {
                "acct_id": "12345678901",
                "acct_active_status": "Y",
                "acct_curr_bal": "1500.50",
                "acct_credit_limit": "5000.00",
                "acct_cash_credit_limit": "1000.00",
                "acct_open_date": "2020-01-15",
                "acct_expiration_date": "2025-01-15",
                "acct_reissue_date": "2020-01-15",
                "acct_curr_cyc_credit": "0.00",
                "acct_curr_cyc_debit": "1500.50",
                "acct_addr_zip": "12345",
                "acct_group_id": "GROUP001"
            }
        }


class CardXrefRecord(BaseModel):
    xref_card_num: str = Field(..., max_length=16, description="Card number")
    xref_cust_id: str = Field(..., max_length=9, description="Customer ID")
    xref_acct_id: str = Field(..., max_length=11, description="Account ID")
    
    class Config:
        json_schema_extra = {
            "example": {
                "xref_card_num": "4111111111111111",
                "xref_cust_id": "000000001",
                "xref_acct_id": "12345678901"
            }
        }


class TransactionRecord(BaseModel):
    tran_id: str = Field(..., max_length=16, description="Transaction ID")
    tran_type_cd: str = Field(..., max_length=2, description="Transaction type code")
    tran_cat_cd: int = Field(..., description="Transaction category code")
    tran_source: str = Field(..., max_length=10)
    tran_desc: str = Field(..., max_length=100)
    tran_amt: Decimal
    tran_merchant_id: int
    tran_merchant_name: str = Field(..., max_length=50)
    tran_merchant_city: str = Field(..., max_length=50)
    tran_merchant_zip: str = Field(..., max_length=10)
    tran_card_num: str = Field(..., max_length=16)
    tran_orig_ts: str = Field(..., max_length=26, description="Original timestamp")
    tran_proc_ts: str = Field(..., max_length=26, description="Processing timestamp")
    
    class Config:
        json_schema_extra = {
            "example": {
                "tran_id": "0000000000000001",
                "tran_type_cd": "02",
                "tran_cat_cd": 2,
                "tran_source": "POS TERM",
                "tran_desc": "BILL PAYMENT - ONLINE",
                "tran_amt": "1500.50",
                "tran_merchant_id": 999999999,
                "tran_merchant_name": "BILL PAYMENT",
                "tran_merchant_city": "N/A",
                "tran_merchant_zip": "N/A",
                "tran_card_num": "4111111111111111",
                "tran_orig_ts": "2024-10-07-12:30:00.000000",
                "tran_proc_ts": "2024-10-07-12:30:00.000000"
            }
        }
