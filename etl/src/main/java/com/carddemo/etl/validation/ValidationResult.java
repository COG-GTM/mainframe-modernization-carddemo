package com.carddemo.etl.validation;

import java.util.List;

/**
 * Outcome of validating a single {@code Account}. Holds zero or more human-readable violation
 * messages; an empty list means the record is valid.
 */
public record ValidationResult(List<String> violations) {

    public ValidationResult {
        violations = List.copyOf(violations);
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    public String summary() {
        return String.join("; ", violations);
    }
}
