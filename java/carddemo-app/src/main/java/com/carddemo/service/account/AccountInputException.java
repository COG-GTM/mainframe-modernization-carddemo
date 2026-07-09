package com.carddemo.service.account;

import java.util.List;

import com.carddemo.web.account.dto.FieldValidationError;

/**
 * Signals a field-level edit failure — the Java equivalent of {@code COACTVWC}/
 * {@code COACTUPC} setting {@code INPUT-ERROR} and populating {@code WS-RETURN-MSG}. The
 * controller maps it to HTTP 400.
 *
 * <p>{@link #getMessage()} is the first message the COBOL program would have shown (COBOL
 * only sets {@code WS-RETURN-MSG} once, when it is still {@code SPACES}); {@link #errors()}
 * lists every failing field in edit order for a richer API response.</p>
 */
public class AccountInputException extends RuntimeException {

    private final transient List<FieldValidationError> errors;

    public AccountInputException(String message) {
        this(message, List.of());
    }

    public AccountInputException(String message, List<FieldValidationError> errors) {
        super(message);
        this.errors = List.copyOf(errors);
    }

    public List<FieldValidationError> errors() {
        return errors;
    }
}
