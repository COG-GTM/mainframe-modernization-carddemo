package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InterestCalculationService {
    private static final Logger log = LoggerFactory.getLogger(InterestCalculationService.class);
    private final TransactionCategoryBalanceRepository tcbRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountRepository accountRepository;

    public InterestCalculationService(TransactionCategoryBalanceRepository tcbRepository,
                                       DisclosureGroupRepository disclosureGroupRepository,
                                       AccountRepository accountRepository) {
        this.tcbRepository = tcbRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public BigDecimal calculateInterestForAccount(Long acctId) {
        Account account = accountRepository.findById(acctId).orElse(null);
        if (account == null) {
            log.warn("Account not found: {}", acctId);
            return BigDecimal.ZERO;
        }

        List<TransactionCategoryBalance> balances = tcbRepository.findByAcctId(acctId);
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TransactionCategoryBalance tcb : balances) {
            if (tcb.getTranCatBal() == null || tcb.getTranCatBal().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            DisclosureGroup dg = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(account.getGroupId(), tcb.getTranTypeCd(), tcb.getTranCatCd())
                .orElse(null);

            if (dg == null || dg.getIntRate() == null) {
                continue;
            }

            // Monthly interest = balance * (annual rate / 12 / 100)
            BigDecimal monthlyRate = dg.getIntRate().divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            BigDecimal interest = tcb.getTranCatBal().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(interest);
            log.info("Account {}: category {}/{} balance={} rate={}% interest={}",
                acctId, tcb.getTranTypeCd(), tcb.getTranCatCd(), tcb.getTranCatBal(), dg.getIntRate(), interest);
        }

        // Update account balance with interest
        if (totalInterest.compareTo(BigDecimal.ZERO) > 0) {
            account.setCurrBal(account.getCurrBal().add(totalInterest));
            accountRepository.save(account);
            log.info("Account {}: total interest charged = {}", acctId, totalInterest);
        }

        return totalInterest;
    }
}
