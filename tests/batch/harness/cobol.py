"""Compile CardDemo batch programs with GnuCOBOL and run them like JCL steps.

Programs under test are compiled as modules with ``-ftraceall`` and started
through the RUNPGM driver (support/RUNPGM.cbl), which passes a JCL-style PARM.
Every run writes a GnuCOBOL trace file that ``coverage.py`` turns into
statement/paragraph coverage for the original sources in app/cbl.
"""
from __future__ import annotations

import itertools
import os
import shutil
import subprocess
from dataclasses import dataclass
from pathlib import Path

from .records import Layout

REPO = Path(__file__).resolve().parents[3]
APP_CBL = REPO / "app" / "cbl"
APP_CPY = REPO / "app" / "cpy"
SUPPORT = Path(__file__).resolve().parents[1] / "support"
COBC_FLAGS = ["-std=ibm", "-fsign=EBCDIC", "-I", str(APP_CPY)]
ABEND_RC = 99  # set by support/CEE3ABD.cbl


@dataclass
class RunResult:
    returncode: int
    stdout: str
    stderr: str

    @property
    def lines(self) -> list[str]:
        return [line.rstrip() for line in self.stdout.splitlines()]

    @property
    def abended(self) -> bool:
        return self.returncode == ABEND_RC and "CEE3ABD: ABEND U0999" in self.stdout


class Cobol:
    def __init__(self, build_dir: Path):
        self.build_dir = build_dir
        self.lib_dir = build_dir / "lib"
        self.src_dir = build_dir / "src"
        self.trace_dir = build_dir / "traces"
        for d in (self.lib_dir, self.src_dir, self.trace_dir):
            d.mkdir(parents=True, exist_ok=True)
        self._built: set[str] = set()
        self._seq = itertools.count()
        self.traced_programs: set[str] = set()

    # ---------------------------------------------------------------- build
    def _cobc(self, *args: str) -> None:
        proc = subprocess.run(["cobc", *args], capture_output=True, text=True)
        if proc.returncode != 0:
            raise RuntimeError(f"cobc {' '.join(args)} failed:\n{proc.stdout}{proc.stderr}")

    def _support(self, name: str, executable: bool = False) -> Path:
        out = self.lib_dir / (name if executable else f"{name}.so")
        if name not in self._built:
            mode = "-x" if executable else "-m"
            self._cobc(mode, *COBC_FLAGS, "-o", str(out), str(SUPPORT / f"{name}.cbl"))
            self._built.add(name)
        return out

    def program(self, name: str) -> None:
        """Compile app/cbl/<name> as a traced module (once per session)."""
        if name in self._built:
            return
        src = next(p for p in APP_CBL.iterdir() if p.stem.upper() == name.upper())
        self._cobc("-m", *COBC_FLAGS, "-ftraceall", f"-save-temps={self.src_dir}",
                   "-o", str(self.lib_dir / f"{name}.so"), str(src))
        self._built.add(name)
        self.traced_programs.add(name)

    def _idxutil(self, layout: Layout) -> Path:
        alt = layout.alt_key
        tag = f"IX{layout.rec_len}K{layout.key_len}" + (f"A{alt[0]}L{alt[1]}" if alt else "")
        exe = self.lib_dir / tag
        if tag in self._built:
            return exe
        fields = [(layout.key_len, "IDX-KEY")]
        if alt:
            off, ln = alt
            fields = [(layout.key_len, "IDX-KEY"), (off - layout.key_len, "FILLER"),
                      (ln, "IDX-ALT-KEY")]
        used = sum(n for n, _ in fields)
        if layout.rec_len > used:
            fields.append((layout.rec_len - used, "FILLER"))
        src = (SUPPORT / "IDXUTIL.cbl.in").read_text()
        src = (src.replace("@PROGRAM@", tag).replace("@RECLEN@", str(layout.rec_len))
               .replace("@ALTKEY@", "                  ALTERNATE RECORD KEY IS IDX-ALT-KEY"
                        if alt else "")
               .replace("@FIELDS@", "\n".join(
                   f"          05 {n:<20} PIC X({ln})." for ln, n in fields if ln > 0)))
        path = self.src_dir / f"{tag}.cbl"
        path.write_text(src)
        self._cobc("-x", "-std=ibm", "-o", str(exe), str(path))
        self._built.add(tag)
        return exe

    # ------------------------------------------------------------ datasets
    def load_indexed(self, path: Path, layout: Layout, records: list[str]) -> Path:
        """Create a KSDS stand-in at ``path`` holding ``records`` (sorted by key)."""
        for r in records:
            if len(r) != layout.rec_len:
                raise ValueError(f"{layout.name}: record length {len(r)} != {layout.rec_len}")
        text = path.with_suffix(".load.txt")
        text.write_text("".join(r + "\n" for r in sorted(records, key=layout.key)))
        env = {**os.environ, "IDXUTIL_MODE": "LOAD", "IDXTXT": str(text), "IDXFILE": str(path)}
        proc = subprocess.run([str(self._idxutil(layout))], env=env, capture_output=True, text=True)
        if proc.returncode != 0:
            raise RuntimeError(f"loading {path} failed: {proc.stdout}{proc.stderr}")
        return path

    def dump_indexed(self, path: Path, layout: Layout) -> list[str]:
        text = path.with_suffix(".dump.txt")
        env = {**os.environ, "IDXUTIL_MODE": "DUMP", "IDXTXT": str(text), "IDXFILE": str(path)}
        proc = subprocess.run([str(self._idxutil(layout))], env=env, capture_output=True, text=True)
        if proc.returncode != 0:
            raise RuntimeError(f"dumping {path} failed: {proc.stdout}{proc.stderr}")
        return [line.ljust(layout.rec_len) for line in text.read_text().splitlines()]

    @staticmethod
    def write_sequential(path: Path, records: list[str], rec_len: int) -> Path:
        """Fixed-length RECFM=F file (no line terminators), as SEQUENTIAL FDs expect."""
        for r in records:
            if len(r) != rec_len:
                raise ValueError(f"record length {len(r)} != {rec_len}")
        path.write_bytes("".join(records).encode("ascii"))
        return path

    @staticmethod
    def read_sequential(path: Path, rec_len: int) -> list[str]:
        data = path.read_bytes().decode("ascii")
        assert len(data) % rec_len == 0, f"{path}: {len(data)} not a multiple of {rec_len}"
        return [data[i:i + rec_len] for i in range(0, len(data), rec_len)]

    # ----------------------------------------------------------------- run
    def _run(self, exe: Path, env: dict, current_date: str | None) -> RunResult:
        self._support("CEE3ABD")
        trace = self.trace_dir / f"run{next(self._seq):05d}.trc"
        full_env = {**os.environ, **env, "COB_LIBRARY_PATH": str(self.lib_dir),
                    "COB_SET_TRACE": "Y", "COB_TRACE_FILE": str(trace)}
        if current_date:
            full_env["COB_CURRENT_DATE"] = current_date
        proc = subprocess.run([str(exe)], env=full_env, capture_output=True, text=True,
                              timeout=120)
        return RunResult(proc.returncode, proc.stdout, proc.stderr)

    def run(self, program: str, dds: dict[str, Path | str], parm: str = "",
            current_date: str | None = None, env: dict | None = None) -> RunResult:
        """Run ``program`` like ``EXEC PGM=program,PARM=parm`` with ``dds`` as DD statements."""
        self.program(program)
        exe = self._support("RUNPGM", executable=True)
        run_env = {name: str(path) for name, path in dds.items()}
        run_env.update({"RUNPGM_PROGRAM": program, "RUNPGM_PARM": parm, **(env or {})})
        return self._run(exe, run_env, current_date)

    def run_driver(self, driver: str, programs: list[str], env: dict,
                   current_date: str | None = None) -> RunResult:
        """Run a test driver from support/ that CALLs the given programs."""
        for p in programs:
            self.program(p)
        return self._run(self._support(driver, executable=True), env, current_date)

    def stub(self, name: str) -> None:
        self._support(name)


def cobc_available() -> bool:
    return shutil.which("cobc") is not None
