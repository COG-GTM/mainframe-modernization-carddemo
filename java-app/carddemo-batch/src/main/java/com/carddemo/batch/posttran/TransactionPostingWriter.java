package com.carddemo.batch.posttran;

import com.carddemo.repository.DailyTransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Writes posting results: valid transactions to Transaction table,
 * rejects to DailyTransactionReject table.
 */
public class TransactionPostingWriter implements ItemWriter<TransactionPostingResult> {

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRejectRepository rejectRepository;

    public TransactionPostingWriter(TransactionRepository transactionRepository,
                                     DailyTransactionRejectRepository rejectRepository) {
        this.transactionRepository = transactionRepository;
        this.rejectRepository = rejectRepository;
    }

    @Override
    public void write(Chunk<? extends TransactionPostingResult> results) throws Exception {
        for (TransactionPostingResult result : results) {
            if (result.isValid()) {
                transactionRepository.save(result.getPostedTransaction());
            } else {
                rejectRepository.save(result.getReject());
            }
        }
    }
}
