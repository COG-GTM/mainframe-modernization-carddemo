package com.carddemo.batch.orchestration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the CS-14 batch orchestration layer, bound from the
 * {@code carddemo.batch.orchestration.*} namespace.
 *
 * <p>All properties have safe defaults so the feature is fully functional out of the box:
 * scheduling is <strong>disabled</strong>, and the glue steps write their unloaded/backup files
 * under {@code target/batch/*}. None of this touches {@code application.yml}; the orchestration
 * layer owns its own {@code @ConfigurationProperties}.</p>
 */
@ConfigurationProperties(prefix = "carddemo.batch.orchestration")
public class BatchOrchestrationProperties {

    private final Scheduling scheduling = new Scheduling();

    /** Directory for the transaction-master backup unload (TRANBKP / REPROC). */
    private String backupDir = "target/batch/backup";

    /** Directory for rendered reports (TRANREPT) and the category-balance print (PRTCATBL). */
    private String reportDir = "target/batch/report";

    /**
     * Directory for generated statements (CREASTMT). Defaults to the same directory the CS-12
     * {@code StatementItemWriter} writes to ({@code carddemo.batch.statement.output-dir}), so the
     * prepare-files glue step clears the reports the statement step is about to (re)write.
     */
    private String statementDir = "target/statements";

    /**
     * Default inclusive start date for the transaction report when the caller does not supply
     * one. Wide by default so the daily report includes everything posted.
     */
    private String defaultReportStartDate = "0000-01-01";

    /** Default inclusive end date for the transaction report when the caller does not supply one. */
    private String defaultReportEndDate = "9999-12-31";

    public Scheduling getScheduling() {
        return scheduling;
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(String reportDir) {
        this.reportDir = reportDir;
    }

    public String getStatementDir() {
        return statementDir;
    }

    public void setStatementDir(String statementDir) {
        this.statementDir = statementDir;
    }

    public String getDefaultReportStartDate() {
        return defaultReportStartDate;
    }

    public void setDefaultReportStartDate(String defaultReportStartDate) {
        this.defaultReportStartDate = defaultReportStartDate;
    }

    public String getDefaultReportEndDate() {
        return defaultReportEndDate;
    }

    public void setDefaultReportEndDate(String defaultReportEndDate) {
        this.defaultReportEndDate = defaultReportEndDate;
    }

    /**
     * Cron-based scheduling of the daily pipelines. Disabled by default; enabling it does not
     * change the fact that jobs are never auto-run on startup — the scheduler only fires on the
     * configured cron expressions.
     */
    public static class Scheduling {

        /** Master switch for {@link BatchPipelineScheduler}. Disabled by default. */
        private boolean enabled = false;

        /** Cron for the daily POSTTRAN pipeline. Default 01:00. */
        private String postTranCron = "0 0 1 * * *";

        /** Cron for the monthly INTCALC pipeline. Default 02:00 on the 1st. */
        private String intcalcCron = "0 0 2 1 * *";

        /** Cron for the monthly CREASTMT (statement) pipeline. Default 03:00 on the 1st. */
        private String createStatementCron = "0 0 3 1 * *";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPostTranCron() {
            return postTranCron;
        }

        public void setPostTranCron(String postTranCron) {
            this.postTranCron = postTranCron;
        }

        public String getIntcalcCron() {
            return intcalcCron;
        }

        public void setIntcalcCron(String intcalcCron) {
            this.intcalcCron = intcalcCron;
        }

        public String getCreateStatementCron() {
            return createStatementCron;
        }

        public void setCreateStatementCron(String createStatementCron) {
            this.createStatementCron = createStatementCron;
        }
    }
}
