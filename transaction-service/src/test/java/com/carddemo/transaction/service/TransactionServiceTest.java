package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.*;
import com.carddemo.transaction.entity.Account;
import com.carddemo.transaction.entity.CardXref;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.exception.AccountNotFoundException;
import com.carddemo.transaction.exception.TransactionNotFoundException;
import com.carddemo.transaction.exception.TransactionValidationException;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TransactionService.
 * Covers list, view, add, and bill payment operations.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private TransactionIdGenerator idGenerator;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setTranId("0000000000000001");
        sampleTransaction.setTranTypeCd("01");
        sampleTransaction.setTranCatCd(1234);
        sampleTransaction.setTranSource("ONLINE");
        sampleTransaction.setTranDesc("Test transaction");
        sampleTransaction.setTranAmt(new BigDecimal("100.00"));
        sampleTransaction.setTranMerchantId(123456789L);
        sampleTransaction.setTranMerchantName("Test Merchant");
        sampleTransaction.setTranMerchantCity("New York");
        sampleTransaction.setTranMerchantZip("10001");
        sampleTransaction.setTranCardNum("4567890123456789");
        sampleTransaction.setTranOrigTs("2024-01-15 10:30:00.000000");
        sampleTransaction.setTranProcTs("2024-01-15 10:30:00.000000");
    }

    // --- listTransactions (COTRN00C) ---

    @Test
    void listTransactions_defaultPagination_returns10PerPage() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findAllByOrderByTranIdAsc(any(Pageable.class)))
                .thenReturn(page);

        TransactionListResponse response = transactionService.listTransactions(0, null, null);
        assertNotNull(response);
        assertEquals(1, response.transactions().size());
        assertEquals(1, response.pageNumber());
    }

    @Test
    void listTransactions_withFilter_validatesNumeric() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.listTransactions(0, null, "ABC"));
        assertEquals("Tran ID must be Numeric ...", ex.getMessage());
    }

    @Test
    void listTransactions_withNumericFilter_filtersById() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findByTranIdGreaterThanEqualOrderByTranIdAsc(
                eq("0001"), any(Pageable.class))).thenReturn(page);

        TransactionListResponse response = transactionService.listTransactions(0, null, "0001");
        assertEquals(1, response.transactions().size());
    }

    @Test
    void listTransactions_emptyResult_returnsEmptyList() {
        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.findAllByOrderByTranIdAsc(any(Pageable.class)))
                .thenReturn(page);

        TransactionListResponse response = transactionService.listTransactions(0, null, null);
        assertTrue(response.transactions().isEmpty());
        assertFalse(response.hasNextPage());
    }

    // --- viewTransaction (COTRN01C) ---

    @Test
    void viewTransaction_emptyId_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.viewTransaction(""));
        assertEquals("Tran ID can NOT be empty...", ex.getMessage());
    }

    @Test
    void viewTransaction_notFound_throwsError() {
        when(transactionRepository.findById("9999999999999999")).thenReturn(Optional.empty());
        TransactionNotFoundException ex = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.viewTransaction("9999999999999999"));
        assertEquals("Transaction ID NOT found...", ex.getMessage());
    }

    @Test
    void viewTransaction_found_returnsResponse() {
        when(transactionRepository.findById("0000000000000001"))
                .thenReturn(Optional.of(sampleTransaction));
        TransactionResponse response = transactionService.viewTransaction("0000000000000001");
        assertEquals("0000000000000001", response.tranId());
        assertEquals("Test transaction", response.tranDesc());
    }

    // --- addTransaction (COTRN02C) ---

    @Test
    void addTransaction_notConfirmed_throwsError() {
        TransactionAddRequest request = new TransactionAddRequest(
                "12345678901", null, "01", "1234", "ONLINE", "Test",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345", false);

        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.addTransaction(request));
        assertEquals("Confirm to add this transaction...", ex.getMessage());
    }

    @Test
    void addTransaction_validRequest_createsTransaction() {
        TransactionAddRequest request = new TransactionAddRequest(
                "12345678901", null, "01", "1234", "ONLINE", "Test purchase",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Test Merchant", "New York", "10001", true);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4567890123456789");
        xref.setXrefAcctId(12345678901L);
        when(cardXrefRepository.findByXrefAcctId(12345678901L)).thenReturn(Optional.of(xref));
        when(idGenerator.generateNextId()).thenReturn("0000000000000001");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.addTransaction(request);
        assertEquals("0000000000000001", response.tranId());
        assertEquals("01", response.tranTypeCd());
        assertEquals(1234, response.tranCatCd());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void addTransaction_byCardNumber_resolvesCard() {
        TransactionAddRequest request = new TransactionAddRequest(
                null, "4567890123456789", "01", "1234", "ONLINE", "Test purchase",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Test Merchant", "New York", "10001", true);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4567890123456789");
        xref.setXrefAcctId(12345678901L);
        when(cardXrefRepository.findByXrefCardNum("4567890123456789"))
                .thenReturn(Optional.of(xref));
        when(idGenerator.generateNextId()).thenReturn("0000000000000002");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.addTransaction(request);
        assertEquals("4567890123456789", response.tranCardNum());
    }

    // --- processBillPayment (COBIL00C) ---

    @Test
    void processBillPayment_emptyAccountId_throwsError() {
        BillPaymentRequest request = new BillPaymentRequest("", true);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.processBillPayment(request));
        assertEquals("Acct ID can NOT be empty...", ex.getMessage());
    }

    @Test
    void processBillPayment_notConfirmed_throwsError() {
        BillPaymentRequest request = new BillPaymentRequest("12345678901", false);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.processBillPayment(request));
        assertEquals("Confirm to make a bill payment...", ex.getMessage());
    }

    @Test
    void processBillPayment_nonNumericAccountId_throwsError() {
        BillPaymentRequest request = new BillPaymentRequest("ABC", true);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.processBillPayment(request));
        assertEquals("Account ID must be Numeric...", ex.getMessage());
    }

    @Test
    void processBillPayment_accountNotFound_throwsError() {
        BillPaymentRequest request = new BillPaymentRequest("99999999999", true);
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> transactionService.processBillPayment(request));
    }

    @Test
    void processBillPayment_zeroBalance_throwsError() {
        Account account = new Account();
        account.setAcctId(12345678901L);
        account.setAcctCurrBal(BigDecimal.ZERO);
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(account));

        BillPaymentRequest request = new BillPaymentRequest("12345678901", true);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.processBillPayment(request));
        assertEquals("You have nothing to pay...", ex.getMessage());
    }

    @Test
    void processBillPayment_success_createsTransactionAndUpdatesBalance() {
        Account account = new Account();
        account.setAcctId(12345678901L);
        account.setAcctCurrBal(new BigDecimal("500.00"));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(account));

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4567890123456789");
        xref.setXrefAcctId(12345678901L);
        when(cardXrefRepository.findByXrefAcctId(12345678901L)).thenReturn(Optional.of(xref));

        when(idGenerator.generateNextId()).thenReturn("0000000000000099");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        BillPaymentRequest request = new BillPaymentRequest("12345678901", true);
        BillPaymentResponse response = transactionService.processBillPayment(request);

        assertEquals("0000000000000099", response.transactionId());
        assertEquals(new BigDecimal("500.00"), response.amountPaid());
        assertEquals(new BigDecimal("0.00"), response.newBalance());

        // Verify atomic: both transaction and account saved
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(any(Account.class));
    }
}
