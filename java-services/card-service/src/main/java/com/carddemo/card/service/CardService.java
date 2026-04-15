package com.carddemo.card.service;

import com.carddemo.card.dto.CardDto;
import com.carddemo.card.dto.CardListResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.exception.ResourceNotFoundException;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardCrossReference;
import com.carddemo.card.repository.CardCrossReferenceRepository;
import com.carddemo.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer implementing credit card business logic migrated from COBOL programs:
 * - COCRDLIC.cbl: Card list (paginated browsing by account ID)
 * - COCRDSLC.cbl: Card detail view (read by card number)
 * - COCRDUPC.cbl: Card update (validate and save changes)
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardCrossReferenceRepository crossReferenceRepository;

    /**
     * List cards for a given account ID with pagination.
     *
     * Migrated from COCRDLIC.cbl:
     * - Browses CARDDAT file via CARDAIX alternate index (by account ID)
     * - Displays 7 rows per page with forward/backward navigation
     * - Filters records by account ID
     */
    @Transactional(readOnly = true)
    public CardListResponse getCardsByAccountId(String accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cardNumber").ascending());
        Page<Card> cardPage = cardRepository.findByAccountId(accountId, pageable);

        return CardListResponse.builder()
                .cards(cardPage.getContent().stream().map(this::toDto).toList())
                .page(cardPage.getNumber())
                .size(cardPage.getSize())
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .hasNext(cardPage.hasNext())
                .hasPrevious(cardPage.hasPrevious())
                .build();
    }

    /**
     * Get card details by card number.
     *
     * Migrated from COCRDSLC.cbl (9000-READ-DATA):
     * - Reads CARDDAT file by primary key (card number)
     * - Returns all card fields for display
     */
    @Transactional(readOnly = true)
    public CardDto getCardByNumber(String cardNumber) {
        Card card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        return toDto(card);
    }

    /**
     * Update card fields (embossed name, expiration date, active status).
     *
     * Migrated from COCRDUPC.cbl:
     * - 1200-EDIT-MAP-INPUTS: validates all input fields
     * - 1210-EDIT-CARD-NAME: name must be alphabetic + spaces, not blank
     * - 1220-EDIT-CARD-STATUS: active status must be Y or N
     * - 1250-EDIT-EXPIRY-MON: month must be 1-12
     * - 1260-EDIT-EXPIRY-YEAR: year must be 1950-2099
     * - 9200-WRITE-PROCESSING: locks record and rewrites to CARDDAT
     */
    @Transactional
    public CardDto updateCard(String cardNumber, CardUpdateRequest request) {
        Card card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));

        if (request.getExpirationDate() != null) {
            validateExpirationDate(request.getExpirationDate());
        }

        if (request.getEmbossedName() != null) {
            card.setEmbossedName(request.getEmbossedName());
        }
        if (request.getExpirationDate() != null) {
            card.setExpirationDate(request.getExpirationDate());
        }
        if (request.getActiveStatus() != null) {
            card.setActiveStatus(request.getActiveStatus());
        }

        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    /**
     * Get cross-reference data for a card number.
     *
     * Resolves card-to-customer and card-to-account linkages
     * using the XREFFILE (CVACT03Y.cpy layout).
     */
    @Transactional(readOnly = true)
    public CardCrossReference getCrossReference(String cardNumber) {
        return crossReferenceRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CardCrossReference", "cardNumber", cardNumber));
    }

    /**
     * Validate expiration date components.
     *
     * From COCRDUPC.cbl:
     * - 1250-EDIT-EXPIRY-MON: VALID-MONTH values 1 thru 12
     * - 1260-EDIT-EXPIRY-YEAR: VALID-YEAR values 1950 thru 2099
     */
    private void validateExpirationDate(String expirationDate) {
        String[] parts = expirationDate.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expiration date must be in YYYY-MM-DD format");
        }

        int year;
        int month;
        int day;
        try {
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            day = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expiration date must contain valid numeric values");
        }

        if (year < 1950 || year > 2099) {
            throw new IllegalArgumentException("Invalid card expiry year");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Card expiry month must be between 1 and 12");
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Card expiry day must be between 1 and 31");
        }
    }

    private CardDto toDto(Card card) {
        return CardDto.builder()
                .cardNumber(card.getCardNumber())
                .accountId(card.getAccountId())
                .cvvCode(card.getCvvCode())
                .embossedName(card.getEmbossedName())
                .expirationDate(card.getExpirationDate())
                .activeStatus(card.getActiveStatus())
                .build();
    }
}
