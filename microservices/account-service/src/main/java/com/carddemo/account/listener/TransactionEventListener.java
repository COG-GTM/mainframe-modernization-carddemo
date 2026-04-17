package com.carddemo.account.listener;

import com.carddemo.account.config.RabbitMQConfig;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Listens for transaction.posted events from RabbitMQ.
 *
 * In the original mainframe, CBTRN02C batch program would:
 *   1. Read transaction records sequentially
 *   2. For each transaction, update the account balance
 *   3. ADD TRAN-AMT TO ACCT-CURR-BAL
 *   4. Update ACCT-CURR-CYC-CREDIT or ACCT-CURR-CYC-DEBIT based on sign
 *
 * In the modernized event-driven architecture, this listener
 * processes individual transaction events as they are posted,
 * replacing the nightly batch cycle with real-time processing.
 */
@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    private final AccountService accountService;

    public TransactionEventListener(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Handle transaction.posted events.
     *
     * Expected message payload:
     *   { "accountId": "00000000001", "amount": 125.50 }
     *
     * Positive amount = credit (payment received)
     * Negative amount = debit (purchase/charge)
     *
     * Exception handling strategy:
     *  - Transient errors (e.g. OptimisticLockingFailure): re-throw so Spring
     *    AMQP nacks and requeues the message for automatic retry.
     *  - Permanent errors (e.g. account not found, bad data): wrap in
     *    AmqpRejectAndDontRequeueException so the message is routed to the
     *    dead-letter queue instead of retrying forever.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleTransactionPosted(Map<String, Object> event) {
        String accountId = (String) event.get("accountId");
        BigDecimal amount = new BigDecimal(event.get("amount").toString());

        log.info("Received transaction.posted event: accountId={}, amount={}",
                accountId, amount);

        try {
            BalanceUpdateRequest request = new BalanceUpdateRequest(amount);
            accountService.updateBalance(accountId, request);
            log.info("Successfully processed transaction event for account {}", accountId);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Transient: concurrent modification -- requeue for retry
            log.warn("Optimistic lock conflict for account {}, requeueing for retry",
                    accountId, e);
            throw e;
        } catch (IllegalArgumentException e) {
            // Permanent: bad data or account not found -- send to DLQ
            log.error("Permanent failure processing event for account {}: {}",
                    accountId, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(
                    "Permanent failure for account " + accountId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            // Unknown: send to DLQ to avoid infinite retry loops
            log.error("Unexpected failure processing event for account {}: {}",
                    accountId, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(
                    "Unexpected failure for account " + accountId + ": " + e.getMessage(), e);
        }
    }
}
