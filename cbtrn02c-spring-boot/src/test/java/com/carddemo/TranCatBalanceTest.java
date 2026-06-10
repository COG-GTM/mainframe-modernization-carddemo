package com.carddemo;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.TranCatBalanceId;
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
class TranCatBalanceTest {

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
        accountRepository.save(TestDataFactory.account());
    }

    @Test
    void testNewTranCatBalCreated() {
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000020", new BigDecimal("125.50"));

        service.processSingleTransaction(txn);

        TranCatBalance bal = tranCatBalanceRepository
                .findByAcctIdAndTypeCdAndCatCd(TestDataFactory.ACCT_ID, "01", 5).orElseThrow();
        assertEquals(new BigDecimal("125.50"), bal.getBalance());
    }

    @Test
    void testExistingTranCatBalUpdated() {
        tranCatBalanceRepository.save(new TranCatBalance(
                new TranCatBalanceId(TestDataFactory.ACCT_ID, "01", 5), new BigDecimal("100.00")));

        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000021", new BigDecimal("25.50"));

        service.processSingleTransaction(txn);

        TranCatBalance bal = tranCatBalanceRepository
                .findByAcctIdAndTypeCdAndCatCd(TestDataFactory.ACCT_ID, "01", 5).orElseThrow();
        assertEquals(new BigDecimal("125.50"), bal.getBalance());
    }
}
