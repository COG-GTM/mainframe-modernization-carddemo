package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionService.
 * Tests the business logic migrated from COTRN02C ADD-TRANSACTION,
 * COPY-LAST-TRAN-DATA, and transaction ID generation.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardLookupService cardLookupService;

    @Mock
    private DateValidationService dateValidationService;

    @InjectMocks
    private TransactionService transactionService;

    private CreateTransactionRequest validRequest;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTransactionRequest();
        validRequest.setCardNumber("4111111111111111");
        validRequest.setTransactionTypeCd("01");
        validRequest.setTransactionCatCd(5001);
        validRequest.setSource("ONLINE");
        validRequest.setDescription("Test purchase");
        validRequest.setAmount(new BigDecimal("-150.75"));
        validRequest.setOriginationDate(LocalDate.of(2024, 1, 15));
        validRequest.setProcessingDate(LocalDate.of(2024, 1, 15));
        validRequest.setMerchantId(123456789);
        validRequest.setMerchantName("Test Merchant");
        validRequest.setMerchantCity("Seattle");
        validRequest.setMerchantZip("98101");

        existingTransaction = new Transaction();
        existingTransaction.setTranId("0000000000000003");
        existingTransaction.setTranTypeCd("01");
        existingTransaction.setTranCatCd(5001);
        existingTransaction.setTranSource("ONLINE");
        existingTransaction.setTranDesc("Existing transaction");
        existingTransaction.setTranAmt(new BigDecimal("-100.00"));
        existingTransaction.setTranCardNum("4111111111111111");
        existingTransaction.setTranMerchantId(123456789);
        existingTransaction.setTranMerchantName("Existing Merchant");
        existingTransaction.setTranMerchantCity("Portland");
        existingTransaction.setTranMerchantZip("97201");
        existingTransaction.setTranOrigTs("2024-01-10");
        existingTransaction.setTranProcTs("2024-01-10");
    }

    /**
     * Tests successful transaction creation.
     * Verifies the ADD-TRANSACTION flow:
     * 1. Card lookup resolves card number
     * 2. New ID is generated (last ID + 1)
     * 3. Transaction is saved
     * 4. Success message includes the generated ID
     */
    @Test
    void createTransaction_success() {
        when(cardLookupService.resolveCardNumber(null, "4111111111111111"))
                .thenReturn("4111111111111111");
        when(transactionRepository.findTopByOrderByTranIdDesc())
                .thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(validRequest);

        assertNotNull(response);
        assertEquals("0000000000000004", response.getTransactionId());
        assertEquals("4111111111111111", response.getCardNumber());
        assertEquals("01", response.getTransactionTypeCd());
        assertEquals(5001, response.getTransactionCatCd());
        assertEquals("ONLINE", response.getSource());
        assertEquals("Test purchase", response.getDescription());
        assertEquals(new BigDecimal("-150.75"), response.getAmount());
        assertTrue(response.getMessage().contains("Transaction added successfully"));
        assertTrue(response.getMessage().contains("0000000000000004"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    /**
     * Tests transaction creation when no previous transactions exist.
     * The first transaction should get ID "0000000000000001".
     */
    @Test
    void createTransaction_firstTransaction() {
        when(cardLookupService.resolveCardNumber(null, "4111111111111111"))
                .thenReturn("4111111111111111");
        when(transactionRepository.findTopByOrderByTranIdDesc())
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(validRequest);

        assertEquals("0000000000000001", response.getTransactionId());
    }

    /**
     * Tests transaction creation using account ID instead of card number.
     */
    @Test
    void createTransaction_withAccountId() {
        validRequest.setAccountId("12345678901");
        validRequest.setCardNumber(null);

        when(cardLookupService.resolveCardNumber("12345678901", null))
                .thenReturn("4111111111111111");
        when(transactionRepository.findTopByOrderByTranIdDesc())
                .thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(validRequest);

        assertNotNull(response);
        assertEquals("4111111111111111", response.getCardNumber());
    }

    /**
     * Tests retrieving a transaction by ID.
     * Replaces: READ-TRANSACT-FILE in COTRN01C.
     */
    @Test
    void getTransaction_success() {
        when(transactionRepository.findById("0000000000000003"))
                .thenReturn(Optional.of(existingTransaction));

        TransactionResponse response = transactionService.getTransaction("0000000000000003");

        assertNotNull(response);
        assertEquals("0000000000000003", response.getTransactionId());
        assertEquals("Existing transaction", response.getDescription());
    }

    /**
     * Tests that getting a non-existent transaction throws ResourceNotFoundException.
     * Replaces: DFHRESP(NOTFND) handling in READ-TRANSACT-FILE.
     */
    @Test
    void getTransaction_notFound() {
        when(transactionRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransaction("9999999999999999"));
    }

    /**
     * Tests getting the last transaction (COPY-LAST-TRAN-DATA / PF5).
     */
    @Test
    void getLastTransaction_success() {
        when(transactionRepository.findTopByOrderByTranIdDesc())
                .thenReturn(Optional.of(existingTransaction));

        TransactionResponse response = transactionService.getLastTransaction();

        assertNotNull(response);
        assertEquals("0000000000000003", response.getTransactionId());
    }

    /**
     * Tests that getting last transaction when none exist throws exception.
     */
    @Test
    void getLastTransaction_noTransactions() {
        when(transactionRepository.findTopByOrderByTranIdDesc())
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getLastTransaction());
    }
}
