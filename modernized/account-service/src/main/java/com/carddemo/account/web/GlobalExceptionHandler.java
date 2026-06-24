package com.carddemo.account.web;

import com.carddemo.account.exception.AccountFileException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountFileException.class)
    public ResponseEntity<Map<String, Object>> handleAccountFile(AccountFileException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "ACCOUNT_FILE_ERROR",
                "message", ex.getMessage()));
    }
}
