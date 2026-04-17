package com.carddemo.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Combined response DTO returning account + customer + linked cards.
 * Migrated from: COACTVWC.cbl screen output combining ACCTDAT + CUSTDAT + CXACAIX data.
 */
public record AccountResponse(
        AccountData account,
        CustomerData customer,
        List<String> linkedCardNumbers
) {

    public record AccountData(
            Long accountId,
            String activeStatus,
            BigDecimal currentBalance,
            BigDecimal creditLimit,
            BigDecimal cashCreditLimit,
            LocalDate openDate,
            LocalDate expirationDate,
            LocalDate reissueDate,
            BigDecimal currentCycleCredit,
            BigDecimal currentCycleDebit,
            String groupId
    ) {}

    public record CustomerData(
            Long custId,
            String firstName,
            String middleName,
            String lastName,
            String addrLine1,
            String addrLine2,
            String addrLine3,
            String addrStateCd,
            String addrCountryCd,
            String addrZip,
            String phoneNum1,
            String phoneNum2,
            Long ssn,
            String govtIssuedId,
            LocalDate dob,
            String eftAccountId,
            String primaryCardHolderInd,
            Integer ficoCreditScore
    ) {}
}
