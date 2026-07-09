package com.carddemo.batch.orchestration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Catalog of the orchestration pipelines exposed to launchers (REST / CLI / scheduler).
 *
 * <p>Each entry pairs a short, launch-friendly {@code name} (the {@code {name}} path variable of
 * {@code POST /api/batch/jobs/{name}}) with the Spring bean name of the orchestration {@link
 * org.springframework.batch.core.Job} defined in {@link BatchPipelineConfig}, and records which
 * legacy JCL(s) it replaces.</p>
 */
public enum BatchPipeline {

    /** POSTTRAN daily pipeline: TRANBKP → DALYREJS → POSTTRAN (CBTRN02C) → TRANREPT (CBTRN03C). */
    POSTTRAN("posttran", "postTranPipelineJob",
            "POSTTRAN.jcl (+ TRANBKP.jcl, DALYREJS.jcl, TRANREPT.jcl)"),

    /** INTCALC monthly pipeline: INTCALC (CBACT04C) → COMBTRAN merge of system transactions. */
    INTCALC("intcalc", "intcalcPipelineJob",
            "INTCALC.jcl (+ COMBTRAN.jcl)"),

    /** CREASTMT monthly pipeline: prepare statement files → CBSTM03A statement generation. */
    CREASTMT("creastmt", "createStatementPipelineJob",
            "CREASTMT.JCL"),

    /** TRANREPT stand-alone report pipeline: unload/backup → CBTRN03C report. */
    TRANREPT("tranrept", "transactionReportPipelineJob",
            "TRANREPT.jcl"),

    /** PRTCATBL: unload + sort + print of the transaction-category-balance file. */
    PRTCATBL("prtcatbl", "printCategoryBalancePipelineJob",
            "PRTCATBL.jcl");

    private final String name;
    private final String jobBeanName;
    private final String legacyJcl;

    BatchPipeline(String name, String jobBeanName, String legacyJcl) {
        this.name = name;
        this.jobBeanName = jobBeanName;
        this.legacyJcl = legacyJcl;
    }

    public String getName() {
        return name;
    }

    public String getJobBeanName() {
        return jobBeanName;
    }

    public String getLegacyJcl() {
        return legacyJcl;
    }

    /** Resolves a pipeline by its case-insensitive short name. */
    public static Optional<BatchPipeline> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(p -> p.name.equals(normalized)).findFirst();
    }

    public static List<BatchPipeline> all() {
        return Arrays.asList(values());
    }
}
