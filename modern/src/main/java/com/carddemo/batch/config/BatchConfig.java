package com.carddemo.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration.
 *
 * COBOL Traceability: Replaces the JCL job definitions (POSTTRAN, INTCALC,
 * CREASTMT) that orchestrate batch COBOL programs CBTRN01C, CBTRN02C,
 * CBTRN03C, and CBACT04C.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
}
