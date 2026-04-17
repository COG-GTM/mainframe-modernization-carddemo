package com.carddemo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.carddemo.event.TransactionPostedEvent;
import com.carddemo.service.StatementGenerationService;

/**
 * Kafka listener for posted transaction events.
 *
 * Replaces: CREASTMT.JCL which runs after POSTTRAN.jcl completes.
 * Instead of a batch dependency chain (POSTTRAN → CREASTMT), this listener
 * reacts to each posted transaction event in near real-time.
 *
 * CREASTMT.JCL step mapping:
 *   //DELDEF01  (delete/redefine TRXFL)   → eliminated (no temp file needed)
 *   //STEP010   (SORT by card+tran-id)    → replaced by DB ORDER BY clause
 *   //STEP020   (REPRO to TRXFL VSAM)     → eliminated (no temp file needed)
 *   //STEP030   (delete old output files) → eliminated (statements are events)
 *   //STEP040   EXEC PGM=CBSTM03A        → StatementGenerationService
 *
 * Only active when Kafka is the messaging transport (i.e. not under the "sqs" profile).
 */
@Component
@Profile("!sqs")
public class TransactionPostedListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostedListener.class);

    private final StatementGenerationService statementService;

    public TransactionPostedListener(StatementGenerationService statementService) {
        this.statementService = statementService;
    }

    /**
     * Consume a posted transaction event and trigger statement generation.
     *
     * Each event triggers statement generation for the associated card/account.
     * In a production system, this might be batched or debounced to avoid
     * regenerating statements on every single transaction.
     */
    @KafkaListener(
            topics = "transaction.posted",
            groupId = "${spring.kafka.consumer.group-id:carddemo-statement}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransactionPosted(TransactionPostedEvent event) {
        log.info("Received posted transaction: id={}, card={}, account={}",
                event.transactionId(), event.cardNumber(), event.accountId());

        statementService.generateStatement(
                event.cardNumber(), event.customerId(), event.accountId());
    }
}
