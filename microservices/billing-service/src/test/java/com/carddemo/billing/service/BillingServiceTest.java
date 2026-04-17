package com.carddemo.billing.service;

import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import com.carddemo.billing.dto.ReportRequest;
import com.carddemo.billing.dto.ReportResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for BillingService.
 *
 * Validates the coordination between the BillPaymentSaga and
 * ReportSubmissionService, which together modernize COBIL00C.cbl
 * and CORPT00C.cbl respectively.
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillPaymentSaga billPaymentSaga;

    @Mock
    private ReportSubmissionService reportSubmissionService;

    @InjectMocks
    private BillingService billingService;

    @Test
    @DisplayName("processPayment delegates to BillPaymentSaga")
    void testProcessPayment() {
        // Arrange
        String accountId = "00000000001";
        BillPaymentRequest request = new BillPaymentRequest(new BigDecimal("100.00"), "4111111111111111");

        BillPaymentResponse expectedResponse = new BillPaymentResponse(
                "0000000000000001",
                accountId,
                new BigDecimal("100.00"),
                new BigDecimal("400.00"),
                "SUCCESS",
                Instant.now()
        );

        when(billPaymentSaga.execute(eq(accountId), any(BillPaymentRequest.class)))
                .thenReturn(Mono.just(expectedResponse));

        // Act & Assert
        StepVerifier.create(billingService.processPayment(accountId, request))
                .assertNext(response -> {
                    assertThat(response.transactionId()).isEqualTo("0000000000000001");
                    assertThat(response.accountId()).isEqualTo(accountId);
                    assertThat(response.status()).isEqualTo("SUCCESS");
                })
                .verifyComplete();

        verify(billPaymentSaga).execute(eq(accountId), any(BillPaymentRequest.class));
    }

    @Test
    @DisplayName("submitReport delegates to ReportSubmissionService")
    void testSubmitReport() {
        // Arrange
        ReportRequest request = new ReportRequest("00000000001", "2024-01-01", "2024-12-31");

        ReportResponse expectedResponse = new ReportResponse(
                "SUBMITTED",
                "00000000001",
                "2024-01-01",
                "2024-12-31",
                Instant.now()
        );

        when(reportSubmissionService.submitReport(any(ReportRequest.class)))
                .thenReturn(expectedResponse);

        // Act
        ReportResponse response = billingService.submitReport(request);

        // Assert
        assertThat(response.status()).isEqualTo("SUBMITTED");
        assertThat(response.accountId()).isEqualTo("00000000001");
        assertThat(response.startDate()).isEqualTo("2024-01-01");
        assertThat(response.endDate()).isEqualTo("2024-12-31");

        verify(reportSubmissionService).submitReport(any(ReportRequest.class));
    }
}
