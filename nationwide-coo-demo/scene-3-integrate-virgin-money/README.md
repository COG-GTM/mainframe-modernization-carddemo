# Scene 3 — Integrate Virgin Money

**The COO question:** *"Nationwide and Virgin Money have completely different data models. How do we make one experience for members without a five-year platform consolidation?"*

This scene shows the anti-corruption layer approach. Each source estate keeps its native data model. The unified API exposes one contract to the digital channel and the contact centre. Adapters at the boundary translate.

## What's in this folder

| Folder | What's there |
|---|---|
| [`nationwide-model/`](./nationwide-model/) | Documentation of the Nationwide source model (CardDemo VSAM-shaped). Entity classes live inside the unified-api module. |
| [`virgin-money-model/`](./virgin-money-model/) | Documentation of the Virgin Money source model (Spring Boot / JPA). Entity classes live inside the unified-api module. |
| [`unified-api/`](./unified-api/) | Runnable Spring Boot 3 service with anti-corruption adapters and a unified OpenAPI 3 contract. |
| [`data-mapping/data-mapping.md`](./data-mapping/data-mapping.md) | Field-by-field mapping with transformation rules and open questions. |

## The architecture

```mermaid
flowchart LR
    classDef channel fill:#1e3a8a,color:#fff,stroke:#1e3a8a
    classDef api     fill:#0d9488,color:#fff,stroke:#0d9488
    classDef adapter fill:#6b21a8,color:#fff,stroke:#6b21a8
    classDef estate  fill:#7c2d12,color:#fff,stroke:#7c2d12

    APP["Mobile app and<br/>contact centre"]:::channel
    UNIFIED["Unified Member API<br/>GET /api/v1/members/{id}"]:::api
    DISPATCH["MemberLookupService<br/>(prefix-based routing)"]:::api
    NW_ADAPT["NationwideMemberAdapter"]:::adapter
    VM_ADAPT["VirginMoneyMemberAdapter"]:::adapter
    NW_DB[("Nationwide<br/>cust / acct / xref")]:::estate
    VM_DB[("Virgin Money<br/>user / primary / savings")]:::estate

    APP --> UNIFIED --> DISPATCH
    DISPATCH -->|nbs-...| NW_ADAPT
    DISPATCH -->|vm-...|  VM_ADAPT
    NW_ADAPT --> NW_DB
    VM_ADAPT --> VM_DB
```

The point of the diagram: **the channel knows about `Member`, not about either estate's tables.** Adapters know about *one* estate only — `NationwideMemberAdapter` doesn't import a single Virgin Money class, and vice versa. New estates plug in by adding a new adapter and extending the routing rule; no existing adapter needs to change.

## What Devin produced

| Artefact | File |
|---|---|
| Canonical domain | [`unified-api/src/main/java/uk/co/nationwide/unified/domain/`](./unified-api/src/main/java/uk/co/nationwide/unified/domain/) |
| Nationwide adapter | [`unified-api/src/main/java/uk/co/nationwide/unified/adapter/NationwideMemberAdapter.java`](./unified-api/src/main/java/uk/co/nationwide/unified/adapter/NationwideMemberAdapter.java) |
| Virgin Money adapter | [`unified-api/src/main/java/uk/co/nationwide/unified/adapter/VirginMoneyMemberAdapter.java`](./unified-api/src/main/java/uk/co/nationwide/unified/adapter/VirginMoneyMemberAdapter.java) |
| Unified service | [`unified-api/src/main/java/uk/co/nationwide/unified/service/MemberLookupService.java`](./unified-api/src/main/java/uk/co/nationwide/unified/service/MemberLookupService.java) |
| REST controller | [`unified-api/src/main/java/uk/co/nationwide/unified/api/MemberController.java`](./unified-api/src/main/java/uk/co/nationwide/unified/api/MemberController.java) |
| OpenAPI 3 spec | [`unified-api/src/main/resources/api/openapi.yaml`](./unified-api/src/main/resources/api/openapi.yaml) |
| Field-by-field mapping | [`data-mapping/data-mapping.md`](./data-mapping/data-mapping.md) |
| End-to-end integration tests | [`unified-api/src/test/java/uk/co/nationwide/unified/MemberLookupIntegrationTest.java`](./unified-api/src/test/java/uk/co/nationwide/unified/MemberLookupIntegrationTest.java) |

## Member ID scheme

Routing key, kept deliberately simple:

| Prefix | Means | Maps to |
|---|---|---|
| `nbs-NNNNNNNNN` | Nationwide member | 9-digit zero-padded `CUST-ID` on the VSAM customer master |
| `vm-N...` | Virgin Money member | JPA-generated `userId` (Long) on the online-banking user entity |

A canonical-member registry would replace this scheme in production. See [`data-mapping/data-mapping.md`](./data-mapping/data-mapping.md) for open questions.

## How to run it

### Tests only

```bash
cd unified-api
./mvnw test    # or: mvn test
```

Runs the end-to-end integration test. Seeds one member in each estate (Helen Wright on Nationwide, Sanjay Gupta on Virgin Money), routes lookups by prefix, asserts the unified API returns the right canonical view.

### Live with docker compose

```bash
# From the repo root
cd nationwide-coo-demo
docker compose up -d postgres
docker compose up --build unified-api
```

Then:

```bash
# Nationwide member (Helen, with one card account)
curl http://localhost:8083/api/v1/members/nbs-000001234 | jq

# Virgin Money member (Sanjay, with current + savings)
curl http://localhost:8083/api/v1/members/vm-1 | jq

# Unknown member in either estate -> 404
curl -i http://localhost:8083/api/v1/members/nbs-999999999
```

## What this demonstrates (and what it does *not*)

**Demonstrated:**

- Devin reads two heterogeneous codebases (COBOL/VSAM and Spring/JPA) and produces a faithful canonical domain.
- Devin produces *clean* adapters — neither adapter imports the other estate's classes. This is the architectural property the COO wants. The integration team can ship one estate's adapter without waiting on the other.
- Devin produces a field-by-field mapping doc *with open questions* — surfacing the integration architect's job rather than papering over it.

**Not demonstrated (deliberate):**

- This API is read-only. Writes would route directly to the source-system APIs; the unified API is not the authoritative store for either estate. Production design would add a write path with idempotency keys.
- Transactions are out of scope for scene 3. See the `data-mapping.md` "out of scope" section for why and where transactions fit.
- No canonical-member registry. The prefix scheme is the smallest viable routing key. Production would add a registry.
