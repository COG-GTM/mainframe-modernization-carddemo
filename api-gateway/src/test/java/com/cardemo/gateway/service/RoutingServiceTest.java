package com.cardemo.gateway.service;

import com.cardemo.gateway.config.GatewayConfig;
import com.cardemo.gateway.dto.RouteResponse;
import com.cardemo.gateway.dto.ServiceRouteInfo;
import com.cardemo.gateway.enums.ProgramContext;
import com.cardemo.gateway.enums.RouteDefinition;
import com.cardemo.gateway.enums.UserType;
import com.cardemo.gateway.exception.AccessDeniedException;
import com.cardemo.gateway.exception.RouteNotFoundException;
import com.cardemo.gateway.model.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RoutingService — verifies XCTL/LINK routing semantics,
 * access control, and route resolution from COBOL program names.
 */
class RoutingServiceTest {

    private RoutingService routingService;
    private GatewayConfig gatewayConfig;

    @BeforeEach
    void setUp() {
        gatewayConfig = new GatewayConfig();
        gatewayConfig.setServices(Map.of(
                "auth-service", "http://localhost:8081",
                "menu-navigation-service", "http://localhost:8082",
                "account-service", "http://localhost:8083",
                "card-service", "http://localhost:8084",
                "transaction-service", "http://localhost:8085",
                "reports-service", "http://localhost:8086"
        ));
        routingService = new RoutingService(gatewayConfig);
    }

    @Test
    void resolveXctl_shouldRouteAccountView() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);
        RouteResponse response = routingService.resolveXctl("COACTVWC", context);

        assertEquals("/api/v1/accounts/{id}", response.resolvedPath());
        assertEquals("account-service", response.serviceName());
        assertEquals("GET", response.httpMethod());
        assertEquals(0, response.programContext()); // XCTL resets to ENTER
    }

    @Test
    void resolveXctl_shouldSetProgramContextToEnter() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);
        context.setProgramContext(ProgramContext.REENTER);

        routingService.resolveXctl("COACTVWC", context);

        // XCTL always sets context to ENTER (fresh entry)
        assertEquals(ProgramContext.ENTER, context.getProgramContext());
    }

    @Test
    void resolveLink_shouldPreserveProgramContext() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);
        context.setProgramContext(ProgramContext.REENTER);

        RouteResponse response = routingService.resolveLink("COACTVWC", context);

        // LINK preserves the current context
        assertEquals(1, response.programContext());
    }

    @Test
    void resolveXctl_adminUserCanAccessAdminRoutes() {
        SessionContext context = new SessionContext("ADMIN001", UserType.ADMIN);
        RouteResponse response = routingService.resolveXctl("COUSR00C", context);

        assertEquals("/api/v1/users", response.resolvedPath());
        assertEquals("auth-service", response.serviceName());
        assertTrue(response.requiresAdmin());
    }

    @Test
    void resolveXctl_regularUserCannotAccessAdminRoutes() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> routingService.resolveXctl("COUSR00C", context));
        assertTrue(ex.getMessage().contains("No access - Admin Only option"));
    }

    @Test
    void resolveXctl_unknownProgramThrowsRouteNotFound() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);

        assertThrows(RouteNotFoundException.class,
                () -> routingService.resolveXctl("UNKNOWN1", context));
    }

    @Test
    void resolveRoute_shouldMapAllRegularMenuPrograms() {
        // All 10 regular menu options from COMEN02Y.cpy
        assertNotNull(routingService.resolveRoute("COACTVWC")); // Account View
        assertNotNull(routingService.resolveRoute("COACTUPC")); // Account Update
        assertNotNull(routingService.resolveRoute("COCRDLIC")); // Card List
        assertNotNull(routingService.resolveRoute("COCRDSLC")); // Card View
        assertNotNull(routingService.resolveRoute("COCRDUPC")); // Card Update
        assertNotNull(routingService.resolveRoute("COTRN00C")); // Transaction List
        assertNotNull(routingService.resolveRoute("COTRN01C")); // Transaction View
        assertNotNull(routingService.resolveRoute("COTRN02C")); // Transaction Add
        assertNotNull(routingService.resolveRoute("CORPT00C")); // Reports
        assertNotNull(routingService.resolveRoute("COBIL00C")); // Bill Payment
    }

    @Test
    void resolveRoute_shouldMapAllAdminMenuPrograms() {
        // All 4 admin menu options from COADM02Y.cpy
        assertNotNull(routingService.resolveRoute("COUSR00C")); // User List
        assertNotNull(routingService.resolveRoute("COUSR01C")); // User Add
        assertNotNull(routingService.resolveRoute("COUSR02C")); // User Update
        assertNotNull(routingService.resolveRoute("COUSR03C")); // User Delete
    }

    @Test
    void resolveRoute_shouldMapSignonProgram() {
        RouteDefinition route = routingService.resolveRoute("COSGN00C");
        assertEquals("/api/v1/auth/login", route.getRoutePath());
        assertTrue(route.isPublic());
    }

    @Test
    void validateAccess_publicRouteAllowsAnyUser() {
        RouteDefinition signon = routingService.resolveRoute("COSGN00C");
        assertDoesNotThrow(() -> routingService.validateAccess(signon, UserType.USER));
        assertDoesNotThrow(() -> routingService.validateAccess(signon, UserType.ADMIN));
    }

    @Test
    void validateAccess_adminRouteBlocksRegularUser() {
        RouteDefinition userList = routingService.resolveRoute("COUSR00C");
        assertThrows(AccessDeniedException.class,
                () -> routingService.validateAccess(userList, UserType.USER));
    }

    @Test
    void validateAccess_adminRouteAllowsAdminUser() {
        RouteDefinition userList = routingService.resolveRoute("COUSR00C");
        assertDoesNotThrow(() -> routingService.validateAccess(userList, UserType.ADMIN));
    }

    @Test
    void resolveServiceUrl_shouldBuildFullUrl() {
        RouteDefinition route = routingService.resolveRoute("COACTVWC");
        String url = routingService.resolveServiceUrl(route);
        assertEquals("http://localhost:8083/api/v1/accounts/{id}", url);
    }

    @Test
    void getServiceRoutes_shouldReturnAllServices() {
        List<ServiceRouteInfo> services = routingService.getServiceRoutes();
        assertFalse(services.isEmpty());

        // Should have routes for all 6 services
        List<String> serviceNames = services.stream()
                .map(ServiceRouteInfo::serviceName)
                .toList();
        assertTrue(serviceNames.contains("auth-service"));
        assertTrue(serviceNames.contains("account-service"));
        assertTrue(serviceNames.contains("card-service"));
        assertTrue(serviceNames.contains("transaction-service"));
        assertTrue(serviceNames.contains("reports-service"));
    }

    @Test
    void resolveXctl_transactionRoutes() {
        SessionContext context = new SessionContext("USER0001", UserType.USER);

        RouteResponse listResponse = routingService.resolveXctl("COTRN00C", context);
        assertEquals("/api/v1/transactions", listResponse.resolvedPath());
        assertEquals("GET", listResponse.httpMethod());

        RouteResponse addResponse = routingService.resolveXctl("COTRN02C", context);
        assertEquals("/api/v1/transactions", addResponse.resolvedPath());
        assertEquals("POST", addResponse.httpMethod());
    }
}
