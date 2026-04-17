package com.carddemo.statement.service;

import com.carddemo.statement.dto.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Client for the Transaction Service.
 * Replaces CBSTM03B's TRNXFILE sequential read operation.
 *
 * <p>In the COBOL program, CBSTM03A read all transactions from the TRNXFILE
 * into a 2D array (WS-TRNX-TABLE with 51 cards x 10 transactions each),
 * then matched them by card number during statement generation.
 * This is now a REST GET call with query parameters for filtering.</p>
 */
@Service
public class TransactionClient {

    private static final Logger log = LoggerFactory.getLogger(TransactionClient.class);

    private final WebClient webClient;

    public TransactionClient(@Qualifier("transactionServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<TransactionData> getTransactions(String cardNumber, String startDate, String endDate) {
        log.info("Fetching transactions for cardNum={}, period={} to {}", cardNumber, startDate, endDate);
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/transactions")
                            .queryParam("cardNum", cardNumber)
                            .queryParam("startDate", startDate)
                            .queryParam("endDate", endDate)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionData>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch transactions for card {}: {} {}", cardNumber, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Transaction service error for card=" + cardNumber + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Transaction service unavailable for card={}: {}", cardNumber, e.getMessage());
            throw new RuntimeException("Transaction service unavailable: " + e.getMessage(), e);
        }
    }
}
