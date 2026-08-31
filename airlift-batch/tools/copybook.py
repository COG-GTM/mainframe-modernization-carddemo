#!/usr/bin/env python3
"""Parser for the CardDemo copybooks in app/cpy.

The parser is the single source of truth for record layouts on the harness
side: fixtures, the golden-output codec and the neutral comparator all derive
their field offsets from the checked-in copybook text rather than from
hand-written constants.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

# Area A starts in column 8 of a fixed-format COBOL line; column 7 carries the
# indicator (`*` for a comment). Copybook text in app/cpy is fixed format.
COMMENT_COLUMN = 6
CODE_START = 6
CODE_END = 72

LEVEL_RE = re.compile(r"^\s*(\d\d)\s+([A-Z0-9$#@-]+|FILLER)\b(.*)$", re.IGNORECASE)
PIC_RE = re.compile(r"\b(?:PIC|PICTURE)\s+(?:IS\s+)?(\S+)", re.IGNORECASE)
USAGE_RE = re.compile(
    r"\b(?:USAGE\s+(?:IS\s+)?)?(COMP-3|COMPUTATIONAL-3|PACKED-DECIMAL|COMP-5|"
    r"COMP-4|COMP-1|COMP-2|COMP|COMPUTATIONAL|BINARY|DISPLAY)\b",
    re.IGNORECASE,
)
REDEFINES_RE = re.compile(r"\bREDEFINES\s+([A-Z0-9$#@-]+)", re.IGNORECASE)
OCCURS_RE = re.compile(r"\bOCCURS\s+(\d+)", re.IGNORECASE)


class CopybookError(Exception):
    pass


@dataclass
class Field:
    name: str
    level: int
    offset: int = 0
    size: int = 0
    picture: str | None = None
    usage: str = "DISPLAY"
    digits: int = 0
    scale: int = 0
    signed: bool = False
    numeric: bool = False
    occurs: int = 1
    redefines: str | None = None
    children: list["Field"] = field(default_factory=list)

    @property
    def is_group(self) -> bool:
        return bool(self.children)

    def leaves(self, prefix: str = "") -> list[tuple[str, "Field"]]:
        """Flatten to (dotted-name, field) pairs, skipping REDEFINES and FILLER."""
        out: list[tuple[str, Field]] = []
        for child in self.children:
            if child.redefines is not None:
                continue
            name = child.name if not prefix else f"{prefix}.{child.name}"
            if child.is_group:
                out.extend(child.leaves(name))
            elif child.name != "FILLER":
                out.append((name, child))
        return out


def _logical_lines(text: str) -> list[str]:
    """Strip sequence area, comments and columns past 72; join continuations."""
    lines: list[str] = []
    for raw in text.splitlines():
        padded = raw.ljust(CODE_END)
        if len(padded) > COMMENT_COLUMN and padded[COMMENT_COLUMN] in "*/":
            continue
        code = padded[CODE_START:CODE_END].rstrip()
        if not code.strip():
            continue
        lines.append(code)
    statements: list[str] = []
    buffer = ""
    for code in lines:
        buffer = f"{buffer} {code.strip()}" if buffer else code.strip()
        while "." in buffer:
            head, _, buffer = buffer.partition(".")
            if head.strip():
                statements.append(head.strip())
            buffer = buffer.strip()
    if buffer.strip():
        statements.append(buffer.strip())
    return statements


def _picture_size(picture: str, usage: str) -> tuple[int, int, int, bool, bool]:
    """Return (size, digits, scale, signed, numeric) for a PICTURE string."""
    pic = picture.upper().rstrip(".")
    expanded = ""
    index = 0
    while index < len(pic):
        symbol = pic[index]
        if index + 1 < len(pic) and pic[index + 1] == "(":
            close = pic.index(")", index)
            count = int(pic[index + 2 : close])
            expanded += symbol * count
            index = close + 1
        else:
            expanded += symbol
            index += 1
    signed = "S" in expanded
    body = expanded.replace("S", "")
    if "V" in body:
        whole, _, frac = body.partition("V")
        scale = frac.count("9")
        digits = whole.count("9") + scale
    else:
        scale = 0
        digits = body.count("9")
    numeric = digits > 0 and set(body) <= {"9", "V"}
    if not numeric:
        # Character (or edited) item: one byte per position except the implied
        # decimal point, which occupies no storage.
        return len(body.replace("V", "")), digits, scale, signed, False
    if usage in ("COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL"):
        size = (digits + 2) // 2
    elif usage in ("COMP", "COMPUTATIONAL", "BINARY", "COMP-4", "COMP-5"):
        size = 2 if digits <= 4 else (4 if digits <= 9 else 8)
    else:
        usage = "DISPLAY"
        size = digits
    return size, digits, scale, signed, True


def parse_text(text: str) -> list[Field]:
    """Parse copybook text into the 01-level records it declares."""
    records: list[Field] = []
    stack: list[Field] = []
    offsets: dict[int, int] = {}
    for statement in _logical_lines(text):
        match = LEVEL_RE.match(statement)
        if not match:
            continue
        level = int(match.group(1))
        name = match.group(2).upper()
        rest = match.group(3)
        if level == 88:
            continue
        node = Field(name=name, level=level)
        redefines = REDEFINES_RE.search(rest)
        if redefines:
            node.redefines = redefines.group(1).upper()
        occurs = OCCURS_RE.search(rest)
        if occurs:
            node.occurs = int(occurs.group(1))
        usage_match = USAGE_RE.search(rest)
        usage = (usage_match.group(1).upper() if usage_match else "DISPLAY")
        picture = PIC_RE.search(rest)
        while stack and stack[-1].level >= level:
            stack.pop()
        parent = stack[-1] if stack else None
        if parent is None and level != 1:
            # A group-less 01 was expected first; ignore stray levels.
            continue
        if picture:
            node.picture = picture.group(1).rstrip(".")
            size, digits, scale, signed, numeric = _picture_size(node.picture, usage)
            node.size = size * node.occurs
            node.digits = digits
            node.scale = scale
            node.signed = signed
            node.numeric = numeric
            node.usage = usage if numeric else "DISPLAY"
        if parent is None:
            node.offset = 0
            records.append(node)
            offsets[level] = 0
        else:
            if node.redefines is not None:
                sibling = next(
                    (c for c in parent.children if c.name == node.redefines), None
                )
                node.offset = sibling.offset if sibling else parent.offset
            else:
                node.offset = offsets.get(level, parent.offset)
                if not parent.children:
                    node.offset = parent.offset
                else:
                    last = parent.children[-1]
                    node.offset = last.offset + last.size
            parent.children.append(node)
        stack.append(node)
        offsets[level] = node.offset + node.size
        # Propagate group sizes upwards.
        for entry in reversed(stack[:-1]):
            if node.redefines is None:
                entry.size = max(
                    entry.size,
                    node.offset + node.size - entry.offset,
                )
    return records


def parse_file(path: Path) -> list[Field]:
    return parse_text(path.read_text(encoding="utf-8", errors="replace"))


def load_record(cpy_dir: Path, copybook: str, record: str) -> Field:
    """Load a single 01-level record from a named copybook."""
    candidates = [
        cpy_dir / f"{copybook}.cpy",
        cpy_dir / f"{copybook}.CPY",
    ]
    for candidate in candidates:
        if candidate.exists():
            for parsed in parse_file(candidate):
                if parsed.name == record.upper():
                    return parsed
            raise CopybookError(f"{record} not found in {candidate}")
    raise CopybookError(f"copybook {copybook} not found under {cpy_dir}")


def describe(record: Field) -> str:
    lines = [f"{record.name} (length {record.size})"]
    for name, leaf in record.leaves():
        lines.append(
            f"  {leaf.offset:4d} {leaf.size:3d} {name} {leaf.picture} {leaf.usage}"
        )
    return "\n".join(lines)


if __name__ == "__main__":
    import sys

    for argument in sys.argv[1:]:
        for parsed in parse_file(Path(argument)):
            print(describe(parsed))
