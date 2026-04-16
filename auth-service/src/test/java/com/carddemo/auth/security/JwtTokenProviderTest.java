package com.carddemo.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hmac-sha",
                86400000L
        );
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");
        Optional<String> userId = jwtTokenProvider.getUserIdFromToken(token);
        assertTrue(userId.isPresent());
        assertEquals("ADMIN001", userId.get());
    }

    @Test
    void getUserTypeFromToken_shouldReturnCorrectUserType() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");
        Optional<String> userType = jwtTokenProvider.getUserTypeFromToken(token);
        assertTrue(userType.isPresent());
        assertEquals("A", userType.get());
    }

    @Test
    void getUserTypeFromToken_shouldReturnUserTypeForRegularUser() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");
        Optional<String> userType = jwtTokenProvider.getUserTypeFromToken(token);
        assertTrue(userType.isPresent());
        assertEquals("U", userType.get());
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void getUserIdFromToken_shouldReturnEmptyForInvalidToken() {
        Optional<String> userId = jwtTokenProvider.getUserIdFromToken("invalid.token");
        assertTrue(userId.isEmpty());
    }

    @Test
    void generateToken_shouldProduceExpiredToken_whenExpirationIsZero() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hmac-sha",
                0L
        );
        String token = shortLived.generateToken("USER0001", "U");
        assertNotNull(token);
        // Token is immediately expired
        assertFalse(shortLived.validateToken(token));
    }
}
