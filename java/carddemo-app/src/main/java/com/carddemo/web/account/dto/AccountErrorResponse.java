package com.carddemo.web.account.dto;

import java.util.List;

/**
 * Error payload for the account endpoints. {@code message} is the exact COBOL
 * {@code WS-RETURN-MSG} the online program would have shown; {@code fieldErrors} carries the
 * per-field breakdown for update validation failures (empty for not-found and account-id
 * errors).
 */
public record AccountErrorResponse(String message, List<FieldValidationError> fieldErrors) {
}
