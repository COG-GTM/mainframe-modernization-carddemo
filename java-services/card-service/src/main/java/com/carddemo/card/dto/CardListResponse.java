package com.carddemo.card.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated response for card listing.
 * Corresponds to the paginated card list screen in COCRDLIC.cbl
 * which displays 7 rows per page with forward/backward navigation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardListResponse {

    private List<CardDto> cards;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
