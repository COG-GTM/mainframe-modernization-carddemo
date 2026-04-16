package com.carddemo.batch.service;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.RejectedTransaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransactionValidationService using @DataJpaTest with H2.
 * Validates the business rules from COBOL 1500-VALIDATE-TRAN.
 */
@DataJpaTest
@Import(TransactionValidationService.class)
class TransactionValidationServiceTest {

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionValidationService validationService;

    private static final String VALID_CARD = "4111111111111111";
    private static final long VALID_ACCOUNT_ID = 12345678901L;
    private static final long VALID_CUSTOMER_ID = 123456789L;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();

        CardXref xref = new CardXref();
        xref.setCardNumber(VALID_CARD);
        xref.setCustomerId(VALID_CUSTOMER_ID);
        xref.setAccountId(VALID_ACCOUNT_ID);
        cardXrefRepository.save(xref);

        Account account = new Account();
        account.setAccountId(VALID_ACCOUNT_ID);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("5000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        account.setReissueDate(LocalDate.of(2025, 1, 1));
        account.setCurrentCycleCredit(new BigDecimal("1000.00"));
        account.setCurrentCycleDebit(new BigDecimal("500.00"));
        account.setAddressZip("12345");
        account.setGroupId("GRP001");
        accountRepository.save(account);
    }

    private DailyTransaction createTransaction(String cardNumber, BigDecimal amount,
                                                LocalDateTime originTimestamp) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTransactionId("TXN0000000000001");
        txn.setTypeCode("SA");
        txn.setCategoryCode(5001);
        txn.setSource("ONLINE");
        txn.setDescription("Test transaction");
        txn.setAmount(amount);
        txn.setMerchantId(123456789L);
        txn.setMerchantName("Test Merchant");
        txn.setMerchantCity("Test City");
        txn.setMerchantZip("12345");
        txn.setCardNumber(cardNumber);
        txn.setOriginTimestamp(originTimestamp);
        return txn;
    }

    @Test
    void testValidTransaction() {
        DailyTransaction txn = createTransaction(VALID_CARD,
                new BigDecimal("100.00"),
                LocalDateTime.of(2025, 6, 15, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isEmpty(), "Valid transaction should not be rejected");
    }

    @Test
    void testInvalidCardNumber() {
        DailyTransaction txn = createTransaction("9999999999999999",
                new BigDecimal("100.00"),
                LocalDateTime.of(2025, 6, 15, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isPresent(), "Transaction with invalid card should be rejected");
        assertEquals(100, result.get().getReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.get().getReasonDescription());
    }

    @Test
    void testAccountNotFound() {
        // Create XREF pointing to non-existent account
        CardXref xref = new CardXref();
        xref.setCardNumber("5222222222222222");
        xref.setCustomerId(999999999L);
        xref.setAccountId(99999999999L);
        cardXrefRepository.save(xref);

        DailyTransaction txn = createTransaction("5222222222222222",
                new BigDecimal("100.00"),
                LocalDateTime.of(2025, 6, 15, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isPresent(), "Transaction with missing account should be rejected");
        assertEquals(101, result.get().getReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.get().getReasonDescription());
    }

    @Test
    void testOverlimitTransaction() {
        // tempBal = cycCredit(1000) - cycDebit(500) + amount(10000) = 10500
        // creditLimit = 10000 < 10500 => overlimit
        DailyTransaction txn = createTransaction(VALID_CARD,
                new BigDecimal("10000.00"),
                LocalDateTime.of(2025, 6, 15, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isPresent(), "Overlimit transaction should be rejected");
        assertEquals(102, result.get().getReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.get().getReasonDescription());
    }

    @Test
    void testExpiredAccount() {
        // expirationDate = 2030-12-31, txnDate = 2031-01-01 => expired
        DailyTransaction txn = createTransaction(VALID_CARD,
                new BigDecimal("100.00"),
                LocalDateTime.of(2031, 1, 1, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isPresent(), "Expired account transaction should be rejected");
        assertEquals(103, result.get().getReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                result.get().getReasonDescription());
    }

    @Test
    void testOverlimitAndExpired() {
        // Both overlimit AND expired: reason 103 should win (last-writer-wins)
        // tempBal = 1000 - 500 + 10000 = 10500 > 10000 => overlimit (102)
        // expirationDate = 2030-12-31 < 2031-01-01 => expired (103)
        // COBOL behavior: 103 overwrites 102
        DailyTransaction txn = createTransaction(VALID_CARD,
                new BigDecimal("10000.00"),
                LocalDateTime.of(2031, 1, 1, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isPresent(), "Both overlimit and expired should be rejected");
        assertEquals(103, result.get().getReasonCode(),
                "Last-writer-wins: reason 103 should overwrite 102");
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                result.get().getReasonDescription());
    }

    @Test
    void testExactCreditLimit() {
        // tempBal = cycCredit(1000) - cycDebit(500) + amount(9500) = 10000
        // creditLimit = 10000 >= 10000 => should PASS (COBOL uses >=)
        DailyTransaction txn = createTransaction(VALID_CARD,
                new BigDecimal("9500.00"),
                LocalDateTime.of(2025, 6, 15, 10, 30, 0));

        Optional<RejectedTransaction> result = validationService.validate(txn);

        assertTrue(result.isEmpty(),
                "Transaction at exact credit limit should pass (COBOL >= comparison)");
    }
}
