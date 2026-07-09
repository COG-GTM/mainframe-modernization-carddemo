package com.carddemo.service.card;

/**
 * Raised when a card read finds no matching record — the {@code DFHRESP(NOTFND)} branch of
 * {@code 9100-GETCARD-BYACCTCARD} in {@code COCRDSLC}/{@code COCRDUPC}. The
 * {@link #getMessage()} is the verbatim "Did not find cards for this search condition"
 * screen message; the owning controller maps it to an HTTP 404.
 */
public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String message) {
        super(message);
    }
}
