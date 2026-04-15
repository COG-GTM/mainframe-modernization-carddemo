package com.carddemo.batch.writer;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.repository.DailyTransactionRepository;
import com.carddemo.batch.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Writer that persists posted transactions and marks daily transactions as processed.
 * Mirrors CBTRN02C 2900-WRITE-TRANSACTION-FILE.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionWriter implements ItemWriter<Transaction> {

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;

    @Override
    public void write(Chunk<? extends Transaction> transactions) throws Exception {
        for (Transaction tran : transactions) {
            transactionRepository.save(tran);

            // Mark the daily transaction as processed
            Optional<DailyTransaction> dailyTran =
                    dailyTransactionRepository.findById(tran.getTranId());
            dailyTran.ifPresent(dt -> {
                dt.setProcessed(true);
                dailyTransactionRepository.save(dt);
            });

            log.info("Posted transaction: {} amount: {}", tran.getTranId(), tran.getAmount());
        }
    }
}
