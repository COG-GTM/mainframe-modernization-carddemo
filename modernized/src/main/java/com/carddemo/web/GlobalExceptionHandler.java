package com.carddemo.web;

import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.ConcurrentUpdateException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps the converted COBOL failure conditions onto HTTP responses the way CICS RESP codes were
 * surfaced on the BMS maps.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) {
        return status(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(RecordNotFoundException ex) {
        return status(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    public ResponseEntity<ApiError> handleConflict(ConcurrentUpdateException ex) {
        return status(HttpStatus.CONFLICT, ex.getMessage());
    }

    private static ResponseEntity<ApiError> status(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message));
    }
}
