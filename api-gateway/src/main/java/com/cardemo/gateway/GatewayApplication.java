package com.cardemo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway for the CardDemo application.
 * Replaces CICS COMMAREA routing with REST API routing,
 * JWT-based authentication, and role-based access control.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
