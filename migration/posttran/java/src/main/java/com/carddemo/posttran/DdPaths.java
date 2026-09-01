package com.carddemo.posttran;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * The six DD statements of POSTTRAN.jcl:28-42, as configuration.
 *
 * <p>Names are kept identical to the DD names so the JCL and the job stay greppable against each
 * other; the COBOL {@code SELECT ... ASSIGN TO <dd>} clauses are at CBTRN02C.cbl:29-61.
 */
@ConfigurationProperties(prefix = "posttran.dd")
public record DdPaths(
        Path dalytran,
        Path tranfile,
        Path xreffile,
        Path dalyrejs,
        Path acctfile,
        Path tcatbalf) {
}
