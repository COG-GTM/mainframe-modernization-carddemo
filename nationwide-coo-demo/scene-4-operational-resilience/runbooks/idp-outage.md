# Runbook — Identity Provider Outage

**Affected IBS:** IBS 2 (Customer Account Management), IBS 3 (Savings & Transfers).

**Headline.** The cross-estate Identity Provider is unavailable or returning elevated error rates on authentication. Members cannot sign in to mobile, web, or contact-centre (where the desktop also depends on the IdP).

## Severity & detection

| Severity | Detection |
|---|---|
| **P1** | Pages from: (a) channel-side authentication-failure rate >5% over 1 minute, (b) IdP status page degraded, (c) IdP probe failure from the synthetic monitor. |

Time-to-detect SLO: **2 minutes**.

## Impact tolerance & escalation clock

| Elapsed time | Action |
|---|---|
| **0–15 min** | Duty IM verifies; contacts IdP vendor support. |
| **30 min** | Escalate to Director of Digital. Begin coordination with Director of Virgin Money Operations. |
| **2 hr** | **Impact-tolerance breach for IBS 2 and IBS 3** (assuming both estates rely on the same IdP — confirmed dependency in `../dependency-analysis/cross-estate-dependencies.md`). Notify PRA and FCA. |

## Immediate containment

1. **Verify the scope.** Is it both estates' channels or one? If both, this is a cross-estate concentration event — a P0 candidate.
2. **Lengthen session windows** on already-authenticated users where the channel supports it (so members already signed in stay signed in for the duration of the outage).
3. **Switch authentication probes** to non-IdP-dependent health checks so the rest of the platform's monitoring isn't drowned out.

## Recovery procedure

Recovery is largely vendor-led:

1. Maintain comms with the IdP vendor; collect their ETA.
2. When the vendor reports restoration, sample a controlled set of sign-ins before opening the channel fully.
3. Watch the channel-side authentication success rate; resume traffic shaping if it spikes errors.
4. Coordinate with the contact-centre platform team if their desktop also depends on the IdP — they may have a fallback flow.

## Customer comms

| Channel | When | Template |
|---|---|---|
| Mobile / web sign-in screen | Immediate | "Sign-in is temporarily unavailable. We're working to restore it." |
| Mobile / web banner (already signed in) | At 30 min | "Sign-in is temporarily unavailable for new sessions. Your existing session is fine." |
| Contact-centre script | At 30 min | "Yes, we're aware. We're working with our identity vendor to restore service." |

## Regulatory notification triggers

- **PRA.** At 2 hr (IBS 2 and IBS 3 both breach).
- **FCA.** In parallel — consumer-duty impact for >100k members.

## Post-incident review

- IdP vendor's RCA — request within 5 working days.
- Whether session-window extension was effective (this is the lever that buys time during an IdP outage).
- The OR committee's standing item on cross-estate IdP concentration risk.

---

**Service Owner:** Director of Digital
**IdP Vendor:** *redacted in demo*
**Last drafted:** Devin, [date]
