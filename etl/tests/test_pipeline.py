from datetime import datetime
from decimal import Decimal
from pathlib import Path

from carddemo_etl.extract import extract
from carddemo_etl.layout import TRANSACTION_LAYOUT
from carddemo_etl.pipeline import PipelineStats, run_pipeline
from carddemo_etl.transform import transform_record

REPO_ROOT = Path(__file__).resolve().parents[2]
ASCII_FIXTURE = REPO_ROOT / "app" / "data" / "ASCII" / "dailytran.txt"


def test_layout_record_length():
    assert TRANSACTION_LAYOUT.record_length == 350


def test_layout_offsets_are_contiguous():
    offsets = TRANSACTION_LAYOUT.offsets()
    prev_end = 0
    for _, start, end in offsets:
        assert start == prev_end
        prev_end = end
    assert prev_end == 350


def test_extract_first_record_fields():
    rec = next(extract(ASCII_FIXTURE, TRANSACTION_LAYOUT, encoding="ascii"))
    assert rec.fields["TRAN-ID"] == "0000000000683580"
    assert rec.fields["TRAN-TYPE-CD"] == "01"
    assert rec.fields["TRAN-AMT"] == "0000005047G"
    assert rec.fields["TRAN-CARD-NUM"] == "4859452612877065"


def test_transform_first_record():
    rec = next(extract(ASCII_FIXTURE, TRANSACTION_LAYOUT, encoding="ascii"))
    row = transform_record(rec, TRANSACTION_LAYOUT).row
    assert row["tran_id"] == "0000000000683580"
    assert row["tran_type_cd"] == "01"
    assert row["tran_cat_cd"] == "0001"
    assert row["tran_source"] == "POS TERM"
    assert row["tran_amt"] == Decimal("504.77")
    assert row["tran_merchant_id"] == 800000000
    assert row["tran_merchant_name"] == "Abshire-Lowe"
    assert row["tran_orig_ts"] == datetime(2022, 6, 10, 19, 27, 53)
    assert row["tran_proc_ts"] is None  # unprocessed daily tran
    assert "filler" not in row


def test_transform_masks_pan():
    rec = next(extract(ASCII_FIXTURE, TRANSACTION_LAYOUT, encoding="ascii"))
    row = transform_record(rec, TRANSACTION_LAYOUT, mask_pan=True).row
    assert row["tran_card_num"] == "************7065"


def test_full_pipeline_over_fixture():
    stats = PipelineStats()
    rows = list(run_pipeline(ASCII_FIXTURE, TRANSACTION_LAYOUT, stats=stats))
    assert stats.read == 300
    assert stats.transformed == 300
    assert stats.errors == []
    assert len(rows) == 300
    # Primary keys are unique.
    ids = {r["tran_id"] for r in rows}
    assert len(ids) == 300
    # Every amount parsed to a Decimal.
    assert all(isinstance(r["tran_amt"], Decimal) for r in rows)


def test_negative_amount_present_in_fixture():
    rows = list(run_pipeline(ASCII_FIXTURE, TRANSACTION_LAYOUT))
    assert any(r["tran_amt"] < 0 for r in rows)
