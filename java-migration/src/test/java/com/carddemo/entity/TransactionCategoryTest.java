package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionCategory} entity.
 */
class TransactionCategoryTest {

    @Test
    @DisplayName("All-args constructor populates all fields")
    void testAllArgsConstructor() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        TransactionCategory category = new TransactionCategory(id, "Regular Sales Draft");

        assertNotNull(category.getId());
        assertEquals("01", category.getId().getTypeCode());
        assertEquals(1, category.getId().getCategoryCode());
        assertEquals("Regular Sales Draft", category.getDescription());
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void testNoArgConstructor() {
        TransactionCategory category = new TransactionCategory();
        assertNull(category.getId());
        assertNull(category.getDescription());
    }

    @Test
    @DisplayName("Setter updates description")
    void testSetDescription() {
        TransactionCategory category = new TransactionCategory();
        category.setDescription("Cash payment");
        assertEquals("Cash payment", category.getDescription());
    }
}
