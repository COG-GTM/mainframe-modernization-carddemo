package com.carddemo.posttran;

import com.carddemo.posttran.domain.DalytranRecord;
import com.carddemo.posttran.io.FixedWidthFiles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Runs the migrated step over one input set and exposes its outputs, so tests can assert on the
 * same six datasets the legacy harness captures.
 *
 * <p>Input staging mirrors {@code harness/java_run.sh}: records are padded to the copybook length
 * (the drop's ASCII {@code cardxref.txt} is truncated to 36 of its 50 bytes, see the Phase 1
 * report) and the two updated-in-place datasets are copied so the source tree is never written to.
 */
final class StepRun {

    static final Path TESTDATA = Path.of("..", "testdata");

    private final Path out;
    final int returnCode;
    final List<String> sysout;

    private StepRun(Path out, int returnCode, List<String> sysout) {
        this.out = out;
        this.returnCode = returnCode;
        this.sysout = sysout;
    }

    /**
     * Copies {@code inputs} into {@code work} at full copybook width and returns them as DD paths.
     * ACCTFILE and TCATBALF are updated in place by the step, so it never runs on the source tree.
     */
    static DdPaths stageInputs(Path inputs, Path work) {
        Path data = work.resolve("data");
        mkdirs(data);
        stage(inputs.resolve("dalytran.txt"), data.resolve("DALYTRAN"), 350);
        stage(inputs.resolve("cardxref.txt"), data.resolve("XREFFILE"), 50);
        stage(inputs.resolve("acctdata.txt"), data.resolve("ACCTFILE"), 300);
        stage(inputs.resolve("tcatbal.txt"), data.resolve("TCATBALF"), 50);
        return new DdPaths(
                data.resolve("DALYTRAN"), data.resolve("TRANFILE"), data.resolve("XREFFILE"),
                data.resolve("DALYREJS"), data.resolve("ACCTFILE"), data.resolve("TCATBALF"));
    }

    /** Runs {@code inputs} into {@code work} with the processing clock pinned. */
    static StepRun execute(Path inputs, Path work) {
        DdPaths dd = stageInputs(inputs, work);

        Clock fixed = Clock.fixed(Instant.parse("2024-03-01T12:34:56.780Z"), ZoneOffset.UTC);
        TransactionPoster poster = new TransactionPoster(dd, new Db2Timestamp(fixed));
        poster.openFiles();
        for (String record : FixedWidthFiles.read(dd.dalytran(), DalytranRecord.LENGTH)) {
            poster.processTransaction(new DalytranRecord(record));
        }
        int rc = poster.closeFiles();
        return new StepRun(work.resolve("data"), rc, poster.sysout());
    }

    List<String> tranfile() {
        return FixedWidthFiles.read(out.resolve("TRANFILE"), 350);
    }

    List<String> acctfile() {
        return FixedWidthFiles.read(out.resolve("ACCTFILE"), 300);
    }

    List<String> tcatbalf() {
        return FixedWidthFiles.read(out.resolve("TCATBALF"), 50);
    }

    List<String> dalyrejs() {
        return FixedWidthFiles.read(out.resolve("DALYREJS"), 430);
    }

    private static void stage(Path source, Path target, int length) {
        FixedWidthFiles.write(target, pad(source, length));
    }

    /**
     * The drop's ASCII files are newline-delimited and not all of them are full width — the
     * conversion truncated {@code cardxref.txt} to 36 of its 50 bytes — so short lines are padded
     * back out to the copybook length, exactly as the shell harness does.
     */
    static List<String> pad(Path source, int length) {
        String text;
        try {
            text = Files.readString(source, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (text.indexOf('\n') < 0) {
            return FixedWidthFiles.read(source, length);
        }
        return text.lines()
                .map(line -> line.length() >= length ? line.substring(0, length)
                        : line + " ".repeat(length - line.length()))
                .toList();
    }

    private static void mkdirs(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<String> baseline(String name, String dataset, int length) {
        return FixedWidthFiles.read(TESTDATA.resolve("baseline").resolve(name).resolve(dataset), length);
    }

    static List<String> baselineSysout(String name) {
        try {
            return Files.readAllLines(
                    TESTDATA.resolve("baseline").resolve(name).resolve("sysout.txt"),
                    StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static int baselineReturnCode(String name) {
        try {
            return Integer.parseInt(Files.readString(
                    TESTDATA.resolve("baseline").resolve(name).resolve("rc.txt")).trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
