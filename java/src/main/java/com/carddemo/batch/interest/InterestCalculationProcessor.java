package com.carddemo.batch.interest;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.DisclosureGroupId;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: CBACT04C — driver of the interest calculation (replaces
 * {@code app/jcl/INTCALC.jcl}, PGM=CBACT04C PARM='yyyymmddhh').
 *
 * <p>Reproduces the PROCEDURE DIVISION control break over TCATBALF (CVTRA01Y): every
 * transaction category balance row is read in key order, the account (CVACT01Y) and its card
 * cross-reference (CVACT03Y) are fetched on each account change, the disclosure group
 * (CVTRA02Y) supplies the annual rate, and one interest transaction (CVTRA05Y) is written per
 * non-zero rate.
 *
 * <p>Two behaviours of the original program are preserved deliberately:
 * <ul>
 *   <li>a DISCGRP miss (VSAM status 23) retries the lookup under the {@code DEFAULT} group,
 *       and a miss on that retry abends the program (1200-A-GET-DEFAULT-INT-RATE);</li>
 *   <li>the accumulated interest of the <em>last</em> account of the file is never posted:
 *       1050-UPDATE-ACCOUNT only runs on an account change, and the ELSE branch of the main
 *       PERFORM that would flush the final account is unreachable because the PERFORM UNTIL
 *       test already ends the loop when END-OF-FILE is set. The interest transactions of that
 *       last account are still written.</li>
 * </ul>
 */
@Service
public class InterestCalculationProcessor {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationProcessor.class);

    private static final Sort TCATBAL_KEY_ORDER =
            Sort.by("id.accountId", "id.typeCode", "id.categoryCode");

    private final TransactionCategoryBalanceRepository categoryBalanceRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;
    private final InterestCalculationService interestCalculationService;

    public InterestCalculationProcessor(TransactionCategoryBalanceRepository categoryBalanceRepository,
                                        AccountRepository accountRepository,
                                        CardXrefRepository cardXrefRepository,
                                        DisclosureGroupRepository disclosureGroupRepository,
                                        TransactionRepository transactionRepository,
                                        InterestCalculationService interestCalculationService) {
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
        this.interestCalculationService = interestCalculationService;
    }

    /**
     * Runs the whole job for the given PARM date, using {@code now} wherever the COBOL calls
     * FUNCTION CURRENT-DATE (Z-GET-DB2-FORMAT-TIMESTAMP).
     */
    @Transactional
    public InterestCalculationResult run(String parmDate, LocalDateTime now) {
        List<TransactionCategoryBalance> balances = categoryBalanceRepository.findAll(TCATBAL_KEY_ORDER);

        Long lastAccountId = null;
        boolean firstTime = true;
        BigDecimal totalInterest = BigDecimal.ZERO;
        Account account = null;
        CardXref cardXref = null;

        long recordCount = 0;
        int tranIdSuffix = 0;
        long accountsUpdated = 0;
        BigDecimal totalInterestPosted = BigDecimal.ZERO;

        for (TransactionCategoryBalance balance : balances) {
            recordCount++;
            Long accountId = balance.getId().getAccountId();

            if (!accountId.equals(lastAccountId)) {
                if (!firstTime) {
                    interestCalculationService.applyTotalInterest(account, totalInterest);
                    accountRepository.save(account);
                    accountsUpdated++;
                    totalInterestPosted = totalInterestPosted.add(totalInterest);
                } else {
                    firstTime = false;
                }
                totalInterest = BigDecimal.ZERO;
                lastAccountId = accountId;
                account = getAccountData(accountId);
                cardXref = getXrefData(accountId);
            }

            BigDecimal rate = getInterestRate(account, balance);
            if (rate.signum() != 0) {
                BigDecimal monthlyInterest =
                        interestCalculationService.monthlyInterest(balance.getBalance(), rate);
                totalInterest = totalInterest.add(monthlyInterest);
                tranIdSuffix++;
                Transaction transaction = interestCalculationService.buildInterestTransaction(
                        parmDate, tranIdSuffix, account.getAccountId(), cardXref.getCardNumber(),
                        monthlyInterest, now);
                transactionRepository.save(transaction);
            }
        }

        return new InterestCalculationResult(recordCount, tranIdSuffix, accountsUpdated,
                totalInterestPosted);
    }

    /** 1100-GET-ACCT-DATA: random read of ACCTFILE on ACCT-ID. */
    private Account getAccountData(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + accountId));
    }

    /** 1110-GET-XREF-DATA: read of the XREFFILE alternate index on XREF-ACCT-ID. */
    private CardXref getXrefData(Long accountId) {
        return cardXrefRepository.findFirstByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + accountId));
    }

    /**
     * 1200-GET-INTEREST-RATE plus 1200-A-GET-DEFAULT-INT-RATE: the account group is tried
     * first and a status 23 (record not found) falls back to the {@code DEFAULT} group.
     */
    private BigDecimal getInterestRate(Account account, TransactionCategoryBalance balance) {
        String accountGroupId = CobolUtils.padRight(account.getGroupId(), 10).trim();
        String typeCode = balance.getId().getTypeCode();
        Integer categoryCode = balance.getId().getCategoryCode();

        Optional<DisclosureGroup> group = findDisclosureGroup(accountGroupId, typeCode, categoryCode);
        if (group.isEmpty()) {
            log.debug("DISCLOSURE GROUP RECORD MISSING, TRY WITH DEFAULT GROUP CODE: {} {} {}",
                    accountGroupId, typeCode, categoryCode);
            group = findDisclosureGroup(InterestCalculationService.DEFAULT_DISCLOSURE_GROUP,
                    typeCode, categoryCode);
            if (group.isEmpty()) {
                throw new IllegalStateException("ERROR READING DEFAULT DISCLOSURE GROUP: "
                        + typeCode + " " + categoryCode);
            }
        }
        return CobolUtils.nvl(group.get().getInterestRate());
    }

    private Optional<DisclosureGroup> findDisclosureGroup(String accountGroupId,
                                                          String typeCode,
                                                          Integer categoryCode) {
        return disclosureGroupRepository.findById(DisclosureGroupId.builder()
                .accountGroupId(accountGroupId)
                .transactionTypeCode(typeCode)
                .transactionCategoryCode(categoryCode)
                .build());
    }
}
