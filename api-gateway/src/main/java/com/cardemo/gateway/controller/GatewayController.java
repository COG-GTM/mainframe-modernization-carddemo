package com.cardemo.gateway.controller;

import com.cardemo.gateway.dto.RouteRequest;
import com.cardemo.gateway.dto.RouteResponse;
import com.cardemo.gateway.dto.ServiceRouteInfo;
import com.cardemo.gateway.enums.ProgramContext;
import com.cardemo.gateway.enums.UserType;
import com.cardemo.gateway.model.SessionContext;
import com.cardemo.gateway.service.JwtTokenProvider;
import com.cardemo.gateway.service.RoutingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Gateway controller providing route resolution and navigation endpoints.
 *
 * Replaces the CICS XCTL/LINK routing pattern with REST API endpoints.
 * The controller resolves COBOL program names to downstream service URLs
 * and manages the session context (modernized COMMAREA).
 */
@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    private final RoutingService routingService;
    private final JwtTokenProvider jwtTokenProvider;

    public GatewayController(RoutingService routingService, JwtTokenProvider jwtTokenProvider) {
        this.routingService = routingService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Resolve a route via XCTL semantics (transfer control, no return).
     * The caller does NOT resume — equivalent to navigating away.
     *
     * COBOL: EXEC CICS XCTL PROGRAM(target) COMMAREA(data)
     */
    @PostMapping("/route/xctl")
    public ResponseEntity<RouteResponse> routeXctl(
            @Valid @RequestBody RouteRequest request,
            Authentication authentication) {

        SessionContext context = buildSessionContext(authentication, request);
        RouteResponse response = routingService.resolveXctl(request.targetProgram(), context);
        return ResponseEntity.ok(response);
    }

    /**
     * Resolve a route via LINK semantics (subroutine call, caller resumes).
     * The caller DOES resume after the target returns.
     *
     * COBOL: EXEC CICS LINK PROGRAM(target) COMMAREA(data)
     */
    @PostMapping("/route/link")
    public ResponseEntity<RouteResponse> routeLink(
            @Valid @RequestBody RouteRequest request,
            Authentication authentication) {

        SessionContext context = buildSessionContext(authentication, request);
        RouteResponse response = routingService.resolveLink(request.targetProgram(), context);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all available service routes grouped by downstream service.
     * Equivalent to building the menu options from COMEN02Y and COADM02Y copybooks.
     */
    @GetMapping("/routes")
    public ResponseEntity<List<ServiceRouteInfo>> getRoutes() {
        return ResponseEntity.ok(routingService.getServiceRoutes());
    }

    /**
     * Navigate to a program by name — resolves the route and returns routing info.
     * Used by the frontend to determine which service endpoint to call.
     *
     * Maps the COBOL menu selection pattern:
     *   XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME(WS-OPTION))
     */
    @GetMapping("/navigate/{programName}")
    public ResponseEntity<RouteResponse> navigate(
            @PathVariable String programName,
            @RequestParam(defaultValue = "0") int programContext,
            Authentication authentication) {

        SessionContext context = buildSessionContext(authentication, null);
        context.setProgramContext(ProgramContext.fromValue(programContext));

        RouteResponse response = routingService.resolveXctl(programName, context);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for the gateway.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "version", "1.0.0"
        ));
    }

    private SessionContext buildSessionContext(Authentication authentication, RouteRequest request) {
        String userId = authentication != null ? authentication.getName() : "ANONYMOUS";
        UserType userType = resolveUserType(authentication);

        SessionContext context = new SessionContext(userId, userType);

        if (request != null) {
            int pgmCtx = request.programContext() != null ? request.programContext() : 0;
            context.setProgramContext(ProgramContext.fromValue(pgmCtx));
            context.setToProgram(request.targetProgram());

            if (request.accountId() != null) {
                context.setAccountId(request.accountId());
            }
            if (request.cardNumber() != null) {
                context.setCardNumber(request.cardNumber());
            }
            if (request.customerId() != null) {
                context.setCustomerId(request.customerId());
            }
        }

        return context;
    }

    private UserType resolveUserType(Authentication authentication) {
        if (authentication == null) {
            return UserType.USER;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        return isAdmin ? UserType.ADMIN : UserType.USER;
    }
}
