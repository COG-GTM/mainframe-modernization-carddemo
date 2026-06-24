"""Load: write transformed rows to a sink (PostgreSQL, CSV, or JSON).

The PostgreSQL loader is the primary, production-oriented target and matches
``etl/schema.sql``.  CSV/JSON sinks require no database and are used for dry
runs, debugging and equivalence testing.
"""

from __future__ import annotations

import csv
import json
from collections.abc import Iterable
from datetime import datetime
from decimal import Decimal
from pathlib import Path
from typing import Any

from .layout import RecordLayout

TABLE_NAME = "transactions"
PRIMARY_KEY = "tran_id"


def _columns(layout: RecordLayout) -> list[str]:
    return [f.column for f in layout.fields if f.name != "FILLER"]


def _json_default(value: Any) -> Any:
    if isinstance(value, Decimal):
        return str(value)
    if isinstance(value, datetime):
        return value.isoformat()
    raise TypeError(f"not serialisable: {type(value)!r}")


def write_json(rows: Iterable[dict[str, Any]], path: str | Path) -> int:
    rows = list(rows)
    Path(path).write_text(
        json.dumps(rows, indent=2, default=_json_default), encoding="utf-8"
    )
    return len(rows)


def write_csv(rows: Iterable[dict[str, Any]], path: str | Path, layout: RecordLayout) -> int:
    cols = _columns(layout)
    count = 0
    with Path(path).open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=cols)
        writer.writeheader()
        for row in rows:
            writer.writerow(row)
            count += 1
    return count


def load_postgres(
    rows: Iterable[dict[str, Any]],
    dsn: str,
    layout: RecordLayout,
    *,
    batch_size: int = 1000,
    upsert: bool = True,
) -> int:
    """Bulk-load rows into PostgreSQL using an idempotent UPSERT.

    Requires ``psycopg2`` (see ``etl/requirements.txt``).  Imported lazily so
    the rest of the package works without the database driver installed.
    """
    try:
        import psycopg2
        from psycopg2.extras import execute_values
    except ImportError as exc:  # pragma: no cover - environment dependent
        raise RuntimeError(
            "psycopg2 is required for the postgres sink. "
            "Install it with `pip install -r etl/requirements.txt`."
        ) from exc

    cols = _columns(layout)
    non_pk = [c for c in cols if c != PRIMARY_KEY]
    col_list = ", ".join(cols)
    if upsert:
        updates = ", ".join(f"{c} = EXCLUDED.{c}" for c in non_pk)
        conflict = (
            f"ON CONFLICT ({PRIMARY_KEY}) DO UPDATE SET {updates}, updated_at = now()"
        )
    else:
        conflict = f"ON CONFLICT ({PRIMARY_KEY}) DO NOTHING"

    sql = f"INSERT INTO {TABLE_NAME} ({col_list}) VALUES %s {conflict}"

    total = 0
    conn = psycopg2.connect(dsn)
    try:
        with conn:
            with conn.cursor() as cur:
                batch: list[tuple[Any, ...]] = []
                for row in rows:
                    batch.append(tuple(row.get(c) for c in cols))
                    if len(batch) >= batch_size:
                        execute_values(cur, sql, batch)
                        total += len(batch)
                        batch.clear()
                if batch:
                    execute_values(cur, sql, batch)
                    total += len(batch)
    finally:
        conn.close()
    return total
