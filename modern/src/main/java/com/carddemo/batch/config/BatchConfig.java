package com.carddemo.batch.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration.
 *
 * COBOL Traceability: Replaces the JCL job definitions (POSTTRAN, INTCALC,
 * CREASTMT) that orchestrate batch COBOL programs CBTRN01C, CBTRN02C,
 * CBTRN03C, and CBACT04C.
 *
 * Note: @EnableBatchProcessing is intentionally NOT used here. In Spring Boot 3.x,
 * it disables BatchAutoConfiguration, preventing spring.batch.jdbc.initialize-schema
 * from creating batch metadata tables. Spring Boot 3.x auto-configures batch without it.
 */
@Configuration
public class BatchConfig {
}
