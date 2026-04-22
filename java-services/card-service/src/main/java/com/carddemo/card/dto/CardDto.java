package com.carddemo.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for card detail responses, mapped from Card entity fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {

    private String cardNumber;
    private String accountId;
    private String cvvCode;
    private String embossedName;
    private String expirationDate;
    private String activeStatus;
}
