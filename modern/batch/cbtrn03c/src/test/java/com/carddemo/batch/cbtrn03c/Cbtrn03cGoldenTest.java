package com.carddemo.batch.cbtrn03c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.record.TranRecord;
import com.carddemo.batch.core.report.ReportAccountTotals;
import com.carddemo.batch.core.report.ReportGrandTotals;
import com.carddemo.batch.core.report.ReportLines;
import com.carddemo.batch.core.report.ReportPageTotals;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Golden tests for {@link Cbtrn03cJob}. The expected reports under {@code golden/} were produced by
 * running the original {@code app/cbl/CBTRN03C.cbl} under GnuCOBOL on the same fixtures
 * ({@code cobol-crosscheck/crosscheck.sh}) and hand-checked against the COBOL control flow.
 */
class Cbtrn03cGoldenTest {

    private static final PrintStream QUIET = new PrintStream(OutputStream.nullOutputStream());
    private static final int LINE = ReportLines.LENGTH + 1;

    @TempDir
    Path out;

    @Test
    void multiPageReportMatchesGolden() throws Exception {
        assertArrayEquals(golden("multipage"), run("multipage"));
    }

    @Test
    void multiPageReportFollowsCobolTotalsAndPaging() throws Exception {
        List<String> lines = lines(run("multipage"));
        List<TranRecord> trans = SequentialFileReader.readAll(
                fixture("multipage/tranfile.txt"), TranRecord.LENGTH, TranRecord::fromBytes);

        assertEquals(46, trans.size());
        assertEquals(46 + 4 * 3 + 2 * 3 + 2 * 3 + 1, lines.size());
        assertEquals(3, count(lines, "DALYREPT"), "first-time headers + 2 page breaks");
        assertEquals(3, count(lines, ReportPageTotals.LABEL), "2 page breaks + EOF page total");
        assertEquals(3, count(lines, ReportAccountTotals.LABEL), "4 cards, no account total at EOF");
        assertEquals(1, count(lines, ReportGrandTotals.LABEL));
        assertTrue(lines.get(lines.size() - 1).startsWith(ReportGrandTotals.LABEL));

        // Page breaks happen when WS-LINE-COUNTER is 20 and 60; 40 is skipped by an account total.
        assertTrue(lines.get(20).startsWith(ReportPageTotals.LABEL));
        assertTrue(lines.get(60).startsWith(ReportPageTotals.LABEL));

        BigDecimal sum = trans.stream().map(TranRecord::amt).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal last = trans.get(trans.size() - 1).amt();
        BigDecimal pages = BigDecimal.ZERO;
        for (String line : lines) {
            if (line.startsWith(ReportPageTotals.LABEL)) {
                pages = pages.add(ReportPageTotals.fromBytes(bytes(line)).total());
            }
        }
        BigDecimal grand = ReportGrandTotals.fromBytes(bytes(lines.get(lines.size() - 1))).total();
        assertEquals(sum.add(last), grand, "EOF branch adds the last TRAN-AMT a second time");
        assertEquals(grand, pages);
        assertEquals(new BigDecimal("1252119.68"), grand);
    }

    @Test
    void outOfRangeRecordEndsTheReportWithoutTotals() throws Exception {
        byte[] report = run("out-of-range");
        assertArrayEquals(golden("out-of-range"), report);
        List<String> lines = lines(report);
        assertEquals(4 + 3, lines.size(), "headers + the 3 in-range records before the 2022-05-31 one");
        assertEquals(0, count(lines, ReportPageTotals.LABEL));
        assertEquals(0, count(lines, ReportGrandTotals.LABEL));
    }

    @Test
    void leadingOutOfRangeRecordGivesEmptyReport() throws Exception {
        byte[] report = run("leading-out-of-range");
        assertArrayEquals(golden("leading-out-of-range"), report);
        assertEquals(0, report.length);
    }

    @Test
    void emptyDateParmGivesEmptyReport() throws Exception {
        Path dateParm = Files.write(out.resolve("dateparm.txt"), new byte[0]);
        assertEquals(0, run(fixture("multipage/tranfile.txt"), dateParm).length);
    }

    @Test
    void emptyTranFileGivesEmptyReport() throws Exception {
        Path tran = Files.write(out.resolve("tranfile.txt"), new byte[0]);
        assertEquals(0, run(tran, fixture("multipage/dateparm.txt")).length);
    }

    @Test
    void unknownCardNumberAbends() throws Exception {
        Path tran = rewriteFirstRecord(262, "9999999999999999");
        Cbtrn03cAbend abend = assertThrows(Cbtrn03cAbend.class,
                () -> run(tran, fixture("multipage/dateparm.txt")));
        assertTrue(abend.getMessage().startsWith("INVALID CARD NUMBER : 9999999999999999"));
        assertEquals(999, abend.abendCode());
    }

    @Test
    void unknownTranCategoryAbends() throws Exception {
        Path tran = rewriteFirstRecord(18, "0099");
        Cbtrn03cAbend abend = assertThrows(Cbtrn03cAbend.class,
                () -> run(tran, fixture("multipage/dateparm.txt")));
        assertTrue(abend.getMessage().startsWith("INVALID TRAN CATG KEY : 010099"));
    }

    @Test
    void fromConfigUsesArgsForTranFileAndDateParm() throws Exception {
        Path inputs = fixture("cardxref.txt").getParent();
        BatchConfig config = new BatchConfig(inputs, out);
        Cbtrn03cJob job = Cbtrn03cJob.fromConfig(config, new String[] {
                fixture("multipage/tranfile.txt").toString(), fixture("multipage/dateparm.txt").toString()}, QUIET);
        job.run();
        assertEquals(out.resolve(Cbtrn03cJob.REPORT_FILE), job.reportFile());
        assertArrayEquals(golden("multipage"), Files.readAllBytes(job.reportFile()));
    }

    private byte[] run(String scenario) throws Exception {
        return run(fixture(scenario + "/tranfile.txt"), fixture(scenario + "/dateparm.txt"));
    }

    private byte[] run(Path tranFile, Path dateParm) throws Exception {
        Path report = out.resolve("report").resolve(Cbtrn03cJob.REPORT_FILE);
        new Cbtrn03cJob(tranFile, fixture("cardxref.txt"), fixture("trantype.txt"), fixture("trancatg.txt"),
                dateParm, report, QUIET).run();
        return Files.readAllBytes(report);
    }

    private Path rewriteFirstRecord(int offset, String value) throws Exception {
        byte[] data = Files.readAllBytes(fixture("multipage/tranfile.txt"));
        System.arraycopy(FixedWidth.encode(value), 0, data, offset, value.length());
        return Files.write(out.resolve("tranfile.txt"), data);
    }

    private static byte[] golden(String scenario) throws Exception {
        return Files.readAllBytes(resource("golden/" + scenario + "-tranrept.txt"));
    }

    private static Path fixture(String name) throws URISyntaxException {
        return resource("fixtures/" + name);
    }

    private static Path resource(String name) throws URISyntaxException {
        return Paths.get(Cbtrn03cGoldenTest.class.getClassLoader().getResource(name).toURI());
    }

    private static List<String> lines(byte[] report) {
        assertEquals(0, report.length % LINE, "every record is 133 bytes + newline");
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < report.length; i += LINE) {
            assertEquals('\n', report[i + ReportLines.LENGTH]);
            lines.add(FixedWidth.decode(java.util.Arrays.copyOfRange(report, i, i + ReportLines.LENGTH)));
        }
        return lines;
    }

    private static byte[] bytes(String line) {
        return FixedWidth.encode(line);
    }

    private static long count(List<String> lines, String prefix) {
        return lines.stream().filter(l -> l.startsWith(prefix)).count();
    }
}
