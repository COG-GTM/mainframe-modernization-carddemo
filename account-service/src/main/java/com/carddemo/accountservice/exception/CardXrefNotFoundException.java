package com.carddemo.accountservice.exception;

/**
 * Thrown when account is not found in card cross-reference file.
 * Preserves COBOL error message from COACTVWC.cbl.
 */
public class CardXrefNotFoundException extends RuntimeException {

    public static final String XREF_NOT_FOUND =
            "Did not find this account in account card xref file";

    public CardXrefNotFoundException() {
        super(XREF_NOT_FOUND);
    }

    public CardXrefNotFoundException(String message) {
        super(message);
    }
}
