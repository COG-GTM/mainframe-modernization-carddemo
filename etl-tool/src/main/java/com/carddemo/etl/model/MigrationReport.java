package com.carddemo.etl.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects migration statistics and validation results for reporting.
 */
public class MigrationReport {

    private final Map<String, FileStats> fileStats = new LinkedHashMap<>();
    private final List<String> xrefErrors = new ArrayList<>();
    private boolean xrefValidationPassed = true;

    public void addFileStats(String fileName, int sourceRecords, int loadedRecords, int errorCount,
                             List<String> errors) {
        fileStats.put(fileName, new FileStats(sourceRecords, loadedRecords, errorCount, errors));
    }

    public void addXrefError(String error) {
        xrefErrors.add(error);
        xrefValidationPassed = false;
    }

    public Map<String, FileStats> getFileStats() {
        return fileStats;
    }

    public List<String> getXrefErrors() {
        return xrefErrors;
    }

    public boolean isXrefValidationPassed() {
        return xrefValidationPassed;
    }

    public boolean isFullySuccessful() {
        if (!xrefValidationPassed) {
            return false;
        }
        for (FileStats stats : fileStats.values()) {
            if (stats.errorCount() > 0 || stats.sourceRecords() != stats.loadedRecords()) {
                return false;
            }
        }
        return true;
    }

    public record FileStats(int sourceRecords, int loadedRecords, int errorCount, List<String> errors) {
    }
}
