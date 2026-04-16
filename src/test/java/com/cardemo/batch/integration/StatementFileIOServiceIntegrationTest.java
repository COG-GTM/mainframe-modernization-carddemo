package com.cardemo.batch.integration;

import com.cardemo.batch.TestDataFactory;
import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.CardXref;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.CustomerRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.StatementFileIOService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for StatementFileIOService.
 * Validates that repository method invocations correctly replace
 * CBSTM03B CALL linkage file I/O operations.
 */
@SpringBootTest
@ActiveProfiles("test")
class StatementFileIOServiceIntegrationTest {

    @Autowired
    private StatementFileIOService fileIOService;

    @Autowired
    private TransactionRecordRepository transactionRepo;

    @Autowired
    private CardXrefRepository cardXrefRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AccountRepository accountRepo;

    @BeforeEach
    void setUp() {
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        customerRepo.deleteAll();
        accountRepo.deleteAll();

        // Set up test data
        customerRepo.save(TestDataFactory.createCustomer("000000001"));
        customerRepo.save(TestDataFactory.createCustomer("000000002"));

        accountRepo.save(TestDataFactory.createAccount("00000000001"));
        accountRepo.save(TestDataFactory.createAccount("00000000002"));

        cardXrefRepo.save(TestDataFactory.createCardXref(
                "4111111111111111", "000000001", "00000000001"));
        cardXrefRepo.save(TestDataFactory.createCardXref(
                "4222222222222222", "000000002", "00000000002"));

        transactionRepo.save(TestDataFactory.createTransaction(
                "4111111111111111", "0000000000000001",
                "Grocery Purchase", new BigDecimal("45.67")));
        transactionRepo.save(TestDataFactory.createTransaction(
                "4111111111111111", "0000000000000002",
                "Gas Station", new BigDecimal("32.10")));
        transactionRepo.save(TestDataFactory.createTransaction(
                "4222222222222222", "0000000000000010",
                "Online Shopping", new BigDecimal("199.99")));
    }

    @Test
    void readAllXrefs_returnsOrderedByCardNum() {
        List<CardXref> xrefs = fileIOService.readAllXrefs();

        assertEquals(2, xrefs.size());
        assertTrue(xrefs.get(0).getCardNum().compareTo(xrefs.get(1).getCardNum()) < 0);
    }

    @Test
    void readCustomerByKey_returnsCorrectCustomer() {
        Optional<Customer> customer = fileIOService.readCustomerByKey("000000001");

        assertTrue(customer.isPresent());
        assertEquals("John", customer.get().getFirstName());
    }

    @Test
    void readAccountByKey_returnsCorrectAccount() {
        Optional<Account> account = fileIOService.readAccountByKey("00000000001");

        assertTrue(account.isPresent());
        assertEquals(new BigDecimal("5000.50"), account.get().getCurrBal());
    }

    @Test
    void readTransactionsByCard_returnsOrderedBySortKey() {
        List<TransactionRecord> transactions =
                fileIOService.readTransactionsByCard("4111111111111111");

        assertEquals(2, transactions.size());
        // Verify composite key sort order (card_num + tran_id)
        assertTrue(transactions.get(0).getTranId()
                .compareTo(transactions.get(1).getTranId()) < 0);
    }

    @Test
    void readCustomerByKey_returnsEmptyForMissingKey() {
        Optional<Customer> customer = fileIOService.readCustomerByKey("999999999");
        assertFalse(customer.isPresent());
    }
}
