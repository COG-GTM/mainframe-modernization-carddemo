package com.carddemo.batch.reader;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.repository.DailyTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Reader for unprocessed daily transactions.
 * Mirrors CBTRN01C/CBTRN02C sequential read of DALYTRAN file.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DailyTransactionReader implements ItemReader<DailyTransaction> {

    private final DailyTransactionRepository dailyTransactionRepository;
    private Iterator<DailyTransaction> iterator;

    @Override
    public synchronized DailyTransaction read() {
        if (iterator == null) {
            List<DailyTransaction> unprocessed = dailyTransactionRepository.findByProcessedFalse();
            log.info("Found {} unprocessed daily transactions", unprocessed.size());
            iterator = unprocessed.iterator();
        }

        if (iterator.hasNext()) {
            return iterator.next();
        }

        // Reset for next job run
        iterator = null;
        return null;
    }

    public void resetReader() {
        iterator = null;
    }
}
