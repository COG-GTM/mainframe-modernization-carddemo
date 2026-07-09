package com.carddemo.batch.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Cron scheduling of the daily/monthly orchestration pipelines.
 *
 * <p><strong>Disabled by default.</strong> The whole component (including {@code @EnableScheduling})
 * is only registered when {@code carddemo.batch.orchestration.scheduling.enabled=true}, so with the
 * default configuration no timer is created and nothing is auto-run. When enabled it fires the
 * pipelines on the configured cron expressions (defaults: POSTTRAN 01:00 daily, INTCALC 02:00 and
 * CREASTMT 03:00 on the 1st of the month) via {@link BatchJobLauncherService}.</p>
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "carddemo.batch.orchestration.scheduling", name = "enabled",
        havingValue = "true")
public class BatchPipelineScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchPipelineScheduler.class);

    private final BatchJobLauncherService launcher;

    public BatchPipelineScheduler(BatchJobLauncherService launcher) {
        this.launcher = launcher;
        log.info("Batch pipeline scheduling is ENABLED.");
    }

    @Scheduled(cron = "${carddemo.batch.orchestration.scheduling.post-tran-cron:0 0 1 * * *}")
    public void runPostTranPipeline() {
        launcher.launch(BatchPipeline.POSTTRAN.getName());
    }

    @Scheduled(cron = "${carddemo.batch.orchestration.scheduling.intcalc-cron:0 0 2 1 * *}")
    public void runIntcalcPipeline() {
        launcher.launch(BatchPipeline.INTCALC.getName());
    }

    @Scheduled(cron = "${carddemo.batch.orchestration.scheduling.create-statement-cron:0 0 3 1 * *}")
    public void runCreateStatementPipeline() {
        launcher.launch(BatchPipeline.CREASTMT.getName());
    }
}
