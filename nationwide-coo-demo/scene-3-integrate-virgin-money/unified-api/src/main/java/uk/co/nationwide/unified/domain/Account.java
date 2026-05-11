package uk.co.nationwide.unified.domain;

import java.math.BigDecimal;

/**
 * Canonical account view across estates. The {@code source} attribution
 * tells the consumer which estate the underlying record lives in, so the
 * channel can decide which back office to call for changes.
 */
public record Account(
        String accountId,
        AccountType type,
        String displayNumber,
        BigDecimal balance,
        BigDecimal creditLimit,
        String currency,
        AccountStatus status,
        SourceSystem source
) {

    public enum AccountType { CARD, CURRENT, SAVINGS }

    public enum AccountStatus { ACTIVE, INACTIVE, CLOSED }
}
