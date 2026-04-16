package com.carddemo.reports.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReportTypeTest {

    @Test
    void fromCode_monthly() {
        assertEquals(ReportType.MONTHLY, ReportType.fromCode("01"));
    }

    @Test
    void fromCode_yearly() {
        assertEquals(ReportType.YEARLY, ReportType.fromCode("02"));
    }

    @Test
    void fromCode_custom() {
        assertEquals(ReportType.CUSTOM, ReportType.fromCode("03"));
    }

    @Test
    void fromCode_invalidReturnsNull() {
        assertNull(ReportType.fromCode("04"));
        assertNull(ReportType.fromCode(""));
        assertNull(ReportType.fromCode("abc"));
    }

    @Test
    void displayName_matchesCobolLabels() {
        assertEquals("Monthly", ReportType.MONTHLY.displayName());
        assertEquals("Yearly", ReportType.YEARLY.displayName());
        assertEquals("Custom", ReportType.CUSTOM.displayName());
    }

    @Test
    void getCode_returnsCorrectValues() {
        assertEquals("01", ReportType.MONTHLY.getCode());
        assertEquals("02", ReportType.YEARLY.getCode());
        assertEquals("03", ReportType.CUSTOM.getCode());
    }
}
