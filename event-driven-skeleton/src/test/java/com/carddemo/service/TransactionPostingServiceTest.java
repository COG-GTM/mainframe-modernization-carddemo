package com.carddemo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.event.TransactionPostedEvent;
import com.carddemo.event.TransactionRejectedEvent;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionValidationService.ValidationResult;

/**
 * Unit tests for TransactionPostingService.
 *
 * Verifies the orchestration logic that replaces CBTRN02C main loop:
 *   validate → post (update category bal + account bal + write txn) → emit event
 *   validate → reject → emit rejection event
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingServiceTest {

    @Mock
    private TransactionValidationService validationService;

    @Mock
    private AccountUpdateService accountUpdateService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private TransactionPostingService postingService;

    @BeforeEach
    void setUp() {
        postingService = new TransactionPostingService(
                validationService, accountUpdateService, kafkaTemplate);
    }

    private TransactionPendingEvent createEvent() {
        return new TransactionPendingEvent(
                "TXN-001", "SA", 5001, "ONLINE", "Test purchase",
                new BigDecimal("100.00"), 123456789L, "Test Merchant",
                "Test City", "12345", "4111111111111111",
                "2025-01-15-10.30.00.000000", null
        );
    }

    @Test
    @DisplayName("Valid transaction: updates category balance, account, writes txn, publishes posted event")
    void shouldPostValidTransaction() {
        TransactionPendingEvent event = createEvent();
        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        Account account = new Account(5001L);
        Transaction postedTxn = new Transaction(
                "TXN-001", "SA", 5001, "ONLINE", "Test purchase",
                new BigDecimal("100.00"), 123456789L, "Test Merchant",
                "Test City", "12345", "4111111111111111",
                "2025-01-15-10.30.00.000000", "2025-01-15-11.00.00.000000"
        );

        when(validationService.validate(event))
                .thenReturn(ValidationResult.success(xref, account));
        when(accountUpdateService.writeTransaction(event)).thenReturn(postedTxn);

        postingService.postTransaction(event);

        // Verify: 2700-UPDATE-TCATBAL
        verify(accountUpdateService).updateTransactionCategoryBalance(
                5001L, "SA", 5001, new BigDecimal("100.00"));

        // Verify: 2800-UPDATE-ACCOUNT-REC
        verify(accountUpdateService).updateAccountBalance(account, new BigDecimal("100.00"));

        // Verify: 2900-WRITE-TRANSACTION-FILE
        verify(accountUpdateService).writeTransaction(event);

        // Verify: posted event published (replaces TRANSACT → CREASTMT coupling)
        verify(kafkaTemplate).send(eq("transaction.posted"), eq("4111111111111111"),
                any(TransactionPostedEvent.class));

        // Verify: no rejection event
        verify(kafkaTemplate, never()).send(eq("transaction.rejected"), any(), any());
    }

    @Test
    @DisplayName("Invalid transaction: publishes rejection event, no account updates")
    void shouldRejectInvalidTransaction() {
        TransactionPendingEvent event = createEvent();

        when(validationService.validate(event))
                .thenReturn(ValidationResult.failure(100, "INVALID CARD NUMBER FOUND"));

        postingService.postTransaction(event);

        // Verify: rejection event published (replaces 2500-WRITE-REJECT-REC)
        verify(kafkaTemplate).send(eq("transaction.rejected"), eq("TXN-001"),
                any(TransactionRejectedEvent.class));

        // Verify: no account updates performed
        verify(accountUpdateService, never()).updateTransactionCategoryBalance(
                any(Long.class), any(), any(Integer.class), any());
        verify(accountUpdateService, never()).updateAccountBalance(any(), any());
        verify(accountUpdateService, never()).writeTransaction(any());

        // Verify: no posted event
        verify(kafkaTemplate, never()).send(eq("transaction.posted"), any(), any());
    }
}
