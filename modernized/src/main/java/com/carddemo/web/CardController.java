package com.carddemo.web;

import com.carddemo.service.CardService;
import com.carddemo.web.dto.CardSummary;
import com.carddemo.web.dto.CardUpdateRequest;
import com.carddemo.web.dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** COCRDLIC (list), COCRDSLC (view) and COCRDUPC (update). */
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public PageResponse<CardSummary> list(@RequestParam(required = false) Long accountId,
                                          @RequestParam(required = false) String cardNumber,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "7") int size) {
        return cardService.list(accountId, cardNumber, page, size);
    }

    @GetMapping("/{cardNumber}")
    public CardSummary view(@PathVariable String cardNumber) {
        return cardService.view(cardNumber);
    }

    @PutMapping("/{cardNumber}")
    public CardSummary update(@PathVariable String cardNumber, @RequestBody CardUpdateRequest request) {
        return cardService.update(cardNumber, request);
    }
}
