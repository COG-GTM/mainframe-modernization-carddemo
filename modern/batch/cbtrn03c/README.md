# cbtrn03c — transaction detail report

Java port of `app/cbl/CBTRN03C.cbl`: `com.carddemo.batch.cbtrn03c.Cbtrn03cJob`. The COBOL quirks it
keeps on purpose are listed in the class Javadoc.

```bash
mvn -pl cbtrn03c exec:java -Dexec.args="<tranfile> <dateparm>"
```

- `tranfile`: 350-byte `TRAN-RECORD`s (CBTRN02C output, sorted by card number). Default `<inputDir>/tranfile.txt`. Line sequential by default; pass `FIXED` as a third arg for raw undelimited RECFM=F data.
- `dateparm`: one 80-byte line, `YYYY-MM-DD YYYY-MM-DD` (start, filler, end). Default `<inputDir>/dateparm.txt`.
- CARDXREF/TRANTYPE/TRANCATG come from the input directory. The report goes to `<outputDir>/tranrept.txt`.

## Golden files

`src/test/resources/golden/*-tranrept.txt` were produced by the original COBOL under GnuCOBOL:

```bash
cobol-crosscheck/crosscheck.sh src/test/resources/fixtures/multipage src/test/resources/fixtures out.txt
```

The script compiles `CBTRN03C.cbl` with `cobc -std=ibm -fsign=EBCDIC`, loads the lookup fixtures into
indexed files (`LOADIDX.cbl`), runs the program and writes the report as line-sequential records.
