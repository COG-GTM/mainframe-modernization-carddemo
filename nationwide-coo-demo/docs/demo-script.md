# Demo Script — Nationwide COO Narrative

**Audience:** Chief Operating Officer (or peers — CIO, CTO, Chief Transformation Officer, Chief Risk Officer) of a UK building society / retail bank facing a mainframe modernization tail, an integration of an acquired bank, and the PRA SS1/21 operational-resilience evidence wall.

**Time:** 25 minutes. Five minutes per scene plus opening and close.

**Tone:** Confident, grounded, *not* triumphalist. The COO already knows the problem. The demo is not "let me tell you what's wrong with your bank" — it's "here is what good looks like."

---

## 0 — Opening (3 minutes)

> "Before we start, two sentences of context so we're talking about your business and not a generic AI demo.
>
> Nationwide is the UK's largest mutual. You serve sixteen million members. You bought Virgin Money in October 2024 — the biggest UK financial-services integration in a decade. You've made a public promise of a *full-service modern mutual*. That promise lands on the COO desk as three problems, and they all run on the same clock the PRA gave you for Operational Resilience: March 2025.
>
> The big modernization programmes — core, mortgage, mobile — get the headlines. The modernization *tail* doesn't. Hundreds of legacy components nobody wants to touch, two complete tech estates to merge, and a regulator that wants evidence on every Important Business Service.
>
> We've built a demo around exactly those three problems. We'll show you four scenes. In each scene we'll do two things: walk through what a Devin agent *actually produces* against real code, and call out the before/after — what this looks like today on your team versus what it looks like with Devin. The code under the demo is real. The Spring Boot services run on my laptop. Let's go."

**Why this opens well:** It names the audience's actual world ("sixteen million members," "March 2025," "the COO desk"). It explicitly de-positions Devin from the "we will replace your modernization programme" overclaim. It frames the demo as evidence-led.

---

## Scene 1 — "Understand the Tail" (5 minutes)

**The problem:** *"We have hundreds of COBOL programs and nobody on the team knows what half of them do anymore."*

### Before Devin

> "Today, when the modernization team wants to attack a legacy component, they form a discovery squad. A senior engineer with mainframe experience — increasingly hard to hire — spends *six to eight weeks* reading source, reading JCL, tracing copybook references, interviewing whoever's left who knew the system. That's per component. You have hundreds of them. You can't staff that.
>
> So in practice, the team picks the visible ones, ships them, and the long tail stays on the mainframe — which is the bit the COO is actually worried about."

### With Devin

> "Open scene one. Devin has ingested fifty COBOL programs from the cards-processing system. That took twelve minutes wall-clock. Here's what Devin produced."

*[Open `scene-1-understand-the-tail/outputs/`]*

Walk the audience through three artefacts in this order:

1. **`dependency-graph.md`** — a mermaid graph showing every program-to-program call, every program-to-VSAM-file relationship, every BMS map binding. The COO sees the *shape* of the estate for the first time. "This took twelve minutes. We've spoken to building societies who paid £200k for a manual version of this map."
2. **`modernization-backlog.md`** — every program scored on complexity (cyclomatic + line count + copybook density), risk (no test coverage / no documentation / single-author), and business criticality. Effort estimates in person-weeks. Sequenced for migration. "This is a backlog you can hand to the modernization PMO on a Monday morning."
3. **`complexity-report.md`** — highlighted red list of components that have no test coverage *and* no living author. "These are your land mines. You want to know about them *before* you trigger one."

### The line to land

> "Devin doesn't take work off your senior mainframe engineers. Devin takes the *discovery* work off them so they can spend their time on the migration itself. The thing that used to be six weeks of pre-work is twelve minutes. Now your engineers are doing engineering."

---

## Scene 2 — "Migrate at Speed" (5 minutes)

**The problem:** *"Even once we know what a program does, migrating it takes weeks per program."*

### Before Devin

> "Take one program: `CBTRN02C`. It's a batch program that posts daily card transactions to the master files. It reads a transaction file, validates each record against the card cross-reference and the account master, checks credit limit, posts to the transaction file, updates category balances, updates account balances. About a thousand lines of COBOL plus six copybooks.
>
> Today, migrating one program like this looks like: two engineers, three to four weeks. They have to understand the COBOL, design the Java equivalent, write the entities, the service layer, the database schema, the migrations, the tests, the runbook. Then code review. Then UAT. Per program."

### With Devin

> "Open scene two. The folder structure is literal: `before/` holds the COBOL. `after/` is what Devin produced."

*[Open `scene-2-migrate-at-speed/`]*

Walk through, in this order:

1. **`before/CBTRN02C.cbl`** — show the COBOL. "About a thousand lines. Standard 1990s mainframe shop."
2. **`after/pom.xml`** — Spring Boot 3.2, Java 17, Flyway, Postgres, Maven. "Same stack you're using on the Virgin Money side."
3. **`after/src/main/java/.../domain/Transaction.java`** — JPA entity. "The fields are derived from `CVTRA05Y` — the COBOL copybook. Same names, modern types — `BigDecimal` for amount, `Instant` for the timestamps. Devin preserves the data fidelity."
4. **`after/src/main/java/.../service/TransactionPostingService.java`** — the migrated business logic. "This is `2000-POST-TRANSACTION` from the COBOL, in Java. Validation, credit-limit check, balance update, transaction write — all behind a transactional boundary."
5. **`after/src/main/resources/db/migration/V1__init_card_platform.sql`** — Flyway. "VSAM is gone. PostgreSQL tables with the right indexes are in. The migration script is auditable, replayable, and rolls into your existing CI."
6. **`after/src/test/java/.../TransactionPostingServiceTest.java`** — JUnit. "Tests cover the rejection paths from the COBOL — expired card, exceeded credit limit, missing cross-reference. Run them: `mvn test`. They pass."
7. **`README.md` side-by-side diff** — the COBOL paragraph and the Java method, side by side. "Same logic, modern stack. The auditor can trace line by line."

If the room wants to see it run, switch to a terminal:

```bash
docker compose up -d postgres
docker compose up --build transaction-posting
curl -X POST localhost:8082/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d @scene-2-migrate-at-speed/after/src/test/resources/sample-transaction.json
```

It responds with the posted transaction ID and the updated account balance.

### The line to land

> "We're not claiming a 1000-line batch program migrates itself. We're claiming the first 80% — the entities, the schema, the migrations, the service skeleton, the tests — takes Devin minutes, not weeks. Your engineers spend their time on the 20% that *needs* a senior engineer: edge cases, behaviour the COBOL got wrong, business rules nobody documented. That's where you want senior time spent."

Reference Centene as evidence this is real: COG-GTM has a centene-cobol-demo → centene-migrated-app pattern in production-scale form. This isn't a toy.

---

## Scene 3 — "Integrate Virgin Money" (5 minutes)

**The problem:** *"Nationwide and Virgin Money have completely different data models. How do we make one experience without a five-year platform consolidation?"*

### Before Devin

> "Today the integration team picks a member journey — say, *one customer view across both estates* — and starts mapping. Field by field. Meeting by meeting. Architecture forum after architecture forum. Six months to agree the mapping, three months to build the adapter, two months to test. Per journey. And you have dozens of journeys."

### With Devin

> "Open scene three. Devin has read the data model on both sides. Here's the output."

*[Open `scene-3-integrate-virgin-money/`]*

Walk through:

1. **`data-mapping/data-mapping.md`** — field-by-field mapping document. Customer identity. Account. Card. Transaction. Address. Status. For each field, the Nationwide source, the Virgin Money source, the chosen canonical form, the transformation rule, the open question for the data owner. "This is the document that today takes the integration architect three months to produce."
2. **`unified-api/src/main/resources/api/openapi.yaml`** — OpenAPI 3 spec for `/api/v1/members/{memberId}`. One unified contract spanning both estates. "Your mobile team and your contact-centre team build against *this*. Not against the two underlying systems."
3. **`unified-api/src/main/java/.../adapter/NationwideMemberAdapter.java`** and `VirginMoneyMemberAdapter.java` — the **anti-corruption layer**. "Two adapters. Each one knows about one source system. Neither one knows about the other. The unified API layer composes them. That's the strategic answer to integration: you're not picking which model wins. You're letting both stay native, and putting clean boundaries around them."
4. **`unified-api/src/test/java/.../MemberLookupIntegrationTest.java`** — integration test. "Looks up a member by canonical ID. The lookup hits both adapters. The response is a unified `Member` object. The test asserts that the savings balance came from Virgin Money and the card balance came from Nationwide. End-to-end proof."

Run it live:

```bash
docker compose up --build unified-api
curl localhost:8083/api/v1/members/nbs-000000001234 | jq
```

The response is a single unified JSON payload with member, accounts (current, savings, card), and transaction summary.

### The line to land

> "The strategic point is the *anti-corruption layer*. You don't have to pick a winner between the two estates. You don't have to consolidate the data model. You build clean adapters at the boundary, and you let the digital channel and the contact-centre work against one unified contract. Devin builds those adapters for you. The integration architect now does what their title says — *architecture* — instead of spending nine months doing the field mapping by hand."

---

## Scene 4 — "Operational Resilience" (5 minutes)

**The problem:** *"The PRA wants evidence of operational resilience by March 2025. Every Important Business Service mapped, every impact tolerance set, every severe-but-plausible scenario tested, every runbook documented. The board reviews it quarterly."*

### Before Devin

> "Today that evidence pack is produced by the Operational Resilience team in second line. They pull architecture diagrams. They schedule interviews with every system owner. They reverse-engineer impact tolerances from change records. They write runbooks based on what happened the last time something broke. Six to nine months per IBS. And the system underneath the IBS keeps changing while they're writing.
>
> When the PRA visits, the evidence pack is current to a point six months ago. That's not great."

### With Devin

> "Open scene four. Devin has walked the code from scenes one, two, and three — the whole estate the demo represents. Here's the evidence pack."

*[Open `scene-4-operational-resilience/`]*

Walk through:

1. **`ibs-mapping/ibs-catalogue.md`** — three Important Business Services named, scoped, and member-facing. *Payments Processing.* *Customer Account Management.* *Savings & Transfers.* Each one with its in-scope channels, its member-facing definition (the words you'd put on a service-status page), and which estate provides it.
2. **`ibs-mapping/ibs-to-tech.mmd`** — a mermaid diagram. Each IBS maps to specific components (Spring Boot services from scene 2 and 3, COBOL programs from scene 1, the Postgres DB, the legacy VSAM files). "This is the vulnerability map SS1/21 asks for. It is *generated from the code*, so it's current the day you run it."
3. **`ibs-mapping/impact-tolerances.md`** — board-paper-grade impact tolerance definitions. Time bounds, volume bounds, customer-impact bounds. With the rationale. "This is what your Risk Committee approves. Devin gives them the draft."
4. **`dependency-analysis/cross-system-dependencies.md`** — every external dependency named. The card scheme. The payment-scheme rail. The Virgin Money third-party card processor. The mainframe BCP site. Each one named, classified by criticality, mapped to an IBS. "This is where the PRA reads first. *Who do you depend on, and what happens when they fail?*"
5. **`runbooks/`** — four runbooks. *VSAM corruption recovery.* *Postgres primary failover.* *Card scheme outage rerouting.* *Mainframe BCP invocation.* Each one with detection, immediate action, escalation, communication, postmortem checklist. "These aren't pretty. They're not meant to be. They're meant to be what the on-call SRE follows at 3am."
6. **`pra-evidence/sa-template.md`** — the self-assessment template the PRA expects, populated against the work above. Section by section, mapped to SS1/21 paragraphs. "When the regulator visits, this is what you hand them."

### The line to land

> "The PRA isn't asking you to invent resilience. They're asking you to *evidence* it. Today that evidence is a six-to-nine-month exercise per IBS for the second line of defence. Devin produces the first-draft evidence pack from the code itself, the day you run it. Your resilience team reviews, challenges, signs. That's where the senior time should be spent — on the judgement, not the document production."

---

## Close (2 minutes)

> "Quick recap.
>
> *Scene one.* Devin gives you the modernization tail — every legacy component, scored, sequenced, risk-flagged. Twelve minutes instead of six months.
>
> *Scene two.* Devin migrates a batch program in minutes — entities, schema, service, tests, runbook. Your senior engineers go straight to the 20% that needs them.
>
> *Scene three.* Devin builds the anti-corruption layer between Nationwide and Virgin Money. Field-level mapping document, unified OpenAPI, working adapters. No five-year platform consolidation required.
>
> *Scene four.* Devin produces the operational resilience evidence pack the PRA is going to ask for, generated from the live system, ready for your second line to review and sign.
>
> Devin doesn't replace your modernization programme. It accelerates the *tail*. It de-risks the *integration*. It generates the *evidence*.
>
> If those three are the three things on the COO's desk this year, we should set up a workshop. I'll bring engineers. You bring one of those mainframe components nobody wants to touch."

**Optional add-on lines depending on the audience reaction:**

- For a CRO in the room: "The evidence pack in scene four isn't just for the PRA. The same pack feeds your three-lines-of-defence assurance work and your ICAAP control-environment narrative."
- For a CTO in the room: "Scene two's pattern — COBOL to Spring Boot via Devin — is the same pattern we ran against Centene's stack in the US. Same Java conventions, same Flyway approach, same test framework. Production-proven, not demoware."
- For a CFO in the room: "We can walk through the unit economics — how many person-weeks the four scenes would have cost you, versus the Devin licence. The number tends to surprise people."

---

## Pre-demo checklist

Before walking into the room:

- [ ] `docker compose up -d` ran clean on your laptop this morning.
- [ ] You can hit `localhost:8082/api/v1/transactions` and `localhost:8083/api/v1/members/nbs-000000001234` and get JSON back.
- [ ] You've opened `scene-1-understand-the-tail/outputs/dependency-graph.md` in something that renders mermaid (GitHub, VS Code with the mermaid plugin).
- [ ] You've read `docs/nationwide-context.md` and `docs/operational-resilience.md` once on the train.
- [ ] You know the COO's name. You know who else is in the room. You know if there's a CRO and a CTO so you can call out the optional add-on lines.
