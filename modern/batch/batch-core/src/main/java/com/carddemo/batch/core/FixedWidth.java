package com.carddemo.batch.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Fixed-width, space-padded text helpers (COBOL alphanumeric MOVE semantics). */
public final class FixedWidth {

    /**
     * Record charset. The data files are plain ASCII; ISO-8859-1 is its one-byte superset, which
     * guarantees a lossless byte-for-byte round trip.
     */
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private FixedWidth() {
    }

    /** Left-justifies {@code value} in {@code length} characters: truncates or pads with spaces. */
    public static String fit(String value, int length) {
        String v = value == null ? "" : value;
        return v.length() >= length ? v.substring(0, length) : v + " ".repeat(length - v.length());
    }

    /** Pads a raw record with trailing spaces, or truncates it, to exactly {@code length} bytes. */
    public static byte[] padRecord(byte[] raw, int length) {
        if (raw.length == length) {
            return raw;
        }
        byte[] out = Arrays.copyOf(raw, length);
        if (raw.length < length) {
            Arrays.fill(out, raw.length, length, (byte) ' ');
        }
        return out;
    }

    public static byte[] encode(String text) {
        return text.getBytes(CHARSET);
    }

    public static String decode(byte[] bytes) {
        return new String(bytes, CHARSET);
    }
}
