package com.carddemo.batch.interest;

import com.carddemo.entity.*;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Interest calculation processor — translates CBACT04C.cbl precisely.
 * 
 * For each TransactionCategoryBalance record:
 * 1. Look up DisclosureGroup by (account's groupId, tranTypeCd, tranCatCd)
 * 2. If not found, fall back to group ID = 'DEFAULT' (mirrors DISCGRP-STATUS = '23')
 * 3. Compute monthly interest: categoryBalance * interestRate / 1200 with RoundingMode.HALF_UP
 * 4. Create an interest Transaction record
 */
public class InterestCalculationProcessor implements ItemProcessor<TransactionCategoryBalance, Transaction> {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationProcessor.class);
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("1200");

    private final AccountRepository accountRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;

    public InterestCalculationProcessor(AccountRepository accountRepository,
                                         DisclosureGroupRepository disclosureGroupRepository) {
        this.accountRepository = accountRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
    }

    @Override
    public Transaction process(TransactionCategoryBalance tcb) throws Exception {
        Long acctId = tcb.getId().getAcctId();
        String tranTypeCd = tcb.getId().getTranTypeCd();
        Integer tranCatCd = tcb.getId().getTranCatCd();
        BigDecimal balance = tcb.getBalance();

        if (balance == null || balance.compareTo(BigDecimal.ZERO) == 0) {
            return null; // Skip zero-balance categories
        }

        // Look up account to get group ID
        Optional<Account> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            log.warn("Account not found for interest calculation: {}", acctId);
            return null;
        }

        Account account = acctOpt.get();
        String groupId = account.getGroupId();

        // Look up disclosure group for interest rate
        Optional<DisclosureGroup> dgOpt = disclosureGroupRepository
                .findByGroupIdAndTranTypeCdAndTranCatCd(groupId, tranTypeCd, tranCatCd);

        // Fall back to DEFAULT group if not found (mirrors DISCGRP-STATUS = '23')
        if (dgOpt.isEmpty()) {
            dgOpt = disclosureGroupRepository.findDefaultGroup(tranTypeCd, tranCatCd);
        }

        if (dgOpt.isEmpty()) {
            log.warn("No disclosure group found for acct={}, type={}, cat={}", acctId, tranTypeCd, tranCatCd);
            return null;
        }

        BigDecimal interestRate = dgOpt.get().getInterestRate();
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return null; // No interest to charge
        }

        // Compute monthly interest: categoryBalance * interestRate / 1200
        BigDecimal monthlyInterest = balance.multiply(interestRate)
                .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);

        if (monthlyInterest.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // Create interest transaction
        Transaction interestTran = new Transaction();
        interestTran.setTransactionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        interestTran.setCardNum(null); // Interest is per-account, not per-card
        interestTran.setTypeCd(tranTypeCd);
        interestTran.setCategoryCd(tranCatCd);
        interestTran.setSource("INTEREST");
        interestTran.setDescription("Monthly interest charge");
        interestTran.setAmount(monthlyInterest);
        interestTran.setOrigTimestamp(LocalDateTime.now());
        interestTran.setProcTimestamp(LocalDateTime.now());

        // Update account balance
        BigDecimal currentBalance = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
        account.setCurrentBalance(currentBalance.add(monthlyInterest));
        // Reset cycle credits/debits (mirrors COBOL: when account changes, reset)
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(account);

        return interestTran;
    }

    /**
     * 1400-COMPUTE-FEES: Stubbed in the COBOL source.
     * TODO: Implement fee computation when business rules are defined.
     */
    public BigDecimal computeFees(Account account) {
        // Stubbed — mirrors COBOL source where this is also stubbed
        return BigDecimal.ZERO;
    }
}
