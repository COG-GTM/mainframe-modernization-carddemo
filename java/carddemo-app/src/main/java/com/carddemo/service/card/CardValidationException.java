package com.carddemo.service.card;

/**
 * Raised when a card request fails one of the COBOL input edits (the {@code SET INPUT-ERROR}
 * branches in {@code COCRDLIC}/{@code COCRDSLC}/{@code COCRDUPC}). The {@link #getMessage()}
 * is the verbatim screen message (see {@link CardMessages}); the owning controller maps it
 * to an HTTP 400 with that message rather than relying on a global handler.
 */
public class CardValidationException extends RuntimeException {

    public CardValidationException(String message) {
        super(message);
    }
}
