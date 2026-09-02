package com.carddemo.repository.memory;

import com.carddemo.domain.Card;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.CardRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CARDDATA backed by a map seeded from carddata.txt. */
@Repository
public class InMemoryCardRepository implements CardRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, Card> cards = new LinkedHashMap<>();

    public InMemoryCardRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        cards.clear();
        dataSet.cards().forEach(card -> cards.put(card.getCardNumber(), Card.parse(card.format())));
    }

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        return Optional.ofNullable(cards.get(RecordKeys.cardNumber(cardNumber)));
    }

    @Override
    public List<Card> findByAccountId(String accountId) {
        String key = RecordKeys.accountId(accountId);
        return cards.values().stream().filter(card -> key.equals(card.getAccountId())).toList();
    }

    @Override
    public List<Card> findAll() {
        return new ArrayList<>(cards.values());
    }

    @Override
    public Card save(Card card) {
        cards.put(RecordKeys.cardNumber(card.getCardNumber()), card);
        return card;
    }
}
