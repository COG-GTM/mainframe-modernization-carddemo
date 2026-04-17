package com.carddemo.card.exception;

/**
 * Thrown when a card record is not found in CARDDAT.
 *
 * Migrated from: COCRDSLC.cbl / COCRDUPC.cbl
 * CICS equivalent: RESP(NOTFND) on EXEC CICS READ DATASET('CARDDAT')
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String cardNumber) {
        super("Card not found: " + cardNumber);
    }
}
