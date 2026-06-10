package com.carddemo.interestcalc.domain;

/**
 * Card cross-reference record.
 *
 * <p>Copybook: {@code app/cpy/CVACT03Y.cpy} ({@code CARD-XREF-RECORD}, RECLN = 50)
 *
 * <ul>
 *   <li>{@code cardNumber} &larr; {@code XREF-CARD-NUM PIC X(16)}</li>
 *   <li>{@code customerId} &larr; {@code XREF-CUST-ID  PIC 9(09)}</li>
 *   <li>{@code accountId}  &larr; {@code XREF-ACCT-ID  PIC 9(11)}</li>
 *   <li>(FILLER PIC X(14) not mapped)</li>
 * </ul>
 */
public record CardXrefRecord(
        String cardNumber,
        long customerId,
        long accountId) {
}
