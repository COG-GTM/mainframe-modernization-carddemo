package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.TranCatBalance;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests for {@link InterestCalculationService}, the transpilation of CBACT04C.
 *
 * <p>Spring Batch auto-run is disabled so the job does not fire on context start; each test seeds
 * the repositories directly and invokes the service.
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class InterestCalculationServiceTest {

    private static final String PROCESSING_DATE = "2022071800";

    @Autowired
    private InterestCalculationService service;

    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        tranCatBalanceRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private Account account(long acctId, String groupId, String currBal) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setActiveStatus("Y");
        a.setGroupId(groupId);
        a.setCurrBal(new BigDecimal(currBal));
        a.setCurrCycCredit(new BigDecimal("150.00"));
        a.setCurrCycDebit(new BigDecimal("75.00"));
        return accountRepository.save(a);
    }

    private void xref(String cardNum, long custId, long acctId) {
        cardXrefRepository.save(new CardXref(cardNum, custId, acctId));
    }

    private void disclosure(String group, String type, int cat, String rate) {
        disclosureGroupRepository.save(new DisclosureGroup(group, type, cat, new BigDecimal(rate)));
    }

    private void balance(long acctId, String type, int cat, String bal) {
        tranCatBalanceRepository.save(new TranCatBalance(acctId, type, cat, new BigDecimal(bal)));
    }

    @Test
    void basicInterestCalculation() {
        // balance=1000, rate=12% -> monthly interest = (1000 * 12) / 1200 = 10.00
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("A000000000", "01", 1, "12.00");
        balance(1L, "01", 1, "1000.00");

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.transactionsWritten()).isEqualTo(1);
        assertThat(result.totalInterest()).isEqualByComparingTo("10.00");

        List<Transaction> txns = transactionRepository.findAll();
        assertThat(txns).hasSize(1);
        assertThat(txns.get(0).getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void zeroInterestRateSkipsComputation() {
        // IF DIS-INT-RATE NOT = 0 -> rate 0 produces no transaction, no interest.
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("A000000000", "01", 1, "0.00");
        balance(1L, "01", 1, "1000.00");

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.transactionsWritten()).isZero();
        assertThat(transactionRepository.findAll()).isEmpty();
        // Account still rewritten with zero interest added; cycle totals reset.
        Account updated = accountRepository.findById(1L).orElseThrow();
        assertThat(updated.getCurrBal()).isEqualByComparingTo("1000.00");
        assertThat(updated.getCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(updated.getCurrCycDebit()).isEqualByComparingTo("0.00");
    }

    @Test
    void fallsBackToDefaultDisclosureGroup() {
        // No A000000000/01/0001 row -> COBOL status '23' -> retry with DEFAULT group.
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("DEFAULT", "01", 1, "12.00");
        balance(1L, "01", 1, "1000.00");

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.transactionsWritten()).isEqualTo(1);
        assertThat(result.totalInterest()).isEqualByComparingTo("10.00");
    }

    @Test
    void accountBalanceUpdatedAndCycleFieldsReset() {
        // 1050-UPDATE-ACCOUNT: ACCT-CURR-BAL += total interest; cycle credit/debit -> 0.
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("A000000000", "01", 1, "12.00");
        balance(1L, "01", 1, "1000.00");

        service.calculateInterest(PROCESSING_DATE);

        Account updated = accountRepository.findById(1L).orElseThrow();
        assertThat(updated.getCurrBal()).isEqualByComparingTo("1010.00");
        assertThat(updated.getCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(updated.getCurrCycDebit()).isEqualByComparingTo("0.00");
    }

    @Test
    void multipleCategoryBalancesAccumulateInterest() {
        // Two categories for the same account -> interest accumulates into one balance update.
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("A000000000", "01", 1, "12.00");   // (1000*12)/1200 = 10.00
        disclosure("A000000000", "02", 2, "24.00");   // (500*24)/1200 = 10.00
        balance(1L, "01", 1, "1000.00");
        balance(1L, "02", 2, "500.00");

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.transactionsWritten()).isEqualTo(2);
        assertThat(result.totalInterest()).isEqualByComparingTo("20.00");

        Account updated = accountRepository.findById(1L).orElseThrow();
        assertThat(updated.getCurrBal()).isEqualByComparingTo("1020.00");
    }

    @Test
    void interestIsTruncatedNotRounded() {
        // COBOL COMPUTE has no ROUNDED clause -> truncate to scale 2.
        // (505 * 5) / 1200 = 2.104166... -> 2.10
        account(1L, "A000000000", "1000.00");
        xref("0000000000000001", 1L, 1L);
        disclosure("A000000000", "01", 1, "5.00");
        balance(1L, "01", 1, "505.00");

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.totalInterest()).isEqualByComparingTo("2.10");
    }

    @Test
    void transactionRecordFieldsMatchCobol() {
        // 1300-B-WRITE-TX: type '01', cat 0005, source 'System', desc 'Int. for a/c <acctId>'.
        account(7L, "A000000000", "1000.00");
        xref("4444333322221111", 9L, 7L);
        disclosure("A000000000", "01", 1, "12.00");
        balance(7L, "01", 1, "1000.00");

        service.calculateInterest(PROCESSING_DATE);

        Transaction tx = transactionRepository.findAll().get(0);
        assertThat(tx.getTranId()).isEqualTo("2022071800000001");
        assertThat(tx.getTypeCd()).isEqualTo("01");
        assertThat(tx.getCatCd()).isEqualTo(5);
        assertThat(tx.getSource()).isEqualTo("System");
        assertThat(tx.getDescription()).isEqualTo("Int. for a/c 00000000007");
        assertThat(tx.getCardNum()).isEqualTo("4444333322221111");
        assertThat(tx.getMerchantId()).isZero();
        assertThat(tx.getOrigTs()).isNotBlank();
        assertThat(tx.getProcTs()).isEqualTo(tx.getOrigTs());
    }

    @Test
    void multipleAccountsEachUpdatedIndependently() {
        // Account-change boundary flushes the previous account; final account flushed after loop.
        account(1L, "A000000000", "1000.00");
        account(2L, "A000000000", "2000.00");
        xref("0000000000000001", 1L, 1L);
        xref("0000000000000002", 2L, 2L);
        disclosure("A000000000", "01", 1, "12.00");
        balance(1L, "01", 1, "1000.00");   // interest 10.00 -> acct1 1010.00
        balance(2L, "01", 1, "2000.00");   // interest 20.00 -> acct2 2020.00

        InterestCalculationResult result = service.calculateInterest(PROCESSING_DATE);

        assertThat(result.accountsUpdated()).isEqualTo(2);
        assertThat(accountRepository.findById(1L).orElseThrow().getCurrBal()).isEqualByComparingTo("1010.00");
        assertThat(accountRepository.findById(2L).orElseThrow().getCurrBal()).isEqualByComparingTo("2020.00");
    }
}
