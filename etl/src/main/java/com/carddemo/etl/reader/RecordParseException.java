package com.carddemo.etl.reader;

/** Thrown when a fixed-length Account record cannot be decoded into an {@code Account}. */
public class RecordParseException extends RuntimeException {

    public RecordParseException(String message) {
        super(message);
    }

    public RecordParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
