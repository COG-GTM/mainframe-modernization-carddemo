package com.carddemo.batch.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Input/output directories for a batch job.
 *
 * <p>Resolution order for each directory: system property ({@code carddemo.inputDir} /
 * {@code carddemo.outputDir}), then environment variable ({@code CARDDEMO_INPUT_DIR} /
 * {@code CARDDEMO_OUTPUT_DIR}), then the default: the nearest {@code app/data/ASCII} found by
 * walking up from the working directory for inputs, and {@code target/batch-output} for outputs.
 */
public record BatchConfig(Path inputDir, Path outputDir) {

    public static final String INPUT_DIR_PROPERTY = "carddemo.inputDir";
    public static final String OUTPUT_DIR_PROPERTY = "carddemo.outputDir";
    public static final String INPUT_DIR_ENV = "CARDDEMO_INPUT_DIR";
    public static final String OUTPUT_DIR_ENV = "CARDDEMO_OUTPUT_DIR";
    public static final Path ASCII_DATA_DIR = Paths.get("app", "data", "ASCII");

    public BatchConfig {
        inputDir = inputDir.toAbsolutePath().normalize();
        outputDir = outputDir.toAbsolutePath().normalize();
    }

    public static BatchConfig fromEnvironment() {
        return new BatchConfig(
                resolve(INPUT_DIR_PROPERTY, INPUT_DIR_ENV, defaultInputDir()),
                resolve(OUTPUT_DIR_PROPERTY, OUTPUT_DIR_ENV, defaultOutputDir()));
    }

    /** Nearest {@code app/data/ASCII} at or above the working directory. */
    public static Path defaultInputDir() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve(ASCII_DATA_DIR);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return ASCII_DATA_DIR.toAbsolutePath();
    }

    public static Path defaultOutputDir() {
        return Paths.get("target", "batch-output").toAbsolutePath();
    }

    public Path input(String fileName) {
        return inputDir.resolve(fileName);
    }

    public Path input(CardDemoFile file) {
        return input(file.fileName());
    }

    public Path output(String fileName) {
        return outputDir.resolve(fileName);
    }

    private static Path resolve(String property, String env, Path fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(env);
        }
        return value == null || value.isBlank() ? fallback : Paths.get(value);
    }
}
