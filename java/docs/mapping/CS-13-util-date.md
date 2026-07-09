# CS-13 — Date validation utility (`CSUTLDTC`)

**WAVE 3 (BATCH), scope CS-13 (Utilities / date validation).**

This document maps the COBOL date-validation routine `CSUTLDTC` and its companion copybooks
`CSUTLDPY` (procedure) and `CSUTLDWY` (working storage) to the reusable Java component
`com.carddemo.util.DateValidator` (in `carddemo-app`).

## Source artifacts

| Artifact | Kind | Role |
|----------|------|------|
| [`app/cbl/CSUTLDTC.cbl`](../../../app/cbl/CSUTLDTC.cbl) | COBOL program | Wrapper over the Language Environment callable service `CEEDAYS`: takes a date string + a picture string, validates the date and maps the `CEEDAYS` feedback code to a severity, a message number and a result text. |
| [`app/cpy/CSUTLDPY.cpy`](../../../app/cpy/CSUTLDPY.cpy) | Procedure copybook | Reusable edit paragraphs (`EDIT-DATE-CCYYMMDD`, `EDIT-YEAR-CCYY`, `EDIT-MONTH`, `EDIT-DAY`, `EDIT-DAY-MONTH-YEAR`, `EDIT-DATE-LE`, `EDIT-DATE-OF-BIRTH`). `EDIT-DATE-LE` is the paragraph that actually `CALL 'CSUTLDTC'`s. |
| [`app/cpy/CSUTLDWY.cpy`](../../../app/cpy/CSUTLDWY.cpy) | Working-storage copybook | The work areas the edit paragraphs use (`WS-EDIT-DATE-CCYYMMDD`, the month/day level-88s, the validity flags, and the `WS-DATE-VALIDATION-RESULT` message buffer). |

## What `CSUTLDTC` does

`CSUTLDTC` receives three parameters:

| COBOL parameter | PIC | Java equivalent |
|-----------------|-----|-----------------|
| `LS-DATE` | `X(10)` | `String date` argument of `validate(date, format)` |
| `LS-DATE-FORMAT` | `X(10)` | `String format` argument (the picture, e.g. `YYYYMMDD`) |
| `LS-RESULT` | `X(80)` | `DateValidationResult.formattedMessage()` |

It moves the two strings into `CEEDAYS` variable-length strings, calls `CEEDAYS`, and then:

- `MOVE WS-SEVERITY-N TO RETURN-CODE` → `DateValidationResult.severity()`
- `MOVE MSG-NO OF FEEDBACK-CODE TO WS-MSG-NO-N` → `DateValidationResult.messageNumber()`
- an `EVALUATE` over the feedback-code level-88s selects the 15-char `WS-RESULT` text →
  `DateValidationResult.resultText()` / `DateValidationStatus`.

Because there is no mainframe runtime in the Java stack, `DateValidator` reproduces the relevant
`CEEDAYS` semantics directly: it parses the picture, checks each field is numeric, checks the
month is `1–12`, checks the day is valid for the month (Gregorian leap-year rule), and checks the
resulting date is inside the `CEEDAYS`-supported Lilian range. `OUTPUT-LILLIAN` (the Lilian day
number `CEEDAYS` returns) is exposed as `DateValidationResult.lilianDay()`.

## Return-code / feedback-code mapping

The `FEEDBACK-CODE` level-88s in `CSUTLDTC` are 8-byte LE condition tokens laid out as
`severity(2) · message-number(2) · case/severity-control(1, 0x59) · facility-id(3 = "CEE" in
EBCDIC, 0xC3C5C5)`. Each maps to a `DateValidationStatus` constant:

| COBOL 88-level (`FEEDBACK-TOKEN-VALUE`) | Hex token | Severity | Msg # | `WS-RESULT` text | `DateValidationStatus` |
|-----------------------------------------|-----------|:--------:|:-----:|------------------|------------------------|
| `FC-INVALID-DATE`      | `0000000000000000` | 0 | 0    | `Date is valid`   | `VALID` |
| `FC-INSUFFICIENT-DATA` | `000309CB59C3C5C5` | 3 | 2507 | `Insufficient`    | `INSUFFICIENT_DATA` |
| `FC-BAD-DATE-VALUE`    | `000309CC59C3C5C5` | 3 | 2508 | `Datevalue error` | `BAD_DATE_VALUE` |
| `FC-INVALID-ERA`       | `000309CD59C3C5C5` | 3 | 2509 | `Invalid Era`     | `INVALID_ERA` |
| `FC-UNSUPP-RANGE`      | `000309D159C3C5C5` | 3 | 2513 | `Unsupp. Range`   | `UNSUPPORTED_RANGE` |
| `FC-INVALID-MONTH`     | `000309D559C3C5C5` | 3 | 2517 | `Invalid month`   | `INVALID_MONTH` |
| `FC-BAD-PIC-STRING`    | `000309D659C3C5C5` | 3 | 2518 | `Bad Pic String`  | `BAD_PICTURE_STRING` |
| `FC-NON-NUMERIC-DATA`  | `000309D859C3C5C5` | 3 | 2520 | `Nonnumeric data` | `NONNUMERIC_DATA` |
| `FC-YEAR-IN-ERA-ZERO`  | `000309D959C3C5C5` | 3 | 2521 | `YearInEra is 0`  | `YEAR_IN_ERA_ZERO` |
| `WHEN OTHER`           | — | 3 | 0 | `Date is invalid` | `INVALID_DATE` |

Notes:
- `FC-INVALID-DATE` is the (confusingly named) **success** token: an all-zero condition token means
  `CEEDAYS` accepted the date. `severity == 0` ⇒ `DateValidationResult.isValid()`.
- Message numbers are the decimal value of the 2-byte message-number field (e.g. `0x09CC = 2508`);
  they are the `CEExxxx` LE message numbers.
- `INVALID_ERA` / `YEAR_IN_ERA_ZERO` are era-picture conditions. `DateValidator` returns
  `YEAR_IN_ERA_ZERO` for a `0000` year; `INVALID_ERA` is retained for completeness (Gregorian
  pictures never produce it).

## `DateValidator` validation order

The checks are ordered to match `CEEDAYS`' feedback priority:

1. **Picture** — parse `format`. Supported tokens: `YYYY`/`YY` (year), `MM` (month), `DD` (day);
   any non-alphanumeric char is a literal separator. An unrecognised letter run, a `Y` run that is
   not 2 or 4 long, or a picture with no field → `BAD_PICTURE_STRING` (2518).
2. **Insufficient data** — the input is shorter than the picture requires → `INSUFFICIENT_DATA` (2507).
   (`null`/empty input maps here too.)
3. **Nonnumeric data** — a numeric field contains a non-digit (letters, embedded spaces) →
   `NONNUMERIC_DATA` (2520).
4. **Year in era zero** — a `0000` year → `YEAR_IN_ERA_ZERO` (2521).
5. **Invalid month** — month `< 1` or `> 12` → `INVALID_MONTH` (2517).
6. **Bad date value** — day `< 1` or `>` days-in-month, incl. Feb 29 in a non-leap year →
   `BAD_DATE_VALUE` (2508).
7. **Unsupported range** — a real calendar date outside `1582-10-15 … 9999-12-31` (the `CEEDAYS`
   Lilian range) → `UNSUPPORTED_RANGE` (2513).
8. Otherwise `VALID` (0), and `lilianDay()` is populated (`1582-10-15` = Lilian day 1).

Trailing blanks in both the date and the picture are ignored, so the CardDemo convention of
passing 10-byte fields (`"20240229  "` with mask `"YYYYMMDD  "`) validates correctly.

## `CSUTLDWY` field mapping

| COBOL field (`CSUTLDWY`) | Meaning | Java handling |
|--------------------------|---------|---------------|
| `WS-EDIT-DATE-CCYYMMDD` `PIC 9(8)` (`CC`,`YY`,`MM`,`DD` subfields) | the CCYYMMDD date under edit | parsed positionally from the input per the picture tokens |
| `WS-VALID-MONTH` (88, `1 THRU 12`) | month range check | step 5 (`INVALID_MONTH`) |
| `WS-31-DAY-MONTH` / `WS-DAY-31` / `WS-DAY-30` / `WS-DAY-29` (88s) | per-month day limits | `daysInMonth(month, year)` in step 6 |
| `WS-FEBRUARY` (88, value 2) + divide-by-4/400 in `EDIT-DAY-MONTH-YEAR` | leap-year test | `isLeapYear(year)` (÷400 for century years, else ÷4) |
| `WS-DATE-VALIDATION-RESULT` (severity, `Mesg Code:`, msg #, result, `TstDate:`, date, `Mask used:`, mask) | the 80-char result buffer | `DateValidationResult.formattedMessage()` reproduces this layout byte-for-byte (see below) |

### `WS-MESSAGE` / `WS-DATE-VALIDATION-RESULT` 80-char layout

`formattedMessage()` reproduces the `WS-MESSAGE` group (`CSUTLDTC`) / `WS-DATE-VALIDATION-RESULT`
(`CSUTLDWY`):

```
severity(4) | "Mesg Code:"(11) | msgNo(4) | " " | result(15) | " " |
"TstDate:"(9) | date(10) | " " | "Mask used:"(10) | mask(10) | " " | "   "   = 80 chars
```

Example (`validate("20240229","YYYYMMDD")`):
`0000Mesg Code: 0000 Date is valid   TstDate: 20240229   Mask used:YYYYMMDD   `

## `CSUTLDPY` paragraphs — reuse guidance

`CSUTLDPY`'s `EDIT-DATE-LE` paragraph is exactly the "call `CSUTLDTC` and inspect the severity"
pattern that `DateValidator.validate(...)` now provides in Java. The other paragraphs are
caller-side field edits layered *around* the `CSUTLDTC` call:

- `EDIT-YEAR-CCYY` additionally restricts the century to `19`/`20` (years `1900–2099`, the
  `THIS-CENTURY`/`LAST-CENTURY` level-88s) and produces field-specific messages. **This business
  restriction is not part of `CSUTLDTC`/`CEEDAYS` and is intentionally not enforced by
  `DateValidator`.**
- `EDIT-MONTH` / `EDIT-DAY` / `EDIT-DAY-MONTH-YEAR` duplicate, in COBOL, the month/day/leap-year
  rules that `DateValidator` already applies.
- `EDIT-DATE-OF-BIRTH` adds a "not in the future" reasonableness check (out of scope here).

**Reuse note:** later migration waves that currently in-line their own date edits (account, card,
transaction maintenance, etc.) should delegate the calendar validation to
`com.carddemo.util.DateValidator` rather than re-implementing these rules, layering only their own
business restrictions (century window, date-of-birth checks) on top of the `DateValidationResult`.

## Java artifacts added

| File | Purpose |
|------|---------|
| `carddemo-app/.../util/DateValidator.java` | `@Component`; the CEEDAYS-equivalent validator |
| `carddemo-app/.../util/DateValidationStatus.java` | enum of feedback codes → severity / msg # / result text / hex token |
| `carddemo-app/.../util/DateValidationResult.java` | immutable result (status, severity, msg #, result text, Lilian day, 80-char message) |
| `carddemo-app/.../util/package-info.java` | package documentation |
| `carddemo-app/src/test/.../util/DateValidatorTest.java` | unit tests (valid/invalid/leap/boundary/mapping) |
