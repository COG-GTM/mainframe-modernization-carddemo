package com.carddemo.account.service;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.model.Account;
import com.carddemo.account.model.CardXref;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private AccountService accountService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .accountId(10000000001L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("1500.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("2020-03-15")
                .expirationDate("2026-03-15")
                .reissueDate("2024-03-15")
                .currentCycleCredit(new BigDecimal("200.00"))
                .currentCycleDebit(new BigDecimal("150.00"))
                .addressZip("60601")
                .groupId("GRP001")
                .build();
    }

    @Test
    void getAccountById_existingAccount_returnsDto() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(sampleAccount));

        AccountDto result = accountService.getAccountById(10000000001L);

        assertThat(result.getAccountId()).isEqualTo(10000000001L);
        assertThat(result.getActiveStatus()).isEqualTo("Y");
        assertThat(result.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getCreditLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getOpenDate()).isEqualTo("2020-03-15");
        assertThat(result.getGroupId()).isEqualTo("GRP001");
    }

    @Test
    void getAccountById_notFound_throwsResourceNotFoundException() {
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99999999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account ID NOT found");
    }

    @Test
    void listAccounts_returnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(List.of(sampleAccount), pageable, 1);
        when(accountRepository.findAll(pageable)).thenReturn(accountPage);

        Page<AccountDto> result = accountService.listAccounts(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAccountId()).isEqualTo(10000000001L);
    }

    @Test
    void updateAccount_validRequest_updatesAndReturnsDto() {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("N")
                .creditLimit(new BigDecimal("8000.00"))
                .cashCreditLimit(new BigDecimal("2500.00"))
                .openDate("2020-03-15")
                .expirationDate("2028-03-15")
                .reissueDate("2026-03-15")
                .groupId("GRP002")
                .build();

        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = accountService.updateAccount(10000000001L, request);

        assertThat(result.getActiveStatus()).isEqualTo("N");
        assertThat(result.getCreditLimit()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(result.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(result.getExpirationDate()).isEqualTo("2028-03-15");
        assertThat(result.getGroupId()).isEqualTo("GRP002");
    }

    @Test
    void updateAccount_notFound_throwsResourceNotFoundException() {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("Y")
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("2020-03-15")
                .expirationDate("2026-03-15")
                .build();

        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(99999999999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account ID NOT found");
    }

    @Test
    void getAccountByCardNumber_existingCard_returnsAccountDto() {
        CardXref xref = CardXref.builder()
                .cardNumber("4111111111111111")
                .customerId(100000001L)
                .accountId(10000000001L)
                .build();

        when(cardXrefRepository.findByCardNumber("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(sampleAccount));

        AccountDto result = accountService.getAccountByCardNumber("4111111111111111");

        assertThat(result.getAccountId()).isEqualTo(10000000001L);
        assertThat(result.getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void getAccountByCardNumber_cardNotFound_throwsResourceNotFoundException() {
        when(cardXrefRepository.findByCardNumber("9999999999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountByCardNumber("9999999999999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Did not find this account in account card xref file");
    }
}
