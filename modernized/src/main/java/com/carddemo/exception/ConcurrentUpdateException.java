package com.carddemo.exception;

/** Equivalent of the COACTUPC/COCRDUPC "Record changed by some one else" condition. */
public class ConcurrentUpdateException extends RuntimeException {

    public ConcurrentUpdateException(String message) {
        super(message);
    }
}
