"""Fixed-width record layouts mirroring the CardDemo copybooks (app/cpy).

Signed zoned-decimal fields use the EBCDIC overpunch convention found in
app/data/ASCII (``{``/``A``-``I`` positive, ``}``/``J``-``R`` negative), which is
what GnuCOBOL produces with ``-fsign=EBCDIC``.
"""
from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

_POS = "{ABCDEFGHI"
_NEG = "}JKLMNOPQR"


@dataclass(frozen=True)
class Field:
    name: str
    kind: str  # "X", "9" (unsigned zoned) or "S9" (signed zoned)
    length: int
    scale: int = 0


def encode_zoned(value, length: int, scale: int = 0, signed: bool = True) -> str:
    units = int((Decimal(str(value)) * (10 ** scale)).to_integral_value())
    digits = str(abs(units)).rjust(length, "0")
    if len(digits) > length:
        raise ValueError(f"{value} does not fit in {length} digits")
    if not signed:
        if units < 0:
            raise ValueError(f"{value} is negative for an unsigned field")
        return digits
    table = _NEG if units < 0 else _POS
    return digits[:-1] + table[int(digits[-1])]


def decode_zoned(text: str, scale: int = 0) -> Decimal:
    last = text[-1]
    sign = 1
    if last in _POS:
        last = str(_POS.index(last))
    elif last in _NEG:
        last, sign = str(_NEG.index(last)), -1
    elif last == "+":
        text, last = text[:-1], text[-2]
    elif last == "-":
        text, last, sign = text[:-1], text[-2], -1
    units = int((text[:-1] + last).replace(" ", "0") or "0")
    return Decimal(sign * units).scaleb(-scale)


class Layout:
    def __init__(self, name: str, fields: list[Field], key_len: int,
                 alt_key: tuple[int, int] | None = None):
        self.name = name
        self.fields = fields
        self.key_len = key_len
        self.alt_key = alt_key  # (offset, length) of an ALTERNATE RECORD KEY
        self.rec_len = sum(f.length for f in fields)

    def build(self, **values) -> str:
        unknown = set(values) - {f.name for f in self.fields}
        if unknown:
            raise KeyError(f"{self.name}: unknown fields {sorted(unknown)}")
        out = []
        for f in self.fields:
            v = values.get(f.name)
            if f.kind == "X":
                text = "" if v is None else str(v)
                if len(text) > f.length:
                    raise ValueError(f"{f.name}: {text!r} longer than {f.length}")
                out.append(text.ljust(f.length))
            else:
                out.append(encode_zoned(v or 0, f.length, f.scale, f.kind == "S9"))
        return "".join(out)

    def parse(self, record: str) -> dict:
        record = record.ljust(self.rec_len)
        result, pos = {}, 0
        for f in self.fields:
            raw = record[pos:pos + f.length]
            pos += f.length
            if f.name.startswith("FILLER"):
                continue
            result[f.name] = raw.rstrip() if f.kind == "X" else decode_zoned(raw, f.scale)
        return result

    def key(self, record: str) -> str:
        return record[:self.key_len]


X, N, S = "X", "9", "S9"

ACCOUNT = Layout("ACCOUNT-RECORD (CVACT01Y)", [
    Field("ACCT-ID", N, 11), Field("ACCT-ACTIVE-STATUS", X, 1),
    Field("ACCT-CURR-BAL", S, 12, 2), Field("ACCT-CREDIT-LIMIT", S, 12, 2),
    Field("ACCT-CASH-CREDIT-LIMIT", S, 12, 2), Field("ACCT-OPEN-DATE", X, 10),
    Field("ACCT-EXPIRAION-DATE", X, 10), Field("ACCT-REISSUE-DATE", X, 10),
    Field("ACCT-CURR-CYC-CREDIT", S, 12, 2), Field("ACCT-CURR-CYC-DEBIT", S, 12, 2),
    Field("ACCT-ADDR-ZIP", X, 10), Field("ACCT-GROUP-ID", X, 10), Field("FILLER", X, 178),
], key_len=11)

CARD = Layout("CARD-RECORD (CVACT02Y)", [
    Field("CARD-NUM", X, 16), Field("CARD-ACCT-ID", N, 11), Field("CARD-CVV-CD", N, 3),
    Field("CARD-EMBOSSED-NAME", X, 50), Field("CARD-EXPIRAION-DATE", X, 10),
    Field("CARD-ACTIVE-STATUS", X, 1), Field("FILLER", X, 59),
], key_len=16)

XREF = Layout("CARD-XREF-RECORD (CVACT03Y)", [
    Field("XREF-CARD-NUM", X, 16), Field("XREF-CUST-ID", N, 9),
    Field("XREF-ACCT-ID", N, 11), Field("FILLER", X, 14),
], key_len=16, alt_key=(25, 11))

CUSTOMER = Layout("CUSTOMER-RECORD (CVCUS01Y)", [
    Field("CUST-ID", N, 9), Field("CUST-FIRST-NAME", X, 25), Field("CUST-MIDDLE-NAME", X, 25),
    Field("CUST-LAST-NAME", X, 25), Field("CUST-ADDR-LINE-1", X, 50),
    Field("CUST-ADDR-LINE-2", X, 50), Field("CUST-ADDR-LINE-3", X, 50),
    Field("CUST-ADDR-STATE-CD", X, 2), Field("CUST-ADDR-COUNTRY-CD", X, 3),
    Field("CUST-ADDR-ZIP", X, 10), Field("CUST-PHONE-NUM-1", X, 15),
    Field("CUST-PHONE-NUM-2", X, 15), Field("CUST-SSN", N, 9),
    Field("CUST-GOVT-ISSUED-ID", X, 20), Field("CUST-DOB-YYYY-MM-DD", X, 10),
    Field("CUST-EFT-ACCOUNT-ID", X, 10), Field("CUST-PRI-CARD-HOLDER-IND", X, 1),
    Field("CUST-FICO-CREDIT-SCORE", N, 3), Field("FILLER", X, 168),
], key_len=9)

TCATBAL = Layout("TRAN-CAT-BAL-RECORD (CVTRA01Y)", [
    Field("TRANCAT-ACCT-ID", N, 11), Field("TRANCAT-TYPE-CD", X, 2), Field("TRANCAT-CD", N, 4),
    Field("TRAN-CAT-BAL", S, 11, 2), Field("FILLER", X, 22),
], key_len=17)

DISCGRP = Layout("DIS-GROUP-RECORD (CVTRA02Y)", [
    Field("DIS-ACCT-GROUP-ID", X, 10), Field("DIS-TRAN-TYPE-CD", X, 2),
    Field("DIS-TRAN-CAT-CD", N, 4), Field("DIS-INT-RATE", S, 6, 2), Field("FILLER", X, 28),
], key_len=16)

TRANTYPE = Layout("TRAN-TYPE-RECORD (CVTRA03Y)", [
    Field("TRAN-TYPE", X, 2), Field("TRAN-TYPE-DESC", X, 50), Field("FILLER", X, 8),
], key_len=2)

TRANCATG = Layout("TRAN-CAT-RECORD (CVTRA04Y)", [
    Field("TRAN-TYPE-CD", X, 2), Field("TRAN-CAT-CD", N, 4),
    Field("TRAN-CAT-TYPE-DESC", X, 50), Field("FILLER", X, 4),
], key_len=6)

_TRAN_FIELDS = [
    Field("TRAN-ID", X, 16), Field("TRAN-TYPE-CD", X, 2), Field("TRAN-CAT-CD", N, 4),
    Field("TRAN-SOURCE", X, 10), Field("TRAN-DESC", X, 100), Field("TRAN-AMT", S, 11, 2),
    Field("TRAN-MERCHANT-ID", N, 9), Field("TRAN-MERCHANT-NAME", X, 50),
    Field("TRAN-MERCHANT-CITY", X, 50), Field("TRAN-MERCHANT-ZIP", X, 10),
    Field("TRAN-CARD-NUM", X, 16), Field("TRAN-ORIG-TS", X, 26), Field("TRAN-PROC-TS", X, 26),
    Field("FILLER", X, 20),
]
# TRAN-RECORD (CVTRA05Y) and DALYTRAN-RECORD (CVTRA06Y) share one layout.
TRAN = Layout("TRAN-RECORD (CVTRA05Y/CVTRA06Y)", _TRAN_FIELDS, key_len=16)
# CBSTM03B's TRNXFILE is keyed on card number + transaction id.
TRNX = Layout("TRNX-FILE (CBSTM03B)", [Field("TRNX-CARD", X, 16)] + _TRAN_FIELDS[:-1]
              + [Field("FILLER", X, 4)], key_len=32)
