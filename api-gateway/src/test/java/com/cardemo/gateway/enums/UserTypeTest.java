package com.cardemo.gateway.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserType enum — verifies CDEMO-USER-TYPE code mapping.
 */
class UserTypeTest {

    @Test
    void fromCode_shouldMapAdminCode() {
        assertEquals(UserType.ADMIN, UserType.fromCode("A"));
    }

    @Test
    void fromCode_shouldMapUserCode() {
        assertEquals(UserType.USER, UserType.fromCode("U"));
    }

    @Test
    void fromCode_shouldBeCaseInsensitive() {
        assertEquals(UserType.ADMIN, UserType.fromCode("a"));
        assertEquals(UserType.USER, UserType.fromCode("u"));
    }

    @Test
    void fromCode_shouldThrowForUnknownCode() {
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode("X"));
    }

    @Test
    void getCode_shouldReturnOriginalCode() {
        assertEquals("A", UserType.ADMIN.getCode());
        assertEquals("U", UserType.USER.getCode());
    }
}
