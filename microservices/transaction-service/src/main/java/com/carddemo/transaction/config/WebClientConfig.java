package com.carddemo.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for cross-service communication.
 * Translates the COBOL XREF file lookup (READ XREF-FILE / CARDXREF)
 * into a REST call to Card Service for card validation.
 */
@Configuration
public class WebClientConfig {

    @Value("${carddemo.card-service.url:http://card-service:8080}")
    private String cardServiceUrl;

    @Bean
    public WebClient cardServiceWebClient() {
        return WebClient.builder()
                .baseUrl(cardServiceUrl)
                .build();
    }
}
