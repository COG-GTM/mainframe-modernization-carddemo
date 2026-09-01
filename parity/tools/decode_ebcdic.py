#!/usr/bin/env python3
"""Decode the delivered EBCDIC sample datasets into the ASCII byte images the GnuCOBOL baseline
and the Java migration both read.

The drop ships each dataset twice: raw EBCDIC under app/data/EBCDIC and a text rendering under
app/data/ASCII. Only the EBCDIC copies are used here, because they are the untranslated form; the
transliteration is CP037 (IBM-037), which round-trips the zoned-decimal sign overpunches
('{' = +0, 'A'-'I' = +1..+9, '}' = -0, 'J'-'R' = -1..-9) that the record layouts in app/cpy depend
on.

Record lengths come from the IDCAMS RECORDSIZE of the cluster each dataset loads
(app/jcl/XREFFILE.jcl:44, app/jcl/ACCTFILE.jcl:41, app/jcl/TCATBALF.jcl:41,
app/jcl/TRANFILE.jcl RECORDSIZE 350) and are asserted so a short or padded extract fails loudly.
"""
import sys
from pathlib import Path

DATASETS = {
    "AWS.M2.CARDDEMO.DALYTRAN.PS": ("DALYTRAN.dat", 350),
    "AWS.M2.CARDDEMO.CARDXREF.PS": ("CARDXREF.dat", 50),
    "AWS.M2.CARDDEMO.ACCTDATA.PS": ("ACCTDATA.dat", 300),
    "AWS.M2.CARDDEMO.TCATBALF.PS": ("TCATBALF.dat", 50),
    "AWS.M2.CARDDEMO.DALYTRAN.PS.INIT": ("TRANINIT.dat", 350),
}


def main(root: Path, out_dir: Path) -> int:
    src_dir = root / "app" / "data" / "EBCDIC"
    out_dir.mkdir(parents=True, exist_ok=True)
    for dsn, (name, reclen) in DATASETS.items():
        raw = (src_dir / dsn).read_bytes()
        if len(raw) % reclen:
            print(f"FAIL {dsn}: {len(raw)} bytes is not a multiple of LRECL {reclen}")
            return 1
        text = raw.decode("cp037").encode("latin1")
        (out_dir / name).write_bytes(text)
        print(f"{dsn} -> {name}: {len(raw) // reclen} records of {reclen}")
    return 0


if __name__ == "__main__":
    repo_root = Path(__file__).resolve().parents[2]
    target = Path(sys.argv[1]) if len(sys.argv) > 1 else repo_root / "parity" / "work" / "in"
    sys.exit(main(repo_root, target))
