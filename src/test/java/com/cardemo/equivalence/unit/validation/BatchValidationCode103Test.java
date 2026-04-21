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
 * Tests for validation rule Code 103: TRANSACTION RECEIVED AFTER ACCT EXPIRATION.
 * COBOL: IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10) -> OK
 * Note: typo "EXPIRAION" preserved from COBOL source.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Validation Rule - Code 103: Account Expiration")
class BatchValidationCode103Test {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private void setupMocks(String cardNum, long acctId, String expirationDate) {
        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);

        Account account = new Account();
        account.setAcctId(acctId);
        account.setCreditLimit(new BigDecimal("99999.99"));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate(expirationDate);

        when(cardXrefRepository.findById(cardNum)).thenReturn(Optional.of(xref));
        when(accountRepository.findById(acctId)).thenReturn(Optional.of(account));
    }

    private DailyTransaction createTransaction(String cardNum, String origTimestamp) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN103");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("10.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp(origTimestamp);
        return dt;
    }

    @Test
    @DisplayName("Should reject with code 103 when account expired before transaction")
    void validate_expired_returnsCode103() {
        setupMocks("CARD103", 200L, "2023-06-30");
        DailyTransaction dt = createTransaction("CARD103", "2024-01-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should match COBOL error message: 'TRANSACTION RECEIVED AFTER ACCT EXPIRATION'")
    void validate_expired_exactErrorMessage() {
        setupMocks("CARD103B", 201L, "2020-01-01");
        DailyTransaction dt = createTransaction("CARD103B", "2024-06-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Should pass when expiration date equals transaction date")
    void validate_sameDate_passes() {
        setupMocks("CARD_SAME", 202L, "2024-01-15");
        DailyTransaction dt = createTransaction("CARD_SAME", "2024-01-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should pass when expiration date is after transaction date")
    void validate_notExpired_passes() {
        setupMocks("CARD_FUTURE", 203L, "2025-12-31");
        DailyTransaction dt = createTransaction("CARD_FUTURE", "2024-01-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject when expired by one day")
    void validate_expiredByOneDay_rejects() {
        setupMocks("CARD_1DAY", 204L, "2024-01-14");
        DailyTransaction dt = createTransaction("CARD_1DAY", "2024-01-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should compare using string comparison (COBOL semantics)")
    void validate_stringComparison_yearBoundary() {
        setupMocks("CARD_YEAR", 205L, "2023-12-31");
        DailyTransaction dt = createTransaction("CARD_YEAR", "2024-01-01-00.00.00.000000");

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should handle null expiration date gracefully")
    void validate_nullExpDate_passes() {
        CardXref xref = new CardXref();
        xref.setCardNum("CARD_NULL_EXP");
        xref.setAcctId(206L);

        Account account = new Account();
        account.setAcctId(206L);
        account.setCreditLimit(new BigDecimal("99999.99"));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate(null);

        when(cardXrefRepository.findById("CARD_NULL_EXP")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(206L)).thenReturn(Optional.of(account));

        DailyTransaction dt = createTransaction("CARD_NULL_EXP", "2024-01-15-10.30.00.000000");

        ValidationResult result = validationService.validate(dt);

        // Null expiration date should not trigger code 103
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should extract first 10 chars from timestamp for date comparison")
    void validate_usesFirst10CharsOfTimestamp() {
        // expDate=2024-01-15, timestamp first 10 chars = "2024-01-15" -> equal -> PASS
        setupMocks("CARD_SUBSTR", 207L, "2024-01-15");
        DailyTransaction dt = createTransaction("CARD_SUBSTR", "2024-01-15-23.59.59.999999");

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should handle short timestamp gracefully")
    void validate_shortTimestamp_passes() {
        setupMocks("CARD_SHORT", 208L, "2024-01-15");
        DailyTransaction dt = createTransaction("CARD_SHORT", "2024-01");

        ValidationResult result = validationService.validate(dt);

        // Short timestamp -> tranDatePortion is empty -> expDate >= "" is true -> PASS
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should handle null timestamp gracefully")
    void validate_nullTimestamp_passes() {
        setupMocks("CARD_NULL_TS", 209L, "2024-01-15");
        DailyTransaction dt = createTransaction("CARD_NULL_TS", null);

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }
}
