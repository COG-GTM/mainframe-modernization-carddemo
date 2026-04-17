package com.carddemo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.operations.SqsTemplate;

/**
 * SQS-backed {@link EventPublisher} for the "sqs" profile.
 *
 * Like {@link KafkaEventPublisher}, defers sending until after
 * the database transaction commits.
 *
 * Queue names use the same logical names as Kafka topics
 * (e.g. "transaction.posted") which SQS maps to queue URLs
 * via Spring Cloud AWS auto-configuration.
 */
@Component
@Profile("sqs")
public class SqsEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsEventPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public SqsEventPublisher(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, String key, Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doSend(topic, key, event);
                        }
                    });
        } else {
            doSend(topic, key, event);
        }
    }

    private void doSend(String topic, String key, Object event) {
        log.debug("Publishing event to SQS queue={}, key={}", topic, key);
        try {
            String payload = objectMapper.writeValueAsString(event);
            // Pass key as MessageGroupId so FIFO queues preserve per-card ordering,
            // matching the Kafka implementation's partition-key semantics.
            sqsTemplate.send(to -> to.queue(topic)
                    .payload(payload)
                    .messageGroupId(key));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event for SQS", e);
        }
    }
}
