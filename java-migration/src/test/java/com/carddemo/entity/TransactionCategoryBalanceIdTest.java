package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionCategoryBalanceId}.
 * Validates composite key behavior mapped from COBOL TRAN-CAT-KEY (CVTRA01Y).
 */
class TransactionCategoryBalanceIdTest {

    @Test
    @DisplayName("Equals and hashCode: identical keys are equal")
    void testEqualsIdenticalKeys() {
        TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "01", 1);
        TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(1L, "01", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("Equals: different account IDs are not equal")
    void testEqualsDifferentAccountId() {
        TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "01", 1);
        TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(2L, "01", 1);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Equals: different type codes are not equal")
    void testEqualsDifferentTypeCode() {
        TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "01", 1);
        TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(1L, "02", 1);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Equals: different category codes are not equal")
    void testEqualsDifferentCategoryCode() {
        TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "01", 1);
        TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(1L, "01", 2);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void testNoArgConstructor() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId();

        assertNull(id.getAccountId());
        assertNull(id.getTypeCode());
        assertNull(id.getCategoryCode());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void testSetters() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId();
        id.setAccountId(42L);
        id.setTypeCode("AB");
        id.setCategoryCode(9999);

        assertEquals(42L, id.getAccountId());
        assertEquals("AB", id.getTypeCode());
        assertEquals(9999, id.getCategoryCode());
    }
}
