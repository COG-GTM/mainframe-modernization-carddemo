# Nationwide ↔ Virgin Money Data Mapping

> Devin-generated, field-by-field translation between the Nationwide card platform (CardDemo VSAM copybooks) and the Virgin Money online-banking platform (Spring Boot / JPA entities). Time to produce from cold: ~7 minutes after reading both source repos.

## Scope of this document

This is the working data dictionary that lets the integration team write adapter code (see `../unified-api/src/main/java/uk/co/nationwide/unified/adapter/`) and have confidence the canonical domain is faithful to both estates.

For each canonical field in the unified `Member` and `Account` domain, this document records:

- the source field on the Nationwide side
- the source field on the Virgin Money side
- the chosen canonical representation
- the transformation rule (if any)
- open questions for the data owner

## Member identity

| Canonical | Nationwide source | Virgin Money source | Transformation | Notes |
|---|---|---|---|---|
| `memberId` | `CUST-ID` (PIC 9(09), VSAM `CUSTDAT`) | `userId` (Long, JPA `User`) | Prefix scheme: `nbs-NNNNNNNNN` (zero-padded) or `vm-N...` | Routing key for the adapters. **Open Q:** does a canonical-member registry exist? If so, replace the prefix scheme with a registry lookup. |
| `firstName` | `CUST-FIRST-NAME` (X(25)) | `User.firstName` | Trim trailing spaces | Nationwide pads to 25 chars; Virgin Money is unpadded. |
| `lastName` | `CUST-LAST-NAME` (X(25)) | `User.lastName` | Trim trailing spaces | |
| `email` | *(not on `CUSTOMER-RECORD`)* | `User.email` (unique, not null) | — | Nationwide email lives in a separate **CIF extension table** not in the CardDemo source. **Open Q:** is the email a member-confirmed channel for both estates, or only Virgin Money? |
| `phone` | `CUST-PHONE-NUM-1` (X(15)) | `User.phone` | Trim | Nationwide stores up to 2 phone numbers; canonical only carries primary. |
| `dateOfBirth` | `CUST-DOB-YYYY-MM-DD` (X(10)) | *(not stored on `User`)* | Parse `yyyy-MM-dd` to `LocalDate` | Virgin Money DoB lives in the branch CRM and isn't on the digital user record. **Open Q:** does consumer-duty require DoB display on a unified member view? |

## Address

| Canonical | Nationwide source | Virgin Money source | Transformation | Notes |
|---|---|---|---|---|
| `address.line1` | `CUST-ADDR-LINE-1` (X(50)) | *(not on `User`)* | Trim | |
| `address.line2` | `CUST-ADDR-LINE-2` | *(not stored)* | Trim | |
| `address.line3` | `CUST-ADDR-LINE-3` | *(not stored)* | Trim | |
| `address.city` | *(embedded in line-3 by convention)* | *(not stored)* | Parse from line-3 if needed | The Nationwide record predates a structured city field. |
| `address.postcode` | `CUST-ADDR-ZIP` (X(10)) | *(not stored)* | Trim | UK postcodes are X(8) in practice; the X(10) leaves slack. |
| `address.countryCode` | `CUST-ADDR-COUNTRY-CD` (X(03)) | *(implicit, GBR)* | ISO 3166-1 alpha-3 | Defaults to `GBR` on Virgin Money side. |

**Open Q:** Virgin Money's authoritative address is in the branch CRM, not the digital user record. For a unified read view, do we (a) accept that Virgin Money members have no address until the next CRM sync, (b) call the branch CRM in the lookup hot path, or (c) maintain a cached projection? Recommend (c) — outside scope of this demo.

## Accounts — card

| Canonical | Nationwide source | Virgin Money source | Transformation | Notes |
|---|---|---|---|---|
| `account.accountId` | `ACCT-ID` (PIC 9(11)) | n/a (no card on VM stack) | String of the 11-digit number | |
| `account.type` | constant `CARD` | n/a | — | |
| `account.displayNumber` | `XREF-CARD-NUM` (X(16)) | n/a | Mask all but the last 4 — `****-****-****-NNNN` | Following PCI-DSS-aligned display practice. |
| `account.balance` | `ACCT-CURR-BAL` (S9(10)V99) | n/a | `BigDecimal` | Sign zoned to two-decimal `BigDecimal`. |
| `account.creditLimit` | `ACCT-CREDIT-LIMIT` (S9(10)V99) | n/a | `BigDecimal` | |
| `account.status` | `ACCT-ACTIVE-STATUS` (X(01)) | n/a | `Y` → ACTIVE, anything else → INACTIVE | |
| `account.source` | constant `NATIONWIDE` | n/a | — | |

## Accounts — current (Virgin Money primary)

| Canonical | Nationwide source | Virgin Money source | Transformation | Notes |
|---|---|---|---|---|
| `account.accountId` | n/a (no current account on NW card stack) | `PrimaryAccount.id` (Long) | String of Long | The CardDemo stack predates current accounts — Nationwide current accounts live on a separate platform not represented in this demo. |
| `account.type` | n/a | constant `CURRENT` | — | |
| `account.displayNumber` | n/a | `PrimaryAccount.accountNumber` (int) | String | Virgin Money's `int` is **8 digits**; flag this as fragile — production should use `String` to preserve leading zeros. |
| `account.balance` | n/a | `PrimaryAccount.accountBalance` (BigDecimal) | — | |
| `account.creditLimit` | n/a | n/a | always `null` | Current accounts don't carry a credit limit. |
| `account.status` | n/a | derived from `User.enabled` | `true` → ACTIVE, `false` → INACTIVE | Virgin Money doesn't carry per-account status. |
| `account.source` | n/a | constant `VIRGIN_MONEY` | — | |

## Accounts — savings

Same shape as current, sourced from `SavingsAccount`. The unified API exposes both as separate `Account` entries with `type=SAVINGS`.

## Transactions — out of scope for this scene's adapter

| Canonical | Nationwide source | Virgin Money source | Notes |
|---|---|---|---|
| `txn.id` | `TRAN-ID` (X(16)) | `PrimaryTransaction.id` (Long) | |
| `txn.amount` | `TRAN-AMT` (S9(09)V99) | `PrimaryTransaction.amount` (**double**) | **Critical issue.** Virgin Money stores transaction amounts as a `double`. This is a *bug* in the upstream model — money must not be `double`. We flag it and recommend changing the source field to `BigDecimal` before any production unification. |
| `txn.timestamp` | `TRAN-PROC-TS` (X(26)) | `PrimaryTransaction.date` (`java.util.Date`) | Migrate Virgin Money to `Instant` long term. |
| `txn.description` | `TRAN-DESC` (X(100)) | `PrimaryTransaction.description` | |

Transactions are *intentionally out of scope* for the unified read API in this demo. Members get transaction history from their estate's native channel until the merchant transaction stream is unified. This is the right phasing — Devin recommends *unifying customer/account first*, then *transactions* as a separate workstream.

## Findings & open questions for the data owner

1. **Canonical member identity.** Nationwide and Virgin Money have completely independent identifier spaces. The demo uses a prefix scheme as a routing key; production needs a canonical-member registry. **Action:** scope a separate workstream.
2. **Email channel.** Email is unique on Virgin Money's `User`; on Nationwide it lives in a CIF extension table not in CardDemo. **Action:** confirm with the digital-channel product owner that we'll source the canonical email from Virgin Money for shared members.
3. **Address.** Nationwide has a structured address on `CUSTOMER-RECORD`; Virgin Money's authoritative address is in branch CRM. **Action:** introduce a cached projection of the Virgin Money address for the unified read API.
4. **Transaction amount precision.** Virgin Money stores `PrimaryTransaction.amount` as a `double`. **Action:** raise as a defect against the Virgin Money platform team; fix in the source model before unifying transactions.
5. **Account number type.** Virgin Money stores `accountNumber` as an `int`. UK account numbers are 8 digits and can technically have leading zeros (rare but possible). **Action:** change source field to `String` before production unification.
6. **DoB.** Held on Nationwide, not on Virgin Money digital user. **Action:** decide whether the canonical Member exposes DoB at all in the unified read API (consumer-duty implications).
7. **Status semantics.** `Y/N` on Nationwide is per-account; Virgin Money's `enabled` is per-user, not per-account. **Action:** raise with Virgin Money product whether account-level status is needed.

These are exactly the open questions an integration architect would identify in a 3-month manual mapping exercise. Devin produced them in the same pass as the field-level mapping above.
