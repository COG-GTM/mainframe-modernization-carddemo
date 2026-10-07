# CardDemo TypeScript ports

TypeScript replacements for CardDemo COBOL batch programs.

| Path | COBOL program | JCL job |
| --- | --- | --- |
| [`batch/posttran/`](batch/posttran) | `app/cbl/CBTRN02C.cbl` – daily transaction posting | `app/jcl/POSTTRAN.jcl` (STEP15) |

## POSTTRAN / CBTRN02C

Requires Node.js 20 or later.

```bash
cd ts/batch/posttran
npm install
npm run posttran        # STEP15 on app/data/ASCII -> ts/batch/posttran/out/
echo $?                 # RETURN-CODE: 0, or 4 if any transaction was rejected
npm test                # unit + golden-file tests
npm run typecheck
```

On the bundled sample data it processes 300 transactions, rejects 38 (all `0102 OVERLIMIT TRANSACTION`) and exits 4. This matches the original COBOL.

### What it does (strict parity with CBTRN02C)

For each DALYTRAN record:

1. `1500-A-LOOKUP-XREF`: looks up the card number in XREFFILE. If it isn't there, reject **100** `INVALID CARD NUMBER FOUND`.
2. `1500-B-LOOKUP-ACCT`: looks up the xref account ID in ACCTFILE. If it isn't there, reject **101** `ACCOUNT RECORD NOT FOUND`. Otherwise:
   - If `CURR-CYC-CREDIT - CURR-CYC-DEBIT + amount > CREDIT-LIMIT`, reject **102** `OVERLIMIT TRANSACTION`.
   - If `EXPIRATION-DATE < ORIG-TS(1:10)`, reject **103** `TRANSACTION RECEIVED AFTER ACCT EXPIRATION`. When both checks fail, 103 overwrites 102, the same as in the COBOL.
3. A valid record is posted:
   - `2700`: adds the amount to TCATBALF for (account, type, category), creating the record if it doesn't exist.
   - `2800`: adds the amount to `CURR-BAL`, and to `CURR-CYC-CREDIT` (if >= 0) or `CURR-CYC-DEBIT` (if < 0).
   - `2900`: writes TRAN-RECORD to TRANFILE, stamped with `TRAN-PROC-TS`.
4. A rejected record is written to DALYREJS: the 350-byte DALYTRAN record, then a 4-digit reason code and a 76-byte description.

Any I/O error makes the program abend (COBOL `U0999`, CLI exit code 12). CBTRN02C does not read CARDDAT or TRANTYPE and does not use disclosure groups. Their copybooks are still ported (`CVACT02Y`, `CVTRA02Y`/`03Y`/`04Y`) so that other batch programs can reuse them.

### DD names → files

| DD | Default input | Default output |
| --- | --- | --- |
| `DALYTRAN` | `app/data/ASCII/dailytran.txt` | – |
| `XREFFILE` | `app/data/ASCII/cardxref.txt` | – (read-only) |
| `ACCTFILE` | `app/data/ASCII/acctdata.txt` | `out/acctdata.txt` |
| `TCATBALF` | `app/data/ASCII/tcatbal.txt` | `out/tcatbal.txt` |
| `TRANFILE` | `app/data/ASCII/transact.txt` (starts empty if missing) | `out/transact.txt` |
| `DALYREJS` | – | `out/dalyrejs.txt` |

Overrides:

```bash
npm run posttran -- --data-dir ../../../samples/data --out-dir /tmp/run1
npm run posttran -- --dd DALYTRAN=/data/today.txt --dd DALYREJS=/tmp/rejects.json
DD_ACCTFILE=/data/acct.txt DD_ACCTFILE_OUT=/data/acct.new.txt npm run posttran
npm run posttran -- --in-place      # rewrite the KSDS files in place, like VSAM
```

A path that ends in `.json` is read and written as a JSON array of records. Monetary fields are stored as decimal strings. Any other path is read and written as fixed-width, newline-terminated ASCII records in the copybook layout, with zoned-decimal sign overpunch (`{`/`A`–`I` for positive, `}`/`J`–`R` for negative).

### Layout

```
src/
  posttran.ts          entry point: DD binding, RC handling (JCL STEP15)
  cbtrn02c/program.ts  Cbtrn02c class, one method per COBOL paragraph
  cbtrn02c/records.ts  reject record layout, reason codes
  copybooks/           CVTRA06Y CVTRA05Y CVACT01Y-03Y CVTRA01Y-04Y COCOM01Y
                       numeric.ts: decimal.js codecs for zoned and COMP-3, PIC truncation
  dal/                 KeyedFile / SequentialInput / SequentialOutput interfaces,
                       with in-memory and flat-file (fixed-width/JSON) implementations
test/                  unit, codec, DAL and golden-file tests
test/golden/           output of the original COBOL run under GnuCOBOL
tools/gnucobol/        harness that regenerates test/golden/
```

**Numbers.** Every `PIC S9(n)V99` field is a `decimal.js` `Decimal`, so JavaScript floats are never used. Results are truncated to the field's PIC the same way a COBOL `MOVE`/`ADD` without `ROUNDED`/`ON SIZE ERROR` truncates. `encodePacked`/`decodePacked` handle COMP-3 for EBCDIC/binary extracts.

**Plugging in a database.** Implement `KeyedFile<T>` (`open`/`read(key)`/`write`/`rewrite`/`close`, returning VSAM file status codes `00`/`22`/`23`/…) for each table. Pass the implementations to `runCbtrn02c({ dalytran, tranfile, xreffile, dalyrejs, acctfile, tcatbalf })`. The business logic does not need to change.

### Golden files

`test/golden/` holds the output of the real `CBTRN02C.cbl`, compiled with GnuCOBOL 3.1 (BDB indexed files stand in for VSAM) and run on `app/data/ASCII`. The golden test compares `dalyrejs`, `acctdata`, `tcatbal`, `transact` and SYSOUT line by line. `TRAN-PROC-TS` is the exception: it is wall-clock time in both implementations, so the test only checks its format. To regenerate the golden files:

```bash
sudo apt-get install -y gnucobol
ts/batch/posttran/tools/gnucobol/run-cobol-posttran.sh
```
