# CS-15 — Integration & validation (WAVE 4 FINAL)

The final wave does not migrate a new COBOL scope; it **validates the whole migration**
end-to-end and produces the authoritative project [`README`](../../README.md) (overview,
architecture, build/run, and the complete COBOL→Java mapping table). This document summarises the
end-to-end coverage, the numeric validations and their COBOL-derived expected values, and any
integration fixes.

All new tests live under `com.carddemo.e2e`
([`carddemo-app/src/test/java/com/carddemo/e2e`](../../carddemo-app/src/test/java/com/carddemo/e2e))
so they do not clash with the per-module tests. Everything runs under the `test` profile on H2
and is exercised by `mvn -B verify`.

## Integration fixes

**None.** Every COBOL-derived expected value matched the existing Java implementation exactly, so
no production code under `carddemo-app`/`carddemo-domain` was changed. CS-15 adds test and
documentation files only.

One implementation characteristic worth recording (a schema property, not a bug): reject reason
**101 (ACCOUNT NOT FOUND)** cannot arise from a *populated* card/xref whose account row is
missing, because `FK_CARD_ACCOUNT` and the `card_xref` foreign keys enforce referential
integrity that the legacy VSAM files did not. The Java posting processor still assigns 101
defensively when the resolved account id is absent/`null`; the test reproduces exactly that
condition (a nullable account id on the card/xref), matching how the per-module `PostTranJobTest`
already covers it.

## End-to-end coverage

### Online golden path — `OnlineGoldenPathE2ETest`

A single `@SpringBootTest` conversation with the real seed data (`carddemo.seed.enabled=true`)
and a persisted `MockHttpSession`, walking the legacy screen path and asserting HTTP status +
COMMAREA/session behaviour:

| Step | Legacy program(s) | REST call | Key assertions |
|------|-------------------|-----------|----------------|
| Sign-on (admin) | `COSGN00C` | `POST /api/auth/signon` | `ROLE_ADMIN`, destination `ADMIN_MENU` |
| Post-auth routing | `COSGN00C`→`COADM01C` | `POST /api/nav/signon` | COMMAREA `userType=ADMIN`, `toProgram=COADM01C` |
| Admin menu | `COADM01C` | `GET /api/menu/admin` | `programName=COADM01C`, options present |
| Account view | `COACTVWC` | `GET /api/accounts/00000000001` | customer `000000001`, first name `Immanuel` |
| Card list / detail | `COCRDLIC` / `COCRDSLC` | `GET /api/cards?accountId=…`, `GET /api/cards/{num}` | list contains card `9680294154603697` |
| Add / view transaction | `COTRN02C` / `COTRN01C` | `POST /api/transactions`, `GET /api/transactions/{id}` | new 16-digit id generated, resolves back to the card, amount `100.50` |
| Bill payment | `COBIL00C` | `POST /api/billpay` | prompt shows balance `194.00`; confirmed payment → `newBalance=0.00` |
| Report | `CORPT00C` | `POST /api/reports/transactions` | `submitted=true`, `reportName=Custom` |
| User CRUD | `COUSR00C/01C/02C/03C` | `GET/POST/PUT/DELETE /api/admin/users` | list/add(201)/update/delete with COBOL messages |

A second test signs on as a regular user (`USER0001`) and confirms routing to `MAIN_MENU`
(`COMEN01C`) plus **403** on the admin menu and user-admin endpoints. The class is
`@Transactional` so the mutations roll back and leave the shared seed intact for sibling tests.

### Batch golden path — `BatchGoldenPathE2ETest`

Launches the three CS-14 pipelines back-to-back through `BatchJobLauncherService` (the same code
path the REST/CLI/scheduler launchers use) against a deterministic fixture, asserting each
reaches `COMPLETED` and the chained DB state. See the numeric section for the exact values.

## Numeric validations (COBOL-derived expected values)

### Posting — `CBTRN02C` (`BatchGoldenPathE2ETest` + `PostingRejectNumericValidationE2ETest`)

Fixture account starts `ACCT-CURR-BAL = 100.00`, `ACCT-CURR-CYC-CREDIT = 50.00`,
`ACCT-CURR-CYC-DEBIT = 20.00`, credit limit `5000.00`, group `DEFAULT`. Daily file: `+200.00`,
`-30.00` (both valid, card `7777777777777701`), and one unknown card (rejected).

| Quantity | COBOL source | Expected |
|----------|--------------|----------|
| `ACCT-CURR-BAL` | `2800-UPDATE-ACCOUNT-REC`: `ADD DALYTRAN-AMT TO ACCT-CURR-BAL` → 100 + 200 − 30 | **270.00** |
| `ACCT-CURR-CYC-CREDIT` | positive amounts → `ADD … TO ACCT-CURR-CYC-CREDIT` → 50 + 200 | **250.00** |
| `ACCT-CURR-CYC-DEBIT` | negative amount → `ADD … TO ACCT-CURR-CYC-DEBIT` → 20 + (−30) | **−10.00** |
| `TRAN-CAT-BAL` (acct, `01`, `1000`) | `2700-UPDATE-TCATBAL`: `ADD DALYTRAN-AMT TO TRAN-CAT-BAL` → 0 + 200 − 30 | **170.00** |
| Reject count | unknown card → `1500-A-LOOKUP-XREF` INVALID KEY | **1** (reason **100**) |

Reject reason codes (`PostingRejectNumericValidationE2ETest`, CBTRN02C lines 380–422):

| Reason | Condition (COBOL) | Fixture |
|--------|-------------------|---------|
| **100** INVALID CARD NUMBER | card absent from XREF (line 385) | unknown card `9999999999999999` |
| **101** ACCOUNT NOT FOUND | account read INVALID KEY (line 397) | card/xref with `null` account id |
| **102** OVERLIMIT | `WS-TEMP-BAL = CYC-CREDIT − CYC-DEBIT + DALYTRAN-AMT`; reject iff `ACCT-CREDIT-LIMIT < WS-TEMP-BAL` (lines 403–413) | limit `1000.00`, txn `1000.01` → `WS-TEMP-BAL = 1000.01` > limit |
| **103** AFTER EXPIRATION | `ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)` (lines 414–420) | expiry `2020-12-31`, orig date `2024-01-15` |

**Overlimit boundary** is pinned precisely: a transaction bringing `WS-TEMP-BAL` *exactly to* the
limit (`1000.00`, limit `1000.00`) is **posted** because the COBOL test is `>=`
(`ACCT-CREDIT-LIMIT >= WS-TEMP-BAL → CONTINUE`); one cent more is rejected 102.

### Interest — `CBACT04C` (`InterestNumericValidationTest` + `BatchGoldenPathE2ETest`)

`1300-COMPUTE-INTEREST`: `COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200` with
`WS-MONTHLY-INT PIC S9(09)V99` (line 168) and **no `ROUNDED` phrase** → COBOL truncates toward
zero to scale 2. Reproduced by `InterestCalculator.monthlyInterest` with `RoundingMode.DOWN`.

| `TRAN-CAT-BAL` | `DIS-INT-RATE` | exact quotient | expected (truncated) | note |
|----------------|----------------|----------------|----------------------|------|
| 1234.56 | 18.00 | 18.51840 | **18.51** | fraction < ½ |
| 100.00 | 23.00 | 1.916666… | **1.91** | discriminating: HALF_UP would give 1.92 |
| 500.00 | 24.00 | 10.00000 | **10.00** | exact |
| 170.00 | 12.00 | 1.70000 | **1.70** | chained in batch golden path |
| 250.00 | 12.00 | 2.50000 | **2.50** | DEFAULT-group rate |
| 0.50 | 12.00 | 0.005 | **0.00** | sub-cent → 0 |

In the batch golden path the posted category balance `170.00` at the `DEFAULT` rate `12.00`
yields interest `1.70`; `1120-UPDATE-ACCOUNT` then `ADD WS-TOTAL-INT TO ACCT-CURR-BAL`
(270.00 + 1.70 = **271.70**) and zeroes the cycle buckets (`ACCT-CURR-CYC-CREDIT`/`-DEBIT` →
**0.00**). One interest transaction of **1.70** is written for the card.

### Statements — `CBSTM03A` / `COSTM01` (`BatchGoldenPathE2ETest`)

After posting + interest, `creastmt` prints the statement. `CreateStatementItemProcessor` sums
`TRAN-AMT` across the account's cards (`WS-TOTAL-AMT`) and shows the account's current balance:

| Field (COSTM01) | COBOL picture | Expected text |
|-----------------|---------------|---------------|
| `ST-CURR-BAL` | `PIC 9(9).99-` | `000000271.70 ` (balance 271.70) |
| `ST-TOTAL-TRAMT` | `PIC Z(9).99-` | `$      171.70 ` (200.00 − 30.00 + 1.70) |

Every text line is asserted to be exactly `StatementFormatter.TEXT_WIDTH` (80) columns, matching
the fixed-width `CBSTM03A` output.

## Validation

`cd java && mvn -B verify` is green (all modules, including the CS-15 e2e + numeric-validation
tests). The application still boots without auto-running any batch job
(`spring.batch.job.enabled=false`; every runner guarded by `@ConditionalOnProperty`).
