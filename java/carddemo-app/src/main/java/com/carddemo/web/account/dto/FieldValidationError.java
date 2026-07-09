package com.carddemo.web.account.dto;

/**
 * A single field-level validation failure produced by the ported {@code COACTUPC} edits.
 *
 * <p>{@code field} is the logical field label the COBOL program uses in
 * {@code WS-EDIT-VARIABLE-NAME} (e.g. {@code "Credit Limit"}), and {@code message} is the
 * exact {@code WS-RETURN-MSG} text the program would have displayed for that field.</p>
 */
public record FieldValidationError(String field, String message) {
}
