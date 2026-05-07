package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroupId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DisclosureGroupRepository}.
 * Uses H2 in-memory database with Flyway migrations and seed data.
 */
@DataJpaTest
class DisclosureGroupRepositoryTest {

    @Autowired
    private DisclosureGroupRepository repository;

    @Test
    @DisplayName("Flyway seeds 51 disclosure group records")
    void seedDataLoaded() {
        long count = repository.count();
        assertEquals(51, count, "Expected 51 seed records from discgrp.txt");
    }

    @Test
    @DisplayName("Find by composite primary key returns correct record")
    void findById() {
        DisclosureGroupId id = new DisclosureGroupId("A000000000", "01", 1);
        Optional<DisclosureGroup> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getInterestRate().compareTo(new java.math.BigDecimal("15.00")));
    }

    @Test
    @DisplayName("Find by non-existent key returns empty")
    void findByIdNotFound() {
        DisclosureGroupId id = new DisclosureGroupId("NONEXIST", "99", 9999);
        Optional<DisclosureGroup> result = repository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByIdAccountGroupId returns all records for account group")
    void findByAccountGroupId() {
        List<DisclosureGroup> results = repository.findByIdAccountGroupId("A000000000");

        assertEquals(17, results.size(), "A000000000 group should have 17 records");
    }

    @Test
    @DisplayName("findByIdTransactionTypeCode returns records across all groups")
    void findByTransactionTypeCode() {
        List<DisclosureGroup> results = repository.findByIdTransactionTypeCode("01");

        assertFalse(results.isEmpty());
        results.forEach(dg ->
                assertEquals("01", dg.getId().getTransactionTypeCode()));
    }
}
