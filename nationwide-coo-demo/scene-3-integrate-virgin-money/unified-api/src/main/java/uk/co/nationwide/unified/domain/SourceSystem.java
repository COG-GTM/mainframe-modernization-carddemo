package uk.co.nationwide.unified.domain;

/**
 * The estate that owns the underlying record.
 */
public enum SourceSystem {
    /** Nationwide legacy card platform (CardDemo VSAM). */
    NATIONWIDE,
    /** Virgin Money online-banking platform (Spring Boot + JPA). */
    VIRGIN_MONEY
}
