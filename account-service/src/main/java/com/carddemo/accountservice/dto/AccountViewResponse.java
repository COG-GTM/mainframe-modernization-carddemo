package com.carddemo.accountservice.dto;

import java.math.BigDecimal;

/**
 * Response DTO for GET /api/v1/accounts/{id}.
 * Combines account details + customer details + card info,
 * mirroring the COACTVWC.cbl Account View screen output.
 */
public record AccountViewResponse(
        AccountDetails account,
        CustomerDetails customer,
        CardInfo card
) {

    public record AccountDetails(
            Long acctId,
            String activeStatus,
            BigDecimal currentBalance,
            BigDecimal creditLimit,
            BigDecimal cashCreditLimit,
            String openDate,
            String expirationDate,
            String reissueDate,
            BigDecimal currentCycleCredit,
            BigDecimal currentCycleDebit,
            String addressZip,
            String groupId
    ) {
    }

    public record CustomerDetails(
            Long custId,
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

    public record CardInfo(
            String cardNumber
    ) {
    }
}
