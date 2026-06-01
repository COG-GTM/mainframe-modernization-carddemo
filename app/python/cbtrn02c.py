"""CBTRN02C - POSTTRAN batch job (Python / SQLite translation).

Direct translation of ``app/cbl/CBTRN02C.cbl``: the CardDemo batch program that
posts records from the daily transaction file to the transaction master, while
validating each record against the card cross-reference and account master.

Translation notes
-----------------
* VSAM KSDS / sequential files are replaced by SQLite tables (see ``DDL``).
* Monetary amounts use :class:`decimal.Decimal` (never floating point), matching
  the COBOL fixed-point ``PIC S9(n)V99`` semantics.
* Dataclasses mirror the COBOL copybook record layouts:
    - :class:`DalyTranRecord`   -> CVTRA06Y (daily transaction)
    - :class:`TranRecord`       -> CVTRA05Y (transaction master)
    - :class:`AccountRecord`    -> CVACT01Y (account)
    - :class:`CardXrefRecord`   -> CVACT03Y (card xref)
    - :class:`TranCatBalRecord` -> CVTRA01Y (transaction category balance)
* Validation reason codes match the COBOL program:
    - 100 -> invalid card number (xref lookup failed)
    - 101 -> account record not found
    - 102 -> overlimit transaction
    - 103 -> transaction received after account expiration
* ``proc_ts`` is generated in DB2 timestamp format
  (``YYYY-MM-DD-HH.MM.SS.NN0000``) matching ``Z-GET-DB2-FORMAT-TIMESTAMP``.
* The process exit code is ``4`` when any transaction was rejected, otherwise
  ``0`` -- matching the COBOL ``RETURN-CODE`` behaviour.
"""

from __future__ import annotations

import sqlite3
import sys
from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal, ROUND_HALF_UP

# Validation failure reason codes (from CBTRN02C 1500-VALIDATE-TRAN).
REASON_INVALID_CARD = 100
REASON_ACCT_NOT_FOUND = 101
REASON_OVERLIMIT = 102
REASON_EXPIRED = 103

_TWO_PLACES = Decimal("0.01")
_ZERO = Decimal("0.00")


def _money(value) -> Decimal:
    """Coerce a value to a 2-decimal-place :class:`Decimal` (PIC S9(n)V99)."""
    if value is None or value == "":
        return _ZERO
    return Decimal(str(value)).quantize(_TWO_PLACES, rounding=ROUND_HALF_UP)


# ---------------------------------------------------------------------------
# Copybook record layouts (DATA DIVISION)
# ---------------------------------------------------------------------------
@dataclass
class DalyTranRecord:
    """CVTRA06Y - DALYTRAN-RECORD (RECLN = 350)."""

    dalytran_id: str = ""                 # PIC X(16)
    dalytran_type_cd: str = ""            # PIC X(02)
    dalytran_cat_cd: int = 0              # PIC 9(04)
    dalytran_source: str = ""             # PIC X(10)
    dalytran_desc: str = ""               # PIC X(100)
    dalytran_amt: Decimal = _ZERO         # PIC S9(09)V99
    dalytran_merchant_id: int = 0         # PIC 9(09)
    dalytran_merchant_name: str = ""      # PIC X(50)
    dalytran_merchant_city: str = ""      # PIC X(50)
    dalytran_merchant_zip: str = ""       # PIC X(10)
    dalytran_card_num: str = ""           # PIC X(16)
    dalytran_orig_ts: str = ""            # PIC X(26)
    dalytran_proc_ts: str = ""            # PIC X(26)

    @classmethod
    def from_row(cls, row: sqlite3.Row) -> "DalyTranRecord":
        return cls(
            dalytran_id=row["dalytran_id"],
            dalytran_type_cd=row["dalytran_type_cd"],
            dalytran_cat_cd=int(row["dalytran_cat_cd"]),
            dalytran_source=row["dalytran_source"] or "",
            dalytran_desc=row["dalytran_desc"] or "",
            dalytran_amt=_money(row["dalytran_amt"]),
            dalytran_merchant_id=int(row["dalytran_merchant_id"] or 0),
            dalytran_merchant_name=row["dalytran_merchant_name"] or "",
            dalytran_merchant_city=row["dalytran_merchant_city"] or "",
            dalytran_merchant_zip=row["dalytran_merchant_zip"] or "",
            dalytran_card_num=row["dalytran_card_num"],
            dalytran_orig_ts=row["dalytran_orig_ts"] or "",
            dalytran_proc_ts=row["dalytran_proc_ts"] or "",
        )

    def to_record_string(self) -> str:
        """Render the fixed-width 350-byte DALYTRAN-RECORD image (for rejects)."""
        cents = int((self.dalytran_amt * 100).to_integral_value(rounding=ROUND_HALF_UP))
        sign = "-" if cents < 0 else "+"
        amt = sign + str(abs(cents)).zfill(11)
        parts = [
            self.dalytran_id.ljust(16)[:16],
            self.dalytran_type_cd.ljust(2)[:2],
            str(self.dalytran_cat_cd).zfill(4)[:4],
            self.dalytran_source.ljust(10)[:10],
            self.dalytran_desc.ljust(100)[:100],
            amt,
            str(self.dalytran_merchant_id).zfill(9)[:9],
            self.dalytran_merchant_name.ljust(50)[:50],
            self.dalytran_merchant_city.ljust(50)[:50],
            self.dalytran_merchant_zip.ljust(10)[:10],
            self.dalytran_card_num.ljust(16)[:16],
            self.dalytran_orig_ts.ljust(26)[:26],
            self.dalytran_proc_ts.ljust(26)[:26],
        ]
        return "".join(parts).ljust(350)[:350]


@dataclass
class TranRecord:
    """CVTRA05Y - TRAN-RECORD (RECLN = 350)."""

    tran_id: str = ""                     # PIC X(16)
    tran_type_cd: str = ""                # PIC X(02)
    tran_cat_cd: int = 0                  # PIC 9(04)
    tran_source: str = ""                 # PIC X(10)
    tran_desc: str = ""                   # PIC X(100)
    tran_amt: Decimal = _ZERO             # PIC S9(09)V99
    tran_merchant_id: int = 0             # PIC 9(09)
    tran_merchant_name: str = ""          # PIC X(50)
    tran_merchant_city: str = ""          # PIC X(50)
    tran_merchant_zip: str = ""           # PIC X(10)
    tran_card_num: str = ""               # PIC X(16)
    tran_orig_ts: str = ""                # PIC X(26)
    tran_proc_ts: str = ""                # PIC X(26)


@dataclass
class AccountRecord:
    """CVACT01Y - ACCOUNT-RECORD (RECLN 300)."""

    acct_id: int = 0                      # PIC 9(11)
    acct_active_status: str = ""          # PIC X(01)
    acct_curr_bal: Decimal = _ZERO        # PIC S9(10)V99
    acct_credit_limit: Decimal = _ZERO    # PIC S9(10)V99
    acct_cash_credit_limit: Decimal = _ZERO  # PIC S9(10)V99
    acct_open_date: str = ""              # PIC X(10)
    acct_expiraion_date: str = ""         # PIC X(10)
    acct_reissue_date: str = ""           # PIC X(10)
    acct_curr_cyc_credit: Decimal = _ZERO  # PIC S9(10)V99
    acct_curr_cyc_debit: Decimal = _ZERO   # PIC S9(10)V99
    acct_addr_zip: str = ""               # PIC X(10)
    acct_group_id: str = ""               # PIC X(10)

    @classmethod
    def from_row(cls, row: sqlite3.Row) -> "AccountRecord":
        return cls(
            acct_id=int(row["acct_id"]),
            acct_active_status=row["acct_active_status"] or "",
            acct_curr_bal=_money(row["acct_curr_bal"]),
            acct_credit_limit=_money(row["acct_credit_limit"]),
            acct_cash_credit_limit=_money(row["acct_cash_credit_limit"]),
            acct_open_date=row["acct_open_date"] or "",
            acct_expiraion_date=row["acct_expiraion_date"] or "",
            acct_reissue_date=row["acct_reissue_date"] or "",
            acct_curr_cyc_credit=_money(row["acct_curr_cyc_credit"]),
            acct_curr_cyc_debit=_money(row["acct_curr_cyc_debit"]),
            acct_addr_zip=row["acct_addr_zip"] or "",
            acct_group_id=row["acct_group_id"] or "",
        )


@dataclass
class CardXrefRecord:
    """CVACT03Y - CARD-XREF-RECORD (RECLN 50)."""

    xref_card_num: str = ""               # PIC X(16)
    xref_cust_id: int = 0                 # PIC 9(09)
    xref_acct_id: int = 0                 # PIC 9(11)

    @classmethod
    def from_row(cls, row: sqlite3.Row) -> "CardXrefRecord":
        return cls(
            xref_card_num=row["xref_card_num"],
            xref_cust_id=int(row["xref_cust_id"] or 0),
            xref_acct_id=int(row["xref_acct_id"]),
        )


@dataclass
class TranCatBalRecord:
    """CVTRA01Y - TRAN-CAT-BAL-RECORD (RECLN = 50)."""

    trancat_acct_id: int = 0              # PIC 9(11)
    trancat_type_cd: str = ""             # PIC X(02)
    trancat_cd: int = 0                   # PIC 9(04)
    tran_cat_bal: Decimal = _ZERO         # PIC S9(09)V99

    @classmethod
    def from_row(cls, row: sqlite3.Row) -> "TranCatBalRecord":
        return cls(
            trancat_acct_id=int(row["trancat_acct_id"]),
            trancat_type_cd=row["trancat_type_cd"],
            trancat_cd=int(row["trancat_cd"]),
            tran_cat_bal=_money(row["tran_cat_bal"]),
        )


@dataclass
class ValidationTrailer:
    """WS-VALIDATION-TRAILER."""

    reason: int = 0                       # WS-VALIDATION-FAIL-REASON  PIC 9(04)
    desc: str = ""                        # WS-VALIDATION-FAIL-REASON-DESC PIC X(76)


# ---------------------------------------------------------------------------
# DDL - create all SQLite tables mirroring the VSAM files used by CBTRN02C.
# ---------------------------------------------------------------------------
DDL = """
CREATE TABLE IF NOT EXISTS daily_transactions (
    dalytran_id            TEXT PRIMARY KEY,
    dalytran_type_cd       TEXT,
    dalytran_cat_cd        INTEGER,
    dalytran_source        TEXT,
    dalytran_desc          TEXT,
    dalytran_amt           TEXT,
    dalytran_merchant_id   INTEGER,
    dalytran_merchant_name TEXT,
    dalytran_merchant_city TEXT,
    dalytran_merchant_zip  TEXT,
    dalytran_card_num      TEXT,
    dalytran_orig_ts       TEXT,
    dalytran_proc_ts       TEXT
);

CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num TEXT PRIMARY KEY,
    xref_cust_id  INTEGER,
    xref_acct_id  INTEGER
);

CREATE TABLE IF NOT EXISTS accounts (
    acct_id                INTEGER PRIMARY KEY,
    acct_active_status     TEXT,
    acct_curr_bal          TEXT,
    acct_credit_limit      TEXT,
    acct_cash_credit_limit TEXT,
    acct_open_date         TEXT,
    acct_expiraion_date    TEXT,
    acct_reissue_date      TEXT,
    acct_curr_cyc_credit   TEXT,
    acct_curr_cyc_debit    TEXT,
    acct_addr_zip          TEXT,
    acct_group_id          TEXT
);

CREATE TABLE IF NOT EXISTS transactions (
    tran_id            TEXT PRIMARY KEY,
    tran_type_cd       TEXT,
    tran_cat_cd        INTEGER,
    tran_source        TEXT,
    tran_desc          TEXT,
    tran_amt           TEXT,
    tran_merchant_id   INTEGER,
    tran_merchant_name TEXT,
    tran_merchant_city TEXT,
    tran_merchant_zip  TEXT,
    tran_card_num      TEXT,
    tran_orig_ts       TEXT,
    tran_proc_ts       TEXT
);

CREATE TABLE IF NOT EXISTS tran_cat_bal (
    trancat_acct_id INTEGER,
    trancat_type_cd TEXT,
    trancat_cd      INTEGER,
    tran_cat_bal    TEXT,
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

CREATE TABLE IF NOT EXISTS rejected_transactions (
    id                          INTEGER PRIMARY KEY AUTOINCREMENT,
    dalytran_id                 TEXT,
    reject_tran_data            TEXT,
    validation_fail_reason      INTEGER,
    validation_fail_reason_desc TEXT
);
"""


class AbendError(RuntimeError):
    """Raised in place of 9999-ABEND-PROGRAM (fatal I/O error)."""


class PostTran:
    """POSTTRAN batch job (CBTRN02C PROCEDURE DIVISION)."""

    def __init__(self, conn: sqlite3.Connection):
        self.conn = conn
        self.conn.row_factory = sqlite3.Row
        self.transaction_count = 0          # WS-TRANSACTION-COUNT
        self.reject_count = 0               # WS-REJECT-COUNT

    # -- main driver (PROCEDURE DIVISION) -----------------------------------
    def run(self) -> int:
        print("START OF EXECUTION OF PROGRAM CBTRN02C")
        for daly in self._dalytran_get_next():
            self.transaction_count += 1
            trailer = ValidationTrailer()
            self._validate_tran(daly, trailer)
            if trailer.reason == 0:
                self._post_transaction(daly)
            else:
                self.reject_count += 1
                self._write_reject_rec(daly, trailer)
        self.conn.commit()

        print(f"TRANSACTIONS PROCESSED :{self.transaction_count:09d}")
        print(f"TRANSACTIONS REJECTED  :{self.reject_count:09d}")
        return_code = 4 if self.reject_count > 0 else 0
        print("END OF EXECUTION OF PROGRAM CBTRN02C")
        return return_code

    # -- 1000-DALYTRAN-GET-NEXT --------------------------------------------
    def _dalytran_get_next(self):
        cur = self.conn.execute(
            "SELECT * FROM daily_transactions ORDER BY rowid"
        )
        for row in cur:
            yield DalyTranRecord.from_row(row)

    # -- 1500-VALIDATE-TRAN -------------------------------------------------
    def _validate_tran(self, daly: DalyTranRecord, trailer: ValidationTrailer) -> None:
        xref = self._lookup_xref(daly, trailer)
        if trailer.reason == 0 and xref is not None:
            self._lookup_acct(daly, xref, trailer)
        # ADD MORE VALIDATIONS HERE

    # -- 1500-A-LOOKUP-XREF -------------------------------------------------
    def _lookup_xref(self, daly: DalyTranRecord, trailer: ValidationTrailer):
        row = self.conn.execute(
            "SELECT * FROM card_xref WHERE xref_card_num = ?",
            (daly.dalytran_card_num,),
        ).fetchone()
        if row is None:
            trailer.reason = REASON_INVALID_CARD
            trailer.desc = "INVALID CARD NUMBER FOUND"
            return None
        return CardXrefRecord.from_row(row)

    # -- 1500-B-LOOKUP-ACCT -------------------------------------------------
    def _lookup_acct(
        self,
        daly: DalyTranRecord,
        xref: CardXrefRecord,
        trailer: ValidationTrailer,
    ):
        row = self.conn.execute(
            "SELECT * FROM accounts WHERE acct_id = ?",
            (xref.xref_acct_id,),
        ).fetchone()
        if row is None:
            trailer.reason = REASON_ACCT_NOT_FOUND
            trailer.desc = "ACCOUNT RECORD NOT FOUND"
            return None

        acct = AccountRecord.from_row(row)
        temp_bal = (
            acct.acct_curr_cyc_credit
            - acct.acct_curr_cyc_debit
            + daly.dalytran_amt
        )
        if acct.acct_credit_limit >= temp_bal:
            pass
        else:
            trailer.reason = REASON_OVERLIMIT
            trailer.desc = "OVERLIMIT TRANSACTION"

        if acct.acct_expiraion_date >= daly.dalytran_orig_ts[:10]:
            pass
        else:
            trailer.reason = REASON_EXPIRED
            trailer.desc = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"
        return acct

    # -- 2000-POST-TRANSACTION ---------------------------------------------
    def _post_transaction(self, daly: DalyTranRecord) -> None:
        # The xref/account were validated already; re-resolve the account id.
        xref = CardXrefRecord.from_row(
            self.conn.execute(
                "SELECT * FROM card_xref WHERE xref_card_num = ?",
                (daly.dalytran_card_num,),
            ).fetchone()
        )

        tran = TranRecord(
            tran_id=daly.dalytran_id,
            tran_type_cd=daly.dalytran_type_cd,
            tran_cat_cd=daly.dalytran_cat_cd,
            tran_source=daly.dalytran_source,
            tran_desc=daly.dalytran_desc,
            tran_amt=daly.dalytran_amt,
            tran_merchant_id=daly.dalytran_merchant_id,
            tran_merchant_name=daly.dalytran_merchant_name,
            tran_merchant_city=daly.dalytran_merchant_city,
            tran_merchant_zip=daly.dalytran_merchant_zip,
            tran_card_num=daly.dalytran_card_num,
            tran_orig_ts=daly.dalytran_orig_ts,
            tran_proc_ts=self._get_db2_format_timestamp(),
        )

        self._update_tcatbal(daly, xref.xref_acct_id)
        self._update_account_rec(daly, xref.xref_acct_id)
        self._write_transaction_file(tran)

    # -- 2500-WRITE-REJECT-REC ---------------------------------------------
    def _write_reject_rec(self, daly: DalyTranRecord, trailer: ValidationTrailer) -> None:
        self.conn.execute(
            "INSERT INTO rejected_transactions "
            "(dalytran_id, reject_tran_data, validation_fail_reason, "
            " validation_fail_reason_desc) VALUES (?, ?, ?, ?)",
            (
                daly.dalytran_id,
                daly.to_record_string(),
                trailer.reason,
                trailer.desc,
            ),
        )

    # -- 2700-UPDATE-TCATBAL -----------------------------------------------
    def _update_tcatbal(self, daly: DalyTranRecord, acct_id: int) -> None:
        row = self.conn.execute(
            "SELECT * FROM tran_cat_bal WHERE trancat_acct_id = ? "
            "AND trancat_type_cd = ? AND trancat_cd = ?",
            (acct_id, daly.dalytran_type_cd, daly.dalytran_cat_cd),
        ).fetchone()

        if row is None:
            print(
                "TCATBAL record not found for key : "
                f"{acct_id}{daly.dalytran_type_cd}{daly.dalytran_cat_cd}"
                ".. Creating."
            )
            self._create_tcatbal_rec(daly, acct_id)
        else:
            self._update_tcatbal_rec(daly, TranCatBalRecord.from_row(row))

    # -- 2700-A-CREATE-TCATBAL-REC -----------------------------------------
    def _create_tcatbal_rec(self, daly: DalyTranRecord, acct_id: int) -> None:
        rec = TranCatBalRecord(
            trancat_acct_id=acct_id,
            trancat_type_cd=daly.dalytran_type_cd,
            trancat_cd=daly.dalytran_cat_cd,
            tran_cat_bal=_money(daly.dalytran_amt),
        )
        self.conn.execute(
            "INSERT INTO tran_cat_bal "
            "(trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) "
            "VALUES (?, ?, ?, ?)",
            (
                rec.trancat_acct_id,
                rec.trancat_type_cd,
                rec.trancat_cd,
                str(rec.tran_cat_bal),
            ),
        )

    # -- 2700-B-UPDATE-TCATBAL-REC -----------------------------------------
    def _update_tcatbal_rec(self, daly: DalyTranRecord, rec: TranCatBalRecord) -> None:
        rec.tran_cat_bal = _money(rec.tran_cat_bal + daly.dalytran_amt)
        self.conn.execute(
            "UPDATE tran_cat_bal SET tran_cat_bal = ? "
            "WHERE trancat_acct_id = ? AND trancat_type_cd = ? AND trancat_cd = ?",
            (
                str(rec.tran_cat_bal),
                rec.trancat_acct_id,
                rec.trancat_type_cd,
                rec.trancat_cd,
            ),
        )

    # -- 2800-UPDATE-ACCOUNT-REC -------------------------------------------
    def _update_account_rec(self, daly: DalyTranRecord, acct_id: int) -> None:
        row = self.conn.execute(
            "SELECT * FROM accounts WHERE acct_id = ?", (acct_id,)
        ).fetchone()
        if row is None:
            raise AbendError("ACCOUNT RECORD NOT FOUND")  # reason 109

        acct = AccountRecord.from_row(row)
        acct.acct_curr_bal = _money(acct.acct_curr_bal + daly.dalytran_amt)
        if daly.dalytran_amt >= 0:
            acct.acct_curr_cyc_credit = _money(
                acct.acct_curr_cyc_credit + daly.dalytran_amt
            )
        else:
            acct.acct_curr_cyc_debit = _money(
                acct.acct_curr_cyc_debit + daly.dalytran_amt
            )

        self.conn.execute(
            "UPDATE accounts SET acct_curr_bal = ?, acct_curr_cyc_credit = ?, "
            "acct_curr_cyc_debit = ? WHERE acct_id = ?",
            (
                str(acct.acct_curr_bal),
                str(acct.acct_curr_cyc_credit),
                str(acct.acct_curr_cyc_debit),
                acct.acct_id,
            ),
        )

    # -- 2900-WRITE-TRANSACTION-FILE ---------------------------------------
    def _write_transaction_file(self, tran: TranRecord) -> None:
        self.conn.execute(
            "INSERT INTO transactions "
            "(tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
            " tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
            " tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) "
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (
                tran.tran_id,
                tran.tran_type_cd,
                tran.tran_cat_cd,
                tran.tran_source,
                tran.tran_desc,
                str(tran.tran_amt),
                tran.tran_merchant_id,
                tran.tran_merchant_name,
                tran.tran_merchant_city,
                tran.tran_merchant_zip,
                tran.tran_card_num,
                tran.tran_orig_ts,
                tran.tran_proc_ts,
            ),
        )

    # -- Z-GET-DB2-FORMAT-TIMESTAMP ----------------------------------------
    @staticmethod
    def _get_db2_format_timestamp(now: datetime | None = None) -> str:
        """Return a DB2-format timestamp: ``YYYY-MM-DD-HH.MM.SS.NN0000``.

        Mirrors COBOL ``Z-GET-DB2-FORMAT-TIMESTAMP`` which keeps the hundredths
        of a second (COB-MIL, 2 digits) and pads the remainder with ``0000``.
        """
        now = now or datetime.now()
        hundredths = now.microsecond // 10000  # 2-digit COB-MIL
        return (
            f"{now.year:04d}-{now.month:02d}-{now.day:02d}-"
            f"{now.hour:02d}.{now.minute:02d}.{now.second:02d}."
            f"{hundredths:02d}0000"
        )


def create_schema(conn: sqlite3.Connection) -> None:
    """Execute the DDL to create all required SQLite tables."""
    conn.executescript(DDL)


def main(argv: list[str] | None = None) -> int:
    argv = sys.argv[1:] if argv is None else argv
    db_path = argv[0] if argv else "carddemo.db"
    conn = sqlite3.connect(db_path)
    try:
        create_schema(conn)
        return PostTran(conn).run()
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
