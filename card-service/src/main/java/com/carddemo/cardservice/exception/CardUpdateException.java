package com.carddemo.cardservice.exception;

/**
 * Thrown when a card update fails.
 * Preserves COBOL messages from COCRDUPC.cbl update validation.
 */
public class CardUpdateException extends RuntimeException {

    public CardUpdateException(String message) {
        super(message);
    }
}
