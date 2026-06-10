package com.carddemo.interestcalc.service;

import com.carddemo.interestcalc.domain.AccountRecord;
import com.carddemo.interestcalc.domain.CardXrefRecord;
import com.carddemo.interestcalc.domain.DisclosureGroupRecord;
import com.carddemo.interestcalc.domain.TranCatBalRecord;
import com.carddemo.interestcalc.domain.TransactionRecord;
import com.carddemo.interestcalc.repository.InMemoryAccountRepository;
import com.carddemo.interestcalc.repository.InMemoryCardXrefRepository;
import com.carddemo.interestcalc.repository.InMemoryDisclosureGroupRepository;
import com.carddemo.interestcalc.repository.InMemoryTransactionWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end equivalence tests for the CBACT04C main loop: control break on account id,
 * disclosure group DEFAULT fallback, account REWRITE semantics, and transaction output.
 */
class InterestCalculationServiceTest {

    private static final String PARM_DATE = "2022-07-19";

    private InMemoryAccountRepository accounts;
    private InMemoryCardXrefRepository xrefs;
    private InMemoryDisclosureGroupRepository discGroups;
    private InMemoryTransactionWriter transactions;
    private InterestCalculationService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryAccountRepository();
        xrefs = new InMemoryCardXrefRepository();
        discGroups = new InMemoryDisclosureGroupRepository();
        transactions = new InMemoryTransactionWriter();
        Clock fixedClock = Clock.fixed(Instant.parse("2022-07-19T23:12:31.420Z"), ZoneOffset.UTC);
        service = new InterestCalculationService(accounts, xrefs, discGroups, transactions, fixedClock);
    }

    private AccountRecord account(long id, String balance, String groupId) {
        return new AccountRecord(id, "Y", new BigDecimal(balance),
                new BigDecimal("20200.00"), new BigDecimal("10200.00"),
                LocalDate.parse("2014-11-20"), LocalDate.parse("2025-05-20"), LocalDate.parse("2025-05-20"),
                new BigDecimal("100.00"), new BigDecimal("250.00"),
                "A000000000", groupId);
    }

    @Test
    void singleAccountSingleCategoryComputesInterestAndUpdatesBalance() {
        accounts.load(account(1L, "1000.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 1, new BigDecimal("15.00")));

        long processed = service.run(List.of(
                new TranCatBalRecord(1L, "01", 1, new BigDecimal("1000.00"))), PARM_DATE);

        assertThat(processed).isEqualTo(1);
        // 1000.00 * 15.00 / 1200 = 12.50
        AccountRecord updated = accounts.findById(1L).orElseThrow();
        assertThat(updated.getCurrentBalance()).isEqualByComparingTo("1012.50");
        // 1050-UPDATE-ACCOUNT zeroes cycle credit and debit
        assertThat(updated.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(updated.getCurrentCycleDebit()).isEqualByComparingTo("0.00");

        assertThat(transactions.getTransactions()).hasSize(1);
        TransactionRecord tx = transactions.getTransactions().get(0);
        assertThat(tx.transactionId()).isEqualTo(PARM_DATE + "000001");
        assertThat(tx.typeCode()).isEqualTo("01");
        assertThat(tx.categoryCode()).isEqualTo("0005");
        assertThat(tx.source()).isEqualTo("System");
        assertThat(tx.description()).isEqualTo("Int. for a/c 00000000001");
        assertThat(tx.amount()).isEqualByComparingTo("12.50");
        assertThat(tx.cardNumber()).isEqualTo("4111111111111111");
        assertThat(tx.originTimestamp()).isEqualTo("2022-07-19-23.12.31.420000");
        assertThat(tx.processTimestamp()).isEqualTo(tx.originTimestamp());
    }

    @Test
    void multipleCategoriesForSameAccountAccumulateIntoTotalInterest() {
        accounts.load(account(1L, "100.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 1, new BigDecimal("11.99")));
        discGroups.load(new DisclosureGroupRecord("01", "01", 2, new BigDecimal("15.00")));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 2, new BigDecimal("15.00")));

        service.run(List.of(
                new TranCatBalRecord(1L, "01", 1, new BigDecimal("100.00")),   // 0.99 (truncated)
                new TranCatBalRecord(1L, "01", 2, new BigDecimal("1000.00"))), // 12.50
                PARM_DATE);

        // Total interest = 0.99 + 12.50 = 13.49 added once at control break / EOF
        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo("113.49");
        assertThat(transactions.getTransactions()).hasSize(2);
        assertThat(transactions.getTransactions().get(1).transactionId()).isEqualTo(PARM_DATE + "000002");
    }

    @Test
    void controlBreakUpdatesEachAccountSeparatelyAndSuffixIsGlobal() {
        accounts.load(account(1L, "1000.00", "GOLD"));
        accounts.load(account(2L, "2000.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        xrefs.load(new CardXrefRecord("4222222222222222", 2L, 2L));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 1, new BigDecimal("12.00")));

        service.run(List.of(
                new TranCatBalRecord(1L, "01", 1, new BigDecimal("1000.00")),  // 10.00
                new TranCatBalRecord(2L, "01", 1, new BigDecimal("2000.00"))), // 20.00
                PARM_DATE);

        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("1010.00");
        assertThat(accounts.findById(2L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("2020.00");
        // WS-TRANID-SUFFIX is never reset between accounts
        assertThat(transactions.getTransactions().get(0).transactionId()).isEqualTo(PARM_DATE + "000001");
        assertThat(transactions.getTransactions().get(1).transactionId()).isEqualTo(PARM_DATE + "000002");
        assertThat(transactions.getTransactions().get(1).cardNumber()).isEqualTo("4222222222222222");
    }

    @Test
    void missingDisclosureGroupFallsBackToDefaultGroup() {
        // Seed accounts have blank ACCT-GROUP-ID, so the keyed read misses (status '23')
        // and 1200-A-GET-DEFAULT-INT-RATE retries with group 'DEFAULT'.
        accounts.load(account(1L, "1000.00", ""));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        discGroups.load(new DisclosureGroupRecord("DEFAULT", "01", 1, new BigDecimal("12.00")));

        service.run(List.of(new TranCatBalRecord(1L, "01", 1, new BigDecimal("1000.00"))), PARM_DATE);

        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("1010.00");
    }

    @Test
    void zeroInterestRateWritesNoTransactionAndLeavesBalanceUnchanged() {
        accounts.load(account(1L, "1000.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 1, new BigDecimal("0.00")));

        service.run(List.of(new TranCatBalRecord(1L, "01", 1, new BigDecimal("1000.00"))), PARM_DATE);

        // DIS-INT-RATE = 0 skips 1300-COMPUTE-INTEREST, but the account is still
        // rewritten at EOF with WS-TOTAL-INT = 0 and cycle amounts reset.
        AccountRecord updated = accounts.findById(1L).orElseThrow();
        assertThat(updated.getCurrentBalance()).isEqualByComparingTo("1000.00");
        assertThat(updated.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(updated.getCurrentCycleDebit()).isEqualByComparingTo("0.00");
        assertThat(transactions.getTransactions()).isEmpty();
    }

    @Test
    void emptyInputProcessesNothingAndUpdatesNoAccounts() {
        accounts.load(account(1L, "1000.00", "GOLD"));

        long processed = service.run(List.of(), PARM_DATE);

        assertThat(processed).isZero();
        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("1000.00");
        assertThat(transactions.getTransactions()).isEmpty();
    }

    @Test
    void negativeBalanceProducesNegativeInterestTruncatedTowardZero() {
        accounts.load(account(1L, "1000.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));
        discGroups.load(new DisclosureGroupRecord("GOLD", "01", 1, new BigDecimal("11.99")));

        service.run(List.of(new TranCatBalRecord(1L, "01", 1, new BigDecimal("-100.00"))), PARM_DATE);

        // -100.00 * 11.99 / 1200 = -0.999166... -> -0.99 (truncation toward zero, not -1.00)
        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("999.01");
        assertThat(transactions.getTransactions().get(0).amount()).isEqualByComparingTo("-0.99");
    }

    @Test
    void missingAccountAbendsLikeCobol() {
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));

        assertThatThrownBy(() -> service.run(
                List.of(new TranCatBalRecord(1L, "01", 1, new BigDecimal("100.00"))), PARM_DATE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACCOUNT NOT FOUND");
    }

    @Test
    void missingDefaultDisclosureGroupAbendsLikeCobol() {
        accounts.load(account(1L, "1000.00", "GOLD"));
        xrefs.load(new CardXrefRecord("4111111111111111", 1L, 1L));

        assertThatThrownBy(() -> service.run(
                List.of(new TranCatBalRecord(1L, "01", 1, new BigDecimal("100.00"))), PARM_DATE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DEFAULT DISCLOSURE GROUP RECORD MISSING");
    }
}
