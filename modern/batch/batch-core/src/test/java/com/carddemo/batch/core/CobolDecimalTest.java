package com.carddemo.batch.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CobolDecimalTest {

    @ParameterizedTest
    @CsvSource({
        "00000001940{, 10, 194.00",
        "0000005047G,  9,  504.77",
        "0000009190},  9,  -919.00",
        "0000000678H,  9,  67.88",
        "0000000123R,  9,  -12.39",
        "00000000000{, 10, 0.00",
        "0000000123y,  9,  -12.39",
        "-0000000123,  9,  -1.23",
        "0000000123+,  9,  1.23"
    })
    void parsesSignedZonedFields(String field, int integerDigits, String expected) {
        assertEquals(new BigDecimal(expected), CobolDecimal.parseSigned(field, 2));
    }

    @ParameterizedTest
    @CsvSource({
        "194.00,   10, 00000001940{",
        "504.77,    9, 0000005047G",
        "-919.00,   9, 0000009190}",
        "-12.39,    9, 0000000123R",
        "0,         9, 0000000000{",
        "1.239,     9, 0000000012C",
        "-1.239,    9, 0000000012L",
        "12345678901.23, 9, 3456789012C"
    })
    void formatsSignedFieldsWithTruncation(String value, int integerDigits, String expected) {
        assertEquals(expected, CobolDecimal.formatSigned(new BigDecimal(value), integerDigits, 2));
    }

    @Test
    void blankSignedFieldIsZero() {
        assertEquals(new BigDecimal("0.00"), CobolDecimal.parseSigned("           ", 2));
    }

    @Test
    void rejectsInvalidZonedDecimal() {
        assertThrows(NumberFormatException.class, () -> CobolDecimal.parseSigned("12A4", 2));
    }

    @Test
    void unsignedFields() {
        assertEquals(1L, CobolDecimal.parseUnsigned("00000000001"));
        assertEquals(0L, CobolDecimal.parseUnsigned("    "));
        assertEquals("0005", CobolDecimal.formatUnsigned(5, 4));
        assertEquals("2345", CobolDecimal.formatUnsigned(12345, 4));
        assertEquals("0005", CobolDecimal.formatUnsigned(-5, 4));
        assertEquals("00012345", CobolDecimal.formatUnsigned(new BigDecimal("123.456"), 6, 2));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "-1234.5      | -ZZZ,ZZZ,ZZZ.ZZ | '-      1,234.50'",
        "1234.5       | -ZZZ,ZZZ,ZZZ.ZZ | '       1,234.50'",
        "0.05         | -ZZZ,ZZZ,ZZZ.ZZ | '            .05'",
        "-0.05        | -ZZZ,ZZZ,ZZZ.ZZ | '-           .05'",
        "0            | -ZZZ,ZZZ,ZZZ.ZZ | '               '",
        "999999999.99 | -ZZZ,ZZZ,ZZZ.ZZ | ' 999,999,999.99'",
        "1234567.89   | +ZZZ,ZZZ,ZZZ.ZZ | '+  1,234,567.89'",
        "-5           | +ZZZ,ZZZ,ZZZ.ZZ | '-          5.00'",
        "0            | +ZZZ,ZZZ,ZZZ.ZZ | '               '",
        "1000         | +ZZZ,ZZZ,ZZZ.ZZ | '+      1,000.00'",
        "0            | +ZZZ,ZZ9.99     | '+      0.00'",
        "12.3         | Z(3)9.99        | '  12.30'"
    })
    void formatsEditedPictures(String value, String picture, String expected) {
        assertEquals(expected, CobolDecimal.formatEdited(new BigDecimal(value), picture));
    }

    @Test
    void editedPictureTruncatesHighOrderDigits() {
        assertEquals("+234,567,890.12", CobolDecimal.formatEdited(new BigDecimal("1234567890.129"), "+ZZZ,ZZZ,ZZZ.ZZ"));
    }

    @Test
    void parsesEditedText() {
        assertEquals(new BigDecimal("-1234.50"), CobolDecimal.parseEdited("-      1,234.50", 2));
        assertEquals(new BigDecimal("1234567.89"), CobolDecimal.parseEdited("+  1,234,567.89", 2));
        assertEquals(new BigDecimal("0.00"), CobolDecimal.parseEdited("               ", 2));
    }

    @Test
    void db2TimestampMatchesZGetDb2FormatTimestamp() {
        Clock clock = Clock.fixed(Instant.parse("2022-07-18T14:05:09.876543Z"), ZoneOffset.UTC);
        assertEquals("2022-07-18-14.05.09.870000", CobolDecimal.db2Timestamp(clock));
        assertEquals(26, CobolDecimal.db2Timestamp(clock).length());
    }

    @Test
    void db2TimestampUsesClockZone() {
        Clock clock = Clock.fixed(Instant.parse("2022-12-31T23:59:59.999Z"), ZoneOffset.ofHours(2));
        assertEquals("2023-01-01-01.59.59.990000", CobolDecimal.db2Timestamp(clock));
    }
}
