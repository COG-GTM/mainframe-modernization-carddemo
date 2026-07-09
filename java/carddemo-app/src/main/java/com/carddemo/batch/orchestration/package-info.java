/**
 * CS-14 — batch orchestration layer.
 *
 * <p>Maps the legacy CardDemo JCL processing pipelines (under {@code app/jcl/} and
 * {@code app/proc/}) to Spring Batch orchestration {@code Job}s that sequence the already-built
 * CS-10/CS-11/CS-12 steps ({@code intcalcStep}, {@code postTranStep}, {@code transactionReportStep},
 * {@code createStatementStep}) together with small "glue" steps that replace the mainframe
 * utilities (IDCAMS DELETE/DEFINE, SORT, IEBGENER/REPRO).</p>
 *
 * <p>Everything financial is delegated to the existing CS-10/11/12 jobs, so {@code BigDecimal}
 * exactness is preserved end-to-end. Nothing in the pre-existing batch packages is modified.</p>
 *
 * <p>Pipelines can be launched on demand through {@link com.carddemo.batch.orchestration.BatchJobController}
 * ({@code POST /api/batch/jobs/{name}}, {@code ROLE_ADMIN}), through the
 * {@link com.carddemo.batch.orchestration.BatchPipelineCommandLineRunner CLI runner}, or on a
 * cron schedule ({@link com.carddemo.batch.orchestration.BatchPipelineScheduler}, disabled by
 * default). The application never auto-runs jobs on startup
 * ({@code spring.batch.job.enabled=false}).</p>
 */
package com.carddemo.batch.orchestration;
