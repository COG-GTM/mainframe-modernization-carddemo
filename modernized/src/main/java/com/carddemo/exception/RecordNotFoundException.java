package com.carddemo.exception;

/** Equivalent of a CICS {@code DFHRESP(NOTFND)} response. */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(String message) {
        super(message);
    }
}
