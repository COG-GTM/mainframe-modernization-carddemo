package com.carddemo;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.BatchResult;
import com.carddemo.service.TransactionPostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BatchResultTest {

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
    void testAllValid() {
        List<DailyTransaction> txns = List.of(
                TestDataFactory.transaction("TXN0000000000030", new BigDecimal("10.00")),
                TestDataFactory.transaction("TXN0000000000031", new BigDecimal("20.00")),
                TestDataFactory.transaction("TXN0000000000032", new BigDecimal("30.00")));

        BatchResult result = service.processAllTransactions(txns);

        assertEquals(3, result.getTransactionCount());
        assertEquals(0, result.getRejectCount());
        assertEquals(0, result.getExitStatus());
    }

    @Test
    void testSomeRejects() {
        DailyTransaction bad = TestDataFactory.transaction("TXN0000000000040", new BigDecimal("10.00"));
        bad.setCardNum("0000000000000000"); // not in XREF -> reject 100
        List<DailyTransaction> txns = List.of(
                TestDataFactory.transaction("TXN0000000000041", new BigDecimal("10.00")),
                bad,
                TestDataFactory.transaction("TXN0000000000042", new BigDecimal("30.00")));

        BatchResult result = service.processAllTransactions(txns);

        assertEquals(3, result.getTransactionCount());
        assertEquals(1, result.getRejectCount());
        assertEquals(4, result.getExitStatus());
    }
}
