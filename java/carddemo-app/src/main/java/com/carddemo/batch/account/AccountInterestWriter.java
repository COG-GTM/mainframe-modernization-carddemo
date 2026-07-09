package com.carddemo.batch.account;

import com.carddemo.domain.Account;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Persists the results of {@link AccountInterestProcessor}.
 *
 * <p>Combines the two COBOL I/O effects of {@code CBACT04C}: {@code REWRITE ACCOUNT-FILE}
 * ({@code 1050-UPDATE-ACCOUNT}) → {@link AccountRepository#save} of the updated account, and
 * {@code WRITE TRANSACT-FILE} ({@code 1300-B-WRITE-TX}) → {@link TransactionRepository#saveAll}
 * of the generated interest transactions.</p>
 */
public class AccountInterestWriter implements ItemWriter<AccountInterestResult> {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountInterestWriter(AccountRepository accountRepository,
                                 TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void write(Chunk<? extends AccountInterestResult> chunk) {
        List<Account> accounts = new ArrayList<>(chunk.size());
        List<Transaction> transactions = new ArrayList<>();
        for (AccountInterestResult result : chunk) {
            accounts.add(result.account());
            transactions.addAll(result.interestTransactions());
        }
        accountRepository.saveAll(accounts);
        transactionRepository.saveAll(transactions);
    }
}
