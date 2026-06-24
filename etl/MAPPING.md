# Data Mapping — Transaction (`CVTRA05Y` / `TRANSACT`)

Produced with the `data-mapper` skill (`.devin/skills/data-mapper/SKILL.md`) and
used as the contract for the `carddemo_etl` pipeline.

- **Copybook:** `app/cpy/CVTRA05Y.cpy`
- **VSAM file:** `TRANSACT` (KSDS)
- **Record length:** 350 bytes (fixed)
- **Primary key:** `TRAN-ID` (`PIC X(16)`)
- **Shared layout:** identical to `CVTRA06Y` (`DALYTRAN`), so the ASCII seed file
  `app/data/ASCII/dailytran.txt` is used as the test fixture.

## Field-by-field mapping

| # | COBOL Field | PIC | Offset | Len | COBOL Type | Modern Type | Nullable | Transformation Notes |
|---|---|---|---|---|---|---|---|---|
| 1 | TRAN-ID | X(16) | 1 | 16 | ALPHANUMERIC | `char(16)` (PK) | NO | EBCDIC→UTF-8 |
| 2 | TRAN-TYPE-CD | X(02) | 17 | 2 | ALPHANUMERIC | `char(2)` | NO | EBCDIC→UTF-8; FK→TRANTYPE |
| 3 | TRAN-CAT-CD | 9(04) | 19 | 4 | NUMERIC | `char(4)` | NO | Code, keep leading zeros; FK→TRANCATG |
| 4 | TRAN-SOURCE | X(10) | 23 | 10 | ALPHANUMERIC | `varchar(10)` | YES | Strip right padding |
| 5 | TRAN-DESC | X(100) | 33 | 100 | ALPHANUMERIC | `varchar(100)` | YES | Strip right padding |
| 6 | TRAN-AMT | S9(09)V99 | 133 | 11 | SIGNED DISPLAY | `numeric(11,2)` | NO | **Overpunch sign** on last byte; implied `V99` |
| 7 | TRAN-MERCHANT-ID | 9(09) | 144 | 9 | NUMERIC | `bigint` | NO | Arithmetic id |
| 8 | TRAN-MERCHANT-NAME | X(50) | 153 | 50 | ALPHANUMERIC | `varchar(50)` | YES | Strip right padding |
| 9 | TRAN-MERCHANT-CITY | X(50) | 203 | 50 | ALPHANUMERIC | `varchar(50)` | YES | Strip right padding |
| 10 | TRAN-MERCHANT-ZIP | X(10) | 253 | 10 | ALPHANUMERIC | `varchar(10)` | YES | Strip right padding |
| 11 | TRAN-CARD-NUM | X(16) | 263 | 16 | ALPHANUMERIC | `char(16)` | NO | **PCI** — mask for non-prod; FK→CARDDAT |
| 12 | TRAN-ORIG-TS | X(26) | 279 | 26 | ALPHANUMERIC | `timestamp` | NO | Parse 26-char timestamp |
| 13 | TRAN-PROC-TS | X(26) | 305 | 26 | ALPHANUMERIC | `timestamp` | YES | Blank/zero placeholder → NULL |
| 14 | FILLER | X(20) | 331 | 20 | ALPHANUMERIC | (dropped) | — | Reserved padding |

## Migration risk flags

| Risk | Field | Reason |
|------|-------|--------|
| HIGH | TRAN-AMT | Zoned-decimal **overpunch sign** on the final byte (`{`/`A`–`I` = +0–9, `}`/`J`–`R` = −0–9). Naive `int()` parsing corrupts both sign and last digit. Decoded by `transforms.parse_signed_decimal`. |
| HIGH | TRAN-CARD-NUM | Plaintext PAN — PCI-DSS. ETL supports `--mask-pan` for non-production sinks. |
| MEDIUM | TRAN-ORIG-TS / TRAN-PROC-TS | Stored as 26-char text; format may vary across records. `TRAN-PROC-TS` is frequently blank for unprocessed records → NULL. |
| MEDIUM | TRAN-AMT byte length | The skill's generic rule assumed a separate trailing sign byte (12 bytes). The actual record uses an **overpunched** 11-byte field (11 + rest = 350). The layout encodes 11 bytes; mis-sizing here shifts every subsequent field. |
| LOW | FILLER (20) | Trailing reserved padding; dropped on load. |
| LOW | TRAN-CAT-CD | Numeric code carrying leading zeros — kept as text, not integer. |
