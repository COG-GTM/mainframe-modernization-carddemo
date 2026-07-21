package com.carddemo.billpay.config;

import com.carddemo.billpay.domain.AccountEntity;
import com.carddemo.billpay.domain.CardXrefEntity;
import com.carddemo.billpay.domain.TransactionEntity;
import com.carddemo.billpay.repository.AccountRepository;
import com.carddemo.billpay.repository.CardXrefRepository;
import com.carddemo.billpay.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds demo data for the running application (screen recordings, manual UI checks).
 * Active only under the {@code demo} profile so the test suite starts from empty tables.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public DemoDataSeeder(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;
        }
        accountRepository.save(account(11L, new BigDecimal("123.45"), "90210", "GRP001"));
        accountRepository.save(account(12L, new BigDecimal("0.00"), "10001", "GRP001"));
        accountRepository.save(account(13L, new BigDecimal("5000.00"), "60601", "GRP002"));

        cardXrefRepository.save(xref("4111111111111111", 1L, 11L));
        cardXrefRepository.save(xref("4000000000000012", 2L, 12L));
        cardXrefRepository.save(xref("4222222222222222", 3L, 13L));

        TransactionEntity seedTxn = new TransactionEntity();
        seedTxn.setId("0000000000000100");
        seedTxn.setTypeCode("01");
        seedTxn.setCategoryCode(1);
        seedTxn.setSource("POS TERM");
        seedTxn.setDescription("INITIAL PURCHASE");
        seedTxn.setAmount(new BigDecimal("50.00"));
        seedTxn.setMerchantId(123456789L);
        seedTxn.setMerchantName("ACME STORE");
        seedTxn.setMerchantCity("ANYTOWN");
        seedTxn.setMerchantZip("90210");
        seedTxn.setCardNum("4111111111111111");
        seedTxn.setOrigTs("2024-06-01 10:00:00.000000");
        seedTxn.setProcTs("2024-06-01 10:00:00.000000");
        transactionRepository.save(seedTxn);
    }

    private static AccountEntity account(Long id, BigDecimal bal, String zip, String group) {
        AccountEntity a = new AccountEntity();
        a.setAcctId(id);
        a.setActiveStatus("Y");
        a.setCurrBal(bal);
        a.setCreditLimit(new BigDecimal("5000.00"));
        a.setCashCreditLimit(new BigDecimal("1000.00"));
        a.setOpenDate("2020-01-01");
        a.setExpirationDate("2027-01-01");
        a.setReissueDate("2024-01-01");
        a.setCurrCycCredit(new BigDecimal("0.00"));
        a.setCurrCycDebit(new BigDecimal("0.00"));
        a.setAddrZip(zip);
        a.setGroupId(group);
        return a;
    }

    private static CardXrefEntity xref(String card, Long cust, Long acct) {
        CardXrefEntity x = new CardXrefEntity();
        x.setCardNum(card);
        x.setCustId(cust);
        x.setAcctId(acct);
        return x;
    }
}
