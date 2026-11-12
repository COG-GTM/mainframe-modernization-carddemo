"""Run CardDemo's CICS online programs with GnuCOBOL against a tiny CICS stand-in.

``translate`` plays the part of the CICS translator: every ``EXEC CICS ... END-EXEC``
becomes ``CALL 'CICSSIM' USING '<descriptor>' <args>`` on the same source lines, so
GnuCOBOL's trace output still points at the original line numbers in app/cbl.
support/cics/cicssim.c implements those commands over fixed-length datasets, BMS
map buffers and a small event log that the tests inspect.
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
from dataclasses import dataclass, field
from pathlib import Path

from .records import Field, Layout, N, X

CICS_SUPPORT = Path(__file__).resolve().parents[1] / "support" / "cics"

RESP = {"NORMAL": 0, "ERROR": 1, "FILENOTFOUND": 12, "NOTFND": 13, "DUPREC": 14,
        "DUPKEY": 15, "INVREQ": 16, "IOERR": 17, "NOSPACE": 18, "NOTOPEN": 19,
        "ENDFILE": 20, "ILLOGIC": 21, "LENGERR": 22, "MAPFAIL": 36, "PGMIDERR": 27,
        "DISABLED": 84}

AID = {"ENTER": 0x7D, "CLEAR": 0x6D, "PA1": 0x6C, "PA2": 0x6E, "PA3": 0x6B}
AID.update({f"PF{i + 1}": b for i, b in enumerate(
    [0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0x7A, 0x7B, 0x7C,
     0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0x4A, 0x4B, 0x4C])})

# Options that carry an argument; everything else is a flag. LENGTH/KEYLENGTH are
# dropped: the simulator uses the size of the INTO/FROM/RIDFLD/COMMAREA argument.
_ARG_OPTS = {"DATASET": "DS", "FILE": "DS", "INTO": "IN", "FROM": "FR", "RIDFLD": "RI",
             "RESP": "RS", "RESP2": "R2", "MAP": "MP", "MAPSET": "MS", "TRANSID": "TR",
             "COMMAREA": "CA", "PROGRAM": "PG", "ABCODE": "AB", "APPLID": "AP",
             "SYSID": "SY", "ABSTIME": "AT", "YYYYMMDD": "YD", "DATESEP": "DP",
             "TIME": "TM", "TIMESEP": "TP", "QUEUE": "QU"}
_DROPPED = {"LENGTH", "KEYLENGTH", "LABEL"}
_FLAGS = {"ERASE": "ER", "CURSOR": "CU", "FREEKB": "FK", "UPDATE": "UP", "GTEQ": "GE",
          "EQUAL": "EQ", "NOHANDLE": "NH", "TD": "TD", "ROLLBACK": "RB", "TEXT": "TX"}
_NOOP = {"HANDLE", "IGNORE", "PUSH", "POP"}
_TOKEN = re.compile(r"([A-Z0-9-]+)(?:\s*(\((?:[^()']|'[^']*'|\((?:[^()]|'[^']*')*\))*\)))?")
_DFHRESP = re.compile(r"DFHRESP\s*\(\s*([A-Z0-9]+)\s*\)")


def _code_area(line: str) -> str:
    return line[6:72] if len(line) > 6 else ""


def _is_comment(line: str) -> bool:
    return len(line) > 6 and line[6] in "*/"


def _call_for(body: str) -> str:
    toks = [(m[1], m[2][1:-1].strip() if m[2] else None) for m in _TOKEN.finditer(body)]
    cmd = toks[0][0]
    if cmd in _NOOP:
        return "CONTINUE"
    desc, args, has_into = [cmd], [], False
    map_name = None
    for name, arg in toks[1:]:
        if name in _DROPPED:
            continue
        if arg is not None and name in _ARG_OPTS:
            desc.append(_ARG_OPTS[name])
            args.append(" ".join(arg.split()))
            has_into |= name == "INTO"
            if name == "MAP":
                map_name = arg
        elif arg is None and name in _FLAGS:
            desc.append(_FLAGS[name])
        elif name == "ABEND" and cmd == "HANDLE":
            continue
        else:
            raise ValueError(f"unsupported EXEC CICS option {name} in: {body}")
    if cmd == "RECEIVE" and not has_into and map_name and map_name.startswith("'"):
        desc.append("IN")
        args.append(map_name.strip("'") + "I")
    return "CALL 'CICSSIM' USING '" + " ".join(desc) + "' " + " ".join(args) + " END-CALL"


def _layout_lines(text: str, n_lines: int, indent: int) -> list[str]:
    width = 72 - 7 - indent
    words, out, cur = _split_words(text), [], ""
    for w in words:
        if cur and len(cur) + 1 + len(w) > width:
            out.append(cur)
            cur = w
        else:
            cur = f"{cur} {w}" if cur else w
    out.append(cur)
    if len(out) > n_lines:
        raise ValueError(f"translated EXEC CICS needs {len(out)} lines, has {n_lines}: {text}")
    out += [""] * (n_lines - len(out))
    return [(" " * (7 + indent) + o).rstrip() if o else "" for o in out]


def _split_words(text: str) -> list[str]:
    # keep quoted literals and parenthesised groups together
    return re.findall(r"'[^']*'\S*|\S*\([^()]*\)\S*|\S+", text)


def translate(source: str) -> str:
    lines = source.splitlines()
    out = list(lines)
    i = 0
    while i < len(lines):
        line = lines[i]
        if _is_comment(line):
            i += 1
            continue
        area = _code_area(line)
        if re.search(r"\bPROCEDURE\s+DIVISION\s*\.", area):
            out[i] = line[:6] + re.sub(r"PROCEDURE\s+DIVISION\s*\.",
                                       "PROCEDURE DIVISION USING DFHEIBLK DFHCOMMAREA.", area)
        elif re.search(r"\bLINKAGE\s+SECTION\s*\.", area):
            out[i] = line[:6] + area.rstrip() + " COPY DFHEIBLK."
        m = re.search(r"\bEXEC\s+CICS\b", area)
        if not m:
            out[i] = out[i][:6] + _DFHRESP.sub(lambda r: str(RESP[r[1]]), _code_area(out[i]))
            i += 1
            continue
        prefix = area[:m.start()]
        body, j, rest = area[m.end():], i, ""
        while True:
            e = re.search(r"\bEND-EXEC\b", body)
            if e:
                rest, body = body[e.end():], body[:e.start()]
                break
            j += 1
            nxt = lines[j]
            body += " " + ("" if _is_comment(nxt) else _code_area(nxt))
        segment = (prefix.strip() + " " if prefix.strip() else "") + _call_for(" ".join(body.split()))
        segment += rest.rstrip()
        indent = len(area) - len(area.lstrip()) if not prefix.strip() else len(prefix) - len(prefix.lstrip())
        out[i:j + 1] = _layout_lines(segment, j - i + 1, max(indent - 1, 4))
        i = j + 1
    return "\n".join(out) + "\n"


# ------------------------------------------------------------------ BMS maps
@dataclass
class MapLayout:
    name: str
    size: int
    fields: dict[str, tuple[int, int]]  # name -> (offset, length)

    def build_input(self, **values) -> bytes:
        buf = bytearray(self.size)
        for key, val in values.items():
            name = key.upper().replace("_", "-")
            if not name.endswith("I"):
                name += "I"
            off, ln = self.fields[name]
            data = str(val).encode("latin-1")[:ln]
            buf[off:off + ln] = data.ljust(ln, b" ")
            base = name[:-1]
            if base + "L" in self.fields:
                loff, _ = self.fields[base + "L"]
                buf[loff:loff + 2] = len(str(val).rstrip()).to_bytes(2, "big")
        return bytes(buf)

    def parse_output(self, data: bytes) -> dict[str, str]:
        result = {}
        for name, (off, ln) in self.fields.items():
            if name.endswith("O") or name.endswith("C") or name.endswith("A"):
                result[name] = data[off:off + ln].decode("latin-1").replace("\x00", " ").rstrip()
        return result

    def raw(self, data: bytes, name: str) -> bytes:
        off, ln = self.fields[name]
        return data[off:off + ln]


_PIC = re.compile(r"PIC(?:TURE)?\s+(S?)([X9])(?:\((\d+)\))?", re.I)


def parse_bms_copybook(path: Path) -> dict[str, MapLayout]:
    """Offsets of every named field in the symbolic map(s) of a cpy-bms copybook."""
    maps: dict[str, MapLayout] = {}
    imap, size, off, redef_base, fields = None, 0, 0, None, {}

    def finish():
        if imap:
            maps[imap[:-1]] = MapLayout(imap[:-1], size, fields)

    for raw in path.read_text(errors="replace").splitlines():
        if _is_comment(raw):
            continue
        parts = _code_area(raw).strip().rstrip(".").split()
        if len(parts) < 2:
            continue
        level, name = parts[0], parts[1]
        if level == "01":
            if "REDEFINES" not in parts:
                finish()
                imap, fields = name, {}
            off, redef_base = 0, None
            continue
        if "REDEFINES" in parts:
            redef_base = fields[parts[3]][0]
            continue
        pm = _PIC.search(" ".join(parts))
        length = 2 if "COMP" in parts else (int(pm[3] or 1) if pm else 0)
        if level == "03" and redef_base is not None:
            fields[name] = (redef_base, length)
            continue
        redef_base = None
        if name != "FILLER":
            fields[name] = (off, length)
        off += length
        size = max(size, off)
    finish()
    return maps


# -------------------------------------------------------------- commarea
COMMAREA = Layout("CARDDEMO-COMMAREA (COCOM01Y)", [
    Field("CDEMO-FROM-TRANID", X, 4), Field("CDEMO-FROM-PROGRAM", X, 8),
    Field("CDEMO-TO-TRANID", X, 4), Field("CDEMO-TO-PROGRAM", X, 8),
    Field("CDEMO-USER-ID", X, 8), Field("CDEMO-USER-TYPE", X, 1),
    Field("CDEMO-PGM-CONTEXT", N, 1), Field("CDEMO-CUST-ID", N, 9),
    Field("CDEMO-CUST-FNAME", X, 25), Field("CDEMO-CUST-MNAME", X, 25),
    Field("CDEMO-CUST-LNAME", X, 25), Field("CDEMO-ACCT-ID", N, 11),
    Field("CDEMO-ACCT-STATUS", X, 1), Field("CDEMO-CARD-NUM", N, 16),
    Field("CDEMO-LAST-MAP", X, 7), Field("CDEMO-LAST-MAPSET", X, 7),
], key_len=0)


def commarea(user_type="U", context=0, user_id="USER0001", from_program="COMEN01C",
             from_tranid="CM00", **extra) -> str:
    return COMMAREA.build(**{"CDEMO-FROM-TRANID": from_tranid,
                             "CDEMO-FROM-PROGRAM": from_program,
                             "CDEMO-USER-ID": user_id, "CDEMO-USER-TYPE": user_type,
                             "CDEMO-PGM-CONTEXT": context, **extra})


# -------------------------------------------------------------------- runs
@dataclass
class Screen:
    map: str
    mapset: str
    flags: list[str]
    data: bytes
    fields: dict[str, str]

    def __getitem__(self, name: str) -> str:
        return self.fields[name.upper()]


@dataclass
class CicsResult:
    returncode: int
    stdout: str
    stderr: str
    events: list[list[str]]
    screens: list[Screen] = field(default_factory=list)
    texts: list[str] = field(default_factory=list)
    commarea: bytes | None = None

    def _event(self, cmd: str):
        return next((e for e in self.events if e[0] == cmd), None)

    @property
    def screen(self) -> Screen:
        assert self.screens, f"no SEND MAP; events={self.events}"
        return self.screens[-1]

    @property
    def xctl(self) -> str | None:
        e = self._event("XCTL")
        return e[1] if e else None

    @property
    def return_transid(self) -> str | None:
        e = self._event("RETURN")
        return e[1] if e and len(e) > 1 else None

    @property
    def returned(self) -> bool:
        return self._event("RETURN") is not None

    @property
    def abend(self) -> str | None:
        e = self._event("ABEND")
        return e[1] if e else None

    @property
    def message(self) -> str:
        return self.screen["ERRMSGO"]

    def common(self) -> dict:
        assert self.commarea is not None, "no COMMAREA passed on RETURN/XCTL"
        return COMMAREA.parse(self.commarea[:COMMAREA.rec_len].decode("latin-1"))

    def called(self, cmd: str) -> list[list[str]]:
        return [e for e in self.events if e[0] == cmd]


class Cics:
    """One region: datasets in ``workdir/files``; each ``run`` is one task."""

    def __init__(self, cobol, workdir: Path):
        self.cobol = cobol
        self.dir = workdir
        (self.dir / "files").mkdir(parents=True, exist_ok=True)
        self.layouts: dict[str, tuple[int, int, int]] = {}

    def dataset(self, name: str, layout: Layout, records: list[str],
                key: tuple[int, int] | None = None) -> None:
        off, ln = key if key else (0, layout.key_len)
        recs = sorted(records, key=lambda r: r[off:off + ln])
        for r in recs:
            if len(r) != layout.rec_len:
                raise ValueError(f"{name}: record length {len(r)} != {layout.rec_len}")
        base = self.dir / "files" / name
        base.with_suffix(".dat").write_bytes("".join(recs).encode("latin-1"))
        base.with_suffix(".meta").write_text(f"{layout.rec_len} {off} {ln}\n")

    def records(self, name: str) -> list[str]:
        base = self.dir / "files" / name
        reclen = int(base.with_suffix(".meta").read_text().split()[0])
        data = base.with_suffix(".dat").read_bytes().decode("latin-1")
        return [data[i:i + reclen] for i in range(0, len(data), reclen)]

    def send(self, program: str, map_name: str, aid: str = "ENTER",
             commarea: str | bytes | None = None, faults: list[str] | None = None,
             **fields) -> CicsResult:
        """A pseudo-conversational re-entry: the user typed ``fields`` into ``map_name``."""
        data = self.cobol.bms_maps()[map_name].build_input(**fields)
        return self.run(program, aid, commarea, {map_name: data}, faults)

    def run(self, program: str, aid: str = "ENTER", commarea: str | bytes | None = None,
            maps: dict[str, bytes] | None = None, faults: list[str] | None = None,
            date: str = "2024-03-15", time: str = "10:11:12") -> CicsResult:
        self.cobol.online_program(program)
        task = self.dir / f"task-{program}-{next(self.cobol._seq):05d}"
        (task / "in").mkdir(parents=True)
        (task / "out").mkdir()
        if commarea is not None:
            data = commarea.encode("latin-1") if isinstance(commarea, str) else commarea
            (task / "in" / "commarea").write_bytes(data)
        for name, data in (maps or {}).items():
            (task / "in" / f"map.{name}").write_bytes(data)
        (task / "faults").write_text("".join(f + "\n" for f in faults or []))
        env = {"CICSSIM_FILES": str(self.dir / "files"), "CICSSIM_TASK": str(task),
               "CICSSIM_PROGRAM": program, "CICSSIM_AID": f"{AID[aid]:02X}",
               "CICSSIM_DATE": date, "CICSSIM_TIME": time,
               "COB_CURRENT_DATE": f"{date.replace('-', '/')} {time}.00"}
        res = self.cobol._run(self.cobol.cicsrun(), env, None)
        events = [line.split("\t") for line in
                  (task / "out" / "events").read_text(errors="replace").splitlines()] \
            if (task / "out" / "events").exists() else []
        result = CicsResult(res.returncode, res.stdout, res.stderr, events)
        for e in events:
            if e[0] == "SEND" and e[1] == "MAP":
                data = (task / "out" / e[-1]).read_bytes()
                layout = self.cobol.bms_maps()[e[2]]
                result.screens.append(Screen(e[2], e[3], e[4].split(",") if e[4] else [],
                                             data, layout.parse_output(data)))
            elif e[0] in ("SEND", "WRITEQ") and e[1] in ("TEXT", "TD"):
                result.texts.append((task / "out" / e[-1]).read_bytes().decode("latin-1"))
        if (task / "out" / "commarea").exists():
            result.commarea = (task / "out" / "commarea").read_bytes()
        return result
