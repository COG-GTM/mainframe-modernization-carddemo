package com.carddemo.batch.posting;

import com.carddemo.domain.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Persists the effects of each {@link PostingResult} — the Java port of the write paragraphs
 * of {@code CBTRN02C}.
 *
 * <ul>
 *   <li>Posted: writes the {@link com.carddemo.domain.Transaction} ({@code 2900-WRITE-TRANSACTION-FILE}),
 *       and saves the mutated account ({@code 2800}) and category balance ({@code 2700-A}/{@code 2700-B})
 *       computed by {@link TransactionPostingProcessor}.</li>
 *   <li>Rejected: inserts a row into {@code posting_transaction_reject}, replacing the legacy
 *       DALYREJS reject file written by {@code 2500-WRITE-REJECT-REC}.</li>
 * </ul>
 */
public class PostingItemWriter implements ItemWriter<PostingResult> {

    private static final String INSERT_REJECT =
            "INSERT INTO posting_transaction_reject "
                    + "(run_id, dalytran_id, tran_card_num, tran_type_cd, tran_cat_cd, tran_amt, "
                    + "reject_reason_code, reject_reason_desc) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository categoryBalanceRepository;
    private final JdbcTemplate jdbcTemplate;

    private String runId;

    public PostingItemWriter(TransactionRepository transactionRepository,
                             AccountRepository accountRepository,
                             TransactionCategoryBalanceRepository categoryBalanceRepository,
                             JdbcTemplate jdbcTemplate) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.runId = String.valueOf(stepExecution.getJobExecutionId());
    }

    @Override
    public void write(Chunk<? extends PostingResult> chunk) {
        for (PostingResult result : chunk) {
            if (result.isPosted()) {
                categoryBalanceRepository.save(result.getCategoryBalance());
                accountRepository.save(result.getAccount());
                transactionRepository.save(result.getTransaction());
            } else {
                writeReject(result);
            }
        }
    }

    private void writeReject(PostingResult result) {
        DailyTransaction dt = result.getRejected();
        RejectReason reason = result.getReason();
        jdbcTemplate.update(INSERT_REJECT,
                runId,
                dt.getDalytranId(),
                dt.getTranCardNum(),
                dt.getTranTypeCd(),
                dt.getTranCatCd(),
                dt.getTranAmt(),
                reason.code(),
                reason.description());
    }
}
