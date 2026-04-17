package com.carddemo.statement.service;

import com.carddemo.statement.dto.CardXrefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Client for the Card Service.
 * Replaces CBSTM03B's XREFFILE sequential read operation.
 *
 * <p>In the COBOL program, CBSTM03A iterated through the XREF file
 * sequentially (M03B-READ) to find all cards associated with an account.
 * This is now a REST GET call to /cards/xref/{accountId}.</p>
 */
@Service
public class CardClient {

    private static final Logger log = LoggerFactory.getLogger(CardClient.class);

    private final WebClient webClient;

    public CardClient(@Qualifier("cardServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<CardXrefData> getCardsForAccount(String accountId) {
        log.info("Fetching card cross-references for accountId={}", accountId);
        try {
            return webClient.get()
                    .uri("/cards/xref/{accountId}", accountId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CardXrefData>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch cards for account {}: {} {}", accountId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Card service error for accountId=" + accountId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Card service unavailable for accountId={}: {}", accountId, e.getMessage());
            throw new RuntimeException("Card service unavailable: " + e.getMessage(), e);
        }
    }
}
