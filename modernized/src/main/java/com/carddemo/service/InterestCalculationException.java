package com.carddemo.service;

/**
 * Raised when the interest run hits an unrecoverable condition.
 *
 * <p>Mirrors CBACT04C's {@code 9999-ABEND-PROGRAM} (which issues {@code CALL 'CEE3ABD'} to abend
 * the job). Being unchecked, it propagates out of the Spring Batch step and rolls back the active
 * chunk transaction, the modern analogue of a batch ABEND.
 */
public class InterestCalculationException extends RuntimeException {

    public InterestCalculationException(String message) {
        super(message);
    }
}
