package com.carddemo.billing.service;

import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import com.carddemo.billing.dto.ReportRequest;
import com.carddemo.billing.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Main billing service coordinating bill payments and report submissions.
 *
 * Modernized from two COBOL programs:
 * - COBIL00C.cbl (Bill Payment): delegates to BillPaymentSaga
 * - CORPT00C.cbl (Report Submission): delegates to ReportSubmissionService
 */
@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final BillPaymentSaga billPaymentSaga;
    private final ReportSubmissionService reportSubmissionService;

    public BillingService(BillPaymentSaga billPaymentSaga,
                          ReportSubmissionService reportSubmissionService) {
        this.billPaymentSaga = billPaymentSaga;
        this.reportSubmissionService = reportSubmissionService;
    }

    /**
     * Process a bill payment for the given account.
     *
     * @param accountId the account ID
     * @param request   the bill payment request
     * @return the bill payment response
     */
    public Mono<BillPaymentResponse> processPayment(String accountId, BillPaymentRequest request) {
        log.info("Processing bill payment for account {}: amount={}", accountId, request.amount());
        return billPaymentSaga.execute(accountId, request);
    }

    /**
     * Submit a report generation request.
     *
     * @param request the report request
     * @return the report submission response
     */
    public ReportResponse submitReport(ReportRequest request) {
        log.info("Submitting report for account {}", request.accountId());
        return reportSubmissionService.submitReport(request);
    }
}
