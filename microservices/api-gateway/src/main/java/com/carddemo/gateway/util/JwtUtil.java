package com.carddemo.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT utility for token validation and claims extraction.
 *
 * Maps to the COMMAREA user context from COCOM01Y.cpy:
 * - CDEMO-USER-ID (PIC X(08)) -> "userId" claim / X-User-Id header
 * - CDEMO-USER-TYPE (PIC X(01)) -> "userType" claim / X-User-Type header
 *   - 'A' = Admin (CDEMO-USRTYP-ADMIN)
 *   - 'U' = Regular user (CDEMO-USRTYP-USER)
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            this.secretKey = Keys.hmacShaKeyFor(paddedKey);
        } else {
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * Validate the JWT token and return its claims.
     *
     * @param token the JWT token string
     * @return the parsed Claims
     * @throws JwtException if the token is invalid or expired
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the user ID from JWT claims.
     * Corresponds to CDEMO-USER-ID in COCOM01Y.cpy.
     */
    public String getUserId(Claims claims) {
        String userId = claims.get("userId", String.class);
        return userId != null ? userId : claims.getSubject();
    }

    /**
     * Extract the user type from JWT claims.
     * Corresponds to CDEMO-USER-TYPE in COCOM01Y.cpy:
     * - 'A' for admin (CDEMO-USRTYP-ADMIN)
     * - 'U' for regular user (CDEMO-USRTYP-USER)
     */
    public String getUserType(Claims claims) {
        String userType = claims.get("userType", String.class);
        return userType != null ? userType : "U";
    }

    /**
     * Check if the user type indicates admin access.
     * Mirrors the CDEMO-USRTYP-ADMIN (VALUE 'A') check in COCOM01Y.cpy.
     */
    public boolean isAdmin(Claims claims) {
        return "A".equals(getUserType(claims));
    }
}
