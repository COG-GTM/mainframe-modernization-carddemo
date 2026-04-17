package com.carddemo.billing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for cross-service communication.
 *
 * Configures WebClient instances for calling:
 * - Card Service (card XREF lookup, replacing CICS READ on CXACAIX file)
 * - Transaction Service (transaction creation, replacing CICS WRITE to TRANSACT)
 * - Account Service (balance update, replacing CICS REWRITE to ACCTDAT)
 */
@Configuration
public class WebClientConfig {

    @Value("${services.card-service.url}")
    private String cardServiceUrl;

    @Value("${services.transaction-service.url}")
    private String transactionServiceUrl;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    @Bean
    public WebClient cardServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(cardServiceUrl).build();
    }

    @Bean
    public WebClient transactionServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(transactionServiceUrl).build();
    }

    @Bean
    public WebClient accountServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(accountServiceUrl).build();
    }
}
