package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.TransactionCreateRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.ValidationException;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionTypeRepository transactionTypeRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setTransactionId("0000000000000001");
        sampleTransaction.setTypeCode("01");
        sampleTransaction.setCategoryCode(5001);
        sampleTransaction.setSource("ONLINE");
        sampleTransaction.setDescription("Test Purchase");
        sampleTransaction.setAmount(new BigDecimal("45.67"));
        sampleTransaction.setMerchantId(123456789);
        sampleTransaction.setMerchantName("Test Store");
        sampleTransaction.setMerchantCity("New York");
        sampleTransaction.setMerchantZip("10001");
        sampleTransaction.setCardNumber("4567890123456789");
        sampleTransaction.setOriginTimestamp("2024-01-15-10.30.00.000000");
        sampleTransaction.setProcessedTimestamp("2024-01-15-10.30.05.000000");
    }

    @Test
    void listTransactions_allTransactions_returnsPagedResults() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);

        TransactionListResponse response = transactionService.listTransactions(null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
        assertEquals("0000000000000001", response.getTransactions().get(0).getTransactionId());
        verify(transactionRepository).findAll(any(Pageable.class));
    }

    @Test
    void listTransactions_byCardNumber_filtersResults() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findByCardNumber(eq("4567890123456789"), any(Pageable.class)))
                .thenReturn(page);

        TransactionListResponse response =
                transactionService.listTransactions("4567890123456789", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
        verify(transactionRepository).findByCardNumber(eq("4567890123456789"), any(Pageable.class));
    }

    @Test
    void getTransaction_existingId_returnsDto() {
        when(transactionRepository.findById("0000000000000001"))
                .thenReturn(Optional.of(sampleTransaction));

        TransactionDto dto = transactionService.getTransaction("0000000000000001");

        assertNotNull(dto);
        assertEquals("0000000000000001", dto.getTransactionId());
        assertEquals("01", dto.getTypeCode());
        assertEquals(5001, dto.getCategoryCode());
        assertEquals("Test Purchase", dto.getDescription());
        assertEquals(new BigDecimal("45.67"), dto.getAmount());
        assertEquals("4567890123456789", dto.getCardNumber());
    }

    @Test
    void getTransaction_nonExistingId_throwsResourceNotFound() {
        when(transactionRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransaction("9999999999999999"));
    }

    @Test
    void createTransaction_validRequest_createsAndReturnsDto() {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("01");
        request.setCategoryCode(5001);
        request.setSource("ONLINE");
        request.setDescription("New Purchase");
        request.setAmount(new BigDecimal("99.99"));
        request.setMerchantId(111222333);
        request.setMerchantName("Shop");
        request.setMerchantCity("Boston");
        request.setMerchantZip("02101");

        when(transactionRepository.existsByCardNumber("4567890123456789")).thenReturn(true);
        when(transactionTypeRepository.existsById("01")).thenReturn(true);
        when(transactionRepository.findMaxTransactionId())
                .thenReturn(Optional.of("0000000000000005"));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.createTransaction(request);

        assertNotNull(dto);
        assertEquals("0000000000000006", dto.getTransactionId());
        assertEquals("01", dto.getTypeCode());
        assertEquals(new BigDecimal("99.99"), dto.getAmount());
        assertEquals("4567890123456789", dto.getCardNumber());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_cardNotFound_throwsValidationException() {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("0000000000000000");
        request.setTypeCode("01");
        request.setAmount(new BigDecimal("10.00"));

        when(transactionRepository.existsByCardNumber("0000000000000000")).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_invalidTypeCode_throwsValidationException() {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("99");
        request.setAmount(new BigDecimal("10.00"));

        when(transactionRepository.existsByCardNumber("4567890123456789")).thenReturn(true);
        when(transactionTypeRepository.existsById("99")).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_zeroAmount_throwsValidationException() {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("01");
        request.setAmount(BigDecimal.ZERO);

        when(transactionRepository.existsByCardNumber("4567890123456789")).thenReturn(true);
        when(transactionTypeRepository.existsById("01")).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_firstTransaction_generatesIdFromZero() {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("01");
        request.setCategoryCode(5001);
        request.setAmount(new BigDecimal("25.00"));

        when(transactionRepository.existsByCardNumber("4567890123456789")).thenReturn(true);
        when(transactionTypeRepository.existsById("01")).thenReturn(true);
        when(transactionRepository.findMaxTransactionId()).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = transactionService.createTransaction(request);

        assertEquals("0000000000000001", dto.getTransactionId());
    }
}
