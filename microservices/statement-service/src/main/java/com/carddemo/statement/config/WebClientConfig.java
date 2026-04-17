package com.carddemo.statement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient instances used to communicate with upstream services.
 * Replaces CBSTM03B's file I/O operations with HTTP client calls.
 */
@Configuration
public class WebClientConfig {

    @Value("${services.account.base-url}")
    private String accountServiceBaseUrl;

    @Value("${services.card.base-url}")
    private String cardServiceBaseUrl;

    @Value("${services.transaction.base-url}")
    private String transactionServiceBaseUrl;

    @Bean
    public WebClient accountServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(accountServiceBaseUrl).build();
    }

    @Bean
    public WebClient cardServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(cardServiceBaseUrl).build();
    }

    @Bean
    public WebClient transactionServiceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(transactionServiceBaseUrl).build();
    }
}
