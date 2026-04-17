package com.carddemo.statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Statement Service Application - Modernized from COBOL programs CBSTM03A/CBSTM03B.
 *
 * <p>CBSTM03A orchestrated statement generation by reading transaction, card,
 * account, and customer data, then producing plain text and HTML output.
 * CBSTM03B handled the low-level file I/O operations as a subroutine.</p>
 *
 * <p>In this modernized version, file I/O is replaced with REST calls to
 * Account, Card, and Transaction microservices.</p>
 */
@SpringBootApplication
public class StatementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementServiceApplication.class, args);
    }
}
