package com.carddemo.card.repository;

import com.carddemo.card.entity.CardEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for CardRepository.
 *
 * Uses H2 in-memory database with Flyway migration (test profile).
 * Verifies repository operations against seeded sample data matching CARDDAT VSAM file.
 */
@DataJpaTest
@ActiveProfiles("test")
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void findById_existingCard_returnsCard() {
        Optional<CardEntity> card = cardRepository.findById("4111111111111111");

        assertTrue(card.isPresent());
        assertEquals("JOHN DOE", card.get().getEmbossedName());
        assertEquals(1L, card.get().getAccountId());
        assertEquals(123, card.get().getCvvCode());
        assertEquals("Y", card.get().getActiveStatus());
    }

    @Test
    void findById_nonExistingCard_returnsEmpty() {
        Optional<CardEntity> card = cardRepository.findById("0000000000000000");

        assertFalse(card.isPresent());
    }

    @Test
    void findAll_withPagination_returnsPage() {
        Page<CardEntity> page = cardRepository.findAll(PageRequest.of(0, 5));

        assertNotNull(page);
        assertEquals(5, page.getContent().size());
        assertTrue(page.getTotalElements() >= 12);
    }

    @Test
    void findAll_secondPage_returnsRemainingCards() {
        Page<CardEntity> page = cardRepository.findAll(PageRequest.of(1, 5));

        assertNotNull(page);
        assertTrue(page.getContent().size() > 0);
    }

    @Test
    void findByAccountId_existingAccount_returnsCards() {
        // Account 1 has 3 cards in seed data
        Page<CardEntity> page = cardRepository.findByAccountId(1L, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(3, page.getTotalElements());
        page.getContent().forEach(card ->
                assertEquals(1L, card.getAccountId()));
    }

    @Test
    void findByAccountId_nonExistingAccount_returnsEmpty() {
        Page<CardEntity> page = cardRepository.findByAccountId(99999L, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findByAccountId_withPagination_respectsPageSize() {
        // Account 1 has 3 cards; request page size 2
        Page<CardEntity> page = cardRepository.findByAccountId(1L, PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void save_updateCard_persistsChanges() {
        CardEntity card = cardRepository.findById("4111111111111111").orElseThrow();
        card.setEmbossedName("UPDATED NAME");
        cardRepository.save(card);

        CardEntity updated = cardRepository.findById("4111111111111111").orElseThrow();
        assertEquals("UPDATED NAME", updated.getEmbossedName());
    }
}
