package com.carddemo.auth.service;

import com.carddemo.auth.config.JwtConfig;
import com.carddemo.auth.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for JWT token generation and parsing.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-only-jwt-secret-key-not-for-production");
        jwtConfig.setExpiration(86400000L);
        jwtService = new JwtService(jwtConfig);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        User user = new User("USER0001", "Regular", "User", "hashedpwd", "U");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void parseToken_shouldReturnCorrectClaims() {
        User user = new User("USER0001", "Regular", "User", "hashedpwd", "U");

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);

        assertEquals("USER0001", claims.getSubject());
        assertEquals("USER0001", claims.get("userId", String.class));
        assertEquals("U", claims.get("userType", String.class));
        assertEquals("Regular", claims.get("firstName", String.class));
        assertEquals("User", claims.get("lastName", String.class));
    }

    @Test
    void generateToken_adminUser_shouldContainAdminType() {
        User admin = new User("ADMIN001", "Admin", "User", "hashedpwd", "A");

        String token = jwtService.generateToken(admin);
        Claims claims = jwtService.parseToken(token);

        assertEquals("ADMIN001", claims.getSubject());
        assertEquals("A", claims.get("userType", String.class));
        assertEquals("Admin", claims.get("firstName", String.class));
    }

    @Test
    void parseToken_shouldHaveExpirationDate() {
        User user = new User("USER0001", "Regular", "User", "hashedpwd", "U");

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);

        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}
