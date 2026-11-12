# Batch program tests

Runtime tests for the CardDemo batch COBOL programs in `app/cbl`, built and run
locally with [GnuCOBOL](https://gnucobol.sourceforge.io/) 3.x. Each test
creates its own datasets (VSAM KSDS stand-ins as GnuCOBOL indexed files,
RECFM=F sequential files), runs the program the way its JCL step would, and
checks the files and messages it produces.

| Program    | What is covered |
|------------|-----------------|
| `CBACT01C`, `CBACT02C`, `CBACT03C`, `CBCUS01C` | dumping every sample record in key order, field formatting, empty and missing datasets |
| `CBTRN01C` | card/account lookups for each daily transaction (found, unknown card, missing account) |
| `CBTRN02C` | posting to TRANSACT / TCATBAL / ACCOUNT, reject reasons 100-103, inclusive limit and expiry boundaries, return code 4, the full sample daily file |
| `CBTRN03C` | report headers, detail lines, account / page / grand totals, page breaks, date filtering, lookup abends |
| `CBACT04C` | interest transactions per category balance, default disclosure group, account rewrite, lookup abends, the sample files |
| `CBSTM03B` | open / read / keyed read / close for each DD, EOF and not-found status codes |
| `CSUTLDTC` | mapping of every CEEDAYS feedback code to a message and return code |
| all batch programs | compile cleanly (`CBSTM03A` is an expected failure: TABs in `CUSTREC.cpy`) |

Some tests pin existing behaviour that looks unintended (for example
`CBTRN03C` adding the last amount twice and `CBACT04C` not rewriting the last
account). Those tests say so in a comment; change them together with the
program if the behaviour is fixed.

## Running

```sh
sudo apt-get install gnucobol3   # Ubuntu/Debian
pip install pytest
cd tests/batch
python -m pytest                 # add --cobol-cov-fail-under=70 to enforce coverage
```

Programs are compiled with `-std=ibm -fsign=EBCDIC -I app/cpy`. Mainframe
services are replaced by small COBOL test doubles in `support/`:
`CEE3ABD` (ends the run with return code 99) and `CEEDAYS` (returns the
feedback code named by `CEEDAYS_RESULT`). `RUNPGM` passes a JCL-style `PARM`,
and `CSUDRV` / `M03BDRV` call the subprograms under test.

## Coverage

Programs under test are compiled with `-ftraceall`. After the run, pytest
prints statement and paragraph coverage per program, computed from the
GnuCOBOL trace files, and writes the details (including missed source lines)
to `.build/coverage.json`.
