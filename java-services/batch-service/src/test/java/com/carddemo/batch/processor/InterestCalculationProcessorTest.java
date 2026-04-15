package com.carddemo.batch.processor;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for InterestCalculationProcessor.
 * Validates the business logic migrated from COBOL program CBACT04C.
 */
@ExtendWith(MockitoExtension.class)
class InterestCalculationProcessorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private InterestCalculationProcessor processor;

    private Account activeAccountWithBalance;
    private Account activeAccountZeroBalance;
    private Account inactiveAccount;

    @BeforeEach
    void setUp() {
        activeAccountWithBalance = Account.builder()
                .acctId(10000000001L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("5000.00"))
                .creditLimit(new BigDecimal("15000.00"))
                .cashCreditLimit(new BigDecimal("5000.00"))
                .openDate("2020-01-15")
                .expirationDate("2027-01-15")
                .reissueDate("2025-01-15")
                .currentCycleCredit(new BigDecimal("500.00"))
                .currentCycleDebit(new BigDecimal("-200.00"))
                .addrZip("60601")
                .groupId("GROUP01")
                .build();

        activeAccountZeroBalance = Account.builder()
                .acctId(10000000004L)
                .activeStatus("Y")
                .currentBalance(BigDecimal.ZERO)
                .creditLimit(new BigDecimal("10000.00"))
                .cashCreditLimit(new BigDecimal("3000.00"))
                .openDate("2018-11-10")
                .expirationDate("2023-11-10")
                .reissueDate("2021-11-10")
                .currentCycleCredit(BigDecimal.ZERO)
                .currentCycleDebit(BigDecimal.ZERO)
                .addrZip("30301")
                .groupId("GROUP01")
                .build();

        inactiveAccount = Account.builder()
                .acctId(10000000004L)
                .activeStatus("N")
                .currentBalance(new BigDecimal("1000.00"))
                .creditLimit(new BigDecimal("10000.00"))
                .cashCreditLimit(new BigDecimal("3000.00"))
                .openDate("2018-11-10")
                .expirationDate("2023-11-10")
                .reissueDate("2021-11-10")
                .currentCycleCredit(BigDecimal.ZERO)
                .currentCycleDebit(BigDecimal.ZERO)
                .addrZip("30301")
                .groupId("GROUP01")
                .build();
    }

    @Test
    @DisplayName("Should calculate monthly interest correctly - mirrors CBACT04C 1300-COMPUTE-INTEREST")
    void shouldCalculateMonthlyInterest() throws Exception {
        Account result = processor.process(activeAccountWithBalance);

        assertThat(result).isNotNull();

        // Expected interest: 5000.00 * 22.99 / 1200 = 95.79 (rounded HALF_UP)
        BigDecimal expectedInterest = new BigDecimal("5000.00")
                .multiply(new BigDecimal("22.99"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        // New balance: 5000.00 + 95.79 = 5095.79
        BigDecimal expectedBalance = new BigDecimal("5000.00").add(expectedInterest);
        assertThat(result.getCurrentBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("Should reset cycle credits and debits - mirrors CBACT04C 1050-UPDATE-ACCOUNT")
    void shouldResetCycleCreditsAndDebits() throws Exception {
        Account result = processor.process(activeAccountWithBalance);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentCycleCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCurrentCycleDebit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should create interest transaction record - mirrors CBACT04C 1300-B-WRITE-TX")
    void shouldCreateInterestTransactionRecord() throws Exception {
        processor.process(activeAccountWithBalance);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTran = transactionCaptor.getValue();

        assertThat(savedTran.getTypeCd()).isEqualTo("01");
        assertThat(savedTran.getCatCd()).isEqualTo(5);
        assertThat(savedTran.getSource()).isEqualTo("System");
        assertThat(savedTran.getDescription()).contains("MONTHLY INTEREST CHARGE");
        assertThat(savedTran.getDescription()).contains("10000000001");
        assertThat(savedTran.getAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should skip inactive accounts")
    void shouldSkipInactiveAccounts() throws Exception {
        Account result = processor.process(inactiveAccount);

        assertThat(result).isNull();
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip accounts with zero balance")
    void shouldSkipZeroBalanceAccounts() throws Exception {
        Account result = processor.process(activeAccountZeroBalance);

        assertThat(result).isNull();
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip accounts with negative balance")
    void shouldSkipNegativeBalanceAccounts() throws Exception {
        activeAccountWithBalance.setCurrentBalance(new BigDecimal("-500.00"));

        Account result = processor.process(activeAccountWithBalance);

        assertThat(result).isNull();
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate interest using default 22.99% annual rate")
    void shouldUseDefaultAnnualRate() {
        BigDecimal balance = new BigDecimal("10000.00");
        BigDecimal annualRate = new BigDecimal("22.99");

        BigDecimal result = processor.calculateMonthlyInterest(balance, annualRate);

        // 10000 * 22.99 / 1200 = 191.58
        assertThat(result).isEqualByComparingTo(new BigDecimal("191.58"));
    }

    @Test
    @DisplayName("Should calculate interest for large balance")
    void shouldCalculateInterestForLargeBalance() {
        BigDecimal balance = new BigDecimal("50000.00");
        BigDecimal annualRate = new BigDecimal("22.99");

        BigDecimal result = processor.calculateMonthlyInterest(balance, annualRate);

        // 50000 * 22.99 / 1200 = 957.92
        assertThat(result).isEqualByComparingTo(new BigDecimal("957.92"));
    }
}
