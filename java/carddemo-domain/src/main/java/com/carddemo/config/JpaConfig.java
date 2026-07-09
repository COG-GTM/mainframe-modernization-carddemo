package com.carddemo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Datasource / JPA configuration owned by the domain module.
 *
 * <p>Entities live in {@code com.carddemo.domain} and Spring Data repositories in
 * {@code com.carddemo.repository}. Both packages are declared explicitly here so the
 * configuration remains valid regardless of where the {@code @SpringBootApplication}
 * main class is located (it is in the {@code carddemo-app} module).</p>
 */
@Configuration
@EntityScan(basePackages = "com.carddemo.domain")
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class JpaConfig {
}
