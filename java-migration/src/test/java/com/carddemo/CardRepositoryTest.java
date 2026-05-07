package com.carddemo;

import com.carddemo.entity.Card;
import com.carddemo.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldLoadAll50SeedRecords() {
        List<Card> cards = cardRepository.findAll();
        assertThat(cards).hasSize(50);
    }

    @Test
    void shouldHaveCorrectFirstRecordFields() {
        Optional<Card> card = cardRepository.findById("0500024453765740");
        assertThat(card).isPresent();

        Card c = card.get();
        assertThat(c.getCardNum()).isEqualTo("0500024453765740");
        assertThat(c.getAccountId()).isEqualTo(50L);
        assertThat(c.getCvvCode()).isEqualTo(747);
        assertThat(c.getEmbossedName()).isEqualTo("Aniya Von");
        assertThat(c.getExpirationDate()).isEqualTo(LocalDate.of(2023, 3, 9));
        assertThat(c.getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void shouldCreateNewCard() {
        Card newCard = Card.builder()
                .cardNum("9999999999999999")
                .accountId(99999L)
                .cvvCode(123)
                .embossedName("Test User")
                .expirationDate(LocalDate.of(2030, 12, 31))
                .activeStatus("Y")
                .build();

        Card saved = cardRepository.save(newCard);
        entityManager.flush();
        entityManager.clear();

        Optional<Card> found = cardRepository.findById("9999999999999999");
        assertThat(found).isPresent();
        assertThat(found.get().getEmbossedName()).isEqualTo("Test User");
        assertThat(found.get().getAccountId()).isEqualTo(99999L);
    }

    @Test
    void shouldReadExistingCard() {
        Optional<Card> card = cardRepository.findById("0683586198171516");
        assertThat(card).isPresent();
        assertThat(card.get().getEmbossedName()).isEqualTo("Ward Jones");
        assertThat(card.get().getAccountId()).isEqualTo(27L);
    }

    @Test
    void shouldUpdateExistingCard() {
        Optional<Card> card = cardRepository.findById("0500024453765740");
        assertThat(card).isPresent();

        Card c = card.get();
        c.setEmbossedName("Updated Name");
        c.setActiveStatus("N");
        cardRepository.save(c);
        entityManager.flush();
        entityManager.clear();

        Optional<Card> updated = cardRepository.findById("0500024453765740");
        assertThat(updated).isPresent();
        assertThat(updated.get().getEmbossedName()).isEqualTo("Updated Name");
        assertThat(updated.get().getActiveStatus()).isEqualTo("N");
    }

    @Test
    void shouldDeleteCard() {
        assertThat(cardRepository.findById("0500024453765740")).isPresent();

        cardRepository.deleteById("0500024453765740");
        entityManager.flush();
        entityManager.clear();

        assertThat(cardRepository.findById("0500024453765740")).isNotPresent();
        assertThat(cardRepository.findAll()).hasSize(49);
    }

    @Test
    void shouldFindByAccountId() {
        List<Card> cards = cardRepository.findByAccountId(50L);
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getCardNum()).isEqualTo("0500024453765740");
        assertThat(cards.get(0).getEmbossedName()).isEqualTo("Aniya Von");
    }

    @Test
    void shouldFindByActiveStatus() {
        List<Card> activeCards = cardRepository.findByActiveStatus("Y");
        assertThat(activeCards).hasSize(50);

        List<Card> inactiveCards = cardRepository.findByActiveStatus("N");
        assertThat(inactiveCards).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentCardId() {
        Optional<Card> card = cardRepository.findById("0000000000000000");
        assertThat(card).isNotPresent();
    }

    @Test
    void shouldReturnEmptyListForNonExistentAccountId() {
        List<Card> cards = cardRepository.findByAccountId(999999L);
        assertThat(cards).isEmpty();
    }
}
