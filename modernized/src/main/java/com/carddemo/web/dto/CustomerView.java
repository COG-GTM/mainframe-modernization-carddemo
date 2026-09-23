package com.carddemo.web.dto;

/** Customer section of the account view / update screens. */
public record CustomerView(
        Long customerId,
        String firstName,
        String middleName,
        String lastName,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String stateCode,
        String countryCode,
        String zipCode,
        String phoneNumber1,
        String phoneNumber2,
        Long ssn,
        String governmentIssuedId,
        String dateOfBirth,
        String eftAccountId,
        String primaryCardHolderIndicator,
        Integer ficoCreditScore) {
}
