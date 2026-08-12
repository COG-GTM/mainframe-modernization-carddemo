package com.carddemo.batch.statement;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * COBOL program: CBSTM03A — helpers reproducing the COBOL data-movement rules used by the
 * statement layout (copybook COSTM01 / CUSTREC record fields).
 *
 * <p>Covers fixed-width MOVE truncation/padding, {@code STRING ... DELIMITED BY} transfers and
 * the two numeric-edited pictures of the statement: {@code PIC 9(9).99-} (ST-CURR-BAL) and
 * {@code PIC Z(9).99-} (ST-TRANAMT, ST-TOTAL-TRAMT).
 */
final class StatementFormatter {

    /** Number of digit positions in the integer part of both edited amount pictures. */
    private static final int AMOUNT_INTEGER_DIGITS = 9;

    private StatementFormatter() {
    }

    /** MOVE of an alphanumeric value to a {@code PIC X(length)} field: truncate or space-pad. */
    static String alpha(String value, int length) {
        String text = value == null ? "" : value;
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

    /** MOVE of a {@code PIC 9(digits)} display item into a {@code PIC X(length)} field. */
    static String numericToAlpha(Number value, int digits, int length) {
        long number = value == null ? 0L : value.longValue();
        return alpha(String.format("%0" + digits + "d", Math.abs(number)), length);
    }

    /** A run of {@code count} spaces. */
    static String spaces(int count) {
        return " ".repeat(count);
    }

    /** A run of {@code count} copies of {@code filler}, as produced by {@code VALUE ALL}. */
    static String fill(char filler, int count) {
        return String.valueOf(filler).repeat(count);
    }

    /**
     * The part of a sending field transferred by {@code STRING ... DELIMITED BY delimiter}:
     * everything up to (excluding) the first occurrence of the delimiter, or the whole field
     * when the delimiter is absent.
     */
    static String delimited(String value, int fieldLength, String delimiter) {
        String field = alpha(value, fieldLength);
        int index = field.indexOf(delimiter);
        return index < 0 ? field : field.substring(0, index);
    }

    /**
     * Edits an amount as {@code PIC 9(9).99-}: nine zero-filled integer digits, the two
     * decimals, then '-' when negative and a space otherwise. High-order digits beyond nine
     * are truncated, exactly as the COBOL MOVE does.
     */
    static String editedZeroFilled(BigDecimal amount) {
        Digits digits = Digits.of(amount);
        return String.format("%0" + AMOUNT_INTEGER_DIGITS + "d", digits.integerPart)
                + "." + String.format("%02d", digits.fraction) + digits.signCharacter;
    }

    /**
     * Edits an amount as {@code PIC Z(9).99-}: leading zeros of the integer part are replaced
     * by spaces (an integer part of zero prints as nine spaces), followed by the two decimals
     * and the trailing sign position.
     */
    static String editedSuppressed(BigDecimal amount) {
        Digits digits = Digits.of(amount);
        String integerPart = digits.integerPart == 0
                ? spaces(AMOUNT_INTEGER_DIGITS)
                : String.format("%" + AMOUNT_INTEGER_DIGITS + "d", digits.integerPart);
        return integerPart + "." + String.format("%02d", digits.fraction) + digits.signCharacter;
    }

    /** The absolute integer/fraction digits and the trailing sign position of an edited amount. */
    private static final class Digits {

        private static final long INTEGER_MODULUS = 1_000_000_000L;

        private final long integerPart;
        private final long fraction;
        private final String signCharacter;

        private Digits(long integerPart, long fraction, String signCharacter) {
            this.integerPart = integerPart;
            this.fraction = fraction;
            this.signCharacter = signCharacter;
        }

        static Digits of(BigDecimal amount) {
            BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
            BigDecimal scaled = value.setScale(2, RoundingMode.DOWN).abs();
            long unscaled = scaled.unscaledValue().longValue();
            return new Digits(
                    (unscaled / 100) % INTEGER_MODULUS,
                    unscaled % 100,
                    value.signum() < 0 ? "-" : " ");
        }
    }
}
