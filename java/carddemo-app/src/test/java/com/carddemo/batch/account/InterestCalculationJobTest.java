package com.carddemo.batch.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Runs {@code intcalcJob} (the {@code CBACT04C} INTCALC port) end-to-end against H2 with a
 * hand-built fixture, asserting the computed monthly interest, the accumulated account balance
 * update and the generated interest transactions match values computed by hand in
 * {@link BigDecimal} — including the truncation and {@code DEFAULT}-disclosure-group fallback.
 */
@SpringBootTest
@ActiveProfiles("test")
class InterestCalculationJobTest {

    private static final String ACCT_A = "90000000001";
    private static final String CARD_A = "9000000000000001";
    private static final String CUST_A = "900000001";
    private static final String GROUP_A = "GRPTST";

    private static final String ACCT_B = "90000000002";
    private static final String GROUP_B = "NOGRPXX";

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job intcalcJob;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private TransactionCategoryBalanceRepository tcatBalRepository;
    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUpFixture() {
        transactionRepository.deleteAll();

        // --- Account A: full customer/account/card/xref chain, account-specific rates ---------
        customerRepository.save(customer(CUST_A));
        accountRepository.save(account(ACCT_A, GROUP_A, "100.00", "5.00", "7.00"));
        cardRepository.save(card(CARD_A, ACCT_A));
        cardXrefRepository.save(cardXref(CARD_A, CUST_A, ACCT_A));

        disclosureGroupRepository.save(disc(GROUP_A, "01", 5, "18.00"));
        disclosureGroupRepository.save(disc(GROUP_A, "01", 10, "24.00"));
        disclosureGroupRepository.save(disc(GROUP_A, "02", 1, "0.00")); // zero rate → skipped

        tcatBalRepository.save(tcatBal(ACCT_A, "01", 5, "1234.56"));   // 18.5184 -> 18.51
        tcatBalRepository.save(tcatBal(ACCT_A, "01", 10, "500.00"));   // 10.00
        tcatBalRepository.save(tcatBal(ACCT_A, "02", 1, "999.99"));    // rate 0 -> no interest

        // --- Account B: no account-specific rows → DEFAULT disclosure-group fallback ----------
        accountRepository.save(account(ACCT_B, GROUP_B, "0.00", "0.00", "0.00"));
        disclosureGroupRepository.save(disc("DEFAULT", "03", 7, "12.00"));
        tcatBalRepository.save(tcatBal(ACCT_B, "03", 7, "250.00"));    // 2.50
    }

    @Test
    void computesInterestUpdatesBalanceAndWritesTransactions() throws Exception {
        JobExecution execution = jobLauncher.run(intcalcJob, new JobParametersBuilder()
                .addString("parmDate", "2026-07-09")
                .addLong("run", System.nanoTime())
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Account A: 100.00 + 18.51 + 10.00 = 128.51, cycle credit/debit reset to zero.
        Account a = accountRepository.findById(ACCT_A).orElseThrow();
        assertThat(a.getAcctCurrBal()).isEqualByComparingTo("128.51");
        assertThat(a.getAcctCurrBal().scale()).isEqualTo(2);
        assertThat(a.getAcctCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(a.getAcctCurrCycDebit()).isEqualByComparingTo("0.00");

        // Two interest transactions for account A (the zero-rate category is skipped).
        List<Transaction> txA = transactionRepository.findByTranCardNum(CARD_A);
        assertThat(txA).hasSize(2);
        assertThat(txA).extracting(Transaction::getTranAmt)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("18.51"), new BigDecimal("10.00"));
        assertThat(txA).allSatisfy(t -> {
            assertThat(t.getTranTypeCd()).isEqualTo("01");
            assertThat(t.getTranCatCd()).isEqualTo(5);
            assertThat(t.getTranSource()).isEqualTo("System");
            assertThat(t.getTranDesc()).isEqualTo("Int. for a/c " + ACCT_A);
            assertThat(t.getTranId()).startsWith("2026-07-09");
        });

        // Account B via DEFAULT group: 0.00 + 2.50 = 2.50, one transaction.
        Account b = accountRepository.findById(ACCT_B).orElseThrow();
        assertThat(b.getAcctCurrBal()).isEqualByComparingTo("2.50");
        List<Transaction> txB = transactionRepository.findAll().stream()
                .filter(t -> ("Int. for a/c " + ACCT_B).equals(t.getTranDesc()))
                .toList();
        assertThat(txB).hasSize(1);
        assertThat(txB.get(0).getTranAmt()).isEqualByComparingTo("2.50");
    }

    // --- fixture builders ----------------------------------------------------------------------

    private static Customer customer(String id) {
        Customer c = new Customer();
        c.setCustId(id);
        c.setCustFirstName("TEST");
        c.setCustLastName("USER");
        c.setCustFicoCreditScore(700);
        return c;
    }

    private static Account account(String id, String group, String bal, String cycCredit, String cycDebit) {
        Account a = new Account();
        a.setAcctId(id);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal(bal));
        a.setAcctCreditLimit(new BigDecimal("5000.00"));
        a.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        a.setAcctCurrCycCredit(new BigDecimal(cycCredit));
        a.setAcctCurrCycDebit(new BigDecimal(cycDebit));
        a.setAcctGroupId(group);
        return a;
    }

    private static Card card(String num, String acctId) {
        Card c = new Card();
        c.setCardNum(num);
        c.setCardAcctId(acctId);
        c.setCardActiveStatus("Y");
        return c;
    }

    private static CardXref cardXref(String cardNum, String custId, String acctId) {
        CardXref x = new CardXref();
        x.setXrefCardNum(cardNum);
        x.setXrefCustId(custId);
        x.setXrefAcctId(acctId);
        return x;
    }

    private static DisclosureGroup disc(String group, String type, int cat, String rate) {
        DisclosureGroup d = new DisclosureGroup();
        d.setId(new DisclosureGroupId(group, type, cat));
        d.setDisIntRate(new BigDecimal(rate));
        return d;
    }

    private static TransactionCategoryBalance tcatBal(String acctId, String type, int cat, String bal) {
        TransactionCategoryBalance b = new TransactionCategoryBalance();
        b.setId(new TransactionCategoryBalanceId(acctId, type, cat));
        b.setTranCatBal(new BigDecimal(bal));
        return b;
    }
}
