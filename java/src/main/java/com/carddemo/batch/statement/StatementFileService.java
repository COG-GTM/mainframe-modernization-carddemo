package com.carddemo.batch.statement;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * COBOL program: CBSTM03B — the I/O subroutine called by CBSTM03A with the WS-M03B-AREA
 * function code (O/C/R/K) per DD name.
 *
 * <p>The four VSAM files it opens are served here from the JPA repositories:
 * TRNXFILE (the CREASTMT.JCL re-keyed copy of TRANSACT, key TRAN-CARD-NUM + TRAN-ID, read
 * sequentially), XREFFILE (CARDXREF / CVACT03Y, read sequentially), CUSTFILE (CUSTDATA /
 * CVCUS01Y, random read by CUST-ID) and ACCTFILE (ACCTDATA / CVACT01Y, random read by ACCT-ID).
 *
 * <p>The literal CALL interface is not reproduced; the file status codes it returns are, so
 * that the caller keeps the same error behaviour: a missing keyed record yields status 23,
 * which CBSTM03A reports and abends on.
 */
@Service
public class StatementFileService {

    /** VSAM file status '23' — record not found on a keyed read. */
    static final String STATUS_RECORD_NOT_FOUND = "23";

    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementFileService(CardXrefRepository cardXrefRepository,
                                CustomerRepository customerRepository,
                                AccountRepository accountRepository,
                                TransactionRepository transactionRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /** XREFFILE sequential read (2000-XREFFILE-PROC), in XREF-CARD-NUM key order. */
    public List<CardXref> readXrefFile() {
        return cardXrefRepository.findAll(Sort.by(Sort.Direction.ASC, "cardNumber"));
    }

    /**
     * TRNXFILE sequential read (1000-TRNXFILE-PROC). CREASTMT.JCL sorts TRANSACT into
     * TRAN-CARD-NUM + TRAN-ID order before CBSTM03A reads it, which is what lets the program
     * group transactions by card while reading the file only once.
     */
    public List<Transaction> readTransactionFile() {
        return transactionRepository.findAll(
                Sort.by(Sort.Order.asc("cardNumber"), Sort.Order.asc("transactionId")));
    }

    /** CUSTFILE keyed read (3000-CUSTFILE-PROC) on CUST-ID. */
    public Customer readCustomer(Long customerId) {
        return customerRepository.findById(customerId == null ? 0L : customerId)
                .orElseThrow(() -> new StatementFileException("CUSTFILE", STATUS_RECORD_NOT_FOUND));
    }

    /** ACCTFILE keyed read (4000-ACCTFILE-PROC) on ACCT-ID. */
    public Account readAccount(Long accountId) {
        return accountRepository.findById(accountId == null ? 0L : accountId)
                .orElseThrow(() -> new StatementFileException("ACCTFILE", STATUS_RECORD_NOT_FOUND));
    }
}
