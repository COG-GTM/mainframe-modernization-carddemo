package com.carddemo.batch.writer;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Writer that persists updated account balances after interest calculation.
 * Mirrors CBACT04C 1050-UPDATE-ACCOUNT: REWRITE FD-ACCTFILE-REC FROM ACCOUNT-RECORD.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AccountBalanceWriter implements ItemWriter<Account> {

    private final AccountRepository accountRepository;

    @Override
    public void write(Chunk<? extends Account> accounts) throws Exception {
        for (Account account : accounts) {
            accountRepository.save(account);
            log.info("Updated account balance: {} new balance: {}",
                    account.getAcctId(), account.getCurrentBalance());
        }
    }
}
