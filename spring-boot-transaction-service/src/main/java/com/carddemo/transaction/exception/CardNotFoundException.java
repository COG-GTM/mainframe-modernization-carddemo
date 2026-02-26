package com.carddemo.transaction.exception;

/**
 * Thrown when a card number lookup fails in the cross-reference table.
 *
 * Replaces COBOL DFHRESP(NOTFND) handling in READ-CCXREF-FILE:
 *   "Card Number NOT found..."
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String cardNumber) {
        super("Card Number NOT found: " + cardNumber);
    }
}
