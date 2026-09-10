# CardDemo — Java migration

Java port of the COBOL/CICS/VSAM/JCL CardDemo application. The COBOL sources under `app/` stay in
place and remain the reference for parity testing.

## Layout

```
java/carddemo
  src/main/java/com/carddemo/model          Copybook record layouts (Phase 1)
  src/main/java/com/carddemo/model/codec    Fixed-width record codec (Phase 1)
  src/main/java/com/carddemo/context        CARDDEMO-COMMAREA session context (Phase 1)
```

`repository`, `batch` and `web` packages follow in later phases.

## Phases

| Phase | Scope | Status |
| :---- | :---- | :----- |
| 1 | Copybooks (`app/cpy`) → model classes + fixed-width codec | done |
| 2 | VSAM/sequential files → relational schema, repositories, seed from `app/data` | pending |
| 3 | Batch programs (`app/cbl/CB*`) → batch jobs | pending |
| 4 | Online programs (`app/cbl/CO*`) + BMS maps (`app/bms`) → controllers and views | pending |
| 5 | JCL (`app/jcl`) → job orchestration | pending |
| 6 | Parity verification against the COBOL behaviour | pending |

## Build

```
cd java/carddemo
mvn test
```

## Record layout conventions

Each record class is annotated with `@CobolRecord(copybook, length)` and each field with
`@CobolField(name, offset, length, type, scale)`, so the COBOL data name and PIC clause stay
visible in Java:

| PIC clause | Java type | Notes |
| :--------- | :-------- | :---- |
| `X(n)` | `String` | trimmed of trailing spaces on read, space padded on write |
| `9(n)` | `Long` / `Integer` | zero filled, blank fields decode to `null` |
| `S9(n)V9(m)` | `BigDecimal` | zoned decimal with a trailing overpunched sign (`{`=+0 … `R`=-9) |

`FILLER` areas are modelled explicitly so a decode/encode round trip reproduces the original record
image byte for byte, which `SampleDataRoundTripTest` verifies against every file in
`app/data/ASCII`.

## Known data quirks

* `app/data/ASCII/acctdata.txt` carries the account group id (`A000000000`) in the
  `ACCT-ADDR-ZIP` positions; `ACCT-GROUP-ID` is blank in the sample data.
* `app/data/ASCII/cardxref.txt` records are 36 characters long, short of the 50 character LRECL of
  `CVACT03Y`; the missing positions are the trailing `FILLER`.
