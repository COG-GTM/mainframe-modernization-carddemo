package com.carddemo.etl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the ETL migration tool.
 * Maps to the "etl" prefix in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "etl")
public class EtlProperties {

    private String sourceDir = "app/data/EBCDIC";
    private int batchSize = 100;
    private boolean truncateBeforeLoad = true;
    private boolean validateXref = true;

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isTruncateBeforeLoad() {
        return truncateBeforeLoad;
    }

    public void setTruncateBeforeLoad(boolean truncateBeforeLoad) {
        this.truncateBeforeLoad = truncateBeforeLoad;
    }

    public boolean isValidateXref() {
        return validateXref;
    }

    public void setValidateXref(boolean validateXref) {
        this.validateXref = validateXref;
    }
}
