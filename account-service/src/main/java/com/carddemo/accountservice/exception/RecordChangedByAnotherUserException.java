package com.carddemo.accountservice.exception;

/**
 * Thrown when optimistic locking detects a concurrent modification.
 * Preserves COBOL error message from COACTUPC.cbl.
 */
public class RecordChangedByAnotherUserException extends RuntimeException {

    public static final String RECORD_CHANGED =
            "Record changed by some one else. Please review";

    public RecordChangedByAnotherUserException() {
        super(RECORD_CHANGED);
    }
}
