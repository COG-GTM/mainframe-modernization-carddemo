package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Long customerId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private String phoneNumber1;
    private String phoneNumber2;
    private Long ssn;
    private String governmentIssuedId;
    private String dateOfBirth;
    private String eftAccountId;
    private String primaryCardHolderIndicator;
    private Integer ficoCreditScore;
}
