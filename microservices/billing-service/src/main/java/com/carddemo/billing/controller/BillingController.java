package com.carddemo.billing.controller;

import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import com.carddemo.billing.dto.ReportRequest;
import com.carddemo.billing.dto.ReportResponse;
import com.carddemo.billing.service.BillingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST controller for billing operations.
 *
 * Exposes endpoints modernized from two COBOL programs:
 *
 * 1. POST /billing/pay/{accountId} - Bill Payment (from COBIL00C.cbl)
 *    Original CICS transaction: CB00
 *    Processes bill payment using saga pattern across multiple services.
 *
 * 2. POST /billing/reports/submit - Report Submission (from CORPT00C.cbl)
 *    Original CICS transaction: CR00
 *    Submits async report generation request via RabbitMQ.
 *
 * 3. GET /billing/health - Health check endpoint
 */
@RestController
@RequestMapping("/billing")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Process a bill payment for the specified account.
     *
     * Modernized from COBIL00C.cbl PROCESS-ENTER-KEY (lines 154-244).
     * The original flow: validate account -> read XREF -> create transaction ->
     * update balance. Now implemented as a distributed saga.
     *
     * @param accountId the account ID (maps to ACTIDINI screen field / ACCT-ID)
     * @param request   the payment request with amount and card number
     * @return the payment result
     */
    @PostMapping("/pay/{accountId}")
    public Mono<ResponseEntity<BillPaymentResponse>> processPayment(
            @PathVariable String accountId,
            @Valid @RequestBody BillPaymentRequest request) {
        log.info("Received bill payment request for account {}: amount={}", accountId, request.amount());
        return billingService.processPayment(accountId, request)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Bill payment failed for account {}: {}", accountId, ex.getMessage());
                    BillPaymentResponse errorResponse = new BillPaymentResponse(
                            null, accountId, request.amount(), null, "FAILED: " + ex.getMessage(), null
                    );
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                });
    }

    /**
     * Submit a report generation request.
     *
     * Modernized from CORPT00C.cbl SUBMIT-JOB-TO-INTRDR (lines 462-510).
     * The original COBOL program wrote JCL to the JOBS TDQ to submit
     * a batch transaction report job. Now publishes to RabbitMQ.
     *
     * @param request the report request with account ID and date range
     * @return the submission confirmation
     */
    @PostMapping("/reports/submit")
    public ResponseEntity<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        log.info("Received report submission request for account {}", request.accountId());
        ReportResponse response = billingService.submitReport(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "billing-service"
        ));
    }
}
