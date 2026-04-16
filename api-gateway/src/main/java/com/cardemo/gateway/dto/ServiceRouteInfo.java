package com.cardemo.gateway.dto;

import java.util.List;

/**
 * DTO representing a downstream service and its available routes.
 */
public record ServiceRouteInfo(
        String serviceName,
        String baseUrl,
        boolean healthy,
        List<RouteInfo> routes
) {
    public record RouteInfo(
            String programName,
            String description,
            String path,
            String httpMethod,
            boolean requiresAdmin
    ) {
    }
}
