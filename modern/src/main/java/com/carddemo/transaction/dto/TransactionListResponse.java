package com.carddemo.transaction.dto;

import java.util.List;

/**
 * Paginated response for transaction list.
 *
 * COBOL Traceability: Replaces the paginated screen display from COTRN00C
 * which shows 10 transactions per page with PF7/PF8 navigation.
 */
public record TransactionListResponse(
        List<TransactionResponse> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
