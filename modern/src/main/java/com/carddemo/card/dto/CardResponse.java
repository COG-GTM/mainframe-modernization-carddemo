package com.carddemo.card.dto;

/**
 * Response DTO for card detail and list views.
 *
 * Migrated from: COCRDSLC.cbl (detail view) and COCRDLIC.cbl (list view)
 * Maps the CARD-RECORD fields displayed on BMS screens.
 */
public record CardResponse(
        String cardNumber,
        Long accountId,
        String embossedName,
        String expirationDate,
        String activeStatus
) {
}
