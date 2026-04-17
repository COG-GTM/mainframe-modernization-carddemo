package com.carddemo.transaction.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for transaction event publishing.
 * Replaces the direct account balance update from CBTRN02C:
 *   ADD DALYTRAN-AMT TO ACCT-CURR-BAL / REWRITE FD-ACCTFILE-REC
 * with async event publishing to be consumed by Account Service.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "carddemo.events";
    public static final String ROUTING_KEY = "transaction.posted";

    @Bean
    public TopicExchange carddemoExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
