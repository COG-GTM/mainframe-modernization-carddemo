package com.carddemo.batch.batch;

import com.carddemo.batch.model.DailyTransaction;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * No-op writer. Valid transactions are already posted inside the TransactionProcessor.
 * This writer exists to satisfy the Spring Batch step configuration requirement.
 * Since the processor returns null for all items (both valid and rejected),
 * this writer will never actually receive any items.
 */
public class TransactionWriter implements ItemWriter<DailyTransaction> {

    @Override
    public void write(Chunk<? extends DailyTransaction> chunk) {
        // No-op: all writing is handled by TransactionProcessor
    }
}
