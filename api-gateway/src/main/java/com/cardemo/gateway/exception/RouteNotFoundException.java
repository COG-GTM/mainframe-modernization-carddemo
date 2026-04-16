package com.cardemo.gateway.exception;

/**
 * Thrown when a COBOL program name cannot be mapped to a REST API route.
 */
public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String message) {
        super(message);
    }
}
