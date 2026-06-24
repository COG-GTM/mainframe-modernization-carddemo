"""Extract: read fixed-width records from an ASCII or EBCDIC source file.

The mainframe physical files are fixed-length records with no delimiter.  The
ASCII seed files in ``app/data/ASCII`` happen to be newline-terminated, so both
shapes are supported:

* ``record_length`` slicing for true fixed-length binary files (EBCDIC), and
* newline splitting for the text seed files.
"""

from __future__ import annotations

from collections.abc import Iterator
from dataclasses import dataclass
from pathlib import Path

from .layout import RecordLayout
from .transforms import decode_ebcdic


@dataclass
class RawRecord:
    """A single decoded-but-untyped record, keyed by COBOL field name."""

    line_number: int
    raw: str
    fields: dict[str, str]


def _iter_fixed_length(data: bytes, record_length: int) -> Iterator[bytes]:
    if len(data) % record_length != 0:
        # Tolerate a single trailing newline-terminated text file by stripping.
        if data.endswith(b"\n"):
            data = data.replace(b"\r\n", b"\n")
        # Drop a trailing partial chunk made of only whitespace/newlines.
    for i in range(0, len(data) - record_length + 1, record_length):
        yield data[i : i + record_length]


def extract(
    source: str | Path,
    layout: RecordLayout,
    *,
    encoding: str = "ascii",
    codepage: str = "cp037",
) -> Iterator[RawRecord]:
    """Yield :class:`RawRecord` for each record in ``source``.

    Parameters
    ----------
    source:
        Path to the data file.
    layout:
        The :class:`RecordLayout` describing the record.
    encoding:
        ``"ascii"`` for the text seed files (newline-delimited) or ``"ebcdic"``
        for true fixed-length EBCDIC files.
    codepage:
        EBCDIC code page used when ``encoding == "ebcdic"`` (default cp037).
    """
    path = Path(source)
    reclen = layout.record_length
    offsets = layout.offsets()

    if encoding == "ebcdic":
        data = path.read_bytes()
        for line_number, chunk in enumerate(_iter_fixed_length(data, reclen), start=1):
            text = decode_ebcdic(chunk, codepage)
            yield _split(line_number, text, offsets, reclen)
        return

    # ASCII / text: prefer newline framing, fall back to fixed-length slicing.
    text = path.read_text(encoding="utf-8")
    if "\n" in text.rstrip("\n"):
        for line_number, line in enumerate(text.splitlines(), start=1):
            if not line.strip():
                continue
            yield _split(line_number, line, offsets, reclen)
    else:
        for line_number, chunk in enumerate(
            _iter_fixed_length(text.encode("utf-8"), reclen), start=1
        ):
            yield _split(line_number, chunk.decode("utf-8"), offsets, reclen)


def _split(line_number, text, offsets, reclen) -> RawRecord:
    if len(text) < reclen:
        text = text.ljust(reclen)
    fields = {fld.name: text[start:end] for fld, start, end in offsets}
    return RawRecord(line_number=line_number, raw=text, fields=fields)
