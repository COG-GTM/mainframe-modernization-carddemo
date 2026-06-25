package com.carddemo.etl.validation;

import com.carddemo.etl.model.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountValidatorTest {

    private final AccountValidator validator = new AccountValidator();

    private Account account(String status, String creditLimit, String cashLimit) {
        return new Account(
                1L, status,
                new BigDecimal("100.00"),
                new BigDecimal(creditLimit),
                new BigDecimal(cashLimit),
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2024, 1, 1),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "12345", "GRP1");
    }

    @Test
    void acceptsValidAccount() {
        ValidationResult result = validator.validate(account("Y", "5000.00", "1000.00"));
        assertThat(result.isValid()).isTrue();
        assertThat(result.violations()).isEmpty();
    }

    @Test
    void rejectsInvalidStatus() {
        ValidationResult result = validator.validate(account("X", "5000.00", "1000.00"));
        assertThat(result.isValid()).isFalse();
        assertThat(result.summary()).contains("ACCT-ACTIVE-STATUS");
    }

    @Test
    void rejectsCashLimitExceedingCreditLimit() {
        ValidationResult result = validator.validate(account("N", "1000.00", "5000.00"));
        assertThat(result.isValid()).isFalse();
        assertThat(result.summary()).contains("ACCT-CREDIT-LIMIT");
    }

    @Test
    void rejectsNegativeCreditLimit() {
        ValidationResult result = validator.validate(account("Y", "-1.00", "-2.00"));
        assertThat(result.isValid()).isFalse();
        assertThat(result.summary()).contains("must not be negative");
    }

    @Test
    void rejectsNonPositiveAccountId() {
        Account account = new Account(0L, "Y",
                new BigDecimal("0.00"), new BigDecimal("100.00"), new BigDecimal("10.00"),
                null, null, null,
                new BigDecimal("0.00"), new BigDecimal("0.00"), null, null);
        ValidationResult result = validator.validate(account);
        assertThat(result.isValid()).isFalse();
        assertThat(result.summary()).contains("ACCT-ID");
    }

    @Test
    void rejectsExpirationBeforeOpen() {
        Account account = new Account(1L, "Y",
                new BigDecimal("0.00"), new BigDecimal("100.00"), new BigDecimal("10.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2020, 1, 1), null,
                new BigDecimal("0.00"), new BigDecimal("0.00"), null, null);
        ValidationResult result = validator.validate(account);
        assertThat(result.isValid()).isFalse();
        assertThat(result.summary()).contains("must not precede");
    }
}
