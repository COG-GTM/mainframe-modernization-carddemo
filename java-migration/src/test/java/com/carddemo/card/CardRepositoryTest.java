package com.carddemo.card;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifies the Card entity mapping, Flyway seed data, and repository finders
 * against an H2 (PostgreSQL mode) database loaded by the real Flyway migrations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardRepositoryTest {

    @Autowired
    private CardRepository repository;

    @Test
    void seedDataLoadsExpectedRecordCount() {
        assertThat(repository.count()).isEqualTo(50);
    }

    @Test
    void firstSeedRecordFieldsMatchParsedValues() {
        Card card = repository.findById("0500024453765740").orElseThrow();
        assertThat(card.getCardAcctId()).isEqualTo(50L);
        assertThat(card.getCardCvvCd()).isEqualTo(747);
        assertThat(card.getCardEmbossedName()).isEqualTo("Aniya Von");
        assertThat(card.getCardExpirationDate()).isEqualTo(LocalDate.of(2023, 3, 9));
        assertThat(card.getCardActiveStatus()).isEqualTo("Y");
    }

    @Test
    void lastSeedRecordFieldsMatchParsedValues() {
        Card card = repository.findById("9805583408996588").orElseThrow();
        assertThat(card.getCardAcctId()).isEqualTo(40L);
        assertThat(card.getCardCvvCd()).isEqualTo(908);
        assertThat(card.getCardEmbossedName()).isEqualTo("Davon Emmerich");
        assertThat(card.getCardExpirationDate()).isEqualTo(LocalDate.of(2023, 10, 27));
        assertThat(card.getCardActiveStatus()).isEqualTo("Y");
    }

    @Test
    void findByIdReturnsEmptyForUnknownKey() {
        assertThat(repository.findById("0000000000000000")).isEmpty();
    }

    @Test
    void findByCardAcctIdReturnsCardsForAccount() {
        List<Card> cards = repository.findByCardAcctId(50L);
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getCardNum()).isEqualTo("0500024453765740");
        assertThat(repository.findByCardAcctId(99999999999L)).isEmpty();
    }

    @Test
    void findByActiveStatusReturnsAllSeededCards() {
        assertThat(repository.findByCardActiveStatus("Y")).hasSize(50);
        assertThat(repository.findByCardActiveStatus("N")).isEmpty();
    }

    @Test
    void findAllByOrderByCardNumAscReturnsSortedSequentialScan() {
        List<Card> cards = repository.findAllByOrderByCardNumAsc();
        assertThat(cards).hasSize(50);
        assertThat(cards.get(0).getCardNum()).isEqualTo("0500024453765740");
        assertThat(cards.get(cards.size() - 1).getCardNum()).isEqualTo("9805583408996588");
    }

    @Test
    void createPersistsNewCard() {
        repository.save(newCard("1111222233334444", 70000000001L));

        Optional<Card> reloaded = repository.findById("1111222233334444");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCardEmbossedName()).isEqualTo("Test Holder");
        assertThat(reloaded.get().getCardCvvCd()).isEqualTo(123);
    }

    @Test
    void updateModifiesExistingCard() {
        Card card = repository.findById("0683586198171516").orElseThrow();
        card.setCardActiveStatus("N");
        repository.saveAndFlush(card);

        assertThat(repository.findById("0683586198171516").orElseThrow().getCardActiveStatus())
                .isEqualTo("N");
    }

    @Test
    void deleteRemovesCard() {
        repository.deleteById("0923877193247330");
        assertThat(repository.findById("0923877193247330")).isEmpty();
        assertThat(repository.count()).isEqualTo(49);
    }

    private static Card newCard(String cardNum, Long acctId) {
        Card card = new Card();
        card.setCardNum(cardNum);
        card.setCardAcctId(acctId);
        card.setCardCvvCd(123);
        card.setCardEmbossedName("Test Holder");
        card.setCardExpirationDate(LocalDate.of(2030, 1, 1));
        card.setCardActiveStatus("Y");
        return card;
    }
}
