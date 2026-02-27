package com.carddemo.service;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.BusinessException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private BillPaymentService billPaymentService;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(10000000001L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrBal(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setCurrCycCredit(BigDecimal.ZERO);
        testAccount.setCurrCycDebit(BigDecimal.ZERO);
    }

    @Test
    void processPayment_success() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        BillPaymentRequest request = new BillPaymentRequest(10000000001L, new BigDecimal("500.00"));
        Transaction result = billPaymentService.processPayment(request);
        assertNotNull(result);
        assertEquals("BP", result.getTranTypeCd());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void processPayment_accountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        BillPaymentRequest request = new BillPaymentRequest(999L, new BigDecimal("100.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> billPaymentService.processPayment(request));
        assertEquals("3100", ex.getErrorCode());
    }

    @Test
    void processPayment_accountInactive() {
        testAccount.setActiveStatus("N");
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        BillPaymentRequest request = new BillPaymentRequest(10000000001L, new BigDecimal("100.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> billPaymentService.processPayment(request));
        assertEquals("4300", ex.getErrorCode());
    }

    @Test
    void processPayment_negativeAmount() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        BillPaymentRequest request = new BillPaymentRequest(10000000001L, new BigDecimal("-100.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> billPaymentService.processPayment(request));
        assertEquals("5200", ex.getErrorCode());
    }

    @Test
    void processPayment_zeroAmount() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        BillPaymentRequest request = new BillPaymentRequest(10000000001L, BigDecimal.ZERO);
        BusinessException ex = assertThrows(BusinessException.class, () -> billPaymentService.processPayment(request));
        assertEquals("5200", ex.getErrorCode());
    }
}
