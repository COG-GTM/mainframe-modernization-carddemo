package com.carddemo.cardservice.repository;

import com.carddemo.cardservice.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();

        cardRepository.save(new Card(
                "4111111111111111", 12345678901L, 123,
                "JOHN DOE", "2027-06-15", "Y"));
        cardRepository.save(new Card(
                "4222222222222222", 12345678901L, 456,
                "JANE DOE", "2028-03-20", "Y"));
        cardRepository.save(new Card(
                "4333333333333333", 99999999999L, 789,
                "BOB SMITH", "2026-12-31", "N"));
    }

    @Test
    @DisplayName("Find card by primary key — COBOL CARDDAT primary key access")
    void findById_existingCard_returnsCard() {
        Optional<Card> card = cardRepository.findById("4111111111111111");
        assertTrue(card.isPresent());
        assertEquals("JOHN DOE", card.get().getCardEmbossedName());
    }

    @Test
    @DisplayName("Find card by missing key returns empty")
    void findById_missingCard_returnsEmpty() {
        Optional<Card> card = cardRepository.findById("0000000000000000");
        assertTrue(card.isEmpty());
    }

    @Test
    @DisplayName("Find by account ID — COBOL CARDAIX alternate index")
    void findByCardAcctId_returnsMatchingCards() {
        Page<Card> cards = cardRepository.findByCardAcctId(
                12345678901L, PageRequest.of(0, 7));
        assertEquals(2, cards.getTotalElements());
    }

    @Test
    @DisplayName("Find by account ID with no matches returns empty page")
    void findByCardAcctId_noMatches_returnsEmptyPage() {
        Page<Card> cards = cardRepository.findByCardAcctId(
                11111111111L, PageRequest.of(0, 7));
        assertTrue(cards.isEmpty());
    }

    @Test
    @DisplayName("Pagination with page size 1 returns correct pages")
    void findByCardAcctId_pagination_worksCorrectly() {
        Page<Card> firstPage = cardRepository.findByCardAcctId(
                12345678901L, PageRequest.of(0, 1));
        assertEquals(1, firstPage.getContent().size());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());

        Page<Card> secondPage = cardRepository.findByCardAcctId(
                12345678901L, PageRequest.of(1, 1));
        assertEquals(1, secondPage.getContent().size());
        assertFalse(secondPage.hasNext());
    }
}
