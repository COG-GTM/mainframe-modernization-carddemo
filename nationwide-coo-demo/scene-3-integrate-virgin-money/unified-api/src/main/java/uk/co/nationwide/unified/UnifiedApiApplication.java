package uk.co.nationwide.unified;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Unified Member API spanning the Nationwide legacy card platform and the
 * acquired Virgin Money online-banking platform.
 *
 * <p>This service does <em>not</em> consolidate the two underlying data
 * models. It exposes a single member-facing read API and delegates to
 * source-specific adapters (the anti-corruption layer).
 */
@SpringBootApplication
public class UnifiedApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnifiedApiApplication.class, args);
    }
}
