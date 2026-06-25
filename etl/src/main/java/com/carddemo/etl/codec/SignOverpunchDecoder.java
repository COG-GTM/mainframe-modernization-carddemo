package com.carddemo.etl.codec;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Decodes COBOL zoned-decimal (signed {@code DISPLAY}) numerics that carry their sign as an
 * "overpunch" in the least-significant (trailing) digit byte.
 *
 * <p>CardDemo's {@code PIC S9(n)V99} fields are stored as plain digit characters for every
 * position except the last, where the digit and sign are combined into a single overpunch
 * character. The mapping is identical whether the source is the ASCII seed file or an EBCDIC
 * unload decoded through {@code Cp037} (the EBCDIC zoned bytes {@code 0xC0..0xC9 / 0xD0..0xD9}
 * decode to exactly these characters):
 *
 * <pre>
 *   positive: '{'=0 'A'=1 'B'=2 'C'=3 'D'=4 'E'=5 'F'=6 'G'=7 'H'=8 'I'=9
 *   negative: '}'=0 'J'=1 'K'=2 'L'=3 'M'=4 'N'=5 'O'=6 'P'=7 'Q'=8 'R'=9
 * </pre>
 *
 * A plain digit {@code '0'..'9'} in the trailing position is treated as unsigned (positive).
 */
public final class SignOverpunchDecoder {

    private SignOverpunchDecoder() {
    }

    /**
     * Decodes a zoned-decimal field into a {@link BigDecimal}, applying the implied decimal point.
     *
     * @param field the raw field characters (each position is one digit; the last may be overpunched)
     * @param scale number of implied decimal places (e.g. {@code 2} for {@code V99})
     * @return the signed decimal value
     * @throws NumberFormatException if the field contains a non-digit in a leading position or an
     *                               unrecognised trailing overpunch character
     */
    public static BigDecimal decode(String field, int scale) {
        if (field == null || field.isEmpty()) {
            throw new NumberFormatException("Zoned-decimal field is empty");
        }
        int len = field.length();
        StringBuilder digits = new StringBuilder(len);
        boolean negative = false;

        for (int i = 0; i < len; i++) {
            char c = field.charAt(i);
            boolean last = (i == len - 1);
            if (c >= '0' && c <= '9') {
                digits.append(c);
            } else if (last) {
                Overpunch op = decodeOverpunch(c);
                negative = op.negative;
                digits.append((char) ('0' + op.digit));
            } else {
                throw new NumberFormatException(
                        "Non-digit '" + c + "' at position " + i + " in zoned field \"" + field + "\"");
            }
        }

        BigInteger unscaled = new BigInteger(digits.toString());
        if (negative) {
            unscaled = unscaled.negate();
        }
        return new BigDecimal(unscaled, scale);
    }

    private static Overpunch decodeOverpunch(char c) {
        switch (c) {
            case '{': return new Overpunch(0, false);
            case 'A': return new Overpunch(1, false);
            case 'B': return new Overpunch(2, false);
            case 'C': return new Overpunch(3, false);
            case 'D': return new Overpunch(4, false);
            case 'E': return new Overpunch(5, false);
            case 'F': return new Overpunch(6, false);
            case 'G': return new Overpunch(7, false);
            case 'H': return new Overpunch(8, false);
            case 'I': return new Overpunch(9, false);
            case '}': return new Overpunch(0, true);
            case 'J': return new Overpunch(1, true);
            case 'K': return new Overpunch(2, true);
            case 'L': return new Overpunch(3, true);
            case 'M': return new Overpunch(4, true);
            case 'N': return new Overpunch(5, true);
            case 'O': return new Overpunch(6, true);
            case 'P': return new Overpunch(7, true);
            case 'Q': return new Overpunch(8, true);
            case 'R': return new Overpunch(9, true);
            default:
                throw new NumberFormatException("Unrecognised sign-overpunch character '" + c + "'");
        }
    }

    private static final class Overpunch {
        final int digit;
        final boolean negative;

        Overpunch(int digit, boolean negative) {
            this.digit = digit;
            this.negative = negative;
        }
    }
}
