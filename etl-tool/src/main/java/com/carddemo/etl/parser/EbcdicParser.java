package com.carddemo.etl.parser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;

/**
 * Utility class for parsing EBCDIC-encoded fixed-length records.
 * Handles character data (PIC X), zoned decimal (PIC 9), and signed display numeric (PIC S9(n)V99).
 */
public final class EbcdicParser {

    private static final Charset EBCDIC_CHARSET = Charset.forName("IBM037");

    private EbcdicParser() {
    }

    /**
     * Extract a string field (PIC X) from the EBCDIC byte array, trimming trailing spaces.
     */
    public static String parseString(byte[] record, int offset, int length) {
        byte[] field = new byte[length];
        System.arraycopy(record, offset, field, 0, length);
        return new String(field, EBCDIC_CHARSET).trim();
    }

    /**
     * Extract an unsigned numeric field (PIC 9(n)) from the EBCDIC byte array.
     * Digits are in EBCDIC zone decimal format: 0xF0-0xF9 map to '0'-'9'.
     */
    public static long parseUnsignedNumeric(byte[] record, int offset, int length) {
        String str = parseString(record, offset, length).trim();
        if (str.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Extract a signed display numeric field (PIC S9(n)V99) from the EBCDIC byte array.
     * In EBCDIC zoned decimal, the sign is embedded in the last byte's zone nibble:
     *   0xC0-0xC9 or 0xF0-0xF9 = positive
     *   0xD0-0xD9 = negative
     * The implied decimal point (V99) means the last 2 digits are fractional.
     */
    public static BigDecimal parseSignedDecimal(byte[] record, int offset, int length, int decimalPlaces) {
        byte[] field = new byte[length];
        System.arraycopy(record, offset, field, 0, length);

        StringBuilder sb = new StringBuilder();
        boolean negative = false;

        for (int i = 0; i < length; i++) {
            int b = field[i] & 0xFF;
            int zoneNibble = (b >> 4) & 0x0F;
            int digitNibble = b & 0x0F;

            if (i == length - 1) {
                // Last byte: zone nibble carries the sign
                if (zoneNibble == 0x0D) {
                    negative = true;
                }
                // 0x0C, 0x0F = positive; 0x0D = negative
            }

            if (digitNibble >= 0 && digitNibble <= 9) {
                sb.append(digitNibble);
            } else {
                sb.append('0');
            }
        }

        String digits = sb.toString();
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = new BigDecimal(digits).movePointLeft(decimalPlaces);
        if (negative) {
            value = value.negate();
        }
        return value;
    }

    /**
     * Extract an unsigned numeric field and return as int.
     */
    public static int parseUnsignedInt(byte[] record, int offset, int length) {
        return (int) parseUnsignedNumeric(record, offset, length);
    }
}
