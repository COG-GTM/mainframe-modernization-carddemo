package com.carddemo.transaction.service;

import com.carddemo.transaction.config.RabbitMQConfig;
import com.carddemo.transaction.dto.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes transaction events to RabbitMQ.
 * Replaces the direct COBOL account balance update in CBTRN02C (lines 547-554):
 *   ADD DALYTRAN-AMT TO ACCT-CURR-BAL
 *   REWRITE FD-ACCTFILE-REC
 * Account Service will consume these events to update balances asynchronously.
 */
@Service
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public TransactionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTransactionPosted(TransactionEvent event) {
        log.info("Publishing transaction.posted event for transactionId={}, cardNum={}, amount={}",
                event.getTransactionId(), event.getCardNum(), event.getAmount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event);
    }
}
