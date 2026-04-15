package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private String transactionId;
    private String typeCode;
    private Integer categoryCode;
    private String source;
    private String description;
    private BigDecimal amount;
    private Long merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNumber;
    private String originTimestamp;
    private String processedTimestamp;
}
