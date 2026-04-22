package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private String cardNumber;
    private Long accountId;
    private Integer cvvCode;
    private String embossedName;
    private String expirationDate;
    private String activeStatus;
}
