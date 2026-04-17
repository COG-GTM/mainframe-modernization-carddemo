package com.carddemo.statement.service;

import com.carddemo.statement.dto.AccountData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Client for the Account Service.
 * Replaces CBSTM03B's ACCTFILE read-by-key (M03B-READ-K) operation and
 * CBSTM03A's CUSTFILE read for customer details.
 *
 * <p>In the COBOL program, CBSTM03A called CBSTM03B with:
 *   MOVE 'ACCTFILE' TO WS-M03B-DD
 *   SET M03B-READ-K TO TRUE
 *   MOVE XREF-ACCT-ID TO WS-M03B-KEY
 * This is now a REST GET call to /accounts/{id}.</p>
 */
@Service
public class AccountClient {

    private static final Logger log = LoggerFactory.getLogger(AccountClient.class);

    private final WebClient webClient;

    public AccountClient(@Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public AccountData getAccount(String accountId) {
        log.info("Fetching account data for accountId={}", accountId);
        try {
            return webClient.get()
                    .uri("/accounts/{id}", accountId)
                    .retrieve()
                    .bodyToMono(AccountData.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch account {}: {} {}", accountId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Account service error for accountId=" + accountId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Account service unavailable for accountId={}: {}", accountId, e.getMessage());
            throw new RuntimeException("Account service unavailable: " + e.getMessage(), e);
        }
    }
}
