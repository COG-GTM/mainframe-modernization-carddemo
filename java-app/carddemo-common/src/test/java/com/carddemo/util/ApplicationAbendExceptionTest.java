package com.carddemo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApplicationAbendException — replaces CEE3ABD ABEND call behavior.
 */
class ApplicationAbendExceptionTest {

    @Test
    void constructorWithAbendCodeAndMessage() {
        ApplicationAbendException ex = new ApplicationAbendException(999, "Critical error");
        assertEquals(999, ex.getAbendCode());
        assertEquals("Critical error", ex.getMessage());
    }

    @Test
    void constructorWithAbendCodeOnly() {
        ApplicationAbendException ex = new ApplicationAbendException(100, "Abend code 100");
        assertEquals(100, ex.getAbendCode());
        assertTrue(ex.getMessage().contains("100"));
    }

    @Test
    void isRuntimeException() {
        ApplicationAbendException ex = new ApplicationAbendException(0, "Test");
        assertTrue(ex instanceof RuntimeException);
    }
}
