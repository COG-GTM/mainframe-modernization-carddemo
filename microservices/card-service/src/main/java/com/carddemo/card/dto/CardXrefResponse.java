package com.carddemo.card.dto;

import com.carddemo.card.entity.CardXref;

/**
 * Response DTO for card cross-reference data. Maps from CardXref entity.
 * This is the most critical API in the system — 12 programs across
 * 5 other services depend on this endpoint.
 */
public record CardXrefResponse(
        String cardNum,
        String custId,
        String acctId
) {
    public static CardXrefResponse from(CardXref xref) {
        return new CardXrefResponse(
                xref.getXrefCardNum(),
                xref.getXrefCustId(),
                xref.getXrefAcctId()
        );
    }
}
