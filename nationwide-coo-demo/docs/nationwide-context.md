# Nationwide & Virgin Money — Context for the Demo

## Nationwide Building Society at a glance

- **Founded** 1846 (as the Co-operative Permanent Building Society); current name since 1970.
- **Status** Mutual — owned by its ~16 million members. No external shareholders. Profits are retained or returned to members.
- **Scale** ~£260bn total assets, ~18,000 colleagues, ~600 branches — the UK's largest building society and second-largest mortgage and savings provider after Lloyds.
- **Brand promise** "Full service modern mutual" — current account, mortgage, savings, credit card, personal loan, with a 24/7 digital experience expected to match the high-street banks.
- **Members, not customers.** Anyone who holds a qualifying current account, mortgage, or savings product is a member with a vote at the AGM. Demo copy refers to *members* where appropriate.

## The Virgin Money acquisition (2024)

| | |
|---|---|
| Deal announced | March 2024 |
| Completion | 1 October 2024 |
| Price | £2.9bn (recommended cash offer) |
| Brand strategy | Virgin Money brand retained for ~6 years; technology and back office to be integrated |
| Adds to Nationwide | ~£90bn customer deposits, ~£70bn lending balances, ~6.6m customers, business banking capability, full credit card book |

The integration is the **largest UK financial-services M&A integration in a decade**. Nationwide's technology team now owns two distinct estates running in parallel: a mainframe-anchored Nationwide stack and a more recent (but heterogeneous) Virgin Money stack.

## The two technology estates

### Nationwide

- **Core banking** running on IBM Z mainframe; CICS/COBOL for online transactions, JCL/COBOL for nightly batch, VSAM KSDS for master files (accounts, customers, cards, cross-references).
- Hundreds of legacy components written between the late 1980s and the early 2010s. Test coverage is patchy, documentation is older than some of the engineers, and the people who wrote the original code are increasingly retired.
- Modern Java/Spring services exist for digital channels (mobile, web), card switch integration, and open-banking PSD2/PSR endpoints — but they sit *on top of* the mainframe of record.
- Member-data master is the Customer Information File (CIF) on VSAM, keyed by a 9-digit `CUST-ID`.

### Virgin Money (legacy CYBG estate)

- Heterogeneous: components inherited from Clydesdale Bank, Yorkshire Bank, Virgin Money's pre-2018 stack, and B-by-Clydesdale's challenger build.
- Java/Spring Boot dominates the online banking layer. PostgreSQL is the default OLTP store. JPA/Hibernate entities are the source of truth for the digital channel.
- Customer master is on a JPA `User` entity, keyed by an auto-generated `userId` (Long).
- Card platform was outsourced to a third-party processor in 2019; cards are not represented in the core user model.

### Where they collide

| Concept | Nationwide | Virgin Money |
|---|---|---|
| Customer identity | `CUST-ID` (9-digit numeric on VSAM) | `userId` (Long, JPA-generated) |
| Card account | `ACCT-ID` (11-digit numeric on VSAM `ACCOUNT-RECORD`) | Not modelled |
| Current account | Modelled as a card account (CardDemo legacy) | `PrimaryAccount` entity, `accountNumber` (int) |
| Savings account | Not modelled in the card stack | `SavingsAccount` entity |
| Transaction | `TRAN-RECORD` (350-byte fixed VSAM, posted via `CBTRN02C` batch) | `PrimaryTransaction` / `SavingsTransaction` JPA entities |
| Address | Three 50-char lines on `CUSTOMER-RECORD` | Not stored on `User`; held in branch CRM |
| Status | `ACCT-ACTIVE-STATUS` (X(01)) | `enabled` (boolean) on `User` |

The two systems describe the same real-world thing (a member with money) in completely different shapes. Scene 3 walks through how Devin maps between them.

## What the COO actually worries about

Talking to a Nationwide-shaped COO, you'll consistently hear three concerns:

1. **Operational risk in the long tail.** The big modernization programmes (core banking platform, mortgage origination, mobile rebuild) are headline news. The COO is paid to worry about the dozens of components that *aren't* on those slides — the batch job nobody has touched since 2011, the BMS map that still drives a back-office function in Swindon, the IDCAMS definition that runs the cards reconciliation. When something breaks here, it's a service incident at 3am and a regulatory notification by lunchtime.
2. **Integration risk on Virgin Money.** Members expect one digital experience. Colleagues expect one CRM. The regulator expects one risk view. The technology answer "we'll migrate one stack onto the other over 5 years" is not credible to the COO; the answer "we'll keep both and integrate" needs proof the integration is real and tested.
3. **Operational resilience evidence.** PRA SS1/21 + FCA PS6/21 went fully effective March 2025. The COO is personally accountable for showing the regulator that every Important Business Service has a documented impact tolerance, a tested severe-but-plausible failure scenario, and an evidence trail. The board reviews this quarterly. The PRA reviews it on demand.

The Devin pitch lines up exactly with these three concerns. Each scene targets one of them.
