package com.carddemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.event.TransactionPostedEvent;
import com.carddemo.event.TransactionRejectedEvent;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionValidationService.ValidationResult;

/**
 * Core transaction posting orchestrator.
 *
 * Replaces: CBTRN02C procedure division main logic (lines 193-234).
 * Orchestrates the validate → post → emit event pipeline that replaces
 * the POSTTRAN.jcl batch job.
 *
 * Control flow (mirroring CBTRN02C lines 202-219):
 *   1. Validate transaction (PERFORM 1500-VALIDATE-TRAN)
 *   2. If valid:  post transaction (PERFORM 2000-POST-TRANSACTION)
 *   3. If invalid: emit rejection event (PERFORM 2500-WRITE-REJECT-REC)
 */
@Service
public class TransactionPostingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingService.class);

    static final String TOPIC_POSTED = "transaction.posted";
    static final String TOPIC_REJECTED = "transaction.rejected";

    private final TransactionValidationService validationService;
    private final AccountUpdateService accountUpdateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionPostingService(TransactionValidationService validationService,
                                      AccountUpdateService accountUpdateService,
                                      KafkaTemplate<String, Object> kafkaTemplate) {
        this.validationService = validationService;
        this.accountUpdateService = accountUpdateService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Process a single pending transaction: validate, post, and emit events.
     *
     * Replaces the main loop body of CBTRN02C (lines 202-219):
     *   PERFORM 1500-VALIDATE-TRAN
     *   IF WS-VALIDATION-FAIL-REASON = 0
     *     PERFORM 2000-POST-TRANSACTION
     *   ELSE
     *     ADD 1 TO WS-REJECT-COUNT
     *     PERFORM 2500-WRITE-REJECT-REC
     *   END-IF
     *
     * The @Transactional annotation ensures that category balance update,
     * account balance update, and transaction write all succeed or fail
     * atomically — preserving the same guarantees that COBOL achieved
     * through its single-program execution model.
     */
    @Transactional
    public void postTransaction(TransactionPendingEvent event) {
        log.info("Processing transaction: id={}, card={}, amount={}",
                event.transactionId(), event.cardNumber(), event.amount());

        // Step 1: Validate (replaces PERFORM 1500-VALIDATE-TRAN)
        ValidationResult result = validationService.validate(event);

        if (!result.isValid()) {
            // Replaces: 2500-WRITE-REJECT-REC — WRITE DALYREJS-FILE
            log.warn("Transaction {} rejected: code={}, reason={}",
                    event.transactionId(), result.getFailureReasonCode(),
                    result.getFailureDescription());

            TransactionRejectedEvent rejectedEvent = new TransactionRejectedEvent(
                    event, result.getFailureReasonCode(), result.getFailureDescription());
            kafkaTemplate.send(TOPIC_REJECTED, event.transactionId(), rejectedEvent);
            return;
        }

        // Step 2: Post transaction (replaces PERFORM 2000-POST-TRANSACTION)
        CardXref xref = result.getXref();
        Account account = result.getAccount();

        // 2a: Update transaction category balance
        //     Replaces: PERFORM 2700-UPDATE-TCATBAL
        accountUpdateService.updateTransactionCategoryBalance(
                xref.getAcctId(), event.typeCd(), event.catCd(), event.amount());

        // 2b: Update account balance and cycle accumulators
        //     Replaces: PERFORM 2800-UPDATE-ACCOUNT-REC
        accountUpdateService.updateAccountBalance(account, event.amount());

        // 2c: Write transaction to master file
        //     Replaces: PERFORM 2900-WRITE-TRANSACTION-FILE
        Transaction posted = accountUpdateService.writeTransaction(event);

        // Step 3: Publish posted event (replaces implicit TRANSACT VSAM → CREASTMT coupling)
        TransactionPostedEvent postedEvent = new TransactionPostedEvent(
                posted.getTranId(),
                posted.getTypeCd(),
                posted.getCatCd(),
                posted.getSource(),
                posted.getDescription(),
                posted.getAmount(),
                posted.getMerchantId() != null ? posted.getMerchantId() : 0L,
                posted.getMerchantName(),
                posted.getMerchantCity(),
                posted.getMerchantZip(),
                posted.getCardNum(),
                posted.getOriginTimestamp(),
                posted.getProcessTimestamp(),
                xref.getAcctId(),
                xref.getCustId()
        );
        kafkaTemplate.send(TOPIC_POSTED, posted.getCardNum(), postedEvent);

        log.info("Transaction {} posted successfully for account {}",
                event.transactionId(), xref.getAcctId());
    }
}
