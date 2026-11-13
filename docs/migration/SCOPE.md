# CardDemo migration pilot: scope and success criteria

| | |
|---|---|
| Plan phase / step | Discover and baseline the legacy app, step `s1.1` |
| Decision | `d-scope`: **Whole app (online + batch)** |
| Source of truth | `app/` in this repository (legacy COBOL, BMS, copybooks, JCL, CSD) |
| Target | Java 17 + Spring Boot 3 + Spring Batch, Maven, PostgreSQL 16, under `modern/` |
| Result type | **Deployable** to the test AWS account (container service + managed Postgres); no production cutover |

How to read this document: statements about legacy behaviour cite `file:line` (and the COBOL paragraph where one applies). Anything not read directly from the source is marked **Hypothesis**. Effort figures are **estimates** and are marked as such.

---

## 1. Legacy platform facts

| Fact | Evidence |
|---|---|
| Source is already in Git | This repository; `app/` holds every artefact listed below. |
| Online runtime is IBM CICS | 17 programs issue `EXEC CICS` (counts per program in §2.1); resources in `app/csd/CARDDEMO.CSD` (transactions lines 306-488, programs 173-305, files 1-99, mapsets 100-172). `app/jcl/CBADMCDJ.jcl` loads them with `DFHCSDUP` from `OEM.CICSTS.V05R06M0` (CICS TS 5.6). |
| Data is VSAM | 8 CICS files are VSAM KSDS / AIX paths (`CARDDEMO.CSD:1-99`: ACCTDAT, CARDDAT, CUSTDAT, CCXREF, CXACAIX, CARDAIX, TRANSACT, USRSEC); clusters are built with `IDCAMS DEFINE CLUSTER` (e.g. `app/jcl/ACCTFILE.jcl`, `app/jcl/CREASTMT.JCL:29`). Batch also uses sequential files and GDGs (`app/jcl/DEFGDGB.jcl`, `app/jcl/INTCALC.jcl:41` `SYSTRAN(+1)`). |
| Language Environment | 9 batch programs abend through `CALL 'CEE3ABD'` in `9999-ABEND-PROGRAM` (e.g. `CBACT01C.cbl:173`, `CBTRN02C.cbl:711`, `CBSTM03A.CBL:923`). `CSUTLDTC.cbl:116` (`A000-MAIN`) calls the LE date service `CEEDAYS`. |
| No DB2, IMS, MQ | `grep -rniE "EXEC SQL|EXEC DLI|CBLTDLI|DFSRRC00|DSNTIAR|IKJEFT|MQPUT|MQGET|MQOPEN|MQCONN" app` returns no hits. |
| No third-party products | No CA Gen, CA-7, Control-M or TWS artefacts (same grep, `CA-?7|CONTROL-?M|TWS|CA ?GEN`, no hits). |
| Scheduling is plain JCL | 29 jobs in `app/jcl/`, 2 procs in `app/proc/`; run order is a manual list in `README.md` ("Running full batch", lines 152-174). No scheduler definitions exist. |
| No program-to-program LINK/START | Online navigation is `EXEC CICS XCTL` only (no `LINK`/`START` hits); state travels in the COMMAREA (`app/cpy/COCOM01Y.cpy`). |

Size (`wc -l`): COBOL programs 19,254 lines; copybooks 2,614; BMS maps 4,472 (plus 5,632 lines of generated symbolic maps in `app/cpy-bms`); JCL 1,894; procs 114.

---

## 2. In scope

### 2.1 Online: 17 CICS programs (14,693 COBOL lines)

Transaction-to-program mapping is from `CARDDEMO.CSD` (lines 306-488); files are the dataset names each program references; PF keys are the AID values the program tests (ENTER is handled by all).

| Tran | Program | Map | Lines | `EXEC CICS` | Files | PF keys | Function (README inventory) |
|---|---|---|---:|---:|---|---|---|
| CC00 | COSGN00C | COSGN00 | 260 | 10 | USRSEC | PF3 | Sign-on; routes admin users to COADM01C, others to COMEN01C (`COSGN00C.cbl:227-237`) |
| CM00 | COMEN01C | COMEN01 | 282 | 5 | USRSEC | PF3 | User main menu; 10 options from `COMEN02Y.cpy:28-84` |
| CAVW | COACTVWC | COACTVW | 941 | 15 | ACCTDAT, CUSTDAT, CARDDAT, CXACAIX, CARDAIX | PF3 | Account view |
| CAUP | COACTUPC | COACTUP | 4,236 | 17 | ACCTDAT, CUSTDAT, CARDDAT, CXACAIX, CARDAIX | PF3, PF5, PF12 | Account + customer update; `SYNCPOINT` in `0000-MAIN` (line 953) and `SYNCPOINT ROLLBACK` in `9600-WRITE-PROCESSING` (line 4100) |
| CCLI | COCRDLIC | COCRDLI | 1,459 | 18 | CARDDAT, CARDAIX | PF3, PF7, PF8 | Credit card list (paged browse) |
| CCDL | COCRDSLC | COCRDSL | 887 | 14 | CARDDAT, CARDAIX | PF3 | Credit card view |
| CCUP | COCRDUPC | COCRDUP | 1,560 | 12 | CARDDAT, CARDAIX | PF3, PF5, PF12 | Credit card update; `SYNCPOINT` in `0000-MAIN` (line 470) |
| CT00 | COTRN00C | COTRN00 | 699 | 10 | TRANSACT | PF3, PF7, PF8 | Transaction list (paged browse) |
| CT01 | COTRN01C | COTRN01 | 330 | 5 | TRANSACT | PF3, PF4, PF5 | Transaction view |
| CT02 | COTRN02C | COTRN02 | 783 | 11 | TRANSACT, CCXREF, CXACAIX, ACCTDAT | PF3, PF4, PF5 | Transaction add; validates dates via `CALL 'CSUTLDTC'` in `VALIDATE-INPUT-DATA-FIELDS` (lines 393, 413) |
| CR00 | CORPT00C | CORPT00 | 649 | 7 | TRANSACT | PF3 | Transaction report request; builds JCL in working storage (`//TRNRPT00 JOB`, `EXEC PROC=TRANREPT`, lines 84-94) and writes it to TD queue `JOBS` in `WIRTE-JOBSUB-TDQ` (lines 515-521), i.e. submits the batch report |
| CB00 | COBIL00C | COBIL00 | 572 | 13 | ACCTDAT, CXACAIX, TRANSACT | PF3, PF4 | Bill payment |
| CA00 | COADM01C | COADM01 | 268 | 5 | USRSEC | PF3 | Admin menu; 4 options from `COADM02Y.cpy:27-42` |
| CU00 | COUSR00C | COUSR00 | 695 | 11 | USRSEC | PF3, PF7, PF8 | List users |
| CU01 | COUSR01C | COUSR01 | 299 | 5 | USRSEC | PF3, PF4 | Add user |
| CU02 | COUSR02C | COUSR02 | 414 | 6 | USRSEC | PF3, PF4, PF5, PF12 | Update user |
| CU03 | COUSR03C | COUSR03 | 359 | 6 | USRSEC | PF3, PF4, PF5, PF12 | Delete user |

Each screen is migrated as a web form mirroring its BMS map: same fields, same messages, same PF-key actions (approved plan decision). CICS commands in use across the 17 programs (all must have a Spring equivalent): `SEND`/`RECEIVE MAP`, `RETURN TRANSID COMMAREA`, `XCTL`, `READ`/`WRITE`/`REWRITE`/`DELETE` (incl. `UPDATE` locking), `STARTBR`/`READNEXT`/`READPREV`/`ENDBR`, `SYNCPOINT [ROLLBACK]`, `WRITEQ TD`, `ASKTIME`/`FORMATTIME`, `ASSIGN`, `HANDLE ABEND`/`ABEND`.

### 2.2 Batch: 10 programs (4,404 COBOL lines) + utility CSUTLDTC (157 lines)

| Program | Lines | Run by (JCL) | Files (`SELECT ... ASSIGN TO`) | Function |
|---|---:|---|---|---|
| CBTRN02C | 731 | `POSTTRAN.jcl:23` | DALYTRAN, TRANFILE, XREFFILE, DALYREJS, ACCTFILE, TCATBALF | Daily transaction posting (core job) |
| CBACT04C | 652 | `INTCALC.jcl:22`, `PARM='2022071800'` | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT (`SYSTRAN(+1)`) | Interest calculation; PARM read via `PROCEDURE DIVISION USING EXTERNAL-PARMS` (`CBACT04C.cbl:176-180`) |
| CBSTM03A | 924 | `CREASTMT.JCL:79` | STMTFILE, HTMLFILE | Account statements (text + HTML); does its file I/O through CBSTM03B (13 calls, first at line 351 in `1000-XREFFILE-GET-NEXT`) |
| CBSTM03B | 230 | called by CBSTM03A | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | File-access subroutine for statements |
| CBTRN03C | 649 | `TRANREPT.jcl:59`, `TRANREPT.prc:57` (also submitted online by CORPT00C) | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, TRANREPT, DATEPARM | Transaction detail report |
| CBTRN01C | 491 | **no JCL in repo** | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE | Daily transaction validation/lookup with DISPLAY output (header: "Post the records from daily transaction file", line 5) |
| CBACT01C | 193 | `READACCT.jcl:22` | ACCTFILE | Read/print account file |
| CBACT02C | 178 | `READCARD.jcl:22` | CARDFILE | Read/print card file |
| CBACT03C | 178 | `READXREF.jcl:22` | XREFFILE | Read/print cross-reference file |
| CBCUS01C | 178 | `READCUST.jcl:6` | CUSTFILE | Read/print customer file |
| CSUTLDTC | 157 | called by COTRN02C (lines 393, 413) and CORPT00C (lines 392, 412, `PROCESS-ENTER-KEY`) | none | Date validation wrapper around LE `CEEDAYS` |

**Hypothesis:** CBTRN01C is an earlier or diagnostic variant of posting (it reads the same inputs as CBTRN02C but only DISPLAYs results). It stays in scope because the approved plan counts it; it is validated by running it directly under the harness rather than through a job.

### 2.3 Batch job flows (behaviour in scope, not the utilities themselves)

The JCL jobs that wrap utility programs (`IDCAMS`, `SORT`, `IEBGENER`, `IEFBR14`) are migrated as Spring Batch steps or data-load tasks with the same functional effect:

| Job(s) | Effect to reproduce |
|---|---|
| ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, DISCGRP, TCATBALF, TRANTYPE, TRANCATG, TRANFILE, DUSRSECJ, DEFCUST | Create schema and load reference/master data (from `app/data/ASCII/*.txt`; USRSEC rows are inline in `DUSRSECJ.jcl` STEP01 `IEBGENER`) |
| TRANIDX, XREFFILE STEP20-30, CARDFILE STEP40-60 | Alternate indexes → PostgreSQL secondary indexes |
| TRANBKP, DEFGDGB | Transaction backup and GDG generations → dated/versioned backup tables or files |
| COMBTRAN | SORT/merge `TRANSACT.BKUP(0)` + `SYSTRAN(0)` and reload TRANSACT (`COMBTRAN.jcl:22-46`) |
| CREASTMT | SORT transactions, build TRXFL KSDS, run CBSTM03A (`CREASTMT.JCL:22-79`) |
| TRANREPT | Back up, filter/sort, run CBTRN03C (`TRANREPT.jcl:23-59`) |
| PRTCATBL | Back up TCATBALF and SORT it into a printable report (`PRTCATBL.jcl`) |
| DALYREJS, REPTFILE | Define reject/report datasets |

The end-to-end run order to reproduce is the README "Running full batch" list (`README.md:154-174`).

### 2.4 Data stores to migrate to PostgreSQL 16

Record layouts come from the copybooks: account `CVACT01Y`, card `CVACT02Y`, xref `CVACT03Y`, customer `CVCUS01Y`/`CUSTREC`, transaction `CVTRA05Y`, daily transaction `CVTRA06Y`, category balance `CVTRA01Y`, disclosure group `CVTRA02Y`, transaction type `CVTRA03Y`, category `CVTRA04Y`, user security `CSUSR01Y`. Sample data: `app/data/ASCII/` (acctdata 50, carddata 50, cardxref 50, custdata 50, dailytran 300, discgrp 51, tcatbal 50, trancatg 18, trantype 7 lines) and the EBCDIC originals in `app/data/EBCDIC/`.

---

## 3. Out of scope

| Item | Why |
|---|---|
| Program `COCRDSEC` / transaction `CDV1` | Defined in `CARDDEMO.CSD:211` ("CREDIT CARD SEARCH") and `:388` ("DEVELOPER TRANSACTION - 1"), but there is no source in `app/cbl`. Nothing to migrate. |
| `CLOSEFIL`, `OPENFIL` and the SDSF steps in CARDFILE/CUSTFILE/TRANFILE | They close/open VSAM files in the CICS region (`SDSF` console commands). There is no CICS region in the target. |
| `CBADMCDJ.jcl` / `CARDDEMO.CSD` as artefacts | Replaced by Spring configuration (routes, datasource). Used only as reference for the transaction and file inventory. |
| `samples/` (build JCL/procs, AWS M2 and UniKix runtime zips) | Mainframe/rehost build artefacts; replaced by the Maven build. |
| EBCDIC packed/zoned byte-level compatibility in the new store | The target stores natural types; EBCDIC files are read only by the one-time loader and the golden harness. |
| Production cutover, live data migration, mainframe co-existence or sync | CardDemo is a sample application with no production users or data; the pilot ends at a running deployment in the test AWS account. |
| 3270 terminal emulation | The approved UI is web forms mirroring each BMS screen, not a terminal emulator. |
| Security hardening beyond the legacy model | USRSEC user/password/type behaviour is reproduced (`COSGN00C`, `COUSR0xC`); SSO/IAM integration is out of scope for the pilot. |

Earlier experimental PRs in this repository (for example #191-#211, all open and unmerged at the time of writing) are not inputs to this plan.

---

## 4. Validation approach (agreed for phase 3)

### 4.1 Batch: golden input/output sets under GnuCOBOL

1. Build the legacy batch programs with GnuCOBOL (`cobc` 3.1.2.0 is installed on the Devin VM) and run each job flow on golden input sets: the README data set (`app/data/ASCII`) plus targeted edge cases (reject paths in CBTRN02C, interest boundaries in CBACT04C, invalid dates for CSUTLDTC).
2. Capture every output: updated masters (ACCTFILE, TCATBALF, TRANSACT), DALYREJS, SYSTRAN, the TRANREPT report, STATEMNT.PS/.HTML, SYSOUT DISPLAY lines and return codes.
3. Run the Java/Spring Batch job on the same inputs and compare with a field-level comparator driven by the copybook layouts. Any normalisation (encoding, run timestamps) is listed explicitly in the comparator config.

Compile check done for this ticket (`cobc -c -I app/cpy`, no harness changes):

| Program | Result |
|---|---|
| CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03B, CSUTLDTC | compile clean |
| CBSTM03A | fails: 39 errors starting at `app/cpy/CUSTREC.cpy:6` ("unbalanced parentheses"). Lines 6+ of that copybook are indented with TAB characters, which break fixed-format column rules. The harness must expand tabs in a build-time copy; the source file itself is left untouched. |

Known harness gaps (to be closed in the harness step, not here):
- `CEE3ABD` and `CEEDAYS` are LE services. **Hypothesis:** GnuCOBOL does not provide them, so the harness needs stubs: CEE3ABD → terminate with a non-zero return code; CEEDAYS → a real Lilian-date implementation, otherwise CSUTLDTC (and the online date checks that call it) cannot produce trustworthy golden data.
- VSAM KSDS/AIX → GnuCOBOL indexed files. **Hypothesis:** alternate-index paths (`CXACAIX`, `CARDAIX`, `XREFFIL1` in `INTCALC.jcl:31`) need per-job emulation.
- SORT/IDCAMS/IEBGENER steps are reproduced by scripts in the harness.

### 4.2 Online: Playwright UI scenarios per screen

For each of the 17 screens, a Playwright suite drives the migrated web form:
- happy path for every function key the program handles (table in §2.1);
- every user-facing message literal reachable from the program's validation paragraphs, asserted verbatim;
- navigation: COMMAREA-driven flows (sign-on → user or admin menu, menu option → program → PF3 back), as in `COSGN00C.cbl:227-237`, `COMEN02Y.cpy`, `COADM02Y.cpy`;
- data effects checked in PostgreSQL after updates (COACTUPC, COCRDUPC, COTRN02C, COBIL00C, COUSR01C-03C).

There is no CICS region to record golden screens from, so the expected values come from the COBOL source (map fields from `app/bms`, message literals from `app/cbl`), with citations in each scenario.

---

## 5. Success criteria

The pilot succeeds when all of the following hold:

1. **Coverage.** Every in-scope program (17 online, 10 batch, CSUTLDTC) has a Java counterpart under `modern/`, with a traceability map from COBOL paragraph to Java method.
2. **Batch parity.** For every golden input set, the Spring Batch output matches the GnuCOBOL output on every compared field (100%), apart from the normalisations listed in the comparator config.
3. **Online parity.** The Playwright suite covers all 17 screens (fields, messages, PF keys, navigation) and passes 100%.
4. **Data.** All sample records load into PostgreSQL 16; row counts and per-table checksums match the source files.
5. **Deployment.** The application and batch jobs run in the test AWS account (container service + managed PostgreSQL). The full batch sequence can be run on demand, and the Playwright suite passes against the deployed URL.
6. **Discrepancies.** No open parity defects. Every accepted deviation from legacy behaviour is written down with its reason.

---

## 6. Deployment intent

**Deployable, not a showcase-only port.** The migrated code is built, tested and deployed to the test AWS account and must run there unattended. It is not promoted to production, because CardDemo has no production users. Production cutover would need its own decision on IAM/SSO, data retention and operations, all outside this pilot.

---

## 7. Effort and value (estimates)

All figures below are **estimates** made before any migration work and should be recalibrated after the first wave of tickets. Manual figures assume a team experienced in both COBOL and Spring, working at roughly 100-150 COBOL lines per person-day, including analysis and tests.

| Work package | Size | Manual (person-days) | With Devin |
|---|---|---:|---|
| Discovery, baseline, GnuCOBOL golden harness + comparator | 11 batch/utility programs, 29 jobs | 15-20 | 3-4 sessions |
| Data layer: schema from 11 record copybooks, loaders, indexes | 8 VSAM files + batch files | 10-15 | 2-3 sessions |
| Batch programs + job flows | 4,404 + 157 lines, 19 job flows | 25-35 | 6-8 sessions |
| Online programs + web forms | 14,693 lines, 17 screens | 60-80 | 10-14 sessions |
| Playwright suites | 17 screens | 10-15 | 2-3 sessions |
| AWS test deployment (container + managed Postgres, IaC) | 1 environment | 5-10 | 1-2 sessions |
| **Total** | ~19.3k COBOL + 4.5k BMS lines | **125-175 (≈6-8 person-months)** | **24-34 sessions + ≈10-15 person-days of human review and decisions** |

Human time with Devin is PR review, answering legacy-behaviour questions and approving deviations. Sessions run in parallel waves, so elapsed time depends on dependency order rather than the session count.

**Value.** The pilot covers every construct this estate uses (CICS pseudo-conversational screens, COMMAREA navigation, VSAM KSDS/AIX browse and update with syncpoint, online-submitted batch through a TD queue, multi-step JCL with GDGs, LE abends and date services). A passing pilot therefore shows the approach and its parity tooling work end to end, and the harness, comparator and Playwright patterns can be reused on larger applications with the same construct mix.
