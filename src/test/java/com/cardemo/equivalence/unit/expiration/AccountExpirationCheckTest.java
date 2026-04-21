package com.cardemo.equivalence.unit.expiration;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.service.TransactionValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for account expiration check.
 * Business rule 9: Compare ACCT-EXPIRAION-DATE (typo preserved from COBOL)
 * with transaction timestamp.
 * COBOL: IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10) -> OK
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Account Expiration Check Tests")
class AccountExpirationCheckTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private void setupAccount(String cardNum, long acctId, String expirationDate) {
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

    private DailyTransaction createTxn(String cardNum, String timestamp) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("EXP_TXN");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("10.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp(timestamp);
        return dt;
    }

    @Nested
    @DisplayName("Date Comparison Semantics")
    class DateComparison {

        @Test
        @DisplayName("Expiration after transaction date: passes")
        void expirationAfter_passes() {
            setupAccount("C1", 1L, "2025-12-31");
            ValidationResult result = validationService.validate(
                    createTxn("C1", "2024-06-15-10.30.00.000000"));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Expiration equals transaction date: passes (COBOL uses >=)")
        void expirationEquals_passes() {
            setupAccount("C2", 2L, "2024-06-15");
            ValidationResult result = validationService.validate(
                    createTxn("C2", "2024-06-15-10.30.00.000000"));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Expiration before transaction date: fails with code 103")
        void expirationBefore_fails() {
            setupAccount("C3", 3L, "2024-06-14");
            ValidationResult result = validationService.validate(
                    createTxn("C3", "2024-06-15-10.30.00.000000"));
            assertFalse(result.isValid());
            assertEquals(103, result.getFailReasonCode());
        }

        @Test
        @DisplayName("Expiration one day before: fails")
        void expirationOneDayBefore_fails() {
            setupAccount("C4", 4L, "2024-01-14");
            ValidationResult result = validationService.validate(
                    createTxn("C4", "2024-01-15-00.00.00.000000"));
            assertFalse(result.isValid());
            assertEquals(103, result.getFailReasonCode());
        }

        @Test
        @DisplayName("Expiration one day after: passes")
        void expirationOneDayAfter_passes() {
            setupAccount("C5", 5L, "2024-01-16");
            ValidationResult result = validationService.validate(
                    createTxn("C5", "2024-01-15-23.59.59.999999"));
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Year Boundary Cases")
    class YearBoundary {

        @Test
        @DisplayName("Expired in previous year")
        void expiredPreviousYear() {
            setupAccount("YB1", 10L, "2023-12-31");
            ValidationResult result = validationService.validate(
                    createTxn("YB1", "2024-01-01-00.00.00.000000"));
            assertFalse(result.isValid());
            assertEquals(103, result.getFailReasonCode());
        }

        @Test
        @DisplayName("Same year end: passes")
        void sameYearEnd_passes() {
            setupAccount("YB2", 11L, "2024-12-31");
            ValidationResult result = validationService.validate(
                    createTxn("YB2", "2024-12-31-23.59.59.999999"));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Far future expiration: passes")
        void farFuture_passes() {
            setupAccount("YB3", 12L, "2099-12-31");
            ValidationResult result = validationService.validate(
                    createTxn("YB3", "2024-06-15-10.30.00.000000"));
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("String Comparison (COBOL Semantics)")
    class StringComparison {

        @Test
        @DisplayName("Uses first 10 characters of timestamp for comparison")
        void usesFirst10Chars() {
            // expDate "2024-06-15" >= "2024-06-15" (first 10 of timestamp) -> PASS
            setupAccount("SC1", 20L, "2024-06-15");
            ValidationResult result = validationService.validate(
                    createTxn("SC1", "2024-06-15-23.59.59.999999"));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Comparison is lexicographic (string-based like COBOL)")
        void lexicographicComparison() {
            // "2024-02-01" >= "2024-01-31" -> true (lexicographic)
            setupAccount("SC2", 21L, "2024-02-01");
            ValidationResult result = validationService.validate(
                    createTxn("SC2", "2024-01-31-10.30.00.000000"));
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Null expiration date: passes (no expiration)")
        void nullExpiration_passes() {
            CardXref xref = new CardXref();
            xref.setCardNum("NULL_EXP");
            xref.setAcctId(30L);
            Account account = new Account();
            account.setAcctId(30L);
            account.setCreditLimit(new BigDecimal("99999.99"));
            account.setCurrentCycleCredit(BigDecimal.ZERO);
            account.setCurrentCycleDebit(BigDecimal.ZERO);
            account.setExpirationDate(null);
            when(cardXrefRepository.findById("NULL_EXP")).thenReturn(Optional.of(xref));
            when(accountRepository.findById(30L)).thenReturn(Optional.of(account));

            ValidationResult result = validationService.validate(
                    createTxn("NULL_EXP", "2024-01-15-10.30.00.000000"));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Null timestamp: passes")
        void nullTimestamp_passes() {
            setupAccount("NULL_TS", 31L, "2024-12-31");
            ValidationResult result = validationService.validate(
                    createTxn("NULL_TS", null));
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Expired long ago: fails")
        void expiredLongAgo_fails() {
            setupAccount("OLD", 32L, "2000-01-01");
            ValidationResult result = validationService.validate(
                    createTxn("OLD", "2024-06-15-10.30.00.000000"));
            assertFalse(result.isValid());
            assertEquals(103, result.getFailReasonCode());
            assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                    result.getFailReasonDescription());
        }
    }
}
