package com.carddemo.account.batch;

import com.carddemo.account.exception.AccountFileException;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Modern equivalent of the COBOL batch program {@code CBACT01C} (JCL job
 * {@code READACCT}) — "Read and print account data file".
 *
 * <p>The original opens the {@code ACCTDAT} VSAM KSDS, reads it sequentially,
 * displays every field of each record, then closes the file; any non-zero file
 * status aborts via {@code 9999-ABEND-PROGRAM}. Each method below maps one-to-one
 * to a COBOL paragraph so the translation stays traceable. COBOL {@code DISPLAY}
 * statements become SLF4J log lines.
 */
@Service
public class AccountReportService {

    private static final Logger log = LoggerFactory.getLogger(AccountReportService.class);

    private final AccountRepository accountRepository;

    public AccountReportService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * PROCEDURE DIVISION — drives the whole job.
     *
     * @return the number of account records read and printed
     */
    public int runReport() {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C");

        Iterator<Account> cursor = openAccountFile();
        int recordsRead = 0;

        // PERFORM UNTIL END-OF-FILE = 'Y'
        while (true) {
            Account account = getNextAccount(cursor);
            if (account == null) {
                break;
            }
            displayAccountRecord(account);
            recordsRead++;
        }

        closeAccountFile(cursor);

        log.info("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordsRead;
    }

    /** 0000-ACCTFILE-OPEN — OPEN INPUT ACCTFILE-FILE. */
    private Iterator<Account> openAccountFile() {
        try {
            List<Account> accounts = accountRepository.findAllByOrderByAcctIdAsc();
            return accounts.iterator();
        } catch (DataAccessException e) {
            log.error("ERROR OPENING ACCTFILE");
            throw abendProgram("opening", e);
        }
    }

    /**
     * 1000-ACCTFILE-GET-NEXT — READ ACCTFILE-FILE INTO ACCOUNT-RECORD.
     * Returns {@code null} at end-of-file (file status '10').
     */
    private Account getNextAccount(Iterator<Account> cursor) {
        try {
            if (!cursor.hasNext()) {
                return null;
            }
            return cursor.next();
        } catch (DataAccessException e) {
            log.error("ERROR READING ACCOUNT FILE");
            throw abendProgram("reading", e);
        }
    }

    /** 1100-DISPLAY-ACCT-RECORD — DISPLAY each field of ACCOUNT-RECORD. */
    private void displayAccountRecord(Account account) {
        log.info("ACCT-ID                 :{}", account.getAcctId());
        log.info("ACCT-ACTIVE-STATUS      :{}", account.getActiveStatus());
        log.info("ACCT-CURR-BAL           :{}", money(account.getCurrentBalance()));
        log.info("ACCT-CREDIT-LIMIT       :{}", money(account.getCreditLimit()));
        log.info("ACCT-CASH-CREDIT-LIMIT  :{}", money(account.getCashCreditLimit()));
        log.info("ACCT-OPEN-DATE          :{}", account.getOpenDate());
        log.info("ACCT-EXPIRAION-DATE     :{}", account.getExpirationDate());
        log.info("ACCT-REISSUE-DATE       :{}", account.getReissueDate());
        log.info("ACCT-CURR-CYC-CREDIT    :{}", money(account.getCurrentCycleCredit()));
        log.info("ACCT-CURR-CYC-DEBIT     :{}", money(account.getCurrentCycleDebit()));
        log.info("ACCT-GROUP-ID           :{}", account.getGroupId());
        log.info("-------------------------------------------------");
    }

    /** 9000-ACCTFILE-CLOSE — CLOSE ACCTFILE-FILE. */
    private void closeAccountFile(Iterator<Account> cursor) {
        // A JPA result iterator holds no OS file handle; nothing to release.
        // The paragraph is preserved to mirror CBACT01C's structure.
    }

    /** 9999-ABEND-PROGRAM — CALL 'CEE3ABD'. */
    private AccountFileException abendProgram(String operation, Throwable cause) {
        log.error("ABENDING PROGRAM");
        return new AccountFileException("Failed " + operation + " account file", cause);
    }

    private static String money(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
