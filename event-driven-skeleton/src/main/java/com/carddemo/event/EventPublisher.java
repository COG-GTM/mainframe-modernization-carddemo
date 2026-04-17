package com.carddemo.event;

/**
 * Abstraction over the underlying messaging transport (Kafka, SQS, etc.).
 *
 * Services publish domain events through this interface instead of
 * depending on {@code KafkaTemplate} directly, which:
 *   1. Allows the SQS profile to start without a KafkaTemplate bean.
 *   2. Lets implementations defer publishing until after the DB transaction
 *      commits, preventing race conditions with downstream consumers.
 */
public interface EventPublisher {

    /**
     * Publish an event to the given topic/queue.
     *
     * Implementations MUST ensure the event is only delivered after the
     * current database transaction (if any) has committed successfully.
     *
     * @param topic the logical topic or queue name
     * @param key   the partitioning key (e.g. card number)
     * @param event the event payload
     */
    void publish(String topic, String key, Object event);
}
