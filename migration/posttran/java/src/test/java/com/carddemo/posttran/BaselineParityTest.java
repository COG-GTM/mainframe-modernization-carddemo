package com.carddemo.posttran;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares the migrated step against the recorded output of the legacy program itself.
 *
 * <p>The baselines under {@code testdata/baseline} were produced by compiling the untouched
 * {@code app/cbl/CBTRN02C.cbl} with GnuCOBOL and running it over the same inputs
 * ({@code harness/legacy_run.sh}); this test replays them so parity is checked on every build,
 * without needing a COBOL compiler. {@code harness/parity.sh} regenerates them from scratch.
 *
 * <p>Two input sets: {@code shipped} is the drop's own 300-record sample, which only ever fires
 * reject reason 102; {@code mock} is the generated set that also reaches 100, 101, 103 and the
 * branches the sample never touches ({@code harness/gen_mock_data.py}).
 *
 * <p>Only TRAN-PROC-TS is normalised away — it comes from {@code FUNCTION CURRENT-DATE}
 * (CBTRN02C.cbl:692-705) and cannot match across two runs. Its format is asserted separately.
 */
class BaselineParityTest {

    private static final int PROC_TS_OFFSET = 304;
    private static final int PROC_TS_LENGTH = 26;

    @TempDir
    Path work;

    @ParameterizedTest(name = "{0} inputs")
    @ValueSource(strings = {"mock", "shipped"})
    void matchesTheLegacyBaseline(String inputs) {
        StepRun run = StepRun.execute(StepRun.TESTDATA.resolve(inputs), work);

        assertThat(blankProcTs(run.tranfile()))
                .as("posted transactions (TRANFILE)")
                .isEqualTo(blankProcTs(StepRun.baseline(inputs, "TRANFILE.seq", 350)));
        assertThat(run.acctfile())
                .as("account after-images (ACCTFILE)")
                .isEqualTo(StepRun.baseline(inputs, "ACCTFILE.seq", 300));
        assertThat(run.tcatbalf())
                .as("category balance after-images (TCATBALF)")
                .isEqualTo(StepRun.baseline(inputs, "TCATBALF.seq", 50));
        assertThat(run.dalyrejs())
                .as("rejects (DALYREJS)")
                .isEqualTo(StepRun.baseline(inputs, "DALYREJS", 430));
        assertThat(run.sysout).isEqualTo(StepRun.baselineSysout(inputs));
        assertThat(run.returnCode).isEqualTo(StepRun.baselineReturnCode(inputs));
    }

    @ParameterizedTest(name = "{0} inputs")
    @ValueSource(strings = {"mock", "shipped"})
    void stampsProcessingTimestampsInDb2Format(String inputs) {
        StepRun run = StepRun.execute(StepRun.TESTDATA.resolve(inputs), work);

        assertThat(run.tranfile())
                .isNotEmpty()
                .allSatisfy(record -> assertThat(procTs(record))
                        .matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}0000"));
        // The clock is pinned by the test, so every record carries the same instant.
        assertThat(procTs(run.tranfile().getFirst())).isEqualTo("2024-03-01-12.34.56.780000");
    }

    private static String procTs(String record) {
        return record.substring(PROC_TS_OFFSET, PROC_TS_OFFSET + PROC_TS_LENGTH);
    }

    private static List<String> blankProcTs(List<String> records) {
        return records.stream()
                .map(r -> r.substring(0, PROC_TS_OFFSET) + " ".repeat(PROC_TS_LENGTH)
                        + r.substring(PROC_TS_OFFSET + PROC_TS_LENGTH))
                .toList();
    }
}
