package com.carddemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test verifying the Spring Boot application context loads correctly.
 *
 * Uses an in-memory H2 database and disables Kafka auto-configuration
 * so the test can run without external infrastructure.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration,io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration,io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration,io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration",
    "spring.datasource.url=jdbc:h2:mem:carddemo-test",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CardDemoApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the application context starts without errors
    }
}
