# Runbook — Payments Network Failure (Visa Scheme Connection)

**Affected IBS:** IBS 1 (Payments Processing).

**Headline.** The Visa scheme connection is unavailable or experiencing elevated authorisation latency. Members can't transact on Visa-issued Nationwide cards.

## Severity & detection

| Severity | Detection |
|---|---|
| **P1** | Pages from: (a) authorisation success rate dropping below 99% over 1 minute, (b) Visa Network Operations Centre status feed, (c) elevated `99th-pctl-latency` on the scheme connection. |

Time-to-detect SLO: **2 minutes**.

## Impact tolerance & escalation clock

| Elapsed time | Action |
|---|---|
| **0–15 min** | Duty IM owns. Verify the failure is Visa-side (not us). Contact Visa scheme support. |
| **30 min** | Escalate to Director of Card Operations. |
| **1 hr** | Notify CRO duty officer. |
| **2 hr** | **Impact-tolerance breach for IBS 1.** Notify PRA. Decision point: extend Mastercard route? (Out of scope at runtime; on the OR committee's roadmap.) |

## Immediate containment

1. Verify the failure: is it our connection (egress firewall / VPN / certificate) or the scheme's NOC reporting incident? Run the scheme-side health check from `ops-toolkit`.
2. If our side: failover to the secondary scheme-connection endpoint. Verify routing.
3. If scheme side: nothing to do operationally except wait. Update the channel.

## Recovery procedure

This is one of the runbooks where the bank's options are narrow once the failure is scheme-side. The recovery procedure focuses on **member communication** and **graceful return**:

1. Confirm scheme service restored (Visa NOC announcement + our own probes returning healthy).
2. Drain any queued retry traffic at a controlled rate to avoid stampeding herd on the freshly-restored connection.
3. Confirm authorisation success rate returns to baseline.
4. Reconcile any timed-out authorisations against the scheme's settlement file (T+1).

## Customer comms

| Channel | When | Template |
|---|---|---|
| Mobile / web banner | At 30 min | "We're aware of an issue with card transactions. Visa is working to restore service. Your money is safe." |
| Contact-centre script | At 15 min | "Yes — this is a scheme-wide issue, not just Nationwide. Visa is working on it. Your money is safe." |
| Social | At 30 min | Coordinated with Visa's own statement. |

## Regulatory notification triggers

- **PRA.** At 2 hr.
- **PSR.** If any high-value single transaction failed (>£500k).

## Post-incident review

- Whether the secondary endpoint was effective (where applicable).
- Member-affecting decline count from the scheme's reporting.
- Review of the OR committee's decision on Mastercard-route diversification.

---

**Service Owner:** Director of Card Operations
**Scheme Vendor:** Visa Europe
**Last drafted:** Devin, [date]
