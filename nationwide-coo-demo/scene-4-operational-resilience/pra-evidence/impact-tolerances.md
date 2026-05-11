# Impact Tolerances — Draft

> Devin-generated draft impact tolerances for review at the Q1 2026 board Operational Resilience committee. These are the **outermost** times during which the firm can tolerate disruption to each Important Business Service before consumer harm becomes intolerable. Per PRA SS1/21 §2.31–§2.39, the firm sets these; the regulator does not.

## What an impact tolerance is (and isn't)

An impact tolerance is **not** an SLO. It is the duration beyond which disruption causes intolerable harm — measured at the *external user* level. SLOs are internal targets we set to stay safely inside the tolerance. The regulator wants to see:

1. A defined tolerance per IBS, expressed in time.
2. Evidence that the firm can stay within it under severe-but-plausible disruption.
3. A plan to remediate where the firm currently *cannot* meet the tolerance.

## Draft tolerances

| IBS | Impact tolerance | Stated as | Devin's working assumption (subject to OR committee approval) |
|---|---|---|---|
| 1. Payments Processing | **2 hours** | "Authorisation of card transactions must resume within 2 hours of incident detection" | Holds for the *real-time* path. The next-day batch posting has a separate, looser tolerance — see below. |
| 2. Customer Account Management | **2 hours** | "Members must be able to see their balances and recent transactions within 2 hours" | Holds for digital channel. Contact-centre fallback (read-only) acceptable for the read path. |
| 3. Savings & Transfers | **2 hours** for real-time transfers; **24 hours** for scheduled (Bacs) standing orders | "Real-time transfers must resume within 2 hours; scheduled payments must complete before their value date" | The 24-hour figure reflects Bacs's value-date cycle — a missed scheduled run can recover on the next cycle if within the same business day. |

## Tolerances for batch-mode work

Within IBS 1, the **end-of-day posting batch** (currently `CBTRN02C`; modernised in scene 2) has a separate impact tolerance:

| Sub-process | Tolerance | Notes |
|---|---|---|
| Daily posting batch must complete | Before the next-business-day cut at 06:00 | If it doesn't, all of next day's online balances are stale; downstream interest calc and overdraft fees compound the harm |
| Posting must be reconcilable to scheme settlement file | Within 1 business day | If we can't reconcile, the firm carries unresolved posting risk |

## Why these numbers (Devin's draft justification)

These are not arbitrary. The justifications are drawn from:

- **PRA Dear CEO letters** on operational resilience, which signal expectations on response times for authorisation and customer-visible balance information.
- **PSR incident reporting thresholds**, which establish what the regulator considers a material payments-processing event.
- **FCA Consumer Duty Outcome 3 (Consumer Understanding)**, which treats sustained inability to see one's balance as a consumer-impact event.
- **Internal data** — Devin would normally consume historical incident times and member-impact reports here; the demo uses placeholder reasoning.

These figures **must** be reviewed by the OR committee and adjusted based on actuals — particularly because IBS 1 and IBS 2 currently share underlying infrastructure (Postgres, IdP, channel), which means a 2-hour tolerance on each *may* require the underlying components to recover in considerably less than 2 hours to avoid double-counting.

## Open actions arising from this draft

1. **Test the 2-hour figure for IBS 1** against a scenario-based exercise involving Postgres failover + scheme reconnect. (Owner: Director of Card Ops.)
2. **Reconcile the 24-hour Bacs figure** with the actual Bacs cycle for the Virgin Money estate. (Owner: Director of Virgin Money Ops.)
3. **Examine cross-estate IdP dependency.** A single IdP failure can blow both IBS 2 and IBS 3 simultaneously — does the 2-hour tolerance still make sense if a *single* root cause takes out both? (Owner: OR committee chair.)

---

**Drafted by:** Devin
**For review by:** Operational Resilience Committee, Q1 2026
**Approval authority:** Board, via the COO
