package com.cardemo.migration.model;

/**
 * Represents a single field within a COBOL copybook layout.
 *
 * @param name       field name from the copybook (e.g. ACCT-ID)
 * @param offset     byte offset from the start of the record
 * @param length     length in bytes
 * @param type       the COBOL picture type
 * @param scale      decimal scale (digits after V) for numeric fields
 * @param signed     whether the field is signed (S prefix)
 * @param columnName the corresponding database column name
 */
public record CopybookField(
        String name,
        int offset,
        int length,
        FieldType type,
        int scale,
        boolean signed,
        String columnName
) {

    public enum FieldType {
        /** PIC X - alphanumeric display */
        ALPHANUMERIC,
        /** PIC 9 - numeric display (zoned decimal) */
        NUMERIC_DISPLAY,
        /** PIC S9 COMP-3 - packed decimal */
        PACKED_DECIMAL,
        /** PIC 9 COMP - binary */
        BINARY,
        /** FILLER - padding, ignored during validation */
        FILLER
    }

    /**
     * Creates an alphanumeric field (PIC X).
     */
    public static CopybookField alphanumeric(String name, int offset, int length, String columnName) {
        return new CopybookField(name, offset, length, FieldType.ALPHANUMERIC, 0, false, columnName);
    }

    /**
     * Creates a numeric display field (PIC 9).
     */
    public static CopybookField numericDisplay(String name, int offset, int length, String columnName) {
        return new CopybookField(name, offset, length, FieldType.NUMERIC_DISPLAY, 0, false, columnName);
    }

    /**
     * Creates a signed numeric display field (PIC S9 with implied decimal).
     */
    public static CopybookField signedNumericDisplay(String name, int offset, int length,
                                                      int scale, String columnName) {
        return new CopybookField(name, offset, length, FieldType.NUMERIC_DISPLAY, scale, true, columnName);
    }

    /**
     * Creates a FILLER field (padding bytes).
     */
    public static CopybookField filler(int offset, int length) {
        return new CopybookField("FILLER", offset, length, FieldType.FILLER, 0, false, null);
    }
}
