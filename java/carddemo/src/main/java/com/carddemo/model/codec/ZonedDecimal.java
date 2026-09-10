package com.carddemo.model.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Zoned decimal (COBOL {@code DISPLAY}) conversions, including the trailing overpunched sign used
 * by {@code PIC S9(n)V9(m)} fields in the CardDemo data files.
 */
public final class ZonedDecimal {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimal() {
    }

    /** Decodes a signed zoned decimal, applying {@code scale} implied decimal positions. */
    public static BigDecimal decodeSigned(String raw, int scale) {
        String digits = raw.trim();
        if (digits.isEmpty()) {
            return null;
        }
        boolean negative = false;
        char last = digits.charAt(digits.length() - 1);
        int overpunchPositive = POSITIVE_OVERPUNCH.indexOf(last);
        int overpunchNegative = NEGATIVE_OVERPUNCH.indexOf(last);
        if (overpunchPositive >= 0) {
            digits = digits.substring(0, digits.length() - 1) + overpunchPositive;
        } else if (overpunchNegative >= 0) {
            negative = true;
            digits = digits.substring(0, digits.length() - 1) + overpunchNegative;
        } else if (last == '-' || last == '+') {
            negative = last == '-';
            digits = digits.substring(0, digits.length() - 1);
        }
        digits = digits.replace(' ', '0');
        BigDecimal value = new BigDecimal(new BigInteger(digits), scale);
        return negative ? value.negate() : value;
    }

    /**
     * Encodes a signed zoned decimal into {@code length} character positions, overpunching the sign
     * onto the last digit.
     */
    public static String encodeSigned(BigDecimal value, int length, int scale) {
        BigDecimal scaled = (value == null ? BigDecimal.ZERO : value).setScale(scale, RoundingMode.HALF_UP);
        boolean negative = scaled.signum() < 0;
        String digits = scaled.abs().unscaledValue().toString();
        if (digits.length() > length) {
            throw new IllegalArgumentException("Value " + value + " does not fit in PIC S9(" + (length - scale)
                    + ")V9(" + scale + ")");
        }
        digits = "0".repeat(length - digits.length()) + digits;
        int lastDigit = digits.charAt(length - 1) - '0';
        char overpunch = (negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH).charAt(lastDigit);
        return digits.substring(0, length - 1) + overpunch;
    }

    /** Decodes an unsigned zoned decimal; blank and low-value fields decode to {@code null}. */
    public static Long decodeUnsigned(String raw) {
        String digits = raw.trim().replace('\u0000', ' ').trim();
        if (digits.isEmpty()) {
            return null;
        }
        return Long.parseLong(digits);
    }

    /** Encodes an unsigned zoned decimal, zero filled to {@code length} positions. */
    public static String encodeUnsigned(Long value, int length) {
        String digits = String.valueOf(value == null ? 0L : value);
        if (digits.length() > length) {
            throw new IllegalArgumentException("Value " + value + " does not fit in PIC 9(" + length + ")");
        }
        return "0".repeat(length - digits.length()) + digits;
    }
}
