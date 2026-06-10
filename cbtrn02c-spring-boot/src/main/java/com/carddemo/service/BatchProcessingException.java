package com.carddemo.service;

/**
 * Thrown for unrecoverable errors during batch processing. Equivalent to the COBOL
 * program's abend (CEE3ABD with code 999) on file I/O errors.
 */
public class BatchProcessingException extends RuntimeException {

    public BatchProcessingException(String message) {
        super(message);
    }

    public BatchProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
