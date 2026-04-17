package com.carddemo.card;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Card Service - Modernized from CardDemo COBOL mainframe application.
 * Translates COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C programs.
 */
@SpringBootApplication
public class CardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardServiceApplication.class, args);
    }
}
