package com.carddemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * SQS configuration as an alternative to Kafka.
 *
 * Activated via the "sqs" Spring profile. Replaces Kafka topics with
 * SQS queues using the same logical names:
 *   - transaction-pending (replaces DALYTRAN flat file input)
 *   - transaction-posted  (replaces TRANSACT VSAM write → CREASTMT read coupling)
 *   - transaction-rejected (replaces DALYREJS GDG output)
 *   - statement-generated  (replaces STATEMNT.PS / STATEMNT.HTML output)
 *
 * Uses Spring Cloud AWS SQS for seamless queue integration.
 */
@Configuration
@Profile("sqs")
public class SqsConfig {

    // SQS configuration is driven by application.yml properties:
    //   spring.cloud.aws.sqs.region
    //   spring.cloud.aws.sqs.endpoint (for LocalStack testing)
    //
    // Queue names match Kafka topic names for consistency.
    // SQS listeners use @SqsListener annotation instead of @KafkaListener.
}
