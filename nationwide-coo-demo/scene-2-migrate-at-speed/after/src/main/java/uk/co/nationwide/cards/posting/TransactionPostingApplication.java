package uk.co.nationwide.cards.posting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entrypoint for the modernized replacement of CBTRN02C
 * (daily card transaction posting batch).
 *
 * <p>The legacy COBOL program reads a daily transaction file, validates each
 * record against the card cross-reference, card master, and account master,
 * applies credit-limit and expiration checks, then posts to the transaction
 * master while updating category-balance and account-balance records.
 *
 * <p>This service exposes the same logic as a REST API; the batch driver
 * iterates a transaction feed and calls the same service method.
 */
@SpringBootApplication
public class TransactionPostingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionPostingApplication.class, args);
    }
}
