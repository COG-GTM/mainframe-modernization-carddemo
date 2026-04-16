package com.carddemo.menu.exception;

/**
 * Thrown when a navigation context is not found for the given session.
 */
public class NavigationContextNotFoundException extends RuntimeException {

    public NavigationContextNotFoundException(String sessionId) {
        super("Navigation context not found for session: " + sessionId);
    }
}
