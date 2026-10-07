# date-validation

TypeScript port of the CardDemo date edit routines, as a pilot migration:

| COBOL | TypeScript |
| --- | --- |
| `app/cpy/CSUTLDWY.cpy` (CCYYMMDD layout, `FLG-*` flags) | `DateValidationResult`, `FieldFlag` |
| `app/cpy/CSUTLDPY.cpy` `EDIT-DATE-CCYYMMDD` | `validateDate(ccyymmdd, { fieldName? })` |
| `app/cpy/CSUTLDPY.cpy` `EDIT-DATE-OF-BIRTH` (as called by `COACTUPC`) | `validateDateOfBirth(ccyymmdd, { fieldName?, today? })` |
| `app/cbl/CSUTLDTC.cbl` (LE `CEEDAYS`) | `checkCalendarDate(ccyymmdd)` – native `Date` round-trip |

```ts
import { validateDate } from '@carddemo/date-validation';

validateDate('20230229');
// { valid: false,
//   flags: { year: 'NOT_OK', month: 'NOT_OK', day: 'NOT_OK' },
//   message: 'Date:Not a leap year.Cannot have 29 days in this month.' }
```

`flags.*` is `VALID` / `NOT_OK` / `BLANK` (`FLG-xxx-ISVALID` / `-NOT-OK` / `-BLANK`).
`message` is the COBOL `STRING` output: trimmed field name (`WS-EDIT-VARIABLE-NAME`,
default `Date` / `Date of Birth`) followed by the literal, verbatim.

## Behaviour carried over from the COBOL

- Input is handled like a `MOVE` to `PIC X(8)`: short values are space-padded, long ones truncated.
- Only centuries 19 and 20 are accepted.
- Year, month and day edits all run; the first error message wins, but flags from later edits are still set.
- The day goes through `NUMVAL`, so `' 5'`, `'5 '`, `'+5'`, `'5-'` all count as day 05.
- Feb 29: century years must divide by 400, others by 4.
- The date-of-birth check only runs on an otherwise valid date and rejects today (the current date must be strictly later).

## Deviations

- `CEEDAYS` is replaced by a `Date` round-trip plus its supported range (15 Oct 1582 – 31 Dec 9999), reporting severity 3 / message 2508 (bad date value), 2513 (unsupported range) or 2520 (non-numeric). It can't fail after the earlier edits pass, as in the COBOL.
- If that check did fail, the COBOL falls through and resets the flags to valid. The port keeps them `NOT_OK` so they agree with `valid`.
- Feb 29 with a non-numeric year skips the leap-year test (the COBOL `DIVIDE` on non-numeric data is undefined).
- A month must be two digits. The COBOL tests the raw bytes as `PIC 9(2)`, which is undefined for non-digits on IBM; GnuCOBOL accepts e.g. `'1-'`.
- Messages are right-trimmed and capped at 75 characters (`WS-RETURN-MSG PIC X(75)`).

## Scripts

```sh
npm install
npm test           # vitest: unit tests + comparison with real COBOL output
npm run typecheck
npm run build      # emits dist/
```

`test/cobol-golden.test.ts` checks every case in `test/golden/cases.txt` against
`test/golden/cobol-results.txt`, which was produced by compiling the real copybooks with GnuCOBOL.
To regenerate it (needs `apt install gnucobol`): `./tools/gnucobol/run-cobol-golden.sh`.
`CEEDAYS` isn't available there, so `tools/gnucobol/CSUTLDTC.cbl` is a stub that always passes.
