package com.carddemo.cardservice.controller;

import com.carddemo.cardservice.dto.CardListResponse;
import com.carddemo.cardservice.dto.CardResponse;
import com.carddemo.cardservice.dto.CardUpdateRequest;
import com.carddemo.cardservice.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for credit card management.
 * Ported from COBOL programs: COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl.
 *
 * Endpoints:
 *   GET  /api/v1/cards           — List cards (paginated, role-filtered)
 *   GET  /api/v1/cards/{cardNum} — Card detail view
 *   PUT  /api/v1/cards/{cardNum} — Update card
 */
@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Card Management",
     description = "Credit card management operations ported from CardDemo COBOL")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * List credit cards with pagination.
     * Ported from COCRDLIC.cbl.
     *
     * Business rules:
     * - Admin users see all cards when no accountId is provided
     * - Regular users must provide accountId and only see their own cards
     * - Default page size is 7 (matching COBOL WS-MAX-SCREEN-LINES)
     */
    @GetMapping
    @Operation(summary = "List credit cards",
               description = "Lists cards with pagination. Admin sees all; "
                           + "regular user sees only cards for their account.")
    @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
    @ApiResponse(responseCode = "404",
                 description = "NO RECORDS FOUND FOR THIS SEARCH CONDITION.")
    public ResponseEntity<CardListResponse> listCards(
            @Parameter(description = "Account ID to filter cards")
            @RequestParam(required = false) Long accountId,

            @Parameter(description = "Whether the requesting user is an admin")
            @RequestParam(defaultValue = "false") boolean isAdmin,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default 7, matching COBOL)")
            @RequestParam(required = false) Integer pageSize
    ) {
        CardListResponse response = cardService.listCards(
                accountId, isAdmin, page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Get card detail by card number.
     * Ported from COCRDSLC.cbl.
     */
    @GetMapping("/{cardNum}")
    @Operation(summary = "Get card detail",
               description = "Retrieves full card details by card number.")
    @ApiResponse(responseCode = "200", description = "Card retrieved successfully")
    @ApiResponse(responseCode = "404",
                 description = "Did not find cards for this search condition")
    public ResponseEntity<CardResponse> getCardDetail(
            @Parameter(description = "16-character card number")
            @PathVariable String cardNum
    ) {
        CardResponse response = cardService.getCardDetail(cardNum);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a card.
     * Ported from COCRDUPC.cbl.
     */
    @PutMapping("/{cardNum}")
    @Operation(summary = "Update card",
               description = "Updates card details. Validates name, status, "
                           + "and expiration date per COBOL business rules.")
    @ApiResponse(responseCode = "200", description = "Card updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404",
                 description = "Did not find cards for this search condition")
    public ResponseEntity<CardResponse> updateCard(
            @Parameter(description = "16-character card number")
            @PathVariable String cardNum,

            @Valid @RequestBody CardUpdateRequest request
    ) {
        CardResponse response = cardService.updateCard(cardNum, request);
        return ResponseEntity.ok(response);
    }
}
