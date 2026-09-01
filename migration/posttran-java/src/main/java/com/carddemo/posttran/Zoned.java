package com.carddemo.posttran;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Zoned-decimal (USAGE DISPLAY) codec for the record layouts in app/cpy.
 *
 * <p>Signed fields such as {@code ACCT-CURR-BAL PIC S9(10)V99} (app/cpy/CVACT01Y.cpy:7) carry the
 * sign as an overpunch in the low-order byte, the IBM default for DISPLAY without SIGN SEPARATE.
 * The delivered data keeps that convention after the EBCDIC-to-ASCII transliteration: '{'/'A'-'I'
 * for +0..+9 and '}'/'J'-'R' for -0..-9 (see the trailing '{' of ACCT-CASH-CREDIT-LIMIT in
 * app/data/ASCII/acctdata.txt).
 */
public final class Zoned {

    private Zoned() {
    }

    /** Decodes a signed zoned-decimal field with {@code scale} implied decimal digits. */
    public static BigDecimal readSigned(byte[] rec, int off, int len, int scale) {
        char[] digits = new char[len];
        boolean negative = false;
        for (int i = 0; i < len; i++) {
            char c = (char) (rec[off + i] & 0xFF);
            if (i == len - 1) {
                switch (c) {
                    case '{' -> digits[i] = '0';
                    case '}' -> { digits[i] = '0'; negative = true; }
                    default -> {
                        if (c >= 'A' && c <= 'I') {
                            digits[i] = (char) ('1' + (c - 'A'));
                        } else if (c >= 'J' && c <= 'R') {
                            digits[i] = (char) ('1' + (c - 'J'));
                            negative = true;
                        } else {
                            digits[i] = normalizeDigit(c);
                        }
                    }
                }
            } else {
                digits[i] = normalizeDigit(c);
            }
        }
        BigDecimal v = new BigDecimal(new java.math.BigInteger(new String(digits)), scale);
        return negative ? v.negate() : v;
    }

    /** Decodes an unsigned {@code PIC 9(n)} field. */
    public static BigDecimal readUnsigned(byte[] rec, int off, int len, int scale) {
        char[] digits = new char[len];
        for (int i = 0; i < len; i++) {
            digits[i] = normalizeDigit((char) (rec[off + i] & 0xFF));
        }
        return new BigDecimal(new java.math.BigInteger(new String(digits)), scale);
    }

    /**
     * Stores a signed zoned-decimal field, truncating high-order digits the way a COBOL MOVE/ADD
     * without ON SIZE ERROR does.
     */
    public static void writeSigned(byte[] rec, int off, int len, int scale, BigDecimal value) {
        BigDecimal scaled = value.setScale(scale, RoundingMode.DOWN);
        boolean negative = scaled.signum() < 0;
        String digits = scaled.abs().unscaledValue().toString();
        if (digits.length() > len) {
            digits = digits.substring(digits.length() - len); // high-order truncation
        } else {
            digits = "0".repeat(len - digits.length()) + digits;
        }
        for (int i = 0; i < len - 1; i++) {
            rec[off + i] = (byte) digits.charAt(i);
        }
        char last = digits.charAt(len - 1);
        rec[off + len - 1] = (byte) overpunch(last, negative);
    }

    /** Stores an unsigned {@code PIC 9(n)} field with the same truncation rule. */
    public static void writeUnsigned(byte[] rec, int off, int len, int scale, BigDecimal value) {
        String digits = value.abs().setScale(scale, RoundingMode.DOWN).unscaledValue().toString();
        if (digits.length() > len) {
            digits = digits.substring(digits.length() - len);
        } else {
            digits = "0".repeat(len - digits.length()) + digits;
        }
        for (int i = 0; i < len; i++) {
            rec[off + i] = (byte) digits.charAt(i);
        }
    }

    private static char overpunch(char digit, boolean negative) {
        int d = digit - '0';
        if (!negative) {
            return d == 0 ? '{' : (char) ('A' + d - 1);
        }
        return d == 0 ? '}' : (char) ('J' + d - 1);
    }

    private static char normalizeDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c;
        }
        if (c == ' ' || c == 0) {
            return '0';
        }
        if (c == '{' || c == '}') {
            return '0';
        }
        if (c >= 'A' && c <= 'I') {
            return (char) ('1' + (c - 'A'));
        }
        if (c >= 'J' && c <= 'R') {
            return (char) ('1' + (c - 'J'));
        }
        return '0';
    }
}
