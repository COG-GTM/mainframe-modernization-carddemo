package com.carddemo.account.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for consuming transaction events.
 *
 * In the original mainframe, batch programs like CBTRN02C would post
 * transactions and update account balances in the same batch job.
 * In the modernized event-driven architecture, the Transaction Service
 * publishes events and the Account Service consumes them.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "carddemo.events";
    public static final String QUEUE_NAME = "account-service.transaction.posted";
    public static final String ROUTING_KEY = "transaction.posted";

    @Bean
    public TopicExchange carddemoExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue transactionPostedQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding transactionPostedBinding(Queue transactionPostedQueue,
                                            TopicExchange carddemoExchange) {
        return BindingBuilder.bind(transactionPostedQueue)
                .to(carddemoExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
