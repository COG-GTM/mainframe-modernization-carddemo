package com.carddemo.service;

import com.carddemo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider("TestSecretKeyForJWTTokenGenerationMustBeLongEnough256Bits!!", 3600000);
    }

    @Test
    void createAndValidateToken() {
        String token = provider.createToken("ADMIN001", "ADMIN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("ADMIN001", provider.getUserId(token));
        assertEquals("ADMIN", provider.getRole(token));
    }

    @Test
    void invalidToken() {
        assertFalse(provider.validateToken("invalid-token"));
    }

    @Test
    void nullToken() {
        assertFalse(provider.validateToken(null));
    }
}
