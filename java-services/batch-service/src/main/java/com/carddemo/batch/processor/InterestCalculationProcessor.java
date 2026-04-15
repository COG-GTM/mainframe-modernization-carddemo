package com.carddemo.batch.processor;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interest calculation processor migrated from COBOL program CBACT04C.
 *
 * Business logic (mirrors CBACT04C 1300-COMPUTE-INTEREST):
 * 1. For each active account with positive balance
 * 2. Calculate monthly interest: balance * (annual_rate / 1200)
 *    - COBOL: COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 *    - Uses 22.99% default annual rate (mirrors 1200-GET-INTEREST-RATE default)
 * 3. Add interest to account current balance (mirrors 1050-UPDATE-ACCOUNT)
 * 4. Create a transaction record for the interest charge (mirrors 1300-B-WRITE-TX)
 *    - Type '01', category '05', source 'System'
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InterestCalculationProcessor implements ItemProcessor<Account, Account> {

    private static final BigDecimal DEFAULT_ANNUAL_RATE = new BigDecimal("22.99");
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("1200");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS000");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final AtomicLong tranIdSuffix = new AtomicLong(0);

    @Override
    public Account process(Account account) throws Exception {
        // Only process active accounts with positive balance
        if (!"Y".equals(account.getActiveStatus())) {
            log.debug("Skipping inactive account: {}", account.getAcctId());
            return null;
        }

        if (account.getCurrentBalance() == null
                || account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Skipping account {} with non-positive balance: {}",
                    account.getAcctId(), account.getCurrentBalance());
            return null;
        }

        // Calculate monthly interest: balance * (annual_rate / 1200)
        // Mirrors: COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
        BigDecimal monthlyInterest = calculateMonthlyInterest(
                account.getCurrentBalance(), DEFAULT_ANNUAL_RATE);

        log.info("Account {}: balance={}, interest={}",
                account.getAcctId(), account.getCurrentBalance(), monthlyInterest);

        // Add interest to current balance (mirrors 1050-UPDATE-ACCOUNT)
        account.setCurrentBalance(account.getCurrentBalance().add(monthlyInterest));

        // Reset cycle credits/debits (mirrors 1050-UPDATE-ACCOUNT)
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);

        // Create interest transaction record (mirrors 1300-B-WRITE-TX)
        createInterestTransaction(account, monthlyInterest);

        return account;
    }

    /**
     * Calculate monthly interest.
     * Mirrors CBACT04C 1300-COMPUTE-INTEREST:
     *   COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
     */
    BigDecimal calculateMonthlyInterest(BigDecimal balance, BigDecimal annualRate) {
        return balance.multiply(annualRate)
                .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }

    /**
     * Create a transaction record for the interest charge.
     * Mirrors CBACT04C 1300-B-WRITE-TX:
     *   MOVE '01' TO TRAN-TYPE-CD
     *   MOVE '05' TO TRAN-CAT-CD
     *   MOVE 'System' TO TRAN-SOURCE
     *   STRING 'Int. for a/c ' , ACCT-ID INTO TRAN-DESC
     */
    private void createInterestTransaction(Account account, BigDecimal interestAmount) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        long suffix = tranIdSuffix.incrementAndGet();
        String tranId = String.format("INT%013d", suffix);

        Transaction interestTran = Transaction.builder()
                .tranId(tranId)
                .typeCd("01")
                .catCd(5)
                .source("System")
                .description("MONTHLY INTEREST CHARGE for a/c " + account.getAcctId())
                .amount(interestAmount)
                .merchantId(0L)
                .merchantName("")
                .merchantCity("")
                .merchantZip("")
                .cardNum("")
                .origTimestamp(timestamp)
                .procTimestamp(timestamp)
                .build();

        transactionRepository.save(interestTran);
    }
}
