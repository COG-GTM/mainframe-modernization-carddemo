# Runbook — VSAM Master File Corruption (`ACCTDAT`)

**Affected IBS:** IBS 1 (Payments Processing), IBS 2 (Customer Account Management — Nationwide side).

**Headline.** The card account master VSAM dataset is logically corrupt — at least one record has a key inconsistency or unreadable data block. Authorisation cannot be relied on; downstream postings will fail.

## Severity & detection

| Severity | Detection |
|---|---|
| **P1** (highest) | Triggered by any of: (a) `CBTRN02C` job abend with a VSAM file status ≠ '00' on `ACCTDAT`, (b) IDCAMS `EXAMINE` integrity report failure on the dataset, (c) a sustained spike in posting-failure rate (>0.1% over 5 minutes). |

Time-to-detect SLO: **5 minutes**. Detection is paged to the duty IM.

## Impact tolerance & escalation clock

| Elapsed time | Action |
|---|---|
| **0–30 min** | Duty IM owns; assemble the technical bridge. |
| **30 min** | If not contained, escalate to the Director of Card Operations (IBS 1 owner). |
| **1 hr** | If posting is still down or known to be drifted, notify the CRO and start the comms drafting clock. |
| **2 hr** | **Impact-tolerance breach for IBS 1.** Notify the PRA per their standing protocol. Channel banner goes live (see comms below). |
| **6 hr** | If unresolved, the COO chairs the bridge from this point. PSR notification if any single payment >£500k was missed. |

## Immediate containment

1. **Stop posting.** Pause the modernised `transaction-posting` service (`kubectl scale deploy/transaction-posting --replicas=0`) and *halt* the legacy `CBTRN02C` batch job.
2. **Stop authorising.** Set the authorisation feature flag to "decline-with-retry" so the scheme connection returns a retryable code rather than a hard decline.
3. **Snapshot.** Take a forensic snapshot of `ACCTDAT` and the Postgres `account` table.
4. **Page the data-engineering lead** on the bridge — they own the IDCAMS recovery procedure.

## Recovery procedure

1. Run `IDCAMS EXAMINE DATATEST` to identify the corrupt records and the block range. Capture output to incident folder.
2. Restore the most recent **clean** generation of `ACCTDAT` from the GDG, identified by the IDCAMS output as the last pre-corruption generation.
3. Replay the daily transaction file (`DALYTRAN`) forward from the timestamp of the last clean snapshot **using the modernised `transaction-posting` service** to a *staging* Postgres so the legacy and modern paths produce comparable account state.
4. Compare the staging state to the legacy `ACCTDAT` block-by-block. Differences must be reconciled by the data-engineering lead and signed off by the Director of Card Operations.
5. Cut over to the reconciled state. Resume authorisation (lift the feature flag). Resume posting (`kubectl scale deploy/transaction-posting --replicas=N`).

Each step has a rollback gate. If reconciliation fails at step 4, do *not* cut over — return to step 1 and treat as a longer outage.

## Customer comms

| Channel | When | Template |
|---|---|---|
| Mobile / web banner | At 2 hr breach | "Some card transactions may be delayed. Your money is safe. We're working to fix this and will update by [time]." |
| Contact-centre script | At 1 hr | "Yes, we're aware — some card transactions are taking longer than usual to show. Your balance shown is accurate as of [time]." |
| Social (Twitter/X) | At 2 hr breach | Pre-drafted statement from the comms team; published by Head of Comms. |

## Regulatory notification triggers

- **PRA.** Notify if impact tolerance for IBS 1 is breached (i.e. at the 2-hour mark). PRA standing notification channel via the firm's supervisory contact.
- **FCA.** Notify in parallel with PRA for any consumer-impact event affecting >100k members.
- **PSR.** Notify if any single missed payment >£500k OR if cumulative missed payments exceed £5m.

## Post-incident review

The PIR is mandatory for any P1. It includes:

- Root-cause analysis of the corruption (data-engineering lead).
- Effectiveness review of this runbook (duty IM).
- Whether the impact-tolerance breach was avoidable (Director of Card Ops).
- Any updates to this runbook (assigned within 5 working days of the PIR).

---

**Service Owner:** Director of Card Operations
**Last drafted:** Devin, [date]
**Last reviewed (production sign-off):** *pending*
