package com.cog.carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Compares the report of {@link Cbact01c} against the output of the COBOL program
 * {@code app/cbl/CBACT01C.cbl} run over {@code app/data/ASCII/acctdata.txt}.
 *
 * <p>The baseline in {@code golden/CBACT01C.expected.txt} is produced by
 * {@code modern/tools/generate_golden.sh}, which compiles the COBOL source itself.
 */
class Cbact01cEquivalenceTest {

    private static final String GOLDEN = "/golden/CBACT01C.expected.txt";

    @Test
    void reportMatchesTheCobolProgramLineForLine() throws IOException {
        List<String> expected = goldenLines();

        List<String> actual = runProgram(SampleData.accountFile()).lines().toList();

        assertIterableEquals(expected, actual);
    }

    /** The load bearing assertion: line endings and trailing blanks included. */
    @Test
    void reportIsByteForByteIdenticalToTheCobolOutput() throws IOException {
        String expected = String.join("\n", goldenLines()) + "\n";

        assertEquals(expected, runProgram(SampleData.accountFile()));
    }

    @Test
    void everyAccountOfTheSampleDataIsReported() throws IOException {
        String report = runProgram(SampleData.accountFile());

        assertTrue(report.startsWith("START OF EXECUTION OF PROGRAM CBACT01C\n"));
        assertTrue(report.endsWith("END OF EXECUTION OF PROGRAM CBACT01C\n"));
        assertEquals(50, report.lines().filter(line -> line.startsWith("ACCT-ID  ")).count());
    }

    @Test
    void abendsLikeTheCobolProgramWhenTheDatasetIsMissing(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("acctdata.txt");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.ISO_8859_1);

        AbendException abend = assertThrows(AbendException.class, () -> Cbact01c.run(missing, out));

        assertEquals("35", abend.fileStatus());
        assertTrue(buffer.toString(StandardCharsets.ISO_8859_1).contains("ERROR OPENING ACCTFILE\n"));
        assertTrue(buffer.toString(StandardCharsets.ISO_8859_1).endsWith("ABENDING PROGRAM\n"));
    }

    private static String runProgram(Path accountFile) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(buffer, true, StandardCharsets.ISO_8859_1)) {
            Cbact01c.run(accountFile, out);
        }
        return buffer.toString(StandardCharsets.ISO_8859_1);
    }

    private static List<String> goldenLines() throws IOException {
        try (InputStream in = Cbact01cEquivalenceTest.class.getResourceAsStream(GOLDEN)) {
            if (in == null) {
                throw new IllegalStateException("missing test resource " + GOLDEN);
            }
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1).lines().toList();
        }
    }
}
