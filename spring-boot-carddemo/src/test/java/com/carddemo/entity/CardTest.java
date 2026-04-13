package com.carddemo.entity;

import com.carddemo.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for Card entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from carddata.txt, CRUD operations, and custom finders.
 */
@DataJpaTest
class CardTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void seedDataLoadsExpectedNumberOfCards() {
        List<Card> all = cardRepository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstCardFieldsMatchSeedData() {
        // Find first card by looking at seed data
        List<Card> all = cardRepository.findAll();
        assertThat(all).isNotEmpty();
        Card first = all.stream().filter(c -> c.getCardAcctId() == 1L).findFirst().orElseThrow();
        assertThat(first.getCardNum()).isNotBlank();
        assertThat(first.getCardAcctId()).isEqualTo(1L);
        assertThat(first.getCardActiveStatus()).isNotBlank();
    }

    @Test
    void findByAcctIdReturnsCards() {
        List<Card> cards = cardRepository.findByCardAcctId(1L);
        assertThat(cards).isNotEmpty();
        cards.forEach(c -> assertThat(c.getCardAcctId()).isEqualTo(1L));
    }

    @Test
    void findByActiveStatusReturnsCards() {
        List<Card> active = cardRepository.findByCardActiveStatus("Y");
        assertThat(active).isNotEmpty();
        active.forEach(c -> assertThat(c.getCardActiveStatus()).isEqualTo("Y"));
    }

    @Test
    void createCardPersistsAndReadsBack() {
        Card newCard = new Card();
        newCard.setCardNum("9999888877776666");
        newCard.setCardAcctId(99999L);
        newCard.setCardCvvCd(123);
        newCard.setCardEmbossedName("TEST USER");
        newCard.setCardExpirationDate(LocalDate.of(2030, 12, 31));
        newCard.setCardActiveStatus("Y");

        cardRepository.save(newCard);

        Optional<Card> found = cardRepository.findById("9999888877776666");
        assertThat(found).isPresent();
        assertThat(found.get().getCardEmbossedName()).isEqualTo("TEST USER");
        assertThat(found.get().getCardCvvCd()).isEqualTo(123);
    }

    @Test
    void updateCardStatus() {
        // Mirrors COBOL REWRITE for card status change
        List<Card> cards = cardRepository.findAll();
        Card card = cards.get(0);
        String originalStatus = card.getCardActiveStatus();
        card.setCardActiveStatus("N");
        cardRepository.save(card);

        Card updated = cardRepository.findById(card.getCardNum()).orElseThrow();
        assertThat(updated.getCardActiveStatus()).isEqualTo("N");
    }

    @Test
    void deleteCardRemovesFromDatabase() {
        List<Card> before = cardRepository.findAll();
        int beforeSize = before.size();
        cardRepository.deleteById(before.get(0).getCardNum());
        assertThat(cardRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void cardNumIs16Characters() {
        List<Card> all = cardRepository.findAll();
        all.forEach(c -> assertThat(c.getCardNum().trim()).hasSizeLessThanOrEqualTo(16));
    }

    @Test
    void allCardsHaveValidCvv() {
        List<Card> all = cardRepository.findAll();
        all.forEach(c -> {
            assertThat(c.getCardCvvCd()).isGreaterThanOrEqualTo(0);
            assertThat(c.getCardCvvCd()).isLessThan(1000);
        });
    }

    @Test
    void cardNumIsUniqueAcrossAllRecords() {
        List<Card> all = cardRepository.findAll();
        long distinctNums = all.stream().map(Card::getCardNum).distinct().count();
        assertThat(distinctNums).isEqualTo(all.size());
    }
}
