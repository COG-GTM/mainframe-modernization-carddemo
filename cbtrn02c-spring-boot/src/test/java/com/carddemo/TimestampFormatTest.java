package com.carddemo;

import com.carddemo.service.Db2TimestampFormatter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimestampFormatTest {

    @Test
    void testDb2FormatTimestamp() {
        String ts = Db2TimestampFormatter.now();

        assertEquals(26, ts.length());
        // Pattern: YYYY-MM-DD-HH.MM.SS.mm0000
        assertTrue(ts.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}0000"),
                "Timestamp '" + ts + "' did not match DB2 format pattern");
    }

    @Test
    void testDb2FormatTimestampFromFixedInstant() {
        String ts = Db2TimestampFormatter.format(
                LocalDateTime.of(2023, 6, 1, 13, 45, 9, 120_000_000));

        assertEquals("2023-06-01-13.45.09.120000", ts);
        assertEquals(26, ts.length());
    }
}
