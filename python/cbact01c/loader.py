"""Seed the ``accounts`` table from the CardDemo sample data file.

The sample account master ships as fixed-width records. The ASCII copy lives at
``app/data/ASCII/acctdata.txt`` (300-byte records, one per line); the EBCDIC
copies (``app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS``) are byte-for-byte
300-byte records with no line separators. Both are parsed with the CVACT01Y
layout via :mod:`cbact01c.copybook`.

Usage::

    python -m cbact01c.loader ../../app/data/ASCII/acctdata.txt
    python -m cbact01c.loader --encoding cp037 \\
        ../../app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import Iterator

from sqlalchemy import Engine

from cbact01c.copybook import RECORD_LENGTH, parse_account_record
from cbact01c.db import create_db_engine, create_session_factory, init_schema


def iter_records(path: Path, encoding: str = "utf-8") -> Iterator[str]:
    """Yield fixed-width 300-byte account records from ``path``.

    Handles both newline-delimited files (ASCII sample) and headerless blocked
    files (EBCDIC ``.PS`` datasets) by splitting on RECORD_LENGTH boundaries and
    ignoring any line separators.
    """
    raw = path.read_text(encoding=encoding)
    stripped = raw.replace("\r", "").replace("\n", "")
    for start in range(0, len(stripped), RECORD_LENGTH):
        chunk = stripped[start : start + RECORD_LENGTH]
        if chunk.strip():
            yield chunk


def load_accounts(
    engine: Engine,
    path: Path,
    encoding: str = "utf-8",
    *,
    create_schema: bool = True,
) -> int:
    """Parse ``path`` and upsert every account into the DB. Returns the count."""
    if create_schema:
        init_schema(engine)

    session_factory = create_session_factory(engine)
    count = 0
    with session_factory() as session:
        for record in iter_records(path, encoding=encoding):
            account = parse_account_record(record)
            session.merge(account)  # merge = insert or update by primary key
            count += 1
        session.commit()
    return count


def _parse_args(argv: list[str] | None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed the accounts table.")
    parser.add_argument("data_file", type=Path, help="Path to the account data file")
    parser.add_argument(
        "--encoding",
        default="utf-8",
        help="Encoding of the data file (e.g. utf-8 for ASCII, cp037 for EBCDIC)",
    )
    parser.add_argument(
        "--db-url",
        default=None,
        help="SQLAlchemy DB URL (default: env CBACT01C_DB_URL or sqlite:///cbact01c.db)",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = _parse_args(argv)
    if not args.data_file.exists():
        print(f"data file not found: {args.data_file}", file=sys.stderr)
        return 1
    engine = create_db_engine(args.db_url)
    count = load_accounts(engine, args.data_file, encoding=args.encoding)
    print(f"Loaded {count} account(s) into {engine.url}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
