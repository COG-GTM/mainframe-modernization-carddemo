from decimal import Decimal

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.daily_transaction import DailyTransaction
from app.models.rejected_transaction import RejectedTransaction
from app.models.transaction import Transaction
from app.schemas.transaction import (
    BatchProcessRequest,
    BatchProcessResponse,
    BatchUploadRequest,
    DailyTransactionCreate,
    PaginatedTransactionsResponse,
    RejectedTransactionResponse,
    TransactionResponse,
    TransactionResult,
    UploadResponse,
)
from app.services.transaction_service import (
    create_daily_transaction,
    process_batch,
    process_single,
)

router = APIRouter(prefix="/api/v1/transactions", tags=["transactions"])


@router.post("/process", response_model=TransactionResult)
def process_transaction(
    payload: DailyTransactionCreate, db: Session = Depends(get_db)
) -> TransactionResult:
    """Process a single transaction. Equivalent to one iteration of the COBOL main loop."""
    daily_tran = create_daily_transaction(db, payload)
    result = process_single(db, daily_tran)
    db.commit()

    if result.status == "rejected":
        raise HTTPException(
            status_code=422,
            detail={
                "tran_id": result.tran_id,
                "status": result.status,
                "rejection_code": result.rejection_code,
                "rejection_reason": result.rejection_reason,
            },
        )
    return result


@router.post("/process-batch", response_model=BatchProcessResponse)
def process_batch_endpoint(
    _payload: BatchProcessRequest = None, db: Session = Depends(get_db)
) -> BatchProcessResponse:
    """Process all pending daily transactions. Equivalent to the full POSTTRAN JCL job."""
    return process_batch(db)


@router.post("/upload", response_model=UploadResponse)
def upload_transactions(
    payload: BatchUploadRequest, db: Session = Depends(get_db)
) -> UploadResponse:
    """Upload a batch of daily transactions without processing them."""
    for tran_data in payload.transactions:
        create_daily_transaction(db, tran_data)
    db.commit()
    return UploadResponse(inserted=len(payload.transactions))


@router.get("", response_model=PaginatedTransactionsResponse)
def list_transactions(
    page: int = Query(1, ge=1),
    per_page: int = Query(20, ge=1, le=100),
    card_num: str | None = Query(None),
    db: Session = Depends(get_db),
) -> PaginatedTransactionsResponse:
    """List posted transactions with pagination."""
    query = db.query(Transaction)
    if card_num:
        query = query.filter(Transaction.card_num == card_num)

    total = query.count()
    items = query.offset((page - 1) * per_page).limit(per_page).all()

    return PaginatedTransactionsResponse(
        page=page,
        per_page=per_page,
        total=total,
        items=[
            TransactionResponse(
                tran_id=t.tran_id,
                type_cd=t.type_cd,
                cat_cd=int(t.cat_cd),
                source=t.source,
                description=t.description,
                amount=Decimal(str(t.amount)),
                merchant_id=int(t.merchant_id),
                merchant_name=t.merchant_name,
                merchant_city=t.merchant_city,
                merchant_zip=t.merchant_zip,
                card_num=t.card_num,
                orig_ts=t.orig_ts,
                proc_ts=t.proc_ts,
            )
            for t in items
        ],
    )


@router.get("/rejected", response_model=list[RejectedTransactionResponse])
def list_rejected_transactions(
    db: Session = Depends(get_db),
) -> list[RejectedTransactionResponse]:
    """List rejected transactions. Equivalent to reading the DALYREJS-FILE."""
    rejects = db.query(RejectedTransaction).all()
    return [
        RejectedTransactionResponse(
            id=r.id,
            tran_id=r.tran_id,
            card_num=r.card_num,
            amount=Decimal(str(r.amount)),
            orig_ts=r.orig_ts,
            rejection_code=r.rejection_code,
            rejection_reason=r.rejection_reason,
        )
        for r in rejects
    ]


@router.get("/{tran_id}", response_model=TransactionResponse)
def get_transaction(
    tran_id: str, db: Session = Depends(get_db)
) -> TransactionResponse:
    """Get a single posted transaction by ID."""
    t = db.query(Transaction).filter(Transaction.tran_id == tran_id).first()
    if t is None:
        raise HTTPException(status_code=404, detail="Transaction not found")
    return TransactionResponse(
        tran_id=t.tran_id,
        type_cd=t.type_cd,
        cat_cd=int(t.cat_cd),
        source=t.source,
        description=t.description,
        amount=Decimal(str(t.amount)),
        merchant_id=int(t.merchant_id),
        merchant_name=t.merchant_name,
        merchant_city=t.merchant_city,
        merchant_zip=t.merchant_zip,
        card_num=t.card_num,
        orig_ts=t.orig_ts,
        proc_ts=t.proc_ts,
    )
