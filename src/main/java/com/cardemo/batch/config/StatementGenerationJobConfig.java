package com.cardemo.batch.config;

import com.cardemo.batch.service.StatementGenerationService;
import com.cardemo.batch.service.StatementGenerationService.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Spring Batch job configuration replacing JCL CREASTMT.JCL.
 * <p>
 * The original JCL job had steps for:
 * 1. SORT transaction data by card number (replaced by SQL ORDER BY)
 * 2. Load sorted data into VSAM (no longer needed)
 * 3. Run CBSTM03A to generate statements
 * <p>
 * This configuration defines a single-step job that runs the statement
 * generation service and writes output to STMTFILE and HTMLFILE.
 * <p>
 * Mainframe control block addressing (PSA/TCB/TIOT) is replaced
 * with Spring Batch job metadata logging via the JobExecutionListener.
 */
@Configuration
public class StatementGenerationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationJobConfig.class);

    @Value("${statement.output.directory:./output}")
    private String outputDirectory;

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step generateStatementsStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(generateStatementsStep)
                .build();
    }

    @Bean
    public Step generateStatementsStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       StatementGenerationService service) {
        return new StepBuilder("generateStatementsStep", jobRepository)
                .tasklet(statementGenerationTasklet(service), transactionManager)
                .build();
    }

    @Bean
    public Tasklet statementGenerationTasklet(StatementGenerationService service) {
        return (contribution, chunkContext) -> {
            // Log job metadata (replaces mainframe PSA/TCB/TIOT control block access)
            String jobName = chunkContext.getStepContext().getJobName();
            String stepName = chunkContext.getStepContext().getStepName();
            long jobInstanceId = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getJobId();
            log.info("Running Job: {} Step: {} Instance: {} "
                    + "(replaces TIOTNJOB/TIOTJSTP from z/OS control blocks)",
                    jobName, stepName, jobInstanceId);

            List<StatementResult> results = service.generateAllStatements();

            Path outputDir = Paths.get(outputDirectory);
            Files.createDirectories(outputDir);

            // Write STMTFILE (plain text output, sequential)
            Path stmtFile = outputDir.resolve("statements.txt");
            writeLinesToFile(stmtFile, results, true);
            log.info("Plain text statements written to: {}", stmtFile);

            // Write HTMLFILE (HTML output, sequential)
            Path htmlFile = outputDir.resolve("statements.html");
            writeLinesToFile(htmlFile, results, false);
            log.info("HTML statements written to: {}", htmlFile);

            log.info("Statement generation job complete. {} statements generated.",
                    results.size());

            return RepeatStatus.FINISHED;
        };
    }

    private void writeLinesToFile(Path filePath, List<StatementResult> results,
                                  boolean plainText) throws IOException {
        Files.deleteIfExists(filePath);
        for (StatementResult result : results) {
            List<String> lines = plainText
                    ? result.getPlainTextLines()
                    : result.getHtmlLines();
            for (String line : lines) {
                Files.writeString(filePath, line + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
        }
    }
}
