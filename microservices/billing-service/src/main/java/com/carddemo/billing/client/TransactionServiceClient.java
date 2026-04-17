package com.carddemo.billing.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client for Transaction Service.
 *
 * Modernized from COBIL00C.cbl WRITE-TRANSACT-FILE paragraph.
 * The original COBOL program writes a transaction record to the TRANSACT
 * VSAM file with the following key fields (from CVTRA05Y.cpy):
 *   TRAN-ID           PIC X(16)   - auto-incremented
 *   TRAN-TYPE-CD      PIC X(02)   - '02' for bill payment
 *   TRAN-CAT-CD       PIC 9(04)   - 2
 *   TRAN-SOURCE       PIC X(10)   - 'POS TERM'
 *   TRAN-DESC         PIC X(100)  - 'BILL PAYMENT - ONLINE'
 *   TRAN-AMT          PIC S9(09)V99
 *   TRAN-MERCHANT-ID  PIC 9(09)   - 999999999
 *   TRAN-MERCHANT-NAME PIC X(50)  - 'BILL PAYMENT'
 */
@Component
public class TransactionServiceClient {

    private final WebClient webClient;

    public TransactionServiceClient(@Qualifier("transactionServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Create a bill payment transaction.
     *
     * @param transactionData the transaction payload
     * @return the created transaction data including the generated transaction ID
     */
    public Mono<Map<String, Object>> createTransaction(Map<String, Object> transactionData) {
        return webClient.post()
                .uri("/transactions")
                .bodyValue(transactionData)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Reverse a transaction (compensation for saga failure).
     *
     * @param transactionId the ID of the transaction to reverse
     * @return the reversal confirmation
     */
    public Mono<Void> reverseTransaction(String transactionId) {
        return webClient.delete()
                .uri("/transactions/{id}", transactionId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
