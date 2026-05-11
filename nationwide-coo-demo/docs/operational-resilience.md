# UK Operational Resilience — Quick Reference

The COO who's watching this demo is being held to a specific UK regulatory framework. This document is a cheat-sheet so the presenter can speak it fluently and the buyer trusts the demo is grounded in their reality.

## The frameworks

| Document | Issuer | Applies to | Key requirement |
|---|---|---|---|
| **SS1/21** *Operational resilience: Impact tolerances for important business services* | PRA | Banks, building societies, designated investment firms, insurers | Identify IBS; set impact tolerances; map vulnerabilities; test against severe-but-plausible scenarios |
| **SoP — Operational resilience** | PRA Statement of Policy (March 2021) | Same firms as SS1/21 | Sets supervisory approach to assessing resilience |
| **PS6/21** *Building operational resilience* | FCA Policy Statement | All FSMA-regulated firms | Mirrors SS1/21 for FCA-regulated activity |
| **FG21/3** | FCA finalised guidance | Same | Operationalises PS6/21 |
| **SS2/21** *Outsourcing and third party risk management* | PRA | Same firms | Required to identify third-party dependencies of IBS |

Effective dates: framework went fully effective **31 March 2025**. By that date firms must have *remained within* their impact tolerances for every IBS, evidenced by lessons-learned from severe-but-plausible scenario testing.

## The vocabulary

- **Important Business Service (IBS)** — a service provided by a firm to *external* end users (members, customers, market counterparties) that, if disrupted, could pose a risk to (a) the firm's safety and soundness, (b) the firm's policyholders or depositors, or (c) the stability of the financial system / the firm's market integrity. For Nationwide, "Payments Processing," "Customer Account Management," and "Savings & Transfers" are textbook IBS.
- **Impact tolerance** — the *maximum tolerable duration or extent* of disruption to an IBS. Quoted as a time bound (e.g., *2 hours*) and/or volume bound (e.g., *no more than 1% of daily volume affected*). Set by the board.
- **Severe-but-plausible scenario (SBPS)** — a worst-credible disruption event the firm tests against. Cyber attack, third-party outage, mainframe corruption, regional power loss, key-person loss. Must be documented, tested, and the results reviewed at board level annually.
- **Vulnerability mapping** — for each IBS, the set of people, processes, technology, facilities, information and third parties whose failure could breach the impact tolerance.
- **Self-assessment** — the firm's documented evidence that it remains within impact tolerance for each IBS. Reviewed and approved by the board at least annually. PRA can request on demand.

## What "remaining within impact tolerance" actually means

You don't just write down a number. You have to:

1. Define each IBS precisely (which channels, which member journeys).
2. Define the impact tolerance in measurable terms (time, volume, customer impact).
3. Map every resource the IBS depends on (people, process, technology, third parties).
4. Identify the severe-but-plausible scenarios that could disrupt those resources.
5. **Test** the scenarios. Not desktop. Actually run them — failover drills, tabletop crisis simulations, third-party-failure exercises.
6. Document the test result against the tolerance.
7. If the test shows you'd *breach* the tolerance, you have a remediation plan with a board-approved deadline.
8. Communicate clearly to customers (members) when an IBS is disrupted — including saying so on the website, app, and through the branch network.
9. The board signs the self-assessment annually.

This is a *lot* of documentation. It's the bulk of what Scene 4 of the demo addresses.

## What Devin produces (scene 4)

Devin walks the codebase identified in scenes 1–3, then generates:

- A board-ready **IBS catalogue** with named services, in-scope channels, and member-facing definitions.
- A **vulnerability map** linking each IBS to its underlying technology components, owning teams, and named third parties — built from the actual code and config Devin has just seen.
- **Impact tolerance definitions** with explicit time/volume bounds and the rationale a board paper would carry.
- **Failure scenario runbooks** — procedures the on-call SRE follows when a scenario fires (e.g., *VSAM file corruption recovery*, *Postgres primary failover*, *card-scheme outage rerouting*).
- A **PRA self-assessment template** populated with the above, structured against the SS1/21 evidence requirements.

The point is *not* that Devin replaces the resilience team. The point is that the resilience team gets a 60-page first draft sitting on their desk *built from the actual system*, instead of starting from a blank Word template.

## Where this lives in real Nationwide

- Owned by the **Chief Risk Officer** in conjunction with the **COO**.
- Operationalised by the **Operational Resilience** team, sitting in the second line of defence.
- Evidence chain reviewed by **Internal Audit** quarterly.
- PRA dialogue handled by **Regulatory Affairs** with COO/CRO sponsorship.
- The board's **Risk Committee** owns the impact tolerance numbers. The full board approves the annual self-assessment.

Knowing this lets you walk into the demo and use the right names for the right outputs.
