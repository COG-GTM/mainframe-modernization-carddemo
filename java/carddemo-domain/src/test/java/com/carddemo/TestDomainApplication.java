package com.carddemo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot configuration used only to bootstrap {@code @DataJpaTest} slices for
 * the domain module (which has no application main class of its own). Component scanning
 * rooted at {@code com.carddemo} picks up {@code com.carddemo.config.JpaConfig}, which wires
 * the entity and repository packages.
 */
@SpringBootApplication
public class TestDomainApplication {
}
