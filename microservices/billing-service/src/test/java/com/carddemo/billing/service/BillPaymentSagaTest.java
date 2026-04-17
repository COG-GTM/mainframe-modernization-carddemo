package com.carddemo.billing.service;

import com.carddemo.billing.client.AccountServiceClient;
import com.carddemo.billing.client.CardServiceClient;
import com.carddemo.billing.client.TransactionServiceClient;
import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for BillPaymentSaga.
 *
 * Validates the saga pattern that modernizes COBIL00C.cbl's atomic
 * CICS unit of work (WRITE TRANSACT + REWRITE ACCTDAT) into a
 * distributed saga with compensating transactions.
 */
@ExtendWith(MockitoExtension.class)
class BillPaymentSagaTest {

    @Mock
    private CardServiceClient cardServiceClient;

    @Mock
    private TransactionServiceClient transactionServiceClient;

    @Mock
    private AccountServiceClient accountServiceClient;

    private BillPaymentSaga saga;

    private static final String ACCOUNT_ID = "00000000001";
    private static final String CARD_NUM = "4111111111111111";
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("150.00");
    private static final BigDecimal CURRENT_BALANCE = new BigDecimal("500.00");
    private static final String TRANSACTION_ID = "0000000000000001";

    @BeforeEach
    void setUp() {
        saga = new BillPaymentSaga(cardServiceClient, transactionServiceClient, accountServiceClient);
    }

    @Test
    @DisplayName("Bill payment saga succeeds - all steps complete")
    void testSagaSuccess() {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest(PAYMENT_AMOUNT, CARD_NUM);

        // Step 1: Card XREF lookup succeeds
        Map<String, Object> xrefResponse = Map.of(
                "cardNum", CARD_NUM,
                "custId", "000000001",
                "acctId", ACCOUNT_ID
        );
        when(cardServiceClient.getCardXref(ACCOUNT_ID)).thenReturn(Mono.just(xrefResponse));

        // Step 2: Transaction creation succeeds
        Map<String, Object> txnResponse = Map.of(
                "transactionId", TRANSACTION_ID,
                "currentBalance", CURRENT_BALANCE.toString()
        );
        when(transactionServiceClient.createTransaction(any())).thenReturn(Mono.just(txnResponse));

        // Step 3: Account balance update succeeds
        BigDecimal expectedNewBalance = CURRENT_BALANCE.subtract(PAYMENT_AMOUNT);
        Map<String, Object> accountResponse = Map.of(
                "balance", expectedNewBalance.toString()
        );
        when(accountServiceClient.updateBalance(eq(ACCOUNT_ID), any(BigDecimal.class)))
                .thenReturn(Mono.just(accountResponse));

        // Act & Assert
        StepVerifier.create(saga.execute(ACCOUNT_ID, request))
                .assertNext(response -> {
                    assertThat(response.transactionId()).isEqualTo(TRANSACTION_ID);
                    assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
                    assertThat(response.amount()).isEqualByComparingTo(PAYMENT_AMOUNT);
                    assertThat(response.newBalance()).isEqualByComparingTo(expectedNewBalance);
                    assertThat(response.status()).isEqualTo("SUCCESS");
                    assertThat(response.processedAt()).isNotNull();
                })
                .verifyComplete();

        // Verify all saga steps were called
        verify(cardServiceClient).getCardXref(ACCOUNT_ID);
        verify(transactionServiceClient).createTransaction(any());
        verify(accountServiceClient).updateBalance(eq(ACCOUNT_ID), any(BigDecimal.class));
        // No compensation should have been triggered
        verify(transactionServiceClient, never()).reverseTransaction(anyString());
    }

    @Test
    @DisplayName("Bill payment saga compensates on balance update failure")
    void testSagaCompensation() {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest(PAYMENT_AMOUNT, CARD_NUM);

        // Step 1: Card XREF lookup succeeds
        Map<String, Object> xrefResponse = Map.of(
                "cardNum", CARD_NUM,
                "custId", "000000001",
                "acctId", ACCOUNT_ID
        );
        when(cardServiceClient.getCardXref(ACCOUNT_ID)).thenReturn(Mono.just(xrefResponse));

        // Step 2: Transaction creation succeeds
        Map<String, Object> txnResponse = Map.of(
                "transactionId", TRANSACTION_ID,
                "currentBalance", CURRENT_BALANCE.toString()
        );
        when(transactionServiceClient.createTransaction(any())).thenReturn(Mono.just(txnResponse));

        // Step 3: Account balance update FAILS
        when(accountServiceClient.updateBalance(eq(ACCOUNT_ID), any(BigDecimal.class)))
                .thenReturn(Mono.error(new RuntimeException("Account service unavailable")));

        // Compensation: Transaction reversal succeeds
        when(transactionServiceClient.reverseTransaction(TRANSACTION_ID))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(saga.execute(ACCOUNT_ID, request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).contains("unable to update account balance");
                    assertThat(error.getMessage()).contains("Transaction " + TRANSACTION_ID + " has been reversed");
                })
                .verify();

        // Verify compensation was triggered
        verify(transactionServiceClient).reverseTransaction(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Bill payment saga fails on card XREF not found")
    void testSagaCardXrefNotFound() {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest(PAYMENT_AMOUNT, CARD_NUM);

        // Step 1: Card XREF lookup fails
        when(cardServiceClient.getCardXref(ACCOUNT_ID))
                .thenReturn(Mono.error(new RuntimeException("Card XREF not found for account: " + ACCOUNT_ID)));

        // Act & Assert
        StepVerifier.create(saga.execute(ACCOUNT_ID, request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).contains("Card XREF not found");
                })
                .verify();

        // Verify no further saga steps were attempted
        verify(transactionServiceClient, never()).createTransaction(any());
        verify(accountServiceClient, never()).updateBalance(anyString(), any());
        verify(transactionServiceClient, never()).reverseTransaction(anyString());
    }

    @Test
    @DisplayName("Bill payment saga fails on transaction creation failure")
    void testSagaTransactionCreationFailure() {
        // Arrange
        BillPaymentRequest request = new BillPaymentRequest(PAYMENT_AMOUNT, CARD_NUM);

        // Step 1: Card XREF lookup succeeds
        Map<String, Object> xrefResponse = Map.of(
                "cardNum", CARD_NUM,
                "custId", "000000001",
                "acctId", ACCOUNT_ID
        );
        when(cardServiceClient.getCardXref(ACCOUNT_ID)).thenReturn(Mono.just(xrefResponse));

        // Step 2: Transaction creation fails
        when(transactionServiceClient.createTransaction(any()))
                .thenReturn(Mono.error(new RuntimeException("Transaction service unavailable")));

        // Act & Assert
        StepVerifier.create(saga.execute(ACCOUNT_ID, request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).contains("Transaction service unavailable");
                })
                .verify();

        // Verify no balance update or compensation attempted
        verify(accountServiceClient, never()).updateBalance(anyString(), any());
        verify(transactionServiceClient, never()).reverseTransaction(anyString());
    }
}
