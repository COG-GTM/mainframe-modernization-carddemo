package com.carddemo.online.report;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * COBOL program: CORPT00C — wiring for the internal reader replacement.
 */
@Configuration
public class TransactionReportJobConfiguration {

    @Bean
    @ConditionalOnMissingBean(TransactionReportJobSubmitter.class)
    public TransactionReportJobSubmitter transactionReportJobSubmitter() {
        return new RecordingTransactionReportJobSubmitter();
    }
}
