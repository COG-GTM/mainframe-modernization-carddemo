package com.cardemo.batch.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatchReportExceptionTest {

    @Test
    void shouldCreateExceptionWithMessageAndFileStatus() {
        BatchReportException ex = new BatchReportException("ERROR OPENING ACCTFILE", "12");

        assertEquals("ERROR OPENING ACCTFILE", ex.getMessage());
        assertEquals("12", ex.getFileStatus());
        assertEquals(999, ex.getAbendCode());
    }

    @Test
    void shouldCreateExceptionWithCause() {
        RuntimeException cause = new RuntimeException("IO failure");
        BatchReportException ex = new BatchReportException(
                "ERROR READING CARDFILE", "35", cause);

        assertEquals("ERROR READING CARDFILE", ex.getMessage());
        assertEquals("35", ex.getFileStatus());
        assertEquals(999, ex.getAbendCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldCreateExceptionWithCustomAbendCode() {
        BatchReportException ex = new BatchReportException(
                "CUSTOM ERROR", 888, "99");

        assertEquals("CUSTOM ERROR", ex.getMessage());
        assertEquals(888, ex.getAbendCode());
        assertEquals("99", ex.getFileStatus());
    }

    @Test
    void shouldPreserveAbend999Semantics() {
        BatchReportException ex = new BatchReportException("ABENDING PROGRAM", "12");
        assertEquals(999, ex.getAbendCode());
    }

    @Test
    void shouldIncludeAllFieldsInToString() {
        BatchReportException ex = new BatchReportException("ERROR OPENING XREFFILE", "12");
        String str = ex.toString();
        assertTrue(str.contains("ERROR OPENING XREFFILE"));
        assertTrue(str.contains("999"));
        assertTrue(str.contains("12"));
    }
}
