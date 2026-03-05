package com.carddemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple Spring context load test that ensures Flyway migrations apply cleanly
 * and Hibernate validates the schema successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchemaValidationTest {

    @Test
    void contextLoads() {
        // If this test passes, it means:
        // 1. Flyway migrations applied successfully
        // 2. Hibernate schema validation passed (ddl-auto: validate)
        // 3. All entity mappings are consistent with the database schema
    }
}
