package com.carddemo.gateway.filter;

import com.carddemo.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global authentication filter that validates JWT tokens and forwards user context.
 *
 * Modernized from the authentication logic in COMEN01C.cbl and COADM01C.cbl:
 * - Original: COSGN00C validates credentials, sets CDEMO-USER-ID and CDEMO-USER-TYPE
 *   in CARDDEMO-COMMAREA (COCOM01Y.cpy), then XCTL transfers to menu programs
 * - Modernized: JWT token carries the same user context; this filter validates the
 *   token and passes X-User-Id and X-User-Type headers downstream (equivalent to COMMAREA)
 *
 * Public routes (auth service, health) bypass authentication, similar to how
 * COSGN00C was the entry point before any COMMAREA context existed.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth",
            "/actuator/health"
    );

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = jwtUtil.validateToken(token);
            String userId = jwtUtil.getUserId(claims);
            String userType = jwtUtil.getUserType(claims);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Type", userType)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (JwtException e) {
            log.warn("Invalid JWT token for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
