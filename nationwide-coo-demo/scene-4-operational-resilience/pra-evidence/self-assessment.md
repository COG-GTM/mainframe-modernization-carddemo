# Self-Assessment Document — PRA SS1/21 — Draft Template

> The self-assessment document required by PRA SS1/21 §6.1–§6.7. This is the **structure** of the document Nationwide's COO has to keep current and produce on request. Devin drafted the structure, headings, and starting content; the OR committee owns the substantive judgements at each step.

---

## 1. IBS Register

The firm's Important Business Services, with the rationale for each. See [`../ibs-mapping/ibs-catalogue.md`](../ibs-mapping/ibs-catalogue.md).

For each IBS:

- **Service description.**
- **External users and approximate count.**
- **Why disruption could cause intolerable harm.**
- **Service Owner and Board Sponsor.**
- **Review date and reviewer.**

The IBS register is reviewed at least annually and on any material change (acquisition, system retirement, new channel).

---

## 2. Impact Tolerances

The maximum tolerable period of disruption per IBS. See [`./impact-tolerances.md`](./impact-tolerances.md).

For each IBS:

- **Stated impact tolerance.**
- **Unit** (hours, days, business cycles).
- **The narrative justification** (consumer harm, regulator expectation, internal data).
- **Year-on-year change**, if any, with rationale.

---

## 3. Mapping IBS to Resources

The components, people, and third parties that support each IBS. See [`../dependency-analysis/cross-estate-dependencies.md`](../dependency-analysis/cross-estate-dependencies.md).

For each IBS:

- **Internal technology components.**
- **Process and people dependencies** (named roles).
- **Third parties** (with name, contract, criticality).
- **Single points of failure** with current mitigation.

---

## 4. Scenario Testing

Evidence the firm has tested whether the IBS can recover within tolerance under severe-but-plausible disruption.

For each tested scenario:

- **Scenario description** (what disruption).
- **Date tested.**
- **Method** (tabletop / partial-live / full-live).
- **Outcome** — did the firm stay within tolerance?
- **Lessons identified.**
- **Remediations and target dates.**

The scenarios in scope (per the runbooks in [`../runbooks/`](../runbooks/)):

- VSAM master file corruption.
- Postgres primary failover on the modernised card platform.
- Payments network (Visa) failure.
- Cross-estate IdP outage.

A scenario that isn't currently tested *must* be documented as a known gap, with a target test date.

---

## 5. Lessons Learned & Remediation

Operational resilience is a continuous process. This section records:

- **Incidents in the year**: what happened, IBS affected, time-to-recover vs tolerance.
- **Lessons identified**: what we now believe differently because of those incidents.
- **Remediations**: with owners and target dates.
- **Status of prior-year remediations.**

A remediation that has slipped its target date must be re-justified to the OR committee.

---

## 6. Governance

- **Board approval.** When the board last approved the IBS register and impact tolerances.
- **OR committee membership.** Standing membership of the committee.
- **COO accountability statement.** The COO's personal sign-off statement on the self-assessment.
- **Internal audit.** Date of last internal audit review of the self-assessment.

---

## 7. Self-Assessment Conclusion

The COO's signed statement, asserting that:

- The firm has identified its IBS.
- The firm has set impact tolerances and **can** meet them in severe-but-plausible disruption *or* has documented remediation plans where it cannot.
- The firm has scenario-tested its capability against each defined tolerance.
- The firm has a programme of continuous improvement.

The statement is **explicit about what the firm can and cannot do** under severe-but-plausible disruption. Per SS1/21, the regulator prefers an honest *"can't yet, here's the plan"* to a defensive overstatement.

---

## Appendices

- **Appendix A.** Glossary (PRA, FCA, PSR, IBS, RTO, RPO, SS1/21, PS6/21).
- **Appendix B.** Mapping to FCA PS6/21 paragraphs.
- **Appendix C.** Third-party register with criticality classification.
- **Appendix D.** Runbook library reference.

---

**Document owner:** COO
**Approval authority:** Board
**Review cadence:** Annual; sooner on material change
**Drafted by:** Devin
**Last drafted:** [date]
