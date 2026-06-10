package com.carddemo.interestcalc.util;

import java.math.BigDecimal;

/**
 * Parses and formats COBOL signed zoned-decimal display fields (e.g. {@code PIC S9(09)V99})
 * as they appear in the ASCII exports under {@code app/data/ASCII/}.
 *
 * <p>The sign is overpunched on the last digit:
 * <ul>
 *   <li>positive: {@code '{'}=0, {@code 'A'}-{@code 'I'}=1-9</li>
 *   <li>negative: {@code '}'}=0, {@code 'J'}-{@code 'R'}=1-9</li>
 * </ul>
 */
public final class ZonedDecimal {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimal() {
    }

    /**
     * @param text  the raw fixed-width field (digits with sign overpunch on the last char)
     * @param scale number of implied decimal digits (the {@code V99} part)
     */
    public static BigDecimal parse(String text, int scale) {
        char last = text.charAt(text.length() - 1);
        boolean negative = false;
        int lastDigit;
        int posIdx = POSITIVE_OVERPUNCH.indexOf(last);
        int negIdx = NEGATIVE_OVERPUNCH.indexOf(last);
        if (posIdx >= 0) {
            lastDigit = posIdx;
        } else if (negIdx >= 0) {
            lastDigit = negIdx;
            negative = true;
        } else if (Character.isDigit(last)) {
            lastDigit = last - '0';
        } else {
            throw new IllegalArgumentException("Invalid zoned decimal sign character: '" + last + "' in \"" + text + "\"");
        }
        String digits = text.substring(0, text.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(scale).setScale(scale);
        return negative ? value.negate() : value;
    }

    /**
     * Formats a value as a fixed-width zoned decimal with sign overpunch, the inverse
     * of {@link #parse(String, int)}.
     *
     * @param value       the value to format (will be scaled to {@code scale})
     * @param totalDigits total number of digit positions (integer + decimal)
     * @param scale       number of implied decimal digits
     */
    public static String format(BigDecimal value, int totalDigits, int scale) {
        BigDecimal scaled = value.setScale(scale);
        boolean negative = scaled.signum() < 0;
        String digits = scaled.abs().movePointRight(scale).toBigIntegerExact().toString();
        if (digits.length() > totalDigits) {
            throw new IllegalArgumentException("Value " + value + " does not fit in " + totalDigits + " digits");
        }
        digits = "0".repeat(totalDigits - digits.length()) + digits;
        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunch = negative ? NEGATIVE_OVERPUNCH.charAt(lastDigit) : POSITIVE_OVERPUNCH.charAt(lastDigit);
        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
