package com.carddemo.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CardDemo Migration Tool.
 */
@SpringBootApplication(scanBasePackages = "com.carddemo")
public class MigrationToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrationToolApplication.class, args);
    }
}
