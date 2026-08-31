#!/usr/bin/env python3
"""Regenerable inventory of the CardDemo batch estate.

Reads app/jcl, app/proc, app/cbl and app/cpy and writes INVENTORY.md. Every
number in the generated document comes from this parse; nothing is
hand-written. Re-running the tool on an unchanged tree yields a byte-identical
file (all iteration is over sorted collections and no clock or environment
value is embedded).

Usage:
    python3 airlift-batch/tools/inventory.py            # write INVENTORY.md
    python3 airlift-batch/tools/inventory.py --check     # fail if it would change
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from copybook import Field, parse_file  # noqa: E402

REPO = Path(__file__).resolve().parents[2]
APP = REPO / "app"
OUTPUT = REPO / "airlift-batch" / "INVENTORY.md"

JOB_RE = re.compile(r"^//(\S+)\s+JOB\b", re.IGNORECASE)
EXEC_RE = re.compile(r"^//(\S*)\s+EXEC\s+(.*)$", re.IGNORECASE)
DD_RE = re.compile(r"^//(\S+)\s+DD\s*(.*)$", re.IGNORECASE)
CONT_RE = re.compile(r"^//\s+(\S.*)$")
DSN_RE = re.compile(r"DSN=([A-Z0-9$#@.&()+\-]+)", re.IGNORECASE)
PGM_RE = re.compile(r"\bPGM=([A-Z0-9$#@]+)", re.IGNORECASE)
PROC_RE = re.compile(r"\b(?:PROC=)?([A-Z0-9$#@]+)\s*(?:,|$)", re.IGNORECASE)

PROGRAM_ID_RE = re.compile(r"^\s*PROGRAM-ID\s*\.\s*([A-Z0-9$#@-]+)", re.IGNORECASE)
COPY_RE = re.compile(r"\bCOPY\s+([A-Z0-9$#@-]+)", re.IGNORECASE)
CALL_RE = re.compile(r"\bCALL\s+'([A-Z0-9$#@-]+)'", re.IGNORECASE)
SELECT_RE = re.compile(
    r"\bSELECT\s+([A-Z0-9$#@-]+)\s+ASSIGN\s+TO\s+(?:EXTERNAL\s+)?([A-Z0-9$#@-]+)",
    re.IGNORECASE,
)
ORG_RE = re.compile(r"\bORGANIZATION\s+IS\s+([A-Z]+)", re.IGNORECASE)
OPEN_RE = re.compile(r"\bOPEN\s+(INPUT|OUTPUT|I-O|EXTEND)\s+([A-Z0-9$#@-]+)", re.IGNORECASE)
EXEC_CICS_RE = re.compile(r"\bEXEC\s+CICS\b", re.IGNORECASE)
EXEC_SQL_RE = re.compile(r"\bEXEC\s+SQL\b", re.IGNORECASE)


@dataclass
class DD:
    name: str
    dsn: str | None
    kind: str


@dataclass
class Step:
    name: str
    program: str | None
    proc: str | None
    parm: str | None
    dds: list[DD] = field(default_factory=list)


@dataclass
class Job:
    member: str
    name: str
    path: str
    steps: list[Step] = field(default_factory=list)


@dataclass
class Program:
    member: str
    name: str
    total_lines: int
    comment_lines: int
    code_lines: int
    copybooks: list[str]
    calls: list[str]
    files: list[tuple[str, str, str]]  # (logical name, dd name, organization)
    open_modes: dict[str, str]
    cics: int
    sql: int
    comp3: int


@dataclass
class Copybook:
    member: str
    records: list[Field]


def _jcl_statements(text: str) -> list[str]:
    """Fold JCL continuation lines into single logical statements."""
    statements: list[str] = []
    for raw in text.splitlines():
        line = raw[:72].rstrip()
        if not line.startswith("//") or line.startswith("//*"):
            continue
        if statements and CONT_RE.match(line) and not DD_RE.match(line):
            statements[-1] = statements[-1].rstrip(",") + "," + CONT_RE.match(line).group(1)
            continue
        statements.append(line)
    return statements


def classify_dsn(dsn: str | None, gdg_bases: set[str], vsam: set[str]) -> str:
    if dsn is None:
        return "inline/sysout"
    base = dsn.split("(")[0].upper()
    if "(" in dsn and re.search(r"\((?:[+-]?\d+)\)", dsn):
        return "GDG"
    if base in gdg_bases:
        return "GDG"
    if base in vsam or ".VSAM." in base:
        return "VSAM"
    return "sequential"


def parse_jcl(path: Path, gdg_bases: set[str], vsam: set[str]) -> Job:
    statements = _jcl_statements(path.read_text(encoding="utf-8", errors="replace"))
    job = Job(
        member=path.name,
        name=path.stem.upper(),
        path=str(path.relative_to(REPO)),
    )
    current: Step | None = None
    for statement in statements:
        job_match = JOB_RE.match(statement)
        if job_match:
            job.name = job_match.group(1).upper()
            continue
        exec_match = EXEC_RE.match(statement)
        if exec_match:
            body = exec_match.group(2)
            pgm = PGM_RE.search(body)
            proc = None
            if not pgm:
                proc_match = PROC_RE.search(body)
                proc = proc_match.group(1).upper() if proc_match else None
            parm = None
            parm_match = re.search(r"PARM='([^']*)'", body)
            if parm_match:
                parm = parm_match.group(1)
            current = Step(
                name=exec_match.group(1).upper() or f"STEP{len(job.steps) + 1}",
                program=pgm.group(1).upper() if pgm else None,
                proc=proc,
                parm=parm,
            )
            job.steps.append(current)
            continue
        dd_match = DD_RE.match(statement)
        if dd_match and current is not None:
            body = dd_match.group(2)
            dsn = DSN_RE.search(body)
            dsn_value = dsn.group(1).upper() if dsn else None
            current.dds.append(
                DD(
                    name=dd_match.group(1).upper(),
                    dsn=dsn_value,
                    kind=classify_dsn(dsn_value, gdg_bases, vsam),
                )
            )
    return job


def scan_idcams(paths: list[Path]) -> tuple[set[str], set[str]]:
    """Collect GDG bases and VSAM clusters from IDCAMS DEFINE statements."""
    gdg: set[str] = set()
    vsam: set[str] = set()
    kinds = r"GDG|GENERATIONDATAGROUP|CLUSTER|AIX|ALTERNATEINDEX|PATH"
    define_re = re.compile(rf"DEFINE\s+({kinds})", re.IGNORECASE)
    name_re = re.compile(r"NAME\(\s*([A-Z0-9$#@.]+)", re.IGNORECASE)
    for path in paths:
        text = path.read_text(encoding="utf-8", errors="replace")
        blocks = re.split(rf"(?=DEFINE\s+(?:{kinds}))", text, flags=re.IGNORECASE)
        for block in blocks:
            kind = define_re.search(block)
            if not kind:
                continue
            name = name_re.search(block)
            if not name:
                continue
            if kind.group(1).upper() in ("GDG", "GENERATIONDATAGROUP"):
                gdg.add(name.group(1).upper())
            else:
                vsam.add(name.group(1).upper())
    return gdg, vsam


def parse_cobol(path: Path) -> Program:
    text = path.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    comment_lines = sum(1 for line in lines if len(line) > 6 and line[6] in "*/")
    blank_lines = sum(1 for line in lines if not line.strip())
    code = "\n".join(
        line[6:72] for line in lines if not (len(line) > 6 and line[6] in "*/")
    )
    program_id = PROGRAM_ID_RE.search(code)
    files: list[tuple[str, str, str]] = []
    for select in SELECT_RE.finditer(code):
        tail = code[select.end() : select.end() + 400]
        org = ORG_RE.search(tail)
        files.append(
            (
                select.group(1).upper(),
                select.group(2).upper(),
                (org.group(1).upper() if org else "SEQUENTIAL"),
            )
        )
    # OPEN names the COBOL file, not the DD; resolve through the SELECT map so
    # the inventory can report the access mode per DD name.
    dd_of_file = {logical: dd for logical, dd, _ in files}
    open_modes: dict[str, str] = {}
    for opened in OPEN_RE.finditer(code):
        dd_name = dd_of_file.get(opened.group(2).upper())
        if dd_name is not None:
            open_modes.setdefault(dd_name, opened.group(1).upper())
    return Program(
        member=path.name,
        name=(program_id.group(1).upper() if program_id else path.stem.upper()),
        total_lines=len(lines),
        comment_lines=comment_lines,
        code_lines=len(lines) - comment_lines - blank_lines,
        copybooks=sorted({m.group(1).upper() for m in COPY_RE.finditer(code)}),
        calls=sorted({m.group(1).upper() for m in CALL_RE.finditer(code)}),
        files=files,
        open_modes=open_modes,
        cics=len(EXEC_CICS_RE.findall(code)),
        sql=len(EXEC_SQL_RE.findall(code)),
        comp3=len(re.findall(r"\bCOMP-3\b|\bPACKED-DECIMAL\b", code, re.IGNORECASE)),
    )


def difficulty(program: Program) -> tuple[int, str]:
    """Deterministic lift-difficulty score.

    score = code_lines/50 + 3*files + 2*distinct CALL targets
            + 5*(1 if EXEC CICS present) + 5*(1 if EXEC SQL present)
    """
    score = (
        program.code_lines // 50
        + 3 * len(program.files)
        + 2 * len(program.calls)
        + (5 if program.cics else 0)
        + (5 if program.sql else 0)
    )
    reasons = [f"{program.code_lines} code lines", f"{len(program.files)} files"]
    if program.calls:
        reasons.append(f"{len(program.calls)} CALL targets")
    if program.cics:
        reasons.append("EXEC CICS")
    if program.sql:
        reasons.append("EXEC SQL")
    return score, ", ".join(reasons)


def dataset_flow(
    jobs: list[Job], programs: dict[str, Program]
) -> list[tuple[str, str, str, str, str]]:
    """Producer -> consumer edges between jobs, keyed on dataset base name.

    Direction comes from the COBOL program's own OPEN statement for the DD, so
    only steps that execute a program in app/cbl contribute edges. An edge is
    emitted when the producer writes (OUTPUT/EXTEND/I-O) and the consumer
    reads; the mode pair is reported so an I-O update is not mistaken for a
    creation. Edges where both sides are I-O are dropped: they carry no
    ordering information.
    """
    writes: dict[str, set[tuple[str, str]]] = {}
    reads: dict[str, set[tuple[str, str]]] = {}
    for job in jobs:
        for step in job.steps:
            program = programs.get(step.program or "")
            if program is None:
                continue
            for dd in step.dds:
                if dd.dsn is None:
                    continue
                base = dd.dsn.split("(")[0]
                mode = program.open_modes.get(dd.name)
                if mode is None:
                    continue
                if mode in ("OUTPUT", "EXTEND", "I-O"):
                    writes.setdefault(base, set()).add((job.name, mode))
                if mode in ("INPUT", "I-O"):
                    reads.setdefault(base, set()).add((job.name, mode))
    edges: set[tuple[str, str, str, str, str]] = set()
    for dataset, producers in writes.items():
        for consumer, consumer_mode in reads.get(dataset, set()):
            for producer, producer_mode in producers:
                if producer == consumer:
                    continue
                if producer_mode == "I-O" and consumer_mode == "I-O":
                    continue
                edges.add((producer, consumer, dataset, producer_mode, consumer_mode))
    return sorted(edges)


def execution_order(edges: list[tuple[str, str, str, str, str]]) -> list[str]:
    """Topologically sort the job graph, breaking ties alphabetically."""
    nodes = sorted({edge[0] for edge in edges} | {edge[1] for edge in edges})
    incoming = {node: {e[0] for e in edges if e[1] == node} for node in nodes}
    order: list[str] = []
    remaining = set(nodes)
    while remaining:
        ready = sorted(n for n in remaining if not (incoming[n] & remaining))
        if not ready:
            order.extend(sorted(remaining))
            break
        order.extend(ready)
        remaining -= set(ready)
    return order


def render(
    jobs: list[Job],
    programs: dict[str, Program],
    copybooks: list[Copybook],
    gdg_bases: set[str],
    vsam: set[str],
) -> str:
    cobol_programs = sorted(programs.values(), key=lambda p: p.name)
    batch_programs = [p for p in cobol_programs if p.cics == 0]
    online_programs = [p for p in cobol_programs if p.cics > 0]
    jcl_steps = sum(len(job.steps) for job in jobs)
    dd_total = sum(len(step.dds) for job in jobs for step in job.steps)
    executed = {
        step.program
        for job in jobs
        for step in job.steps
        if step.program in programs
    }

    out: list[str] = []
    add = out.append
    add("# CardDemo batch estate inventory")
    add("")
    add(
        "Generated by `airlift-batch/tools/inventory.py` from `app/jcl`, `app/proc`, "
        "`app/cbl` and `app/cpy`. Do not edit by hand: run "
        "`make -C airlift-batch inventory` (or `--check` in CI) to regenerate. "
        "Every count below is produced by that parse."
    )
    add("")
    add("## 1. Totals")
    add("")
    add("| Artefact | Count |")
    add("| --- | --- |")
    add(f"| JCL and PROC members parsed (`app/jcl`, `app/proc`) | {len(jobs)} |")
    add(f"| JCL/PROC steps | {jcl_steps} |")
    add(f"| DD statements | {dd_total} |")
    add(f"| COBOL programs (`app/cbl`) | {len(cobol_programs)} |")
    add(f"| ... batch (no EXEC CICS) | {len(batch_programs)} |")
    add(f"| ... online (EXEC CICS present) | {len(online_programs)} |")
    add(f"| COBOL programs invoked by a JCL step | {len(executed)} |")
    add(f"| Copybooks (`app/cpy`) | {len(copybooks)} |")
    add(f"| GDG bases defined by IDCAMS | {len(gdg_bases)} |")
    add(f"| VSAM clusters/AIX/paths defined by IDCAMS | {len(vsam)} |")
    add("")

    add("## 2. Batch jobs: step -> program -> dataset bindings")
    add("")
    add(
        "Only JCL members whose steps execute a program found in `app/cbl` are "
        "listed here; utility-only members (IDCAMS define/load, SORT, SDSF) are "
        "summarised in section 3."
    )
    add("")
    for job in jobs:
        if not any(step.program in programs for step in job.steps):
            continue
        add(f"### {job.name} (`{job.path}`)")
        add("")
        add("| Step | Program | PARM | DD | Dataset | Type | COBOL OPEN |")
        add("| --- | --- | --- | --- | --- | --- | --- |")
        for step in job.steps:
            program = programs.get(step.program or "")
            if program is None:
                continue
            for dd in step.dds:
                mode = program.open_modes.get(dd.name, "-")
                add(
                    f"| {step.name} | {step.program} | {step.parm or '-'} | {dd.name} "
                    f"| {dd.dsn or '-'} | {dd.kind} | {mode} |"
                )
        add("")

    add("## 3. Utility and definition members")
    add("")
    add("| Member | Job | Steps | Programs |")
    add("| --- | --- | --- | --- |")
    for job in jobs:
        if any(step.program in programs for step in job.steps):
            continue
        utilities = sorted({step.program or f"PROC={step.proc}" for step in job.steps})
        add(f"| {job.path} | {job.name} | {len(job.steps)} | {', '.join(utilities)} |")
    add("")

    add("## 4. Nightly chain (derived from dataset producer/consumer edges)")
    add("")
    add(
        "Edges are derived mechanically: a job *writes* a dataset if the COBOL "
        "program in that step OPENs the matching DD OUTPUT/EXTEND/I-O, and "
        "*reads* it if it OPENs INPUT/I-O. Utility steps (IDCAMS load, SORT, "
        "SDSF) contribute no edges because their direction is not visible in "
        "COBOL source, so the real production chain also depends on the "
        "file-definition jobs in section 3."
    )
    add("")
    edges = dataset_flow(jobs, programs)
    add("| Producer job | Producer OPEN | Consumer job | Consumer OPEN | Dataset |")
    add("| --- | --- | --- | --- | --- |")
    for producer, consumer, dataset, producer_mode, consumer_mode in edges:
        add(
            f"| {producer} | {producer_mode} | {consumer} | {consumer_mode} "
            f"| {dataset} |"
        )
    add("")
    add("Topological order of the jobs in that graph: " + " -> ".join(execution_order(edges)))
    add("")

    add("## 5. Programs")
    add("")
    add("| Program | Member | Total lines | Code lines | Comment lines | Files | COPY | CALL | EXEC CICS | EXEC SQL | COMP-3 refs |")
    add("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
    for program in cobol_programs:
        add(
            f"| {program.name} | {program.member} | {program.total_lines} "
            f"| {program.code_lines} | {program.comment_lines} | {len(program.files)} "
            f"| {len(program.copybooks)} | {len(program.calls)} | {program.cics} "
            f"| {program.sql} | {program.comp3} |"
        )
    add("")

    add("## 6. CALL graph")
    add("")
    callers = [p for p in cobol_programs if p.calls]
    add("| Caller | Callee | Callee in app/cbl |")
    add("| --- | --- | --- |")
    for program in callers:
        for callee in program.calls:
            add(f"| {program.name} | {callee} | {'yes' if callee in programs else 'no'} |")
    add("")

    add("## 7. Copybook usage")
    add("")
    usage: dict[str, list[str]] = {}
    for program in cobol_programs:
        for copybook in program.copybooks:
            usage.setdefault(copybook, []).append(program.name)
    add("| Copybook | Records | Record length(s) | Fields | Used by |")
    add("| --- | --- | --- | --- | --- |")
    for copybook in copybooks:
        member = copybook.member.rsplit(".", 1)[0].upper()
        names = ", ".join(record.name for record in copybook.records) or "-"
        lengths = ", ".join(str(record.size) for record in copybook.records) or "-"
        fields = sum(len(record.leaves()) for record in copybook.records)
        used_by = ", ".join(sorted(usage.get(member, []))) or "-"
        add(f"| {copybook.member} | {names} | {lengths} | {fields} | {used_by} |")
    add("")

    add("## 8. Lift-difficulty ranking")
    add("")
    add(
        "`score = code_lines/50 + 3*files + 2*distinct CALL targets + 5 if EXEC "
        "CICS + 5 if EXEC SQL`, integer division, ascending. The ranking is a "
        "sizing aid computed from the parse above, not a judgement about "
        "business importance."
    )
    add("")
    add("| Rank | Program | Score | Drivers |")
    add("| --- | --- | --- | --- |")
    ranked = sorted(
        ((difficulty(p), p) for p in cobol_programs),
        key=lambda item: (item[0][0], item[1].name),
    )
    for index, ((score, reasons), program) in enumerate(ranked, start=1):
        add(f"| {index} | {program.name} | {score} | {reasons} |")
    add("")
    return "\n".join(out) + "\n"


def collect() -> str:
    jcl_paths = sorted(
        [p for p in (APP / "jcl").iterdir() if p.suffix.lower() == ".jcl"],
        key=lambda p: p.name,
    )
    proc_paths = sorted([p for p in (APP / "proc").iterdir() if p.is_file()], key=lambda p: p.name)
    gdg_bases, vsam = scan_idcams(jcl_paths + proc_paths)
    jobs = [parse_jcl(path, gdg_bases, vsam) for path in jcl_paths]
    jobs += [parse_jcl(path, gdg_bases, vsam) for path in proc_paths]
    jobs.sort(key=lambda job: (job.member, job.name))
    cbl_paths = sorted(
        [p for p in (APP / "cbl").iterdir() if p.suffix.lower() == ".cbl"],
        key=lambda p: p.name,
    )
    programs = {}
    for path in cbl_paths:
        program = parse_cobol(path)
        programs[program.name] = program
    cpy_paths = sorted(
        [p for p in (APP / "cpy").iterdir() if p.is_file()], key=lambda p: p.name
    )
    copybooks = [Copybook(member=path.name, records=parse_file(path)) for path in cpy_paths]
    return render(jobs, programs, copybooks, gdg_bases, vsam)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="fail if INVENTORY.md is stale")
    args = parser.parse_args()
    rendered = collect()
    if args.check:
        current = OUTPUT.read_text(encoding="utf-8") if OUTPUT.exists() else ""
        if current != rendered:
            print(f"{OUTPUT} is not byte-identical to a fresh run", file=sys.stderr)
            return 1
        print(f"{OUTPUT} is up to date")
        return 0
    OUTPUT.write_text(rendered, encoding="utf-8")
    print(f"wrote {OUTPUT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
