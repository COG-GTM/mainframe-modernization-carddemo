package com.cardemo.equivalence.unit.billing;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.TransactionRepository;
import com.cardemo.batch.service.BillPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for bill payment atomicity.
 * Business rule 3: Account balance update + transaction record write must be atomic.
 * Both operations must succeed or fail together.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Bill Payment Atomicity Tests")
class BillPaymentAtomicityTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private BillPaymentService billPaymentService;

    @BeforeEach
    void setUp() {
        billPaymentService = new BillPaymentService(accountRepository, transactionRepository);
    }

    private Account createAccount(long acctId, BigDecimal balance, BigDecimal credit, BigDecimal debit) {
        Account account = new Account();
        account.setAcctId(acctId);
        account.setCurrentBalance(balance);
        account.setCurrentCycleCredit(credit);
        account.setCurrentCycleDebit(debit);
        return account;
    }

    private Transaction createTransaction(String id, BigDecimal amount) {
        Transaction txn = new Transaction();
        txn.setId(id);
        txn.setAmount(amount);
        return txn;
    }

    @Test
    @DisplayName("Successful payment updates balance and writes transaction")
    void processPayment_success_updatesBalanceAndWritesTransaction() {
        Account account = createAccount(1001L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN001", new BigDecimal("-100.00"));
        billPaymentService.processBillPayment(1001L, new BigDecimal("-100.00"), txn);

        verify(accountRepository).save(account);
        verify(transactionRepository).save(txn);
    }

    @Test
    @DisplayName("Payment reduces account balance correctly")
    void processPayment_reducesBalance() {
        Account account = createAccount(1002L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1002L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN002", new BigDecimal("-100.00"));
        billPaymentService.processBillPayment(1002L, new BigDecimal("-100.00"), txn);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("400.00"), captor.getValue().getCurrentBalance());
    }

    @Test
    @DisplayName("Account not found throws IllegalStateException")
    void processPayment_accountNotFound_throws() {
        when(accountRepository.findById(9999L)).thenReturn(Optional.empty());
        Transaction txn = createTransaction("TXN_FAIL", new BigDecimal("-50.00"));

        assertThrows(IllegalStateException.class,
                () -> billPaymentService.processBillPayment(9999L, new BigDecimal("-50.00"), txn));
    }

    @Test
    @DisplayName("When account not found, no transaction is saved")
    void processPayment_accountNotFound_noTransactionSaved() {
        when(accountRepository.findById(8888L)).thenReturn(Optional.empty());
        Transaction txn = createTransaction("TXN_NOSAVE", new BigDecimal("-50.00"));

        try {
            billPaymentService.processBillPayment(8888L, new BigDecimal("-50.00"), txn);
        } catch (IllegalStateException e) {
            // expected
        }

        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("When transaction save fails, exception propagates for rollback")
    void processPayment_transactionSaveFails_exceptionPropagates() {
        Account account = createAccount(1003L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1003L)).thenReturn(Optional.of(account));
        Transaction txn = createTransaction("TXN_FAIL2", new BigDecimal("-50.00"));
        doThrow(new RuntimeException("DB error")).when(transactionRepository).save(txn);

        assertThrows(RuntimeException.class,
                () -> billPaymentService.processBillPayment(1003L, new BigDecimal("-50.00"), txn));
    }

    @Test
    @DisplayName("Negative amount (payment) updates cycle debit")
    void processPayment_negativeAmount_updatesCycleDebit() {
        Account account = createAccount(1004L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1004L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN004", new BigDecimal("-200.00"));
        billPaymentService.processBillPayment(1004L, new BigDecimal("-200.00"), txn);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("-200.00"), captor.getValue().getCurrentCycleDebit());
    }

    @Test
    @DisplayName("Positive amount (purchase) updates cycle credit")
    void processPayment_positiveAmount_updatesCycleCredit() {
        Account account = createAccount(1005L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1005L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN005", new BigDecimal("150.00"));
        billPaymentService.processBillPayment(1005L, new BigDecimal("150.00"), txn);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getCurrentCycleCredit());
    }

    @Test
    @DisplayName("Zero amount updates neither credit nor debit cycle (goes to credit)")
    void processPayment_zeroAmount_updatesBalance() {
        Account account = createAccount(1006L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1006L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN006", BigDecimal.ZERO);
        billPaymentService.processBillPayment(1006L, BigDecimal.ZERO, txn);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("500.00"), captor.getValue().getCurrentBalance());
        assertEquals(BigDecimal.ZERO, captor.getValue().getCurrentCycleCredit());
    }

    @Test
    @DisplayName("Account save happens before transaction save")
    void processPayment_saveOrder() {
        Account account = createAccount(1007L, new BigDecimal("500.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(accountRepository.findById(1007L)).thenReturn(Optional.of(account));

        Transaction txn = createTransaction("TXN007", new BigDecimal("-50.00"));

        var accountOrder = inOrder(accountRepository, transactionRepository);

        billPaymentService.processBillPayment(1007L, new BigDecimal("-50.00"), txn);

        accountOrder.verify(accountRepository).save(account);
        accountOrder.verify(transactionRepository).save(txn);
    }
}
