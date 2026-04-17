package com.carddemo.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Billing Service Application.
 *
 * Modernized from COBOL programs:
 * - COBIL00C.cbl: Bill Payment processing
 * - CORPT00C.cbl: Transaction Report submission via TDQ
 */
@SpringBootApplication
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
