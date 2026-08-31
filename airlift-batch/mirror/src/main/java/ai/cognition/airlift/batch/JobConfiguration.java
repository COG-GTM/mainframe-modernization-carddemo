package ai.cognition.airlift.batch;

import ai.cognition.airlift.slice.AccountReport;
import ai.cognition.airlift.slice.InterestCalculator;
import ai.cognition.airlift.slice.TransactionPoster;
import ai.cognition.airlift.store.Datasets;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * One Spring Batch job per JCL job in the slice, with one step per JCL step.
 *
 * <p>The IDCAMS DELETE/DEFINE/REPRO steps that surround the programs become the
 * load and unload steps; the programs themselves become chunk-oriented steps with
 * a chunk size of one, because the COBOL commits nothing and processes strictly
 * one record at a time.
 */
@Configuration
public class JobConfiguration {

  private final Datasets datasets;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public JobConfiguration(
      Datasets datasets, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.datasets = datasets;
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  /** POSTTRAN followed by INTCALC, the two jobs the slice covers. */
  @Bean
  public Job posttranIntcalc(TransactionPoster poster, InterestCalculator calculator) {
    return new JobBuilder("posttran-intcalc", jobRepository)
        .start(tasklet("load-clusters", datasets::loadSlice))
        .next(postTransactions(poster))
        .next(calculateInterest(calculator))
        .next(tasklet("unload-clusters", datasets::unload))
        .build();
  }

  /** READACCT: the untrapped control slice. */
  @Bean
  public Job readacct(AccountReport report) {
    return new JobBuilder("readacct", jobRepository)
        .start(tasklet("load-acctfile", datasets::loadAccounts))
        .next(
            tasklet(
                "report-accounts",
                () -> write(datasets.out("acctreport.txt"), report.render())))
        .build();
  }

  /** POSTTRAN step 3: CBTRN02C. */
  private Step postTransactions(TransactionPoster poster) {
    return new StepBuilder("post-transactions", jobRepository)
        .<byte[], byte[]>chunk(1, transactionManager)
        .reader(
            new ImageReader(
                () ->
                    ai.cognition.airlift.io.FixedRecordFile.read(
                        datasets.in("dalytran.dat"),
                        datasets.layouts().dailyTransaction().length())))
        .writer(
            new DatasetWriter(
                datasets.out("dalyrejs.dat"),
                image -> poster.handle(image).map(List::of).orElseGet(List::of)))
        .listener(
            new ProgramStepListener(
                poster::reset,
                () ->
                    System.out.printf(
                        "TRANSACTIONS PROCESSED :%09d%nTRANSACTIONS REJECTED  :%09d%n",
                        poster.transactionCount(), poster.rejectCount())))
        .build();
  }

  /** INTCALC step 3: CBACT04C. */
  private Step calculateInterest(InterestCalculator calculator) {
    return new StepBuilder("calculate-interest", jobRepository)
        .<byte[], byte[]>chunk(1, transactionManager)
        .reader(new ImageReader(() -> datasets.tranCatBalances().inKeyOrder()))
        .writer(
            new DatasetWriter(
                datasets.out("systran.dat"),
                image -> calculator.handle(image).map(List::of).orElseGet(List::of)))
        .listener(
            new ProgramStepListener(
                calculator::reset,
                () -> {
                  calculator.finish();
                  System.out.printf(
                      "TRANSACTION CATEGORY BALANCES READ :%09d%n", calculator.recordCount());
                }))
        .build();
  }

  private Step tasklet(String name, Runnable work) {
    Tasklet tasklet =
        (contribution, context) -> {
          work.run();
          return RepeatStatus.FINISHED;
        };
    return new StepBuilder(name, jobRepository).tasklet(tasklet, transactionManager).build();
  }

  private static void write(java.nio.file.Path path, List<String> lines) {
    try {
      Files.createDirectories(path.getParent());
      Files.write(
          path, String.join("\n", lines).concat("\n").getBytes(StandardCharsets.ISO_8859_1));
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
