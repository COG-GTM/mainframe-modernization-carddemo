package uk.co.nationwide.unified.domain;

/**
 * Canonical member identifier.
 *
 * <p>Member IDs are prefixed so the routing layer can dispatch to the right
 * source-system adapter without an extra lookup:
 *
 * <ul>
 *   <li><code>nbs-NNNNNNNNN</code> — Nationwide customer (9-digit
 *       <code>CUST-ID</code> from the CardDemo VSAM <code>CUSTDAT</code>).</li>
 *   <li><code>vm-NNN...</code> — Virgin Money user (auto-generated Long
 *       <code>userId</code> from the JPA <code>User</code> entity).</li>
 * </ul>
 *
 * <p>This is the simplest correct scheme for a brownfield integration. In
 * production a separate canonical-member registry would mint a single
 * cross-estate identity. That registry is out of scope for this demo —
 * Devin's recommendation for it sits in
 * {@code data-mapping/data-mapping.md} under "open questions".
 */
public final class MemberId {

    public static final String NW_PREFIX = "nbs-";
    public static final String VM_PREFIX = "vm-";

    public static SourceSystem sourceOf(String memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is null");
        }
        if (memberId.startsWith(NW_PREFIX)) return SourceSystem.NATIONWIDE;
        if (memberId.startsWith(VM_PREFIX)) return SourceSystem.VIRGIN_MONEY;
        throw new IllegalArgumentException("memberId prefix not recognised: " + memberId);
    }

    public static Long nationwideCustId(String memberId) {
        require(memberId.startsWith(NW_PREFIX), "expected nbs- prefix: " + memberId);
        return Long.parseLong(memberId.substring(NW_PREFIX.length()));
    }

    public static Long virginMoneyUserId(String memberId) {
        require(memberId.startsWith(VM_PREFIX), "expected vm- prefix: " + memberId);
        return Long.parseLong(memberId.substring(VM_PREFIX.length()));
    }

    public static String forNationwide(Long custId) {
        return NW_PREFIX + String.format("%09d", custId);
    }

    public static String forVirginMoney(Long userId) {
        return VM_PREFIX + userId;
    }

    private static void require(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }

    private MemberId() { }
}
