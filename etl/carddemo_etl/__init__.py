"""CardDemo Transaction ETL.

Extract / transform / load pipeline that migrates the COBOL ``TRANSACT`` VSAM
file (copybook ``CVTRA05Y``) into a modern PostgreSQL ``transactions`` table,
following the field mapping produced by the ``data-mapper`` skill.
"""

from .layout import TRANSACTION_LAYOUT, RecordLayout
from .pipeline import PipelineStats, run_pipeline

__all__ = ["TRANSACTION_LAYOUT", "RecordLayout", "PipelineStats", "run_pipeline"]

__version__ = "0.1.0"
