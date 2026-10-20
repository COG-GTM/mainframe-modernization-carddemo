package com.carddemo.batch.core;

import java.math.BigDecimal;

/** Reads the fields of a fixed-length record left to right, in copybook order. */
public final class RecordReader {

    private final String data;
    private int position;

    public RecordReader(byte[] bytes, int expectedLength) {
        if (bytes.length != expectedLength) {
            throw new IllegalArgumentException(
                    "Expected a " + expectedLength + "-byte record but got " + bytes.length + " bytes");
        }
        this.data = FixedWidth.decode(bytes);
    }

    /** {@code PIC X(width)}, returned with its trailing spaces. */
    public String alpha(int width) {
        String value = data.substring(position, position + width);
        position += width;
        return value;
    }

    /** {@code PIC 9(digits)}. */
    public long unsigned(int digits) {
        return CobolDecimal.parseUnsigned(alpha(digits));
    }

    /** {@code PIC 9(digits)} for fields that fit an int (e.g. category codes). */
    public int unsignedInt(int digits) {
        return Math.toIntExact(unsigned(digits));
    }

    /** {@code PIC S9(integerDigits)V9(scale)}. */
    public BigDecimal signed(int integerDigits, int scale) {
        return CobolDecimal.parseSigned(alpha(integerDigits + scale), scale);
    }

    /** Numeric-edited field of {@code width} characters. */
    public BigDecimal edited(int width, int scale) {
        return CobolDecimal.parseEdited(alpha(width), scale);
    }

    public RecordReader skip(int width) {
        position += width;
        return this;
    }

    public RecordReader seek(int offset) {
        position = offset;
        return this;
    }

    public int position() {
        return position;
    }
}
