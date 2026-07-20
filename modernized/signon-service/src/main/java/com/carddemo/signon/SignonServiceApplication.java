package com.carddemo.signon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the modernized CardDemo signon service.
 *
 * <p>This service is the cloud-deployable replacement for the CICS COBOL
 * program {@code COSGN00C} (transaction {@code CC00}), the CardDemo signon
 * screen. Business logic, persistence and endpoints are added in later,
 * independently reviewable pull requests; this module is the project skeleton.
 */
@SpringBootApplication
public class SignonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignonServiceApplication.class, args);
    }
}
