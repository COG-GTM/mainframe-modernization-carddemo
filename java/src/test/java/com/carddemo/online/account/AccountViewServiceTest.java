package com.carddemo.online.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** COBOL program COACTVWC — 2210-EDIT-ACCOUNT and 9000-READ-ACCT. */
@ExtendWith(MockitoExtension.class)
class AccountViewServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerRepository customerRepository;

    private AccountViewService service;

    @BeforeEach
    void setUp() {
        service = new AccountViewService(cardXrefRepository, accountRepository, customerRepository);
    }

    @Test
    void displaysAccountAndCustomerDetails() {
        when(cardXrefRepository.findFirstByAccountId(1L)).thenReturn(Optional.of(
                CardXref.builder().cardNumber("4111111111111111").accountId(1L).customerId(9L).build()));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account()));
        when(customerRepository.findById(9L)).thenReturn(Optional.of(customer()));

        CardDemoCommarea commarea = new CardDemoCommarea();
        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("00000000001").build(), commarea);

        assertThat(response.isAccountFound()).isTrue();
        assertThat(response.isCustomerFound()).isTrue();
        assertThat(response.getInfoMessage()).isEqualTo(AccountViewService.INFORM_OUTPUT);
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getCurrentBalance()).isEqualByComparingTo("194.00");
        assertThat(response.getCustomerSsn()).isEqualTo("123-45-6789");
        assertThat(response.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(commarea.getAccountId()).isEqualTo(1L);
        assertThat(commarea.getLastMap()).isEqualTo("CACTVWA");
    }

    @Test
    void asteriskIsTreatedAsNoSearchKey() {
        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("*").build(), new CardDemoCommarea());

        assertThat(response.getErrorMessage()).isEqualTo(AccountViewService.NO_SEARCH_CRITERIA);
        assertThat(response.getFieldErrors()).containsEntry("accountId", AccountViewService.PROMPT_FOR_ACCT);
        assertThat(response.isAccountFound()).isFalse();
    }

    @Test
    void rejectsNonNumericAccountFilter() {
        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("0000000000X").build(), new CardDemoCommarea());

        assertThat(response.getErrorMessage()).isEqualTo(AccountViewService.ACCT_FILTER_NOT_VALID);
        assertThat(response.getFieldErrors()).containsEntry("accountId", AccountViewService.ACCT_FILTER_NOT_VALID);
    }

    @Test
    void rejectsZeroAccountFilter() {
        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("00000000000").build(), new CardDemoCommarea());

        assertThat(response.getErrorMessage()).isEqualTo(AccountViewService.ACCT_FILTER_NOT_VALID);
    }

    @Test
    void reportsMissingCrossReferenceWithRespCodes() {
        when(cardXrefRepository.findFirstByAccountId(7L)).thenReturn(Optional.empty());

        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("00000000007").build(), new CardDemoCommarea());

        assertThat(response.getErrorMessage())
                .isEqualTo("Account:00000000007 not found in Cross ref file.  Resp:000000013 Reas:000000000");
    }

    @Test
    void reportsMissingAccountMasterRecord() {
        when(cardXrefRepository.findFirstByAccountId(7L)).thenReturn(Optional.of(
                CardXref.builder().cardNumber("4111111111111111").accountId(7L).customerId(9L).build()));
        when(accountRepository.findById(7L)).thenReturn(Optional.empty());

        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("00000000007").build(), new CardDemoCommarea());

        assertThat(response.getErrorMessage())
                .isEqualTo("Account:00000000007 not found in Acct Master file.Resp:000000013 Reas:000000000");
    }

    @Test
    void reportsMissingCustomerButStillShowsAccount() {
        when(cardXrefRepository.findFirstByAccountId(1L)).thenReturn(Optional.of(
                CardXref.builder().cardNumber("4111111111111111").accountId(1L).customerId(9L).build()));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account()));
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());

        AccountViewResponse response = service.view(
                AccountViewRequest.builder().accountId("00000000001").build(), new CardDemoCommarea());

        assertThat(response.isAccountFound()).isTrue();
        assertThat(response.isCustomerFound()).isFalse();
        assertThat(response.getErrorMessage())
                .isEqualTo("CustId:000000009 not found in customer master.Resp: 000000013 REAS:000000000");
    }

    private static Account account() {
        return Account.builder()
                .accountId(1L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("194.00"))
                .creditLimit(new BigDecimal("2020.00"))
                .cashCreditLimit(new BigDecimal("1020.00"))
                .currentCycleCredit(BigDecimal.ZERO)
                .currentCycleDebit(BigDecimal.ZERO)
                .openDate("2014-11-20")
                .expirationDate("2025-05-20")
                .reissueDate("2025-05-20")
                .groupId("")
                .build();
    }

    private static Customer customer() {
        return Customer.builder()
                .customerId(9L)
                .firstName("Immanuel")
                .middleName("Madeline")
                .lastName("Kessler")
                .addressLine1("618 Deshaun Route")
                .addressLine2("Apt. 802")
                .addressLine3("New York")
                .stateCode("NY")
                .countryCode("USA")
                .zipCode("10001")
                .phoneNumber1("(908)119-8310")
                .phoneNumber2("(373)693-8684")
                .ssn(123456789L)
                .governmentIssuedId("0053581756")
                .dateOfBirth("1961-06-08")
                .eftAccountId("0053581756")
                .primaryCardHolderIndicator("Y")
                .ficoCreditScore(274)
                .build();
    }
}
