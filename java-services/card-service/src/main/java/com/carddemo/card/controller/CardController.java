package com.carddemo.card.controller;

import com.carddemo.card.dto.CardDto;
import com.carddemo.card.dto.CardListResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.model.CardCrossReference;
import com.carddemo.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for credit card management endpoints.
 *
 * Migrated from CICS transactions:
 * - CCLI (COCRDLIC): Credit Card List
 * - CCDL (COCRDSLC): Credit Card Detail/Select
 * - CCUP (COCRDUPC): Credit Card Update
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
     * List cards for an account (paginated).
     * Migrated from COCRDLIC.cbl — browses card file by account ID.
     * Default page size of 7 matches the original COBOL screen (WS-MAX-SCREEN-LINES = 7).
     */
    @GetMapping
    public ResponseEntity<CardListResponse> listCards(
            @RequestParam String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        CardListResponse response = cardService.getCardsByAccountId(accountId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * View card details by card number.
     * Migrated from COCRDSLC.cbl — reads card by primary key.
     */
    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardDto> getCard(@PathVariable String cardNumber) {
        CardDto card = cardService.getCardByNumber(cardNumber);
        return ResponseEntity.ok(card);
    }

    /**
     * Update card fields (embossed name, expiration date, active status).
     * Migrated from COCRDUPC.cbl — validates and updates card record.
     */
    @PutMapping("/{cardNumber}")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable String cardNumber,
            @Valid @RequestBody CardUpdateRequest request) {
        CardDto updated = cardService.updateCard(cardNumber, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get cross-reference data for a card number.
     * Returns customer ID and account ID linkage from XREFFILE.
     */
    @GetMapping("/xref/{cardNumber}")
    public ResponseEntity<CardCrossReference> getCrossReference(@PathVariable String cardNumber) {
        CardCrossReference xref = cardService.getCrossReference(cardNumber);
        return ResponseEntity.ok(xref);
    }
}
