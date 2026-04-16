package com.cardemo.equivalence.unit.validation;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for validation rule Code 102: OVERLIMIT TRANSACTION.
 * COBOL formula: COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
 * IF ACCT-CREDIT-LIMIT < WS-TEMP-BAL -> reject (note: strict less-than)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Validation Rule - Code 102: Overlimit Transaction")
class BatchValidationCode102Test {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private void setupMocks(String cardNum, long acctId, BigDecimal creditLimit,
                             BigDecimal cycCredit, BigDecimal cycDebit) {
        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);

        Account account = new Account();
        account.setAcctId(acctId);
        account.setCreditLimit(creditLimit);
        account.setCurrentCycleCredit(cycCredit);
        account.setCurrentCycleDebit(cycDebit);
        account.setExpirationDate("2025-12-31");

        when(cardXrefRepository.findById(cardNum)).thenReturn(Optional.of(xref));
        when(accountRepository.findById(acctId)).thenReturn(Optional.of(account));
    }

    private DailyTransaction createTransaction(String cardNum, BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN102");
        dt.setCardNum(cardNum);
        dt.setAmount(amount);
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");
        return dt;
    }

    @Test
    @DisplayName("Should reject with code 102 when transaction causes overlimit")
    void validate_overlimit_returnsCode102() {
        setupMocks("CARD102", 100L, new BigDecimal("1000.00"),
                new BigDecimal("900.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD102", new BigDecimal("200.00"));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should match COBOL error message character-for-character: 'OVERLIMIT TRANSACTION'")
    void validate_overlimit_exactErrorMessage() {
        setupMocks("CARD102B", 101L, new BigDecimal("500.00"),
                new BigDecimal("500.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD102B", new BigDecimal("1.00"));

        ValidationResult result = validationService.validate(dt);

        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Should pass when transaction exactly at credit limit (COBOL uses < not <=)")
    void validate_exactlyAtLimit_passes() {
        // creditLimit=1000, cycCredit=500, cycDebit=0, amount=500
        // tempBal = 500 - 0 + 500 = 1000, creditLimit(1000) < tempBal(1000) is FALSE -> PASS
        setupMocks("CARD_AT_LIMIT", 102L, new BigDecimal("1000.00"),
                new BigDecimal("500.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_AT_LIMIT", new BigDecimal("500.00"));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject when one cent over limit")
    void validate_oneCentOverLimit_rejects() {
        // creditLimit=1000, cycCredit=500, cycDebit=0, amount=500.01
        // tempBal = 500 - 0 + 500.01 = 1000.01, creditLimit(1000) < tempBal(1000.01) is TRUE -> REJECT
        setupMocks("CARD_OVER_1C", 103L, new BigDecimal("1000.00"),
                new BigDecimal("500.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_OVER_1C", new BigDecimal("500.01"));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should correctly factor in cycle debit when computing temp balance")
    void validate_withCycleDebit_computesCorrectly() {
        // creditLimit=1000, cycCredit=800, cycDebit=300, amount=600
        // tempBal = 800 - 300 + 600 = 1100 > 1000 -> REJECT
        setupMocks("CARD_DEBIT", 104L, new BigDecimal("1000.00"),
                new BigDecimal("800.00"), new BigDecimal("300.00"));
        DailyTransaction dt = createTransaction("CARD_DEBIT", new BigDecimal("600.00"));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should pass when debit reduces temp balance below limit")
    void validate_debitReducesTempBal_passes() {
        // creditLimit=1000, cycCredit=800, cycDebit=300, amount=400
        // tempBal = 800 - 300 + 400 = 900 < 1000 -> PASS
        setupMocks("CARD_PASS", 105L, new BigDecimal("1000.00"),
                new BigDecimal("800.00"), new BigDecimal("300.00"));
        DailyTransaction dt = createTransaction("CARD_PASS", new BigDecimal("400.00"));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should handle negative transaction amounts (refunds) correctly")
    void validate_negativeAmount_reducesBalance() {
        // creditLimit=1000, cycCredit=900, cycDebit=0, amount=-100
        // tempBal = 900 - 0 + (-100) = 800 < 1000 -> PASS
        setupMocks("CARD_REFUND", 106L, new BigDecimal("1000.00"),
                new BigDecimal("900.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_REFUND", new BigDecimal("-100.00"));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should handle zero credit limit")
    void validate_zeroCreditLimit_rejectsPositiveAmount() {
        setupMocks("CARD_ZERO", 107L, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_ZERO", new BigDecimal("0.01"));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should handle large amounts in overlimit calculation")
    void validate_largeAmounts_overlimit() {
        // creditLimit=99999999.99, cycCredit=99999999.00, cycDebit=0, amount=1.00
        // tempBal = 99999999.00 - 0 + 1.00 = 100000000.00 > 99999999.99 -> REJECT
        setupMocks("CARD_LARGE", 108L, new BigDecimal("99999999.99"),
                new BigDecimal("99999999.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_LARGE", new BigDecimal("1.00"));

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should pass zero amount transaction")
    void validate_zeroAmount_passes() {
        setupMocks("CARD_ZERO_AMT", 109L, new BigDecimal("1000.00"),
                new BigDecimal("1000.00"), BigDecimal.ZERO);
        DailyTransaction dt = createTransaction("CARD_ZERO_AMT", BigDecimal.ZERO);

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }
}
