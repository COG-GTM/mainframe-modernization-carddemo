package com.carddemo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DisclosureGroup} entity.
 */
class DisclosureGroupTest {

    @Test
    @DisplayName("All-args constructor sets fields correctly")
    void allArgsConstructor() {
        DisclosureGroupId id = new DisclosureGroupId("A000000000", "01", 1);
        DisclosureGroup entity = new DisclosureGroup(id, new BigDecimal("15.00"));

        assertEquals(id, entity.getId());
        assertEquals(new BigDecimal("15.00"), entity.getInterestRate());
    }

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void noArgConstructor() {
        DisclosureGroup entity = new DisclosureGroup();

        assertNull(entity.getId());
        assertNull(entity.getInterestRate());
    }

    @Test
    @DisplayName("Setter updates interest rate")
    void setInterestRate() {
        DisclosureGroup entity = new DisclosureGroup();
        entity.setInterestRate(new BigDecimal("25.50"));

        assertEquals(new BigDecimal("25.50"), entity.getInterestRate());
    }
}
