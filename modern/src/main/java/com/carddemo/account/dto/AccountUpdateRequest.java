package com.carddemo.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating account and customer data atomically.
 * Migrated from: COACTUPC.cbl — editable fields on the Account Update screen.
 * Both account and customer sections are updated in a single @Transactional operation,
 * matching the original CICS unit-of-work behavior (EXEC CICS REWRITE on ACCTDAT + CUSTDAT).
 */
public record AccountUpdateRequest(
        AccountFields account,
        CustomerFields customer
) {

    public record AccountFields(
            @Size(max = 1) String activeStatus,
            BigDecimal currentBalance,
            @DecimalMin("0") BigDecimal creditLimit,
            @DecimalMin("0") BigDecimal cashCreditLimit,
            LocalDate openDate,
            LocalDate expirationDate,
            LocalDate reissueDate,
            BigDecimal currentCycleCredit,
            BigDecimal currentCycleDebit,
            @Size(max = 10) String groupId
    ) {}

    public record CustomerFields(
            @Size(max = 25) String firstName,
            @Size(max = 25) String middleName,
            @Size(max = 25) String lastName,
            @Size(max = 50) String addrLine1,
            @Size(max = 50) String addrLine2,
            @Size(max = 50) String addrLine3,
            @Size(max = 2) String addrStateCd,
            @Size(max = 3) String addrCountryCd,
            @Size(max = 10) String addrZip,
            @Size(max = 15) String phoneNum1,
            @Size(max = 15) String phoneNum2,
            Long ssn,
            @Size(max = 20) String govtIssuedId,
            LocalDate dob,
            @Size(max = 10) String eftAccountId,
            @Size(max = 1) String primaryCardHolderInd,
            Integer ficoCreditScore
    ) {}
}
