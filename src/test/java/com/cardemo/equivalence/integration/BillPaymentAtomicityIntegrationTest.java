package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.TransactionRepository;
import com.cardemo.batch.service.BillPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for bill payment atomicity with real Spring context.
 * Business rule 3: Account balance update + transaction write must be atomic.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Bill Payment Atomicity Integration Tests")
class BillPaymentAtomicityIntegrationTest {

    @Autowired
    private BillPaymentService billPaymentService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        Account account = new Account();
        account.setAcctId(30001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("2000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("500.00"));
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate("2025-12-31");
        account.setGroupId("STANDARD");
        accountRepository.save(account);
    }

    private Transaction createTransaction(String id, BigDecimal amount) {
        Transaction txn = new Transaction();
        txn.setId(id);
        txn.setAmount(amount);
        txn.setTypeCd("01");
        txn.setCatCd(5);
        txn.setSource("System");
        txn.setDescription("Test payment");
        return txn;
    }

    @Test
    @DisplayName("IT: Successful payment updates both account and transaction")
    void successfulPayment_updatesBoth() {
        Transaction txn = createTransaction("ITXN_PAY1", new BigDecimal("-200.00"));
        billPaymentService.processBillPayment(30001L, new BigDecimal("-200.00"), txn);

        Account updated = accountRepository.findById(30001L).orElseThrow();
        assertEquals(new BigDecimal("1800.00"), updated.getCurrentBalance());

        assertTrue(transactionRepository.findById("ITXN_PAY1").isPresent());
    }

    @Test
    @DisplayName("IT: Payment reduces balance correctly")
    void payment_reducesBalance() {
        Transaction txn = createTransaction("ITXN_PAY2", new BigDecimal("-500.00"));
        billPaymentService.processBillPayment(30001L, new BigDecimal("-500.00"), txn);

        Account updated = accountRepository.findById(30001L).orElseThrow();
        assertEquals(new BigDecimal("1500.00"), updated.getCurrentBalance());
    }

    @Test
    @DisplayName("IT: Positive amount updates cycle credit")
    void positiveAmount_updatesCycleCredit() {
        Transaction txn = createTransaction("ITXN_CREDIT", new BigDecimal("300.00"));
        billPaymentService.processBillPayment(30001L, new BigDecimal("300.00"), txn);

        Account updated = accountRepository.findById(30001L).orElseThrow();
        assertEquals(new BigDecimal("800.00"), updated.getCurrentCycleCredit());
        assertEquals(new BigDecimal("2300.00"), updated.getCurrentBalance());
    }

    @Test
    @DisplayName("IT: Negative amount updates cycle debit")
    void negativeAmount_updatesCycleDebit() {
        Transaction txn = createTransaction("ITXN_DEBIT", new BigDecimal("-150.00"));
        billPaymentService.processBillPayment(30001L, new BigDecimal("-150.00"), txn);

        Account updated = accountRepository.findById(30001L).orElseThrow();
        assertEquals(new BigDecimal("-150.00"), updated.getCurrentCycleDebit());
    }

    @Test
    @DisplayName("IT: Account not found throws exception")
    void accountNotFound_throws() {
        Transaction txn = createTransaction("ITXN_NOACCT", new BigDecimal("-50.00"));
        assertThrows(IllegalStateException.class,
                () -> billPaymentService.processBillPayment(99999L, new BigDecimal("-50.00"), txn));
    }

    @Test
    @DisplayName("IT: Multiple payments accumulate correctly")
    void multiplePayments_accumulate() {
        billPaymentService.processBillPayment(30001L, new BigDecimal("100.00"),
                createTransaction("ITXN_M1", new BigDecimal("100.00")));
        billPaymentService.processBillPayment(30001L, new BigDecimal("-50.00"),
                createTransaction("ITXN_M2", new BigDecimal("-50.00")));
        billPaymentService.processBillPayment(30001L, new BigDecimal("200.00"),
                createTransaction("ITXN_M3", new BigDecimal("200.00")));

        Account updated = accountRepository.findById(30001L).orElseThrow();
        assertEquals(new BigDecimal("2250.00"), updated.getCurrentBalance());
    }
}
