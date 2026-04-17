package com.carddemo.billing.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client for Card Service cross-reference lookups.
 *
 * Modernized from COBIL00C.cbl READ-CXACAIX-FILE paragraph (lines 408-435).
 * The original COBOL program reads the CXACAIX alternate index file
 * to look up the card number associated with an account ID.
 *
 * VSAM XREF record layout (CVACT03Y.cpy):
 *   XREF-CARD-NUM  PIC X(16)
 *   XREF-CUST-ID   PIC 9(09)
 *   XREF-ACCT-ID   PIC 9(11)
 */
@Component
public class CardServiceClient {

    private final WebClient webClient;

    public CardServiceClient(@Qualifier("cardServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Look up the card cross-reference for a given account ID.
     *
     * @param accountId the account ID (maps to XREF-ACCT-ID)
     * @return a map containing cardNum, custId, acctId
     */
    public Mono<Map<String, Object>> getCardXref(String accountId) {
        return webClient.get()
                .uri("/cards/xref/{accountId}", accountId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(WebClientResponseException.NotFound.class, ex ->
                        Mono.error(new RuntimeException("Card XREF not found for account: " + accountId)));
    }
}
