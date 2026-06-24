package com.carddemo.account.parser;

import java.math.BigDecimal;

/**
 * Decodes a COBOL zoned-decimal (display) numeric with a trailing sign overpunch,
 * as written by CardDemo's ASCII seed files for {@code PIC S9(n)V99} fields.
 *
 * <p>In a signed zoned-decimal field the sign is folded into the last digit. The
 * ASCII seed data uses the IBM overpunch convention:
 * <pre>
 *   positive last digit:  { A B C D E F G H I  ->  0 1 2 3 4 5 6 7 8 9
 *   negative last digit:  } J K L M N O P Q R  ->  0 1 2 3 4 5 6 7 8 9
 * </pre>
 * A plain digit ('0'-'9') in the last position is treated as positive.
 */
public final class ZonedDecimal {

    private ZonedDecimal() {
    }

    /**
     * Decode a zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw          the raw fixed-width field (e.g. {@code "00000001940{"})
     * @param decimalDigits number of implied decimal places (the {@code V99} part)
     */
    public static BigDecimal decode(String raw, int decimalDigits) {
        String field = raw.trim();
        if (field.isEmpty()) {
            return BigDecimal.ZERO.setScale(decimalDigits);
        }

        char last = field.charAt(field.length() - 1);
        String leadingDigits = field.substring(0, field.length() - 1);

        boolean negative = false;
        char lastDigit;
        switch (last) {
            case '{' -> lastDigit = '0';
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' -> lastDigit = (char) ('1' + (last - 'A'));
            case '}' -> { negative = true; lastDigit = '0'; }
            case 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R' -> { negative = true; lastDigit = (char) ('1' + (last - 'J')); }
            default -> {
                if (last >= '0' && last <= '9') {
                    lastDigit = last;
                } else {
                    throw new IllegalArgumentException("Invalid zoned-decimal sign overpunch: '" + last + "' in \"" + raw + "\"");
                }
            }
        }

        String digits = leadingDigits + lastDigit;
        if (!digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Non-numeric zoned-decimal field: \"" + raw + "\"");
        }

        BigDecimal unscaled = new BigDecimal(new java.math.BigInteger(digits))
                .movePointLeft(decimalDigits)
                .setScale(decimalDigits);
        return negative ? unscaled.negate() : unscaled;
    }
}
