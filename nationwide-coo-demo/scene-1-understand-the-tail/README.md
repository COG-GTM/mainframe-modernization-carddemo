# Scene 1 — Understand the Tail

**The COO question:** *"We have hundreds of legacy components in the card-processing tail. Nobody knows what half of them do. Where do we even start?"*

This scene shows what Devin produces when pointed at a real COBOL estate. The input is **30 COBOL programs and 28 copybooks** under `cobol-source/` — rebranded as the "Nationwide Legacy Card Processing System" (in reality the public AWS *CardDemo* reference application). The output is three artefacts under `outputs/`.

## The set-up

Devin was given one prompt:

> "Read every program and copybook in `cobol-source/`. Produce: (1) a dependency graph showing program-to-program calls and program-to-data-file relationships, (2) a modernization backlog scored by complexity, risk, and business value with rough effort estimates, (3) a risk report flagging programs with poor test coverage or missing documentation."

Wall-clock time to produce the three artefacts: **~12 minutes** on a Devin instance with no other context provided.

## The outputs

| Artefact | What it is | Audience |
|---|---|---|
| [`outputs/dependency-graph.md`](./outputs/dependency-graph.md) | Mermaid diagrams: program-to-program call graph and program-to-data dataset map | Modernization architects, integration team |
| [`outputs/modernization-backlog.md`](./outputs/modernization-backlog.md) | Per-program complexity score, risk score, effort estimate, suggested wave | Modernization PMO, capacity planning |
| [`outputs/complexity-report.md`](./outputs/complexity-report.md) | Risk-ranked land-mine list — programs with no tests, no docs, single-author history | COO, Operational Resilience, Internal Audit |

## What the COO sees

- **The estate has a shape.** Online programs cluster around BMS maps and the customer/account VSAM masters. Batch programs are a different cluster — they feed the masters from external files (`DALYTRAN.PS`) and produce statements (`CBSTM03*`). The cross-reference master (`CARDXREF`) is the single most-depended-upon dataset.
- **The estate has a few load-bearing components.** `CBTRN02C` (daily transaction posting) is a critical-path batch — every card transaction goes through it. `COSGN00C` (sign-on) is the single gateway for the online channel. Failure of either is a member-facing outage.
- **The estate has land mines.** Six of the thirty programs have *no test coverage*, *no inline documentation past the 30-line PROCEDURE-DIVISION banner*, and were last touched more than a decade ago. Touching them without further reverse-engineering is operational risk.

## How to read the outputs in the demo

When walking the room through this scene, open the three files in this order:

1. `dependency-graph.md` first — gives the audience a shape they can hold in their head.
2. `modernization-backlog.md` next — translates the shape into a delivery plan.
3. `complexity-report.md` last — names the specific risks the COO is going to want to see remediated *before* migration starts.

## Source attribution

The 30 COBOL programs and copybooks in `cobol-source/` are copies of the AWS [CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) reference application, licensed Apache 2.0. They are presented here under Nationwide-themed framing for the purpose of this sales demo. The Apache 2.0 `LICENSE` and `NOTICE` files from the source repo are preserved at the root of the carrying repo.
