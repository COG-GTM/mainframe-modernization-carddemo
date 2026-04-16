package com.cardemo.gateway.dto;

/**
 * Response DTO for route resolution.
 * Contains the resolved REST API path and service information.
 */
public record RouteResponse(
        String resolvedPath,
        String serviceName,
        String serviceUrl,
        String httpMethod,
        String programName,
        String description,
        int programContext,
        boolean requiresAdmin
) {
}
