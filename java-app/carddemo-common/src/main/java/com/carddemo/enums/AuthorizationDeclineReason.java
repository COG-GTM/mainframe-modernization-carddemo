package com.carddemo.enums;

/**
 * Authorization decline reason codes - mirrors COBOL authorization decision logic.
 */
public enum AuthorizationDeclineReason {
    INSUFFICIENT_FUNDS('I', "Insufficient funds"),
    CARD_NOT_ACTIVE('A', "Card not active"),
    ACCOUNT_CLOSED('C', "Account closed"),
    CARD_FRAUD('F', "Card flagged for fraud"),
    MERCHANT_FRAUD('M', "Merchant flagged for fraud");

    private final char code;
    private final String description;

    AuthorizationDeclineReason(char code, String description) {
        this.code = code;
        this.description = description;
    }

    public char getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AuthorizationDeclineReason fromCode(char code) {
        for (AuthorizationDeclineReason reason : values()) {
            if (reason.code == code) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown decline reason code: " + code);
    }
}
