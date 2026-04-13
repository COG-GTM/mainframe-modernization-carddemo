package com.cardemo.service;

import com.cardemo.entity.Card;
import com.cardemo.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Card entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ (STARTBR/READNEXT/READPREV), REWRITE, WRITE.
 */
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

    @Transactional(readOnly = true)
    public List<Card> findByAcctId(Long acctId) {
        return cardRepository.findByCardAcctId(acctId);
    }

    @Transactional(readOnly = true)
    public List<Card> findByActiveStatus(String status) {
        return cardRepository.findByCardActiveStatus(status);
    }
}
