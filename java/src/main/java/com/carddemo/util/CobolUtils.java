package com.carddemo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for handling the fixed-width, zoned-decimal record formats used by the
 * original COBOL/VSAM files (see the copybooks in {@code app/cpy}).
 */
public final class CobolUtils {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private CobolUtils() {
    }

    /** Returns the trimmed substring at [start, start+length) of a fixed-width record. */
    public static String str(String record, int start, int length) {
        if (record == null) {
            return "";
        }
        int from = Math.min(start, record.length());
        int to = Math.min(start + length, record.length());
        return record.substring(from, to).trim();
    }

    /** Reads an unsigned PIC 9(n) field, returning 0 for blank fields. */
    public static long num(String record, int start, int length) {
        String raw = str(record, start, length).replace(" ", "");
        if (raw.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(raw);
    }

    /** Reads an unsigned PIC 9(n) field as an int. */
    public static int intNum(String record, int start, int length) {
        return Math.toIntExact(num(record, start, length));
    }

    /**
     * Reads a signed zoned-decimal PIC S9(m)V9(scale) field. The sign is carried by the
     * overpunch character in the last byte ({@code {}=+0, A-I=+1..+9, }=-0, J-R=-1..-9).
     */
    public static BigDecimal decimal(String record, int start, int length, int scale) {
        String raw = record.substring(Math.min(start, record.length()),
                Math.min(start + length, record.length()));
        return decimal(raw, scale);
    }

    /** Parses a standalone signed zoned-decimal string with the given number of decimals. */
    public static BigDecimal decimal(String raw, int scale) {
        if (raw == null) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.UNNECESSARY);
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.UNNECESSARY);
        }
        boolean negative = false;
        char last = value.charAt(value.length() - 1);
        int positive = POSITIVE_OVERPUNCH.indexOf(last);
        int minus = NEGATIVE_OVERPUNCH.indexOf(last);
        if (positive >= 0) {
            value = value.substring(0, value.length() - 1) + positive;
        } else if (minus >= 0) {
            negative = true;
            value = value.substring(0, value.length() - 1) + minus;
        } else if (last == '-' || last == '+') {
            negative = last == '-';
            value = value.substring(0, value.length() - 1);
        }
        value = value.replace(" ", "");
        if (value.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.UNNECESSARY);
        }
        BigDecimal result = new BigDecimal(value).movePointLeft(scale);
        return negative ? result.negate() : result;
    }

    /** Left-justified, space-padded rendering of a PIC X(n) field. */
    public static String padRight(String value, int length) {
        String base = value == null ? "" : value;
        if (base.length() >= length) {
            return base.substring(0, length);
        }
        return base + " ".repeat(length - base.length());
    }

    /** Right-justified, zero-padded rendering of a PIC 9(n) field. */
    public static String padLeftZeros(Object value, int length) {
        String base = value == null ? "" : String.valueOf(value);
        if (base.length() >= length) {
            return base.substring(base.length() - length);
        }
        return "0".repeat(length - base.length()) + base;
    }

    /** Applies COBOL {@code ROUNDED} semantics (half-up) at the given scale. */
    public static BigDecimal rounded(BigDecimal value, int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }

    /** Null-safe zero default, mirroring COBOL numeric fields that are never null. */
    public static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
