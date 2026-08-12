package com.carddemo.online.billpay;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** COBOL program: COBIL00C — the REST face of the CB00 screen. */
@WebMvcTest(BillPaymentController.class)
class BillPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillPaymentService billPaymentService;

    @Test
    void aConfirmedPaymentIsReported() throws Exception {
        given(billPaymentService.pay(any()))
                .willReturn(BillPaymentResponse.builder()
                        .success(true)
                        .message("Payment successful.  Your Transaction ID is 0000000000000001.")
                        .transactionId("0000000000000001")
                        .paidAmount(new BigDecimal("194.00"))
                        .currentBalance(BigDecimal.ZERO)
                        .currentBalanceDisplay("+0000000000.00")
                        .build());

        mockMvc.perform(post("/api/billpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().accountId("7").confirm("Y").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAmount").value(194.00))
                .andExpect(jsonPath("$.currentBalanceDisplay").value("+0000000000.00"));
    }

    @Test
    void theConfirmPromptIsReportedAsABadRequest() throws Exception {
        given(billPaymentService.pay(any()))
                .willReturn(BillPaymentResponse.builder()
                        .success(false)
                        .message(BillPaymentService.MSG_CONFIRM)
                        .currentBalanceDisplay("+0000000194.00")
                        .build());

        mockMvc.perform(post("/api/billpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().accountId("7").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Confirm to make a bill payment..."));
    }
}
