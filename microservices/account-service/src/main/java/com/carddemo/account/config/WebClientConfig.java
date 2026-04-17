package com.carddemo.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for cross-service communication.
 *
 * Used to call Card Service for card cross-reference data,
 * replacing the COBOL CICS READ on CARDXREF (CXACAIX) file.
 *
 * Original COBOL (COACTVWC 9200-GETCARDXREF-BYACCT):
 *   EXEC CICS READ DATASET(CXACAIX) RIDFLD(acct-id) INTO(CARD-XREF-RECORD)
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
