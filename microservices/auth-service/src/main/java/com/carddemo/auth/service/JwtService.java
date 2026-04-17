package com.carddemo.auth.service;

import com.carddemo.auth.config.JwtConfig;
import com.carddemo.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token generation and parsing service.
 * Generates tokens with claims that mirror the COBOL COMMAREA fields:
 *   - userId   (CDEMO-USER-ID)
 *   - userType (CDEMO-USER-TYPE: 'A' = admin, 'U' = regular)
 *   - firstName, lastName from SEC-USER-DATA
 */
@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(user.getUsrId())
                .claim("userId", user.getUsrId())
                .claim("userType", user.getUsrType())
                .claim("firstName", user.getUsrFname())
                .claim("lastName", user.getUsrLname())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        // Pad short secrets to 32 bytes (HMAC-SHA256 minimum).
        // This matches the identical logic in api-gateway's JwtUtil so both
        // services derive the same signing key from the same configured secret.
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(paddedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
