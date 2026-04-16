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
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementing CICS routing semantics as REST API operations.
 *
 * XCTL (transfer control, no return):
 *   COBOL: EXEC CICS XCTL PROGRAM('COACTVWC') COMMAREA(data)
 *   Java:  resolveXctl("COACTVWC", context) → RouteResponse with redirect info
 *   The caller does NOT resume — equivalent to an HTTP redirect or forward.
 *
 * LINK (subroutine call, caller resumes):
 *   COBOL: EXEC CICS LINK PROGRAM('SUBPGM') COMMAREA(data)
 *   Java:  resolveLink("SUBPGM", context) → RouteResponse with service call info
 *   The caller DOES resume — equivalent to a synchronous service method call.
 */
@Service
public class RoutingService {

    private final GatewayConfig gatewayConfig;

    public RoutingService(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Resolves a route for XCTL-equivalent routing (transfer control, no return).
     * Maps COBOL program name to REST API route and validates access.
     *
     * In COBOL: EXEC CICS XCTL PROGRAM(name) COMMAREA(data)
     * The calling program does not get control back.
     */
    public RouteResponse resolveXctl(String targetProgram, SessionContext context) {
        RouteDefinition route = resolveRoute(targetProgram);
        validateAccess(route, context.getUserType());

        // XCTL sets context to ENTER (0) — fresh entry into the target program
        context.setProgramContext(ProgramContext.ENTER);
        context.setFromProgram(context.getToProgram());
        context.setToProgram(targetProgram);

        return buildRouteResponse(route, context);
    }

    /**
     * Resolves a route for LINK-equivalent routing (subroutine call, caller resumes).
     * Maps COBOL program name to REST API route and validates access.
     *
     * In COBOL: EXEC CICS LINK PROGRAM(name) COMMAREA(data)
     * The calling program resumes after the linked program returns.
     */
    public RouteResponse resolveLink(String targetProgram, SessionContext context) {
        RouteDefinition route = resolveRoute(targetProgram);
        validateAccess(route, context.getUserType());

        // LINK preserves the current program context
        return buildRouteResponse(route, context);
    }

    /**
     * Resolves a route by COBOL program name.
     */
    public RouteDefinition resolveRoute(String programName) {
        RouteDefinition route = RouteDefinition.fromProgramName(programName);
        if (route == null) {
            throw new RouteNotFoundException("Unknown program: " + programName);
        }
        return route;
    }

    /**
     * Validates that the user has access to the target route.
     * Maps COBOL pattern from COMEN01C:
     *   IF CDEMO-USRTYP-USER AND CDEMO-MENU-OPT-USRTYPE(WS-OPTION) = 'A'
     *       'No access - Admin Only option...'
     */
    public void validateAccess(RouteDefinition route, UserType userType) {
        if (route.isPublic()) {
            return;
        }
        if (route.requiresAdmin() && userType != UserType.ADMIN) {
            throw new AccessDeniedException(
                    "No access - Admin Only option... " + route.getDescription());
        }
    }

    /**
     * Returns all available routes grouped by service.
     */
    public List<ServiceRouteInfo> getServiceRoutes() {
        Map<String, List<RouteDefinition>> routesByService = Arrays.stream(RouteDefinition.values())
                .collect(Collectors.groupingBy(RouteDefinition::getServiceName));

        return routesByService.entrySet().stream()
                .map(entry -> {
                    String serviceName = entry.getKey();
                    String baseUrl = gatewayConfig.getServiceUrl(serviceName);
                    List<ServiceRouteInfo.RouteInfo> routes = entry.getValue().stream()
                            .map(rd -> new ServiceRouteInfo.RouteInfo(
                                    rd.getProgramName(),
                                    rd.getDescription(),
                                    rd.getRoutePath(),
                                    rd.getHttpMethod(),
                                    rd.requiresAdmin()
                            ))
                            .toList();
                    return new ServiceRouteInfo(serviceName, baseUrl, true, routes);
                })
                .toList();
    }

    /**
     * Resolves the full downstream URL for a given route.
     */
    public String resolveServiceUrl(RouteDefinition route) {
        String baseUrl = gatewayConfig.getServiceUrl(route.getServiceName());
        return baseUrl + route.getRoutePath();
    }

    private RouteResponse buildRouteResponse(RouteDefinition route, SessionContext context) {
        String serviceUrl = resolveServiceUrl(route);
        return new RouteResponse(
                route.getRoutePath(),
                route.getServiceName(),
                serviceUrl,
                route.getHttpMethod(),
                route.getProgramName(),
                route.getDescription(),
                context.getProgramContext().getValue(),
                route.requiresAdmin()
        );
    }
}
