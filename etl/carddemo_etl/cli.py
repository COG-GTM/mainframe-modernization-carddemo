"""Command-line entry point for the CardDemo Transaction ETL.

Examples
--------
Dry-run against the ASCII seed data, writing masked JSON::

    python -m carddemo_etl.cli run \\
        --source app/data/ASCII/dailytran.txt \\
        --sink json --out /tmp/transactions.json --mask-pan

Load EBCDIC source into PostgreSQL::

    python -m carddemo_etl.cli run \\
        --source app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS \\
        --encoding ebcdic \\
        --sink postgres --dsn "postgresql://user:pw@localhost/carddemo"
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from .layout import TRANSACTION_LAYOUT
from .load import load_postgres, write_csv, write_json
from .pipeline import PipelineStats, run_pipeline


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="carddemo-etl",
        description="ETL for the CardDemo Transaction entity (CVTRA05Y / TRANSACT).",
    )
    sub = parser.add_subparsers(dest="command", required=True)

    run = sub.add_parser("run", help="extract -> transform -> load")
    run.add_argument("--source", required=True, help="path to the source data file")
    run.add_argument(
        "--encoding",
        choices=["ascii", "ebcdic"],
        default="ascii",
        help="source file encoding (default: ascii)",
    )
    run.add_argument(
        "--codepage", default="cp037", help="EBCDIC code page (default: cp037)"
    )
    run.add_argument(
        "--sink",
        choices=["postgres", "csv", "json"],
        default="json",
        help="destination type (default: json)",
    )
    run.add_argument("--out", help="output path for csv/json sinks")
    run.add_argument("--dsn", help="PostgreSQL DSN for the postgres sink")
    run.add_argument(
        "--mask-pan",
        action="store_true",
        help="mask TRAN-CARD-NUM (keep last 4) - use for non-prod sinks",
    )
    run.add_argument(
        "--no-upsert",
        action="store_true",
        help="postgres sink: INSERT .. ON CONFLICT DO NOTHING instead of UPSERT",
    )
    run.add_argument(
        "--stop-on-error",
        action="store_true",
        help="abort on the first record that fails to transform",
    )

    sub.add_parser("schema", help="print the PostgreSQL CREATE TABLE DDL")
    return parser


def _print_schema() -> int:
    ddl = Path(__file__).resolve().parent.parent / "schema.sql"
    sys.stdout.write(ddl.read_text(encoding="utf-8"))
    return 0


def _run(args: argparse.Namespace) -> int:
    layout = TRANSACTION_LAYOUT
    stats = PipelineStats()
    rows = run_pipeline(
        args.source,
        layout,
        encoding=args.encoding,
        codepage=args.codepage,
        mask_pan=args.mask_pan,
        stop_on_error=args.stop_on_error,
        stats=stats,
    )

    if args.sink == "postgres":
        if not args.dsn:
            print("error: --dsn is required for the postgres sink", file=sys.stderr)
            return 2
        loaded = load_postgres(rows, args.dsn, layout, upsert=not args.no_upsert)
        sink_desc = f"postgres ({loaded} rows)"
    elif args.sink == "csv":
        if not args.out:
            print("error: --out is required for the csv sink", file=sys.stderr)
            return 2
        loaded = write_csv(rows, args.out, layout)
        sink_desc = f"csv -> {args.out} ({loaded} rows)"
    else:  # json
        if not args.out:
            print("error: --out is required for the json sink", file=sys.stderr)
            return 2
        loaded = write_json(rows, args.out)
        sink_desc = f"json -> {args.out} ({loaded} rows)"

    print(
        f"read={stats.read} transformed={stats.transformed} "
        f"errors={len(stats.errors)} loaded -> {sink_desc}",
        file=sys.stderr,
    )
    for err in stats.errors[:10]:
        print(f"  ! {err}", file=sys.stderr)
    if stats.errors and len(stats.errors) > 10:
        print(f"  ... and {len(stats.errors) - 10} more", file=sys.stderr)

    return 1 if stats.errors and args.stop_on_error else 0


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    if args.command == "schema":
        return _print_schema()
    if args.command == "run":
        return _run(args)
    parser.error("unknown command")
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
