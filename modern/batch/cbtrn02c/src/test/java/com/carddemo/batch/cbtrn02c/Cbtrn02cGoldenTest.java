package com.carddemo.batch.cbtrn02c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.record.RejectRecord;
import com.carddemo.batch.core.record.TranRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Golden tests for {@link Cbtrn02cJob}.
 *
 * <p>The expected files under {@code src/test/resources/golden/<case>/} were produced by compiling
 * and running the original {@code app/cbl/CBTRN02C.cbl} with GnuCOBOL against the same inputs
 * ({@code src/test/cobol/regen-golden.sh}), with FUNCTION CURRENT-DATE pinned to {@link #CLOCK}.
 * Case {@code full} uses {@code app/data/ASCII}; the others use the hand-built fixtures under
 * {@code src/test/resources/fixtures/<case>/}.
 */
class Cbtrn02cGoldenTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2022-06-11T03:15:30.45Z"), ZoneOffset.UTC);

    private static final List<CardDemoFile> INPUTS = List.of(
            CardDemoFile.DAILYTRAN, CardDemoFile.CARDXREF, CardDemoFile.ACCTDATA, CardDemoFile.TCATBAL);
    private static final List<String> OUTPUTS = List.of(
            Cbtrn02cJob.TRANFILE, Cbtrn02cJob.DALYREJS, Cbtrn02cJob.ACCTFILE, Cbtrn02cJob.TCATBALF);

    @TempDir
    Path tmp;

    @ParameterizedTest
    @ValueSource(strings = {"full", "clean", "reject100", "reject101", "reject102", "reject103", "overlimit-and-expired"})
    void outputsMatchCobolGolden(String caseName) throws Exception {
        Path source = inputDir(caseName);
        Path in = copyInputs(source);
        Path out = tmp.resolve("out");
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();

        int rc = new Cbtrn02cJob(new BatchConfig(in, out), CLOCK, new PrintStream(sysout, true, StandardCharsets.ISO_8859_1)).run();

        Path golden = resource("golden/" + caseName);
        assertEquals(Integer.parseInt(Files.readString(golden.resolve("rc.txt")).trim()), rc, "return code");
        for (String name : OUTPUTS) {
            assertArrayEquals(Files.readAllBytes(golden.resolve(name)), Files.readAllBytes(out.resolve(name)), name);
        }
        assertEquals(Files.readString(golden.resolve("sysout.txt"), StandardCharsets.ISO_8859_1),
                sysout.toString(StandardCharsets.ISO_8859_1), "DISPLAY output");
        for (CardDemoFile file : INPUTS) {
            assertArrayEquals(Files.readAllBytes(source.resolve(file.fileName())),
                    Files.readAllBytes(in.resolve(file.fileName())), "input modified: " + file.fileName());
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "clean                 | 4 | 0 | ''",
        "reject100             | 2 | 4 | 100:INVALID CARD NUMBER FOUND",
        "reject101             | 2 | 4 | 101:ACCOUNT RECORD NOT FOUND",
        "reject102             | 2 | 4 | 102:OVERLIMIT TRANSACTION",
        "reject103             | 2 | 4 | 103:TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
        "overlimit-and-expired | 2 | 4 | 103:TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
    })
    void rejectReasons(String caseName, long processed, int expectedRc, String expectedRejects) throws Exception {
        Path in = copyInputs(inputDir(caseName));
        Path out = tmp.resolve("out");
        Cbtrn02cJob job = new Cbtrn02cJob(new BatchConfig(in, out), CLOCK, new PrintStream(new ByteArrayOutputStream()));

        assertEquals(expectedRc, job.run());
        assertEquals(processed, job.transactionCount());
        String rejects = SequentialFileReader.readAll(out.resolve(Cbtrn02cJob.DALYREJS), RejectRecord.LENGTH, RejectRecord::fromBytes)
                .stream()
                .map(r -> r.validationFailReason() + ":" + r.validationFailReasonDesc().strip())
                .collect(Collectors.joining(","));
        assertEquals(expectedRejects, rejects);
        assertEquals(expectedRejects.isEmpty() ? 0 : expectedRejects.split(",").length, job.rejectCount());
    }

    @Test
    void refusesToOverwriteInputs() throws Exception {
        Path in = copyInputs(inputDir("clean"));
        byte[] before = Files.readAllBytes(in.resolve(Cbtrn02cJob.ACCTFILE));

        Cbtrn02cJob job = new Cbtrn02cJob(new BatchConfig(in, in), CLOCK, new PrintStream(new ByteArrayOutputStream()));

        assertThrows(IllegalStateException.class, job::run);
        assertArrayEquals(before, Files.readAllBytes(in.resolve(Cbtrn02cJob.ACCTFILE)));
    }

    @Test
    void abendKeepsPostingsMadeBeforeIt() throws Exception {
        Path in = copyInputs(inputDir("clean"));
        Path daily = in.resolve(CardDemoFile.DAILYTRAN.fileName());
        String first = Files.readAllLines(daily, StandardCharsets.ISO_8859_1).get(0);
        Files.writeString(daily, first + "\n" + first + "\n", StandardCharsets.ISO_8859_1);
        Path out = tmp.resolve("out");
        Cbtrn02cJob job = new Cbtrn02cJob(new BatchConfig(in, out), CLOCK, new PrintStream(new ByteArrayOutputStream()));

        assertThrows(Cbtrn02cJob.Cbtrn02cAbend.class, job::run);

        List<TranRecord> posted = SequentialFileReader.readAll(out.resolve(Cbtrn02cJob.TRANFILE), TranRecord.LENGTH, TranRecord::fromBytes);
        assertEquals(1, posted.size());
        assertEquals(first.substring(0, 16), posted.get(0).id());
        assertNotEquals(Files.readString(in.resolve(Cbtrn02cJob.ACCTFILE)), Files.readString(out.resolve(Cbtrn02cJob.ACCTFILE)));
    }

    @Test
    void repeatedRunsDoNotAccumulateCounts() throws Exception {
        Path in = copyInputs(inputDir("reject102"));
        Path out = tmp.resolve("out");
        Cbtrn02cJob job = new Cbtrn02cJob(new BatchConfig(in, out), CLOCK, new PrintStream(new ByteArrayOutputStream()));
        job.run();
        long processed = job.transactionCount();
        long rejected = job.rejectCount();

        job.run();

        assertEquals(processed, job.transactionCount());
        assertEquals(rejected, job.rejectCount());
    }

    @Test
    void missingReferenceFileFailsWithoutWritingOutputs() throws Exception {
        Path in = copyInputs(inputDir("clean"));
        Files.delete(in.resolve(CardDemoFile.CARDXREF.fileName()));
        Path out = tmp.resolve("out");
        Cbtrn02cJob job = new Cbtrn02cJob(new BatchConfig(in, out), CLOCK, new PrintStream(new ByteArrayOutputStream()));

        assertThrows(RuntimeException.class, job::run);
        assertFalse(Files.exists(out.resolve(Cbtrn02cJob.TRANFILE)));
    }

    private static Path inputDir(String caseName) throws URISyntaxException {
        return caseName.equals("full") ? BatchConfig.defaultInputDir() : resource("fixtures/" + caseName);
    }

    private Path copyInputs(Path source) throws IOException {
        Path in = Files.createDirectories(tmp.resolve("in"));
        for (CardDemoFile file : INPUTS) {
            Files.copy(source.resolve(file.fileName()), in.resolve(file.fileName()));
        }
        return in;
    }

    private static Path resource(String name) throws URISyntaxException {
        return Path.of(Cbtrn02cGoldenTest.class.getClassLoader().getResource(name).toURI());
    }
}
