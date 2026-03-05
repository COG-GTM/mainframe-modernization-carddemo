package com.carddemo.controller;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Card;
import com.carddemo.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Card management controller — replaces COCRDLIC, COCRDSLC, COCRDUPC CICS transactions.
 */
@RestController
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/api/accounts/{acctId}/cards")
    public ResponseEntity<Page<Card>> listCards(@PathVariable Long acctId, Pageable pageable) {
        return ResponseEntity.ok(cardService.listCards(acctId, pageable));
    }

    @GetMapping("/api/cards/{cardNum}")
    public ResponseEntity<Map<String, Object>> getCard(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/api/cards/{cardNum}")
    public ResponseEntity<Card> updateCard(@PathVariable String cardNum,
                                            @Valid @RequestBody CardUpdateRequest dto) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, dto));
    }
}
