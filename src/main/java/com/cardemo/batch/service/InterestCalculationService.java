package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core interest calculation service ported from COBOL program CBACT04C.
 * Implements all business rules from paragraphs 1050 through 1400.
 */
@Service
public class InterestCalculationService {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationService.class);
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");
    private static final String DEFAULT_GROUP_ID = "DEFAULT";
    private static final String INTEREST_TRAN_TYPE_CD = "01";
    private static final String INTEREST_TRAN_CAT_CD = "05";
    private static final String TRAN_SOURCE = "System";
    private static final DateTimeFormatter DB2_TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    private final AtomicLong recordCount = new AtomicLong(0);
    private final AtomicLong tranIdSuffix = new AtomicLong(0);

    private final Counter recordsProcessedCounter;
    private final Counter transactionsGeneratedCounter;

    public InterestCalculationService(
            DisclosureGroupRepository disclosureGroupRepository,
            AccountRepository accountRepository,
            CardXrefRepository cardXrefRepository,
            TransactionRecordRepository transactionRecordRepository,
            MeterRegistry meterRegistry) {
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.recordsProcessedCounter = Counter.builder("interest.records.processed")
                .description("Number of category balance records processed")
                .register(meterRegistry);
        this.transactionsGeneratedCounter = Counter.builder("interest.transactions.generated")
                .description("Number of interest transactions generated")
                .register(meterRegistry);
    }

    /**
     * Compute monthly interest: (balance * rate) / 1200.
     * Corresponds to paragraph 1300-COMPUTE-INTEREST.
     */
    public BigDecimal computeMonthlyInterest(BigDecimal balance, BigDecimal interestRate) {
        if (balance == null || interestRate == null) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(interestRate)
                .divide(TWELVE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * Look up the interest rate from the disclosure group table.
     * First tries the account's group ID; falls back to 'DEFAULT'.
     * Corresponds to paragraphs 1200-GET-INTEREST-RATE and 1200-A-GET-DEFAULT-INT-RATE.
     */
    public BigDecimal getInterestRate(String acctGroupId, String tranTypeCd, String tranCatCd) {
        Optional<DisclosureGroup> discGroup = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(acctGroupId, tranTypeCd, tranCatCd);

        if (discGroup.isPresent()) {
            return discGroup.get().getIntRate();
        }

        log.info("Disclosure group not found for group={}, type={}, cat={}. Trying DEFAULT.",
                acctGroupId, tranTypeCd, tranCatCd);

        Optional<DisclosureGroup> defaultGroup = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(DEFAULT_GROUP_ID, tranTypeCd, tranCatCd);

        if (defaultGroup.isPresent()) {
            return defaultGroup.get().getIntRate();
        }

        log.warn("DEFAULT disclosure group not found for type={}, cat={}. Using zero rate.",
                tranTypeCd, tranCatCd);
        return BigDecimal.ZERO;
    }

    /**
     * Update account after processing all category balances.
     * Adds total interest to current balance and resets cycle counters.
     * Corresponds to paragraph 1050-UPDATE-ACCOUNT.
     */
    @Transactional
    public void updateAccount(String acctId, BigDecimal totalInterest) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + acctId));

        BigDecimal currentBal = account.getCurrBal() != null ? account.getCurrBal() : BigDecimal.ZERO;
        account.setCurrBal(currentBal.add(totalInterest));
        account.setCurrCycCredit(BigDecimal.ZERO);
        account.setCurrCycDebit(BigDecimal.ZERO);

        accountRepository.save(account);
        log.info("Updated account {}: added interest {}, new balance {}",
                acctId, totalInterest, account.getCurrBal());
    }

    /**
     * Generate an interest transaction record.
     * Corresponds to paragraph 1300-B-WRITE-TX.
     */
    public TransactionRecord generateInterestTransaction(
            String parmDate,
            String acctId,
            BigDecimal monthlyInterest,
            String cardNum) {

        long suffix = tranIdSuffix.incrementAndGet();
        String tranId = String.format("%s%06d", parmDate, suffix);

        TransactionRecord txn = new TransactionRecord();
        txn.setTranId(tranId);
        txn.setTranTypeCd(INTEREST_TRAN_TYPE_CD);
        txn.setTranCatCd(INTEREST_TRAN_CAT_CD);
        txn.setTranSource(TRAN_SOURCE);
        txn.setTranDesc("Int. for a/c " + acctId);
        txn.setTranAmt(monthlyInterest);
        txn.setMerchantId("0");
        txn.setMerchantName("");
        txn.setMerchantCity("");
        txn.setMerchantZip("");
        txn.setCardNum(cardNum != null ? cardNum : "");

        String timestamp = LocalDateTime.now().format(DB2_TIMESTAMP_FMT);
        txn.setOrigTs(timestamp);
        txn.setProcTs(timestamp);

        transactionsGeneratedCounter.increment();
        return txn;
    }

    /**
     * Fee computation placeholder. Currently a no-op stub.
     * Corresponds to paragraph 1400-COMPUTE-FEES ("To be implemented").
     */
    public BigDecimal computeFees(TransactionCategoryBalance catBalance) {
        // Stub: To be implemented
        return BigDecimal.ZERO;
    }

    /**
     * Look up the card number for an account via the cross-reference table.
     * Corresponds to paragraph 1110-GET-XREF-DATA.
     */
    public String lookupCardNumber(String acctId) {
        return cardXrefRepository.findFirstByAcctId(acctId)
                .map(CardXref::getCardNum)
                .orElse("");
    }

    /**
     * Look up the account's group ID.
     * Corresponds to paragraph 1100-GET-ACCT-DATA.
     */
    public Account getAccountData(String acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + acctId));
    }

    /**
     * Process all transaction category balance records and generate interest transactions.
     * This is the main batch loop corresponding to the PROCEDURE DIVISION main loop.
     *
     * @param categoryBalances all category balance records ordered by acctId, typeCd, catCd
     * @param parmDate the run date parameter (LINKAGE SECTION PARM-DATE)
     * @return list of generated interest transaction records
     */
    @Transactional
    public List<TransactionRecord> processAllRecords(
            List<TransactionCategoryBalance> categoryBalances,
            String parmDate) {

        List<TransactionRecord> generatedTransactions = new ArrayList<>();
        String lastAcctId = null;
        boolean firstTime = true;
        BigDecimal totalInterest = BigDecimal.ZERO;
        String currentCardNum = "";
        String currentGroupId = "";

        for (TransactionCategoryBalance catBal : categoryBalances) {
            recordCount.incrementAndGet();
            recordsProcessedCounter.increment();

            String currentAcctId = catBal.getAcctId();

            // Account change detection (paragraph main loop lines 194-206)
            if (!currentAcctId.equals(lastAcctId)) {
                if (!firstTime) {
                    updateAccount(lastAcctId, totalInterest);
                } else {
                    firstTime = false;
                }

                totalInterest = BigDecimal.ZERO;
                lastAcctId = currentAcctId;

                // Load account data (1100-GET-ACCT-DATA)
                Account account = getAccountData(currentAcctId);
                currentGroupId = account.getGroupId() != null ? account.getGroupId() : "";

                // Load cross-reference data (1110-GET-XREF-DATA)
                currentCardNum = lookupCardNumber(currentAcctId);
            }

            // Look up interest rate (1200-GET-INTEREST-RATE with fallback)
            BigDecimal interestRate = getInterestRate(
                    currentGroupId, catBal.getTypeCd(), catBal.getCatCd());

            if (interestRate.compareTo(BigDecimal.ZERO) != 0) {
                // Compute interest (1300-COMPUTE-INTEREST)
                BigDecimal monthlyInt = computeMonthlyInterest(catBal.getTranCatBal(), interestRate);
                totalInterest = totalInterest.add(monthlyInt);

                // Generate transaction record (1300-B-WRITE-TX)
                TransactionRecord txn = generateInterestTransaction(
                        parmDate, currentAcctId, monthlyInt, currentCardNum);
                generatedTransactions.add(txn);

                // Compute fees (1400-COMPUTE-FEES) - stub
                computeFees(catBal);
            }
        }

        // Update the last account after the loop ends (line 220)
        if (lastAcctId != null) {
            updateAccount(lastAcctId, totalInterest);
        }

        // Persist all generated transactions
        transactionRecordRepository.saveAll(generatedTransactions);

        log.info("Interest calculation complete. Records processed: {}, Transactions generated: {}",
                recordCount.get(), generatedTransactions.size());

        return generatedTransactions;
    }

    public long getRecordCount() {
        return recordCount.get();
    }

    public long getTranIdSuffix() {
        return tranIdSuffix.get();
    }

    public void resetCounters() {
        recordCount.set(0);
        tranIdSuffix.set(0);
    }
}
