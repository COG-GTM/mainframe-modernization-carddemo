package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatementGenerationService {
    private static final Logger log = LoggerFactory.getLogger(StatementGenerationService.class);
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementGenerationService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Map<String, Object>> generateStatements() {
        List<Account> accounts = accountRepository.findAll();
        List<Map<String, Object>> statements = new ArrayList<>();

        for (Account account : accounts) {
            Map<String, Object> statement = new HashMap<>();
            statement.put("accountId", account.getAcctId());
            statement.put("currentBalance", account.getCurrBal());
            statement.put("creditLimit", account.getCreditLimit());
            statement.put("cycleCredits", account.getCurrCycCredit());
            statement.put("cycleDebits", account.getCurrCycDebit());

            List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTranCardNum() != null)
                .toList();
            statement.put("transactionCount", transactions.size());
            statements.add(statement);
            log.info("Generated statement for account {}", account.getAcctId());
        }

        return statements;
    }
}
