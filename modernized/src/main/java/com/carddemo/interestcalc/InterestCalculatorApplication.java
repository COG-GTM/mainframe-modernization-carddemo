package com.carddemo.interestcalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone Spring Boot batch application — Java migration of the mainframe batch
 * program {@code app/cbl/CBACT04C.cbl} (CardDemo interest calculator).
 */
@SpringBootApplication
public class InterestCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterestCalculatorApplication.class, args);
    }
}
