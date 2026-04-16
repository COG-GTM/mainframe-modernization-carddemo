package com.cardemo.batch.config;

import com.cardemo.batch.listener.ReportJobListener;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.processor.DateRangeFilterProcessor;
import com.cardemo.batch.reader.TransactionRecordRowMapper;
import com.cardemo.batch.service.ReferenceDataService;
import com.cardemo.batch.service.ReportFormatterService;
import com.cardemo.batch.writer.TransactionReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring Batch configuration for the Transaction Detail Report job.
 * Ported from CBTRN03C.CBL — reads transactions from the database (TRANFILE),
 * filters by date range (DATEPARM), enriches via reference lookups (TRANTYPE,
 * TRANCATG, CARDXREF), and writes a 133-byte fixed-width report (TRANREPT).
 */
@Configuration
public class TransactionReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportJobConfig.class);

    @Value("${dateparm.start-date:2022-01-01}")
    private String startDate;

    @Value("${dateparm.end-date:2022-12-31}")
    private String endDate;

    @Value("${report.page-size:20}")
    private int pageSize;

    @Value("${report.output-path:./output/transaction-detail-report.txt}")
    private String outputPath;

    /**
     * Reader: reads transactions ordered by card number then processing timestamp.
     * This ordering ensures account-break logic works correctly (grouping by card).
     * Equivalent to sequential read of TRANFILE.
     */
    @Bean
    public JdbcCursorItemReader<TransactionRecord> transactionReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<TransactionRecord>()
                .name("transactionReader")
                .dataSource(dataSource)
                .sql("SELECT tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
                        + "tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
                        + "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts "
                        + "FROM transaction_record "
                        + "ORDER BY tran_card_num, tran_proc_ts")
                .rowMapper(new TransactionRecordRowMapper())
                .build();
    }

    /**
     * Processor: filters transactions by date range.
     * Equivalent to 0550-DATEPARM-READ + main loop date check.
     */
    @Bean
    public DateRangeFilterProcessor dateRangeFilterProcessor() {
        log.info("Reporting from {} to {}", startDate, endDate);
        return new DateRangeFilterProcessor(startDate, endDate);
    }

    /**
     * Writer: produces the 133-byte fixed-width report file.
     * Equivalent to TRANREPT output with all CVTRA07Y report structures.
     */
    @Bean
    public TransactionReportWriter transactionReportWriter(
            ReportFormatterService formatter,
            ReferenceDataService referenceDataService) throws IOException {
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());
        BufferedWriter bw = Files.newBufferedWriter(path);
        return new TransactionReportWriter(bw, formatter, referenceDataService,
                startDate, endDate, pageSize);
    }

    /**
     * Job listener: handles report start/end logging and writes closing totals.
     */
    @Bean
    public ReportJobListener reportJobListener(TransactionReportWriter reportWriter) {
        return new ReportJobListener(reportWriter);
    }

    /**
     * Step: read -> filter by date -> write report.
     * Chunk size of 100 for efficient batch processing.
     */
    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      JdbcCursorItemReader<TransactionRecord> transactionReader,
                                      DateRangeFilterProcessor dateRangeFilterProcessor,
                                      TransactionReportWriter transactionReportWriter) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(100, transactionManager)
                .reader(transactionReader)
                .processor(dateRangeFilterProcessor)
                .writer(transactionReportWriter)
                .build();
    }

    /**
     * Job: Transaction Detail Report.
     */
    @Bean
    public Job transactionDetailReportJob(JobRepository jobRepository,
                                          Step transactionReportStep,
                                          ReportJobListener reportJobListener) {
        return new JobBuilder("transactionDetailReportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(reportJobListener)
                .start(transactionReportStep)
                .build();
    }
}
