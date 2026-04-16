package com.cardemo.batch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimestampService.
 * Tests DB2-compatible timestamp format: YYYY-MM-DD-HH.MM.SS.NNNNNN
 * Maps to Z-GET-DB2-FORMAT-TIMESTAMP in CBTRN02C.
 */
class TimestampServiceTest {

    private TimestampService timestampService;

    @BeforeEach
    void setUp() {
        timestampService = new TimestampService();
    }

    @Test
    @DisplayName("DB2 timestamp format matches YYYY-MM-DD-HH.MM.SS.NNNNNN pattern")
    void generateDb2Timestamp_matchesPattern() {
        String ts = timestampService.generateDb2Timestamp();
        // Pattern: YYYY-MM-DD-HH.MM.SS.NNNNNN
        assertTrue(ts.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{6}"),
                "Timestamp should match DB2 format: " + ts);
    }

    @Test
    @DisplayName("DB2 timestamp has correct separators (dashes and dots)")
    void generateDb2Timestamp_correctSeparators() {
        String ts = timestampService.generateDb2Timestamp();
        assertEquals('-', ts.charAt(4));
        assertEquals('-', ts.charAt(7));
        assertEquals('-', ts.charAt(10));
        assertEquals('.', ts.charAt(13));
        assertEquals('.', ts.charAt(16));
        assertEquals('.', ts.charAt(19));
    }

    @Test
    @DisplayName("formatDb2Timestamp with known date produces exact output")
    void formatDb2Timestamp_knownDate() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 15, 14, 30, 45, 123456000);
        String ts = timestampService.formatDb2Timestamp(dateTime);
        assertEquals("2025-03-15-14.30.45.123456", ts);
    }

    @Test
    @DisplayName("DB2 timestamp length is exactly 26 characters")
    void generateDb2Timestamp_length26() {
        String ts = timestampService.generateDb2Timestamp();
        assertEquals(26, ts.length());
    }
}
