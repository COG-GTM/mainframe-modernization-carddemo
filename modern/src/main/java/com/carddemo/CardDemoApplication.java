package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Transaction Service - Spring Boot Application.
 *
 * COBOL Traceability: Replaces CICS online programs COTRN00C, COTRN01C,
 * COTRN02C, COBIL00C, CORPT00C and batch programs CBTRN01C, CBTRN02C,
 * CBTRN03C, CBACT04C.
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
