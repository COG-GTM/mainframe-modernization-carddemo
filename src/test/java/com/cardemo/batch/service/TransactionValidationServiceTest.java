package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionValidationService.
 * Tests the 4 validation rules from CBTRN02C paragraphs 1500-A and 1500-B.
 */
@ExtendWith(MockitoExtension.class)
class TransactionValidationServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private DailyTransaction createDailyTransaction(String cardNum, BigDecimal amount, String origTs) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TRAN0000000001");
        dt.setCardNum(cardNum);
        dt.setAmount(amount);
        dt.setOrigTimestamp(origTs);
        dt.setTypeCd("SA");
        dt.setCatCd(5001);
        return dt;
    }

    private CardXref createCardXref(String cardNum, long acctId) {
        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);
        return xref;
    }

    private Account createAccount(long acctId, BigDecimal creditLimit,
                                   BigDecimal cycCredit, BigDecimal cycDebit,
                                   String expDate) {
        Account acct = new Account();
        acct.setAcctId(acctId);
        acct.setCreditLimit(creditLimit);
        acct.setCurrentCycleCredit(cycCredit);
        acct.setCurrentCycleDebit(cycDebit);
        acct.setExpirationDate(expDate);
        acct.setCurrentBalance(BigDecimal.ZERO);
        return acct;
    }

    // --- Rule 100: INVALID CARD NUMBER FOUND ---

    @Test
    @DisplayName("Rule 100: Card not in XREF returns code 100 with exact message")
    void validate_cardNotFound_returnsCode100() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", BigDecimal.TEN, "2025-01-15-10.30.00.000000");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Rule 100: Verify exact error message character-for-character")
    void validate_cardNotFound_exactMessage() {
        DailyTransaction dt = createDailyTransaction("0000000000000000", BigDecimal.ONE, "2025-06-01-12.00.00.000000");
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
    }

    // --- Rule 101: ACCOUNT RECORD NOT FOUND ---

    @Test
    @DisplayName("Rule 101: Account not found returns code 101 with exact message")
    void validate_accountNotFound_returnsCode101() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", BigDecimal.TEN, "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(101, result.getFailReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Rule 101: Verify exact error message character-for-character")
    void validate_accountNotFound_exactMessage() {
        DailyTransaction dt = createDailyTransaction("9999999999999999", BigDecimal.ONE, "2025-06-01-12.00.00.000000");
        CardXref xref = createCardXref("9999999999999999", 99999999999L);
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
    }

    // --- Rule 102: OVERLIMIT TRANSACTION ---

    @Test
    @DisplayName("Rule 102: Overlimit transaction returns code 102 with exact message")
    void validate_overlimit_returnsCode102() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("500.00"), "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        // creditLimit=1000, cycCredit=800, cycDebit=100 -> tempBal = 800 - 100 + 500 = 1200 > 1000
        Account acct = createAccount(12345678901L, new BigDecimal("1000.00"),
                new BigDecimal("800.00"), new BigDecimal("100.00"), "2030-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Rule 102: Transaction exactly at limit should pass (creditLimit >= tempBal)")
    void validate_exactlyAtLimit_passes() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("300.00"), "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        // creditLimit=1000, cycCredit=800, cycDebit=100 -> tempBal = 800 - 100 + 300 = 1000 == 1000
        Account acct = createAccount(12345678901L, new BigDecimal("1000.00"),
                new BigDecimal("800.00"), new BigDecimal("100.00"), "2030-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Rule 102: Verify exact error message character-for-character")
    void validate_overlimit_exactMessage() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("5000.00"), "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        Account acct = createAccount(12345678901L, new BigDecimal("1000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2030-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
    }

    // --- Rule 103: TRANSACTION RECEIVED AFTER ACCT EXPIRATION ---

    @Test
    @DisplayName("Rule 103: Expired account returns code 103 with exact message")
    void validate_expired_returnsCode103() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("50.00"), "2025-06-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        // expDate 2025-01-01 < tranDate 2025-06-15
        Account acct = createAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2025-01-01");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Rule 103: Account expiration same as transaction date should pass")
    void validate_expirationSameAsTransDate_passes() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("50.00"), "2025-06-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        Account acct = createAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2025-06-15");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Rule 103: Verify exact error message character-for-character")
    void validate_expired_exactMessage() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("50.00"), "2026-01-01-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        Account acct = createAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2020-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailReasonDescription());
    }

    // --- Happy path ---

    @Test
    @DisplayName("Valid transaction passes all 4 validation rules")
    void validate_allRulesPass_returnsSuccess() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("100.00"), "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        Account acct = createAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("500.00"), new BigDecimal("200.00"), "2030-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
        assertEquals(0, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Negative transaction amount (debit) passes validation when within limit")
    void validate_negativeAmount_passesWhenWithinLimit() {
        DailyTransaction dt = createDailyTransaction("1234567890123456", new BigDecimal("-50.00"), "2025-01-15-10.30.00.000000");
        CardXref xref = createCardXref("1234567890123456", 12345678901L);
        Account acct = createAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("500.00"), new BigDecimal("200.00"), "2030-12-31");
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(12345678901L)).thenReturn(Optional.of(acct));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }
}
