package com.carddemo.posttran;

import com.carddemo.posttran.io.FixedWidthFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test of the wiring the other tests bypass: the job really is submitted on startup, the six
 * DD properties really are bound, and the step's return code really reaches the exit code.
 */
class PostTranApplicationTest {

    @TempDir
    Path work;

    @Test
    void runsTheJobOnStartupAndSurfacesTheStepReturnCode() {
        DdPaths dd = StepRun.stageInputs(StepRun.TESTDATA.resolve("mock"), work);

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(PostTranApplication.class)
                .run("--posttran.dd.dalytran=" + dd.dalytran(),
                     "--posttran.dd.xreffile=" + dd.xreffile(),
                     "--posttran.dd.acctfile=" + dd.acctfile(),
                     "--posttran.dd.tcatbalf=" + dd.tcatbalf(),
                     "--posttran.dd.tranfile=" + dd.tranfile(),
                     "--posttran.dd.dalyrejs=" + dd.dalyrejs())) {

            assertThat(context.getBean(StepReturnCode.class).getExitCode())
                    .as("RC=4 because the run has rejects (CBTRN02C.cbl:229-231)")
                    .isEqualTo(4);
            assertThat(FixedWidthFiles.read(dd.tranfile(), 350)).hasSize(6);
            assertThat(FixedWidthFiles.read(dd.dalyrejs(), 430)).hasSize(5);
        }
    }
}
