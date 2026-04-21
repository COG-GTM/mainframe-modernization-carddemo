package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;

/**
 * Parses individual fields from raw EBCDIC byte arrays based on copybook field definitions.
 * Supports alphanumeric (PIC X), zoned decimal (PIC 9/S9), and packed decimal (COMP-3) fields.
 */
public class EbcdicFieldParser {

    private static final Charset EBCDIC_CHARSET = Charset.forName("IBM037");

    private EbcdicFieldParser() {
    }

    /**
     * Parses a field from a raw record byte array and returns its string representation.
     *
     * @param record the complete record as raw bytes
     * @param field  the copybook field descriptor
     * @return the parsed field value as a string
     */
    public static String parseField(byte[] record, CopybookField field) {
        if (field.type() == CopybookField.FieldType.FILLER) {
            return "";
        }

        byte[] fieldBytes = extractFieldBytes(record, field.offset(), field.length());

        return switch (field.type()) {
            case ALPHANUMERIC -> parseAlphanumeric(fieldBytes);
            case NUMERIC_DISPLAY -> parseNumericDisplay(fieldBytes, field.scale(), field.signed());
            case PACKED_DECIMAL -> parsePackedDecimal(fieldBytes, field.scale()).toPlainString();
            case BINARY -> parseBinary(fieldBytes, field.signed());
            case FILLER -> "";
        };
    }

    /**
     * Parses a packed decimal field and returns a BigDecimal value.
     *
     * @param record the complete record as raw bytes
     * @param field  the copybook field descriptor (must be PACKED_DECIMAL type)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parsePackedDecimalField(byte[] record, CopybookField field) {
        byte[] fieldBytes = extractFieldBytes(record, field.offset(), field.length());
        return parsePackedDecimal(fieldBytes, field.scale());
    }

    /**
     * Parses a numeric display field and returns a BigDecimal value.
     *
     * @param record the complete record as raw bytes
     * @param field  the copybook field descriptor
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseNumericField(byte[] record, CopybookField field) {
        byte[] fieldBytes = extractFieldBytes(record, field.offset(), field.length());
        String raw = parseNumericDisplay(fieldBytes, field.scale(), field.signed());
        return new BigDecimal(raw);
    }

    static byte[] extractFieldBytes(byte[] record, int offset, int length) {
        if (offset + length > record.length) {
            throw new IllegalArgumentException(
                    String.format("Field at offset %d with length %d exceeds record length %d",
                            offset, length, record.length));
        }
        byte[] result = new byte[length];
        System.arraycopy(record, offset, result, 0, length);
        return result;
    }

    /**
     * Parses a PIC X(n) alphanumeric field from EBCDIC bytes.
     * Converts from EBCDIC to ASCII and trims trailing spaces.
     */
    static String parseAlphanumeric(byte[] fieldBytes) {
        String value = new String(fieldBytes, EBCDIC_CHARSET);
        return value.stripTrailing();
    }

    /**
     * Parses a PIC 9(n) or PIC S9(n)V99 zoned decimal field from EBCDIC bytes.
     * In EBCDIC zoned decimal, each byte holds one digit in its low nibble.
     * The sign is encoded in the high nibble of the last byte:
     *   C or F = positive, D = negative.
     */
    static String parseNumericDisplay(byte[] fieldBytes, int scale, boolean signed) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;

        for (int i = 0; i < fieldBytes.length; i++) {
            int b = fieldBytes[i] & 0xFF;
            int highNibble = (b >> 4) & 0x0F;
            int lowNibble = b & 0x0F;

            if (signed && i == fieldBytes.length - 1) {
                if (highNibble == 0x0D) {
                    negative = true;
                }
                digits.append(lowNibble);
            } else {
                // For EBCDIC zoned decimal, digits 0-9 are encoded as 0xF0-0xF9
                if (highNibble == 0x0F) {
                    digits.append(lowNibble);
                } else {
                    // Fallback: try interpreting via EBCDIC charset
                    String ch = new String(new byte[]{fieldBytes[i]}, EBCDIC_CHARSET);
                    if (ch.length() == 1 && Character.isDigit(ch.charAt(0))) {
                        digits.append(ch.charAt(0));
                    } else {
                        digits.append('0');
                    }
                }
            }
        }

        String numStr = digits.toString();
        if (scale > 0 && numStr.length() > scale) {
            String intPart = numStr.substring(0, numStr.length() - scale);
            String decPart = numStr.substring(numStr.length() - scale);
            numStr = intPart + "." + decPart;
        }

        if (negative) {
            numStr = "-" + numStr;
        }

        return numStr;
    }

    /**
     * Parses a COMP-3 packed decimal field.
     * Each byte contains two decimal digits (one per nibble), except the last byte
     * where the low nibble is the sign (C/F = positive, D = negative).
     */
    static BigDecimal parsePackedDecimal(byte[] fieldBytes, int scale) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;

        for (int i = 0; i < fieldBytes.length; i++) {
            int b = fieldBytes[i] & 0xFF;
            int highNibble = (b >> 4) & 0x0F;
            int lowNibble = b & 0x0F;

            if (i == fieldBytes.length - 1) {
                digits.append(highNibble);
                // Low nibble is sign: C(0xC)=positive, F(0xF)=positive, D(0xD)=negative
                if (lowNibble == 0x0D) {
                    negative = true;
                }
            } else {
                digits.append(highNibble);
                digits.append(lowNibble);
            }
        }

        // Remove leading zeros but keep at least one digit
        String numStr = digits.toString().replaceFirst("^0+(?=.)", "");
        if (numStr.isEmpty()) {
            numStr = "0";
        }

        BigDecimal result = new BigDecimal(numStr);
        if (scale > 0) {
            result = result.movePointLeft(scale);
        }
        if (negative) {
            result = result.negate();
        }

        return result.round(new MathContext(numStr.length() + scale));
    }

    /**
     * Parses a COMP (binary) field.
     */
    static String parseBinary(byte[] fieldBytes, boolean signed) {
        long value = 0;
        for (byte b : fieldBytes) {
            value = (value << 8) | (b & 0xFF);
        }

        if (signed && fieldBytes.length > 0 && (fieldBytes[0] & 0x80) != 0) {
            // Sign extend for negative values
            for (int i = fieldBytes.length; i < 8; i++) {
                value |= (0xFFL << (i * 8));
            }
        }

        return Long.toString(value);
    }
}
