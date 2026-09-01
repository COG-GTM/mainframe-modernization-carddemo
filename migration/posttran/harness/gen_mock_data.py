#!/usr/bin/env python3
"""Generate a branch-coverage mock dataset for POSTTRAN / CBTRN02C.

The shipped sample (app/data/ASCII/dailytran.txt) fires only reject reason 102.
This builder derives a small, fully-labelled dataset from the shipped ASCII data
so that every branch cited in docs/phase1-posttran-inventory.md is exercised:

  reason 100  CBTRN02C.cbl:385  card number not in XREF
  reason 101  CBTRN02C.cbl:397  XREF hit but account missing
  reason 102  CBTRN02C.cbl:410  overlimit
  reason 103  CBTRN02C.cbl:417  transaction dated after ACCT-EXPIRAION-DATE
  102+103     CBTRN02C.cbl:410,417  103 wins (checked second, overwrites)
  boundary    CBTRN02C.cbl:407  credit limit exactly equal -> accepted
  boundary    CBTRN02C.cbl:414  expiry date exactly equal -> accepted
  create      CBTRN02C.cbl:503  TCATBAL key absent -> WRITE
  update      CBTRN02C.cbl:526  TCATBAL key present -> REWRITE (accumulates)
  credit      CBTRN02C.cbl:549  positive amount -> ACCT-CURR-CYC-CREDIT
  debit       CBTRN02C.cbl:551  negative amount -> ACCT-CURR-CYC-DEBIT

Layouts come from the copybooks:
  CVTRA06Y  DALYTRAN-RECORD      350
  CVACT03Y  CARD-XREF-RECORD      50
  CVACT01Y  ACCOUNT-RECORD       300
  CVTRA01Y  TRAN-CAT-BAL-RECORD   50

Outputs newline-delimited fixed-width text (same convention as app/data/ASCII).
"""
from __future__ import annotations

import pathlib

REPO = pathlib.Path(__file__).resolve().parents[3]
SRC = REPO / "app" / "data" / "ASCII"
OUT = REPO / "migration" / "posttran" / "testdata" / "mock"

# ---------------------------------------------------------------- zoned decimal
POS = "{ABCDEFGHI"
NEG = "}JKLMNOPQR"


def zoned(value_cents: int, digits: int) -> str:
    """Signed zoned decimal with trailing sign overpunch, as in the ASCII drop."""
    neg = value_cents < 0
    body = str(abs(value_cents)).rjust(digits, "0")
    if len(body) > digits:
        raise ValueError(f"{value_cents} does not fit in {digits} digits")
    table = NEG if neg else POS
    return body[:-1] + table[int(body[-1])]


def unzoned(field: str) -> int:
    """Inverse of zoned()."""
    last = field[-1]
    if last in POS:
        return int(field[:-1] + str(POS.index(last)))
    if last in NEG:
        return -int(field[:-1] + str(NEG.index(last)))
    return int(field)


# ------------------------------------------------------------------- CVTRA06Y
DALYTRAN_FIELDS = [
    ("id", 16), ("type_cd", 2), ("cat_cd", 4), ("source", 10), ("desc", 100),
    ("amt", 11), ("merchant_id", 9), ("merchant_name", 50), ("merchant_city", 50),
    ("merchant_zip", 10), ("card_num", 16), ("orig_ts", 26), ("proc_ts", 26),
    ("filler", 20),
]


def dalytran(tran_id, type_cd, cat_cd, amt_cents, card_num, orig_ts, desc):
    parts = {
        "id": tran_id.ljust(16), "type_cd": type_cd.ljust(2), "cat_cd": cat_cd.rjust(4, "0"),
        "source": "MOCK".ljust(10), "desc": desc.ljust(100), "amt": zoned(amt_cents, 11),
        "merchant_id": "900000001", "merchant_name": "Mock Merchant".ljust(50),
        "merchant_city": "Mock City".ljust(50), "merchant_zip": "00001".ljust(10),
        "card_num": card_num.ljust(16), "orig_ts": orig_ts.ljust(26),
        "proc_ts": " " * 26, "filler": " " * 20,
    }
    rec = "".join(parts[n][:ln].ljust(ln) for n, ln in DALYTRAN_FIELDS)
    assert len(rec) == 350, len(rec)
    return rec


def xref(card_num, cust_id, acct_id):
    rec = card_num.ljust(16) + str(cust_id).rjust(9, "0") + str(acct_id).rjust(11, "0") + " " * 14
    assert len(rec) == 50
    return rec


# ------------------------------------------------------------------ read drop
def read_fixed(path, length):
    rows = []
    for line in path.read_text().splitlines():
        rows.append(line.ljust(length)[:length])
    return rows


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)

    accounts = read_fixed(SRC / "acctdata.txt", 300)
    xrefs = read_fixed(SRC / "cardxref.txt", 50)
    tcatbals = read_fixed(SRC / "tcatbal.txt", 50)

    acct_by_id = {a[0:11]: a for a in accounts}
    xref_by_card = {x[0:16].strip(): x for x in xrefs}
    tcat_keys = {t[0:17] for t in tcatbals}

    # Pick two real cards whose accounts exist, one with an existing TCATBAL key.
    def acct_of(card):
        return xref_by_card[card][25:36]

    live_cards = [c for c in xref_by_card if acct_of(c) in acct_by_id]
    live_cards.sort()
    card_a, card_b = live_cards[0], live_cards[1]
    acct_a, acct_b = acct_of(card_a), acct_of(card_b)

    # An existing TCATBAL key for acct_a (shipped data uses type 01 / cat 0001).
    existing_key = next(k for k in sorted(tcat_keys) if k.startswith(acct_a))
    exist_type, exist_cat = existing_key[11:13], existing_key[13:17]
    assert acct_a + exist_type + exist_cat in tcat_keys

    a = acct_by_id[acct_a]
    limit_a = unzoned(a[24:36])
    cyc_credit_a = unzoned(a[78:90])
    cyc_debit_a = unzoned(a[90:102])
    expiry_a = a[58:68]
    expiry_b = acct_by_id[acct_b][58:68]

    # headroom = limit - (cycle credit - cycle debit); an amount above it rejects with 102.
    headroom_a = limit_a - (cyc_credit_a - cyc_debit_a)

    # A card that is in XREF but whose account does not exist -> reason 101.
    orphan_card = "MOCKCARD00000001"
    orphan_acct = "99999999999"
    assert orphan_acct not in acct_by_id

    rows = []
    add = rows.append
    # 1 accepted, TCATBAL REWRITE branch, credit branch
    add(dalytran("MOCK000000000001", exist_type, exist_cat, 1000, card_a,
                 "2022-06-01 10:00:00.000000", "accepted - tcatbal update, credit"))
    # 2 accepted, same key again -> proves accumulation across records
    add(dalytran("MOCK000000000002", exist_type, exist_cat, 2500, card_a,
                 "2022-06-01 10:00:01.000000", "accepted - tcatbal accumulate"))
    # 3 accepted, unseen type/cat -> TCATBAL WRITE branch
    add(dalytran("MOCK000000000003", "07", "0009", 1500, card_a,
                 "2022-06-01 10:00:02.000000", "accepted - tcatbal create"))
    # 4 accepted, negative amount -> ACCT-CURR-CYC-DEBIT branch
    add(dalytran("MOCK000000000004", exist_type, exist_cat, -3000, card_a,
                 "2022-06-01 10:00:03.000000", "accepted - negative, debit branch"))
    # 5 reason 100 - card not in XREF
    add(dalytran("MOCK000000000005", "01", "0001", 500, "9999999999999999",
                 "2022-06-01 10:00:04.000000", "reject 100 - card not in xref"))
    # 6 reason 101 - XREF hit, account missing
    add(dalytran("MOCK000000000006", "01", "0001", 500, orphan_card,
                 "2022-06-01 10:00:05.000000", "reject 101 - account missing"))
    # 7 reason 102 - overlimit
    add(dalytran("MOCK000000000007", "01", "0001", headroom_a + 100_00, card_a,
                 "2022-06-01 10:00:06.000000", "reject 102 - overlimit"))
    # 8 reason 103 - dated after expiry
    add(dalytran("MOCK000000000008", "01", "0001", 100, card_b,
                 "2099-01-01 00:00:00.000000", "reject 103 - after expiration"))
    # 9 overlimit AND expired -> 103 wins, it is evaluated second (:417)
    add(dalytran("MOCK000000000009", "01", "0001", 99_999_999_00, card_b,
                 "2099-01-02 00:00:00.000000", "reject 102+103 - 103 must win"))
    # 10 boundary: expiry date exactly equal -> accepted (>= at :414)
    add(dalytran("MOCK000000000010", "01", "0001", 100, card_b,
                 expiry_b + " 00:00:00.00000", "accepted - expiry boundary equal"))
    # 11 boundary: credit limit exactly equal -> accepted (>= at :407).
    #    Records 1-4 already moved this account's cycle figures (a positive
    #    amount lands in ACCT-CURR-CYC-CREDIT :549, a negative one in
    #    ACCT-CURR-CYC-DEBIT :551 -- which therefore goes negative), and the
    #    rejected records 5-7 leave it untouched. The exact boundary amount is
    #    the headroom after those four postings.
    posted = [1000, 2500, 1500, -3000]
    credit_after = cyc_credit_a + sum(x for x in posted if x >= 0)
    debit_after = cyc_debit_a + sum(x for x in posted if x < 0)
    boundary_amt = limit_a - (credit_after - debit_after)
    add(dalytran("MOCK000000000011", "01", "0001", boundary_amt, card_a,
                 "2022-06-01 10:00:07.000000", "accepted - credit limit exact boundary"))

    (OUT / "dalytran.txt").write_text("".join(r + "\n" for r in rows))

    mock_xrefs = xrefs + [xref(orphan_card, 999999999, orphan_acct)]
    (OUT / "cardxref.txt").write_text("".join(r + "\n" for r in mock_xrefs))
    # Accounts and category balances are used unchanged from the shipped drop.
    (OUT / "acctdata.txt").write_text("".join(r + "\n" for r in accounts))
    (OUT / "tcatbal.txt").write_text("".join(r + "\n" for r in tcatbals))

    print(f"cards: accepted={card_a} (acct {acct_a}, expiry {expiry_a}), "
          f"expiry-case={card_b} (acct {acct_b}, expiry {expiry_b})")
    print(f"acct {acct_a}: limit={limit_a/100:.2f} cyc_credit={cyc_credit_a/100:.2f} "
          f"cyc_debit={cyc_debit_a/100:.2f} headroom={headroom_a/100:.2f}")
    print(f"existing TCATBAL key reused: {existing_key}")
    print(f"record 11 exact credit-limit boundary amount: {boundary_amt/100:.2f}")
    print(f"wrote {len(rows)} dalytran, {len(mock_xrefs)} xref, "
          f"{len(accounts)} account, {len(tcatbals)} tcatbal records to {OUT}")


if __name__ == "__main__":
    main()
