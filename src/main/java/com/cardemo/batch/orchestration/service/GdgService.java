package com.cardemo.batch.orchestration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Generation Data Group (GDG) service.
 * Replaces the mainframe GDG pattern (DSN=dataset(+1)) with timestamped
 * output files or versioned storage for batch output management.
 *
 * <p>JCL GDG references:
 * <ul>
 *   <li>(+1) = Create new generation</li>
 *   <li>(0) = Current/latest generation</li>
 *   <li>(-1) = Previous generation</li>
 * </ul>
 */
@Service
public class GdgService {

    private static final Logger log = LoggerFactory.getLogger(GdgService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

    private final String baseOutputDir;

    public GdgService(@Value("${cardemo.batch.output.dir:./batch-output}") String baseOutputDir) {
        this.baseOutputDir = baseOutputDir;
    }

    /**
     * Creates a new generation path for the given dataset name.
     * Replaces JCL DSN=dataset(+1) pattern.
     *
     * @param datasetName the logical dataset name (e.g., "DALYREJS", "SYSTRAN", "STATEMNT")
     * @param extension   the file extension (e.g., "dat", "html", "txt")
     * @return the path to the new generation file
     */
    public Path createNewGeneration(String datasetName, String extension) throws IOException {
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());
        Path dir = Path.of(baseOutputDir, datasetName);
        Files.createDirectories(dir);

        String fileName = datasetName + "." + timestamp + "." + extension;
        Path filePath = dir.resolve(fileName);
        log.info("GDG: Created new generation for {}: {}", datasetName, filePath);
        return filePath;
    }

    /**
     * Gets the latest generation for a dataset.
     * Replaces JCL DSN=dataset(0) pattern.
     */
    public Path getLatestGeneration(String datasetName) throws IOException {
        Path dir = Path.of(baseOutputDir, datasetName);
        if (!Files.exists(dir)) {
            throw new IOException("No generations found for dataset: " + datasetName);
        }

        return Files.list(dir)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().startsWith(datasetName + "."))
                .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                .findFirst()
                .orElseThrow(() -> new IOException("No generations found for dataset: " + datasetName));
    }

    /**
     * Lists all generations for a dataset.
     */
    public java.util.List<Path> listGenerations(String datasetName) throws IOException {
        Path dir = Path.of(baseOutputDir, datasetName);
        if (!Files.exists(dir)) {
            return java.util.List.of();
        }

        return Files.list(dir)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().startsWith(datasetName + "."))
                .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                .toList();
    }

    public String getBaseOutputDir() {
        return baseOutputDir;
    }
}
