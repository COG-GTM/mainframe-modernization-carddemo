package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.service.TransactionValidationService;
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
 * Integration tests for batch validation rules using real Spring context and H2 database.
 * Tests all 4 validation codes (100-103) with actual database lookups.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Validation Rules Integration Tests")
class ValidationRulesIntegrationTest {

    @Autowired
    private TransactionValidationService validationService;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private AccountRepository accountRepository;

    private Account validAccount;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();

        validAccount = new Account();
        validAccount.setAcctId(10001L);
        validAccount.setActiveStatus("Y");
        validAccount.setCurrentBalance(new BigDecimal("1000.00"));
        validAccount.setCreditLimit(new BigDecimal("5000.00"));
        validAccount.setCurrentCycleCredit(new BigDecimal("200.00"));
        validAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        validAccount.setExpirationDate("2025-12-31");
        validAccount.setGroupId("STANDARD");
        accountRepository.save(validAccount);

        CardXref xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setAcctId(10001L);
        cardXrefRepository.save(xref);
    }

    private DailyTransaction createTxn(String cardNum, BigDecimal amount, String timestamp) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("ITXN001");
        dt.setCardNum(cardNum);
        dt.setAmount(amount);
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp(timestamp);
        return dt;
    }

    @Test
    @DisplayName("IT: Valid transaction passes all validation rules")
    void validTransaction_passesAll() {
        DailyTransaction dt = createTxn("4111111111111111",
                new BigDecimal("100.00"), "2024-06-15-10.30.00.000000");
        ValidationResult result = validationService.validate(dt);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("IT: Code 100 - Card not found in database")
    void code100_cardNotInDb() {
        DailyTransaction dt = createTxn("9999999999999999",
                new BigDecimal("100.00"), "2024-06-15-10.30.00.000000");
        ValidationResult result = validationService.validate(dt);
        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("IT: Code 101 - Account not in database for valid card")
    void code101_accountNotInDb() {
        CardXref orphanXref = new CardXref();
        orphanXref.setCardNum("5555555555555555");
        orphanXref.setAcctId(99999L);
        cardXrefRepository.save(orphanXref);

        DailyTransaction dt = createTxn("5555555555555555",
                new BigDecimal("100.00"), "2024-06-15-10.30.00.000000");
        ValidationResult result = validationService.validate(dt);
        assertFalse(result.isValid());
        assertEquals(101, result.getFailReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("IT: Code 102 - Overlimit transaction with real data")
    void code102_overlimitWithRealData() {
        DailyTransaction dt = createTxn("4111111111111111",
                new BigDecimal("5000.00"), "2024-06-15-10.30.00.000000");
        // tempBal = 200 - 0 + 5000 = 5200 > 5000 -> REJECT
        ValidationResult result = validationService.validate(dt);
        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("IT: Code 102 - Exactly at limit passes")
    void code102_exactlyAtLimit_passes() {
        DailyTransaction dt = createTxn("4111111111111111",
                new BigDecimal("4800.00"), "2024-06-15-10.30.00.000000");
        // tempBal = 200 - 0 + 4800 = 5000, limit=5000, 5000 < 5000 is FALSE -> PASS
        ValidationResult result = validationService.validate(dt);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("IT: Code 103 - Expired account in database")
    void code103_expiredAccount() {
        Account expired = new Account();
        expired.setAcctId(20001L);
        expired.setActiveStatus("Y");
        expired.setCurrentBalance(new BigDecimal("500.00"));
        expired.setCreditLimit(new BigDecimal("10000.00"));
        expired.setCurrentCycleCredit(BigDecimal.ZERO);
        expired.setCurrentCycleDebit(BigDecimal.ZERO);
        expired.setExpirationDate("2023-01-01");
        expired.setGroupId("STANDARD");
        accountRepository.save(expired);

        CardXref xref = new CardXref();
        xref.setCardNum("4222222222222222");
        xref.setAcctId(20001L);
        cardXrefRepository.save(xref);

        DailyTransaction dt = createTxn("4222222222222222",
                new BigDecimal("10.00"), "2024-06-15-10.30.00.000000");
        ValidationResult result = validationService.validate(dt);
        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                result.getFailReasonDescription());
    }

    @Test
    @DisplayName("IT: Validation chain order with real database")
    void chainOrder_code100BeforeCode101() {
        DailyTransaction dt = createTxn("NONEXISTENT_CARD",
                new BigDecimal("100.00"), "2024-06-15-10.30.00.000000");
        ValidationResult result = validationService.validate(dt);
        assertEquals(100, result.getFailReasonCode());
    }

    @Test
    @DisplayName("IT: Counter semantics with real validation - batch orchestration level")
    void counterSemantics_withRealDb() {
        int transactionCount = 0;
        int rejectCount = 0;

        // Valid transaction
        DailyTransaction dt1 = createTxn("4111111111111111",
                new BigDecimal("10.00"), "2024-06-15-10.30.00.000000");
        transactionCount++;
        ValidationResult r1 = validationService.validate(dt1);
        if (!r1.isValid()) rejectCount++;

        // Invalid transaction
        DailyTransaction dt2 = createTxn("INVALID_CARD",
                new BigDecimal("10.00"), "2024-06-15-10.30.00.000000");
        transactionCount++;
        ValidationResult r2 = validationService.validate(dt2);
        if (!r2.isValid()) rejectCount++;

        assertEquals(2, transactionCount);
        assertEquals(1, rejectCount);
        int returnCode = rejectCount > 0 ? 4 : 0;
        assertEquals(4, returnCode);
    }
}
