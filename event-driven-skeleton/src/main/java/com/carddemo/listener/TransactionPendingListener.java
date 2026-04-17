package com.carddemo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.service.TransactionPostingService;

/**
 * Kafka listener for pending transaction events.
 *
 * Replaces: POSTTRAN.jcl STEP010 which executes CBTRN02C to process
 * the DALYTRAN sequential file.
 *
 * Instead of a batch job reading a flat file, this listener consumes
 * individual transaction events from the "transaction.pending" topic
 * and delegates to TransactionPostingService for real-time processing.
 *
 * POSTTRAN.jcl JCL mapping:
 *   //STEP010  EXEC PGM=CBTRN02C  →  this listener + TransactionPostingService
 *   //DALYTRAN DD ...              →  "transaction.pending" Kafka topic
 *   //DALYREJS DD ...              →  "transaction.rejected" Kafka topic
 *   //TRANSACT DD ...              →  TransactionRepository (JPA)
 *   //XREFFILE DD ...              →  CardXrefRepository (JPA)
 *   //ACCTFILE DD ...              →  AccountRepository (JPA)
 *   //TCATBALF DD ...              →  TranCatBalanceRepository (JPA)
 */
@Component
public class TransactionPendingListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionPendingListener.class);

    private final TransactionPostingService postingService;

    public TransactionPendingListener(TransactionPostingService postingService) {
        this.postingService = postingService;
    }

    /**
     * Consume and process a pending transaction event.
     *
     * Each invocation replaces one iteration of the CBTRN02C main loop
     * (1000-DALYTRAN-GET → 1500-VALIDATE → 2000-POST or 2500-REJECT).
     */
    @KafkaListener(
            topics = "transaction.pending",
            groupId = "${spring.kafka.consumer.group-id:carddemo-posting}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransactionPending(TransactionPendingEvent event) {
        log.info("Received pending transaction: id={}, card={}",
                event.transactionId(), event.cardNumber());
        postingService.postTransaction(event);
    }
}
