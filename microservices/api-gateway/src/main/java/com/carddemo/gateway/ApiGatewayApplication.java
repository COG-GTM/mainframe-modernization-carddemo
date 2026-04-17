package com.carddemo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo API Gateway Application.
 *
 * Modernized from the COBOL mainframe menu navigation programs:
 * - COMEN01C.cbl: Regular user menu routing (10 options -> downstream services)
 * - COADM01C.cbl: Admin menu routing (4 options -> user admin service)
 *
 * The original COBOL programs used CICS XCTL to transfer control between
 * programs based on menu selection and user type (CDEMO-USER-TYPE from COCOM01Y.cpy).
 * This gateway translates that navigation logic into HTTP route-based dispatching
 * with JWT-based authentication and role-based authorization.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
