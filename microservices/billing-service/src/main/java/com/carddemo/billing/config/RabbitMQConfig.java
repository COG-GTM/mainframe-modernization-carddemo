package com.carddemo.billing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for event publishing.
 *
 * Modernized from CORPT00C.cbl's TDQ (Transient Data Queue) pattern.
 * The original COBOL program wrote JCL records to the 'JOBS' TDQ
 * to submit batch report jobs. This is replaced with RabbitMQ
 * message publishing to trigger the Statement Service asynchronously.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "carddemo.events";
    public static final String REPORT_ROUTING_KEY = "report.requested";
    public static final String REPORT_QUEUE_NAME = "carddemo.report.requested";

    @Bean
    public TopicExchange carddemoExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue reportQueue() {
        return new Queue(REPORT_QUEUE_NAME, true);
    }

    @Bean
    public Binding reportBinding(Queue reportQueue, TopicExchange carddemoExchange) {
        return BindingBuilder.bind(reportQueue)
                .to(carddemoExchange)
                .with(REPORT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
