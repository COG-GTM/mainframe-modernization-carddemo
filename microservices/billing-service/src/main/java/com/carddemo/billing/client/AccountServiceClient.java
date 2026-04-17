package com.carddemo.billing.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Client for Account Service.
 *
 * Modernized from COBIL00C.cbl paragraphs:
 * - READ-ACCTDAT-FILE (lines 343-372): reads account record from ACCTDAT VSAM
 * - UPDATE-ACCTDAT-FILE (lines 377-403): rewrites account record after balance update
 *
 * The original COBOL logic (line 234):
 *   COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
 *   PERFORM UPDATE-ACCTDAT-FILE
 *
 * Account record layout (CVACT01Y.cpy):
 *   ACCT-ID             PIC 9(11)
 *   ACCT-ACTIVE-STATUS  PIC X(01)
 *   ACCT-CURR-BAL       PIC S9(10)V99
 *   ACCT-CREDIT-LIMIT   PIC S9(10)V99
 */
@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@Qualifier("accountServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Update the account balance after a bill payment.
     * Corresponds to: COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
     *
     * @param accountId the account ID (ACCT-ID)
     * @param newBalance the updated balance
     * @return the updated account data
     */
    public Mono<Map<String, Object>> updateBalance(String accountId, BigDecimal newBalance) {
        Map<String, Object> body = Map.of("balance", newBalance);
        return webClient.put()
                .uri("/accounts/{id}/balance", accountId)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Get the current account details.
     *
     * @param accountId the account ID
     * @return the account data
     */
    public Mono<Map<String, Object>> getAccount(String accountId) {
        return webClient.get()
                .uri("/accounts/{id}", accountId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
