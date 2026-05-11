package uk.co.nationwide.cards.posting.service;

/**
 * Modernized form of <code>WS-VALIDATION-FAIL-REASON</code> from CBTRN02C.
 * Each enum constant corresponds to one of the failure codes the COBOL
 * paragraph <code>1500-VALIDATE-TRAN</code> assigns:
 *
 * <pre>
 *   COBOL code (WS-VALIDATION-FAIL-REASON)   Enum constant
 *   --------------------------------------   ----------------------------
 *   100  Invalid card number                  CARD_NOT_FOUND_IN_XREF
 *   101  Account not found                    ACCOUNT_NOT_FOUND
 *   102  Account inactive                     ACCOUNT_INACTIVE
 *   103  Card inactive                        CARD_INACTIVE
 *   104  Card expired                         CARD_EXPIRED
 *   105  Exceeds credit limit                 EXCEEDS_CREDIT_LIMIT
 * </pre>
 */
public enum ValidationFailure {
    NONE("", 0),
    CARD_NOT_FOUND_IN_XREF("Invalid card number — not found in card cross-reference", 100),
    ACCOUNT_NOT_FOUND("Account not found", 101),
    ACCOUNT_INACTIVE("Account inactive", 102),
    CARD_INACTIVE("Card inactive", 103),
    CARD_EXPIRED("Card expired", 104),
    EXCEEDS_CREDIT_LIMIT("Transaction exceeds available credit limit", 105);

    private final String description;
    private final int legacyCode;

    ValidationFailure(String description, int legacyCode) {
        this.description = description;
        this.legacyCode = legacyCode;
    }

    public String description() { return description; }
    public int legacyCode() { return legacyCode; }
}
