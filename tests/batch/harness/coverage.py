"""Statement and paragraph coverage for app/cbl programs from GnuCOBOL traces.

The universe of statements/paragraphs comes from the C that ``cobc -ftraceall``
generates (one ``/* Line: N : VERB ... */`` marker per traced statement); the
executed set comes from the COB_TRACE_FILE of every run.
"""
from __future__ import annotations

import json
import re
from collections import defaultdict
from pathlib import Path

_C_MARK = re.compile(r"/\* Line: (\d+)\s*: (\S+)(?:\s+(\S+))?\s*: (\S+) \*/")
_TRACE = re.compile(
    r"^Program-Id:\s+(\S+)\s+(?:(Paragraph|Section|Entry|Exit):\s+(\S+)|(\S+))\s+Line:\s+(\d+)")


def _universe(src_dir: Path, program: str):
    stmts, paras = set(), {}
    c_file = src_dir / f"{program}.c"
    for m in _C_MARK.finditer(c_file.read_text(errors="replace")):
        line, verb, name, source = int(m[1]), m[2], m[3], m[4]
        if Path(source).stem.upper() != program.upper():
            continue
        if verb == "Paragraph":
            paras[name] = line
        elif verb not in ("Entry", "Section", "Exit"):
            stmts.add(line)
    return stmts, paras


def collect(src_dir: Path, trace_dir: Path, programs: set[str]) -> dict:
    hit_stmts, hit_paras = defaultdict(set), defaultdict(set)
    for trace in trace_dir.glob("*.trc"):
        for raw in trace.read_text(errors="replace").splitlines():
            m = _TRACE.match(raw)
            if not m:
                continue
            prog = m[1].upper()
            if m[2] == "Paragraph":
                hit_paras[prog].add(m[3])
            elif m[4]:
                hit_stmts[prog].add(int(m[5]))
    report = {}
    for program in sorted(programs):
        stmts, paras = _universe(src_dir, program)
        hs = hit_stmts[program.upper()] & stmts
        hp = hit_paras[program.upper()] & set(paras)
        report[program] = {
            "statements": len(stmts), "statements_hit": len(hs),
            "paragraphs": len(paras), "paragraphs_hit": len(hp),
            "missed_lines": sorted(stmts - hs),
            "missed_paragraphs": sorted(set(paras) - hp, key=paras.get),
        }
    return report


def totals(report: dict) -> tuple[int, int]:
    return (sum(r["statements_hit"] for r in report.values()),
            sum(r["statements"] for r in report.values()))


def format_table(report: dict) -> str:
    rows = [f"{'Program':<10} {'Stmts':>6} {'Hit':>6} {'Stmt%':>7} {'Paras':>6} {'Hit':>5} {'Para%':>7}"]
    for prog, r in report.items():
        s = 100 * r["statements_hit"] / r["statements"] if r["statements"] else 100
        p = 100 * r["paragraphs_hit"] / r["paragraphs"] if r["paragraphs"] else 100
        rows.append(f"{prog:<10} {r['statements']:>6} {r['statements_hit']:>6} {s:>6.1f}% "
                    f"{r['paragraphs']:>6} {r['paragraphs_hit']:>5} {p:>6.1f}%")
    hit, total = totals(report)
    ph = sum(r["paragraphs_hit"] for r in report.values())
    pt = sum(r["paragraphs"] for r in report.values())
    rows.append(f"{'TOTAL':<10} {total:>6} {hit:>6} {100 * hit / max(total, 1):>6.1f}% "
                f"{pt:>6} {ph:>5} {100 * ph / max(pt, 1):>6.1f}%")
    return "\n".join(rows)


def write_json(report: dict, path: Path) -> None:
    path.write_text(json.dumps(report, indent=2) + "\n")
