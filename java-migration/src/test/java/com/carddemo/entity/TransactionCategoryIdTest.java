package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionCategoryId} composite key.
 */
class TransactionCategoryIdTest {

    @Test
    @DisplayName("Equal IDs with same typeCode and categoryCode are equal")
    void testEqualIds() {
        TransactionCategoryId id1 = new TransactionCategoryId("01", 1);
        TransactionCategoryId id2 = new TransactionCategoryId("01", 1);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("IDs with different typeCode are not equal")
    void testDifferentTypeCode() {
        TransactionCategoryId id1 = new TransactionCategoryId("01", 1);
        TransactionCategoryId id2 = new TransactionCategoryId("02", 1);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("IDs with different categoryCode are not equal")
    void testDifferentCategoryCode() {
        TransactionCategoryId id1 = new TransactionCategoryId("01", 1);
        TransactionCategoryId id2 = new TransactionCategoryId("01", 2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void testNoArgConstructor() {
        TransactionCategoryId id = new TransactionCategoryId();
        assertNull(id.getTypeCode());
        assertNull(id.getCategoryCode());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void testSetters() {
        TransactionCategoryId id = new TransactionCategoryId();
        id.setTypeCode("03");
        id.setCategoryCode(5);
        assertEquals("03", id.getTypeCode());
        assertEquals(5, id.getCategoryCode());
    }
}
