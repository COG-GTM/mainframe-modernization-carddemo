package com.carddemo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RoleBasedAuthorizationFilter - validates the admin-only route
 * protection that mirrors the COBOL check in COMEN01C.cbl:
 *   IF CDEMO-USRTYP-USER AND CDEMO-MENU-OPT-USRTYPE(WS-OPTION) = 'A'
 *       MOVE 'No access - Admin Only option...' TO WS-MESSAGE
 *
 * Admin routes correspond to COADM02Y.cpy options (COUSR00C-03C user management).
 */
class RoleBasedAuthorizationFilterTest {

    private RoleBasedAuthorizationFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RoleBasedAuthorizationFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_allowsAdminAccessToUserRoutes() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/list")
                .header("X-User-Type", "A")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_blocksRegularUserFromUserRoutes() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/list")
                .header("X-User-Type", "U")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_blocksRequestWithNoUserTypeHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/list")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_allowsRegularUserAccessToNonAdminRoutes() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/12345")
                .header("X-User-Type", "U")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_allowsAdminAccessToNonAdminRoutes() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/transactions/list")
                .header("X-User-Type", "A")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void filter_blocksRegularUserFromNestedUserPaths() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/users/create")
                .header("X-User-Type", "U")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_allowsAdminToDeleteUser() {
        MockServerHttpRequest request = MockServerHttpRequest
                .delete("/api/users/USER0002")
                .header("X-User-Type", "A")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    @Test
    void getOrder_returnsNegativeOne() {
        assertEquals(-1, filter.getOrder());
    }
}
