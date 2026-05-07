package com.carddemo.repository;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalanceId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TransactionCategoryBalanceRepository}.
 * Uses H2 in-memory database with Flyway migrations applied automatically.
 */
@DataJpaTest
class TransactionCategoryBalanceRepositoryTest {

    @Autowired
    private TransactionCategoryBalanceRepository repository;

    @Test
    @DisplayName("Seed data: 50 records loaded from Flyway migration")
    void testSeedDataLoaded() {
        List<TransactionCategoryBalance> all = repository.findAll();
        assertEquals(50, all.size());
    }

    @Test
    @DisplayName("findById: retrieves record by composite key (mirrors COBOL READ by TRAN-CAT-KEY)")
    void testFindById() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        Optional<TransactionCategoryBalance> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("findById: returns empty for non-existent key (mirrors COBOL RESP NOTFND)")
    void testFindByIdNotFound() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(99999L, "ZZ", 9999);
        Optional<TransactionCategoryBalance> result = repository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByIdAccountId: returns all records for account (mirrors COBOL sequential read by ACCT-ID)")
    void testFindByAccountId() {
        List<TransactionCategoryBalance> results = repository.findByIdAccountId(1L);

        assertFalse(results.isEmpty());
        results.forEach(r -> assertEquals(1L, r.getId().getAccountId()));
    }

    @Test
    @DisplayName("findByIdTypeCode: returns all records for type code")
    void testFindByTypeCode() {
        List<TransactionCategoryBalance> results = repository.findByIdTypeCode("01");

        assertEquals(50, results.size());
        results.forEach(r -> assertEquals("01", r.getId().getTypeCode()));
    }

    @Test
    @DisplayName("save: creates new record (mirrors COBOL WRITE in CBTRN02C 2700-A)")
    void testSaveNewRecord() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(999L, "02", 5);
        TransactionCategoryBalance entity = new TransactionCategoryBalance(id, new BigDecimal("100.50"));

        TransactionCategoryBalance saved = repository.save(entity);

        assertNotNull(saved);
        assertEquals(999L, saved.getId().getAccountId());
        assertEquals("02", saved.getId().getTypeCode());
        assertEquals(5, saved.getId().getCategoryCode());
        assertEquals(0, new BigDecimal("100.50").compareTo(saved.getBalance()));
    }

    @Test
    @DisplayName("save: updates existing record balance (mirrors COBOL REWRITE in CBTRN02C 2700-B)")
    void testUpdateExistingRecord() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        Optional<TransactionCategoryBalance> existing = repository.findById(id);
        assertTrue(existing.isPresent());

        existing.get().setBalance(new BigDecimal("250.75"));
        repository.save(existing.get());

        Optional<TransactionCategoryBalance> updated = repository.findById(id);
        assertTrue(updated.isPresent());
        assertEquals(0, new BigDecimal("250.75").compareTo(updated.get().getBalance()));
    }

    @Test
    @DisplayName("deleteById: removes record by composite key")
    void testDeleteById() {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1L, "01", 1);
        assertTrue(repository.findById(id).isPresent());

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }
}
