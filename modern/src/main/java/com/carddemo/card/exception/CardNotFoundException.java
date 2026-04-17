package com.carddemo.card.exception;

/**
 * Thrown when a card record is not found in CARDDAT.
 *
 * Migrated from: COCRDSLC.cbl / COCRDUPC.cbl
 * CICS equivalent: RESP(NOTFND) on EXEC CICS READ DATASET('CARDDAT')
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String cardNumber) {
        super("Card not found: " + maskCardNumber(cardNumber));
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
