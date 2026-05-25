"""Business logic reimplementing the PROCEDURE DIVISION of CBTRN02C.cbl."""

from datetime import datetime, timezone
from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.daily_transaction import DailyTransaction
from app.models.rejected_transaction import RejectedTransaction
from app.models.tran_cat_balance import TranCatBalance
from app.models.transaction import Transaction
from app.schemas.transaction import (
    BatchProcessResponse,
    DailyTransactionCreate,
    TransactionResult,
)


def _get_db2_format_timestamp() -> str:
    """Reimplements Z-GET-DB2-FORMAT-TIMESTAMP (lines 692-705).

    Format: YYYY-MM-DD-HH.MM.SS.NN0000
    """
    now = datetime.now(timezone.utc)
    return now.strftime("%Y-%m-%d-%H.%M.%S.") + f"{now.microsecond // 10000:02d}0000"


def validate_transaction(
    db: Session, daily_tran: DailyTransaction
) -> tuple[int, str | None, CardXref | None, Account | None]:
    """Reimplements 1500-VALIDATE-TRAN (lines 370-421).

    Returns (code, reason, xref, account).
    code=0 means valid.
    """
    # 1500-A-LOOKUP-XREF (lines 380-392)
    xref = (
        db.query(CardXref)
        .filter(CardXref.card_num == daily_tran.card_num)
        .first()
    )
    if xref is None:
        return (100, "INVALID CARD NUMBER FOUND", None, None)

    # 1500-B-LOOKUP-ACCT (lines 393-421)
    account = (
        db.query(Account)
        .filter(Account.acct_id == xref.acct_id)
        .first()
    )
    if account is None:
        return (101, "ACCOUNT RECORD NOT FOUND", xref, None)

    # Credit limit check (lines 403-412)
    curr_cyc_credit = Decimal(str(account.curr_cyc_credit or 0))
    curr_cyc_debit = Decimal(str(account.curr_cyc_debit or 0))
    amount = Decimal(str(daily_tran.amount or 0))
    credit_limit = Decimal(str(account.credit_limit or 0))

    temp_bal = curr_cyc_credit - curr_cyc_debit + amount
    if credit_limit < temp_bal:
        return (102, "OVERLIMIT TRANSACTION", xref, account)

    # Expiration check (lines 414-419)
    expiration_date = account.expiration_date or ""
    orig_ts_date = (daily_tran.orig_ts or "")[:10]
    if expiration_date < orig_ts_date:
        return (103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION", xref, account)

    return (0, None, xref, account)


def post_transaction(
    db: Session,
    daily_tran: DailyTransaction,
    xref: CardXref,
    account: Account,
) -> None:
    """Reimplements 2000-POST-TRANSACTION (lines 424-444)."""
    proc_ts = _get_db2_format_timestamp()

    # Create Transaction record (lines 425-438)
    tran = Transaction(
        tran_id=daily_tran.tran_id,
        type_cd=daily_tran.type_cd,
        cat_cd=daily_tran.cat_cd,
        source=daily_tran.source,
        description=daily_tran.description,
        amount=daily_tran.amount,
        merchant_id=daily_tran.merchant_id,
        merchant_name=daily_tran.merchant_name,
        merchant_city=daily_tran.merchant_city,
        merchant_zip=daily_tran.merchant_zip,
        card_num=daily_tran.card_num,
        orig_ts=daily_tran.orig_ts,
        proc_ts=proc_ts,
    )

    # 2700-UPDATE-TCATBAL (lines 467-542)
    _update_tcatbal(db, daily_tran, xref)

    # 2800-UPDATE-ACCOUNT-REC (lines 545-560)
    _update_account(db, daily_tran, account)

    # 2900-WRITE-TRANSACTION-FILE (lines 562-579)
    db.add(tran)


def _update_tcatbal(
    db: Session, daily_tran: DailyTransaction, xref: CardXref
) -> None:
    """Reimplements 2700-UPDATE-TCATBAL (lines 467-542)."""
    tcatbal = (
        db.query(TranCatBalance)
        .filter(
            TranCatBalance.acct_id == xref.acct_id,
            TranCatBalance.type_cd == daily_tran.type_cd,
            TranCatBalance.cat_cd == daily_tran.cat_cd,
        )
        .first()
    )

    amount = Decimal(str(daily_tran.amount or 0))

    if tcatbal is None:
        # 2700-A-CREATE-TCATBAL-REC (lines 503-524)
        tcatbal = TranCatBalance(
            acct_id=xref.acct_id,
            type_cd=daily_tran.type_cd,
            cat_cd=daily_tran.cat_cd,
            balance=amount,
        )
        db.add(tcatbal)
    else:
        # 2700-B-UPDATE-TCATBAL-REC (lines 526-542)
        tcatbal.balance = Decimal(str(tcatbal.balance or 0)) + amount


def _update_account(
    db: Session, daily_tran: DailyTransaction, account: Account
) -> None:
    """Reimplements 2800-UPDATE-ACCOUNT-REC (lines 545-560)."""
    amount = Decimal(str(daily_tran.amount or 0))
    account.curr_bal = Decimal(str(account.curr_bal or 0)) + amount

    if amount >= 0:
        account.curr_cyc_credit = Decimal(str(account.curr_cyc_credit or 0)) + amount
    else:
        account.curr_cyc_debit = Decimal(str(account.curr_cyc_debit or 0)) + amount


def reject_transaction(
    db: Session,
    daily_tran: DailyTransaction,
    code: int,
    reason: str,
) -> None:
    """Reimplements 2500-WRITE-REJECT-REC (lines 446-465)."""
    rejected = RejectedTransaction(
        tran_id=daily_tran.tran_id,
        card_num=daily_tran.card_num,
        amount=daily_tran.amount,
        orig_ts=daily_tran.orig_ts,
        rejection_code=code,
        rejection_reason=reason,
        original_data={
            "tran_id": daily_tran.tran_id,
            "type_cd": daily_tran.type_cd,
            "cat_cd": int(daily_tran.cat_cd) if daily_tran.cat_cd else None,
            "source": daily_tran.source,
            "description": daily_tran.description,
            "amount": str(daily_tran.amount),
            "merchant_id": int(daily_tran.merchant_id) if daily_tran.merchant_id else None,
            "merchant_name": daily_tran.merchant_name,
            "merchant_city": daily_tran.merchant_city,
            "merchant_zip": daily_tran.merchant_zip,
            "card_num": daily_tran.card_num,
            "orig_ts": daily_tran.orig_ts,
        },
    )
    db.add(rejected)


def process_single(db: Session, daily_tran: DailyTransaction) -> TransactionResult:
    """Reimplements the main loop body (lines 202-218)."""
    code, reason, xref, account = validate_transaction(db, daily_tran)

    if code == 0:
        post_transaction(db, daily_tran, xref, account)
        daily_tran.processed = True
        db.flush()
        return TransactionResult(
            tran_id=daily_tran.tran_id,
            status="posted",
        )
    else:
        reject_transaction(db, daily_tran, code, reason)
        daily_tran.processed = True
        db.flush()
        return TransactionResult(
            tran_id=daily_tran.tran_id,
            status="rejected",
            rejection_code=code,
            rejection_reason=reason,
        )


def process_batch(db: Session) -> BatchProcessResponse:
    """Reimplements the full program (lines 193-234)."""
    pending = (
        db.query(DailyTransaction)
        .filter(DailyTransaction.processed == False)  # noqa: E712
        .all()
    )

    results: list[TransactionResult] = []
    reject_count = 0

    for daily_tran in pending:
        result = process_single(db, daily_tran)
        results.append(result)
        if result.status == "rejected":
            reject_count += 1

    db.commit()

    return BatchProcessResponse(
        transactions_processed=len(results),
        transactions_rejected=reject_count,
        results=results,
    )


def create_daily_transaction(
    db: Session, data: DailyTransactionCreate
) -> DailyTransaction:
    """Insert a new daily transaction record."""
    daily_tran = DailyTransaction(
        tran_id=data.tran_id,
        type_cd=data.type_cd,
        cat_cd=data.cat_cd,
        source=data.source,
        description=data.description,
        amount=data.amount,
        merchant_id=data.merchant_id,
        merchant_name=data.merchant_name,
        merchant_city=data.merchant_city,
        merchant_zip=data.merchant_zip,
        card_num=data.card_num,
        orig_ts=data.orig_ts,
        processed=False,
    )
    db.add(daily_tran)
    db.flush()
    return daily_tran
