package com.carddemo.account.listener;

import com.carddemo.account.config.RabbitMQConfig;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
        } catch (Exception e) {
            log.error("Failed to process transaction event for account {}: {}",
                    accountId, e.getMessage(), e);
        }
    }
}
