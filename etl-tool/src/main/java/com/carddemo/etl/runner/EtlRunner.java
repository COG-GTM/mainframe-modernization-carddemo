package com.carddemo.etl.runner;

import com.carddemo.etl.model.MigrationReport;
import com.carddemo.etl.service.EtlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring Boot CommandLineRunner that triggers the ETL migration on application startup.
 * Exit code 0 = success, 1 = completed with issues.
 */
@Component
public class EtlRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EtlRunner.class);

    private final EtlService etlService;

    public EtlRunner(EtlService etlService) {
        this.etlService = etlService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("CardDemo EBCDIC ETL Migration Tool starting...");

        MigrationReport report = etlService.runMigration();

        if (report.isFullySuccessful()) {
            log.info("Migration completed successfully.");
        } else {
            log.warn("Migration completed with issues. Review the report above for details.");
        }
    }
}
