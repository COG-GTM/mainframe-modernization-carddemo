package com.carddemo.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Transaction Service - Spring Boot migration of COBOL transaction processing.
 * Ports COTRN00C (List), COTRN01C (View), COTRN02C (Add), and COBIL00C (Bill Payment).
 */
@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
