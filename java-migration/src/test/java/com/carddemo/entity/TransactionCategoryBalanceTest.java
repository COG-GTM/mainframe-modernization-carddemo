package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionCategoryBalance} entity.
 * Validates entity structure mapped from COBOL TRAN-CAT-BAL-RECORD (CVTRA01Y).
 */
class TransactionCategoryBalanceTest {

    @Test
    @DisplayName("All-args constructor sets fields correctly")
    void testAllArgsConstructor() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        BigDecimal balance = new BigDecimal("1234.56");

        TransactionCategoryBalance entity = new TransactionCategoryBalance(id, balance);

        assertEquals(id, entity.getId());
        assertEquals(balance, entity.getBalance());
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void testNoArgConstructor() {
        TransactionCategoryBalance entity = new TransactionCategoryBalance();

        assertNull(entity.getId());
        assertNull(entity.getBalance());
    }

    @Test
    @DisplayName("Balance supports COBOL PIC S9(09)V99 max positive value")
    void testMaxPositiveBalance() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        BigDecimal maxBalance = new BigDecimal("999999999.99");

        TransactionCategoryBalance entity = new TransactionCategoryBalance(id, maxBalance);

        assertEquals(maxBalance, entity.getBalance());
    }

    @Test
    @DisplayName("Balance supports negative values (signed field)")
    void testNegativeBalance() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        BigDecimal negativeBalance = new BigDecimal("-500.25");

        TransactionCategoryBalance entity = new TransactionCategoryBalance(id, negativeBalance);

        assertEquals(negativeBalance, entity.getBalance());
    }
}
