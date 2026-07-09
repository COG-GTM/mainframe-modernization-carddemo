package com.carddemo.web.account.dto;

import java.math.BigDecimal;

/**
 * Response DTO for the account view screen — the Java analogue of the {@code COACTVW} BMS
 * output map ({@code app/bms/COACTVW.bms}). It carries the selected account (copybook
 * {@code CVACT01Y}) together with the associated customer (copybook {@code CVCUS01Y}) that
 * {@code COACTVWC} resolves through the card cross-reference ({@code CVACT03Y}).
 *
 * <p>Field-for-field mapping to the BMS output fields is documented in
 * {@code java/docs/mapping/CS-4-accounts.md}. Following {@code java/README.md}: fixed-width
 * zero-padded identifiers are {@link String}; money amounts are {@link BigDecimal} scale 2;
 * component dates are exposed as their stored {@code yyyy-MM-dd} strings.</p>
 */
public record AccountViewResponse(
    // ---- Account (CVACT01Y) ----
    String acctId,
    String acctActiveStatus,
    BigDecimal currentBalance,
    BigDecimal creditLimit,
    BigDecimal cashCreditLimit,
    BigDecimal currentCycleCredit,
    BigDecimal currentCycleDebit,
    String openDate,
    String expirationDate,
    String reissueDate,
    String accountGroupId,
    // ---- Customer (CVCUS01Y) ----
    String custId,
    String firstName,
    String middleName,
    String lastName,
    String addressLine1,
    String addressLine2,
    String city,
    String stateCode,
    String zipCode,
    String countryCode,
    String phoneNum1,
    String phoneNum2,
    String ssn,
    String governmentIssuedId,
    String dateOfBirth,
    String eftAccountId,
    String primaryCardHolderIndicator,
    Integer ficoScore) {
}
