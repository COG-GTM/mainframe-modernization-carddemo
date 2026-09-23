package com.carddemo.web.dto;

import java.math.BigDecimal;

public record BillPaymentResponse(String transactionId, BigDecimal amountPaid, BigDecimal remainingBalance) {
}
