package com.carddemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the Spring application context loads (all modules wire together,
 * Flyway migrates, datasource / JPA / security / batch autoconfiguration succeeds).
 */
@SpringBootTest
@ActiveProfiles("test")
class CardDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
