package com.carddemo.service;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CBACT04C - INTCALC: monthly interest calculation.
 *
 * <p>For every transaction category balance of an account the interest rate is looked up in the
 * disclosure group of the account (falling back to the DEFAULT group), interest is computed as
 * {@code balance * rate / 1200}, written as a system generated transaction (type 01, category 05)
 * and added to the account balance. The cycle totals are reset once the account is complete.
 */
@Service
public class InterestCalculationService {

    public static final String INTEREST_TYPE_CODE = "01";
    public static final int INTEREST_CATEGORY_CODE = 5;

    private static final BigDecimal MONTHS_TIMES_PERCENT = BigDecimal.valueOf(1200);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransactionCategoryBalanceRepository categoryBalances;
    private final AccountRepository accounts;
    private final CardXrefRepository xrefs;
    private final DisclosureGroupRepository disclosureGroups;
    private final TransactionRepository transactions;
    private final TransactionIdGenerator idGenerator;

    public InterestCalculationService(TransactionCategoryBalanceRepository categoryBalances,
                                      AccountRepository accounts,
                                      CardXrefRepository xrefs,
                                      DisclosureGroupRepository disclosureGroups,
                                      TransactionRepository transactions,
                                      TransactionIdGenerator idGenerator) {
        this.categoryBalances = categoryBalances;
        this.accounts = accounts;
        this.xrefs = xrefs;
        this.disclosureGroups = disclosureGroups;
        this.transactions = transactions;
        this.idGenerator = idGenerator;
    }

    /** Processes one account, mirroring the account break logic of the COBOL program. */
    @Transactional
    public BigDecimal calculateForAccount(Long accountId) {
        Optional<Account> maybeAccount = accounts.findById(accountId);
        if (maybeAccount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Account account = maybeAccount.get();
        String cardNumber = xrefs.findFirstByAccountIdOrderByCardNumber(accountId)
                .map(CardXref::getCardNumber)
                .orElse(null);

        List<TransactionCategoryBalance> balances =
                categoryBalances.findByIdAccountIdOrderByIdTypeCodeAscIdCategoryCodeAsc(accountId);

        BigDecimal totalInterest = BigDecimal.ZERO;
        for (TransactionCategoryBalance balance : balances) {
            BigDecimal rate = interestRate(account.getGroupId(), balance);
            if (rate.signum() == 0) {
                continue;
            }
            BigDecimal interest = nz(balance.getBalance())
                    .multiply(rate)
                    .divide(MONTHS_TIMES_PERCENT, 2, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(interest);
            writeInterestTransaction(account, cardNumber, interest);
        }

        account.setCurrentBalance(nz(account.getCurrentBalance()).add(totalInterest));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accounts.save(account);
        return totalInterest;
    }

    private BigDecimal interestRate(String groupId, TransactionCategoryBalance balance) {
        String group = groupId == null || groupId.isBlank() ? DisclosureGroup.DEFAULT_GROUP_ID : groupId.trim();
        String typeCode = balance.getId().getTypeCode();
        Integer categoryCode = balance.getId().getCategoryCode();
        return disclosureGroups.findById(new DisclosureGroupId(group, typeCode, categoryCode))
                .or(() -> disclosureGroups.findById(
                        new DisclosureGroupId(DisclosureGroup.DEFAULT_GROUP_ID, typeCode, categoryCode)))
                .map(DisclosureGroup::getInterestRate)
                .orElse(BigDecimal.ZERO);
    }

    private void writeInterestTransaction(Account account, String cardNumber, BigDecimal interest) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        Transaction transaction = new Transaction();
        transaction.setId(idGenerator.next());
        transaction.setTypeCode(INTEREST_TYPE_CODE);
        transaction.setCategoryCode(INTEREST_CATEGORY_CODE);
        transaction.setSource("System");
        transaction.setDescription("Int. for a/c " + account.getId());
        transaction.setAmount(interest);
        transaction.setMerchantId(0L);
        transaction.setMerchantName("");
        transaction.setMerchantCity("");
        transaction.setMerchantZip("");
        transaction.setCardNumber(cardNumber);
        transaction.setOriginTimestamp(timestamp);
        transaction.setProcessingTimestamp(timestamp);
        transactions.save(transaction);
    }


    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
