package com.carddemo.repository;

import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TransactionCategoryRepository}.
 * Uses H2 in-memory database with Flyway migrations applied automatically.
 */
@DataJpaTest
class TransactionCategoryRepositoryTest {

    @Autowired
    private TransactionCategoryRepository repository;

    @Test
    @DisplayName("All 18 seed records are loaded by Flyway")
    void testSeedDataCount() {
        assertEquals(18, repository.count());
    }

    @Test
    @DisplayName("Find by composite ID returns correct record")
    void testFindById() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        Optional<TransactionCategory> result = repository.findById(id);
        assertTrue(result.isPresent());
        assertEquals("Regular Sales Draft", result.get().getDescription());
    }

    @Test
    @DisplayName("Find by non-existent ID returns empty")
    void testFindByIdNotFound() {
        TransactionCategoryId id = new TransactionCategoryId("99", 9999);
        Optional<TransactionCategory> result = repository.findById(id);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByIdTypeCode returns all categories for type '01'")
    void testFindByTypeCode() {
        List<TransactionCategory> results = repository.findByIdTypeCode("01");
        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("findByIdTypeCode returns empty list for unknown type")
    void testFindByTypeCodeNotFound() {
        List<TransactionCategory> results = repository.findByIdTypeCode("99");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findByDescriptionContainingIgnoreCase finds matching records")
    void testFindByDescriptionContaining() {
        List<TransactionCategory> results = repository.findByDescriptionContainingIgnoreCase("cash");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(c ->
                c.getDescription().toLowerCase().contains("cash")));
    }

    @Test
    @DisplayName("Save and retrieve a new transaction category")
    void testSaveNewCategory() {
        TransactionCategoryId id = new TransactionCategoryId("08", 1);
        TransactionCategory category = new TransactionCategory(id, "Test Category");
        repository.save(category);

        Optional<TransactionCategory> result = repository.findById(id);
        assertTrue(result.isPresent());
        assertEquals("Test Category", result.get().getDescription());
        assertEquals(19, repository.count());
    }
}
