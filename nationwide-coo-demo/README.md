# Nationwide Building Society — COO Demo

> A "milk-and-honey" narrative for a Chief Operating Officer of the UK's largest building society. Four scenes showing how **Devin** accelerates the long tail of modernization, de-risks the Virgin Money integration, and produces the evidence the PRA needs for Operational Resilience.

This is a **reference demo**. All "Devin-generated" artefacts under each scene's `outputs/` folder are realistic, high-quality examples of what Devin produces against a real codebase — not hand-written specs dressed up as AI output. The two Spring Boot services in scenes 2 and 3 compile, pass tests, and run locally via `docker compose`.

## The setup

Nationwide Building Society is the UK's largest mutual: ~16 million members, ~£260bn of assets, a 165-year-old technology estate now sitting next to **Virgin Money** (acquired October 2024). The CEO's promise to members is a *full-service modern mutual* — which lands on the COO's desk as three problems at once:

1. **The tail.** The big modernization programmes hit the front of the slide. The COO knows the real operational risk lives in the *hundreds of legacy components* nobody wants to touch — undocumented COBOL, 30-year-old VSAM files, batch windows held together by GDGs and prayer.
2. **The integration.** Two complete tech estates have to look like one mutual to the member, the colleague, and the regulator. Nationwide's mainframe card platform and Virgin Money's online banking stack don't share a single data model.
3. **The regulator.** PRA SS1/21 (and FCA PS6/21) **Operational Resilience** rules went fully effective in **March 2025**. Every Important Business Service (IBS) needs a documented impact tolerance, a tested failure scenario, and an evidence trail the regulator can audit.

Devin doesn't replace the modernization programme. Devin accelerates the tail, de-risks the integration, and generates the evidence.

## The four scenes

| Scene | Problem | Devin output |
|---|---|---|
| [**1 — Understand the Tail**](./scene-1-understand-the-tail/) | "We have hundreds of COBOL programs and nobody knows what they do." | Dependency graph, modernization backlog with effort estimates, risk-scored complexity report — generated in minutes from the source. |
| [**2 — Migrate at Speed**](./scene-2-migrate-at-speed/) | "Migrating one batch program takes a senior engineer weeks." | `CBTRN02C` (the daily transaction posting batch) reborn as a runnable Spring Boot 3 microservice with JPA entities, Flyway VSAM→PostgreSQL migrations, REST API, and JUnit business-logic tests. Side-by-side COBOL/Java diff in the README. |
| [**3 — Integrate Virgin Money**](./scene-3-integrate-virgin-money/) | "Nationwide and Virgin Money have completely different data models." | Field-level mapping document, anti-corruption layer in Java translating between the two domains, unified OpenAPI 3 spec, integration tests proving the round-trip. |
| [**4 — Operational Resilience**](./scene-4-operational-resilience/) | "The PRA wants evidence by March 2025." | IBS catalogue mapped to technology components, impact tolerance definitions, failure scenario runbooks, PRA SS1/21 self-assessment template, and a mermaid IBS-to-tech map across both estates. |

## How to run the live portion

```bash
cd nationwide-coo-demo
docker compose up -d postgres
docker compose up --build transaction-posting unified-api
```

Once the services are healthy:

```bash
# Scene 2 — post a transaction through the Spring Boot replacement of CBTRN02C
curl -X POST http://localhost:8082/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d @scene-2-migrate-at-speed/after/src/test/resources/sample-transaction.json

# Scene 3 — fetch a unified member profile spanning both estates
curl http://localhost:8083/api/v1/members/nbs-000000001234
```

## How to present the demo

Read [`docs/demo-script.md`](./docs/demo-script.md). It's a 25-minute talk track scene-by-scene with the "before Devin / with Devin" beats called out, plus the closing slide.

## Disclaimers

- This is **not** a real Nationwide Building Society system. Nationwide and Virgin Money branding is used here for a narrative demo only.
- The COBOL under `scene-1-understand-the-tail/cobol-source/` is the public AWS *CardDemo* reference application, re-contextualized as "Nationwide Legacy Card Processing System."
- The Virgin Money domain model under `scene-3-integrate-virgin-money/virgin-money-model/` is adapted from a public open-source Spring Boot online-banking example.
- Spring Boot services use Maven, Java 17, Spring Boot 3.2, Flyway, PostgreSQL 16. Tests run via `mvn test` without any external services (H2 in-memory).

## Layout

```
nationwide-coo-demo/
├── README.md
├── docker-compose.yml
├── docs/
│   ├── demo-script.md
│   ├── nationwide-context.md
│   └── operational-resilience.md
├── scene-1-understand-the-tail/
│   ├── README.md
│   ├── cobol-source/           # rebranded copy of CardDemo cbl/cpy/bms
│   └── outputs/                # Devin-generated analysis artefacts
├── scene-2-migrate-at-speed/
│   ├── README.md
│   ├── before/                 # original CBTRN02C.cbl
│   └── after/                  # Spring Boot transaction-posting service
├── scene-3-integrate-virgin-money/
│   ├── README.md
│   ├── nationwide-model/       # CardDemo VSAM layouts as Java entities
│   ├── virgin-money-model/     # Online-banking domain model
│   ├── unified-api/            # ACL + unified REST + OpenAPI
│   └── data-mapping/           # field-level mapping document
└── scene-4-operational-resilience/
    ├── README.md
    ├── ibs-mapping/
    ├── dependency-analysis/
    ├── runbooks/
    └── pra-evidence/
```
