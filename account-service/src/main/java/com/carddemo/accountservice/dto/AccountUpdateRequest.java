package com.carddemo.accountservice.dto;

import java.math.BigDecimal;

/**
 * Request DTO for PUT /api/v1/accounts/{id}.
 * Mirrors the updatable fields from COACTUPC.cbl Account Update program.
 */
public record AccountUpdateRequest(
        // Account fields
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId,

        // Customer fields
        String firstName,
        String middleName,
        String lastName,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String stateCode,
        String countryCode,
        String zip,
        String phoneNumber1,
        String phoneNumber2,
        String ssn,
        String govtIssuedId,
        String dateOfBirth,
        String eftAccountId,
        String primaryCardHolderIndicator,
        Integer ficoCreditScore
) {
}
