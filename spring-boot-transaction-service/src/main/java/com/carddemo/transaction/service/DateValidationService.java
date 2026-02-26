package com.carddemo.transaction.service;

import com.carddemo.transaction.exception.ValidationException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Date validation service.
 * Replaces the COBOL CALL 'CSUTLDTC' (CEEDAYS date validation utility).
 *
 * In COBOL, CSUTLDTC called the IBM Language Environment CEEDAYS API
 * to validate date strings against a format mask (YYYY-MM-DD).
 * In Java, LocalDate parsing inherently validates dates, making this
 * utility much simpler.
 */
@Service
public class DateValidationService {

    /**
     * Validates that a date is not null and is a valid calendar date.
     * Java's LocalDate already enforces valid dates (e.g., rejects Feb 30).
     *
     * @param date the date to validate
     * @param fieldName the field name for error messages
     * @throws ValidationException if the date is null
     */
    public void validateDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new ValidationException(fieldName + " - Not a valid date");
        }
    }
}
