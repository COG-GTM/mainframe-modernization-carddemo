package com.carddemo.account.service;

import com.carddemo.account.dto.AccountListResponse;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.dto.AccountUpdateRequest.AccountFields;
import com.carddemo.account.dto.AccountUpdateRequest.CustomerFields;
import com.carddemo.account.entity.AccountEntity;
import com.carddemo.account.entity.CardXrefEntity;
import com.carddemo.account.entity.CustomerEntity;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountService.
 * Validates business logic migration from COACTVWC.cbl and COACTUPC.cbl.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AccountService accountService;

    private AccountEntity sampleAccount;
    private CustomerEntity sampleCustomer;
    private List<CardXrefEntity> sampleXrefs;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountEntity();
        sampleAccount.setAccountId(80001000001L);
        sampleAccount.setActiveStatus("Y");
        sampleAccount.setCurrentBalance(new BigDecimal("1500.00"));
        sampleAccount.setCreditLimit(new BigDecimal("10000.00"));
        sampleAccount.setCashCreditLimit(new BigDecimal("5000.00"));
        sampleAccount.setOpenDate(LocalDate.of(2018, 3, 15));
        sampleAccount.setExpirationDate(LocalDate.of(2028, 3, 15));
        sampleAccount.setReissueDate(LocalDate.of(2023, 3, 15));
        sampleAccount.setCurrentCycleCredit(new BigDecimal("200.00"));
        sampleAccount.setCurrentCycleDebit(new BigDecimal("350.00"));
        sampleAccount.setGroupId("GROUP001");

        sampleCustomer = new CustomerEntity();
        sampleCustomer.setCustId(100000001L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setMiddleName("M");
        sampleCustomer.setLastName("Smith");
        sampleCustomer.setFicoCreditScore(750);

        CardXrefEntity xref1 = new CardXrefEntity();
        xref1.setCardNum("4111111111111111");
        xref1.setCustId(100000001L);
        xref1.setAccountId(80001000001L);

        CardXrefEntity xref2 = new CardXrefEntity();
        xref2.setCardNum("4111111111112222");
        xref2.setCustId(100000001L);
        xref2.setAccountId(80001000001L);

        sampleXrefs = List.of(xref1, xref2);
    }

    @Test
    void getAccount_returnsCombinedData() {
        when(accountRepository.findById(80001000001L)).thenReturn(Optional.of(sampleAccount));
        when(cardXrefRepository.findByAccountId(80001000001L)).thenReturn(sampleXrefs);
        when(customerService.findById(100000001L)).thenReturn(sampleCustomer);
        when(customerService.toDto(sampleCustomer)).thenReturn(
                new AccountResponse.CustomerData(100000001L, "John", "M", "Smith",
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, 750));

        AccountResponse response = accountService.getAccount(80001000001L);

        assertThat(response.account().accountId()).isEqualTo(80001000001L);
        assertThat(response.account().activeStatus()).isEqualTo("Y");
        assertThat(response.account().currentBalance()).isEqualByComparingTo("1500.00");
        assertThat(response.customer().custId()).isEqualTo(100000001L);
        assertThat(response.customer().firstName()).isEqualTo("John");
        assertThat(response.linkedCardNumbers()).hasSize(2);
        assertThat(response.linkedCardNumbers()).containsExactly("4111111111111111", "4111111111112222");
    }

    @Test
    void getAccount_notFound_throwsException() {
        when(accountRepository.findById(99999999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99999999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void updateAccount_updatesAccountAndCustomer() {
        when(accountRepository.findById(80001000001L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any())).thenReturn(sampleAccount);
        when(cardXrefRepository.findByAccountId(80001000001L)).thenReturn(sampleXrefs);
        when(customerService.updateCustomer(eq(100000001L), any())).thenReturn(sampleCustomer);
        when(customerService.toDto(sampleCustomer)).thenReturn(
                new AccountResponse.CustomerData(100000001L, "Jonathan", "M", "Smith",
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, 750));

        AccountUpdateRequest request = new AccountUpdateRequest(
                new AccountFields("N", null, null, null, null, null, null, null, null, "NEWGROUP"),
                new CustomerFields("Jonathan", null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null)
        );

        AccountResponse response = accountService.updateAccount(80001000001L, request);

        assertThat(response.account().activeStatus()).isEqualTo("N");
        assertThat(response.account().groupId()).isEqualTo("NEWGROUP");
        verify(accountRepository).save(any());
        verify(customerService).updateCustomer(eq(100000001L), any());
    }

    @Test
    void updateAccount_onlyAccountFields_doesNotUpdateCustomer() {
        when(accountRepository.findById(80001000001L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any())).thenReturn(sampleAccount);
        when(cardXrefRepository.findByAccountId(80001000001L)).thenReturn(sampleXrefs);
        when(customerService.findById(100000001L)).thenReturn(sampleCustomer);
        when(customerService.toDto(sampleCustomer)).thenReturn(
                new AccountResponse.CustomerData(100000001L, "John", "M", "Smith",
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, 750));

        AccountUpdateRequest request = new AccountUpdateRequest(
                new AccountFields(null, new BigDecimal("2000.00"), null, null, null, null, null, null, null, null),
                null
        );

        AccountResponse response = accountService.updateAccount(80001000001L, request);

        verify(accountRepository).save(any());
        verify(customerService, never()).updateCustomer(any(), any());
    }

    @Test
    void listAccounts_returnsPaginatedResults() {
        PageRequest pageable = PageRequest.of(0, 2, Sort.by("accountId"));
        when(accountRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(sampleAccount), pageable, 5));

        AccountListResponse response = accountService.listAccounts(0, 2);

        assertThat(response.accounts()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
    }
}
