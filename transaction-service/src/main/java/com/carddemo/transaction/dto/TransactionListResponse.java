package com.carddemo.transaction.dto;

import java.util.List;

/**
 * Paginated response for transaction list.
 * Mirrors COTRN00C pagination: 10 per page, page numbers, next/prev flags.
 */
public record TransactionListResponse(
        List<TransactionResponse> transactions,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNextPage,
        boolean hasPreviousPage
) {
}
