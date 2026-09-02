# CardDemo Java migration — shared foundation

Spring Boot / Spring Batch project that the COBOL programs in `app/cbl` are being migrated into.
This module currently contains the shared data layer only; the online (CICS) and batch (JCL)
programs land in `online/` and `batch/` in follow-up work.

## Layout

| Package | Contents |
| --- | --- |
| `com.carddemo.domain` | One class per copybook record (`CVACT01Y` → `Account`, …) plus `CardDemoContext` (`COCOM01Y` COMMAREA) |
| `com.carddemo.util` | `CobolCodec` (zoned-decimal / overpunch sign codec, fixed-width helpers) and `FieldCursor` |
| `com.carddemo.fixture` | `FixedWidthFixtureLoader`, `CardDemoDataSet`, `MockUserData` |
| `com.carddemo.repository` | Repository interfaces for the six core VSAM files plus the reference tables |
| `com.carddemo.repository.memory` | In-memory implementations seeded from the sample data |
| `com.carddemo.online` | Spring Boot controllers/services for the CICS programs |
| `com.carddemo.batch` | Spring Batch jobs for the batch programs |

## Mock data

The sample fixed-width files in `app/data/ASCII` are exposed on the classpath under
`carddemo-data/` (see the resource mapping in `pom.xml`), so there is no duplicate copy of the data.
`FixedWidthFixtureLoader.loadAll()` parses every file into a `CardDemoDataSet`, which the
`FixtureConfig` bean publishes and every in-memory repository seeds itself from.

| File | Record | LRECL |
| --- | --- | --- |
| `acctdata.txt` | `Account` (CVACT01Y) | 300 |
| `carddata.txt` | `Card` (CVACT02Y) | 150 |
| `cardxref.txt` | `CardXref` (CVACT03Y) | 50 |
| `custdata.txt` | `Customer` (CVCUS01Y) | 500 |
| `dailytran.txt` | `Transaction` (CVTRA05Y / CVTRA06Y) | 350 |
| `discgrp.txt` | `DisclosureGroup` (CVTRA02Y) | 50 |
| `tcatbal.txt` | `TransactionCategoryBalance` (CVTRA01Y) | 50 |
| `trancatg.txt` | `TransactionCategory` (CVTRA04Y) | 60 |
| `trantype.txt` | `TransactionType` (CVTRA03Y) | 60 |
| — | `User` (CSUSR01Y) from `MockUserData` | 80 |

USRSEC has no ASCII file, so `MockUserData` reproduces the in-stream users of
`app/jcl/DUSRSECJ.jcl`, including the README seed logins `ADMIN001/PASSWORD` (admin) and
`USER0001/PASSWORD` (regular).

Tests that mutate data restore the canonical dataset by injecting `List<Reseedable>` and calling
`reseed()`.

## Numeric fields

Signed COBOL amounts (`PIC S9(n)V99`) are stored as zoned decimal with the sign overpunched onto the
last digit: `{ A…I` for positive digits `0`–`9`, `} J…R` for negative. `CobolCodec.decodeSigned`
returns a `BigDecimal` with the exact copybook scale and `CobolCodec.encodeSigned` writes the field
back byte-for-byte; every domain class round-trips `parse(record).format().equals(record)`.

Account, customer and card identifiers stay fixed-width strings (`"00000000001"`), never numbers.
Repository lookups accept either padded or unpadded keys via `RecordKeys`.

Note on the sample account data: `acctdata.txt` carries the group id (`A000000000`) in the
`ACCT-ADDR-ZIP` position and leaves `ACCT-GROUP-ID` blank, matching what the COBOL programs read, so
interest calculation resolves against the `DEFAULT` rows of `discgrp.txt`.

## Build and test

```bash
cd java-app
mvn test          # unit tests for the codec, fixture loader and repositories
mvn spring-boot:run
```
