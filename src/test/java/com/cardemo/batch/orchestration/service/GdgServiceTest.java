package com.cardemo.batch.orchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GdgService (Generation Data Group).
 */
class GdgServiceTest {

    @TempDir
    Path tempDir;

    private GdgService gdgService;

    @BeforeEach
    void setUp() {
        gdgService = new GdgService(tempDir.toString());
    }

    @Test
    void createNewGeneration_createsFile() throws IOException {
        Path path = gdgService.createNewGeneration("DALYREJS", "dat");
        assertNotNull(path);
        assertTrue(path.getFileName().toString().startsWith("DALYREJS."));
        assertTrue(path.getFileName().toString().endsWith(".dat"));
        assertTrue(Files.exists(path.getParent()));
    }

    @Test
    void createNewGeneration_createsDirectoryIfNotExists() throws IOException {
        Path path = gdgService.createNewGeneration("NEWDATASET", "csv");
        assertTrue(Files.exists(path.getParent()));
        assertEquals("NEWDATASET", path.getParent().getFileName().toString());
    }

    @Test
    void getLatestGeneration_returnsNewestFile() throws IOException, InterruptedException {
        // Create multiple generations with slight delay to ensure different timestamps
        Path first = gdgService.createNewGeneration("SYSTRAN", "dat");
        Files.writeString(first, "first");
        Thread.sleep(1100); // Ensure different second-level timestamp
        Path second = gdgService.createNewGeneration("SYSTRAN", "dat");
        Files.writeString(second, "second");

        Path latest = gdgService.getLatestGeneration("SYSTRAN");
        assertEquals("second", Files.readString(latest));
    }

    @Test
    void getLatestGeneration_throwsWhenNoGenerations() {
        assertThrows(IOException.class, () ->
                gdgService.getLatestGeneration("NONEXISTENT"));
    }

    @Test
    void listGenerations_returnsAllGenerations() throws IOException, InterruptedException {
        // Create two generations with unique filenames by using a manual file
        // alongside the service-generated one, or by waiting for a distinct timestamp
        Path dir = Path.of(tempDir.toString(), "STATEMNT");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("STATEMNT.20260101-120000.html"), "gen1");
        Files.writeString(dir.resolve("STATEMNT.20260101-120001.html"), "gen2");

        List<Path> generations = gdgService.listGenerations("STATEMNT");
        assertEquals(2, generations.size());
    }

    @Test
    void listGenerations_emptyForNonexistentDataset() throws IOException {
        List<Path> generations = gdgService.listGenerations("DOESNOTEXIST");
        assertTrue(generations.isEmpty());
    }

    @Test
    void getBaseOutputDir_returnsConfiguredPath() {
        assertEquals(tempDir.toString(), gdgService.getBaseOutputDir());
    }
}
