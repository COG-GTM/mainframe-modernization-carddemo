package com.carddemo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.service.TransactionValidationService.ValidationResult;

/**
 * Unit tests for TransactionValidationService.
 *
 * Verifies all four validation rules from CBTRN02C:
 *   100 — Invalid card number (XREF not found)
 *   101 — Account not found
 *   102 — Overlimit transaction
 *   103 — Expired account
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

    private TransactionPendingEvent createEvent(String cardNumber, BigDecimal amount,
                                                 String originTimestamp) {
        return new TransactionPendingEvent(
                "TXN-001", "SA", 5001, "ONLINE", "Test purchase",
                amount, 123456789L, "Test Merchant", "Test City", "12345",
                cardNumber, originTimestamp, null
        );
    }

    @Test
    @DisplayName("Code 100: Reject when card number not found in XREF")
    void shouldRejectInvalidCardNumber() {
        // Replaces: 1500-A-LOOKUP-XREF — READ XREF-FILE returns NOTFND
        TransactionPendingEvent event = createEvent("9999999999999999",
                new BigDecimal("100.00"), "2025-01-15-10.30.00.000000");

        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(event);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailureReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getFailureDescription());
    }

    @Test
    @DisplayName("Code 101: Reject when account not found for card")
    void shouldRejectAccountNotFound() {
        // Replaces: 1500-B-LOOKUP-ACCT — READ ACCOUNT-FILE returns NOTFND
        TransactionPendingEvent event = createEvent("4111111111111111",
                new BigDecimal("100.00"), "2025-01-15-10.30.00.000000");

        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(5001L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(event);

        assertFalse(result.isValid());
        assertEquals(101, result.getFailureReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailureDescription());
    }

    @Test
    @DisplayName("Code 102: Reject when transaction exceeds credit limit")
    void shouldRejectOverlimitTransaction() {
        // Replaces: credit limit check in 1500-B-LOOKUP-ACCT
        TransactionPendingEvent event = createEvent("4111111111111111",
                new BigDecimal("5000.00"), "2025-01-15-10.30.00.000000");

        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        Account account = new Account(5001L);
        // Set credit limit to 1000, cycle credit to 0, cycle debit to 0
        // Projected balance: 0 - 0 + 5000 = 5000 > 1000 limit
        setAccountFields(account, new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, "2030-12-31");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(5001L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(event);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailureReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getFailureDescription());
    }

    @Test
    @DisplayName("Code 103: Reject when account is expired")
    void shouldRejectExpiredAccount() {
        // Replaces: expiration date check in 1500-B-LOOKUP-ACCT
        TransactionPendingEvent event = createEvent("4111111111111111",
                new BigDecimal("50.00"), "2025-06-15-10.30.00.000000");

        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        Account account = new Account(5001L);
        // Expired before transaction date
        setAccountFields(account, new BigDecimal("10000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, "2025-01-01");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(5001L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(event);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailureReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailureDescription());
    }

    @Test
    @DisplayName("Code 103 wins when both overlimit AND expired (COBOL fall-through semantics)")
    void shouldReturnCode103WhenBothOverlimitAndExpired() {
        // COBOL CBTRN02C lines 407-420: both checks execute, last failure (103) wins
        TransactionPendingEvent event = createEvent("4111111111111111",
                new BigDecimal("5000.00"), "2025-06-15-10.30.00.000000");

        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        Account account = new Account(5001L);
        // Credit limit 1000, projected balance 5000 → overlimit (102)
        // Expiration 2025-01-01 < txn date 2025-06-15 → expired (103)
        setAccountFields(account, new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, "2025-01-01");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(5001L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(event);

        assertFalse(result.isValid());
        // Last failure wins, matching COBOL fall-through
        assertEquals(103, result.getFailureReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailureDescription());
    }

    @Test
    @DisplayName("Valid transaction passes all checks")
    void shouldAcceptValidTransaction() {
        TransactionPendingEvent event = createEvent("4111111111111111",
                new BigDecimal("50.00"), "2025-01-15-10.30.00.000000");

        CardXref xref = new CardXref("4111111111111111", 1001L, 5001L);
        Account account = new Account(5001L);
        setAccountFields(account, new BigDecimal("10000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, "2030-12-31");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(5001L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(event);

        assertTrue(result.isValid());
        assertNotNull(result.getXref());
        assertNotNull(result.getAccount());
        assertEquals(5001L, result.getXref().getAcctId());
    }

    /**
     * Helper to set account fields via setters for test scenarios.
     */
    private void setAccountFields(Account account, BigDecimal creditLimit,
                                   BigDecimal cycleCredit, BigDecimal cycleDebit,
                                   String expirationDate) {
        // Use reflection-free approach — we need setters for test setup
        // Account entity has setters for mutable fields
        account.setCurrentCycleCredit(cycleCredit);
        account.setCurrentCycleDebit(cycleDebit);
        // For creditLimit and expirationDate, we need to use a test-friendly approach
        // Since Account only has setters for balance fields, we use a test subclass
        // or add package-private setters. For skeleton code, we'll use reflection.
        try {
            var creditLimitField = Account.class.getDeclaredField("creditLimit");
            creditLimitField.setAccessible(true);
            creditLimitField.set(account, creditLimit);

            var expirationField = Account.class.getDeclaredField("expirationDate");
            expirationField.setAccessible(true);
            expirationField.set(account, expirationDate);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set test fields", e);
        }
    }
}
