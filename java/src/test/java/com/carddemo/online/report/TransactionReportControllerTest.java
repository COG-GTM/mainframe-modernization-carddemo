package com.carddemo.online.report;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** COBOL program: CORPT00C — the REST face of the CR00 screen. */
@WebMvcTest(TransactionReportController.class)
class TransactionReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionReportService reportService;

    @Test
    void anAcceptedRequestCarriesTheResolvedDateRange() throws Exception {
        given(reportService.submit(any()))
                .willReturn(TransactionReportResponse.builder()
                        .success(true)
                        .reportName("Monthly")
                        .startDate("2024-02-01")
                        .endDate("2024-02-29")
                        .message("Monthly report submitted for printing ...")
                        .build());

        mockMvc.perform(post("/api/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TransactionReportRequest.builder()
                                .monthly("Y")
                                .confirm("Y")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2024-02-01"))
                .andExpect(jsonPath("$.endDate").value("2024-02-29"));
    }

    @Test
    void aValidationFailureIsReportedAsABadRequest() throws Exception {
        given(reportService.submit(any()))
                .willReturn(TransactionReportResponse.builder()
                        .success(false)
                        .message(TransactionReportService.MSG_START_DATE_INVALID)
                        .reportName("Custom")
                        .build());

        mockMvc.perform(post("/api/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TransactionReportRequest.builder().custom("Y").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start Date - Not a valid date..."));
    }
}
