# Important Business Services — Catalogue

> Devin-generated draft of the IBS register required under PRA SS1/21 §2.7–§2.11 and FCA PS6/21. Three IBS in scope of the combined Nationwide / Virgin Money card and account estate. Times to first draft from cold (after reading scenes 1–3 and `docs/operational-resilience.md`): ~9 minutes.

## What counts as an Important Business Service

Per PRA SS1/21 §2.7, an IBS is a service provided by the firm to an external user, where its disruption could cause **intolerable harm to consumers or risk to market integrity**. Internal services (e.g. internal accounting, employee HR) are *not* IBS even if they are operationally critical.

The COO is accountable for identifying and documenting the IBS register, approving it at the board, and reviewing it at least annually. This document is the working draft for that review.

## IBS register

| # | IBS | External users | Disruption harm | Estate(s) |
|---|---|---|---|---|
| 1 | **Payments Processing** — authorising and settling card transactions for members | Cardholders at point of sale, e-commerce | Member can't pay for goods/services; reputational harm; PSR-monitored | Nationwide (card platform) |
| 2 | **Customer Account Management** — viewing balances, transaction history, statements | All members via mobile app, web, contact centre | Member loses visibility of own money; consumer-duty harm; FCA-monitored | Both (read via unified API) |
| 3 | **Savings & Transfers** — moving money between own accounts, scheduled standing orders, transfers to recipients | Virgin Money customers via mobile app, web | Member can't access own savings; consumer-duty harm; PSR-monitored if a standing order misses | Virgin Money |

A fourth candidate — **New Account Opening** — was considered and de-scoped for this register. Opening a new account is high-value but not real-time; a several-hour outage doesn't cause intolerable consumer harm. It belongs in the wider Operational Resilience programme, just not as a top-tier IBS.

## IBS 1 — Payments Processing

**Service summary.** Authorisation and posting of card transactions on Nationwide-issued cards, for both Nationwide and (post-acquisition) Virgin Money members holding a Nationwide-issued card.

**External users.** Cardholders at merchants (in-store, e-commerce, ATM).

**Why intolerable harm at scale.** A 4-hour disruption on a Friday evening prevents weekend purchases for ~1.4m active cards; concentrated harm on members least able to absorb declined transactions (universal-credit week-ends).

**Underlying components.**

| Component | Estate | Source | Role in IBS |
|---|---|---|---|
| `CBTRN02C` (legacy) or `transaction-posting` service (modernised) | Nationwide | Scenes 1 & 2 | Posts transactions to the card ledger |
| `TRANSACT` VSAM master (legacy) or `transaction` table (modernised) | Nationwide | Scene 2 schema | Authoritative ledger of posted transactions |
| `ACCTDAT` VSAM master or `account` table | Nationwide | Scene 2 schema | Account balances and credit limits |
| `CARDXREF` VSAM master or `card_xref` table | Nationwide | Scene 2 schema | Card → account routing |
| Postgres cluster running the modernised platform | Nationwide | Scene 2 deploy | Storage for the modernised service |
| Card scheme connections (Visa/Mastercard) | External | n/a | Authorisation messaging |
| Tokenisation / HSM | Internal | n/a | PIN-block and PAN protection |
| Payment Systems Regulator (PSR) reporting feed | External | n/a | Mandatory incident reporting >£500k |

**Critical third parties.** Visa scheme connection. Mastercard scheme connection. SWIFT (interbank settlement). HSM vendor.

**Owner.** Director of Card Operations. **Board sponsor.** COO.

## IBS 2 — Customer Account Management

**Service summary.** Read access to the member's account balances, recent transactions, and statements across both estates, via the unified API consumed by mobile, web, and contact-centre.

**External users.** All members (current accounts, savings accounts, credit cards). ~16m members; ~7m monthly active digital users.

**Why intolerable harm at scale.** Members lose visibility into their own money. Consumer duty (Outcome 3, Consumer Understanding) requires we don't actively impede members' ability to make informed financial decisions. A multi-hour read outage during pay-day weekend impedes that for ~16m members.

**Underlying components.**

| Component | Estate | Source | Role |
|---|---|---|---|
| Unified Member API (`unified-api`) | Cross-estate | Scene 3 | Single read surface for digital channel |
| `NationwideMemberAdapter` | Nationwide | Scene 3 | ACL onto card-platform schema |
| `VirginMoneyMemberAdapter` | Virgin Money | Scene 3 | ACL onto VM online-banking schema |
| Nationwide card platform (CardDemo legacy / modernised) | Nationwide | Scenes 1–2 | Source of card balances and transactions |
| Virgin Money online-banking platform | Virgin Money | Scene 3 | Source of current/savings balances |
| Identity Provider (member sign-on) | Cross-estate | n/a | Authn for the channel |
| Mobile app, web channel, contact-centre desktop | Channel | n/a | Consumers of the unified API |

**Critical third parties.** Identity Provider. CDN. Mobile push-notification provider (PNS for breach-of-tolerance alerts).

**Owner.** Director of Digital. **Board sponsor.** COO.

## IBS 3 — Savings & Transfers

**Service summary.** Member-initiated movements of money: between own accounts, to a saved recipient, scheduled standing orders.

**External users.** Virgin Money customers via app/web. (Nationwide-side equivalents run on separate platforms not represented in this demo.)

**Why intolerable harm at scale.** Members can't access their savings for urgent expenditure. Missed standing orders to landlords / utility providers cascade into late-payment fees borne by the consumer. PSR cares about missed payments to other PSPs.

**Underlying components.** Virgin Money online-banking primary/savings tables, the transfers and payments orchestration service (Virgin Money side; not represented in this demo), Faster Payments connection, Bacs connection for standing orders, fraud-screening service.

**Critical third parties.** Faster Payments Service (FPS) scheme. Bacs scheme. Fraud-vendor model API.

**Owner.** Director of Virgin Money Operations. **Board sponsor.** COO.

## Review cadence

- IBS register: reviewed annually (PRA-mandated minimum) or sooner on material change (acquisition, system retirement, new channel).
- Impact tolerances: see [`../pra-evidence/impact-tolerances.md`](../pra-evidence/impact-tolerances.md).
- Cross-estate dependency map: refreshed each time an IBS register update is made.

The next mandated review for Nationwide is the **board OR committee meeting in Q1 2026**, following the March 2025 compliance deadline. This document is the working draft prepared for that review.
