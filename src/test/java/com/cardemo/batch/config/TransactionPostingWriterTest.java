package com.cardemo.batch.config;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.RejectedTransaction;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionPostingResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.RejectedTransactionRepository;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionPostingWriter.
 * Tests the composite write logic for posted and rejected transactions.
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingWriterTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RejectedTransactionRepository rejectedTransactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionCategoryBalanceRepository categoryBalanceRepository;

    private TransactionPostingWriter writer;

    @BeforeEach
    void setUp() {
        writer = new TransactionPostingWriter(
                transactionRepository, rejectedTransactionRepository,
                accountRepository, categoryBalanceRepository);
    }

    @Test
    @DisplayName("Posted transaction saves to transaction, account, and category balance repos")
    void write_postedTransaction_savesToAllRepos() throws Exception {
        Transaction txn = new Transaction();
        txn.setId("TRAN0001");
        Account acct = new Account();
        acct.setAcctId(123L);
        TransactionCategoryBalance catBal = new TransactionCategoryBalance();

        TransactionPostingResult posted = TransactionPostingResult.posted(txn, acct, catBal, false);
        Chunk<TransactionPostingResult> chunk = new Chunk<>(posted);

        writer.write(chunk);

        verify(transactionRepository).save(txn);
        verify(accountRepository).save(acct);
        verify(categoryBalanceRepository).save(catBal);
        verify(rejectedTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejected transaction saves to rejected repo only")
    void write_rejectedTransaction_savesToRejectedRepo() throws Exception {
        RejectedTransaction rejected = new RejectedTransaction();
        rejected.setTransactionId("TRAN0002");
        rejected.setFailReasonCode(100);

        TransactionPostingResult rejResult = TransactionPostingResult.rejected(rejected);
        Chunk<TransactionPostingResult> chunk = new Chunk<>(rejResult);

        writer.write(chunk);

        verify(rejectedTransactionRepository).save(rejected);
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
        verify(categoryBalanceRepository, never()).save(any());
    }
}
