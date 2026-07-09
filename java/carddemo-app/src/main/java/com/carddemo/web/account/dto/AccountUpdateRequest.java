package com.carddemo.web.account.dto;

/**
 * Request DTO for the account update screen — a field-for-field port of the editable
 * {@code UNPROT} fields of the {@code COACTUP} BMS map ({@code app/bms/COACTUP.bms}), which
 * {@code COACTUPC} reads into its {@code ACUP-NEW-*} working storage before validating.
 *
 * <p>Every field is a raw {@link String} so the COBOL field-level edits (blank / numeric /
 * range / format checks in {@code 1200-EDIT-MAP-INPUTS}) can be reproduced exactly,
 * including the original error-message text. Dates and phone numbers arrive as the same
 * component parts the 3270 map splits them into (year/month/day, area/prefix/line). The
 * BMS field name each property maps to is noted in the comments and in
 * {@code java/docs/mapping/CS-4-accounts.md}.</p>
 */
public record AccountUpdateRequest(
    String acctActiveStatus,   // ACSTTUS
    String openYear,           // OPNYEAR
    String openMonth,          // OPNMON
    String openDay,            // OPNDAY
    String creditLimit,        // ACRDLIM
    String expiryYear,         // EXPYEAR
    String expiryMonth,        // EXPMON
    String expiryDay,          // EXPDAY
    String cashCreditLimit,    // ACSHLIM
    String reissueYear,        // RISYEAR
    String reissueMonth,       // RISMON
    String reissueDay,         // RISDAY
    String currentBalance,     // ACURBAL
    String currentCycleCredit, // ACRCYCR
    String currentCycleDebit,  // ACRCYDB
    String accountGroupId,     // AADDGRP
    String ssnPart1,           // ACTSSN1
    String ssnPart2,           // ACTSSN2
    String ssnPart3,           // ACTSSN3
    String dobYear,            // DOBYEAR
    String dobMonth,           // DOBMON
    String dobDay,             // DOBDAY
    String ficoScore,          // ACSTFCO
    String firstName,          // ACSFNAM
    String middleName,         // ACSMNAM
    String lastName,           // ACSLNAM
    String addressLine1,       // ACSADL1
    String addressLine2,       // ACSADL2
    String city,               // ACSCITY (customer address line 3)
    String stateCode,          // ACSSTTE
    String zipCode,            // ACSZIPC
    String countryCode,        // ACSCTRY
    String phone1Area,         // ACSPH1A
    String phone1Prefix,       // ACSPH1B
    String phone1Line,         // ACSPH1C
    String phone2Area,         // ACSPH2A
    String phone2Prefix,       // ACSPH2B
    String phone2Line,         // ACSPH2C
    String governmentIssuedId, // ACSGOVT
    String eftAccountId,       // ACSEFTC
    String primaryCardHolderIndicator) { // ACSPFLG
}
