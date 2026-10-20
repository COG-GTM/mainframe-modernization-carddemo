package com.carddemo.batch.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BatchConfigTest {

    @Test
    void defaultInputDirFindsAsciiDataAboveWorkingDirectory() {
        Path dir = BatchConfig.defaultInputDir();
        assertTrue(dir.endsWith(BatchConfig.ASCII_DATA_DIR), dir.toString());
        for (CardDemoFile file : CardDemoFile.values()) {
            assertTrue(Files.isRegularFile(dir.resolve(file.fileName())), file.fileName());
        }
    }

    @Test
    void systemPropertiesOverrideDefaults() {
        System.setProperty(BatchConfig.INPUT_DIR_PROPERTY, "/tmp/in");
        System.setProperty(BatchConfig.OUTPUT_DIR_PROPERTY, "/tmp/out");
        try {
            BatchConfig config = BatchConfig.fromEnvironment();
            assertEquals(Path.of("/tmp/in/acctdata.txt"), config.input(CardDemoFile.ACCTDATA));
            assertEquals(Path.of("/tmp/out/tranfile.txt"), config.output("tranfile.txt"));
        } finally {
            System.clearProperty(BatchConfig.INPUT_DIR_PROPERTY);
            System.clearProperty(BatchConfig.OUTPUT_DIR_PROPERTY);
        }
    }
}
