package com.carddemo.service;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Card management service — replaces COCRDLIC, COCRDSLC, COCRDUPC.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List cards by account ID with pagination.
     * Mirrors COCRDLIC: CICS STARTBR/READNEXT on CARDXREF by account ID.
     */
    public Page<Card> listCards(Long acctId, Pageable pageable) {
        return cardRepository.findByAcctId(acctId, pageable);
    }

    /**
     * Get card detail with cross-ref info.
     * Mirrors COCRDSLC: reads Card + CardXref.
     */
    public Map<String, Object> getCard(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNum));

        Map<String, Object> result = new HashMap<>();
        result.put("card", card);

        Optional<CardXref> xref = cardXrefRepository.findById(cardNum);
        xref.ifPresent(x -> {
            result.put("acctId", x.getAcctId());
            result.put("custId", x.getCustId());
        });

        return result;
    }

    /**
     * Update card fields.
     * Mirrors COCRDUPC.
     */
    @Transactional
    public Card updateCard(String cardNum, CardUpdateRequest dto) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNum));

        if (dto.getEmbossedName() != null) card.setEmbossedName(dto.getEmbossedName());
        if (dto.getExpirationDate() != null) card.setExpirationDate(LocalDate.parse(dto.getExpirationDate()));
        if (dto.getActiveStatus() != null) card.setActiveStatus(dto.getActiveStatus());

        return cardRepository.save(card);
    }
}
