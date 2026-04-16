package com.cardemo.gateway.config;

import com.cardemo.gateway.controller.GatewayController;
import com.cardemo.gateway.filter.JwtAuthenticationFilter;
import com.cardemo.gateway.service.JwtTokenProvider;
import com.cardemo.gateway.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for SecurityConfig — verifies role-based access control on routes.
 * Maps COBOL access patterns:
 *   Admin endpoints (COUSR00C-03C) → ROLE_ADMIN required
 *   Regular endpoints → ROLE_USER required
 *   Auth login → public
 */
@WebMvcTest(GatewayController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RoutingService.class, GatewayConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void publicEndpoint_healthCheck_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/navigate/COACTVWC"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularEndpoint_withUserRole_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/navigate/COACTVWC"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_withAdminRole_shouldNotBeForbidden() throws Exception {
        // Admin endpoints for user management — no controller handles /api/v1/users
        // directly in the gateway, so we just verify the security layer does not reject.
        // A 404 or 500 is acceptable; 403 would mean the role check failed.
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertNotEquals(403, status, "Admin should not be forbidden from /api/v1/users");
                });
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }
}
