package com.carddemo.cardservice.service;

import com.carddemo.cardservice.dto.CardListResponse;
import com.carddemo.cardservice.dto.CardResponse;
import com.carddemo.cardservice.dto.CardUpdateRequest;
import com.carddemo.cardservice.entity.Card;
import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.CardUpdateException;
import com.carddemo.cardservice.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementing credit card business logic.
 * Ported from COBOL programs: COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl.
 *
 * Business rules:
 * - Card list pagination: 7 cards per page (WS-MAX-SCREEN-LINES VALUE 7)
 * - Admin sees all cards; regular user sees only cards for their account
 * - Selection validation: only 'S' (view) and 'U' (update) allowed
 * - Card status: Y (active) or N (inactive)
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final int defaultPageSize;

    public CardService(CardRepository cardRepository,
                       @Value("${card-service.pagination.default-page-size:7}") int defaultPageSize) {
        this.cardRepository = cardRepository;
        this.defaultPageSize = defaultPageSize;
    }

    /**
     * List cards with pagination and role-based filtering.
     * Ported from COCRDLIC.cbl:
     *   a) All cards if admin user and no account context
     *   b) Only cards for specific account (via CARDAIX) if regular user
     *
     * @param accountId filter by account (null for admin to see all)
     * @param isAdmin   true if admin user
     * @param page      zero-based page number
     * @param pageSize  items per page (defaults to 7)
     * @return paginated card list
     */
    @Transactional(readOnly = true)
    public CardListResponse listCards(Long accountId, boolean isAdmin,
                                      int page, Integer pageSize) {
        int size = (pageSize != null && pageSize > 0) ? pageSize : defaultPageSize;
        Pageable pageable = PageRequest.of(page, size, Sort.by("cardNum").ascending());

        Page<Card> cardPage;

        if (isAdmin && accountId == null) {
            // Admin with no account filter: show all cards
            cardPage = cardRepository.findAll(pageable);
        } else {
            // Regular user or admin with account filter: show only their cards
            Long effectiveAccountId = accountId;
            if (effectiveAccountId == null) {
                // COBOL: 'NO RECORDS FOUND FOR THIS SEARCH CONDITION.'
                throw new CardNotFoundException("no-account");
            }
            cardPage = cardRepository.findByCardAcctId(effectiveAccountId, pageable);
        }

        if (cardPage.isEmpty()) {
            throw new CardNotFoundException("no-results");
        }

        return new CardListResponse(
                cardPage.getContent().stream()
                        .map(CardResponse::fromEntity)
                        .toList(),
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.hasNext(),
                cardPage.hasPrevious()
        );
    }

    /**
     * Get card detail by card number.
     * Ported from COCRDSLC.cbl (9000-READ-DATA section).
     *
     * @param cardNum the 16-character card number
     * @return card details
     */
    @Transactional(readOnly = true)
    public CardResponse getCardDetail(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));
        return CardResponse.fromEntity(card);
    }

    /**
     * Update a card.
     * Ported from COCRDUPC.cbl update logic with validation.
     *
     * Validation rules from COBOL:
     * - Card name can only contain alphabets and spaces
     * - Card Active Status must be Y or N
     * - Card expiry month must be between 1 and 12
     * - Invalid card expiry year (must be 1950-2099)
     * - No change detected with respect to values fetched
     *
     * @param cardNum the card number to update
     * @param request update request with new values
     * @return updated card
     */
    @Transactional
    public CardResponse updateCard(String cardNum, CardUpdateRequest request) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));

        // Validate expiration date components (ported from COCRDUPC.cbl)
        validateExpirationDate(request.expirationDate());

        // Check for actual changes (COBOL: 'No change detected...')
        boolean hasChanges = false;
        if (!request.embossedName().equals(card.getCardEmbossedName())) {
            hasChanges = true;
        }
        if (!request.expirationDate().equals(card.getCardExpirationDate())) {
            hasChanges = true;
        }
        if (!request.activeStatus().equals(card.getCardActiveStatus())) {
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new CardUpdateException(
                    "No change detected with respect to values fetched.");
        }

        card.setCardEmbossedName(request.embossedName());
        card.setCardExpirationDate(request.expirationDate());
        card.setCardActiveStatus(request.activeStatus());

        Card saved = cardRepository.save(card);
        return CardResponse.fromEntity(saved);
    }

    /**
     * Validate expiration date components.
     * Ported from COCRDUPC.cbl month/year validation.
     */
    private void validateExpirationDate(String expirationDate) {
        String[] parts = expirationDate.split("-");
        if (parts.length != 3) {
            throw new CardUpdateException(
                    "Expiration date must be in YYYY-MM-DD format");
        }

        int year;
        int month;
        try {
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new CardUpdateException("Invalid card expiry year");
        }

        // COBOL: 88 VALID-YEAR VALUES 1950 THRU 2099
        if (year < 1950 || year > 2099) {
            throw new CardUpdateException("Invalid card expiry year");
        }

        // COBOL: 88 VALID-MONTH VALUES 1 THRU 12
        if (month < 1 || month > 12) {
            throw new CardUpdateException(
                    "Card expiry month must be between 1 and 12");
        }
    }
}
