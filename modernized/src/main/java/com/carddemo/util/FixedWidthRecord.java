package com.carddemo.util;

import java.math.BigDecimal;

/** Positional accessor over a fixed width legacy record (1-based COBOL column positions). */
public final class FixedWidthRecord {

    private final String line;

    public FixedWidthRecord(String line) {
        this.line = line == null ? "" : line;
    }

    /** Raw slice, right trimmed. {@code start} is 1-based, inclusive. */
    public String string(int start, int length) {
        int from = Math.min(start - 1, line.length());
        int to = Math.min(from + length, line.length());
        return line.substring(from, to).stripTrailing();
    }

    public String stringOrNull(int start, int length) {
        String value = string(start, length).trim();
        return value.isEmpty() ? null : value;
    }

    public Long longValue(int start, int length) {
        return CobolNumbers.parseLong(string(start, length));
    }

    public Integer intValue(int start, int length) {
        return CobolNumbers.parseInteger(string(start, length));
    }

    public BigDecimal signed(int start, int length, int decimals) {
        return CobolNumbers.parseSigned(string(start, length), decimals);
    }

    public int length() {
        return line.length();
    }

    public String raw() {
        return line;
    }
}
