package com.carddemo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conversions for the numeric representations used by the legacy fixed width extracts.
 *
 * <p>Signed COBOL display fields ({@code PIC S9(n)V99}) carry their sign in the last byte as an
 * overpunch character: {@code {} and {@code A-I} denote a positive digit 0-9, {@code }} and
 * {@code J-R} denote a negative digit 0-9.
 */
public final class CobolNumbers {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolNumbers() {
    }

    /** Parses a zoned decimal field with {@code decimals} implied decimal positions. */
    public static BigDecimal parseSigned(String raw, int decimals) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        boolean negative = false;
        char last = value.charAt(value.length() - 1);
        int positive = POSITIVE_OVERPUNCH.indexOf(last);
        int signed = NEGATIVE_OVERPUNCH.indexOf(last);
        if (positive >= 0) {
            value = value.substring(0, value.length() - 1) + positive;
        } else if (signed >= 0) {
            negative = true;
            value = value.substring(0, value.length() - 1) + signed;
        } else if (last == '-' || last == '+') {
            negative = last == '-';
            value = value.substring(0, value.length() - 1);
        } else if (value.charAt(0) == '-' || value.charAt(0) == '+') {
            negative = value.charAt(0) == '-';
            value = value.substring(1);
        }
        value = value.replace(".", "").trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = new BigDecimal(new java.math.BigInteger(value), decimals);
        return negative ? result.negate() : result;
    }

    /** Parses an unsigned numeric display field, tolerating blanks. */
    public static Long parseLong(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        return Long.parseLong(value);
    }

    public static Integer parseInteger(String raw) {
        Long value = parseLong(raw);
        return value == null ? null : value.intValue();
    }

    /** Renders an amount the way the COBOL edited field {@code +99999999.99} does. */
    public static String formatSigned(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        return (value.signum() < 0 ? "-" : "+") + value.abs().toPlainString();
    }
}
