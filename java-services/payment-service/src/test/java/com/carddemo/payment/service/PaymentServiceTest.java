package com.carddemo.payment.service;

import com.carddemo.payment.dto.PaymentRequest;
import com.carddemo.payment.dto.PaymentResponse;
import com.carddemo.payment.exception.PaymentException;
import com.carddemo.payment.exception.ResourceNotFoundException;
import com.carddemo.payment.model.Account;
import com.carddemo.payment.model.CardCrossReference;
import com.carddemo.payment.model.Transaction;
import com.carddemo.payment.repository.AccountRepository;
import com.carddemo.payment.repository.CardCrossReferenceRepository;
import com.carddemo.payment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Account testAccount;
    private CardCrossReference testXref;
    private Transaction lastTransaction;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(12345678901L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("5000.00"));

        testXref = new CardCrossReference();
        testXref.setCardNumber("4111111111111111");
        testXref.setCustomerId(100000001L);
        testXref.setAccountId(12345678901L);

        lastTransaction = new Transaction();
        lastTransaction.setTranId("0000000000000100");
    }

    @Test
    void processBillPayment_successful() {
        // Arrange
        PaymentRequest request = new PaymentRequest(12345678901L);
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(testAccount));
        when(cardCrossReferenceRepository.findByAccountId(12345678901L)).thenReturn(Optional.of(testXref));
        when(transactionRepository.findTopByOrderByTranIdDesc()).thenReturn(Optional.of(lastTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentResponse response = paymentService.processBillPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals("0000000000000101", response.getTransactionId());
        assertEquals(12345678901L, response.getAccountId());
        assertEquals(new BigDecimal("1500.00"), response.getAmountPaid());
        assertEquals(new BigDecimal("1500.00"), response.getPreviousBalance());
        assertEquals(BigDecimal.ZERO, response.getNewBalance());
        assertTrue(response.getMessage().contains("Payment successful"));
        assertTrue(response.getMessage().contains("0000000000000101"));

        // Verify transaction was created with correct COBOL-equivalent values
        ArgumentCaptor<Transaction> tranCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(tranCaptor.capture());
        Transaction savedTran = tranCaptor.getValue();
        assertEquals("02", savedTran.getTypeCode());
        assertEquals(2, savedTran.getCategoryCode());
        assertEquals("POS TERM", savedTran.getSource());
        assertEquals("BILL PAYMENT - ONLINE", savedTran.getDescription());
        assertEquals(new BigDecimal("1500.00"), savedTran.getAmount());
        assertEquals("4111111111111111", savedTran.getCardNumber());
        assertEquals(999999999L, savedTran.getMerchantId());
        assertEquals("BILL PAYMENT", savedTran.getMerchantName());
        assertEquals("N/A", savedTran.getMerchantCity());
        assertEquals("N/A", savedTran.getMerchantZip());

        // Verify account balance was set to 0
        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(acctCaptor.capture());
        assertEquals(0, acctCaptor.getValue().getCurrentBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void processBillPayment_zeroBalance_throwsPaymentException() {
        // Arrange - mirrors COBOL: IF ACCT-CURR-BAL <= ZEROS
        testAccount.setCurrentBalance(BigDecimal.ZERO);
        PaymentRequest request = new PaymentRequest(12345678901L);
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class,
                () -> paymentService.processBillPayment(request));
        assertEquals("You have nothing to pay...", exception.getMessage());

        // Verify no transaction was created
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void processBillPayment_accountNotFound_throwsResourceNotFoundException() {
        // Arrange - mirrors COBOL: WHEN DFHRESP(NOTFND)
        PaymentRequest request = new PaymentRequest(99999999999L);
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processBillPayment(request));
        assertEquals("Account ID NOT found...", exception.getMessage());

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processBillPayment_negativeBalance_throwsPaymentException() {
        // Arrange - negative balance also triggers "nothing to pay"
        testAccount.setCurrentBalance(new BigDecimal("-50.00"));
        PaymentRequest request = new PaymentRequest(12345678901L);
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class,
                () -> paymentService.processBillPayment(request));
        assertEquals("You have nothing to pay...", exception.getMessage());
    }

    @Test
    void processBillPayment_noExistingTransactions_startsFromOne() {
        // Arrange - mirrors COBOL: WHEN DFHRESP(ENDFILE) -> MOVE ZEROS TO TRAN-ID, ADD 1
        PaymentRequest request = new PaymentRequest(12345678901L);
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(testAccount));
        when(cardCrossReferenceRepository.findByAccountId(12345678901L)).thenReturn(Optional.of(testXref));
        when(transactionRepository.findTopByOrderByTranIdDesc()).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentResponse response = paymentService.processBillPayment(request);

        // Assert
        assertEquals("0000000000000001", response.getTransactionId());
    }

    @Test
    void getAccountBalance_found() {
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(testAccount));

        Account result = paymentService.getAccountBalance(12345678901L);

        assertNotNull(result);
        assertEquals(12345678901L, result.getAcctId());
        assertEquals(new BigDecimal("1500.00"), result.getCurrentBalance());
        assertEquals("Y", result.getActiveStatus());
    }

    @Test
    void getAccountBalance_notFound() {
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getAccountBalance(99999999999L));
    }
}
