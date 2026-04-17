package com.carddemo.card.controller;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.dto.CardXrefResponse;
import com.carddemo.card.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for card operations.
 * Exposes modernized endpoints translated from COBOL CICS transactions:
 * - CCLI (COCRDLIC.cbl) -> GET /cards
 * - CCDL (COCRDSLC.cbl) -> GET /cards/{cardNum}
 * - CCUP (COCRDUPC.cbl) -> PUT /cards/{cardNum}
 * - CBACT03C.cbl xref   -> GET /cards/xref/{accountId}
 */
@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * List all cards with optional filtering by account ID.
     * Translates CICS transaction CCLI (COCRDLIC.cbl).
     */
    @GetMapping
    public ResponseEntity<Page<CardResponse>> listCards(
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CardResponse> cards = cardService.listCards(accountId, page, size);
        return ResponseEntity.ok(cards);
    }

    /**
     * Get card details by card number (16-char).
     * Translates CICS transaction CCDL (COCRDSLC.cbl).
     */
    @GetMapping("/{cardNum}")
    public ResponseEntity<CardResponse> getCard(@PathVariable String cardNum) {
        return cardService.getCardByNumber(cardNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update card (embossed name, expiration date, active status).
     * Translates CICS transaction CCUP (COCRDUPC.cbl).
     */
    @PutMapping("/{cardNum}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable String cardNum,
            @Valid @RequestBody CardUpdateRequest request) {
        return cardService.updateCard(cardNum, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get cross-reference records by account ID.
     * Translates CARDXREF VSAM AIX lookup from CBACT03C.cbl.
     * This is the most critical API — 12 programs across 5 other services
     * depend on this endpoint for card-to-account-to-customer resolution.
     */
    @GetMapping("/xref/{accountId}")
    public ResponseEntity<List<CardXrefResponse>> getXrefByAccountId(
            @PathVariable String accountId) {
        List<CardXrefResponse> xrefs = cardService.getXrefByAccountId(accountId);
        return ResponseEntity.ok(xrefs);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "card-service"));
    }
}
