# Bill Payment — Migration Plan (Step 2b: `!plan_stream_migration`)

**Stream:** Bill Payment (`CB00` / `COBIL00C`) → Java (Spring Boot) + DB2 + Angular
**Parity mode:** OFF (correctness = `mvn test` + FR acceptance criteria + screen recordings + independent audit)
**DB target:** DB2 (Docker) at runtime; tests run on H2 in **DB2 compatibility mode** for hermetic/fast CI (same Flyway DDL applies to both).

## Topology (confirmed at STOP 0 — single repo)
| Role | Repo | Branch | Path |
|------|------|--------|------|
| SOURCE | COG-GTM/mainframe-modernization-carddemo | integration branch | `app/` (COBOL, unchanged) |
| BACKEND | same | same | `stack/backend/` (Spring Boot, Flyway) |
| FRONTEND | same | same | `stack/frontend/` (Angular) |
| DOCS | same | same | `functional/BILLPAY/` |

## Target architecture
```
Angular (stack/frontend)  ──HTTP──▶  Spring Boot REST (stack/backend)
  bill-payment component               BillPaymentController
  (Acct ID, Confirm, balance,          POST /api/billpay/inquiry  → balance + prompt
   message, ENTER/F3/F4)               POST /api/billpay/pay      → pay-in-full
                                       │
                                       ▼
                                BillPaymentService  (1/1 COBIL00C.PROCESS-ENTER-KEY logic)
                                       │
                          ┌────────────┼────────────┐
                          ▼            ▼             ▼
                 AccountRepository  CardXrefRepository  TransactionRepository
                          └──────── JPA / DB2 (Flyway) ────────┘
                   tables: ACCOUNT (ACCTDAT), CARD_XREF (CXACAIX), TRANSACTION (TRANSACT)
```

## Phased plan
- **Phase 1 — DB mapping / persistence.** Flyway DB2 migrations derived from `CVACT01Y`/`CVACT03Y`/`CVTRA05Y` (`V1__billpay_schema.sql`) + seed (`V2__billpay_seed.sql`). JPA entities `AccountEntity`, `CardXrefEntity`, `TransactionEntity`; repositories with `findMaxNumericId()` + xref-by-acct query. **No stored procedures** (per 2a SP table) — no DBA registration requests.
- **Phase 2 — program migration (single wave; UI-bearing).**
  - `BillPaymentService` reproduces `PROCESS-ENTER-KEY` exactly (validation order, guards, id sequencing, transaction constants, balance = 0, exact messages).
  - `BillPaymentController` exposes `/inquiry` and `/pay`.
  - Angular `bill-payment` screen mirrors map `COBIL0A` (fields, Y/N confirm, balance, RED/GREEN message, ENTER/F3/F4).
  - Unit + slice tests for every FR; behavior covered regardless of parity mode.
- **Phase 4 — E2E + CI.** `@SpringBootTest` E2E asserting API response **and** the persisted DB row + zeroed balance; GitHub Actions `billpay-ci.yml` (backend `mvn test`, frontend `npm ci && build`).
- **Phase 5 — hardening + sign-off.** Edge cases (empty/blank/invalid confirm, non-positive balance, unknown acct, id sequencing from empty table), `BillPay_migration_signoff.md`.
- **Verification.** `BillPay_screen_recording_checklist.md` + recording of the running Angular UI. Independent audit doc.

## Per-wave touched repos / PR shape
Single integration branch, single PR (single-repo topology): backend + frontend + docs together. (Deviation from the default one-child-per-wave fan-out is deliberate — the stream is a single self-contained program; executing inline is faster and equally verifiable. Noted to the user.)

## DB-target decision
DB2 pinned. Runtime: `docker-compose` DB2 profile + DB2 Flyway DDL. Tests: H2 (`MODE=DB2`) applying the same migrations, so CI needs no DB2 container. A DB2 round-trip is verified once locally against the Docker DB2 instance.

## Sign-off gate (tied to FR acceptance criteria)
Migration is "done" when: all FR-1..FR-11 have passing tests; AC-1..AC-5 hold; E2E green in CI; every FR has a recording case (or is flagged for missing test data); independent audit finds 0 missing/stubbed in-scope behavior.
