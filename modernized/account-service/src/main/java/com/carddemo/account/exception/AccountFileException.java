package com.carddemo.account.exception;

/**
 * Raised when the account "file" cannot be read — the modern equivalent of
 * CBACT01C's {@code 9999-ABEND-PROGRAM} paragraph (which issued {@code CEE3ABD}
 * after a non-zero file status). Instead of abending the JVM we surface a typed
 * exception the caller can handle or log.
 */
public class AccountFileException extends RuntimeException {

    public AccountFileException(String message) {
        super(message);
    }

    public AccountFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
