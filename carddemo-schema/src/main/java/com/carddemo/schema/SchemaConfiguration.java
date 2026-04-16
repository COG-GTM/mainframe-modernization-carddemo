package com.carddemo.schema;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for the CardDemo schema module.
 * Enables JPA entity scanning and repository discovery for all carddemo packages.
 */
@Configuration
@EntityScan(basePackages = "com.carddemo")
@EnableJpaRepositories(basePackages = "com.carddemo")
public class SchemaConfiguration {
}
