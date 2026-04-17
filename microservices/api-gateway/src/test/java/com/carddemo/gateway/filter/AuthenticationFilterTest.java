package com.carddemo.gateway.filter;

import com.carddemo.gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AuthenticationFilter - validates the JWT gate that replaces
 * the COSGN00C sign-on check before allowing access to menu programs.
 */
class AuthenticationFilterTest {

    private static final String SECRET = "carddemo-jwt-secret-key-change-in-production";
    private AuthenticationFilter filter;
    private GatewayFilterChain chain;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);
        filter = new AuthenticationFilter(jwtUtil);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(String userId, String userType) {
        return Jwts.builder()
                .subject(userId)
                .claim("userId", userId)
                .claim("userType", userType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void filter_allowsPublicAuthPaths() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_allowsHealthEndpoint() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void filter_rejectsRequestWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_rejectsRequestWithInvalidAuthScheme() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_rejectsRequestWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_passesValidTokenAndSetsHeaders() {
        String token = createToken("USER0001", "U");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            String userType = ex.getRequest().getHeaders().getFirst("X-User-Type");
            return "USER0001".equals(userId) && "U".equals(userType);
        }));
    }

    @Test
    void filter_passesAdminTokenWithCorrectHeaders() {
        String token = createToken("ADMIN001", "A");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            String userType = ex.getRequest().getHeaders().getFirst("X-User-Type");
            return "ADMIN001".equals(userId) && "A".equals(userType);
        }));
    }

    @Test
    void filter_rejectsExpiredToken() {
        String token = Jwts.builder()
                .subject("USER0001")
                .claim("userId", "USER0001")
                .claim("userType", "U")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(signingKey)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void getOrder_returnsNegativeTwo() {
        assertEquals(-2, filter.getOrder());
    }
}
