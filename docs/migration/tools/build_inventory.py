#!/usr/bin/env python3
"""Build docs/migration/inventory.json and docs/migration/inventory.md from app/.

Everything is derived from the legacy sources (COBOL, copybooks, BMS, CSD, JCL,
PROCs, control cards and the IDCAMS LISTCAT). Every fact carries a
``file:line`` reference so it can be checked against the source.

    python3 docs/migration/tools/build_inventory.py           # (re)generate
    python3 docs/migration/tools/build_inventory.py --check   # verify only
"""
import argparse
import bisect
import json
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
APP = ROOT / "app"
OUT = ROOT / "docs" / "migration"
HLQ = "AWS.M2.CARDDEMO."

EXPECTED = {
    "programs": 28, "bms_mapsets": 17, "copybooks": 28, "jcl_jobs": 29,
    "transactions": 18, "cicsFiles": 8,
}

LE_SERVICES = {
    "CEE3ABD": "IBM Language Environment callable service: abend the enclave",
    "CEEDAYS": "IBM Language Environment callable service: date to Lilian day number",
}

COBOL_VERBS = set("""ACCEPT ADD ALTER CALL CANCEL CLOSE COMPUTE CONTINUE DELETE DISPLAY DIVIDE
ELSE END-EVALUATE END-IF END-PERFORM END-READ END-EXEC EVALUATE EXEC EXIT GO GOBACK IF
INITIALIZE INSPECT MOVE MULTIPLY OPEN PERFORM READ RELEASE RETURN REWRITE SEARCH SET SORT
START STOP STRING SUBTRACT UNSTRING WHEN WRITE NOT AND OR""".split())


def rel(p):
    return str(Path(p).relative_to(ROOT))


def listdir(d):
    return sorted(p for p in d.iterdir() if p.is_file() and not p.name.startswith("."))


def ref(path, line):
    return f"{rel(path)}:{line}"


def strip_lit(v):
    return v.rstrip() if isinstance(v, str) else v


# --------------------------------------------------------------------------- COBOL
class CobolSource:
    """Fixed-format COBOL: indicator in column 7, code in columns 8-72."""

    def __init__(self, path):
        self.path = path
        self.raw = path.read_text(errors="replace").splitlines()
        parts, self._pos, self._ln = [], [], []
        pos = 0
        self.paragraphs = []  # (lineno, name) in PROCEDURE DIVISION
        in_proc = False
        for n, raw in enumerate(self.raw, 1):
            tabbed = "\t" in raw
            raw = raw.expandtabs(8)
            if len(raw) < 7 or raw[6] in "*/":
                continue
            # tab-indented lines (e.g. CUSTREC.cpy) overflow column 72 once expanded
            code = raw[7:].rstrip() if tabbed else raw[7:72]
            if re.search(r"\bPROCEDURE\s+DIVISION\b", code, re.I):
                in_proc = True
            elif in_proc and code[:4].strip() and re.match(r"^([A-Z0-9][A-Z0-9-]*)(\s+SECTION)?\s*\.\s*$", code.strip(), re.I):
                self.paragraphs.append((n, code.strip().rstrip(".").split()[0].upper()))
            self._pos.append(pos)
            self._ln.append(n)
            parts.append(code)
            pos += len(code) + 1
        self.text = "\n".join(parts)
        self.utext = self.text.upper()

    def line_of(self, off):
        return self._ln[bisect.bisect_right(self._pos, off) - 1]

    def paragraph_of(self, line):
        names = [p for p in self.paragraphs if p[0] <= line]
        return names[-1][1] if names else None

    @property
    def line_count(self):
        return len(self.raw)

    def constants(self):
        """Data items with an alphanumeric VALUE literal -> (value, line)."""
        out = {}
        for m in re.finditer(r"\b\d\d\s+([A-Z0-9][A-Z0-9-]*)\s+PIC(?:TURE)?\s+\S+\s+VALUE\s+'([^']*)'", self.utext):
            out.setdefault(m.group(1), (m.group(2).rstrip(), self.line_of(m.start())))
        return out

    def copies(self):
        out = OrderedDict()
        for m in re.finditer(r"(?<![A-Z0-9-])COPY\s+['\"]?([A-Z0-9@#$-]+)", self.utext):
            out.setdefault(m.group(1), self.line_of(m.start()))
        return out

    def exec_cics(self):
        for m in re.finditer(r"EXEC\s+CICS\b(.*?)END-EXEC", self.utext, re.S):
            body = re.sub(r"\s+", " ", m.group(1)).strip()
            yield self.line_of(m.start()), body

    def moves_to(self, var):
        """MOVE <src> TO ... var ... -> list of (src, line)."""
        out = []
        for m in re.finditer(r"(?<![A-Z0-9-])MOVE\s+('[^']*'|[A-Z0-9-]+(?:\([^)]*\))?)\s+TO\s+", self.utext):
            toks = re.findall(r"[A-Z0-9-]+(?:\([^)]*\))?\.?", self.utext[m.end():m.end() + 300])
            targets = []
            for t in toks:
                if t.rstrip(".") in COBOL_VERBS:
                    break
                targets.append(t.rstrip("."))
                if t.endswith("."):
                    break
            if var in targets:
                out.append((m.group(1), self.line_of(m.start())))
        return out


def cics_opt(body, name):
    m = re.search(r"\b" + name + r"\s*\(\s*('[^']*'|[^()]*(?:\([^)]*\))?[^()]*)\s*\)", body)
    return m.group(1).strip() if m else None


# --------------------------------------------------------------------------- copybook layouts
USAGE_RE = re.compile(r"\b(COMP-3|COMPUTATIONAL-3|PACKED-DECIMAL|COMP-5|COMP-4|COMP|COMPUTATIONAL|BINARY)\b")


def pic_size(pic, usage):
    pic = pic.upper().rstrip(".")
    expanded = re.sub(r"(.)\((\d+)\)", lambda m: m.group(1) * int(m.group(2)), pic)
    expanded = expanded.replace("CR", "XX").replace("DB", "XX")
    digits = sum(1 for c in expanded if c in "9Z*P")
    chars = sum(1 for c in expanded if c not in "SVP")
    if usage in ("COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL"):
        return digits // 2 + 1
    if usage in ("COMP", "COMPUTATIONAL", "BINARY", "COMP-4", "COMP-5"):
        return 2 if digits <= 4 else 4 if digits <= 9 else 8
    return chars


def parse_layout(path):
    """Return {01-name: {'length', 'line', 'fields': [(name, level, offset, size, line)]}}."""
    src = CobolSource(path)
    stmts = []
    for m in re.finditer(r"(?<![A-Z0-9-])(\d\d)\s+([A-Z0-9][A-Z0-9-]*|FILLER)\b((?:[^.']|'[^']*'|\.(?=\S))*)\.(?=\s|$)", src.utext):
        level = int(m.group(1))
        if level == 88 or level > 49:
            continue
        rest = m.group(3)
        pic = re.search(r"\bPIC(?:TURE)?\s+(?:IS\s+)?(\S+)", rest)
        occ = re.search(r"\bOCCURS\s+(\d+)", rest)
        usage = USAGE_RE.search(rest)
        stmts.append(dict(level=level, name=m.group(2), pic=pic.group(1) if pic else None,
                          usage=usage.group(1) if usage else None, occurs=int(occ.group(1)) if occ else 1,
                          redefines="REDEFINES" in rest, line=src.line_of(m.start())))

    records = OrderedDict()

    def build(idx, offset, fields, inherited_usage=None):
        node = stmts[idx]
        usage = node["usage"] or inherited_usage
        j = idx + 1
        if node["pic"]:
            size = pic_size(node["pic"], usage)
        else:
            size, child_off, prev_start = 0, offset, offset
            while j < len(stmts) and stmts[j]["level"] > node["level"]:
                if stmts[j]["redefines"]:
                    rsize, j = build(j, prev_start, fields, usage)
                    # a REDEFINES longer than the item it redefines grows the group
                    extra = prev_start + rsize - child_off
                    if extra > 0:
                        size += extra
                        child_off += extra
                else:
                    prev_start = child_off
                    csize, j = build(j, child_off, fields, usage)
                    size += csize
                    child_off += csize
        fields.append((node["name"], node["level"], offset, size * node["occurs"], node["line"]))
        return size * node["occurs"], j

    i = 0
    while i < len(stmts):
        if stmts[i]["level"] == 1:
            name, line, fields = stmts[i]["name"], stmts[i]["line"], []
            size, i = build(i, 0, fields)
            fields.sort(key=lambda f: (f[4], f[1]))
            records[name] = dict(length=size, line=line, fields=fields)
        else:
            i += 1
    return records


# --------------------------------------------------------------------------- CSD
def parse_csd(path):
    text = path.read_text(errors="replace")
    lines = text.splitlines()
    res = {"FILE": OrderedDict(), "PROGRAM": OrderedDict(), "TRANSACTION": OrderedDict(),
           "MAPSET": OrderedDict(), "LIBRARY": OrderedDict(), "TDQUEUE": OrderedDict()}
    cur = None
    for n, line in enumerate(lines, 1):
        m = re.match(r"\s*DEFINE\s+(\w+)\((\w+)\)\s+GROUP\((\w+)\)", line)
        if m:
            cur = {"name": m.group(2), "group": m.group(3), "ref": ref(path, n), "attrs": ""}
            res.setdefault(m.group(1), OrderedDict())[m.group(2)] = cur
            continue
        if cur is not None:
            cur["attrs"] += " " + line.strip()
    for kind in res.values():
        for d in kind.values():
            a = d.pop("attrs")
            for key in ("DSNAME", "PROGRAM", "DESCRIPTION", "TRANSID", "DSNAME01", "RECORDSIZE"):
                m = re.search(r"\b" + key + r"\(([^)]*)\)", a)
                if m:
                    d[key.lower()] = m.group(1).strip()
            ops = [op for op in ("READ", "BROWSE", "ADD", "UPDATE", "DELETE") if re.search(r"\b" + op + r"\(YES\)", a)]
            if ops and "dsname" in d:
                d["ops"] = ops
    return res


# --------------------------------------------------------------------------- JCL
def split_operands(s):
    parts, depth, q, cur = [], 0, False, ""
    for c in s:
        if c == "'":
            q = not q
        if not q:
            if c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
            elif c == "," and depth == 0:
                parts.append(cur)
                cur = ""
                continue
        cur += c
    if cur:
        parts.append(cur)
    out = OrderedDict()
    for i, p in enumerate(parts):
        if "=" in p and not p.startswith("'"):
            k, v = p.split("=", 1)
            out[k.upper()] = v
        else:
            out[f"_{i}"] = p
    return out


def first_token(s):
    out, q = "", False
    for c in s:
        if c == "'":
            q = not q
        if c == " " and not q:
            break
        out += c
    return out


def parse_jcl_statements(path):
    lines = path.read_text(errors="replace").splitlines()
    stmts, i, instream = [], 0, None
    while i < len(lines):
        raw = lines[i].rstrip()[:72]
        n = i + 1
        if instream is not None:
            if raw.startswith("/*") or (raw.startswith("//") and not raw.startswith("//*")):
                stmts[-1]["instream"] = instream
                instream = None
                if raw.startswith("/*"):
                    i += 1
                    continue
            else:
                instream.append((n, raw))
                i += 1
                continue
        if raw.startswith("//*") or not raw.startswith("//") or not raw[2:].strip():
            i += 1
            continue
        body = raw[2:]
        name = "" if body.startswith(" ") else body.split()[0]
        rest = body[len(name):].strip()
        op = rest.split()[0]
        operands = first_token(rest[len(op):].strip())
        while operands.endswith(",") and i + 1 < len(lines) and lines[i + 1].startswith("//") \
                and not lines[i + 1].startswith("//*") and lines[i + 1][2:3] == " ":
            i += 1
            operands += first_token(lines[i].rstrip()[:72][2:].strip())
        st = {"name": name, "op": op.upper(), "operands": operands, "line": n, "ops": split_operands(operands)}
        stmts.append(st)
        if st["op"] == "DD" and (operands in ("*", "DATA") or operands.startswith("*,") or operands.startswith("DATA,")):
            instream = []
        i += 1
    if instream is not None and stmts:
        stmts[-1]["instream"] = instream
    return stmts


def disp_role(disp):
    if not disp:
        return "read"
    d = disp.strip("()").split(",")
    status = d[0].upper() or "NEW"
    if status == "NEW":
        return "create"
    if status == "MOD":
        return "delete" if len(d) > 1 and d[1].upper() == "DELETE" else "append"
    if status == "OLD":
        return "update"
    return "read"


def parse_idcams(text, base_line):
    """Parse IDCAMS command stream -> list of commands with dataset names."""
    joined = []
    for n, l in text:
        l = re.sub(r"/\*.*?\*/", "", l)
        joined.append(l.rstrip())
    s = " ".join(x[:-1] if x.endswith("-") else x for x in joined)
    s = re.sub(r"\s+", " ", s).upper()
    cmds = []
    for m in re.finditer(r"\b(DEFINE|DELETE|REPRO|BLDINDEX|PRINT|LISTCAT)\b(.*?)(?=\b(?:DEFINE|DELETE|REPRO|BLDINDEX|PRINT|LISTCAT|IF|SET)\b|$)", s):
        verb, args = m.group(1), m.group(2)
        c = {"verb": verb, "line": base_line}

        def opt(k, a=args):
            mm = re.search(r"\b" + k + r"\s*\(([^()]*)\)", a)
            return mm.group(1).strip() if mm else None
        if verb == "DEFINE":
            kind = re.match(r"\s*(CLUSTER|ALTERNATEINDEX|AIX|PATH|GENERATIONDATAGROUP|GDG)", args)
            c["kind"] = {"AIX": "ALTERNATEINDEX", "GDG": "GENERATIONDATAGROUP"}.get(kind.group(1), kind.group(1)) if kind else "?"
            c["name"] = opt("NAME")
            for k in ("KEYS", "RECORDSIZE", "RELATE", "PATHENTRY", "LIMIT"):
                v = opt(k)
                if v:
                    c[k.lower()] = [int(x) for x in re.split(r"[ ,]+", v)] if k in ("KEYS", "RECORDSIZE") else v
            if c["kind"] == "ALTERNATEINDEX":
                c["unique"] = "NONUNIQUEKEY" not in args
                c["upgrade"] = "NOUPGRADE" not in args and "UPGRADE" in args
        elif verb == "DELETE":
            c["name"] = args.strip().split()[0] if args.strip() else None
        else:
            for k in ("INFILE", "OUTFILE", "INDATASET", "OUTDATASET", "IDS", "ODS"):
                v = opt(k)
                if v:
                    c[k.lower()] = v
        cmds.append(c)
    return cmds


def parse_jcl(path, procs):
    stmts = parse_jcl_statements(path)
    job = {"name": None, "file": rel(path), "ref": None, "steps": [], "jcllib": None}
    step = None
    for st in stmts:
        if st["op"] == "JOB":
            job["name"], job["ref"] = st["name"], ref(path, st["line"])
            job["description"] = st["ops"].get("_0", "").strip("'")
        elif st["op"] == "JCLLIB":
            job["jcllib"] = st["ops"].get("ORDER")
        elif st["op"] == "PROC":
            job.setdefault("proc", st["name"])
        elif st["op"] == "EXEC":
            o = st["ops"]
            step = {"name": st["name"], "ref": ref(path, st["line"]), "dds": [], "params": {}}
            if "PGM" in o:
                step["program"] = o["PGM"]
            else:
                step["proc"] = o.get("PROC", o.get("_0"))
            for k, v in o.items():
                if k not in ("PGM", "PROC") and not k.startswith("_"):
                    step["params"][k] = v
            job["steps"].append(step)
        elif st["op"] == "DD" and step is not None:
            o = st["ops"]
            dsn = o.get("DSN", o.get("DSNAME"))
            if not st["name"] and step["dds"]:
                prev = step["dds"][-1]
                prev["dsn"] = prev["dsn"] + [dsn] if dsn else prev["dsn"]
                continue
            dd = {"dd": st["name"], "ref": ref(path, st["line"]), "dsn": [dsn] if dsn else [],
                  "disp": o.get("DISP"), "sysout": "SYSOUT" in o, "dummy": "DUMMY" in st["operands"].split(",")[0]}
            if "instream" in st:
                dd["instream"] = [l for _, l in st["instream"]]
                dd["instream_line"] = st["instream"][0][0] if st["instream"] else st["line"]
            step["dds"].append(dd)
    # expand procedures
    expanded = []
    for s in job["steps"]:
        if "proc" in s and s["proc"] in procs:
            for ps in procs[s["proc"]]["steps"]:
                e = json.loads(json.dumps(ps))
                e["name"] = f"{s['name']}.{ps['name']}"
                e["via_proc"] = s["proc"]
                e["ref"] = s["ref"]
                e["proc_ref"] = ps["ref"]
                for dd in e["dds"]:
                    dd["dsn"] = [re.sub(r"&(\w+)\.?", lambda m: s["params"].get(m.group(1), m.group(0)), d) for d in dd["dsn"]]
                    for ov in s["dds"]:
                        if ov["dd"] == f"{ps['name']}.{dd['dd']}":
                            dd.update({k: v for k, v in ov.items() if k != "dd"})
                            dd["override_ref"] = ov["ref"]
                expanded.append(e)
        else:
            expanded.append(s)
    job["steps"] = expanded
    return job


def load_ctl_member(dsn):
    m = re.match(r".*\((\w+)\)$", dsn or "")
    if m:
        p = APP / "ctl" / f"{m.group(1)}.ctl"
        if p.exists():
            return p
    return None


def analyse_step(step):
    """Attach control-card derived facts (IDCAMS, SORT, SDSF CEMT, DFHCSDUP, IEBGENER)."""
    pgm = step.get("program", "")
    sysin = next((d for d in step["dds"] if d["dd"] in ("SYSIN", "ISFIN")), None)
    cards, card_ref = [], None
    if sysin and "instream" in sysin:
        cards = [(sysin["instream_line"] + i, l) for i, l in enumerate(sysin["instream"])]
        card_ref = sysin["ref"]
    elif sysin and sysin["dsn"]:
        p = load_ctl_member(sysin["dsn"][0])
        if p:
            cards = [(i, l) for i, l in enumerate(p.read_text().splitlines(), 1)]
            card_ref = ref(p, 1)
    if pgm == "IDCAMS":
        step["idcams"] = parse_idcams(cards, card_ref)
    elif pgm == "SORT":
        step["sort_control"] = [l.strip() for _, l in cards if l.strip()]
    elif pgm == "SDSF":
        step["cemt"] = [l.strip() for _, l in cards if "CEMT" in l]
    elif pgm == "DFHCSDUP":
        step["csdup"] = [re.sub(r"\s+", " ", l.strip()) for _, l in cards if l.strip().startswith("DEFINE")]


# --------------------------------------------------------------------------- LISTCAT
def parse_listcat(path):
    text = path.read_text(errors="replace")
    entries = OrderedDict()
    blocks = re.split(r"\n0(?=(?:NONVSAM|CLUSTER|DATA|INDEX|AIX|PATH|GDG BASE)\s*-)", text)
    for b in blocks:
        m = re.match(r"(NONVSAM|CLUSTER|DATA|INDEX|AIX|PATH|GDG BASE)\s*-+\s*(\S+)", b)
        if not m:
            continue
        kind, name = m.groups()
        line = text[:text.find(b)].count("\n") + 1
        if kind == "DATA":
            owner = re.search(r"(?:CLUSTER|AIX)-+(\S+)", b)
            attrs = {k: int(v) for k, v in re.findall(r"\b(KEYLEN|RKP|AVGLRECL|MAXLRECL|AXRKP)-+(\d+)", b)}
            if owner:
                entries.setdefault(owner.group(1), {}).update({"data_ref": ref(path, line), **attrs})
        else:
            e = entries.setdefault(name, {})
            e.update({"kind": kind, "ref": ref(path, line)})
            if kind == "PATH":
                pe = re.search(r"AIX-+(\S+)", b)
                if pe:
                    e["pathentry"] = pe.group(1)
            if kind == "AIX":
                c = re.search(r"CLUSTER-+(\S+)", b)
                if c:
                    e["relate"] = c.group(1)
    return entries


# --------------------------------------------------------------------------- main build
def build():
    findings = []
    cbl_files = listdir(APP / "cbl")
    cpy_files = {p.stem.upper(): p for p in listdir(APP / "cpy")}
    bms_cpy = {p.stem.upper(): p for p in listdir(APP / "cpy-bms")}
    bms_files = listdir(APP / "bms")
    jcl_files = listdir(APP / "jcl")
    csd_path = APP / "csd" / "CARDDEMO.CSD"
    csd = parse_csd(csd_path)

    # copybook layouts: 01-name -> copybook
    layouts, rec_owner = {}, {}
    for name, p in cpy_files.items():
        try:
            layouts[name] = parse_layout(p)
        except Exception as exc:  # pragma: no cover - reported, not fatal
            layouts[name] = {}
            findings.append(f"Could not compute layout of {rel(p)}: {exc}")
        for rec in layouts[name]:
            rec_owner.setdefault(rec, name)

    # menu option tables
    def menu_table(cpy, prefix):
        src = CobolSource(cpy_files[cpy])
        fields = re.findall(r"\b15\s+" + prefix + r"-([A-Z]+)", src.utext)
        data = src.utext[src.utext.find("OPTIONS-DATA"):src.utext.find("REDEFINES")]
        vals = [(m.group(1) or m.group(2), src.line_of(src.utext.find("OPTIONS-DATA") + m.start()))
                for m in re.finditer(r"VALUE\s+(?:'([^']*)'|(\d+))", data)]
        rows = []
        for i in range(0, len(vals), len(fields)):
            row = {f.lower(): strip_lit(v[0]) for f, v in zip(fields, vals[i:i + len(fields)])}
            row["ref"] = ref(cpy_files[cpy], vals[i][1])
            rows.append(row)
        return rows
    main_menu = menu_table("COMEN02Y", "CDEMO-MENU-OPT")
    admin_menu = menu_table("COADM02Y", "CDEMO-ADMIN-OPT")

    # bms
    mapsets = OrderedDict()
    for p in bms_files:
        t = p.read_text(errors="replace").splitlines()
        ms, maps = None, []
        for n, l in enumerate(t, 1):
            if l.startswith("*"):
                continue
            m = re.match(r"(\w+)\s+DFHMSD\s+(?!TYPE=FINAL)", l)
            if m:
                ms = (m.group(1), n)
            m = re.match(r"(\w+)\s+DFHMDI\b", l)
            if m:
                maps.append({"map": m.group(1), "ref": ref(p, n)})
        fields = len([l for l in t if re.match(r"^\w+\s+DFHMDF\b", l)])
        mapsets[ms[0]] = {"mapset": ms[0], "file": rel(p), "ref": ref(p, ms[1]), "maps": maps,
                          "named_fields": fields, "symbolic_copybook": rel(bms_cpy[ms[0]]) if ms[0] in bms_cpy else None,
                          "programs": [], "csd": csd["MAPSET"].get(ms[0], {}).get("ref")}

    # ------------------------------------------------------------------ programs
    programs = OrderedDict()
    sources = {}
    for p in cbl_files:
        src = CobolSource(p)
        pid = re.search(r"PROGRAM-ID\.\s*['\"]?([A-Z0-9-]+)", src.utext).group(1)
        sources[pid] = src
        consts = src.constants()
        is_cics = "EXEC CICS" in src.utext
        copies = []
        for c, ln in src.copies().items():
            kind = ("copybook" if c in cpy_files else "bms-symbolic-map" if c in bms_cpy else
                    "cics-system" if c.startswith("DFH") else "unresolved")
            copies.append({"name": c, "kind": kind, "ref": ref(p, ln)})
            if kind == "unresolved":
                findings.append(f"{pid}: COPY {c} ({ref(p, ln)}) has no member in app/cpy or app/cpy-bms")
        prog = {"program": pid, "file": rel(p), "lines": src.line_count,
                "type": "CICS" if is_cics else None, "copybooks": copies,
                "cics_files": OrderedDict(), "batch_files": OrderedDict(),
                "xctl": [], "calls": [], "mapsets": OrderedDict(), "transaction": None,
                "return_transid": [], "tdq": [], "procedure_using": bool(re.search(r"PROCEDURE\s+DIVISION\s+USING", src.utext))}

        def resolve(expr, line):
            expr = expr.strip()
            if expr.startswith("'"):
                return [(expr.strip("'").rstrip(), "literal", ref(p, line))]
            name = re.sub(r"\(.*\)$", "", expr)
            if name in consts:
                return [(consts[name][0], f"constant {name}", ref(p, consts[name][1]))]
            return None

        def resolve_var(var, line):
            """Nearest preceding MOVE to var in the same paragraph, else every MOVE in the program."""
            moves = src.moves_to(var)
            para = src.paragraph_of(line)
            local = [mv for mv in moves if mv[1] < line and src.paragraph_of(mv[1]) == para]
            chosen = [max(local, key=lambda mv: mv[1])] if local else moves
            out = []
            for s, ln in chosen:
                r = resolve(s, ln)
                if r:
                    out += [(v, f"MOVE {s} TO {var} ({ref(p, ln)}) -> {how}", where) for v, how, where in r]
                else:
                    out.append((None, f"MOVE {s} TO {var}", ref(p, ln)))
            return out

        for line, body in src.exec_cics():
            verb = body.split()[0]
            if verb in ("READ", "WRITE", "REWRITE", "DELETE", "STARTBR", "READNEXT", "READPREV", "ENDBR", "UNLOCK"):
                fexpr = cics_opt(body, "DATASET") or cics_opt(body, "FILE")
                r = resolve(fexpr, line)
                fname = r[0][0] if r else None
                if not fname:
                    findings.append(f"{pid}: unresolved CICS file expression {fexpr} at {ref(p, line)}")
                    continue
                op = verb + (" UPDATE" if verb == "READ" and re.search(r"\bUPDATE\b", body) else "")
                rec = cics_opt(body, "INTO") or cics_opt(body, "FROM")
                f = prog["cics_files"].setdefault(fname, {"file": fname, "ops": [], "refs": [], "records": []})
                if op not in f["ops"]:
                    f["ops"].append(op)
                f["refs"].append(ref(p, line))
                if rec and rec not in f["records"]:
                    f["records"].append(rec)
            elif verb == "XCTL":
                pexpr = cics_opt(body, "PROGRAM")
                site = {"ref": ref(p, line), "expression": pexpr, "targets": []}
                r = resolve(pexpr, line)
                if r:
                    site["targets"] = [{"program": v, "via": how, "ref": where} for v, how, where in r]
                elif pexpr.startswith("CDEMO-MENU-OPT-PGMNAME"):
                    site["targets"] = [{"program": o["pgmname"], "via": f"COMEN02Y option {o['num']} '{o['name']}'", "ref": o["ref"]} for o in main_menu]
                elif pexpr.startswith("CDEMO-ADMIN-OPT-PGMNAME"):
                    site["targets"] = [{"program": o["pgmname"], "via": f"COADM02Y option {o['num']} '{o['name']}'", "ref": o["ref"]} for o in admin_menu]
                else:
                    var = re.sub(r"\(.*\)$", "", pexpr)
                    if var == "CDEMO-TO-PROGRAM":
                        res = []
                        for s, ln in src.moves_to(var):
                            rr = resolve(s, ln)
                            if rr:
                                res += [(v, f"MOVE {s} TO {var}", ref(p, ln)) for v, _, _ in rr]
                            elif s == "CDEMO-FROM-PROGRAM":
                                res.append(("<caller>", f"MOVE CDEMO-FROM-PROGRAM TO {var}", ref(p, ln)))
                            else:
                                res.append((None, f"MOVE {s} TO {var}", ref(p, ln)))
                    else:
                        res = resolve_var(var, line)
                    site["targets"] = [{"program": v, "via": how, "ref": where} for v, how, where in res]
                prog["xctl"].append(site)
            elif verb == "LINK":
                findings.append(f"{pid}: EXEC CICS LINK at {ref(p, line)}")
            elif verb in ("SEND", "RECEIVE") and re.search(r"\bMAP\s*\(", body):
                mexpr, msexpr = cics_opt(body, "MAP"), cics_opt(body, "MAPSET")
                mres = resolve(mexpr, line) or resolve_var(mexpr, line)
                msres = resolve(msexpr, line) or resolve_var(msexpr, line)
                for ms in {v for v, _, _ in msres if v}:
                    e = prog["mapsets"].setdefault(ms, {"mapset": ms, "maps": [], "refs": []})
                    for mp in {v for v, _, _ in mres if v}:
                        if mp not in e["maps"]:
                            e["maps"].append(mp)
                    e["refs"].append(f"{verb} {ref(p, line)}")
            elif verb == "RETURN" and "TRANSID" in body:
                r = resolve(cics_opt(body, "TRANSID"), line)
                prog["return_transid"].append({"transid": r[0][0] if r else None, "ref": ref(p, line)})
            elif verb == "WRITEQ":
                q = resolve(cics_opt(body, "QUEUE"), line)
                prog["tdq"].append({"queue": q[0][0] if q else None, "ref": ref(p, line), "type": body.split()[1]})

        # CALL
        for m in re.finditer(r"(?<![A-Z0-9-])CALL\s+(['\"])([A-Z0-9-]+)\1", src.utext):
            tgt = m.group(2)
            ln = src.line_of(m.start())
            if not any(c["program"] == tgt for c in prog["calls"]):
                prog["calls"].append({"program": tgt, "refs": [], "kind": None})
            next(c for c in prog["calls"] if c["program"] == tgt)["refs"].append(ref(p, ln))
        for m in re.finditer(r"(?<![A-Z0-9-])CALL\s+([A-Z][A-Z0-9-]*)\b", src.utext):
            findings.append(f"{pid}: dynamic CALL {m.group(1)} at {ref(p, src.line_of(m.start()))}")

        # batch file control
        fds = {}
        for m in re.finditer(r"(?<![A-Z0-9-])FD\s+([A-Z0-9-]+)[^.]*\.\s*(?:COPY\s+\S+\.\s*)?01\s+([A-Z0-9-]+)", src.utext):
            fds[m.group(2)] = m.group(1)
        for m in re.finditer(r"(?<![A-Z0-9-])SELECT\s+([A-Z0-9-]+)\s+ASSIGN\s+TO\s+([A-Z0-9-]+)(.*?)\.", src.utext, re.S):
            fname, dd, clauses = m.group(1), m.group(2), re.sub(r"\s+", " ", m.group(3))
            org = re.search(r"ORGANIZATION\s+(?:IS\s+)?(\w+)", clauses)
            acc = re.search(r"ACCESS\s+(?:MODE\s+)?(?:IS\s+)?(\w+)", clauses)
            key = re.search(r"(?<!ALTERNATE )RECORD\s+KEY\s+(?:IS\s+)?([A-Z0-9-]+)", clauses)
            alt = re.search(r"ALTERNATE\s+RECORD\s+KEY\s+(?:IS\s+)?([A-Z0-9-]+)", clauses)
            prog["batch_files"][fname] = {"select": fname, "dd": dd, "ref": ref(p, src.line_of(m.start())),
                                          "organization": org.group(1) if org else "SEQUENTIAL",
                                          "access": acc.group(1) if acc else "SEQUENTIAL",
                                          "record_key": key.group(1) if key else None,
                                          "alternate_key": alt.group(1) if alt else None,
                                          "open": [], "verbs": [], "records": []}
        for m in re.finditer(r"(?<![A-Z0-9-])OPEN\s+((?:(?:INPUT|OUTPUT|I-O|EXTEND)\s+(?:[A-Z0-9-]+\s*)+)+)", src.utext):
            mode = None
            for tok in m.group(1).split():
                if tok in ("INPUT", "OUTPUT", "I-O", "EXTEND"):
                    mode = tok
                elif tok in prog["batch_files"]:
                    bf = prog["batch_files"][tok]
                    entry = f"{mode} ({ref(p, src.line_of(m.start()))})"
                    if entry not in bf["open"]:
                        bf["open"].append(entry)
                else:
                    break
        for m in re.finditer(r"(?<![A-Z0-9-])(READ|WRITE|REWRITE|DELETE|START)\s+([A-Z0-9-]+)(?:\s+NEXT)?(?:\s+RECORD)?(?:\s+(?:INTO|FROM)\s+([A-Z0-9-]+))?", src.utext):
            verb, target, rec = m.groups()
            fname = target if target in prog["batch_files"] else fds.get(target)
            if fname in prog["batch_files"]:
                bf = prog["batch_files"][fname]
                if verb not in bf["verbs"]:
                    bf["verbs"].append(verb)
                if rec and rec not in bf["records"]:
                    bf["records"].append(rec)
        programs[pid] = prog

    def owner(pid, rec):
        """Copybook defining 01 <rec>, preferring the copybooks the program actually COPYs."""
        mine = [c["name"] for c in programs[pid]["copybooks"] if c["kind"] == "copybook" and rec in layouts.get(c["name"], {})]
        return mine[0] if mine else f"inline:{rec}"

    called = {c["program"] for pr in programs.values() for c in pr["calls"]}
    # program types
    for pid, prog in programs.items():
        if prog["type"] == "CICS":
            continue
        if prog["batch_files"]:
            prog["type"] = "batch"
            prog["role"] = "called subprogram" if pid in called else "main program"
        else:
            prog["type"] = "utility"
            prog["role"] = "called subprogram" if pid in called else "main program"
    for pid, prog in programs.items():
        for c in prog["calls"]:
            c["kind"] = ("in-repo program" if c["program"] in programs else
                         "external" if c["program"] in LE_SERVICES else "unresolved")
            if c["kind"] == "external":
                c["note"] = LE_SERVICES[c["program"]]

    # caller resolution for CDEMO-FROM-PROGRAM returns (fixed point)
    def static_callers(target):
        out = set()
        for q, pr in programs.items():
            for s in pr["xctl"]:
                if any(t["program"] == target for t in s["targets"]):
                    out.add(q)
        return out
    changed = True
    while changed:
        changed = False
        for pid, prog in programs.items():
            for s in prog["xctl"]:
                for t in list(s["targets"]):
                    if t["program"] == "<caller>":
                        callers = sorted(static_callers(pid) - {pid})
                        known = {x["program"] for x in s["targets"]}
                        for c in callers:
                            if c not in known:
                                s["targets"].append({"program": c, "via": f"CDEMO-FROM-PROGRAM = calling program ({c} XCTLs to {pid})", "ref": t["ref"]})
                                changed = True
    for prog in programs.values():
        for s in prog["xctl"]:
            s["targets"] = [t for t in s["targets"] if t["program"] != "<caller>"]
            seen = OrderedDict()
            for t in s["targets"]:
                seen.setdefault(t["program"], t)
            s["targets"] = list(seen.values())

    # transactions
    transactions = OrderedDict()
    for tid, t in csd["TRANSACTION"].items():
        pg = t.get("program")
        transactions[tid] = {"transid": tid, "program": pg, "ref": t["ref"], "description": t.get("description"),
                             "program_source": programs[pg]["file"] if pg in programs else None}
        if pg in programs:
            programs[pg]["transaction"] = tid
    for pid, prog in programs.items():
        for rt in prog["return_transid"]:
            if prog["transaction"] and rt["transid"] != prog["transaction"]:
                findings.append(f"{pid}: RETURN TRANSID({rt['transid']}) at {rt['ref']} differs from CSD transaction {prog['transaction']}")
        for ms in prog["mapsets"]:
            if ms in mapsets:
                mapsets[ms]["programs"].append(pid)

    # ------------------------------------------------------------------ JCL
    procs = {}
    for p in listdir(APP / "proc"):
        j = parse_jcl(p, {})
        procs[p.stem.upper()] = {"file": rel(p), "steps": j["steps"], "name": j.get("proc")}
    jobs = OrderedDict()
    for p in jcl_files:
        job = parse_jcl(p, procs)
        for s in job["steps"]:
            analyse_step(s)
        jobs[p.stem.upper()] = job

    # dataset catalogue from IDCAMS DEFINE
    vsam = OrderedDict()
    aix = OrderedDict()
    paths = OrderedDict()
    gdgs = OrderedDict()
    for jn, job in jobs.items():
        for s in job["steps"]:
            for c in s.get("idcams", []):
                if c["verb"] != "DEFINE" or not c.get("name"):
                    continue
                site = f"{job['file']} step {s['name']}"
                if c["kind"] == "CLUSTER":
                    v = vsam.setdefault(c["name"], {"dataset": c["name"], "defined_in": []})
                    v["defined_in"].append(site)
                    v["key_length"], v["key_offset"] = c["keys"]
                    v["record_size"] = c["recordsize"]
                elif c["kind"] == "ALTERNATEINDEX":
                    a = aix.setdefault(c["name"], {"aix": c["name"], "defined_in": []})
                    a["defined_in"].append(site)
                    a.update(relate=c["relate"], key_length=c["keys"][0], key_offset=c["keys"][1],
                             unique=c["unique"], upgrade=c["upgrade"])
                elif c["kind"] == "PATH":
                    paths[c["name"]] = {"path": c["name"], "pathentry": c["pathentry"], "defined_in": site}
                elif c["kind"] == "GENERATIONDATAGROUP":
                    g = gdgs.setdefault(c["name"], {"gdg": c["name"], "definitions": []})
                    g["definitions"].append({"limit": int(c["limit"]) if c.get("limit") else None, "defined_in": site})

    listcat = parse_listcat(APP / "catlg" / "LISTCAT.txt")
    path_to_base = {}
    for pth, pe in paths.items():
        if pe["pathentry"] in aix:
            path_to_base[pth] = aix[pe["pathentry"]]["relate"]

    # CICS files
    cics_files = OrderedDict()
    for fname, f in csd["FILE"].items():
        dsn = f["dsname"]
        base = path_to_base.get(dsn, dsn)
        entry = {"file": fname, "dsname": dsn, "base_cluster": base, "is_aix_path": dsn in path_to_base,
                 "csd_ref": f["ref"], "csd_ops": f.get("ops", []), "programs": OrderedDict()}
        if dsn in path_to_base:
            entry["aix"] = paths[dsn]["pathentry"]
        cics_files[fname] = entry
    for pid, prog in programs.items():
        for fname, f in prog["cics_files"].items():
            if fname not in cics_files:
                findings.append(f"{pid}: CICS file {fname} not defined in CARDDEMO.CSD")
                continue
            cics_files[fname]["programs"][pid] = f["ops"]
            f["dsname"] = cics_files[fname]["dsname"]
            f["copybooks"] = sorted({owner(pid, r) for r in f["records"]})

    # batch program DD -> dataset via JCL
    program_runs = defaultdict(list)
    for jn, job in jobs.items():
        for s in job["steps"]:
            pgm = s.get("program")
            if pgm in programs:
                program_runs[pgm].append({"job": job["name"], "file": job["file"], "step": s["name"], "ref": s["ref"]})
    # CBSTM03B is CALLed: it inherits the DDs of its caller's job step
    for pid, prog in programs.items():
        for c in prog["calls"]:
            if c["kind"] == "in-repo program":
                for run in program_runs.get(pid, []):
                    program_runs[c["program"]].append({**run, "via_call_from": pid})
    for pid, prog in programs.items():
        prog["jcl_runs"] = program_runs.get(pid, [])
        for bf in prog["batch_files"].values():
            bf["datasets"] = []
            for run in prog["jcl_runs"]:
                step = next(s for s in jobs[Path(run["file"]).stem.upper()]["steps"] if s["name"] == run["step"])
                dd = next((d for d in step["dds"] if d["dd"] == bf["dd"]), None)
                if dd is None:
                    findings.append(f"{pid}: DD {bf['dd']} (SELECT {bf['select']}, {bf['ref']}) missing from {run['file']} step {run['step']}")
                    continue
                for dsn in dd["dsn"]:
                    if {"job": run["job"], "dsn": dsn} not in [{"job": x["job"], "dsn": x["dsn"]} for x in bf["datasets"]]:
                        bf["datasets"].append({"job": run["job"], "dsn": dsn, "disp": dd["disp"], "ref": dd["ref"]})
            bf["copybooks"] = sorted({owner(pid, r) for r in bf["records"]})
            modes = {o.split()[0] for o in bf["open"]}
            bf["access"] = "RW" if ("I-O" in modes or ("INPUT" in modes and "OUTPUT" in modes)) else "W" if modes & {"OUTPUT", "EXTEND"} else "R"

    # dataset roles per job step
    def gen_base(dsn):
        return re.sub(r"\([+-]?\d+\)$", "", dsn)
    for jn, job in jobs.items():
        ds = OrderedDict()

        def add(dsn, role, where):
            if not dsn or dsn.startswith("&") or dsn == "NULLFILE":
                return
            e = ds.setdefault(dsn, {"dataset": dsn, "roles": []})
            if (role, where) not in [(r["role"], r["step"]) for r in e["roles"]]:
                e["roles"].append({"role": role, "step": where})
        for s in job["steps"]:
            pgm = s.get("program")
            ddmap = {d["dd"]: d for d in s["dds"]}
            for d in s["dds"]:
                if d["dd"] in ("STEPLIB", "SYSIN", "SYSPRINT", "SYSOUT", "SYMNAMES", "ISFIN", "DFHCSD"):
                    if d["dd"] == "SYSIN" and d["dsn"]:
                        for x in d["dsn"]:
                            add(re.sub(r"&\w+\.?", "", x) if x.startswith("&") else x, "control cards", s["name"])
                    continue
                role = disp_role(d["disp"])
                if pgm in programs:
                    bf = next((b for b in programs[pgm]["batch_files"].values() if b["dd"] == d["dd"]), None)
                    if bf is None:
                        # DD for a CALLed subprogram (CBSTM03B) or AIX path companion
                        for c in programs[pgm]["calls"]:
                            if c["kind"] == "in-repo program":
                                bf = next((b for b in programs[c["program"]]["batch_files"].values() if b["dd"] == d["dd"]), None) or bf
                    if bf is not None:
                        role = {"R": "read", "W": "write", "RW": "read/update"}[bf["access"]] if role != "create" else "create/write"
                    elif d["dd"].endswith("1") and any(b["dd"].startswith(d["dd"][:-1]) and b["alternate_key"] for b in programs[pgm]["batch_files"].values()):
                        role = "read (alternate-index path)"
                elif pgm == "SORT":
                    role = "read" if d["dd"].startswith("SORTIN") else ("create/write" if role == "create" else "write") if d["dd"] == "SORTOUT" else role
                elif pgm == "IEBGENER":
                    role = "read" if d["dd"] == "SYSUT1" else "create/write" if d["dd"] == "SYSUT2" else role
                elif pgm == "IDCAMS":
                    infiles = {c.get("infile") for c in s.get("idcams", [])}
                    outfiles = {c.get("outfile") for c in s.get("idcams", [])}
                    role = "read" if d["dd"] in infiles else "write" if d["dd"] in outfiles else role
                for x in d["dsn"]:
                    add(x, role, s["name"])
            for c in s.get("idcams", []):
                if c["verb"] == "DEFINE":
                    add(c.get("name"), f"define {c['kind'].lower()}", s["name"])
                elif c["verb"] == "DELETE":
                    add(c.get("name"), "delete", s["name"])
                elif c["verb"] == "BLDINDEX":
                    add(c.get("indataset"), "read", s["name"])
                    add(c.get("outdataset"), "build index", s["name"])
                elif c["verb"] == "REPRO":
                    add(c.get("indataset"), "read", s["name"])
                    add(c.get("outdataset"), "write", s["name"])
        job["datasets"] = list(ds.values())
        job["programs"] = sorted({s.get("program") or f"PROC {s.get('proc')}" for s in job["steps"]})

    # VSAM record layout cross-check
    observed_cpy = defaultdict(set)
    for pid, prog in programs.items():
        for f in prog["cics_files"].values():
            if f["file"] in cics_files:
                for c in f.get("copybooks", []):
                    observed_cpy[cics_files[f["file"]]["base_cluster"]].add(c)
        for bf in prog["batch_files"].values():
            for d in bf["datasets"]:
                base = path_to_base.get(d["dsn"], d["dsn"])
                for c in bf["copybooks"]:
                    observed_cpy[base].add(c)
    for name, v in vsam.items():
        lc = listcat.get(name, {})
        v["listcat"] = {k: lc[k] for k in ("KEYLEN", "RKP", "MAXLRECL", "ref") if k in lc} or None
        cands = []
        for c in sorted(observed_cpy.get(name, [])):
            if c.startswith("inline:"):
                cands.append({"copybook": None, "record": c[7:], "length": None})
                continue
            for rec, lay in layouts.get(c, {}).items():
                if rec in {r for pr in programs.values() for f in list(pr["cics_files"].values()) + list(pr["batch_files"].values()) for r in f["records"]}:
                    keyf = [f for f in lay["fields"] if f[2] == v["key_offset"] and f[3] == v["key_length"]]
                    cands.append({"copybook": c, "file": rel(cpy_files[c]), "record": rec, "length": lay["length"],
                                  "matches_recordsize": lay["length"] == v["record_size"][1],
                                  "key_field": keyf[0][0] if keyf else None})
        v["copybooks"] = cands
        v["key_field"] = next((c["key_field"] for c in cands if c.get("key_field")), None)
        if not v["key_field"]:
            # fall back to the RECORD KEY of a batch SELECT whose DD points at this cluster
            for pid, prog in programs.items():
                for bf in prog["batch_files"].values():
                    if bf["record_key"] and any(d["dsn"] == name for d in bf["datasets"]):
                        v["key_field"] = f"{bf['record_key']} (FD in {pid})"
        v["aix"] = []
        v["cics_files"] = [f for f, e in cics_files.items() if e["base_cluster"] == name]
        for a in aix.values():
            if a["relate"] == name:
                lay = next((layouts[c["copybook"]][c["record"]] for c in cands if c.get("copybook")), None)
                keyf = [f for f in lay["fields"] if f[2] == a["key_offset"] and f[3] == a["key_length"]] if lay else []
                pth = next((pp for pp, pe in paths.items() if pe["pathentry"] == a["aix"]), None)
                lca = listcat.get(a["aix"], {})
                v["aix"].append({**a, "key_field": keyf[0][0] if keyf else None, "path": pth,
                                 "cics_file": next((f for f, e in cics_files.items() if e["dsname"] == pth), None),
                                 "listcat": {k: lca[k] for k in ("KEYLEN", "AXRKP", "ref") if k in lca} or None})
        if v["listcat"] and (v["listcat"].get("KEYLEN") != v["key_length"] or v["listcat"].get("MAXLRECL") != v["record_size"][1]):
            findings.append(f"VSAM {name}: JCL DEFINE (key {v['key_length']}, rec {v['record_size']}) differs from LISTCAT {v['listcat']}")

    # copybook usage
    copybooks = OrderedDict()
    for name, p in cpy_files.items():
        users = [pid for pid, pr in programs.items() if any(c["name"] == name for c in pr["copybooks"])]
        copybooks[name] = {"copybook": name, "file": rel(p), "lines": len(p.read_text(errors="replace").splitlines()),
                           "records": {r: l["length"] for r, l in layouts.get(name, {}).items()}, "used_by": users}

    return dict(programs=programs, transactions=transactions, cics_files=cics_files, vsam=vsam, aix=aix,
                paths=paths, gdgs=gdgs, jobs=jobs, procs=procs, mapsets=mapsets, copybooks=copybooks,
                main_menu=main_menu, admin_menu=admin_menu, csd=csd, findings=findings, layouts=layouts)


# --------------------------------------------------------------------------- checks
def unresolved_targets(inv):
    bad = []
    for pid, p in inv["programs"].items():
        for s in p["xctl"]:
            if not s["targets"]:
                bad.append(f"{pid} XCTL {s['expression']} at {s['ref']}: no target")
            for t in s["targets"]:
                if t["program"] not in inv["programs"]:
                    bad.append(f"{pid} XCTL -> {t['program']} at {s['ref']}: not a program in app/cbl")
        for c in p["calls"]:
            if c["kind"] == "unresolved":
                bad.append(f"{pid} CALL {c['program']} at {c['refs'][0]}: unresolved")
    for t in inv["transactions"].values():
        if t["program"] not in inv["programs"] and t["program"] not in inv["csd"]["PROGRAM"]:
            bad.append(f"transaction {t['transid']}: program {t['program']} undefined")
    return bad


def counts(inv):
    return {
        "programs": len(inv["programs"]),
        "programs_cics": sum(p["type"] == "CICS" for p in inv["programs"].values()),
        "programs_batch": sum(p["type"] == "batch" for p in inv["programs"].values()),
        "programs_utility": sum(p["type"] == "utility" for p in inv["programs"].values()),
        "bms_mapsets": len(inv["mapsets"]),
        "copybooks": len(inv["copybooks"]),
        "bms_symbolic_copybooks": len(listdir(APP / "cpy-bms")),
        "jcl_jobs": len(inv["jobs"]),
        "procs": len(inv["procs"]),
        "transactions": len(inv["transactions"]),
        "cicsFiles": len(inv["cics_files"]),
        "vsam_clusters": len(inv["vsam"]),
        "alternate_indexes": len(inv["aix"]),
        "csd_programs": len(inv["csd"]["PROGRAM"]),
        "csd_mapsets": len(inv["csd"]["MAPSET"]),
    }


def short(dsn):
    d = dsn[len(HLQ):] if dsn.startswith(HLQ) else dsn
    return d


def node_id(s):
    return re.sub(r"[^A-Za-z0-9]", "_", s)


# --------------------------------------------------------------------------- JSON
def to_json(inv):
    progs = []
    for pid, p in inv["programs"].items():
        progs.append(OrderedDict(
            program=pid, file=p["file"], type=p["type"], role=p.get("role", "online (pseudo-conversational)" if p["type"] == "CICS" else None),
            lines=p["lines"], transaction=p["transaction"],
            bms_mapsets=[{"mapset": m["mapset"], "maps": m["maps"], "refs": m["refs"]} for m in p["mapsets"].values()],
            copybooks=[c for c in p["copybooks"]],
            cics_files=[{"file": f["file"], "dsname": f.get("dsname"), "ops": f["ops"],
                         "access": "RW" if set(f["ops"]) & {"WRITE", "REWRITE", "DELETE"} else "R",
                         "records": f["records"], "copybooks": f.get("copybooks", []), "refs": f["refs"]}
                        for f in p["cics_files"].values()],
            batch_files=[{k: v for k, v in bf.items()} for bf in p["batch_files"].values()],
            xctl=p["xctl"], xctl_targets=sorted({t["program"] for s in p["xctl"] for t in s["targets"]}),
            calls=p["calls"], tdq=p["tdq"], return_transid=p["return_transid"], jcl_runs=p["jcl_runs"]))
    jobs = []
    for jn, j in inv["jobs"].items():
        steps = []
        for s in j["steps"]:
            st = OrderedDict(step=s["name"], program=s.get("program"), proc=s.get("proc"), via_proc=s.get("via_proc"),
                             ref=s["ref"], params=s["params"] or None,
                             dds=[{k: v for k, v in d.items() if k not in ("instream", "instream_line")} for d in s["dds"]])
            for k in ("idcams", "sort_control", "cemt", "csdup"):
                if k in s:
                    st[k] = s[k]
            steps.append({k: v for k, v in st.items() if v is not None})
        jobs.append(OrderedDict(job=j["name"], file=j["file"], ref=j["ref"], description=j.get("description"),
                                jcllib=j["jcllib"], programs=j["programs"], steps=steps, datasets=j["datasets"]))
    vsam = []
    for name, v in inv["vsam"].items():
        vsam.append(OrderedDict(dataset=name, name=short(name), key_length=v["key_length"], key_offset=v["key_offset"],
                                key_field=v["key_field"], record_size=v["record_size"], copybooks=v["copybooks"],
                                alternate_indexes=v["aix"], cics_files=v["cics_files"], defined_in=v["defined_in"],
                                listcat=v["listcat"]))
    return OrderedDict(
        generated_by="docs/migration/tools/build_inventory.py",
        source_root="app/",
        counts=counts(inv),
        expected_counts=EXPECTED,
        unresolved_targets=unresolved_targets(inv),
        findings=inv["findings"],
        programs=progs,
        transactions=list(inv["transactions"].values()),
        cics_files=[{**{k: v for k, v in f.items() if k != "programs"}, "programs": [{"program": k, "ops": v} for k, v in f["programs"].items()]}
                    for f in inv["cics_files"].values()],
        vsam_files=vsam,
        gdg_bases=list(inv["gdgs"].values()),
        bms_mapsets=list(inv["mapsets"].values()),
        copybooks=list(inv["copybooks"].values()),
        menus={"main": inv["main_menu"], "admin": inv["admin_menu"]},
        jcl_jobs=jobs,
        procs=[{"proc": k, "file": v["file"], "steps": [{"step": s["name"], "program": s.get("program"), "proc": s.get("proc"), "ref": s["ref"]} for s in v["steps"]]}
               for k, v in inv["procs"].items()],
        csd_other={"programs_without_source": [{"program": k, **v} for k, v in inv["csd"]["PROGRAM"].items() if k not in inv["programs"]],
                   "libraries": list(inv["csd"]["LIBRARY"].values()), "tdqueues": list(inv["csd"]["TDQUEUE"].values())},
    )


# --------------------------------------------------------------------------- Mermaid
def mermaid_program_files(inv):
    out = ["flowchart LR"]
    vsam_short = {name: short(name).replace(".VSAM.KSDS", "") for name in inv["vsam"]}

    def ds_node(dsn):
        base = dsn
        for pth, pe in inv["paths"].items():
            if dsn == pth:
                base = inv["aix"][pe["pathentry"]]["relate"]
        if base in vsam_short:
            return node_id("V_" + vsam_short[base]), f"{vsam_short[base]} - VSAM", "vsam"
        b = re.sub(r"\([+-]?\d+\)$", "", short(dsn))
        return node_id("S_" + b), b, "seq"
    nodes, edges = OrderedDict(), []
    out.append("  subgraph online[Online - CICS]")
    for pid, p in inv["programs"].items():
        if p["type"] == "CICS" and p["cics_files"]:
            out.append(f"    {node_id(pid)}[{pid}]")
    out.append("  end")
    out.append("  subgraph batch[Batch]")
    for pid, p in inv["programs"].items():
        if p["type"] == "batch":
            out.append(f"    {node_id(pid)}[{pid}]")
    out.append("  end")
    for pid, p in inv["programs"].items():
        for f in p["cics_files"].values():
            if not f.get("dsname"):
                continue
            nid, label, kind = ds_node(f["dsname"])
            nodes[nid] = (label, kind)
            acc = "RW" if set(f["ops"]) & {"WRITE", "REWRITE", "DELETE"} else "R"
            via = f" via {f['file']}" if f["file"] != vsam_short.get(f["dsname"]) else ""
            edges.append((node_id(pid), nid, f"{acc} {f['file']}"))
        for bf in p["batch_files"].values():
            for d in bf["datasets"]:
                nid, label, kind = ds_node(d["dsn"])
                nodes[nid] = (label, kind)
                edges.append((node_id(pid), nid, f"{bf['access']} {bf['dd']}"))
    seen = set()
    for nid, (label, kind) in nodes.items():
        out.append(f'  {nid}[("{label}")]' if kind == "vsam" else f'  {nid}[/"{label}"/]')
    for a, b, lbl in edges:
        if (a, b, lbl) in seen:
            continue
        seen.add((a, b, lbl))
        out.append(f"  {a} -->|{lbl}| {b}")
    return "\n".join(out)


def mermaid_screen_flow(inv):
    out = ["flowchart TD"]
    for pid, p in inv["programs"].items():
        if p["type"] != "CICS":
            continue
        ms = ",".join(p["mapsets"]) or "-"
        out.append(f"  {node_id(pid)}[\"{p['transaction'] or '?'} {pid}<br/>map {ms}\"]")
    for pid, p in inv["programs"].items():
        for s in p["xctl"]:
            for t in s["targets"]:
                lbl = "menu option" if "option" in t["via"] else "return to caller" if "CDEMO-FROM-PROGRAM" in t["via"] else "XCTL"
                out.append(f"  {node_id(pid)} -->|{lbl}| {node_id(t['program'])}")
    uniq = []
    for l in out:
        if l not in uniq:
            uniq.append(l)
    for pid, p in inv["programs"].items():
        for c in p["calls"]:
            if c["kind"] == "in-repo program" and p["type"] == "CICS":
                uniq.append(f"  {node_id(pid)} -.->|CALL| {node_id(c['program'])}[{c['program']}]")
    return "\n".join(uniq)


def mermaid_batch_flow(inv):
    out = ["flowchart LR"]
    for jn, j in inv["jobs"].items():
        steps = [s for s in j["steps"] if s.get("program") in inv["programs"]]
        for s in steps:
            out.append(f"  J_{node_id(j['name'])}[[{j['name']}]] --> {node_id(s['program'])}[{s['program']}]")
    for pid, p in inv["programs"].items():
        for c in p["calls"]:
            if c["kind"] == "in-repo program" and p["type"] == "batch":
                out.append(f"  {node_id(pid)} -.->|CALL| {node_id(c['program'])}[{c['program']}]")
    return "\n".join(out)


# --------------------------------------------------------------------------- Markdown
def md_table(headers, rows):
    out = ["| " + " | ".join(headers) + " |", "|" + "|".join("---" for _ in headers) + "|"]
    for r in rows:
        out.append("| " + " | ".join(str(c).replace("|", "\\|").replace("\n", "<br>") for c in r) + " |")
    return "\n".join(out)


def cpy_label(cps):
    named = [c for c in cps if not c.startswith("inline:")]
    inline = [c[7:] for c in cps if c.startswith("inline:")]
    if len(inline) > 2:
        named.append(f"{len(inline)} inline WORKING-STORAGE/LINKAGE records")
    else:
        named += [f"inline {r}" for r in inline]
    return ", ".join(named) or "-"


def link(r):
    """file:line -> markdown link relative to docs/migration/."""
    path, _, line = r.partition(":")
    return f"[{Path(path).name}:{line}](../../{path}#L{line})" if line else f"[{Path(path).name}](../../{path})"


def render_md(inv, j):
    c = j["counts"]
    P = inv["programs"]
    L = []
    w = L.append
    w("# CardDemo legacy inventory")
    w("")
    w("Generated from `app/` by `docs/migration/tools/build_inventory.py` (plan step s1.2). "
      "The same data, with a `file:line` reference on every fact, is in [`inventory.json`](inventory.json). "
      "Do not edit this file by hand; re-run the generator.")
    w("")
    w("## 1. Totals")
    w("")
    rows = [
        ("COBOL programs (`app/cbl`)", c["programs"], EXPECTED["programs"], f"{c['programs_cics']} CICS, {c['programs_batch']} batch, {c['programs_utility']} utility"),
        ("BMS mapsets (`app/bms`)", c["bms_mapsets"], EXPECTED["bms_mapsets"], f"{c['bms_symbolic_copybooks']} symbolic-map copybooks in `app/cpy-bms`"),
        ("Copybooks (`app/cpy`)", c["copybooks"], EXPECTED["copybooks"], ""),
        ("JCL jobs (`app/jcl`)", c["jcl_jobs"], EXPECTED["jcl_jobs"], f"+ {c['procs']} procedures in `app/proc`"),
        ("CICS transactions (CSD)", c["transactions"], EXPECTED["transactions"], ""),
        ("CICS files (CSD)", c["cicsFiles"], EXPECTED["cicsFiles"], "6 base clusters + 2 alternate-index paths"),
        ("VSAM KSDS clusters (IDCAMS DEFINE in JCL)", c["vsam_clusters"], "-", f"{c['alternate_indexes']} alternate indexes"),
        ("CSD program definitions", c["csd_programs"], "-", "one more than the 17 CICS sources, see finding F1"),
    ]
    w(md_table(["Artefact", "Found", "Expected (ticket)", "Notes"], [(a, b, e, n) for a, b, e, n in rows]))
    w("")
    unresolved = j["unresolved_targets"]
    w(f"**Unresolved XCTL/CALL targets: {len(unresolved)}.** " + ("Every XCTL and CALL site resolves to a program in `app/cbl` or to an IBM Language Environment service." if not unresolved else ""))
    for u in unresolved:
        w(f"- {u}")
    w("")

    # findings
    w("## 2. Findings and discrepancies")
    w("")
    w("Facts below cite the source line; anything inferred is marked **Hypothesis**.")
    w("")
    csd_only = j["csd_other"]["programs_without_source"]
    for i, f in enumerate(FINDINGS(inv), 1):
        w(f"{i}. {f}")
    for f in inv["findings"]:
        w(f"- Generator note: {f}")
    w("")

    # programs
    w("## 3. Programs")
    w("")
    rows = []
    for pid, p in P.items():
        files = [f"{f['file']} ({'RW' if set(f['ops']) & {'WRITE', 'REWRITE', 'DELETE'} else 'R'})" for f in p["cics_files"].values()]
        files += [f"{bf['dd']} ({bf['access']})" for bf in p["batch_files"].values()]
        cps = [x["name"] for x in p["copybooks"] if x["kind"] == "copybook"]
        xt = sorted({t["program"] for s in p["xctl"] for t in s["targets"]})
        calls = [x["program"] for x in p["calls"]]
        rows.append((f"[{pid}](../../{p['file']})", p["type"] + (f" ({p['role']})" if p.get("role") and p["type"] != "CICS" else ""),
                     p["lines"], p["transaction"] or "-", ", ".join(p["mapsets"]) or "-",
                     ", ".join(cps) or "-", ", ".join(files) or "-", ", ".join(xt) or "-", ", ".join(calls) or "-"))
    w(md_table(["Program", "Type", "Lines", "TransID", "BMS mapset", "Copybooks (app/cpy)", "Files (CICS FILE or batch DD, access)", "XCTL targets", "CALL targets"], rows))
    w("")
    w("Access: R = read/browse only, W = write only (OPEN OUTPUT), RW = read and write/rewrite/delete. "
      "BMS symbolic-map copybooks (`app/cpy-bms`) and CICS system copybooks (DFHAID, DFHBMSCA) are listed per program in the JSON.")
    w("")

    w("### 3.1 Online file access (CICS)")
    w("")
    rows = []
    for pid, p in P.items():
        for f in p["cics_files"].values():
            rows.append((pid, f["file"], ", ".join(f["ops"]), cpy_label(f.get("copybooks", [])), " ".join(link(r) for r in f["refs"][:4]) + (" ..." if len(f["refs"]) > 4 else "")))
    w(md_table(["Program", "CICS file", "EXEC CICS commands", "Record copybook", "Source"], rows))
    w("")
    w("### 3.2 Batch file access (SELECT/ASSIGN, OPEN mode, JCL DD)")
    w("")
    rows = []
    for pid, p in P.items():
        for bf in p["batch_files"].values():
            dsns = sorted({d["dsn"] for d in bf["datasets"]})
            rows.append((pid, bf["select"], bf["dd"], f"{bf['organization']}/{bf['access']}", "; ".join(o.split(" (")[0] for o in bf["open"]),
                         ", ".join(bf["verbs"]), cpy_label(bf["copybooks"]), "<br>".join(short(d) for d in dsns) or "-", link(bf["ref"])))
    w(md_table(["Program", "SELECT", "DD", "Org/access", "OPEN", "Verbs", "Record copybook", "Dataset (via JCL)", "Source"], rows))
    w("")

    # xctl resolution
    w("### 3.3 XCTL and CALL resolution")
    w("")
    w("Rules used (applied by the generator and spot-checked by hand):")
    w("")
    w("- `XCTL PROGRAM('LIT')` or a `PIC X VALUE 'LIT'` constant (e.g. `LIT-MENUPGM`): taken literally.")
    w("- `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME(WS-OPTION))` (COMEN01C) and `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)` (COADM01C): every row of the option table in COMEN02Y / COADM02Y.")
    w("- `XCTL PROGRAM(CDEMO-TO-PROGRAM)`: every `MOVE <literal|constant> TO CDEMO-TO-PROGRAM` in the same program (the shared RETURN-TO-PREV-SCREEN pattern sets it before PERFORMing the XCTL paragraph). "
      "Where the program also does `MOVE CDEMO-FROM-PROGRAM TO CDEMO-TO-PROGRAM` (return to caller), the target set is extended with every program that XCTLs to it.")
    w("- `XCTL PROGRAM(CCARD-NEXT-PROG)` (COCRDLIC): the nearest preceding MOVE in the same paragraph.")
    w("- `CALL 'X'`: in-repo program if `app/cbl` has it, otherwise an IBM Language Environment callable service (CEE3ABD, CEEDAYS). No dynamic CALL and no EXEC CICS LINK/START exists in `app/cbl`.")
    w("")
    rows = []
    for pid, p in P.items():
        for s in p["xctl"]:
            tg = "<br>".join(f"{t['program']} - {t['via']}" for t in s["targets"])
            rows.append((pid, link(s["ref"]), f"`{s['expression']}`", tg))
        for cl in p["calls"]:
            rows.append((pid, " ".join(link(r) for r in cl["refs"][:2]) + (" ..." if len(cl["refs"]) > 2 else ""), f"`CALL '{cl['program']}'`",
                         f"{cl['program']} - {cl['kind']}" + (f" ({cl['note']})" if cl.get("note") else "")))
    w(md_table(["Program", "Site", "Expression", "Resolved targets"], rows))
    w("")

    # transactions
    w("## 4. CICS transactions")
    w("")
    rows = []
    for t in inv["transactions"].values():
        pg = P.get(t["program"])
        rows.append((t["transid"], t["program"], ", ".join(pg["mapsets"]) if pg else "-",
                     "yes" if pg else "**no source**", t.get("description") or "-", link(t["ref"])))
    w(md_table(["TransID", "Program", "BMS mapset", "Source in app/cbl", "CSD description", "CSD"], rows))
    w("")
    w("Main menu (COMEN02Y) and admin menu (COADM02Y) options, which drive the menu XCTLs:")
    w("")
    rows = [(f"main {o['num']}", o["name"], o["pgmname"], o.get("usrtype", "-"), link(o["ref"])) for o in inv["main_menu"]]
    rows += [(f"admin {o['num']}", o["name"], o["pgmname"], "-", link(o["ref"])) for o in inv["admin_menu"]]
    w(md_table(["Option", "Label", "Program", "User type", "Source"], rows))
    w("")

    # cics files
    w("## 5. CICS files")
    w("")
    rows = []
    for f in inv["cics_files"].values():
        rows.append((f["file"], short(f["dsname"]), short(f["base_cluster"]) + (" (AIX path)" if f["is_aix_path"] else ""),
                     ", ".join(f["csd_ops"]), ", ".join(f"{k} ({'/'.join(v)})" for k, v in f["programs"].items()), link(f["csd_ref"])))
    w(md_table(["File", "DSNAME", "Base cluster", "CSD ops enabled", "Programs (commands)", "CSD"], rows))
    w("")

    # vsam
    w("## 6. VSAM datasets")
    w("")
    rows = []
    for v in j["vsam_files"]:
        cp = "<br>".join(f"{c['copybook'] or 'inline'} {c['record']}" + (f" ({c['length']} bytes{'' if c.get('matches_recordsize') else ' - MISMATCH'})" if c.get("length") else "")
                         for c in v["copybooks"]) or "-"
        ax = "<br>".join(f"{short(a['aix'])}: key ({a['key_length']},{a['key_offset']}) {a['key_field'] or '?'}, {'unique' if a['unique'] else 'non-unique'}"
                         + (f", CICS file {a['cics_file']}" if a.get("cics_file") else "") for a in v["alternate_indexes"]) or "-"
        lc = v["listcat"]
        rows.append((v["name"], f"({v['key_length']},{v['key_offset']}) {v['key_field'] or '?'}", "/".join(map(str, v["record_size"])), cp, ax,
                     ", ".join(v["cics_files"]) or "-", "<br>".join(v["defined_in"]),
                     "yes" if lc and lc.get("KEYLEN") == v["key_length"] and lc.get("MAXLRECL") == v["record_size"][1] else "not in LISTCAT" if not lc else "differs"))
    w(md_table(["Cluster", "Primary key (len,offset) field", "Record avg/max", "Record copybook (computed length)", "Alternate indexes", "CICS files", "Defined in", "Matches LISTCAT"], rows))
    w("")
    w("Record lengths are computed from the copybook PIC/USAGE clauses by the generator and compared with the IDCAMS `RECORDSIZE`; "
      "the key field is the copybook item whose offset and length equal `KEYS(len offset)`.")
    w("")
    w("GDG bases: " + ", ".join(f"{short(g['gdg'])} (" + "; ".join(f"limit {d['limit']} in {Path(d['defined_in'].split(' step')[0]).name}" for d in g["definitions"]) + ")"
                             for g in inv["gdgs"].values()) + ". TRANREPT is defined twice: DEFGDGB.jcl with LIMIT(5) SCRATCH followed by `IF LASTCC=12 THEN SET MAXCC=0`, and REPTFILE.jcl with LIMIT(10), no SCRATCH and no such guard, so the effective limit depends on which job ran first.")
    w("")

    # bms
    w("## 7. BMS mapsets")
    w("")
    rows = [(m["mapset"], ", ".join(x["map"] for x in m["maps"]), m["named_fields"], ", ".join(m["programs"]) or "-",
             Path(m["symbolic_copybook"]).name if m["symbolic_copybook"] else "-", link(m["ref"])) for m in inv["mapsets"].values()]
    w(md_table(["Mapset", "Map", "Named fields (DFHMDF)", "Program", "Symbolic map copybook", "Source"], rows))
    w("")

    # copybooks
    w("## 8. Copybooks")
    w("")
    rows = [(c["copybook"], ", ".join(f"{r} ({n})" for r, n in c["records"].items()) or "(procedure/fragment)", ", ".join(c["used_by"]) or "**unused**", f"[{Path(c['file']).name}](../../{c['file']})")
            for c in inv["copybooks"].values()]
    w(md_table(["Copybook", "01 records (computed bytes)", "Used by", "Source"], rows))
    w("")

    # jcl
    w("## 9. JCL jobs")
    w("")
    rows = []
    for job in j["jcl_jobs"]:
        steps = []
        for s in job["steps"]:
            prog = s.get("program") or f"PROC {s.get('proc')}"
            extra = ""
            if s.get("idcams"):
                extra = " " + "/".join(sorted({c["verb"] + (" " + c.get("kind", "") if c["verb"] == "DEFINE" else "") for c in s["idcams"]}))
            if s.get("cemt"):
                extra = " CEMT " + ",".join(re.findall(r"FIL\((\w+)", " ".join(s["cemt"]))) + " " + ("OPE" if "OPE" in s["cemt"][0] else "CLO")
            if s.get("via_proc"):
                extra += f" (from PROC {s['via_proc']})"
            steps.append(f"{s['step']}: {prog}{extra}")
        ds = []
        for d in job["datasets"]:
            roles = sorted({r["role"] for r in d["roles"]})
            ds.append(f"{short(d['dataset'])} [{', '.join(roles)}]")
        rows.append((f"[{job['job']}](../../{job['file']})", (job.get("description") or "").strip(), "<br>".join(steps), "<br>".join(ds) or "-"))
    w(md_table(["Job (file)", "Description", "Steps: program", "Datasets [role]"], rows))
    w("")
    w("Procedures: " + "; ".join(f"`{k}` ({v['file']}): " + ", ".join(f"{s['name']}={s.get('program') or 'PROC ' + str(s.get('proc'))}" for s in v["steps"]) for k, v in inv["procs"].items()) + ".")
    w("")

    # graphs
    w("## 10. Program-to-file graph")
    w("")
    w("Cylinders are VSAM clusters (alternate-index paths are folded into their base cluster; the edge label names the CICS FILE or DD actually used), parallelograms are sequential/GDG datasets. CBTRN01C has no edges because no job in app/jcl runs it (finding F8).")
    w("")
    w("```mermaid")
    w(mermaid_program_files(inv))
    w("```")
    w("")
    w("## 11. Screen-flow graph (online)")
    w("")
    w("COSGN00C XCTLs to COADM01C when `CDEMO-USRTYP-ADMIN` (user type from USRSEC) and to COMEN01C otherwise "
      f"({link(P['COSGN00C']['xctl'][0]['ref'])}, {link(P['COSGN00C']['xctl'][1]['ref'])}).")
    w("")
    w("Edges are EXEC CICS XCTL transfers (section 3.3); `menu option` edges come from the COMEN02Y/COADM02Y tables, `return to caller` from `MOVE CDEMO-FROM-PROGRAM TO CDEMO-TO-PROGRAM`. Each program also re-enters itself with `EXEC CICS RETURN TRANSID(<own transid>)` (pseudo-conversational).")
    w("")
    w("```mermaid")
    w(mermaid_screen_flow(inv))
    w("```")
    w("")
    w("## 12. Batch job-to-program graph")
    w("")
    w("```mermaid")
    w(mermaid_batch_flow(inv))
    w("```")
    w("")
    w("## 13. How this was checked")
    w("")
    for line in VERIFICATION:
        w(f"- {line}")
    w("")
    return "\n".join(L)


def FINDINGS(inv):
    P, csd, T = inv["programs"], inv["csd"], inv["transactions"]
    out = []
    missing = [k for k in csd["PROGRAM"] if k not in P]
    for m in missing:
        tr = [t for t in T.values() if t["program"] == m]
        callers = [pid for pid, p in P.items() if any(t["program"] == m for s in p["xctl"] for t in s["targets"])]
        out.append(f"**F1 - CSD program without source.** CARDDEMO.CSD defines PROGRAM({m}) '{csd['PROGRAM'][m].get('description', '')}' ({link(csd['PROGRAM'][m]['ref'])})"
                   + (f" and TRANSACTION({tr[0]['transid']}) '{tr[0].get('description')}' ({link(tr[0]['ref'])})" if tr else "")
                   + f", but `app/cbl` has no {m} source and {'no program XCTLs to it' if not callers else 'it is reached from ' + ', '.join(callers)}. "
                   "So the CSD has 18 programs / 18 transactions for 17 CICS sources. **Out of scope (no source), per SCOPE.md.**")
    j = inv["jobs"]
    cb = j.get("CBADMCDJ")
    if cb:
        defs = [d for s in cb["steps"] for d in s.get("csdup", [])]
        progs = sorted({re.search(r"PROGRAM\((\w+)\)", d).group(1) for d in defs if "PROGRAM(" in d})
        ghosts = [p for p in progs if p not in P]
        trans = sorted({re.search(r"TRANSACTION\((\w+)\)", d).group(1) for d in defs if "TRANSACTION(" in d})
        out.append(f"**F2 - CBADMCDJ.jcl does not match CARDDEMO.CSD.** Its DFHCSDUP input ({link(next(s['ref'] for s in cb['steps'] if s.get('csdup')))}) defines programs {', '.join(ghosts)} that have no source, "
                   f"and transactions {', '.join(trans)} that are not in CARDDEMO.CSD. The CSD file, not this job, is used as the authority for this inventory. "
                   "**Hypothesis:** CBADMCDJ is an older/sample CSD build job.")
    if "COCRDLIC" in csd["PROGRAM"]:
        out.append(f"**F3 - COCRDLIC PROGRAM definition carries TRANSID(CC00)** ({link(csd['PROGRAM']['COCRDLIC']['ref'])}); CC00 is the sign-on transaction (COSGN00C). "
                   "The PROGRAM TRANSID attribute only matters for DPL mirror transactions, so it has no effect on the XCTL-based flow (inference from CICS semantics).")
    unused = [c for c, v in inv["copybooks"].items() if not v["used_by"]]
    if unused:
        out.append(f"**F4 - Copybooks not COPYed by any program in app/cbl:** {', '.join(unused)}. They are kept in the inventory for completeness.")
    trx = inv["vsam"].get(HLQ + "TRXFL.VSAM.KSDS")
    if trx:
        out.append(f"**F5 - Work cluster TRXFL.** CREASTMT.JCL defines AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS (key {trx['key_length']} = card number + transaction id, built by SORT from TRANSACT) "
                   "for the statement programs CBSTM03A/CBSTM03B. It is not in LISTCAT.txt and has no CICS FILE. CBSTM03B reads it through an inline FD (FD-TRNXFILE-REC), and CBSTM03A maps it with COSTM01 (TRNX-RECORD).")
    x = P.get("CBACT04C")
    if x:
        out.append("**F6 - INTCALC passes an AIX path DD (XREFFIL1) that CBACT04C never names.** CBACT04C declares XREF-FILE with `ALTERNATE RECORD KEY IS FD-XREF-ACCT-ID` on DD XREFFILE "
                   f"({link(x['batch_files']['XREF-FILE']['ref'])}); INTCALC.jcl adds DD XREFFIL1 = CARDXREF.VSAM.AIX.PATH. On z/OS the alternate-index path for an ALTERNATE RECORD KEY is allocated "
                   "with the base DD name truncated to 7 characters plus a digit, so XREFFIL1 belongs to XREFFILE (IBM Enterprise COBOL convention, not verified on a running system).")
    out.append("**F7 - TRANREPT batch flow is also submitted online.** CORPT00C builds JCL in WORKING-STORAGE (`//TRNRPT00 JOB`, `EXEC PROC=TRANREPT`) and writes it to TDQ JOBS "
               f"({link(P['CORPT00C']['tdq'][0]['ref'])}); the CSD defines TDQUEUE(JOBS) ({link(csd['TDQUEUE']['JOBS']['ref'])}). The job it submits runs app/proc/TRANREPT.prc (REPROC backup, SORT, CBTRN03C). "
               "**Hypothesis:** JOBS is an extrapartition queue pointing at the JES internal reader, so the target needs a job-launch API for this screen.")
    out.append("**F8 - Programs with no JCL in app/jcl:** "
               + (", ".join(pid for pid, p in P.items() if p["type"] == "batch" and not p["jcl_runs"]) or "none")
               + ". CBSTM03B runs only via `CALL 'CBSTM03B'` from CBSTM03A (CREASTMT step STEP040). "
               + "".join(f"{pid} also OPENs {', '.join(bf['dd'] for bf in p['batch_files'].values() if not bf['verbs'])} without ever reading them "
                         f"({', '.join(link(bf['ref']) for bf in p['batch_files'].values() if not bf['verbs'])}). "
                         for pid, p in P.items() if p["type"] == "batch" and any(not bf["verbs"] for bf in p["batch_files"].values()))
               + "**Hypothesis:** CBTRN01C is an earlier/unused variant of the daily-transaction posting step (CBTRN02C).")
    dc = j.get("DEFCUST")
    if dc:
        names = sorted({c.get("name") for st in dc["steps"] for c in st.get("idcams", []) if c.get("name")})
        out.append(f"**F9 - DEFCUST.jcl uses dataset names outside the AWS.M2.CARDDEMO HLQ** ({', '.join(names)}; {link(dc['ref'])}); no program, CICS FILE or other job references them, "
                   "and CUSTFILE.jcl is the job that actually defines AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS. Its cluster has KEYS(10 0) whereas CUSTDATA uses KEYS(9 0), and both its steps are named STEP05. **Hypothesis:** obsolete job.")
    return out


VERIFICATION = [
    "Program list: the 28 `PROGRAM-ID`s in `app/cbl` (file names match PROGRAM-ID). Type = CICS when the source contains `EXEC CICS`; batch when it has FILE-CONTROL `SELECT`s; utility otherwise (CSUTLDTC: `PROCEDURE DIVISION USING`, no files, CALLed by CORPT00C and COTRN02C).",
    "Line counts are the physical lines of each source file (same as `wc -l`), comments included.",
    "Copybooks: every `COPY` statement (comment lines in column 7 excluded), matched against `app/cpy`, `app/cpy-bms` and the CICS system copybooks DFHAID/DFHBMSCA.",
    "CICS files: `DATASET(...)`/`FILE(...)` operands of every EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR/READNEXT/READPREV/ENDBR, with `LIT-*`/`WS-*-FILE` constants resolved from their `VALUE` clauses, then matched against `DEFINE FILE` in app/csd/CARDDEMO.CSD.",
    "Batch files: `SELECT ... ASSIGN TO <dd>` + `OPEN INPUT/OUTPUT/I-O` per file, joined to the `//<dd> DD DSN=` of the JCL step that runs the program (PROC overrides such as `PRC001.FILEIN` applied).",
    "XCTL/CALL: every EXEC CICS XCTL and CALL statement, resolved by the rules in 3.3. EXEC CICS LINK, START and dynamic CALL were searched for and do not occur.",
    "Transactions and mapsets: `DEFINE TRANSACTION`/`DEFINE MAPSET` in CARDDEMO.CSD; each program's `RETURN TRANSID(...)` was compared with its CSD transaction and each `SEND/RECEIVE MAP/MAPSET` resolved to the BMS source (DFHMSD/DFHMDI).",
    "VSAM: `DEFINE CLUSTER/ALTERNATEINDEX/PATH` in the IDCAMS SYSIN of each JCL, cross-checked against `KEYLEN`/`RKP`/`MAXLRECL`/`AXRKP` in app/catlg/LISTCAT.txt and against the record length computed from the copybook.",
    "Totals and the 'no unresolved targets' criterion are asserted by `python3 docs/migration/tools/build_inventory.py --check`.",
]


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--check", action="store_true", help="verify counts, resolution and that the committed files are current")
    args = ap.parse_args()
    inv = build()
    j = to_json(inv)
    js = json.dumps(j, indent=2) + "\n"
    md = render_md(inv, j)
    ok = True
    for k, v in EXPECTED.items():
        status = "OK" if j["counts"][k] == v else "MISMATCH"
        ok &= status == "OK"
        print(f"{k:<14} expected {v:>3}  found {j['counts'][k]:>3}  {status}")
    print(f"unresolved XCTL/CALL targets: {len(j['unresolved_targets'])}")
    for u in j["unresolved_targets"]:
        print("  " + u)
    ok &= not j["unresolved_targets"]
    if args.check:
        for name, content in (("inventory.json", js), ("inventory.md", md)):
            cur = (OUT / name).read_text() if (OUT / name).exists() else None
            same = cur == content
            ok &= same
            print(f"{name}: {'up to date' if same else 'STALE - re-run the generator'}")
    else:
        (OUT / "inventory.json").write_text(js)
        (OUT / "inventory.md").write_text(md)
        print(f"wrote {rel(OUT / 'inventory.json')} and {rel(OUT / 'inventory.md')}")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
