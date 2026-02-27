package com.carddemo.service;

import com.carddemo.entity.Card;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Page<Card> listCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Card getCard(String cardNum) {
        return cardRepository.findById(cardNum)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));
    }

    @Transactional
    public Card updateCard(String cardNum, Card updated) {
        Card existing = getCard(cardNum);
        if (updated.getCardActiveStatus() != null) existing.setCardActiveStatus(updated.getCardActiveStatus());
        if (updated.getCardEmbossedName() != null) existing.setCardEmbossedName(updated.getCardEmbossedName());
        if (updated.getCardExpirationDate() != null) existing.setCardExpirationDate(updated.getCardExpirationDate());
        return cardRepository.save(existing);
    }
}
