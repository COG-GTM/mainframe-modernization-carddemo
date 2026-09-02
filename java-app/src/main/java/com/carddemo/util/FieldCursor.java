package com.carddemo.util;

import java.math.BigDecimal;

/**
 * Sequential reader over a fixed-width COBOL record, mirroring the field order of a copybook.
 */
public final class FieldCursor {

    private final String record;
    private int position;

    public FieldCursor(String record, int recordLength) {
        this.record = CobolCodec.padRecord(record, recordLength);
    }

    /** Raw slice of {@code width} characters, unmodified. */
    public String raw(int width) {
        String slice = record.substring(position, position + width);
        position += width;
        return slice;
    }

    /** {@code PIC X(width)} trimmed of trailing blanks. */
    public String text(int width) {
        return CobolCodec.text(raw(width));
    }

    /** {@code PIC X(width)} preserved verbatim (fixed-width identifiers such as card numbers). */
    public String fixed(int width) {
        return raw(width);
    }

    /** {@code PIC 9(width)} as a long. */
    public long number(int width) {
        return CobolCodec.decodeLong(raw(width));
    }

    /** {@code PIC 9(width)} as an int. */
    public int integer(int width) {
        return CobolCodec.decodeInt(raw(width));
    }

    /** {@code PIC S9(width-scale)V9(scale)} zoned decimal with overpunched sign. */
    public BigDecimal signed(int width, int scale) {
        return CobolCodec.decodeSigned(raw(width), scale);
    }

    /** Single-character {@code PIC X(01)} field. */
    public char flag() {
        String slice = raw(1);
        return slice.isEmpty() ? ' ' : slice.charAt(0);
    }

    public void skip(int width) {
        position += width;
    }
}
