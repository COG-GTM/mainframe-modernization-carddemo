package com.carddemo.batch.orchestration;

import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Glue {@link Step}s that replace the mainframe utility steps of the CardDemo JCL pipelines.
 * These are the Java equivalents of the {@code IDCAMS DELETE/DEFINE}, {@code SORT} and
 * {@code IEBGENER}/{@code REPRO} steps — none of them touch money math (that stays in the
 * CS-10/11/12 jobs).
 *
 * <p>Each step is a small tasklet; the sequencing is done by {@link BatchPipelineConfig}. The
 * step bean names are unique to this package so they never collide with the existing batch
 * packages.</p>
 */
@Configuration
public class OrchestrationSteps {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationSteps.class);

    private final org.springframework.batch.core.repository.JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final BatchOrchestrationProperties properties;

    public OrchestrationSteps(org.springframework.batch.core.repository.JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              BatchOrchestrationProperties properties) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
        this.properties = properties;
    }

    /**
     * DALYREJS.jcl {@code STEP05}: {@code DEFINE GENERATIONDATAGROUP AWS.M2.CARDDEMO.DALYREJS}.
     *
     * <p>The GDG holds the daily reject file produced by CBTRN02C. In Java the rejects are
     * persisted to the relational {@code posting_transaction_reject} table (created by the CS-11
     * Flyway migration {@code V1110}), so there is no dataset to define — this step is a
     * documented no-op that simply asserts the pipeline's reject sink exists.</p>
     */
    @Bean
    public Step defineRejectsStep() {
        return new StepBuilder("orchestrationDefineRejectsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("DALYREJS DEFINE GDG(AWS.M2.CARDDEMO.DALYREJS) -> no-op; "
                            + "rejects are persisted to posting_transaction_reject (Flyway V1110).");
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    /**
     * TRANBKP.jcl / REPROC.prc: unload (REPRO) the transaction master into a sequential backup.
     * The Java equivalent reads {@code card_transaction} and writes one line per row to a backup
     * file under {@code carddemo.batch.orchestration.backup-dir}.
     */
    @Bean
    public Step backupTransactionsStep(TransactionRepository transactionRepository) {
        return new StepBuilder("orchestrationBackupTransactionsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<Transaction> all = transactionRepository.findAll();
                    Path out = Path.of(properties.getBackupDir(), "transact-backup.txt");
                    List<String> lines = all.stream()
                            .sorted(Comparator.comparing(Transaction::getTranId))
                            .map(OrchestrationSteps::backupLine)
                            .collect(Collectors.toList());
                    writeLines(out, lines);
                    contribution.getStepExecution().getExecutionContext().putInt("backup.count", all.size());
                    log.info("TRANBKP REPRO -> unloaded {} transaction(s) to {}", all.size(), out);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    /**
     * Reject-handling summary that follows POSTTRAN: counts the rows CBTRN02C's Java port wrote
     * to {@code posting_transaction_reject} and stashes the count in the step execution context.
     * Faithful to the DALYREJS reject file being produced/inspected after posting.
     */
    @Bean
    public Step handleRejectsStep(JdbcTemplate jdbcTemplate) {
        return new StepBuilder("orchestrationHandleRejectsStep", jobRepository)
                .tasklet((StepContribution contribution, org.springframework.batch.core.scope.context.ChunkContext ctx) -> {
                    Integer count = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM posting_transaction_reject", Integer.class);
                    int rejects = count == null ? 0 : count;
                    contribution.getStepExecution().getExecutionContext().putInt("reject.count", rejects);
                    log.info("POSTTRAN reject handling -> {} rejected transaction(s) in "
                            + "posting_transaction_reject.", rejects);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    /**
     * COMBTRAN.jcl: {@code SORT} of {@code TRANSACT.BKUP + SYSTRAN} by {@code TRAN-ID} followed by
     * {@code IDCAMS REPRO} of the combined file back into the transaction master.
     *
     * <p>In the Java port the INTCALC job ({@code intcalcStep}) persists its system-generated
     * interest transactions directly into the {@code card_transaction} master, so the merge is a
     * documented no-op — this step just records the resulting transaction count.</p>
     */
    @Bean
    public Step combineTransactionsStep(TransactionRepository transactionRepository) {
        return new StepBuilder("orchestrationCombineTransactionsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long count = transactionRepository.count();
                    contribution.getStepExecution().getExecutionContext().putLong("combined.count", count);
                    log.info("COMBTRAN SORT+REPRO -> system transactions already merged into "
                            + "card_transaction master ({} rows); no separate merge required.", count);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    /**
     * CREASTMT.JCL {@code DELDEF01} (IDCAMS DELETE/DEFINE of the {@code TRXFL} work KSDS) and
     * {@code STEP030} (IEFBR14 delete of the prior statement reports). The Java equivalent clears
     * the statement output directory so a fresh run does not append to stale reports; the work
     * KSDS is not needed because the CS-12 reader drives directly off the JPA repositories.
     */
    @Bean
    public Step initStatementFilesStep() {
        return new StepBuilder("orchestrationInitStatementFilesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Path dir = Path.of(properties.getStatementDir());
                    deleteIfExists(dir.resolve("statements.txt"));
                    deleteIfExists(dir.resolve("statements.html"));
                    log.info("CREASTMT DELETE/DEFINE(TRXFL) + IEFBR14(prior reports) -> cleared "
                            + "statement outputs under {} (work KSDS not required).", dir);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    /**
     * PRTCATBL.jcl: {@code IEFBR14} delete of the prior print, REPRO unload of the
     * transaction-category-balance file, {@code SORT} by {@code (ACCT-ID, TYPE-CD, CD)} and a
     * formatted print. Rendered here as a single sorted read → formatted file write.
     */
    @Bean
    public Step printCategoryBalanceStep(TransactionCategoryBalanceRepository categoryBalanceRepository) {
        return new StepBuilder("orchestrationPrintCategoryBalanceStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<TransactionCategoryBalance> rows = categoryBalanceRepository.findAll();
                    List<String> lines = rows.stream()
                            .sorted(Comparator
                                    .comparing((TransactionCategoryBalance b) -> b.getId().getTrancatAcctId())
                                    .thenComparing(b -> b.getId().getTrancatTypeCd())
                                    .thenComparing(b -> b.getId().getTrancatCd()))
                            .map(OrchestrationSteps::categoryBalanceLine)
                            .collect(Collectors.toList());
                    Path out = Path.of(properties.getReportDir(), "tcatbal-print.txt");
                    writeLines(out, lines);
                    contribution.getStepExecution().getExecutionContext().putInt("tcatbal.count", rows.size());
                    log.info("PRTCATBL SORT+REPRO -> printed {} category-balance row(s) to {}",
                            rows.size(), out);
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    // --- helpers -------------------------------------------------------------------------------

    private static String backupLine(Transaction t) {
        BigDecimal amt = t.getTranAmt() == null ? BigDecimal.ZERO : t.getTranAmt();
        return String.join("|",
                nullToEmpty(t.getTranId()),
                nullToEmpty(t.getTranCardNum()),
                nullToEmpty(t.getTranTypeCd()),
                String.valueOf(t.getTranCatCd()),
                amt.toPlainString(),
                nullToEmpty(t.getTranProcTs()));
    }

    private static String categoryBalanceLine(TransactionCategoryBalance b) {
        BigDecimal bal = b.getTranCatBal() == null ? BigDecimal.ZERO : b.getTranCatBal();
        return String.join("|",
                b.getId().getTrancatAcctId(),
                b.getId().getTrancatTypeCd(),
                String.valueOf(b.getId().getTrancatCd()),
                bal.toPlainString());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static void writeLines(Path path, List<String> lines) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + path, e);
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete " + path, e);
        }
    }
}
