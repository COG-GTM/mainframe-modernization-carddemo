package com.carddemo.batch.orchestration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Launches the CS-14 {@code intcalcPipelineJob} (INTCALC + COMBTRAN) end-to-end against H2 through
 * {@link BatchJobLauncherService} and asserts the pipeline completes and interest was applied:
 * the account balance is updated to the exact {@link BigDecimal} and the system interest
 * transactions are persisted into the {@code card_transaction} master (the COMBTRAN merge target).
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs14-intcalc;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class IntcalcPipelineJobTest {

    private static final String ACCT_A = "77000000001";
    private static final String CARD_A = "7700000000000001";
    private static final String CUST_A = "770000001";
    private static final String GROUP_A = "GRP14A";

    @Autowired
    private BatchJobLauncherService launcher;
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
    void setUp() {
        transactionRepository.deleteAll();
        tcatBalRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        customerRepository.save(customer(CUST_A));
        accountRepository.save(account(ACCT_A, GROUP_A));
        cardRepository.save(card(CARD_A, ACCT_A));
        cardXrefRepository.save(cardXref(CARD_A, CUST_A, ACCT_A));

        // Balance 500.00 @ 24.00% annual → 500 * 24 / 1200 = 10.00 monthly interest.
        disclosureGroupRepository.save(disc(GROUP_A, "01", 10, "24.00"));
        tcatBalRepository.save(tcatBal(ACCT_A, "01", 10, "500.00"));
    }

    @Test
    void runsIntcalcPipelineAndAppliesInterest() {
        BatchJobLaunchResult result = launcher.launch("intcalc",
                java.util.Map.of("parmDate", "2026-07-09"));

        assertThat(result.getPipeline()).isEqualTo("intcalc");
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        // Account balance updated: 100.00 + 10.00 = 110.00, cycle buckets reset.
        Account a = accountRepository.findById(ACCT_A).orElseThrow();
        assertThat(a.getAcctCurrBal()).isEqualByComparingTo("110.00");
        assertThat(a.getAcctCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(a.getAcctCurrCycDebit()).isEqualByComparingTo("0.00");

        // One interest transaction merged into the card_transaction master (COMBTRAN target).
        List<Transaction> txA = transactionRepository.findByTranCardNum(CARD_A);
        assertThat(txA).hasSize(1);
        assertThat(txA.get(0).getTranAmt()).isEqualByComparingTo("10.00");
        assertThat(txA.get(0).getTranSource()).isEqualTo("System");
    }

    private static Customer customer(String id) {
        Customer c = new Customer();
        c.setCustId(id);
        c.setCustFirstName("TEST");
        c.setCustLastName("USER");
        c.setCustFicoCreditScore(700);
        return c;
    }

    private static Account account(String id, String group) {
        Account a = new Account();
        a.setAcctId(id);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal("100.00"));
        a.setAcctCreditLimit(new BigDecimal("5000.00"));
        a.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        a.setAcctCurrCycCredit(new BigDecimal("5.00"));
        a.setAcctCurrCycDebit(new BigDecimal("7.00"));
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
