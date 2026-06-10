package com.carddemo.batch;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.service.TransactionPostingService;
import com.carddemo.service.ValidationResult;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Spring Batch processor wrapping {@link TransactionPostingService#processSingleTransaction}.
 * Validation and posting (TCATBAL upsert, account update, transaction/reject write) happen
 * atomically inside the service; this processor returns the outcome for the writer to tally.
 */
@Component
public class TransactionItemProcessor implements ItemProcessor<DailyTransaction, ProcessedTransaction> {

    private final TransactionPostingService postingService;

    public TransactionItemProcessor(TransactionPostingService postingService) {
        this.postingService = postingService;
    }

    @Override
    public ProcessedTransaction process(DailyTransaction item) {
        ValidationResult result = postingService.processSingleTransaction(item);
        return new ProcessedTransaction(item, result);
    }
}
