package com.cardemo.gateway.service;

import com.cardemo.gateway.enums.UserType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JwtTokenProvider — verifies JWT generation and validation
 * with COMMAREA identity field mapping (CDEMO-USER-ID → sub, CDEMO-USER-TYPE → userType).
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-for-jwt-signing-must-be-256-bits-long", 24);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = jwtTokenProvider.generateToken("USER0001", UserType.USER);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_shouldReturnClaimsForValidToken() {
        String token = jwtTokenProvider.generateToken("ADMIN001", UserType.ADMIN);
        Optional<Claims> claims = jwtTokenProvider.validateToken(token);

        assertTrue(claims.isPresent());
        assertEquals("ADMIN001", claims.get().getSubject());
    }

    @Test
    void validateToken_shouldReturnEmptyForInvalidToken() {
        Optional<Claims> claims = jwtTokenProvider.validateToken("invalid.token.here");
        assertTrue(claims.isEmpty());
    }

    @Test
    void getUserId_shouldExtractSubjectClaim() {
        String token = jwtTokenProvider.generateToken("USER0001", UserType.USER);
        Claims claims = jwtTokenProvider.validateToken(token).orElseThrow();

        assertEquals("USER0001", jwtTokenProvider.getUserId(claims));
    }

    @Test
    void getUserType_shouldMapAdminType() {
        String token = jwtTokenProvider.generateToken("ADMIN001", UserType.ADMIN);
        Claims claims = jwtTokenProvider.validateToken(token).orElseThrow();

        assertEquals(UserType.ADMIN, jwtTokenProvider.getUserType(claims));
    }

    @Test
    void getUserType_shouldMapRegularUserType() {
        String token = jwtTokenProvider.generateToken("USER0001", UserType.USER);
        Claims claims = jwtTokenProvider.validateToken(token).orElseThrow();

        assertEquals(UserType.USER, jwtTokenProvider.getUserType(claims));
    }

    @Test
    void getUserTypeCode_shouldReturnRawCode() {
        String token = jwtTokenProvider.generateToken("ADMIN001", UserType.ADMIN);
        Claims claims = jwtTokenProvider.validateToken(token).orElseThrow();

        assertEquals("A", jwtTokenProvider.getUserTypeCode(claims));
    }

    @Test
    void validateToken_shouldReturnEmptyForNullToken() {
        Optional<Claims> claims = jwtTokenProvider.validateToken(null);
        assertTrue(claims.isEmpty());
    }

    @Test
    void validateToken_shouldReturnEmptyForEmptyToken() {
        Optional<Claims> claims = jwtTokenProvider.validateToken("");
        assertTrue(claims.isEmpty());
    }
}
