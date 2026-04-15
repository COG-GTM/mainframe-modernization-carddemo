package com.carddemo.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Base batch configuration enabling Spring Batch infrastructure.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
}
