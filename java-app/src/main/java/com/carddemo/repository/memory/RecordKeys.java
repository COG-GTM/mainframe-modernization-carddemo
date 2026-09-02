package com.carddemo.repository.memory;

import com.carddemo.util.CobolCodec;

/**
 * Normalizes lookup keys to the fixed-width form used by the VSAM records, so callers may pass
 * either {@code "1"} or {@code "00000000001"} for an account id.
 */
public final class RecordKeys {

    private RecordKeys() {
    }

    public static String accountId(String accountId) {
        return pad(accountId, 11);
    }

    public static String customerId(String customerId) {
        return pad(customerId, 9);
    }

    public static String cardNumber(String cardNumber) {
        return pad(cardNumber, 16);
    }

    public static String transactionId(String transactionId) {
        return CobolCodec.encodeText(transactionId == null ? "" : transactionId.trim(), 16);
    }

    public static String userId(String userId) {
        return userId == null ? "" : userId.trim();
    }

    /** Left-pads numeric keys with zeros; leaves non-numeric keys blank-padded on the right. */
    private static String pad(String value, int width) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return " ".repeat(width);
        }
        if (trimmed.length() >= width) {
            return trimmed.substring(0, width);
        }
        if (trimmed.chars().allMatch(Character::isDigit)) {
            return "0".repeat(width - trimmed.length()) + trimmed;
        }
        return CobolCodec.encodeText(trimmed, width);
    }
}
