package com.carddemo.menu.controller;

import com.carddemo.menu.dto.MenuItemResponse;
import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.dto.NavigationContextResponse;
import com.carddemo.menu.dto.NavigationResponse;
import com.carddemo.menu.exception.GlobalExceptionHandler;
import com.carddemo.menu.exception.InvalidMenuOptionException;
import com.carddemo.menu.exception.NavigationContextNotFoundException;
import com.carddemo.menu.exception.UnauthorizedMenuAccessException;
import com.carddemo.menu.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests using MockMvc.
 * Verifies HTTP response codes and JSON structure for the menu API.
 */
@WebMvcTest(MenuController.class)
@Import(GlobalExceptionHandler.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @Test
    void getMenu_defaultUserType_returns200() throws Exception {
        MenuResponse response = new MenuResponse("Main Menu", 1,
                List.of(new MenuItemResponse(1L, 1, "Account View", "COACTVWC", "U", "REGULAR", 1)));

        when(menuService.getMenuForUser("U")).thenReturn(response);

        mockMvc.perform(get("/api/v1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuType").value("Main Menu"))
                .andExpect(jsonPath("$.totalOptions").value(1))
                .andExpect(jsonPath("$.menuItems[0].optionName").value("Account View"));
    }

    @Test
    void getMenu_adminUserType_returns200() throws Exception {
        MenuResponse response = new MenuResponse("Admin Menu", 1,
                List.of(new MenuItemResponse(1L, 1, "User List (Security)", "COUSR00C", "A", "ADMIN", 1)));

        when(menuService.getMenuForUser("A")).thenReturn(response);

        mockMvc.perform(get("/api/v1/menu").param("userType", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuType").value("Admin Menu"))
                .andExpect(jsonPath("$.menuItems[0].programName").value("COUSR00C"));
    }

    @Test
    void getMenu_invalidUserType_returns400() throws Exception {
        when(menuService.getMenuForUser("X"))
                .thenThrow(new IllegalArgumentException("Unknown user type code: X"));

        mockMvc.perform(get("/api/v1/menu").param("userType", "X"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown user type code: X"));
    }

    @Test
    void navigate_validRequest_returns200() throws Exception {
        NavigationResponse navResponse = new NavigationResponse(
                "session-1", "COMEN01C", "COACTVWC", "COACTVWC", "Account View", 0);

        when(menuService.navigate(any())).thenReturn(navResponse);

        String requestBody = """
                {
                    "sessionId": "session-1",
                    "optionNumber": 1,
                    "userType": "U"
                }
                """;

        mockMvc.perform(post("/api/v1/menu/navigate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toProgram").value("COACTVWC"))
                .andExpect(jsonPath("$.fromProgram").value("COMEN01C"));
    }

    @Test
    void navigate_invalidOption_returns400() throws Exception {
        when(menuService.navigate(any()))
                .thenThrow(new InvalidMenuOptionException("Please enter a valid option number..."));

        String requestBody = """
                {
                    "sessionId": "session-1",
                    "optionNumber": 99,
                    "userType": "U"
                }
                """;

        mockMvc.perform(post("/api/v1/menu/navigate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter a valid option number..."));
    }

    @Test
    void navigate_unauthorized_returns403() throws Exception {
        when(menuService.navigate(any()))
                .thenThrow(new UnauthorizedMenuAccessException("No access - Admin Only option..."));

        String requestBody = """
                {
                    "sessionId": "session-1",
                    "optionNumber": 1,
                    "userType": "U"
                }
                """;

        mockMvc.perform(post("/api/v1/menu/navigate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No access - Admin Only option..."));
    }

    @Test
    void getNavigationContext_existing_returns200() throws Exception {
        NavigationContextResponse ctx = new NavigationContextResponse(
                "session-1", "USER0001", "U", "CM00", "COMEN01C",
                null, "COACTVWC", 0, LocalDateTime.now());

        when(menuService.getNavigationContext("session-1")).thenReturn(ctx);

        mockMvc.perform(get("/api/v1/menu/context/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.fromProgram").value("COMEN01C"));
    }

    @Test
    void getNavigationContext_notFound_returns404() throws Exception {
        when(menuService.getNavigationContext("missing"))
                .thenThrow(new NavigationContextNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/menu/context/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void navigateBack_existing_returns200() throws Exception {
        NavigationResponse response = new NavigationResponse(
                "session-1", "COMEN01C", "COSGN00C", "COSGN00C", "Sign On Screen", 0);

        when(menuService.navigateBack("session-1")).thenReturn(response);

        mockMvc.perform(post("/api/v1/menu/back/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toProgram").value("COSGN00C"));
    }
}
