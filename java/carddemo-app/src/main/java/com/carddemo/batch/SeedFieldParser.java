package com.carddemo.batch;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Helpers for parsing fixed-width mainframe seed records into Java values, honouring the
 * COBOL→Java conventions: {@code PIC X(n)} → trimmed {@code String}; {@code PIC 9(n)} used
 * in arithmetic → {@code Integer}; signed zoned-decimal money ({@code PIC S9(n)V99}) →
 * {@link BigDecimal} with the exact implied scale.
 *
 * <p>The ASCII seed files store {@code PIC S9..V99} as USAGE DISPLAY (zoned decimal) with a
 * <em>trailing</em> sign over-punch on the last byte. The over-punch encodes both the units
 * digit and the sign: {@code '{'}=+0 … {@code 'I'}=+9, {@code '}'}=-0 … {@code 'R'}=-9.</p>
 */
public final class SeedFieldParser {

    private SeedFieldParser() {
    }

    /** Extract a fixed-width slice, padding with spaces if the line is short. */
    public static String slice(String line, int start, int length) {
        if (line == null) {
            return "";
        }
        int end = start + length;
        if (start >= line.length()) {
            return "";
        }
        return line.substring(start, Math.min(end, line.length()));
    }

    /** {@code PIC X(n)} → trimmed String, or {@code null} when blank. */
    public static String str(String line, int start, int length) {
        String s = slice(line, start, length).trim();
        return s.isEmpty() ? null : s;
    }

    /** Fixed-width raw slice without trimming (preserves leading zeros / exact width). */
    public static String id(String line, int start, int length) {
        String s = slice(line, start, length).trim();
        return s.isEmpty() ? null : s;
    }

    /** {@code PIC 9(n)} used numerically → Integer. */
    public static Integer integer(String line, int start, int length) {
        String s = slice(line, start, length).trim();
        if (s.isEmpty()) {
            return null;
        }
        return Integer.valueOf(s);
    }

    /**
     * Signed zoned-decimal ({@code PIC S9(m)V(scale)}, trailing sign over-punch) → BigDecimal
     * with the given scale.
     */
    public static BigDecimal signedDecimal(String line, int start, int length, int scale) {
        String raw = slice(line, start, length).trim();
        if (raw.isEmpty()) {
            return null;
        }
        char last = raw.charAt(raw.length() - 1);
        String leading = raw.substring(0, raw.length() - 1);
        int sign = 1;
        char digit;
        switch (last) {
            case '{' -> { digit = '0'; }
            case 'A' -> { digit = '1'; }
            case 'B' -> { digit = '2'; }
            case 'C' -> { digit = '3'; }
            case 'D' -> { digit = '4'; }
            case 'E' -> { digit = '5'; }
            case 'F' -> { digit = '6'; }
            case 'G' -> { digit = '7'; }
            case 'H' -> { digit = '8'; }
            case 'I' -> { digit = '9'; }
            case '}' -> { digit = '0'; sign = -1; }
            case 'J' -> { digit = '1'; sign = -1; }
            case 'K' -> { digit = '2'; sign = -1; }
            case 'L' -> { digit = '3'; sign = -1; }
            case 'M' -> { digit = '4'; sign = -1; }
            case 'N' -> { digit = '5'; sign = -1; }
            case 'O' -> { digit = '6'; sign = -1; }
            case 'P' -> { digit = '7'; sign = -1; }
            case 'Q' -> { digit = '8'; sign = -1; }
            case 'R' -> { digit = '9'; sign = -1; }
            default -> {
                if (last >= '0' && last <= '9') {
                    digit = last;
                } else {
                    throw new IllegalArgumentException("Unrecognized zoned-decimal byte: '" + last + "' in \"" + raw + "\"");
                }
            }
        }
        BigInteger unscaled = new BigInteger(leading + digit);
        if (sign < 0) {
            unscaled = unscaled.negate();
        }
        return new BigDecimal(unscaled, scale);
    }
}
