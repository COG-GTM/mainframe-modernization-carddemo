package com.cardemo.gateway.service;

import com.cardemo.gateway.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * JWT token provider mapping COMMAREA identity fields to JWT claims.
 *
 * COBOL mapping:
 *   CDEMO-USER-ID   PIC X(08) → JWT subject claim (sub)
 *   CDEMO-USER-TYPE  PIC X(01) → JWT "userType" claim (A or U)
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_USER_ID = "userId";

    private final SecretKey signingKey;
    private final long expirationHours;

    public JwtTokenProvider(
            @Value("${gateway.jwt.secret}") String secret,
            @Value("${gateway.jwt.expiration-hours:24}") long expirationHours) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String generateToken(String userId, UserType userType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USER_TYPE, userType.getCode())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Claims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    public UserType getUserType(Claims claims) {
        String typeCode = claims.get(CLAIM_USER_TYPE, String.class);
        return UserType.fromCode(typeCode);
    }

    public String getUserTypeCode(Claims claims) {
        return claims.get(CLAIM_USER_TYPE, String.class);
    }
}
