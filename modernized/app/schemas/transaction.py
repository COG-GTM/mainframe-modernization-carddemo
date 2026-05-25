from decimal import Decimal

from pydantic import BaseModel


class DailyTransactionCreate(BaseModel):
    """Schema for submitting an individual transaction (replaces a DALYTRAN-FILE record)."""

    tran_id: str
    type_cd: str
    cat_cd: int
    source: str
    description: str
    amount: Decimal
    merchant_id: int
    merchant_name: str
    merchant_city: str
    merchant_zip: str
    card_num: str
    orig_ts: str


class BatchProcessRequest(BaseModel):
    """Schema to trigger batch processing of all unprocessed daily transactions."""

    pass


class BatchUploadRequest(BaseModel):
    """Schema for uploading a batch of daily transactions."""

    transactions: list[DailyTransactionCreate]


class TransactionResult(BaseModel):
    tran_id: str
    status: str
    rejection_code: int | None = None
    rejection_reason: str | None = None


class TransactionResponse(BaseModel):
    """A successfully posted transaction."""

    tran_id: str
    type_cd: str
    cat_cd: int
    source: str
    description: str
    amount: Decimal
    merchant_id: int
    merchant_name: str
    merchant_city: str
    merchant_zip: str
    card_num: str
    orig_ts: str
    proc_ts: str


class BatchProcessResponse(BaseModel):
    """Equivalent to the final DISPLAY of CBTRN02C (lines 227-228)."""

    transactions_processed: int
    transactions_rejected: int
    results: list[TransactionResult]


class UploadResponse(BaseModel):
    inserted: int


class RejectedTransactionResponse(BaseModel):
    id: int
    tran_id: str
    card_num: str
    amount: Decimal
    orig_ts: str
    rejection_code: int
    rejection_reason: str


class PaginatedTransactionsResponse(BaseModel):
    page: int
    per_page: int
    total: int
    items: list[TransactionResponse]
