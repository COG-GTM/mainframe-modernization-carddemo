package com.carddemo.interestcalc.domain;

import java.math.BigDecimal;

/**
 * Transaction category balance record.
 *
 * <p>Copybook: {@code app/cpy/CVTRA01Y.cpy} ({@code TRAN-CAT-BAL-RECORD}, RECLN = 50)
 *
 * <ul>
 *   <li>{@code accountId}      &larr; {@code TRANCAT-ACCT-ID  PIC 9(11)}</li>
 *   <li>{@code typeCode}       &larr; {@code TRANCAT-TYPE-CD  PIC X(02)}</li>
 *   <li>{@code categoryCode}   &larr; {@code TRANCAT-CD       PIC 9(04)}</li>
 *   <li>{@code balance}        &larr; {@code TRAN-CAT-BAL     PIC S9(09)V99}</li>
 *   <li>(FILLER PIC X(22) not mapped)</li>
 * </ul>
 */
public record TranCatBalRecord(
        long accountId,
        String typeCode,
        int categoryCode,
        BigDecimal balance) {

    public TranCatBalRecord {
        balance = balance.setScale(2);
    }
}
