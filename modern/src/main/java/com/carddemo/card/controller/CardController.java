package com.carddemo.card.controller;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.dto.ErrorResponse;
import com.carddemo.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Credit Card management.
 *
 * Migrated from: COCRDLIC.cbl (Credit Card List — CCLI)
 *                COCRDSLC.cbl (Credit Card Detail View — CCDL)
 *                COCRDUPC.cbl (Credit Card Update — CCUP)
 *
 * CICS operations: STARTBR/READNEXT/READPREV on CARDDAT (list),
 *                  READ on CARDDAT (detail/update),
 *                  REWRITE on CARDDAT (update)
 *
 * Navigation: XCTL from COMEN01C (menu opt 3), XCTL to COCRDSLC/COCRDUPC
 */
@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Card Management",
        description = "Credit card list, detail, and update operations ported from COBOL CardDemo programs")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Paginated card list — replaces COCRDLIC.cbl browse.
     *
     * Migrated from: COCRDLIC.cbl (Credit Card List — CCLI)
     * CICS operations: STARTBR/READNEXT/READPREV on CARDDAT
     * Navigation: XCTL from COMEN01C (menu opt 3), XCTL to COCRDSLC/COCRDUPC
     */
    @GetMapping
    @Operation(
            summary = "List credit cards with pagination",
            description = "Returns a paginated list of credit cards. Optionally filter by account ID. "
                    + "Ports COCRDLIC.cbl browse logic (PF7/PF8 paging).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
            }
    )
    public ResponseEntity<Page<CardResponse>> listCards(
            @Parameter(description = "Filter by account ID (11-digit)", example = "00000000001")
            @RequestParam(value = "accountId", required = false) Long accountId,
            @PageableDefault(size = 10, sort = "cardNumber") Pageable pageable) {
        Page<CardResponse> cards = cardService.listCards(accountId, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Card detail view — replaces COCRDSLC.cbl.
     *
     * Migrated from: COCRDSLC.cbl (Credit Card Detail View — CCDL)
     * CICS operations: READ on CARDDAT by key
     */
    @GetMapping("/{cardNumber}")
    @Operation(
            summary = "View card details",
            description = "Retrieves detailed card information including linked account ID. "
                    + "Ports COCRDSLC.cbl read-only detail view.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card found",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<CardResponse> getCard(
            @Parameter(description = "16-character card number", example = "4111111111111111")
            @PathVariable("cardNumber") String cardNumber) {
        CardResponse card = cardService.getCard(cardNumber);
        return ResponseEntity.ok(card);
    }

    /**
     * Update card — replaces COCRDUPC.cbl.
     *
     * Migrated from: COCRDUPC.cbl (Credit Card Update — CCUP)
     * CICS operations: READ for UPDATE + REWRITE on CARDDAT
     * Card number is immutable (primary key) and cannot be changed.
     */
    @PutMapping("/{cardNumber}")
    @Operation(
            summary = "Update card details",
            description = "Updates editable card fields (embossed name, expiration date, active status). "
                    + "Card number is immutable. Ports COCRDUPC.cbl update logic with validation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card updated",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation errors",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Concurrent modification conflict",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<CardResponse> updateCard(
            @Parameter(description = "16-character card number", example = "4111111111111111")
            @PathVariable("cardNumber") String cardNumber,
            @Valid @RequestBody CardUpdateRequest request) {
        CardResponse updated = cardService.updateCard(cardNumber, request);
        return ResponseEntity.ok(updated);
    }
}
