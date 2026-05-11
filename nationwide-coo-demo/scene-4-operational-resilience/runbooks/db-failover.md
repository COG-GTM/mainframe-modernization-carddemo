# Runbook — Postgres Primary Failover (Card Platform)

**Affected IBS:** IBS 1 (Payments Processing), IBS 2 (Customer Account Management — Nationwide side).

**Headline.** The Postgres primary backing the modernised card platform has become unhealthy and the cluster has either failed over to the standby or is requiring operator-driven failover.

## Severity & detection

| Severity | Detection |
|---|---|
| **P1** if writes are failing in `transaction-posting`. **P2** if reads only are degraded. | Pages from: (a) Postgres clustering alerts (Patroni / native streaming-replication health), (b) APM 5xx rate on `transaction-posting` exceeding 0.5% over 1 min, (c) `Liveness probe failed` on the application. |

Time-to-detect SLO: **2 minutes**.

## Impact tolerance & escalation clock

| Elapsed time | Action |
|---|---|
| **0–10 min** | Cluster should fail over automatically. Duty IM monitors. |
| **10 min** | If not yet recovered, page the platform-database team lead and start an operator-driven failover from the bridge. |
| **30 min** | If still degraded, escalate to Director of Card Operations and CRO duty officer. |
| **2 hr** | **Impact-tolerance breach for IBS 1.** Notify the PRA. Channel banner if read path is degraded. |

## Immediate containment

1. **Confirm the failure mode** — primary unresponsive vs replica lag vs split-brain. Read the Patroni `cluster` output (or equivalent).
2. **Drain connections** on the unhealthy node to allow clean failover.
3. **Pause batch jobs** that write to the card platform tables — `CBTRN02C` legacy batch and the modernised batch driver.

## Recovery procedure

1. **Automatic failover.** Patroni promotes the synchronous standby. The application reconnects via the cluster VIP / proxy.
2. **Manual failover (if automatic didn't trigger or completed inconsistently).** Platform-database lead runs `patronictl failover <cluster>`. Verify the new primary is accepting writes (`SELECT pg_is_in_recovery();` returns `f`).
3. **Re-attach replicas.** Old primary becomes a replica once it returns. Streaming replication is re-established. Verify replication lag is <10 seconds.
4. **Resume the application.** Scale `transaction-posting` back to target replica count. Resume the batch driver.
5. **Reconcile.** Confirm no transactions were lost or duplicated using the cluster's pre/post-failover LSN positions and the `transaction` table's monotonic `tran_id`.

## Customer comms

| Channel | When | Template |
|---|---|---|
| Mobile / web banner | At 30 min (read path degraded) | "Some account information may take longer to load. Your money is safe." |
| Contact-centre script | At 30 min | "We're seeing some delays — please bear with us." |

## Regulatory notification triggers

- **PRA.** At 2 hr impact-tolerance breach for IBS 1.
- **FCA.** If member visibility (IBS 2) is impaired for >2 hr.

## Post-incident review

- Root-cause analysis from the platform-database lead within 3 working days.
- Whether the cluster's RTO matched the IBS 1 impact tolerance (this is the open action raised in `../dependency-analysis/cross-estate-dependencies.md`).

---

**Service Owner:** Director of Card Operations
**Platform Owner:** Head of Platform Engineering
**Last drafted:** Devin, [date]
