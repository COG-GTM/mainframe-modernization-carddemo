package com.carddemo.service;

import com.carddemo.entity.Card;
import com.carddemo.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Card> findById(String cardNum) {
        return cardRepository.findById(cardNum);
    }

    @Transactional(readOnly = true)
    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    public Card save(Card card) {
        return cardRepository.save(card);
    }

    public Card update(Card card) {
        return cardRepository.save(card);
    }

    public void delete(String cardNum) {
        cardRepository.deleteById(cardNum);
    }

    @Transactional(readOnly = true)
    public List<Card> findByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Card> findByActiveStatus(String activeStatus) {
        return cardRepository.findByActiveStatus(activeStatus);
    }
}
