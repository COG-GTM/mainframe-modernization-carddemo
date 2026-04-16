package com.cardemo.batch.orchestration.scheduler;

import com.cardemo.batch.orchestration.flow.CreastmtFlow;
import com.cardemo.batch.orchestration.flow.IntcalcFlow;
import com.cardemo.batch.orchestration.flow.PosttranFlow;
import com.cardemo.batch.orchestration.model.JobFlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for batch job flows, replacing JCL job scheduling.
 *
 * <p>Job scheduling is configurable via application properties:
 * <ul>
 *   <li>{@code cardemo.batch.schedule.posttran.cron} — POSTTRAN schedule (default: daily at 2 AM)</li>
 *   <li>{@code cardemo.batch.schedule.intcalc.cron} — INTCALC schedule (default: monthly on 1st at 3 AM)</li>
 *   <li>{@code cardemo.batch.schedule.creastmt.cron} — CREASTMT schedule (default: monthly on 1st at 4 AM)</li>
 *   <li>{@code cardemo.batch.schedule.enabled} — Enable/disable scheduling (default: true)</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "cardemo.batch.schedule.enabled", havingValue = "true", matchIfMissing = false)
public class BatchJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchJobScheduler.class);

    private final PosttranFlow posttranFlow;
    private final IntcalcFlow intcalcFlow;
    private final CreastmtFlow creastmtFlow;
    private final Job posttranJob;
    private final Job intcalcJob;
    private final Job creastmtJob;

    @Value("${cardemo.batch.intcalc.parm-date:}")
    private String intcalcParmDate;

    public BatchJobScheduler(PosttranFlow posttranFlow,
                              IntcalcFlow intcalcFlow,
                              CreastmtFlow creastmtFlow,
                              @Qualifier("posttranJob") Job posttranJob,
                              @Qualifier("intcalcJob") Job intcalcJob,
                              @Qualifier("creastmtJob") Job creastmtJob) {
        this.posttranFlow = posttranFlow;
        this.intcalcFlow = intcalcFlow;
        this.creastmtFlow = creastmtFlow;
        this.posttranJob = posttranJob;
        this.intcalcJob = intcalcJob;
        this.creastmtJob = creastmtJob;
    }

    /**
     * POSTTRAN: Process and post daily transactions.
     * Default schedule: daily at 2:00 AM.
     * Configurable via: cardemo.batch.schedule.posttran.cron
     */
    @Scheduled(cron = "${cardemo.batch.schedule.posttran.cron:0 0 2 * * *}")
    public void runPosttran() {
        log.info("SCHEDULER: Triggering POSTTRAN job flow");
        try {
            JobFlowResult result = posttranFlow.execute(posttranJob);
            logScheduledResult("POSTTRAN", result);
        } catch (Exception e) {
            log.error("SCHEDULER: POSTTRAN job flow failed with exception", e);
        }
    }

    /**
     * INTCALC: Compute interest for all accounts.
     * Default schedule: 1st of every month at 3:00 AM.
     * Configurable via: cardemo.batch.schedule.intcalc.cron
     */
    @Scheduled(cron = "${cardemo.batch.schedule.intcalc.cron:0 0 3 1 * *}")
    public void runIntcalc() {
        log.info("SCHEDULER: Triggering INTCALC job flow");
        try {
            JobFlowResult result;
            if (intcalcParmDate != null && !intcalcParmDate.isEmpty()) {
                result = intcalcFlow.execute(intcalcJob, intcalcParmDate);
            } else {
                result = intcalcFlow.execute(intcalcJob);
            }
            logScheduledResult("INTCALC", result);
        } catch (Exception e) {
            log.error("SCHEDULER: INTCALC job flow failed with exception", e);
        }
    }

    /**
     * CREASTMT: Generate credit card statements.
     * Default schedule: 1st of every month at 4:00 AM (after INTCALC).
     * Configurable via: cardemo.batch.schedule.creastmt.cron
     */
    @Scheduled(cron = "${cardemo.batch.schedule.creastmt.cron:0 0 4 1 * *}")
    public void runCreastmt() {
        log.info("SCHEDULER: Triggering CREASTMT job flow");
        try {
            JobFlowResult result = creastmtFlow.execute(creastmtJob);
            logScheduledResult("CREASTMT", result);
        } catch (Exception e) {
            log.error("SCHEDULER: CREASTMT job flow failed with exception", e);
        }
    }

    private void logScheduledResult(String flowName, JobFlowResult result) {
        if (result.isSuccessful()) {
            log.info("SCHEDULER: {} completed successfully (RC={})",
                    flowName, result.getOverallReturnCode().getCode());
        } else {
            log.error("SCHEDULER: {} FAILED (RC={}): {}",
                    flowName, result.getOverallReturnCode().getCode(),
                    result.getOverallReturnCode().getMessage());
        }
    }
}
