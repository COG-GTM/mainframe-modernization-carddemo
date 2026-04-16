package com.carddemo.cardservice.dto;

import java.util.List;

/**
 * Paginated list response for cards.
 * Preserves the COBOL pagination model of 7 cards per page.
 */
public record CardListResponse(
        List<CardResponse> cards,
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
