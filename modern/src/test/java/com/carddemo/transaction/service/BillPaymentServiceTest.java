package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.BillPaymentRequest;
import com.carddemo.transaction.dto.BillPaymentResponse;
import com.carddemo.transaction.entity.AccountEntity;
import com.carddemo.transaction.entity.CardXrefEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BillPaymentService.
 *
 * COBOL Traceability: Tests the atomic bill payment logic from COBIL00C.
 * Verifies that payment creates transaction + updates balance atomically.
 */
class BillPaymentServiceTest {

    private AccountRepository accountRepository;
    private CardXrefRepository cardXrefRepository;
    private TransactionRepository transactionRepository;
    private BillPaymentService billPaymentService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        cardXrefRepository = mock(CardXrefRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        billPaymentService = new BillPaymentService(
                accountRepository, cardXrefRepository, transactionRepository);
    }

    /**
     * Test: Successful bill payment creates transaction and updates balance.
     * COBOL Traceability: Verifies COBIL00C multi-file update —
     * WRITE TRANSACT + REWRITE ACCTDAT in single unit of work.
     */
    @Test
    void processPayment_shouldCreateTransactionAndUpdateBalance() {
        AccountEntity account = createAccount("00000000001", new BigDecimal("1500.00"));
        CardXrefEntity xref = createXref("4111111111111111", "00000000001");

        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(cardXrefRepository.findByAccountId("00000000001")).thenReturn(Optional.of(xref));
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(11L);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(AccountEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BillPaymentRequest request = new BillPaymentRequest(
                "00000000001", null, new BigDecimal("500.00"));

        BillPaymentResponse response = billPaymentService.processPayment(request);

        assertNotNull(response);
        assertEquals("0000000000000011", response.transactionId());
        assertEquals(new BigDecimal("500.00"), response.amountPaid());
        assertEquals(new BigDecimal("1500.00"), response.previousBalance());
        assertEquals(new BigDecimal("1000.00"), response.newBalance());

        // Verify both saves happened (atomicity)
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(accountRepository).save(any(AccountEntity.class));
    }

    /**
     * Test: Payment with explicit card number.
     * COBOL Traceability: Verifies COBIL00C when card is provided directly.
     */
    @Test
    void processPayment_withCardNumber_shouldUseProvidedCard() {
        AccountEntity account = createAccount("00000000001", new BigDecimal("1500.00"));

        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(transactionRepository.nextTransactionIdFromSequence())
                .thenReturn(6L);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(AccountEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BillPaymentRequest request = new BillPaymentRequest(
                "00000000001", "4111111111111111", new BigDecimal("200.00"));

        BillPaymentResponse response = billPaymentService.processPayment(request);

        assertEquals(new BigDecimal("1300.00"), response.newBalance());
    }

    /**
     * Test: Account not found returns 404.
     * COBOL Traceability: Verifies COBIL00C DFHRESP(NOTFND) on ACCTDAT.
     */
    @Test
    void processPayment_accountNotFound_shouldThrow() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        BillPaymentRequest request = new BillPaymentRequest(
                "99999999999", null, new BigDecimal("100.00"));

        assertThrows(ResourceNotFoundException.class, () ->
                billPaymentService.processPayment(request));
    }

    /**
     * Test: Zero balance account cannot make payment.
     * COBOL Traceability: Verifies COBIL00C check
     * IF ACCT-CURR-BAL <= ZEROS → "You have nothing to pay".
     */
    @Test
    void processPayment_zeroBalance_shouldThrow() {
        AccountEntity account = createAccount("00000000004", BigDecimal.ZERO);
        when(accountRepository.findById("00000000004")).thenReturn(Optional.of(account));

        BillPaymentRequest request = new BillPaymentRequest(
                "00000000004", null, new BigDecimal("100.00"));

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () ->
                billPaymentService.processPayment(request));
        assertEquals("You have nothing to pay", ex.getMessage());
    }

    /**
     * Test: Payment amount exceeding balance is rejected.
     * COBOL Traceability: Verifies balance validation before payment.
     */
    @Test
    void processPayment_amountExceedsBalance_shouldThrow() {
        AccountEntity account = createAccount("00000000001", new BigDecimal("100.00"));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        BillPaymentRequest request = new BillPaymentRequest(
                "00000000001", null, new BigDecimal("500.00"));

        assertThrows(InvalidRequestException.class, () ->
                billPaymentService.processPayment(request));
    }

    /**
     * Test: Negative payment amount is rejected.
     */
    @Test
    void processPayment_negativeAmount_shouldThrow() {
        AccountEntity account = createAccount("00000000001", new BigDecimal("1500.00"));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        BillPaymentRequest request = new BillPaymentRequest(
                "00000000001", null, new BigDecimal("-50.00"));

        assertThrows(InvalidRequestException.class, () ->
                billPaymentService.processPayment(request));
    }

    private AccountEntity createAccount(String accountId, BigDecimal balance) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setCurrentBalance(balance);
        account.setActiveStatus("Y");
        account.setCreditLimit(new BigDecimal("5000.00"));
        return account;
    }

    private CardXrefEntity createXref(String cardNumber, String accountId) {
        CardXrefEntity xref = new CardXrefEntity();
        xref.setCardNumber(cardNumber);
        xref.setAccountId(accountId);
        xref.setCustomerId("000000001");
        return xref;
    }
}
