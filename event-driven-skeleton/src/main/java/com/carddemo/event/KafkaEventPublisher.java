package com.carddemo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Kafka-backed {@link EventPublisher} that defers sending until the
 * current database transaction has committed.
 *
 * If no transaction is active (e.g. during tests or non-transactional
 * paths), the event is sent immediately.
 *
 * This prevents two classes of bugs:
 *   1. Race condition: downstream consumers querying the DB before the
 *      producing transaction commits.
 *   2. Phantom events: events published for data that is subsequently
 *      rolled back.
 */
@Component
@Profile("!sqs")
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String key, Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Defer until after the DB transaction commits
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doSend(topic, key, event);
                        }
                    });
        } else {
            // No active transaction — send immediately
            doSend(topic, key, event);
        }
    }

    private void doSend(String topic, String key, Object event) {
        log.debug("Publishing event to Kafka topic={}, key={}", topic, key);
        kafkaTemplate.send(topic, key, event);
    }
}
