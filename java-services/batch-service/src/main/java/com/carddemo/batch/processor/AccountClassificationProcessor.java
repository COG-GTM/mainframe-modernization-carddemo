package com.carddemo.batch.processor;

import com.carddemo.batch.model.Account;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Account classification processor migrated from COBOL program CBACT01C.
 *
 * Business logic:
 * 1. Read all accounts
 * 2. Classify into active (status='Y') and inactive (status='N')
 * 3. Generate summary counts and totals per group
 *
 * Mirrors CBACT01C which reads the account file sequentially and writes to
 * multiple output files based on account attributes.
 */
@Component
@Slf4j
@Getter
public class AccountClassificationProcessor implements ItemProcessor<Account, Account> {

    private final AtomicInteger activeCount = new AtomicInteger(0);
    private final AtomicInteger inactiveCount = new AtomicInteger(0);
    private final AtomicReference<BigDecimal> activeTotalBalance =
            new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> inactiveTotalBalance =
            new AtomicReference<>(BigDecimal.ZERO);

    @Override
    public Account process(Account account) throws Exception {
        if ("Y".equals(account.getActiveStatus())) {
            activeCount.incrementAndGet();
            activeTotalBalance.updateAndGet(current ->
                    current.add(account.getCurrentBalance() != null
                            ? account.getCurrentBalance() : BigDecimal.ZERO));
            log.debug("Active account: {} balance: {}",
                    account.getAcctId(), account.getCurrentBalance());
        } else {
            inactiveCount.incrementAndGet();
            inactiveTotalBalance.updateAndGet(current ->
                    current.add(account.getCurrentBalance() != null
                            ? account.getCurrentBalance() : BigDecimal.ZERO));
            log.debug("Inactive account: {} balance: {}",
                    account.getAcctId(), account.getCurrentBalance());
        }
        return account;
    }

    public void resetCounters() {
        activeCount.set(0);
        inactiveCount.set(0);
        activeTotalBalance.set(BigDecimal.ZERO);
        inactiveTotalBalance.set(BigDecimal.ZERO);
    }

    public String getSummary() {
        return String.format(
                "Account Classification Summary:%n" +
                "  Active accounts:   %d (total balance: %s)%n" +
                "  Inactive accounts: %d (total balance: %s)%n" +
                "  Total accounts:    %d",
                activeCount.get(), activeTotalBalance.get().toPlainString(),
                inactiveCount.get(), inactiveTotalBalance.get().toPlainString(),
                activeCount.get() + inactiveCount.get()
        );
    }
}
