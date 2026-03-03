package com.carddemo.posttran;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify the Spring Boot application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostTransactionApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the application context starts without errors
    }
}
