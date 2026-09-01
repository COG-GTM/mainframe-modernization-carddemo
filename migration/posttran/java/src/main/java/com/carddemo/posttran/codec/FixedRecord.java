package com.carddemo.posttran.codec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * A fixed-width mainframe record held as raw characters.
 *
 * <p>Fields are read and written in place. That matters for parity: COBOL's
 * {@code READ ... INTO group} / {@code REWRITE ... FROM group} round-trip leaves every byte the
 * program did not touch exactly as it was on disk, including {@code FILLER} and the sign
 * representation of numeric fields the program never referenced.
 */
public class FixedRecord {

    protected final char[] raw;

    protected FixedRecord(int length) {
        this.raw = new char[length];
        java.util.Arrays.fill(this.raw, ' ');
    }

    protected FixedRecord(String content, int length) {
        this(length);
        int n = Math.min(content.length(), length);
        content.getChars(0, n, this.raw, 0);
    }

    public int length() {
        return raw.length;
    }

    public String text(int offset, int len) {
        return new String(raw, offset, len);
    }

    public BigDecimal number(int offset, int len, int scale) {
        return Zoned.decode(text(offset, len), scale);
    }

    protected void putText(int offset, int len, String value) {
        String v = value.length() > len ? value.substring(0, len) : value;
        for (int i = 0; i < len; i++) {
            raw[offset + i] = i < v.length() ? v.charAt(i) : ' ';
        }
    }

    /** Writes a {@code PIC 9(len)} field: right-justified, zero-filled, unsigned. */
    protected void putUnsigned(int offset, int len, String digits) {
        String d = digits.trim();
        if (d.length() > len) {
            d = d.substring(d.length() - len);
        }
        putText(offset, len, "0".repeat(len - d.length()) + d);
    }

    protected void putNumber(int offset, int len, int scale, BigDecimal value) {
        putText(offset, len, Zoned.encode(value, len, scale));
    }

    @Override
    public String toString() {
        return new String(raw);
    }

    public byte[] bytes() {
        return toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
