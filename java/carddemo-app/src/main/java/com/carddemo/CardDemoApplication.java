package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the migrated CardDemo application.
 *
 * <p>Component scanning is rooted at the base package {@code com.carddemo}, so it picks up
 * the domain module's JPA configuration ({@code com.carddemo.config.JpaConfig}) as well as
 * the service / web / batch / security / config / session packages in this module.</p>
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
