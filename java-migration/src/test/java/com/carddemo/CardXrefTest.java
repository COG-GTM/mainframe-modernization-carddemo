package com.carddemo;

import com.carddemo.entity.CardXref;
import com.carddemo.repository.CardXrefRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the CardXref entity, repository, and seed data.
 * Uses @DataJpaTest with H2 in-memory database.
 * Flyway migrations run automatically, seeding 50 records from cardxref.txt.
 */
@DataJpaTest
class CardXrefTest {

    @Autowired
    private CardXrefRepository repository;

    // --- Seed data tests ---

    @Test
    void shouldLoadAll50SeedRecords() {
        List<CardXref> all = repository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void shouldLoadFirstRecordCorrectly() {
        Optional<CardXref> record = repository.findById("0500024453765740");
        assertThat(record).isPresent();
        assertThat(record.get().getCustomerId()).isEqualTo(50L);
        assertThat(record.get().getAccountId()).isEqualTo(50L);
    }

    @Test
    void shouldLoadLastRecordCorrectly() {
        Optional<CardXref> record = repository.findById("9805583408996588");
        assertThat(record).isPresent();
        assertThat(record.get().getCustomerId()).isEqualTo(40L);
        assertThat(record.get().getAccountId()).isEqualTo(40L);
    }

    // --- CRUD tests ---

    @Test
    void shouldCreateNewCardXref() {
        CardXref newXref = new CardXref("1234567890123456", 99L, 199L);
        CardXref saved = repository.save(newXref);

        assertThat(saved.getCardNum()).isEqualTo("1234567890123456");
        assertThat(saved.getCustomerId()).isEqualTo(99L);
        assertThat(saved.getAccountId()).isEqualTo(199L);

        assertThat(repository.findAll()).hasSize(51);
    }

    @Test
    void shouldUpdateExistingCardXref() {
        Optional<CardXref> existing = repository.findById("0500024453765740");
        assertThat(existing).isPresent();

        CardXref xref = existing.get();
        xref.setCustomerId(999L);
        repository.save(xref);

        Optional<CardXref> updated = repository.findById("0500024453765740");
        assertThat(updated).isPresent();
        assertThat(updated.get().getCustomerId()).isEqualTo(999L);
    }

    @Test
    void shouldDeleteCardXref() {
        repository.deleteById("0500024453765740");

        Optional<CardXref> deleted = repository.findById("0500024453765740");
        assertThat(deleted).isEmpty();
        assertThat(repository.findAll()).hasSize(49);
    }

    // --- Custom finder tests ---

    @Test
    void shouldFindByCustomerId() {
        List<CardXref> results = repository.findByCustomerId(50L);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCardNum()).isEqualTo("0500024453765740");
    }

    @Test
    void shouldFindByAccountId() {
        List<CardXref> results = repository.findByAccountId(27L);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCardNum()).isEqualTo("0683586198171516");
    }

    @Test
    void shouldReturnEmptyForNonExistentCustomerId() {
        List<CardXref> results = repository.findByCustomerId(99999L);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentCardNum() {
        Optional<CardXref> result = repository.findById("0000000000000000");
        assertThat(result).isEmpty();
    }
}
