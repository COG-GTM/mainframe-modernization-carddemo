package com.cardemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the Transaction Detail Report batch service.
 * Ported from COBOL program CBTRN03C.CBL.
 */
@SpringBootApplication
public class TransactionDetailReportApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
                SpringApplication.run(TransactionDetailReportApplication.class, args)));
    }
}
