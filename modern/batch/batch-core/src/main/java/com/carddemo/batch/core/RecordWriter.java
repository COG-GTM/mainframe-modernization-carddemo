package com.carddemo.batch.core;

import java.math.BigDecimal;

/** Builds a fixed-length record field by field, left to right, in copybook order. */
public final class RecordWriter {

    private final int length;
    private final StringBuilder buffer;

    public RecordWriter(int length) {
        this.length = length;
        this.buffer = new StringBuilder(length);
    }

    /** {@code PIC X(width)}. */
    public RecordWriter alpha(String value, int width) {
        buffer.append(FixedWidth.fit(value, width));
        return this;
    }

    /** {@code PIC 9(digits)}. */
    public RecordWriter unsigned(long value, int digits) {
        buffer.append(CobolDecimal.formatUnsigned(value, digits));
        return this;
    }

    /** {@code PIC S9(integerDigits)V9(scale)}. */
    public RecordWriter signed(BigDecimal value, int integerDigits, int scale) {
        buffer.append(CobolDecimal.formatSigned(value, integerDigits, scale));
        return this;
    }

    /** Numeric-edited field, e.g. {@code -ZZZ,ZZZ,ZZZ.ZZ}. */
    public RecordWriter edited(BigDecimal value, String picture) {
        buffer.append(CobolDecimal.formatEdited(value, picture));
        return this;
    }

    public int position() {
        return buffer.length();
    }

    /** Returns the record bytes; any remaining positions are padded with spaces. */
    public byte[] toBytes() {
        if (buffer.length() > length) {
            throw new IllegalStateException("Record overflow: " + buffer.length() + " > " + length);
        }
        return FixedWidth.encode(FixedWidth.fit(buffer.toString(), length));
    }
}
