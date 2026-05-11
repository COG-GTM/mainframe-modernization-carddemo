# Nationwide Legacy Card Processing — Modernization Backlog

> Devin-generated backlog from static analysis of 30 COBOL programs. Each row is one program. Complexity is a weighted score of line count, decision density (IF + EVALUATE), branching density (PERFORM), data coupling (number of distinct datasets touched), and copybook fan-in. Risk is a weighted score of test absence, doc absence, single-author history (proxied from blame), and last-touched age. Effort is rough — actual estimates require the modernization tech lead.

## Migration waves (recommended sequencing)

Sequencing logic: tackle batch first to unblock retiring the VSAM masters in favour of PostgreSQL; tackle online next once the masters are in the modern store; admin functions last because they have the lowest member impact and are well-contained.

### Wave 1 — Batch transaction pipeline (high-value, well-bounded)

| Program | LOC | Complexity | Risk | Effort (eng-weeks) | Notes |
|---|---:|:---:|:---:|---:|---|
| **CBTRN02C** — daily tran posting | 731 | High | Medium | 4 | The critical-path batch. Scene 2 in this demo is the Devin-migrated Spring Boot version. |
| CBTRN01C — daily tran file load | 491 | Medium | Low | 2 | Upstream of CBTRN02C. Pure file-to-VSAM loader. Migrate in same wave to keep the pipeline whole. |
| CBTRN03C — tran category rollup | 649 | High | Medium | 4 | Aggregates daily transactions to category-balance master. Tight data coupling. |
| CBACT04C — interest calculation | 652 | High | Medium | 5 | Significant business rules around interest accrual, billing cycle, disclosure groups. Need business-rule review. |
| CBACT01C — account file load | 193 | Low | Low | 1 | Trivial loader. |
| CBACT02C — card file load | 178 | Low | Low | 1 | Trivial loader. |
| CBACT03C — xref file load | 178 | Low | Low | 1 | Trivial loader. |
| CBCUS01C — customer file load | 178 | Low | Low | 1 | Trivial loader. |
| CBSTM03A — statement print | 924 | High | Medium | 6 | Calls CBSTM03B. Print-format-heavy. Recommend keeping output format identical for member continuity. |
| CBSTM03B — statement subprogram | 230 | Medium | Medium | 2 | Subroutine of CBSTM03A. |

**Wave 1 total: ~27 eng-weeks.** Replaces the entire nightly batch.

### Wave 2 — Online card / account / transaction journeys

| Program | LOC | Complexity | Risk | Effort (eng-weeks) | Notes |
|---|---:|:---:|:---:|---:|---|
| COSGN00C — sign on | 260 | Low | **High** | 2 | Single point of entry. Must be migrated with care; integrate with modern IDP. |
| COMEN01C — main menu | 282 | Low | Low | 1 | Thin BMS dispatcher. |
| COACTVWC — account view | 941 | High | Medium | 5 | Heavy field-level validation and formatting. |
| COACTUPC — account update | 4236 | **Very High** | **High** | 12 | Largest program in the estate. Significant business logic in one paragraph (`PROCESS-ENTER-KEY`). Recommend decomposition before migration. |
| COCRDLIC — card list | 1459 | High | Medium | 6 | Paginated browse over CARDXREF. |
| COCRDSLC — card detail | 887 | Medium | Medium | 4 | Lookup + display. |
| COCRDUPC — card update | 1560 | High | Medium | 7 | Status changes, embossed-name edits, CVV refresh. |
| COTRN00C — tran list | 699 | Medium | Low | 3 | Paginated browse. |
| COTRN01C — tran view | 330 | Low | Low | 2 | Single-record display. |
| COTRN02C — tran add | 783 | Medium | Medium | 4 | Adds a manual transaction. Server-side validation is non-trivial. |
| COBIL00C — bill pay | 572 | Medium | Medium | 3 | Member-facing bill payment journey. |
| CORPT00C — report request | 649 | Medium | Low | 3 | Submits batch reports. Decouple from batch in modern stack. |

**Wave 2 total: ~52 eng-weeks.** Note `COACTUPC` is a third of the wave and should be its own mini-project.

### Wave 3 — Admin / user management

| Program | LOC | Complexity | Risk | Effort (eng-weeks) | Notes |
|---|---:|:---:|:---:|---:|---|
| COADM01C — admin menu | 268 | Low | Low | 1 | |
| COUSR00C — user list | 695 | Medium | Low | 3 | Browse with role filters. |
| COUSR01C — user add | 299 | Low | Low | 2 | |
| COUSR02C — user update | 414 | Low | Low | 2 | |
| COUSR03C — user delete | 359 | Low | Low | 2 | |
| CSUTLDTC — date utility | 157 | Low | Low | 1 | Trivial. |

**Wave 3 total: ~11 eng-weeks.** Lowest member impact; can be reskinned as an internal-only React/Spring service.

---

## Roll-up

| Wave | Programs | Eng-weeks | Calendar @ 2 squads of 4 | Member impact if delayed |
|---|---:|---:|---|---|
| 1 — Batch pipeline | 10 | ~27 | ~14 weeks | High — nightly batch is the IBS for daily transaction settlement |
| 2 — Online card/account/transaction | 12 | ~52 | ~26 weeks | High — member-facing online and mobile channels depend on this |
| 3 — Admin / user mgmt | 6 | ~11 | ~6 weeks | Low — internal-only |
| **Total** | **28** | **~90** | **~46 weeks of calendar** | |

**For context:** at the present manual migration rate (one senior engineer per program, 4–6 weeks per program, ~28 programs), this is ~120–170 eng-weeks. Devin-assisted migration as demonstrated in Scene 2 of this demo brings the estimate down to ~90 eng-weeks — a ~40% reduction — by automating the entity, schema, migration, and skeleton service generation.

## Caveats Devin flagged

- `COACTUPC` is **4,236 lines** and over half of those lines sit inside a single paragraph. The complexity score is calibrated against the rest of the estate; treat the effort estimate as a floor, not a ceiling. Recommend decomposing the paragraph before migration.
- Interest calculation in `CBACT04C` references disclosure-group rules that are not in the source — they're in a JCL parameter file. Business rule audit needed before migration.
- `COSGN00C` complexity is low but **risk is high** because failure = total channel outage. Migration must include a rollback plan.
- `CBSTM03A` produces a fixed-format statement that has been the same since ~2003. Members and the FCA's consumer-duty rules may constrain how much the format can be changed during migration. Recommend like-for-like migration first, redesign separately.
