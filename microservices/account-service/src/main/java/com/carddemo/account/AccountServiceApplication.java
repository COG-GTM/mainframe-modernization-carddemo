package com.carddemo.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Account Service - Modernized from COBOL programs:
 * COACTVWC (Account View), COACTUPC (Account Update),
 * CBACT01C (Batch Print), CBACT04C (Interest Calculator)
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
