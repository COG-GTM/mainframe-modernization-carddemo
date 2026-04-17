package com.carddemo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Role-based authorization filter for admin-only routes.
 *
 * Modernized from the role check in COMEN01C.cbl (lines 136-143):
 *   IF CDEMO-USRTYP-USER AND
 *      CDEMO-MENU-OPT-USRTYPE(WS-OPTION) = 'A'
 *       MOVE 'No access - Admin Only option... ' TO WS-MESSAGE
 *
 * In the original COBOL, COADM01C.cbl was the admin-only menu (COADM02Y.cpy)
 * that routed to COUSR00C-03C for user management. Only users with
 * CDEMO-USER-TYPE = 'A' could access these programs.
 *
 * This filter enforces the same constraint: /api/users/** routes require
 * the X-User-Type header to be 'A' (set by AuthenticationFilter from JWT claims).
 */
@Component
public class RoleBasedAuthorizationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RoleBasedAuthorizationFilter.class);

    private static final List<String> ADMIN_ONLY_PATHS = List.of(
            "/api/users"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!isAdminOnlyPath(path)) {
            return chain.filter(exchange);
        }

        String userType = exchange.getRequest().getHeaders().getFirst("X-User-Type");

        if (!"A".equals(userType)) {
            log.warn("Non-admin user attempted to access admin-only path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isAdminOnlyPath(String path) {
        return ADMIN_ONLY_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
