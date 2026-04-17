package com.carddemo.billing.service;

import com.carddemo.billing.config.RabbitMQConfig;
import com.carddemo.billing.dto.ReportRequest;
import com.carddemo.billing.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for submitting report generation requests.
 *
 * Modernized from CORPT00C.cbl SUBMIT-JOB-TO-INTRDR paragraph (lines 462-510).
 *
 * The original COBOL program submitted batch report jobs by writing JCL
 * records to the 'JOBS' Transient Data Queue (TDQ) using EXEC CICS WRITEQ TD.
 * The JCL defined a TRNRPT00 job with TRANREPT procedure that would:
 *   1. Sort transactions by card number and processing date
 *   2. Generate transaction reports filtered by date range
 *
 * In the microservices architecture, this is replaced with publishing
 * a message to RabbitMQ that triggers the Statement Service asynchronously.
 *
 * The original COBOL supported three report types:
 *   - Monthly: current month start to end
 *   - Yearly: Jan 1 to Dec 31 of current year
 *   - Custom: user-specified date range with validation
 *
 * In the microservice, date ranges are provided directly by the caller.
 */
@Service
public class ReportSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(ReportSubmissionService.class);

    private final RabbitTemplate rabbitTemplate;

    public ReportSubmissionService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Submit a report generation request.
     *
     * @param request the report request with account ID and date range
     * @return the report submission response
     */
    public ReportResponse submitReport(ReportRequest request) {
        Instant requestedAt = Instant.now();

        Map<String, Object> message = new HashMap<>();
        message.put("accountId", request.accountId());
        message.put("startDate", request.startDate());
        message.put("endDate", request.endDate());
        message.put("requestedAt", requestedAt.toString());

        log.info("Submitting report request for account {}: {} to {}",
                request.accountId(), request.startDate(), request.endDate());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                message
        );

        log.info("Report request published to RabbitMQ for account {}", request.accountId());

        return new ReportResponse(
                "SUBMITTED",
                request.accountId(),
                request.startDate(),
                request.endDate(),
                requestedAt
        );
    }
}
