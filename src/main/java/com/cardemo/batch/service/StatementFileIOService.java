package com.cardemo.batch.service;

import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.CardXref;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.CustomerRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Replaces CBSTM03B subroutine (file I/O operations).
 * <p>
 * The original COBOL program used CALL 'CBSTM03B' USING WS-M03B-AREA
 * with operation codes (O=Open, C=Close, R=Read, K=ReadByKey).
 * This service replaces that CALL linkage with repository method invocations.
 */
@Service
public class StatementFileIOService {

    private static final Logger log = LoggerFactory.getLogger(StatementFileIOService.class);

    private final TransactionRecordRepository transactionRepo;
    private final CardXrefRepository cardXrefRepo;
    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;

    public StatementFileIOService(TransactionRecordRepository transactionRepo,
                                  CardXrefRepository cardXrefRepo,
                                  CustomerRepository customerRepo,
                                  AccountRepository accountRepo) {
        this.transactionRepo = transactionRepo;
        this.cardXrefRepo = cardXrefRepo;
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
    }

    /**
     * Replaces XREFFILE sequential read (CBSTM03B 2000-XREFFILE-PROC with M03B-READ).
     * Returns all cross-references ordered by card number.
     */
    public List<CardXref> readAllXrefs() {
        log.debug("Reading all card cross-references (XREFFILE sequential read)");
        return cardXrefRepo.findAllByOrderByCardNumAsc();
    }

    /**
     * Replaces CUSTFILE keyed read (CBSTM03B 3000-CUSTFILE-PROC with M03B-READ-K).
     * Looks up a customer by ID (random access by key).
     */
    public Optional<Customer> readCustomerByKey(String custId) {
        log.debug("Reading customer by key: {} (CUSTFILE READ-K)", custId);
        return customerRepo.findById(custId);
    }

    /**
     * Replaces ACCTFILE keyed read (CBSTM03B 4000-ACCTFILE-PROC with M03B-READ-K).
     * Looks up an account by ID (random access by key).
     */
    public Optional<Account> readAccountByKey(String acctId) {
        log.debug("Reading account by key: {} (ACCTFILE READ-K)", acctId);
        return accountRepo.findById(acctId);
    }

    /**
     * Replaces TRNXFILE sequential read with card number filter.
     * The original COBOL loaded transactions into a 2D array
     * (WS-CARD-TBL OCCURS 51 x WS-TRAN-TBL OCCURS 10).
     * SQL ORDER BY replaces the JCL SORT step.
     */
    public List<TransactionRecord> readTransactionsByCard(String cardNum) {
        log.debug("Reading transactions for card: {} (TRNXFILE read by card)", cardNum);
        return transactionRepo.findByCardNumOrderByCardNumAscTranIdAsc(cardNum);
    }
}
