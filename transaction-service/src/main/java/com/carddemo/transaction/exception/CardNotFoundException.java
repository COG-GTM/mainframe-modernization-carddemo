package com.carddemo.transaction.exception;

/**
 * Thrown when a card number is not found in cross-reference.
 * Preserves COBOL error: 'Card Number NOT found...'
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException() {
        super("Card Number NOT found...");
    }
}
