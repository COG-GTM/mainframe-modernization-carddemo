package com.cardemo.gateway.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProgramContext enum — verifies CDEMO-PGM-CONTEXT value mapping.
 */
class ProgramContextTest {

    @Test
    void fromValue_shouldMapEnterContext() {
        assertEquals(ProgramContext.ENTER, ProgramContext.fromValue(0));
    }

    @Test
    void fromValue_shouldMapReenterContext() {
        assertEquals(ProgramContext.REENTER, ProgramContext.fromValue(1));
    }

    @Test
    void fromValue_shouldThrowForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> ProgramContext.fromValue(2));
    }

    @Test
    void getValue_shouldReturnNumericValue() {
        assertEquals(0, ProgramContext.ENTER.getValue());
        assertEquals(1, ProgramContext.REENTER.getValue());
    }
}
