package com.cog.carddemo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Parsing and formatting of COBOL zoned decimal (USAGE DISPLAY) fields as
 * {@link BigDecimal}. Monetary fields never use binary floating point.
 *
 * <p>Signs are carried in the last character of the field. Data exported from
 * EBCDIC uses the overpunch characters {@code {ABCDEFGHI} / {@code }JKLMNOPQR},
 * while the ASCII runtime convention (GnuCOBOL default) keeps positive values as
 * plain digits and only overpunches negatives. Both are accepted on input;
 * {@link #formatZoned} writes the ASCII convention.
 */
public final class CobolDecimal {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolDecimal() {
    }

    /**
     * Parse a zoned decimal field.
     *
     * @param text  the field contents, one character per digit
     * @param scale number of implied fractional digits (the {@code V} position)
     */
    public static BigDecimal parseZoned(String text, int scale) {
        Objects.requireNonNull(text, "text");
        if (text.isEmpty()) {
            throw new IllegalArgumentException("empty zoned decimal field");
        }
        if (scale < 0 || scale >= text.length()) {
            throw new IllegalArgumentException("invalid scale " + scale + " for field of length " + text.length());
        }

        char sign = text.charAt(text.length() - 1);
        String leading = text.substring(0, text.length() - 1);
        if (!isAllDigits(leading)) {
            throw new IllegalArgumentException("non-digit characters in zoned decimal field: " + text);
        }

        int lastDigit;
        boolean negative;
        int overpunched = POSITIVE_OVERPUNCH.indexOf(sign);
        if (overpunched >= 0) {
            lastDigit = overpunched;
            negative = false;
        } else {
            overpunched = NEGATIVE_OVERPUNCH.indexOf(sign);
            if (overpunched >= 0) {
                lastDigit = overpunched;
                negative = true;
            } else if (sign >= '0' && sign <= '9') {
                lastDigit = sign - '0';
                negative = false;
            } else {
                throw new IllegalArgumentException("invalid zoned decimal sign character: '" + sign + "'");
            }
        }

        BigInteger unscaled = new BigInteger(leading + lastDigit);
        return new BigDecimal(negative ? unscaled.negate() : unscaled, scale);
    }

    /**
     * Format a value as a zoned decimal field using the ASCII sign convention:
     * positive values are plain digits, negative values overpunch the last digit.
     *
     * @param value     the value
     * @param precision total number of digits, fractional digits included
     * @param scale     number of implied fractional digits
     */
    public static String formatZoned(BigDecimal value, int precision, int scale) {
        String digits = digitsOf(value, precision, scale);
        if (value.signum() >= 0) {
            return digits;
        }
        int lastDigit = digits.charAt(precision - 1) - '0';
        return digits.substring(0, precision - 1) + NEGATIVE_OVERPUNCH.charAt(lastDigit);
    }

    /**
     * Render a value the way {@code DISPLAY} renders a signed zoned field: an
     * explicit sign, the integer digits zero padded, then the decimal point.
     *
     * @param value     the value
     * @param precision total number of digits, fractional digits included
     * @param scale     number of implied fractional digits
     */
    public static String formatDisplay(BigDecimal value, int precision, int scale) {
        String digits = digitsOf(value, precision, scale);
        return (value.signum() < 0 ? "-" : "+")
                + digits.substring(0, precision - scale)
                + (scale > 0 ? "." + digits.substring(precision - scale) : "");
    }

    private static String digitsOf(BigDecimal value, int precision, int scale) {
        Objects.requireNonNull(value, "value");
        if (scale < 0 || precision <= scale) {
            throw new IllegalArgumentException("invalid precision/scale: " + precision + "/" + scale);
        }

        BigDecimal scaled;
        try {
            scaled = value.setScale(scale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "value has more than " + scale + " decimal positions: " + value, e);
        }

        String digits = scaled.abs().unscaledValue().toString();
        if (digits.length() > precision) {
            throw new IllegalArgumentException("value does not fit in " + precision + " digits: " + value);
        }
        return "0".repeat(precision - digits.length()) + digits;
    }

    private static boolean isAllDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
