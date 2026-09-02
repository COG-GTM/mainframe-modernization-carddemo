package com.carddemo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conversions between COBOL fixed-width field representations and Java types.
 *
 * <p>Signed numeric fields ({@code PIC S9(n)V99}) are stored in the sample data as zoned decimal
 * with the sign overpunched onto the final digit:
 * <pre>
 *   positive 0-9 -> { A B C D E F G H I
 *   negative 0-9 -> } J K L M N O P Q R
 * </pre>
 */
public final class CobolCodec {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolCodec() {
    }

    /** Decodes a zoned-decimal field with an implied decimal point {@code scale} digits from the right. */
    public static BigDecimal decodeSigned(String field, int scale) {
        String digits = field == null ? "" : field.trim();
        if (digits.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        boolean negative = false;
        char last = digits.charAt(digits.length() - 1);
        int overpunchPos = POSITIVE_OVERPUNCH.indexOf(last);
        if (overpunchPos >= 0) {
            digits = digits.substring(0, digits.length() - 1) + overpunchPos;
        } else {
            overpunchPos = NEGATIVE_OVERPUNCH.indexOf(last);
            if (overpunchPos >= 0) {
                negative = true;
                digits = digits.substring(0, digits.length() - 1) + overpunchPos;
            } else if (last == '-' || last == '+') {
                negative = last == '-';
                digits = digits.substring(0, digits.length() - 1);
            } else if (digits.charAt(0) == '-') {
                negative = true;
                digits = digits.substring(1);
            }
        }
        if (digits.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        BigDecimal value = new BigDecimal(new java.math.BigInteger(digits), scale);
        return negative ? value.negate() : value;
    }

    /**
     * Encodes a decimal back into a zoned-decimal field with overpunched sign.
     *
     * @param totalDigits total digit count of the picture clause, integer + decimal positions
     */
    public static String encodeSigned(BigDecimal value, int totalDigits, int scale) {
        BigDecimal scaled = (value == null ? BigDecimal.ZERO : value).setScale(scale, RoundingMode.HALF_UP);
        boolean negative = scaled.signum() < 0;
        String digits = scaled.abs().unscaledValue().toString();
        if (digits.length() > totalDigits) {
            throw new IllegalArgumentException("Value " + value + " does not fit in PIC S9(" + (totalDigits - scale)
                    + ")V9(" + scale + ")");
        }
        digits = "0".repeat(totalDigits - digits.length()) + digits;
        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunch = (negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH).charAt(lastDigit);
        return digits.substring(0, digits.length() - 1) + overpunch;
    }

    /** Decodes an unsigned zoned field ({@code PIC 9(n)V99}) with no sign overpunch. */
    public static BigDecimal decodeUnsigned(String field, int scale) {
        String digits = field == null ? "" : field.trim();
        return digits.isEmpty() ? BigDecimal.ZERO.setScale(scale)
                : new BigDecimal(new java.math.BigInteger(digits), scale);
    }

    /** Parses a {@code PIC 9(n)} field, tolerating blanks. */
    public static long decodeLong(String field) {
        String digits = field == null ? "" : field.trim();
        return digits.isEmpty() ? 0L : Long.parseLong(digits);
    }

    public static int decodeInt(String field) {
        return (int) decodeLong(field);
    }

    /** Renders a numeric value as a zero-padded fixed-width {@code PIC 9(n)} field. */
    public static String encodeNumeric(long value, int width) {
        return String.format("%0" + width + "d", value);
    }

    /** Renders text as a space-padded fixed-width {@code PIC X(n)} field, truncating when too long. */
    public static String encodeText(String value, int width) {
        String text = value == null ? "" : value;
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }

    /** Right-trims a {@code PIC X(n)} field, returning an empty string for all-blank fields. */
    public static String text(String field) {
        return field == null ? "" : field.stripTrailing();
    }

    /** Pads a record to the given logical record length so trailing FILLER can be sliced safely. */
    public static String padRecord(String record, int length) {
        String value = record == null ? "" : record;
        if (value.length() >= length) {
            return value;
        }
        return value + " ".repeat(length - value.length());
    }
}
