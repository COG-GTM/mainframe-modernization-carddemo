package com.cardemo.migration.validation;

import com.cardemo.migration.config.MigrationConfig;
import com.cardemo.migration.model.ValidationReport;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.model.VsamFileDescriptor;
import com.cardemo.migration.reader.EbcdicFileReader;
import com.cardemo.migration.service.BatchOutputValidator;
import com.cardemo.migration.service.CrossReferenceValidator;
import com.cardemo.migration.service.FieldLevelValidator;
import com.cardemo.migration.service.RecordCountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * Orchestrates all migration validation checks and produces a comprehensive report.
 * Coordinates record count validation, field-level spot checks, cross-reference
 * integrity verification, and batch output comparison.
 */
@Service
public class MigrationValidationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(MigrationValidationOrchestrator.class);

    private final MigrationConfig config;
    private final JdbcTemplate jdbcTemplate;

    public MigrationValidationOrchestrator(MigrationConfig config, JdbcTemplate jdbcTemplate) {
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Runs all validation checks and returns a comprehensive report.
     *
     * @return the full validation report
     */
    public ValidationReport runFullValidation() {
        ValidationReport report = new ValidationReport("CardDemo Migration Validation");
        Path dataDir = Path.of(config.getEbcdic().getDataDir());
        EbcdicFileReader fileReader = new EbcdicFileReader(dataDir);

        log.info("Starting full migration validation from EBCDIC dir: {}", dataDir);

        // Phase 1: Record Count Verification
        log.info("Phase 1: Record Count Verification");
        runRecordCountValidation(report, fileReader);

        // Phase 2: Field-Level Spot Checks
        log.info("Phase 2: Field-Level Spot Checks");
        runFieldLevelValidation(report, fileReader);

        // Phase 3: Cross-Reference Integrity
        log.info("Phase 3: Cross-Reference Integrity");
        runCrossReferenceValidation(report, fileReader);

        report.complete();
        log.info("Validation complete: {}", report.getSummary());

        return report;
    }

    /**
     * Runs only record count validation for all 12 VSAM files.
     */
    public ValidationReport runRecordCountValidation() {
        ValidationReport report = new ValidationReport("Record Count Validation");
        Path dataDir = Path.of(config.getEbcdic().getDataDir());
        EbcdicFileReader fileReader = new EbcdicFileReader(dataDir);
        runRecordCountValidation(report, fileReader);
        report.complete();
        return report;
    }

    /**
     * Runs only cross-reference integrity validation.
     */
    public ValidationReport runCrossReferenceValidation() {
        ValidationReport report = new ValidationReport("Cross-Reference Validation");
        Path dataDir = Path.of(config.getEbcdic().getDataDir());
        EbcdicFileReader fileReader = new EbcdicFileReader(dataDir);
        runCrossReferenceValidation(report, fileReader);
        report.complete();
        return report;
    }

    private void runRecordCountValidation(ValidationReport report, EbcdicFileReader fileReader) {
        RecordCountValidator validator = new RecordCountValidator(fileReader, jdbcTemplate);
        for (VsamFileDescriptor descriptor : VsamFileDescriptor.ALL) {
            ValidationResult result = validator.validate(descriptor);
            report.addResult(result);
        }
    }

    private void runFieldLevelValidation(ValidationReport report, EbcdicFileReader fileReader) {
        int spotCheckCount = config.getValidation().getSpotCheckCount();
        FieldLevelValidator validator = new FieldLevelValidator(
                fileReader, jdbcTemplate, spotCheckCount);
        for (VsamFileDescriptor descriptor : VsamFileDescriptor.ALL) {
            ValidationResult result = validator.validate(descriptor);
            report.addResult(result);
        }
    }

    private void runCrossReferenceValidation(ValidationReport report, EbcdicFileReader fileReader) {
        CrossReferenceValidator validator = new CrossReferenceValidator(fileReader, jdbcTemplate);

        report.addResult(validator.validateCardToAccount());
        report.addResult(validator.validateAccountToCustomer());
        report.addResult(validator.validateFullChain());
        report.addResult(validator.validateDatabaseForeignKeys());
    }

    /**
     * Creates a BatchOutputValidator for comparing batch job outputs.
     */
    public BatchOutputValidator createBatchOutputValidator() {
        return new BatchOutputValidator();
    }
}
