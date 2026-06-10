package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionPostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AccountUpdateTest {

    @Autowired
    private TransactionPostingService service;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired
    private TransactionRejectRepository transactionRejectRepository;

    @BeforeEach
    void setUp() {
        transactionRejectRepository.deleteAll();
        transactionRepository.deleteAll();
        tranCatBalanceRepository.deleteAll();
        accountRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardXrefRepository.save(TestDataFactory.xref());
    }

    @Test
    void testPositiveAmountUpdatesCycCredit() {
        accountRepository.save(TestDataFactory.account());
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000010", new BigDecimal("200.00"));

        service.processSingleTransaction(txn);

        Account account = accountRepository.findByAcctId(TestDataFactory.ACCT_ID).orElseThrow();
        assertEquals(new BigDecimal("300.00"), account.getCurrBal());
        assertEquals(new BigDecimal("200.00"), account.getCurrCycCredit());
        assertEquals(new BigDecimal("0.00"), account.getCurrCycDebit());
    }

    @Test
    void testNegativeAmountUpdatesCycDebit() {
        accountRepository.save(TestDataFactory.account());
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000011", new BigDecimal("-75.00"));

        service.processSingleTransaction(txn);

        Account account = accountRepository.findByAcctId(TestDataFactory.ACCT_ID).orElseThrow();
        assertEquals(new BigDecimal("25.00"), account.getCurrBal());
        assertEquals(new BigDecimal("0.00"), account.getCurrCycCredit());
        assertEquals(new BigDecimal("-75.00"), account.getCurrCycDebit());
    }
}
