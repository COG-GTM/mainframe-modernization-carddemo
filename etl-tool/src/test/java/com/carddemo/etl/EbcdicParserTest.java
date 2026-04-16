package com.carddemo.etl;

import com.carddemo.etl.parser.EbcdicParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EBCDIC parser utility.
 */
class EbcdicParserTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    @Test
    void parseString_extractsAndTrims() {
        // "HELLO     " in EBCDIC
        String input = "HELLO     ";
        byte[] ebcdicBytes = input.getBytes(EBCDIC);
        byte[] record = new byte[20];
        System.arraycopy(ebcdicBytes, 0, record, 5, ebcdicBytes.length);

        String result = EbcdicParser.parseString(record, 5, 10);
        assertEquals("HELLO", result);
    }

    @Test
    void parseUnsignedNumeric_parsesDigits() {
        // "00000000050" in EBCDIC (11 digits for account ID)
        String input = "00000000050";
        byte[] ebcdicBytes = input.getBytes(EBCDIC);
        byte[] record = new byte[20];
        System.arraycopy(ebcdicBytes, 0, record, 0, ebcdicBytes.length);

        long result = EbcdicParser.parseUnsignedNumeric(record, 0, 11);
        assertEquals(50L, result);
    }

    @Test
    void parseSignedDecimal_positiveValue() {
        // Positive signed decimal: "000000019400" -> 194.00
        // In EBCDIC zoned decimal, positive sign = 0xC or 0xF in last byte zone nibble
        // For "000000019400" with positive sign:
        // bytes 0-10: 0xF0 0xF0 0xF0 0xF0 0xF0 0xF0 0xF0 0xF1 0xF9 0xF4 0xF0 0xF0
        // last byte zone nibble changed to 0xC0 for positive: 0xC0
        byte[] record = new byte[20];
        record[0] = (byte) 0xF0; // 0
        record[1] = (byte) 0xF0; // 0
        record[2] = (byte) 0xF0; // 0
        record[3] = (byte) 0xF0; // 0
        record[4] = (byte) 0xF0; // 0
        record[5] = (byte) 0xF0; // 0
        record[6] = (byte) 0xF0; // 0
        record[7] = (byte) 0xF1; // 1
        record[8] = (byte) 0xF9; // 9
        record[9] = (byte) 0xF4; // 4
        record[10] = (byte) 0xF0; // 0
        record[11] = (byte) 0xC0; // 0 with positive sign (0xC zone)

        BigDecimal result = EbcdicParser.parseSignedDecimal(record, 0, 12, 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void parseSignedDecimal_negativeValue() {
        // Negative signed decimal: last byte zone nibble = 0xD
        byte[] record = new byte[20];
        record[0] = (byte) 0xF0; // 0
        record[1] = (byte) 0xF0; // 0
        record[2] = (byte) 0xF0; // 0
        record[3] = (byte) 0xF0; // 0
        record[4] = (byte) 0xF0; // 0
        record[5] = (byte) 0xF0; // 0
        record[6] = (byte) 0xF5; // 5
        record[7] = (byte) 0xF0; // 0
        record[8] = (byte) 0xF0; // 0
        record[9] = (byte) 0xF0; // 0
        record[10] = (byte) 0xF0; // 0
        record[11] = (byte) 0xD0; // 0 with negative sign (0xD zone)

        BigDecimal result = EbcdicParser.parseSignedDecimal(record, 0, 12, 2);
        // 12 digits "000000500000" with 2 decimal places = -5000.00
        assertEquals(new BigDecimal("-5000.00"), result);
    }

    @Test
    void parseUnsignedInt_parsesSmallNumber() {
        String input = "123";
        byte[] ebcdicBytes = input.getBytes(EBCDIC);
        byte[] record = new byte[10];
        System.arraycopy(ebcdicBytes, 0, record, 0, ebcdicBytes.length);

        int result = EbcdicParser.parseUnsignedInt(record, 0, 3);
        assertEquals(123, result);
    }

    @Test
    void parseString_emptyField() {
        // All EBCDIC spaces (0x40)
        byte[] record = new byte[10];
        for (int i = 0; i < 10; i++) {
            record[i] = 0x40;
        }

        String result = EbcdicParser.parseString(record, 0, 10);
        assertEquals("", result);
    }
}
