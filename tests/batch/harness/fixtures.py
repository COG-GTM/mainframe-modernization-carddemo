"""Access to the sample datasets shipped in app/data/ASCII."""
from __future__ import annotations

from .cobol import REPO

ASCII = REPO / "app" / "data" / "ASCII"


def ascii_records(name: str, rec_len: int) -> list[str]:
    lines = (ASCII / name).read_text().splitlines()
    return [line.ljust(rec_len)[:rec_len] for line in lines if line.strip()]
