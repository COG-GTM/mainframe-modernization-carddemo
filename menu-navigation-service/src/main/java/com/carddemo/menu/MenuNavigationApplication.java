package com.carddemo.menu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the CardDemo Menu Navigation Service.
 * Ports COMEN01C.cbl (regular user menu) and COADM01C.cbl (admin menu)
 * to a Java 21 / Spring Boot REST service.
 */
@SpringBootApplication
public class MenuNavigationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuNavigationApplication.class, args);
    }
}
