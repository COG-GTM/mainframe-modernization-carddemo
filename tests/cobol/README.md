# COBOL program tests

Runtime tests for the CardDemo batch and CICS online COBOL programs in `app/cbl`, built and run
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
cd tests/cobol
python -m pytest                 # add --cobol-cov-fail-under=70 to enforce coverage
```

Programs are compiled with `-std=ibm -fsign=EBCDIC -I app/cpy`. Mainframe
services are replaced by small COBOL test doubles in `support/`:
`CEE3ABD` (ends the run with return code 99) and `CEEDAYS` (returns the
feedback code named by `CEEDAYS_RESULT`). `RUNPGM` passes a JCL-style `PARM`,
and `CSUDRV` / `M03BDRV` call the subprograms under test.

## Online (CICS) programs

The 17 online programs run under a small CICS test double, not a real CICS
region. `harness/cics.py` translates each `EXEC CICS ... END-EXEC` block into
`CALL 'CICSSIM'` and passes `DFHEIBLK` / `DFHCOMMAREA` through the procedure
division; `support/cics/cicssim.c` implements the calls and `cicsrun` starts a
single pseudo-conversational task. `support/cics/DFHAID.cpy`, `DFHBMSCA.cpy`
and `DFHEIBLK.cpy` stand in for the IBM copybooks.

- **Files:** VSAM datasets (and alternate-index paths such as `CXACAIX`) are
  fixed-length records kept sorted by key: READ (incl. UPDATE), WRITE,
  REWRITE, DELETE, STARTBR / READNEXT / READPREV / ENDBR.
- **Screens:** symbolic maps are parsed from `app/cpy-bms`; tests type field
  values into a map (`cics.send(...)`) and assert on the fields of every map
  the program sends back. SEND TEXT and WRITEQ TD payloads are captured too.
- **Control:** XCTL target, RETURN TRANSID and the returned COMMAREA are
  recorded, so multi-step flows chain one task's COMMAREA into the next.
- **Errors:** a test can force any response code for a command on a dataset
  (for example `faults=["REWRITE ACCTDAT 17"]`) to reach error branches.

Building the online programs needs a C compiler and the libcob headers
(`libcob4-dev` on Ubuntu).

| Program | What is covered |
|---------|-----------------|
| `COSGN00C` | sign-on, admin/user routing, credential errors |
| `COMEN01C`, `COADM01C` | every menu option, invalid options, PF3 |
| `COUSR00C`-`COUSR03C` | user list paging/selection, add, update, delete, validation, file errors |
| `COTRN00C`-`COTRN02C` | transaction list paging, view, add (validation, PF5 copy, ID allocation) |
| `COBIL00C` | bill payment: confirmation, posting, balance update, file errors |
| `CORPT00C` | monthly/yearly/custom report JCL written to the `JOBS` queue, date validation |
| `COACTVWC`, `COACTUPC` | account view; account/customer update with field edits, PF5 commit, locking |
| `COCRDLIC`, `COCRDSLC`, `COCRDUPC` | card list filters/paging/selection, card view, card update |

## Coverage

Programs under test are compiled with `-ftraceall`. After the run, pytest
prints statement and paragraph coverage per program, computed from the
GnuCOBOL trace files, and writes the details (including missed source lines)
to `.build/coverage.json`.
