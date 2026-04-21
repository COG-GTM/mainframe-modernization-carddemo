package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EBCDIC field parsing including alphanumeric, zoned decimal,
 * packed decimal (COMP-3), and binary field types.
 */
class EbcdicFieldParserTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    // ======================== Alphanumeric (PIC X) Tests ========================

    @Test
    @DisplayName("Parse PIC X field with EBCDIC-encoded text")
    void parseAlphanumericField() {
        String text = "HELLO WORLD     ";
        byte[] ebcdicBytes = text.getBytes(EBCDIC);
        String result = EbcdicFieldParser.parseAlphanumeric(ebcdicBytes);
        assertEquals("HELLO WORLD", result);
    }

    @Test
    @DisplayName("Parse PIC X field with trailing spaces trimmed")
    void parseAlphanumericFieldTrimsTrailingSpaces() {
        String text = "TEST    ";
        byte[] ebcdicBytes = text.getBytes(EBCDIC);
        String result = EbcdicFieldParser.parseAlphanumeric(ebcdicBytes);
        assertEquals("TEST", result);
    }

    @Test
    @DisplayName("Parse PIC X field that is all spaces returns empty string")
    void parseAlphanumericAllSpaces() {
        String text = "        ";
        byte[] ebcdicBytes = text.getBytes(EBCDIC);
        String result = EbcdicFieldParser.parseAlphanumeric(ebcdicBytes);
        assertEquals("", result);
    }

    // ======================== Numeric Display (PIC 9) Tests ========================

    @Test
    @DisplayName("Parse unsigned PIC 9(11) zoned decimal field")
    void parseUnsignedNumericDisplay() {
        String digits = "00000012345";
        byte[] ebcdicBytes = digits.getBytes(EBCDIC);
        String result = EbcdicFieldParser.parseNumericDisplay(ebcdicBytes, 0, false);
        assertEquals("00000012345", result);
    }

    @Test
    @DisplayName("Parse signed positive PIC S9(10)V99 zoned decimal")
    void parseSignedPositiveNumericDisplay() {
        // In EBCDIC, positive sign is 0xC or 0xF in high nibble of last byte
        // "12345" with positive sign: bytes F1 F2 F3 F4 C5
        byte[] ebcdicBytes = new byte[]{
                (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xC5
        };
        String result = EbcdicFieldParser.parseNumericDisplay(ebcdicBytes, 2, true);
        assertEquals("123.45", result);
    }

    @Test
    @DisplayName("Parse signed negative PIC S9(10)V99 zoned decimal")
    void parseSignedNegativeNumericDisplay() {
        // Negative sign: 0xD in high nibble of last byte
        // "-12345" => F1 F2 F3 F4 D5
        byte[] ebcdicBytes = new byte[]{
                (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xD5
        };
        String result = EbcdicFieldParser.parseNumericDisplay(ebcdicBytes, 2, true);
        assertEquals("-123.45", result);
    }

    // ======================== Packed Decimal (COMP-3) Tests ========================

    @Test
    @DisplayName("Parse COMP-3 positive packed decimal")
    void parsePackedDecimalPositive() {
        // Packed decimal: each byte has 2 digits, last byte high nibble = digit, low nibble = sign
        // 12345C = 0x12 0x34 0x5C → digits "12345", sign C = positive
        byte[] packed = new byte[]{0x12, 0x34, 0x5C};
        BigDecimal result = EbcdicFieldParser.parsePackedDecimal(packed, 2);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    @DisplayName("Parse COMP-3 negative packed decimal")
    void parsePackedDecimalNegative() {
        // -12345: 0x12 0x34 0x5D → digits "12345", sign D = negative
        byte[] packed = new byte[]{0x12, 0x34, 0x5D};
        BigDecimal result = EbcdicFieldParser.parsePackedDecimal(packed, 2);
        assertEquals(new BigDecimal("-123.45"), result);
    }

    @Test
    @DisplayName("Parse COMP-3 zero value")
    void parsePackedDecimalZero() {
        // 0 positive: 0x00 0x0C
        byte[] packed = new byte[]{0x00, 0x0C};
        BigDecimal result = EbcdicFieldParser.parsePackedDecimal(packed, 2);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    @DisplayName("Parse COMP-3 with unsigned positive (F sign)")
    void parsePackedDecimalUnsignedPositive() {
        // 999 unsigned positive: 0x99 0x9F
        byte[] packed = new byte[]{(byte) 0x99, (byte) 0x9F};
        BigDecimal result = EbcdicFieldParser.parsePackedDecimal(packed, 0);
        assertEquals(new BigDecimal("999"), result);
    }

    @Test
    @DisplayName("Parse COMP-3 large amount S9(10)V99")
    void parsePackedDecimalLargeAmount() {
        // 123456789099 as COMP-3 with scale 2 → 1234567890.99
        // 0x01 0x23 0x45 0x67 0x89 0x09 0x9C → digits "0123456789099", sign C
        // After leading zero removal: "123456789099", movePointLeft(2) = 1234567890.99
        byte[] packed = new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, 0x09, (byte) 0x9C};
        BigDecimal result = EbcdicFieldParser.parsePackedDecimal(packed, 2);
        assertEquals(new BigDecimal("1234567890.99"), result);
    }

    // ======================== Field Extraction Tests ========================

    @Test
    @DisplayName("Extract field bytes from record at correct offset")
    void extractFieldBytesCorrectOffset() {
        byte[] record = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] extracted = EbcdicFieldParser.extractFieldBytes(record, 2, 3);
        assertArrayEquals(new byte[]{0x03, 0x04, 0x05}, extracted);
    }

    @Test
    @DisplayName("Extract field bytes throws on overflow")
    void extractFieldBytesOverflow() {
        byte[] record = new byte[]{0x01, 0x02, 0x03};
        assertThrows(IllegalArgumentException.class, () ->
                EbcdicFieldParser.extractFieldBytes(record, 2, 5));
    }

    // ======================== Full parseField Tests ========================

    @Test
    @DisplayName("parseField with FILLER returns empty string")
    void parseFieldFiller() {
        byte[] record = new byte[10];
        CopybookField filler = CopybookField.filler(0, 10);
        String result = EbcdicFieldParser.parseField(record, filler);
        assertEquals("", result);
    }

    @Test
    @DisplayName("parseField with alphanumeric field from full record")
    void parseFieldAlphanumericFromRecord() {
        // Build a 50-byte record with "CARD1234567890AB" at offset 0
        String cardNum = "CARD1234567890AB";
        byte[] cardBytes = cardNum.getBytes(EBCDIC);
        byte[] record = new byte[50];
        System.arraycopy(cardBytes, 0, record, 0, cardBytes.length);

        CopybookField field = CopybookField.alphanumeric("CARD-NUM", 0, 16, "card_num");
        String result = EbcdicFieldParser.parseField(record, field);
        assertEquals("CARD1234567890AB", result);
    }

    // ======================== Binary (COMP) Tests ========================

    @Test
    @DisplayName("Parse unsigned binary field")
    void parseBinaryUnsigned() {
        byte[] fieldBytes = new byte[]{0x00, 0x00, 0x01, 0x00}; // 256
        String result = EbcdicFieldParser.parseBinary(fieldBytes, false);
        assertEquals("256", result);
    }

    @Test
    @DisplayName("Parse signed negative binary field")
    void parseBinarySignedNegative() {
        byte[] fieldBytes = new byte[]{(byte) 0xFF, (byte) 0xFF}; // -1 in 2-byte signed
        String result = EbcdicFieldParser.parseBinary(fieldBytes, true);
        assertEquals("-1", result);
    }
}
