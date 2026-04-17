package com.carddemo.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Transaction Service Application - Modernized from COBOL mainframe programs:
 * COTRN00C (Transaction List), COTRN01C (Transaction View),
 * COTRN02C (Transaction Add), CBTRN01C/CBTRN02C (Batch posting),
 * CBTRN03C (Transaction detail report).
 */
@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
