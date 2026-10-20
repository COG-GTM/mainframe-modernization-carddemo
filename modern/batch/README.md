# CardDemo batch — Java 17

Java 17 / Maven migration of the CardDemo nightly batch slice:
`CBTRN02C` (POSTTRAN) → `CBACT04C` (INTCALC) → `CBTRN03C` (TRANREPT).

| Module | Source | Contents |
|---|---|---|
| `batch-core` | shared | copybook records, fixed-length file I/O, VSAM emulation, COBOL numeric helpers |
| `cbtrn02c` | `app/cbl/CBTRN02C.cbl` | daily transaction posting |
| `cbact04c` | `app/cbl/CBACT04C.cbl` | interest calculator |
| `cbtrn03c` | `app/cbl/CBTRN03C.cbl` | transaction detail report |

Program modules depend only on `batch-core`.

## Build

```bash
cd modern/batch
JAVA_HOME=/path/to/jdk-17 mvn test
```

Run a job (after `mvn install`): `mvn -pl <module> exec:java -Dexec.args="..."`, or run its `main` class.

## batch-core conventions

- **Records** (`com.carddemo.batch.core.record`, `...report`): Java records that follow copybook field
  order and byte offsets. Each has `byte[] toBytes()` and `static fromBytes(byte[])`. Alphanumeric
  components always hold the exact field width (space padded); numeric components are stored the
  way a COBOL MOVE would store them (decimals and high-order digits truncated).
- **Numbers** (`CobolDecimal`): `PIC S9(n)V99` fields use the trailing overpunch sign found in
  `app/data/ASCII` (`{`/`A`–`I` = +0..9, `}`/`J`–`R` = −0..9). Edited pictures such as
  `-ZZZ,ZZZ,ZZZ.ZZ` / `+ZZZ,ZZZ,ZZZ.ZZ` are handled by `formatEdited`. `db2Timestamp(Clock)` ports
  `Z-GET-DB2-FORMAT-TIMESTAMP` (`YYYY-MM-DD-HH.MM.SS.hh0000`).
- **Files** (`com.carddemo.batch.core.io`): `SequentialFileReader`/`SequentialFileWriter` read and write
  line-sequential fixed-length records (short lines are space padded, e.g. `cardxref.txt`).
  `IndexedFileStore<K,V>` emulates a VSAM KSDS (random/sequential read, write, rewrite, delete,
  start, alternate-key read) and returns COBOL file status codes (`00`, `10` EOF, `22`, `23` not found).
- **Directories** (`BatchConfig`): inputs come from `-Dcarddemo.inputDir` / `CARDDEMO_INPUT_DIR`,
  defaulting to the nearest `app/data/ASCII`. Outputs go to `-Dcarddemo.outputDir` /
  `CARDDEMO_OUTPUT_DIR`, defaulting to `target/batch-output`.
