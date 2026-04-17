package com.carddemo.card.service;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.dto.CardXrefResponse;
import com.carddemo.card.entity.Card;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for card operations.
 * Consolidates business logic from:
 * - COCRDLIC.cbl (list cards, with optional account filter)
 * - COCRDSLC.cbl (view card detail by card number)
 * - COCRDUPC.cbl (update card: embossed name, expiration, status)
 * - CBACT02C.cbl (batch read/print all card data)
 * - CBACT03C.cbl (batch read/print all xref data)
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
     * List all cards with optional filtering by account ID.
     * Translates COCRDLIC.cbl browse logic (STARTBR/READNEXT on CARDDAT/CARDAIX).
     */
    public Page<CardResponse> listCards(String accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards;
        if (accountId != null && !accountId.isBlank()) {
            cards = cardRepository.findByCardAcctId(accountId, pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }
        return cards.map(CardResponse::from);
    }

    /**
     * Get card details by card number.
     * Translates COCRDSLC.cbl READ by primary key on CARDDAT file.
     */
    public Optional<CardResponse> getCardByNumber(String cardNum) {
        return cardRepository.findById(cardNum).map(CardResponse::from);
    }

    /**
     * Update card details (embossed name, expiration date, active status).
     * Translates COCRDUPC.cbl REWRITE logic with field-level validation.
     */
    @Transactional
    public Optional<CardResponse> updateCard(String cardNum, CardUpdateRequest request) {
        return cardRepository.findById(cardNum).map(card -> {
            if (request.cardEmbossedName() != null) {
                card.setCardEmbossedName(request.cardEmbossedName());
            }
            if (request.cardExpirationDate() != null) {
                card.setCardExpirationDate(request.cardExpirationDate());
            }
            if (request.cardActiveStatus() != null) {
                card.setCardActiveStatus(request.cardActiveStatus());
            }
            Card saved = cardRepository.save(card);
            return CardResponse.from(saved);
        });
    }

    /**
     * Get cross-reference records by account ID.
     * Translates CBACT03C.cbl sequential read and CARDXREF AIX lookup.
     * This is the most critical API — 12 programs across 5 other services
     * depend on this endpoint for card-to-account-to-customer resolution.
     */
    public List<CardXrefResponse> getXrefByAccountId(String accountId) {
        return cardXrefRepository.findByXrefAcctId(accountId)
                .stream()
                .map(CardXrefResponse::from)
                .toList();
    }
}
