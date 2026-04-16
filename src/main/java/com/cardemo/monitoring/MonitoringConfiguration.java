package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for CardDemo operational monitoring.
 * Configures Micrometer registry with common tags and Prometheus export.
 */
@Configuration
public class MonitoringConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "carddemo",
                        "modernization", "cobol-to-java"
                );
    }
}
