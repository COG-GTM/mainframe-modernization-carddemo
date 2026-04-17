package com.carddemo.account.service;

import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for InterestCalculationService.
 * Validates the COBOL CBACT04C interest calculation logic:
 *   COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 *   ADD WS-MONTHLY-INT TO ACCT-CURR-BAL
 *   MOVE 0 TO ACCT-CURR-CYC-CREDIT
 *   MOVE 0 TO ACCT-CURR-CYC-DEBIT
 */
@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private InterestCalculationService interestCalculationService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId("00000000001");
        testAccount.setAcctActiveStatus("Y");
        testAccount.setAcctCurrBal(new BigDecimal("1200.00"));
        testAccount.setAcctCreditLimit(new BigDecimal("5000.00"));
        testAccount.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        testAccount.setAcctOpenDate("2020-01-15");
        testAccount.setAcctExpirationDate("2025-01-15");
        testAccount.setAcctReissueDate("2023-01-15");
        testAccount.setAcctCurrCycCredit(new BigDecimal("200.00"));
        testAccount.setAcctCurrCycDebit(new BigDecimal("350.00"));
        testAccount.setAcctAddrZip("60601");
        testAccount.setAcctGroupId("GROUP01");
    }

    @Test
    void calculateInterest_withProvidedRate() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal rate = new BigDecimal("12.00");

        AccountResponse response = interestCalculationService.calculateInterest(
                "00000000001", rate);

        // COBOL: (1200.00 * 12.00) / 1200 = 12.00
        BigDecimal expectedInterest = new BigDecimal("1200.00")
                .multiply(rate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        BigDecimal expectedBalance = new BigDecimal("1200.00").add(expectedInterest);

        assertEquals(expectedBalance, response.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycDebit());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void calculateInterest_withDefaultRate() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = interestCalculationService.calculateInterest(
                "00000000001", null);

        // Default rate is 22.99%
        // COBOL: (1200.00 * 22.99) / 1200 = 22.99
        BigDecimal defaultRate = new BigDecimal("22.99");
        BigDecimal expectedInterest = new BigDecimal("1200.00")
                .multiply(defaultRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        BigDecimal expectedBalance = new BigDecimal("1200.00").add(expectedInterest);

        assertEquals(expectedBalance, response.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycDebit());
    }

    @Test
    void calculateInterest_zeroBalance() {
        testAccount.setAcctCurrBal(BigDecimal.ZERO);
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = interestCalculationService.calculateInterest(
                "00000000001", new BigDecimal("15.00"));

        // (0.00 * 15.00) / 1200 = 0.00
        assertEquals(new BigDecimal("0.00"), response.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycDebit());
    }

    @Test
    void calculateInterest_accountNotFound() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> interestCalculationService.calculateInterest(
                        "99999999999", new BigDecimal("12.00")));
    }

    @Test
    void calculateInterest_resettsCycleCounters() {
        // Verify that cycle credit and debit are reset to 0 after interest calc
        // This matches CBACT04C paragraph 1050-UPDATE-ACCOUNT
        testAccount.setAcctCurrCycCredit(new BigDecimal("500.00"));
        testAccount.setAcctCurrCycDebit(new BigDecimal("1200.00"));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = interestCalculationService.calculateInterest(
                "00000000001", new BigDecimal("12.00"));

        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, response.getAcctCurrCycDebit());
    }
}
