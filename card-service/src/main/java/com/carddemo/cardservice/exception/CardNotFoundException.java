package com.carddemo.cardservice.exception;

/**
 * Thrown when a card is not found.
 * Preserves COBOL message: "Did not find cards for this search condition".
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String cardNum) {
        super("Did not find cards for this search condition");
    }
}
