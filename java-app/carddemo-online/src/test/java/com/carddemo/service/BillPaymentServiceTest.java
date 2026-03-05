package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillPaymentService — validates parity with COBIL00C.
 * Ensures bill payment sets balance to zero and creates a payment transaction.
 */
@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private DailyTransactionRepository dailyTransactionRepository;

    @InjectMocks
    private BillPaymentService billPaymentService;

    @Test
    void payBill_updatesBalanceToZero() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setCurrentBalance(new BigDecimal("1500.00"));

        CardXref xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setAcctId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardXrefRepository.findByAcctId(1L)).thenReturn(List.of(xref));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dailyTransactionRepository.save(any(DailyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyTransaction result = billPaymentService.payBill(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, account.getCurrentBalance());
        verify(accountRepository).save(account);
        verify(dailyTransactionRepository).save(any());
    }

    @Test
    void payBill_accountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> billPaymentService.payBill(999L));
    }

    @Test
    void payBill_zeroBalance_throwsException() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setCurrentBalance(BigDecimal.ZERO);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> billPaymentService.payBill(1L));
    }
}
