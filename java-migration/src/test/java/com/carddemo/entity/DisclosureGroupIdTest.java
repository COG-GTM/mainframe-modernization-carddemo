package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DisclosureGroupId} composite key.
 */
class DisclosureGroupIdTest {

    @Test
    @DisplayName("Equal IDs with same fields should be equal")
    void equalIds() {
        DisclosureGroupId id1 = new DisclosureGroupId("A000000000", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("A000000000", "01", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("IDs with different accountGroupId should not be equal")
    void differentAccountGroupId() {
        DisclosureGroupId id1 = new DisclosureGroupId("A000000000", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("DEFAULT", "01", 1);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("IDs with different transactionTypeCode should not be equal")
    void differentTransactionTypeCode() {
        DisclosureGroupId id1 = new DisclosureGroupId("A000000000", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("A000000000", "02", 1);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("IDs with different transactionCategoryCode should not be equal")
    void differentTransactionCategoryCode() {
        DisclosureGroupId id1 = new DisclosureGroupId("A000000000", "01", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("A000000000", "01", 2);

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void noArgConstructor() {
        DisclosureGroupId id = new DisclosureGroupId();

        assertNull(id.getAccountGroupId());
        assertNull(id.getTransactionTypeCode());
        assertNull(id.getTransactionCategoryCode());
    }
}
