package com.carddemo.batch;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Tallies posted transactions and rejects into the step's execution context so the job can
 * report counts (mirrors the COBOL WS-TRANSACTION-COUNT / WS-REJECT-COUNT and RETURN-CODE).
 * The records themselves are persisted by {@code TransactionPostingService}.
 */
@Component
public class TransactionItemWriter implements ItemWriter<ProcessedTransaction> {

    public static final String TRANSACTION_COUNT_KEY = "transactionCount";
    public static final String REJECT_COUNT_KEY = "rejectCount";

    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        stepExecution.getExecutionContext().putLong(TRANSACTION_COUNT_KEY, 0L);
        stepExecution.getExecutionContext().putLong(REJECT_COUNT_KEY, 0L);
    }

    @Override
    public void write(Chunk<? extends ProcessedTransaction> chunk) {
        long transactionCount = stepExecution.getExecutionContext().getLong(TRANSACTION_COUNT_KEY, 0L);
        long rejectCount = stepExecution.getExecutionContext().getLong(REJECT_COUNT_KEY, 0L);
        for (ProcessedTransaction item : chunk) {
            transactionCount++;
            if (item.result().isRejected()) {
                rejectCount++;
            }
        }
        stepExecution.getExecutionContext().putLong(TRANSACTION_COUNT_KEY, transactionCount);
        stepExecution.getExecutionContext().putLong(REJECT_COUNT_KEY, rejectCount);
    }
}
