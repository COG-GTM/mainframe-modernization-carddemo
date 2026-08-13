package com.carddemo.online.account;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * COBOL program: COACTUPC — account update, edit routines 1200-EDIT-MAP-INPUTS and below.
 *
 * <p>Holds the equivalent of the COBOL {@code INPUT-ERROR} flag, the per-field
 * {@code FLG-...-NOT-OK} flags (as a field to message map) and {@code WS-RETURN-MSG},
 * which in COBOL only ever holds the first message raised ({@code IF WS-RETURN-MSG-OFF}).</p>
 */
public class AccountValidationResult {

    private final Map<String, String> fieldErrors = new LinkedHashMap<>();
    private String returnMessage;

    /** Mirrors {@code SET INPUT-ERROR TO TRUE} plus the field level not-ok flag. */
    public void addError(String field, String message) {
        fieldErrors.putIfAbsent(field, message);
        if (returnMessage == null) {
            returnMessage = message;
        }
    }

    public boolean hasError(String field) {
        return fieldErrors.containsKey(field);
    }

    public boolean isInputError() {
        return !fieldErrors.isEmpty();
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }
}
