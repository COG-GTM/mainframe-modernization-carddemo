package com.carddemo.service;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.TranCatBalance;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interest calculator — Java transpilation of {@code app/cbl/CBACT04C.cbl}.
 *
 * <p>The original COBOL batch program reads the transaction-category-balance file ({@code TCATBALF})
 * sequentially, and for each account computes monthly interest per category from the disclosure-group
 * interest rate, writes an interest transaction, and finally updates the account's balance. Each
 * private method below corresponds to a COBOL paragraph (named in the Javadoc) to preserve
 * traceability.
 *
 * <p><b>Fixed-point arithmetic:</b> every money value is {@link BigDecimal}. The COBOL
 * {@code COMPUTE} has no {@code ROUNDED} clause, so the result is truncated to the 2-decimal scale
 * of {@code WS-MONTHLY-INT PIC S9(09)V99}; this is reproduced with {@link RoundingMode#DOWN}.
 */
@Service
public class InterestCalculationService {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationService.class);

    /** Divisor in COBOL: {@code (TRAN-CAT-BAL * DIS-INT-RATE) / 1200} (annual % -> monthly fraction). */
    private static final BigDecimal MONTHLY_DIVISOR = new BigDecimal("1200");

    /** Scale of WS-MONTHLY-INT / TRAN-AMT (S9(09)V99). */
    private static final int MONEY_SCALE = 2;

    private static final String INTEREST_TRAN_TYPE = "01";
    private static final int INTEREST_TRAN_CAT = 5;          // MOVE '05' TO TRAN-CAT-CD (PIC 9(04))
    private static final String INTEREST_TRAN_SOURCE = "System";

    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationService(
            TranCatBalanceRepository tranCatBalanceRepository,
            CardXrefRepository cardXrefRepository,
            DisclosureGroupRepository disclosureGroupRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * PROCEDURE DIVISION main driver (CBACT04C lines 180-232).
     *
     * <p>Opens the files (implicit here), reads {@code TCATBALF} sequentially, and applies interest
     * per account/category. The account-change boundary flushes the previous account
     * ({@code 1050-UPDATE-ACCOUNT}); the final account is flushed after the loop, mirroring the
     * main loop's EOF {@code ELSE PERFORM 1050-UPDATE-ACCOUNT} branch (lines 219-220).
     *
     * @param processingDate the {@code PARM-DATE} from the JCL ({@code PARM='2022071800'}),
     *                        used as the transaction-id prefix
     * @return run counters and the total interest written
     */
    @Transactional
    public InterestCalculationResult calculateInterest(String processingDate) {
        log.info("START OF EXECUTION OF PROGRAM CBACT04C (processingDate={})", processingDate);

        RunState state = new RunState(processingDate);
        List<TranCatBalance> balances =
                tranCatBalanceRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();

        for (TranCatBalance balance : balances) {
            state.recordCount++;                                       // ADD 1 TO WS-RECORD-COUNT
            if (!balance.getAcctId().equals(state.lastAcctNum)) {      // TRANCAT-ACCT-ID NOT= WS-LAST-ACCT-NUM
                if (!state.firstTime) {
                    updateAccount(state);                              // 1050-UPDATE-ACCOUNT (previous account)
                } else {
                    state.firstTime = false;
                }
                state.totalInterest = BigDecimal.ZERO.setScale(MONEY_SCALE); // MOVE 0 TO WS-TOTAL-INT
                state.lastAcctNum = balance.getAcctId();              // MOVE TRANCAT-ACCT-ID TO WS-LAST-ACCT-NUM
                state.currentAccount = getAcctData(balance.getAcctId());   // 1100-GET-ACCT-DATA
                state.currentXref = getXrefData(balance.getAcctId());      // 1110-GET-XREF-DATA
            }

            DisclosureGroup disclosureGroup = getInterestRate(           // 1200-GET-INTEREST-RATE
                    state.currentAccount.getGroupId(),
                    balance.getTypeCd(),
                    balance.getCatCd());

            if (disclosureGroup.getIntRate().signum() != 0) {            // IF DIS-INT-RATE NOT = 0
                computeInterest(state, balance, disclosureGroup);        // 1300-COMPUTE-INTEREST
                computeFees();                                           // 1400-COMPUTE-FEES (no-op)
            }
        }

        if (!state.firstTime) {                                          // main loop EOF: 1050-UPDATE-ACCOUNT
            updateAccount(state);
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT04C (records={}, transactions={}, accounts={})",
                state.recordCount, state.tranidSuffix, state.accountsUpdated);
        return new InterestCalculationResult(
                state.recordCount, state.tranidSuffix, state.accountsUpdated, state.grandTotalInterest);
    }

    /**
     * {@code 1050-UPDATE-ACCOUNT} (CBACT04C lines 350-370): add the accumulated interest to the
     * account balance, reset both cycle totals to zero, and rewrite the account.
     */
    private void updateAccount(RunState state) {
        Account account = state.currentAccount;
        account.setCurrBal(account.getCurrBal().add(state.totalInterest));  // ADD WS-TOTAL-INT TO ACCT-CURR-BAL
        account.setCurrCycCredit(BigDecimal.ZERO.setScale(MONEY_SCALE));    // MOVE 0 TO ACCT-CURR-CYC-CREDIT
        account.setCurrCycDebit(BigDecimal.ZERO.setScale(MONEY_SCALE));     // MOVE 0 TO ACCT-CURR-CYC-DEBIT
        accountRepository.save(account);                                    // REWRITE FD-ACCTFILE-REC
        state.accountsUpdated++;
    }

    /**
     * {@code 1100-GET-ACCT-DATA} (CBACT04C lines 372-391): random read of the account by id; a
     * missing account abends the run.
     */
    private Account getAcctData(Long acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> abend("ERROR READING ACCOUNT FILE - acct " + acctId + " not found"));
    }

    /**
     * {@code 1110-GET-XREF-DATA} (CBACT04C lines 393-413): random read of the card xref by account
     * id through the {@code CXACAIX} alternate index; a missing xref abends the run.
     */
    private CardXref getXrefData(Long acctId) {
        return cardXrefRepository.findFirstByAcctId(acctId)
                .orElseThrow(() -> abend("ERROR READING CROSS REF FILE - acct " + acctId + " not found"));
    }

    /**
     * {@code 1200-GET-INTEREST-RATE} (CBACT04C lines 415-440) plus
     * {@code 1200-A-GET-DEFAULT-INT-RATE} (lines 443-460): random read of the disclosure group by
     * (account-group, tran-type, tran-cat). On a "not found" (COBOL file status {@code '23'}) retry
     * with the {@code 'DEFAULT'} group; if the default is also missing the run abends.
     */
    private DisclosureGroup getInterestRate(String acctGroupId, String tranTypeCd, Integer tranCatCd) {
        Optional<DisclosureGroup> group = disclosureGroupRepository.findById(
                new DisclosureGroup.DisclosureGroupId(acctGroupId, tranTypeCd, tranCatCd));
        if (group.isPresent()) {
            return group.get();
        }
        // DISCGRP-STATUS = '23': MOVE 'DEFAULT' TO FD-DIS-ACCT-GROUP-ID; PERFORM 1200-A-...
        log.info("DISCLOSURE GROUP RECORD MISSING - TRY WITH DEFAULT GROUP CODE");
        return disclosureGroupRepository.findById(
                        new DisclosureGroup.DisclosureGroupId(
                                DisclosureGroup.DEFAULT_GROUP_ID, tranTypeCd, tranCatCd))
                .orElseThrow(() -> abend("ERROR READING DEFAULT DISCLOSURE GROUP for type "
                        + tranTypeCd + " cat " + tranCatCd));
    }

    /**
     * {@code 1300-COMPUTE-INTEREST} (CBACT04C lines 462-470):
     * {@code WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}, truncated to scale 2; the
     * amount is added to the per-account running total and an interest transaction is written.
     */
    private void computeInterest(RunState state, TranCatBalance balance, DisclosureGroup disclosureGroup) {
        BigDecimal monthlyInterest = balance.getTranCatBal()
                .multiply(disclosureGroup.getIntRate())
                .divide(MONTHLY_DIVISOR, MONEY_SCALE, RoundingMode.DOWN);

        state.totalInterest = state.totalInterest.add(monthlyInterest);       // ADD WS-MONTHLY-INT TO WS-TOTAL-INT
        state.grandTotalInterest = state.grandTotalInterest.add(monthlyInterest);
        writeTransaction(state, monthlyInterest);                             // 1300-B-WRITE-TX
    }

    /** {@code 1400-COMPUTE-FEES} (CBACT04C lines 518-520): "To be implemented" — intentional no-op. */
    private void computeFees() {
        // no-op (matches COBOL stub)
    }

    /**
     * {@code 1300-B-WRITE-TX} (CBACT04C lines 473-515): build and write one interest transaction.
     * The transaction id is {@code PARM-DATE} (10) followed by a 6-digit running suffix.
     */
    private void writeTransaction(RunState state, BigDecimal monthlyInterest) {
        state.tranidSuffix++;                                                 // ADD 1 TO WS-TRANID-SUFFIX

        Transaction tx = new Transaction();
        // STRING PARM-DATE, WS-TRANID-SUFFIX DELIMITED BY SIZE INTO TRAN-ID
        tx.setTranId(state.processingDate + String.format("%06d", state.tranidSuffix));
        tx.setTypeCd(INTEREST_TRAN_TYPE);                                    // MOVE '01' TO TRAN-TYPE-CD
        tx.setCatCd(INTEREST_TRAN_CAT);                                      // MOVE '05' TO TRAN-CAT-CD
        tx.setSource(INTEREST_TRAN_SOURCE);                                  // MOVE 'System' TO TRAN-SOURCE
        // STRING 'Int. for a/c ', ACCT-ID INTO TRAN-DESC (ACCT-ID is PIC 9(11), zero-padded)
        tx.setDescription("Int. for a/c " + String.format("%011d", state.currentAccount.getAcctId()));
        tx.setAmount(monthlyInterest);                                       // MOVE WS-MONTHLY-INT TO TRAN-AMT
        tx.setMerchantId(0L);                                                // MOVE 0 TO TRAN-MERCHANT-ID
        tx.setMerchantName("");                                              // MOVE SPACES ...
        tx.setMerchantCity("");
        tx.setMerchantZip("");
        tx.setCardNum(state.currentXref.getCardNum());                       // MOVE XREF-CARD-NUM TO TRAN-CARD-NUM
        String timestamp = currentTimestamp();                              // Z-GET-DB2-FORMAT-TIMESTAMP
        tx.setOrigTs(timestamp);                                             // MOVE DB2-FORMAT-TS TO TRAN-ORIG-TS
        tx.setProcTs(timestamp);                                             // MOVE DB2-FORMAT-TS TO TRAN-PROC-TS

        transactionRepository.save(tx);                                      // WRITE FD-TRANFILE-REC
    }

    /** {@code Z-GET-DB2-FORMAT-TIMESTAMP}: current timestamp, DB2 format {@code YYYY-MM-DD-HH.MM.SS.NNNNNN}. */
    private String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));
    }

    /** {@code 9999-ABEND-PROGRAM}: log and raise (rolls back the batch transaction). */
    private InterestCalculationException abend(String message) {
        log.error("ABENDING PROGRAM: {}", message);
        return new InterestCalculationException(message);
    }

    /**
     * Per-run mutable state, replacing CBACT04C's WORKING-STORAGE scalars
     * (WS-LAST-ACCT-NUM, WS-TOTAL-INT, WS-FIRST-TIME, WS-RECORD-COUNT, WS-TRANID-SUFFIX, …).
     */
    private static final class RunState {
        private final String processingDate;
        private Long lastAcctNum = null;                 // WS-LAST-ACCT-NUM
        private boolean firstTime = true;                // WS-FIRST-TIME = 'Y'
        private BigDecimal totalInterest = BigDecimal.ZERO.setScale(MONEY_SCALE);   // WS-TOTAL-INT
        private BigDecimal grandTotalInterest = BigDecimal.ZERO.setScale(MONEY_SCALE);
        private long recordCount = 0;                    // WS-RECORD-COUNT
        private long tranidSuffix = 0;                   // WS-TRANID-SUFFIX
        private long accountsUpdated = 0;
        private Account currentAccount;                  // ACCOUNT-RECORD
        private CardXref currentXref;                    // CARD-XREF-RECORD

        private RunState(String processingDate) {
            this.processingDate = processingDate;
        }
    }
}
