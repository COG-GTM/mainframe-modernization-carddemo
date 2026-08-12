package com.carddemo.batch.statement;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL program: CBSTM03A, driven by CREASTMT.JCL step STEP040 — writes the statements of every
 * CARDXREF record to the STMTFILE (LRECL 80) and HTMLFILE (LRECL 100) DD names.
 *
 * <p>The output locations are configurable through {@code carddemo.statement.text-file} and
 * {@code carddemo.statement.html-file}.
 */
@Configuration
public class StatementJobConfig {

    /** Job name matching the JCL job that ran CBSTM03A. */
    public static final String JOB_NAME = "creastmtJob";

    private static final Logger LOG = LoggerFactory.getLogger(StatementJobConfig.class);

    @Bean
    public Job creastmtJob(JobRepository jobRepository, Step createStatementsStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(createStatementsStep)
                .build();
    }

    @Bean
    public Step createStatementsStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     StatementService statementService,
                                     @Value("${carddemo.statement.text-file:target/statements/STATEMNT.PS}")
                                     String textFile,
                                     @Value("${carddemo.statement.html-file:target/statements/STATEMNT.HTML}")
                                     String htmlFile) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            List<StatementDocument> statements = statementService.generateStatements();
            write(Path.of(textFile), statements, true);
            write(Path.of(htmlFile), statements, false);
            LOG.info("Wrote {} statements to {} and {}", statements.size(), textFile, htmlFile);
            contribution.incrementWriteCount(statements.size());
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("createStatementsStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    private static void write(Path file, List<StatementDocument> statements, boolean plainText)
            throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.ISO_8859_1)) {
            for (StatementDocument statement : statements) {
                writer.write(plainText ? statement.text() : statement.html());
            }
        }
    }
}
