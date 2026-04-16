package com.cardemo.batch.orchestration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReturnCode model.
 */
class ReturnCodeTest {

    @Test
    void successReturnCode_hasCodeZero() {
        ReturnCode rc = new ReturnCode(ReturnCode.SUCCESS);
        assertEquals(0, rc.getCode());
        assertTrue(rc.isSuccess());
        assertFalse(rc.isWarning());
        assertFalse(rc.isError());
    }

    @Test
    void warningReturnCode_hasCodeFour() {
        ReturnCode rc = new ReturnCode(ReturnCode.WARNING);
        assertEquals(4, rc.getCode());
        assertFalse(rc.isSuccess());
        assertTrue(rc.isWarning());
        assertFalse(rc.isError());
    }

    @Test
    void errorReturnCode_hasCodeEight() {
        ReturnCode rc = new ReturnCode(ReturnCode.ERROR);
        assertEquals(8, rc.getCode());
        assertFalse(rc.isSuccess());
        assertFalse(rc.isWarning());
        assertTrue(rc.isError());
    }

    @Test
    void severeReturnCode_isError() {
        ReturnCode rc = new ReturnCode(ReturnCode.SEVERE);
        assertEquals(12, rc.getCode());
        assertTrue(rc.isError());
    }

    @Test
    void criticalReturnCode_isError() {
        ReturnCode rc = new ReturnCode(ReturnCode.CRITICAL);
        assertEquals(16, rc.getCode());
        assertTrue(rc.isError());
    }

    @Test
    void shouldContinue_withSuccessCode_belowThreshold() {
        ReturnCode rc = new ReturnCode(ReturnCode.SUCCESS);
        assertTrue(rc.shouldContinue(4));
    }

    @Test
    void shouldContinue_withWarningCode_atThreshold() {
        ReturnCode rc = new ReturnCode(ReturnCode.WARNING);
        assertTrue(rc.shouldContinue(4));
    }

    @Test
    void shouldNotContinue_withErrorCode_aboveThreshold() {
        ReturnCode rc = new ReturnCode(ReturnCode.ERROR);
        assertFalse(rc.shouldContinue(4));
    }

    @Test
    void customMessage_preservedInReturnCode() {
        ReturnCode rc = new ReturnCode(4, "3 transactions rejected");
        assertEquals(4, rc.getCode());
        assertEquals("3 transactions rejected", rc.getMessage());
    }

    @Test
    void toString_containsCodeAndMessage() {
        ReturnCode rc = new ReturnCode(ReturnCode.SUCCESS);
        String str = rc.toString();
        assertTrue(str.contains("code=0"));
        assertTrue(str.contains("Successful completion"));
    }
}
