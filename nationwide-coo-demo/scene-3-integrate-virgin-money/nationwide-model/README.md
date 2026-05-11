# Nationwide Source Model

This folder documents the Nationwide-side source model that the unified API consumes via the anti-corruption layer. It does **not** contain a separate Maven project — the entity classes are co-located in the unified-api project under the `uk.co.nationwide.unified.nationwide` package, so the demo builds in one go.

## Source-of-truth for the model

The Nationwide source model is the CardDemo VSAM copybooks. Reference copies:

| Domain entity | Copybook | Java entity in unified-api |
|---|---|---|
| Customer | [`scene-1-understand-the-tail/cobol-source/cpy/CVCUS01Y.cpy`](../../scene-1-understand-the-tail/cobol-source/cpy/CVCUS01Y.cpy) | `NwCustomerEntity.java` |
| Account (card) | [`scene-1-understand-the-tail/cobol-source/cpy/CVACT01Y.cpy`](../../scene-1-understand-the-tail/cobol-source/cpy/CVACT01Y.cpy) | `NwAccountEntity.java` |
| Card | [`scene-1-understand-the-tail/cobol-source/cpy/CVACT02Y.cpy`](../../scene-1-understand-the-tail/cobol-source/cpy/CVACT02Y.cpy) | *(not needed for unified read — display number sourced from xref)* |
| Card cross-reference | [`scene-1-understand-the-tail/cobol-source/cpy/CVACT03Y.cpy`](../../scene-1-understand-the-tail/cobol-source/cpy/CVACT03Y.cpy) | `NwCardXrefEntity.java` |
| Transaction | [`scene-1-understand-the-tail/cobol-source/cpy/CVTRA05Y.cpy`](../../scene-1-understand-the-tail/cobol-source/cpy/CVTRA05Y.cpy) | out of scope for scene 3 (see [data-mapping.md](../data-mapping/data-mapping.md)) |

## ER diagram

```mermaid
erDiagram
    NW_CUSTOMER ||--o{ NW_CARD_XREF : owns
    NW_ACCOUNT  ||--o{ NW_CARD_XREF : holds
    NW_CUSTOMER {
        bigint cust_id PK
        string cust_first_name
        string cust_last_name
        string cust_addr_line_1
        string cust_addr_zip
        date   cust_dob
    }
    NW_ACCOUNT {
        bigint  acct_id PK
        char    acct_active_status
        numeric acct_curr_bal
        numeric acct_credit_limit
    }
    NW_CARD_XREF {
        string xref_card_num PK
        bigint xref_cust_id FK
        bigint xref_acct_id FK
    }
```

## What's *not* in this folder

- A separate `pom.xml` — entities live in the unified-api module.
- The original CardDemo CBL source — those are in scene 1.
- Mock data — seeded by `unified-api/src/main/resources/db/migration/V2__seed_unified.sql`.
