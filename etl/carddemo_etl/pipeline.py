"""Pipeline: glue extract + transform together with error accounting."""

from __future__ import annotations

from collections.abc import Iterator
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from .extract import extract
from .layout import RecordLayout
from .transform import TransformError, transform_record


@dataclass
class PipelineStats:
    read: int = 0
    transformed: int = 0
    errors: list[str] = field(default_factory=list)


def run_pipeline(
    source: str | Path,
    layout: RecordLayout,
    *,
    encoding: str = "ascii",
    codepage: str = "cp037",
    mask_pan: bool = False,
    stop_on_error: bool = False,
    stats: PipelineStats | None = None,
) -> Iterator[dict[str, Any]]:
    """Yield transformed rows, collecting per-record errors in ``stats``."""
    stats = stats if stats is not None else PipelineStats()
    for raw in extract(source, layout, encoding=encoding, codepage=codepage):
        stats.read += 1
        try:
            result = transform_record(raw, layout, mask_pan=mask_pan)
        except TransformError as exc:
            stats.errors.append(str(exc))
            if stop_on_error:
                raise
            continue
        stats.transformed += 1
        yield result.row
