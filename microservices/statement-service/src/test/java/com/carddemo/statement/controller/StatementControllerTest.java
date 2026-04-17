package com.carddemo.statement.controller;

import com.carddemo.statement.dto.StatementResponse;
import com.carddemo.statement.service.StatementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementController.class)
class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatementService statementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthCheck_shouldReturnUpStatus() throws Exception {
        mockMvc.perform(get("/statements/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("statement-service")));
    }

    @Test
    void generateStatement_shouldReturnStatement() throws Exception {
        StatementResponse mockResponse = new StatementResponse(
                "00000000001",
                "2024-01-01 to 2024-01-31",
                "TEXT STATEMENT",
                "<html>HTML STATEMENT</html>"
        );

        when(statementService.generateStatement(any())).thenReturn(mockResponse);

        String requestBody = """
                {
                    "accountId": "00000000001",
                    "startDate": "2024-01-01",
                    "endDate": "2024-01-31"
                }
                """;

        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is("00000000001")))
                .andExpect(jsonPath("$.statementPeriod", is("2024-01-01 to 2024-01-31")))
                .andExpect(jsonPath("$.textStatement", is("TEXT STATEMENT")))
                .andExpect(jsonPath("$.htmlStatement", is("<html>HTML STATEMENT</html>")));
    }

    @Test
    void generateStatement_withMissingAccountId_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "startDate": "2024-01-01",
                    "endDate": "2024-01-31"
                }
                """;

        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateStatement_withMissingStartDate_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "accountId": "00000000001",
                    "endDate": "2024-01-31"
                }
                """;

        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateStatement_withMissingEndDate_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "accountId": "00000000001",
                    "startDate": "2024-01-01"
                }
                """;

        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateStatement_withEmptyBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateStatement_whenServiceThrowsException_shouldReturn500() throws Exception {
        when(statementService.generateStatement(any()))
                .thenThrow(new RuntimeException("Account service unavailable"));

        String requestBody = """
                {
                    "accountId": "00000000001",
                    "startDate": "2024-01-01",
                    "endDate": "2024-01-31"
                }
                """;

        mockMvc.perform(post("/statements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError());
    }
}
