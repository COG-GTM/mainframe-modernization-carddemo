package com.carddemo.batch.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * CLI entry point to run a single orchestration pipeline at startup, e.g.
 * <pre>
 *   java -jar carddemo-app.jar --carddemo.batch.orchestration.run=posttran
 *   mvn -pl carddemo-app spring-boot:run -Dspring-boot.run.arguments=--carddemo.batch.orchestration.run=intcalc
 * </pre>
 *
 * <p>Only registered when {@code carddemo.batch.orchestration.run} is set, so ordinary boots (and
 * the smoke tests) never trigger a job — the application still starts with
 * {@code spring.batch.job.enabled=false} and no auto-run. After running the named pipeline the
 * application continues running (it does not force an exit), matching how the other batch runners
 * behave.</p>
 */
@Component
@ConditionalOnProperty(prefix = "carddemo.batch.orchestration", name = "run")
public class BatchPipelineCommandLineRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchPipelineCommandLineRunner.class);

    private final BatchJobLauncherService launcher;
    private final String pipelineName;

    public BatchPipelineCommandLineRunner(BatchJobLauncherService launcher,
                                          org.springframework.core.env.Environment environment) {
        this.launcher = launcher;
        this.pipelineName = environment.getProperty("carddemo.batch.orchestration.run");
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("CLI batch run requested for pipeline '{}'.", pipelineName);
        BatchJobLaunchResult result = launcher.launch(pipelineName);
        log.info("CLI batch run of '{}' completed with status {} (executionId={}).",
                result.getPipeline(), result.getStatus(), result.getJobExecutionId());
    }
}
