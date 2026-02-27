package com.carddemo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class BillPaymentRequest {
    @NotNull private Long acctId;
    @NotNull @Positive private BigDecimal amount;
    public BillPaymentRequest() {}
    public BillPaymentRequest(Long acctId, BigDecimal amount) { this.acctId = acctId; this.amount = amount; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
}
