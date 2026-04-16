package com.cardemo.gateway.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteDefinition enum — verifies COBOL program name to REST route mapping.
 */
class RouteDefinitionTest {

    @Test
    void fromProgramName_shouldFindAccountView() {
        RouteDefinition route = RouteDefinition.fromProgramName("COACTVWC");
        assertNotNull(route);
        assertEquals(RouteDefinition.ACCOUNT_VIEW, route);
        assertEquals("/api/v1/accounts/{id}", route.getRoutePath());
    }

    @Test
    void fromProgramName_shouldBeCaseInsensitive() {
        RouteDefinition route = RouteDefinition.fromProgramName("coactvwc");
        assertNotNull(route);
        assertEquals(RouteDefinition.ACCOUNT_VIEW, route);
    }

    @Test
    void fromProgramName_shouldReturnNullForUnknown() {
        assertNull(RouteDefinition.fromProgramName("UNKNOWN1"));
    }

    @Test
    void requiresAdmin_shouldBeTrueForAdminRoutes() {
        assertTrue(RouteDefinition.USER_LIST.requiresAdmin());
        assertTrue(RouteDefinition.USER_ADD.requiresAdmin());
        assertTrue(RouteDefinition.USER_UPDATE.requiresAdmin());
        assertTrue(RouteDefinition.USER_DELETE.requiresAdmin());
    }

    @Test
    void requiresAdmin_shouldBeFalseForRegularRoutes() {
        assertFalse(RouteDefinition.ACCOUNT_VIEW.requiresAdmin());
        assertFalse(RouteDefinition.CARD_LIST.requiresAdmin());
        assertFalse(RouteDefinition.TRANSACTION_LIST.requiresAdmin());
    }

    @Test
    void isPublic_shouldBeTrueForSignonOnly() {
        assertTrue(RouteDefinition.SIGNON.isPublic());
        assertFalse(RouteDefinition.ACCOUNT_VIEW.isPublic());
        assertFalse(RouteDefinition.USER_LIST.isPublic());
    }

    @Test
    void allRoutesHaveServiceNames() {
        for (RouteDefinition route : RouteDefinition.values()) {
            assertNotNull(route.getServiceName(), "Missing service name for " + route.name());
            assertFalse(route.getServiceName().isEmpty());
        }
    }

    @Test
    void allRoutesHaveHttpMethods() {
        for (RouteDefinition route : RouteDefinition.values()) {
            assertNotNull(route.getHttpMethod(), "Missing HTTP method for " + route.name());
        }
    }
}
