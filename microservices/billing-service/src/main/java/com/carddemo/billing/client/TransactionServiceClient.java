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
     * Create a bill payment transaction without publishing a RabbitMQ event.
     *
     * The skipEvent=true parameter tells the Transaction Service NOT to publish
     * a transaction.posted event, because the BillPaymentSaga handles the
     * account balance update directly (step 3 of the saga). Without this flag,
     * the event listener in Account Service would also update the balance,
     * resulting in a double-update that cancels out the bill payment.
     *
     * @param transactionData the transaction payload
     * @return the created transaction data including the generated transaction ID
     */
    public Mono<Map<String, Object>> createTransaction(Map<String, Object> transactionData) {
        return webClient.post()
                .uri("/transactions?skipEvent=true")
                .bodyValue(transactionData)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Reverse a transaction by creating a compensating transaction.
     *
     * Uses POST /transactions/{id}/reverse which creates a new transaction
     * with a negated amount and "REVERSAL" description, maintaining a full
     * audit trail rather than deleting the original record.
     *
     * @param transactionId the ID of the transaction to reverse
     * @return completes when the reversal is created
     */
    public Mono<Void> reverseTransaction(String transactionId) {
        return webClient.post()
                .uri("/transactions/{id}/reverse", transactionId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
