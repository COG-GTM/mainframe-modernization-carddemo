package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.CardXrefEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionService.
 *
 * COBOL Traceability: Tests business logic that replaces
 * COTRN00C (list), COTRN01C (view), COTRN02C (add).
 */
class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private CardXrefRepository cardXrefRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        cardXrefRepository = mock(CardXrefRepository.class);
        transactionService = new TransactionService(transactionRepository, cardXrefRepository);
    }

    @Test
    void listTransactions_shouldReturnPaginatedResults() {
        TransactionEntity entity = createSampleEntity();
        Page<TransactionEntity> page = new PageImpl<>(
                List.of(entity), PageRequest.of(0, 10), 1);
        when(transactionRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(page);

        TransactionListResponse result = transactionService.listTransactions(
                0, 10, null, null, null, null);

        assertEquals(1, result.totalElements());
        assertEquals(1, result.transactions().size());
        assertEquals("0000000000000001", result.transactions().get(0).transactionId());
    }

    @Test
    void listTransactions_withDateFilter_shouldParseDates() {
        Page<TransactionEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(transactionRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(page);

        TransactionListResponse result = transactionService.listTransactions(
                0, 10, null, null, "2024-01-01", "2024-01-31");

        assertEquals(0, result.totalElements());
    }

    @Test
    void listTransactions_withInvalidDate_shouldThrow() {
        assertThrows(InvalidRequestException.class, () ->
                transactionService.listTransactions(0, 10, null, null, "invalid", null));
    }

    @Test
    void getTransaction_shouldReturnDetail() {
        TransactionEntity entity = createSampleEntity();
        when(transactionRepository.findById("0000000000000001"))
                .thenReturn(Optional.of(entity));

        TransactionResponse response = transactionService.getTransaction("0000000000000001");

        assertEquals("0000000000000001", response.transactionId());
        assertEquals("01", response.typeCode());
        assertEquals(new BigDecimal("45.67"), response.amount());
    }

    @Test
    void getTransaction_notFound_shouldThrow() {
        when(transactionRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.getTransaction("9999999999999999"));
    }

    @Test
    void createTransaction_withCardNumber_shouldCreateSuccessfully() {
        CardXrefEntity xref = new CardXrefEntity();
        xref.setCardNumber("4111111111111111");
        xref.setAccountId("00000000001");

        when(cardXrefRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(xref));
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(11L);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest(
                null, "4111111111111111", "01", 5001, "POS TERM",
                "TEST PURCHASE", new BigDecimal("99.99"),
                100000001L, "TEST MERCHANT", "NEW YORK", "10001",
                null, null);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals("0000000000000011", response.transactionId());
        assertEquals("4111111111111111", response.cardNumber());
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_withAccountId_shouldLookupCard() {
        CardXrefEntity xref = new CardXrefEntity();
        xref.setCardNumber("4111111111111111");
        xref.setAccountId("00000000001");

        when(cardXrefRepository.findByAccountId("00000000001"))
                .thenReturn(Optional.of(xref));
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(6L);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest(
                "00000000001", null, "01", 5001, "ONLINE",
                "ONLINE PURCHASE", new BigDecimal("29.99"),
                null, null, null, null, null, null);

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals("4111111111111111", response.cardNumber());
        assertEquals("0000000000000006", response.transactionId());
    }

    @Test
    void createTransaction_noAccountOrCard_shouldThrow() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                null, null, "01", 5001, null,
                "TEST", new BigDecimal("10.00"),
                null, null, null, null, null, null);

        assertThrows(InvalidRequestException.class, () ->
                transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_cardNotInXref_shouldThrow() {
        when(cardXrefRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(
                null, "9999999999999999", "01", 5001, null,
                "TEST", new BigDecimal("10.00"),
                null, null, null, null, null, null);

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(request));
    }

    @Test
    void generateNextTransactionId_shouldUseSequence() {
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(100L);

        String nextId = transactionService.generateNextTransactionId();
        assertEquals("0000000000000100", nextId);
    }

    @Test
    void generateNextTransactionId_firstValue_shouldFormatCorrectly() {
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(1L);

        String nextId = transactionService.generateNextTransactionId();
        assertEquals("0000000000000001", nextId);
    }

    private TransactionEntity createSampleEntity() {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId("0000000000000001");
        entity.setTypeCode("01");
        entity.setCategoryCode(5001);
        entity.setSource("POS TERM");
        entity.setDescription("GROCERY STORE PURCHASE");
        entity.setAmount(new BigDecimal("45.67"));
        entity.setMerchantId(100000001L);
        entity.setMerchantName("WHOLE FOODS MARKET");
        entity.setMerchantCity("NEW YORK");
        entity.setMerchantZip("10001");
        entity.setCardNumber("4111111111111111");
        entity.setOriginTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        entity.setProcessedTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 5));
        return entity;
    }
}
