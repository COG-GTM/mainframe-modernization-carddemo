package com.carddemo.account.dto;

import java.math.BigDecimal;

/**
 * Request DTO for balance updates from other services
 * (Transaction Service, Billing Service).
 * Used by PUT /accounts/{id}/balance endpoint.
 */
public class BalanceUpdateRequest {

    private BigDecimal amount;

    public BalanceUpdateRequest() {
    }

    public BalanceUpdateRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
