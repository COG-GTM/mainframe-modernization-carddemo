package com.carddemo.card.service;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.entity.CardEntity;
import com.carddemo.card.exception.CardNotFoundException;
import com.carddemo.card.exception.CardValidationException;
import com.carddemo.card.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Card service implementing business logic for credit card management.
 *
 * Migrated from: COCRDLIC.cbl (Credit Card List — CCLI)
 *                COCRDSLC.cbl (Credit Card Detail View — CCDL)
 *                COCRDUPC.cbl (Credit Card Update — CCUP)
 *
 * CICS operations replaced:
 *   STARTBR/READNEXT/READPREV on CARDDAT → Spring Data pagination
 *   READ on CARDDAT → JPA findById
 *   REWRITE on CARDDAT → JPA save with @Version optimistic locking
 *
 * Navigation context:
 *   XCTL from COMEN01C (main menu, option 3) → COCRDLIC
 *   XCTL from COCRDLIC → COCRDSLC (detail) or COCRDUPC (update)
 *   PF3 returns to caller via CDEMO-FROM-PROGRAM
 */
@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * List cards with pagination — replaces COCRDLIC.cbl browse logic.
     *
     * CICS operations replaced:
     *   EXEC CICS STARTBR DATASET('CARDDAT') RIDFLD(card-key)
     *   EXEC CICS READNEXT DATASET('CARDDAT') INTO(CARD-RECORD)
     *   EXEC CICS READPREV DATASET('CARDDAT') INTO(CARD-RECORD)
     *   EXEC CICS ENDBR DATASET('CARDDAT')
     *
     * PF7 (page up) and PF8 (page down) replaced by Pageable page parameter.
     */
    @Transactional(readOnly = true)
    public Page<CardResponse> listCards(Long accountId, Pageable pageable) {
        Page<CardEntity> page;
        if (accountId != null) {
            log.debug("Listing cards for accountId={}, page={}", accountId, pageable);
            page = cardRepository.findByAccountId(accountId, pageable);
        } else {
            log.debug("Listing all cards, page={}", pageable);
            page = cardRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    /**
     * View card detail — replaces COCRDSLC.cbl read-only view.
     *
     * CICS operations replaced:
     *   EXEC CICS READ DATASET('CARDDAT') INTO(CARD-RECORD) RIDFLD(card-num)
     *
     * Also supports cross-reference lookup by account path for linked account ID.
     */
    @Transactional(readOnly = true)
    public CardResponse getCard(String cardNumber) {
        log.debug("Getting card detail for cardNumber={}", cardNumber);
        CardEntity card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));
        return toResponse(card);
    }

    /**
     * Update card — replaces COCRDUPC.cbl update logic.
     *
     * CICS operations replaced:
     *   EXEC CICS READ DATASET('CARDDAT') INTO(CARD-RECORD) RIDFLD(card-num) UPDATE
     *   (validate changes)
     *   EXEC CICS REWRITE DATASET('CARDDAT') FROM(CARD-RECORD)
     *
     * Card number is immutable (primary key) and cannot be changed.
     * Only embossedName, expirationDate, and activeStatus are updatable.
     * Optimistic locking via @Version replaces CICS record-level locking.
     */
    @Transactional
    public CardResponse updateCard(String cardNumber, CardUpdateRequest request) {
        log.debug("Updating card cardNumber={}", cardNumber);

        CardEntity card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));

        validateUpdateRequest(request);

        if (request.embossedName() != null) {
            card.setEmbossedName(request.embossedName());
        }
        if (request.expirationDate() != null) {
            card.setExpirationDate(request.expirationDate());
        }
        if (request.activeStatus() != null) {
            card.setActiveStatus(request.activeStatus());
        }

        CardEntity saved = cardRepository.save(card);
        log.info("Card updated successfully: cardNumber={}", cardNumber);
        return toResponse(saved);
    }

    private void validateUpdateRequest(CardUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.embossedName() != null && request.embossedName().isBlank()) {
            errors.add("Embossed name must not be blank");
        }

        if (request.expirationDate() != null) {
            if (!request.expirationDate().matches("\\d{2}-\\d{2}-\\d{4}")) {
                errors.add("Expiration date must be in MM-DD-YYYY format");
            } else {
                int month = Integer.parseInt(request.expirationDate().substring(0, 2));
                int day = Integer.parseInt(request.expirationDate().substring(3, 5));
                if (month < 1 || month > 12) {
                    errors.add("Expiration month must be between 01 and 12");
                }
                if (day < 1 || day > 31) {
                    errors.add("Expiration day must be between 01 and 31");
                }
            }
        }

        if (request.activeStatus() != null
                && !request.activeStatus().equals("Y")
                && !request.activeStatus().equals("N")) {
            errors.add("Active status must be 'Y' or 'N'");
        }

        if (!errors.isEmpty()) {
            throw new CardValidationException(errors);
        }
    }

    private CardResponse toResponse(CardEntity card) {
        return new CardResponse(
                card.getCardNumber(),
                card.getAccountId(),
                card.getCvvCode(),
                card.getEmbossedName(),
                card.getExpirationDate(),
                card.getActiveStatus()
        );
    }
}
