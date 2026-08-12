package com.carddemo.batch.interest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.DisclosureGroupId;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.model.entity.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;

/**
 * COBOL program: CBACT04C — control break over TCATBALF, DISCGRP fallback and account update.
 */
class InterestCalculationProcessorTest {

    private static final LocalDateTime RUN_TIME = LocalDateTime.of(2022, 7, 18, 0, 0);
    private static final String PARM_DATE = "2022071800";

    private TransactionCategoryBalanceRepository categoryBalanceRepository;
    private AccountRepository accountRepository;
    private CardXrefRepository cardXrefRepository;
    private DisclosureGroupRepository disclosureGroupRepository;
    private TransactionRepository transactionRepository;
    private InterestCalculationProcessor processor;

    private final Map<DisclosureGroupId, DisclosureGroup> disclosureGroups = new HashMap<>();
    private final List<TransactionCategoryBalance> balances = new ArrayList<>();
    private final Map<Long, Account> accounts = new HashMap<>();

    @BeforeEach
    void setUp() {
        categoryBalanceRepository = Mockito.mock(TransactionCategoryBalanceRepository.class);
        accountRepository = Mockito.mock(AccountRepository.class);
        cardXrefRepository = Mockito.mock(CardXrefRepository.class);
        disclosureGroupRepository = Mockito.mock(DisclosureGroupRepository.class);
        transactionRepository = Mockito.mock(TransactionRepository.class);
        processor = new InterestCalculationProcessor(categoryBalanceRepository, accountRepository,
                cardXrefRepository, disclosureGroupRepository, transactionRepository,
                new InterestCalculationService());

        when(categoryBalanceRepository.findAll(any(Sort.class))).thenReturn(balances);
        when(accountRepository.findById(any())).thenAnswer(call ->
                Optional.ofNullable(accounts.get(call.getArgument(0, Long.class))));
        when(cardXrefRepository.findFirstByAccountId(any())).thenAnswer(call -> Optional.of(
                CardXref.builder()
                        .cardNumber("400000000000000" + call.getArgument(0, Long.class))
                        .accountId(call.getArgument(0, Long.class))
                        .customerId(call.getArgument(0, Long.class))
                        .build()));
        when(disclosureGroupRepository.findById(any())).thenAnswer(call ->
                Optional.ofNullable(disclosureGroups.get(call.getArgument(0, DisclosureGroupId.class))));
    }

    @Test
    void postsInterestPerCategoryAndRewritesTheAccountOnTheControlBreak() {
        account(1L, "A000000000", "500.00");
        account(2L, "A000000000", "100.00");
        rate("A000000000", "01", 1, "12.00");
        rate("A000000000", "01", 2, "18.50");
        rate("A000000000", "02", 1, "24.00");
        balance(1L, "01", 1, "1000.00");
        balance(1L, "01", 2, "250.75");
        balance(2L, "02", 1, "100.00");

        InterestCalculationResult result = processor.run(PARM_DATE, RUN_TIME);

        List<Transaction> written = capturedTransactions();
        assertThat(written).extracting(Transaction::getTransactionId)
                .containsExactly("2022071800000001", "2022071800000002", "2022071800000003");
        assertThat(written).extracting(Transaction::getAmount)
                .containsExactly(new BigDecimal("10.00"), new BigDecimal("3.86"), new BigDecimal("2.00"));
        assertThat(written).extracting(Transaction::getCardNumber)
                .containsExactly("4000000000000001", "4000000000000001", "4000000000000002");

        // Account 1 is rewritten when account 2 starts: 500.00 + 10.00 + 3.86.
        assertThat(accounts.get(1L).getCurrentBalance()).isEqualByComparingTo("513.86");
        assertThat(accounts.get(1L).getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(accounts.get(1L).getCurrentCycleDebit()).isEqualByComparingTo("0.00");

        assertThat(result.recordsRead()).isEqualTo(3);
        assertThat(result.transactionsWritten()).isEqualTo(3);
        assertThat(result.accountsUpdated()).isEqualTo(1);
        assertThat(result.totalInterestPosted()).isEqualByComparingTo("13.86");
    }

    @Test
    void neverPostsTheInterestOfTheLastAccountOfTheFile() {
        account(1L, "A000000000", "100.00");
        rate("A000000000", "01", 1, "12.00");
        balance(1L, "01", 1, "1200.00");

        InterestCalculationResult result = processor.run(PARM_DATE, RUN_TIME);

        // The interest transaction is written, but 1050-UPDATE-ACCOUNT never runs for it.
        assertThat(capturedTransactions()).extracting(Transaction::getAmount)
                .containsExactly(new BigDecimal("12.00"));
        assertThat(accounts.get(1L).getCurrentBalance()).isEqualByComparingTo("100.00");
        assertThat(result.accountsUpdated()).isZero();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void fallsBackToTheDefaultDisclosureGroupWhenTheAccountGroupIsMissing() {
        account(1L, "", "0.00");
        account(2L, "A000000000", "0.00");
        rate("DEFAULT", "05", 3, "24.00");
        balance(1L, "05", 3, "100.00");
        balance(2L, "05", 3, "100.00");
        rate("A000000000", "05", 3, "12.00");

        processor.run(PARM_DATE, RUN_TIME);

        assertThat(capturedTransactions()).extracting(Transaction::getAmount)
                .containsExactly(new BigDecimal("2.00"), new BigDecimal("1.00"));
        assertThat(accounts.get(1L).getCurrentBalance()).isEqualByComparingTo("2.00");
    }

    @Test
    void skipsCategoriesWhoseDisclosureRateIsZero() {
        account(1L, "A000000000", "0.00");
        account(2L, "A000000000", "0.00");
        rate("A000000000", "01", 1, "0.00");
        rate("A000000000", "01", 2, "12.00");
        balance(1L, "01", 1, "5000.00");
        balance(1L, "01", 2, "1200.00");
        balance(2L, "01", 2, "0.00");

        InterestCalculationResult result = processor.run(PARM_DATE, RUN_TIME);

        assertThat(capturedTransactions()).extracting(Transaction::getAmount)
                .containsExactly(new BigDecimal("12.00"), new BigDecimal("0.00"));
        assertThat(result.recordsRead()).isEqualTo(3);
        assertThat(accounts.get(1L).getCurrentBalance()).isEqualByComparingTo("12.00");
    }

    @Test
    void abendsWhenEvenTheDefaultDisclosureGroupIsMissing() {
        account(1L, "A000000000", "0.00");
        balance(1L, "09", 9, "100.00");

        assertThatThrownBy(() -> processor.run(PARM_DATE, RUN_TIME))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ERROR READING DEFAULT DISCLOSURE GROUP");
    }

    private List<Transaction> capturedTransactions() {
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, Mockito.atLeast(0)).save(captor.capture());
        return captor.getAllValues();
    }

    private void account(long accountId, String groupId, String balance) {
        accounts.put(accountId, Account.builder()
                .accountId(accountId)
                .groupId(groupId)
                .currentBalance(new BigDecimal(balance))
                .currentCycleCredit(new BigDecimal("25.00"))
                .currentCycleDebit(new BigDecimal("75.00"))
                .build());
    }

    private void balance(long accountId, String typeCode, int categoryCode, String amount) {
        balances.add(TransactionCategoryBalance.builder()
                .id(TransactionCategoryBalanceId.builder()
                        .accountId(accountId)
                        .typeCode(typeCode)
                        .categoryCode(categoryCode)
                        .build())
                .balance(new BigDecimal(amount))
                .build());
    }

    private void rate(String groupId, String typeCode, int categoryCode, String interestRate) {
        DisclosureGroupId id = DisclosureGroupId.builder()
                .accountGroupId(groupId)
                .transactionTypeCode(typeCode)
                .transactionCategoryCode(categoryCode)
                .build();
        disclosureGroups.put(id, DisclosureGroup.builder()
                .id(id)
                .interestRate(new BigDecimal(interestRate))
                .build());
    }
}
