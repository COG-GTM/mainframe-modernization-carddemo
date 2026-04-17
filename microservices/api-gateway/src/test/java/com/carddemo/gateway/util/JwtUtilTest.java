package com.carddemo.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JwtUtil - validates JWT parsing logic that maps to
 * CDEMO-USER-ID and CDEMO-USER-TYPE from COCOM01Y.cpy.
 */
class JwtUtilTest {

    private static final String SECRET = "carddemo-jwt-secret-key-change-in-production";
    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void validateToken_withValidToken_returnsClaims() {
        String token = Jwts.builder()
                .subject("USER0001")
                .claim("userId", "USER0001")
                .claim("userType", "U")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("USER0001", claims.getSubject());
        assertEquals("USER0001", claims.get("userId", String.class));
        assertEquals("U", claims.get("userType", String.class));
    }

    @Test
    void validateToken_withExpiredToken_throwsException() {
        String token = Jwts.builder()
                .subject("USER0001")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(signingKey)
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_withInvalidSignature_throwsException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "wrong-secret-key-that-is-long-enough-for-hmac".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("USER0001")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey)
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_withMalformedToken_throwsException() {
        assertThrows(JwtException.class, () -> jwtUtil.validateToken("not.a.valid.token"));
    }

    @Test
    void getUserId_returnsUserIdClaim() {
        String token = Jwts.builder()
                .subject("USER0001")
                .claim("userId", "USER0001")
                .claim("userType", "U")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertEquals("USER0001", jwtUtil.getUserId(claims));
    }

    @Test
    void getUserId_fallsBackToSubject_whenUserIdClaimMissing() {
        String token = Jwts.builder()
                .subject("USER0001")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertEquals("USER0001", jwtUtil.getUserId(claims));
    }

    @Test
    void getUserType_returnsUserTypeClaim() {
        String token = Jwts.builder()
                .subject("ADMIN001")
                .claim("userId", "ADMIN001")
                .claim("userType", "A")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertEquals("A", jwtUtil.getUserType(claims));
    }

    @Test
    void getUserType_defaultsToUser_whenClaimMissing() {
        String token = Jwts.builder()
                .subject("USER0001")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertEquals("U", jwtUtil.getUserType(claims));
    }

    @Test
    void isAdmin_returnsTrueForAdminType() {
        String token = Jwts.builder()
                .subject("ADMIN001")
                .claim("userType", "A")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertTrue(jwtUtil.isAdmin(claims));
    }

    @Test
    void isAdmin_returnsFalseForRegularUser() {
        String token = Jwts.builder()
                .subject("USER0001")
                .claim("userType", "U")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateToken(token);
        assertFalse(jwtUtil.isAdmin(claims));
    }
}
