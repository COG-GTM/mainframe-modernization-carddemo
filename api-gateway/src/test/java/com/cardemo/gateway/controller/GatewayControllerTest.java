package com.cardemo.gateway.controller;

import com.cardemo.gateway.config.GatewayConfig;
import com.cardemo.gateway.config.SecurityConfig;
import com.cardemo.gateway.filter.JwtAuthenticationFilter;
import com.cardemo.gateway.service.JwtTokenProvider;
import com.cardemo.gateway.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for the API Gateway.
 * Tests route resolution, access control, and session context handling.
 */
@WebMvcTest(GatewayController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RoutingService.class, GatewayConfig.class})
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void health_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("api-gateway"));
    }

    @Test
    void routes_shouldReturnServiceList() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/routes"))
                .andExpect(status().isOk());
    }

    @Test
    void xctlRoute_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "COACTVWC"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void xctlRoute_withRegularUser_shouldResolveAccountView() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "COACTVWC", "programContext": 0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedPath").value("/api/v1/accounts/{id}"))
                .andExpect(jsonPath("$.serviceName").value("account-service"))
                .andExpect(jsonPath("$.httpMethod").value("GET"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void xctlRoute_regularUserAccessAdminRoute_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "COUSR00C"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void xctlRoute_adminUserCanAccessAdminRoute() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "COUSR00C"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedPath").value("/api/v1/users"))
                .andExpect(jsonPath("$.requiresAdmin").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void linkRoute_shouldResolveAndPreserveContext() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "COTRN00C", "programContext": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedPath").value("/api/v1/transactions"))
                .andExpect(jsonPath("$.programContext").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void navigate_shouldResolveByProgramName() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/navigate/COCRDLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedPath").value("/api/v1/cards"))
                .andExpect(jsonPath("$.serviceName").value("card-service"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void xctlRoute_unknownProgram_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": "UNKNOWN1"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void xctlRoute_missingTargetProgram_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/gateway/route/xctl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetProgram": ""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
