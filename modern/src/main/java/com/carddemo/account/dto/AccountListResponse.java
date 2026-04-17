package com.carddemo.account.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Paginated list response for accounts.
 */
public record AccountListResponse(
        List<AccountSummary> accounts,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public record AccountSummary(
            Long accountId,
            String activeStatus,
            BigDecimal currentBalance,
            BigDecimal creditLimit,
            String groupId
    ) {}
}
