package com.carddemo.interestcalc.domain;

import java.math.BigDecimal;

/**
 * Disclosure group record (interest rate per account group / transaction type / category).
 *
 * <p>Copybook: {@code app/cpy/CVTRA02Y.cpy} ({@code DIS-GROUP-RECORD}, RECLN = 50)
 *
 * <ul>
 *   <li>{@code accountGroupId}      &larr; {@code DIS-ACCT-GROUP-ID PIC X(10)}</li>
 *   <li>{@code transactionTypeCode} &larr; {@code DIS-TRAN-TYPE-CD  PIC X(02)}</li>
 *   <li>{@code transactionCategoryCode} &larr; {@code DIS-TRAN-CAT-CD PIC 9(04)}</li>
 *   <li>{@code interestRate}        &larr; {@code DIS-INT-RATE      PIC S9(04)V99}</li>
 *   <li>(FILLER PIC X(28) not mapped)</li>
 * </ul>
 */
public record DisclosureGroupRecord(
        String accountGroupId,
        String transactionTypeCode,
        int transactionCategoryCode,
        BigDecimal interestRate) {

    /** Group id used by CBACT04C 1200-GET-INTEREST-RATE fallback when the keyed read returns status '23'. */
    public static final String DEFAULT_GROUP_ID = "DEFAULT";

    public DisclosureGroupRecord {
        interestRate = interestRate.setScale(2);
    }
}
