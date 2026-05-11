# Failure-Scenario Runbooks

> Devin-generated runbook templates for the most plausible severe-but-plausible disruptions to the IBS in scope. Drafted from the dependency-analysis map; reviewed and signed off by service owners listed at the foot of each runbook.

PRA SS1/21 §4 expects each IBS to be tested against severe-but-plausible disruption scenarios. The runbooks here are the **first-responder operational procedures** that the duty IM / on-call ops engineer follows when one of those scenarios happens for real, *and* the script used in scenario-testing exercises.

## Runbooks in this folder

| Scenario | Affected IBS | File |
|---|---|---|
| VSAM master file corruption (`ACCTDAT`) | IBS 1, IBS 2 | [`vsam-master-corruption.md`](./vsam-master-corruption.md) |
| Postgres primary failure on the modernised card platform | IBS 1, IBS 2 | [`db-failover.md`](./db-failover.md) |
| Payments network failure (Visa scheme connection) | IBS 1 | [`payments-network-failure.md`](./payments-network-failure.md) |
| Identity Provider outage | IBS 2, IBS 3 | [`idp-outage.md`](./idp-outage.md) |

## Common structure

Each runbook follows the same structure so the duty engineer doesn't have to think about layout under pressure:

1. **Headline.** One-line description of the failure and which IBS it impacts.
2. **Severity & detection.** What alert(s) fire, where, and the time-to-detect SLO.
3. **Impact tolerance & escalation clock.** What the impact tolerance is and the elapsed-time triggers for escalation.
4. **Immediate containment.** First five minutes — stop the bleeding.
5. **Recovery procedure.** Step-by-step recovery, with rollback gates.
6. **Customer comms.** Templates for the digital channel banner and contact-centre script.
7. **Regulatory notification triggers.** When to notify the PRA, FCA, PSR.
8. **Post-incident review trigger.** What goes in the PIR.

These templates were drafted by Devin; the real runbooks for production use will be signed off by the relevant Service Owner (named at the foot of each).
