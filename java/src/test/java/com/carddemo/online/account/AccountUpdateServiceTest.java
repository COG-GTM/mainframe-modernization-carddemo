package com.carddemo.online.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * COBOL program COACTUPC — 1200-EDIT-MAP-INPUTS, 1205-COMPARE-OLD-NEW,
 * 2000-DECIDE-ACTION, 9600-WRITE-PROCESSING and 9700-CHECK-CHANGE-IN-REC.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountUpdateServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerRepository customerRepository;

    private AccountUpdateService service;
    private AccountUpdateState state;
    private CardDemoCommarea commarea;

    @BeforeEach
    void setUp() {
        service = new AccountUpdateService(cardXrefRepository, accountRepository, customerRepository);
        state = new AccountUpdateState();
        commarea = new CardDemoCommarea();
        when(cardXrefRepository.findFirstByAccountId(1L)).thenReturn(Optional.of(
                CardXref.builder().cardNumber("4111111111111111").accountId(1L).customerId(9L).build()));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account()));
        when(customerRepository.findById(9L)).thenReturn(Optional.of(customer()));
    }

    private AccountUpdateResponse fetch() {
        return service.process(AccountUpdateRequest.builder()
                .action(AccountUpdateRequest.ACTION_ENTER)
                .data(AccountUpdateData.builder().accountId("00000000001").build())
                .build(), state, commarea);
    }

    private AccountUpdateResponse send(String action, AccountUpdateData data) {
        return service.process(AccountUpdateRequest.builder().action(action).data(data).build(), state, commarea);
    }

    @Test
    void fetchesDetailsOnFirstEnter() {
        AccountUpdateResponse response = fetch();

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.SHOW_DETAILS);
        assertThat(response.getInfoMessage()).isEqualTo(AccountUpdateService.FOUND_ACCOUNT_DATA);
        assertThat(response.getData().getCreditLimit()).isEqualTo("2020.00");
        assertThat(response.getData().getFicoScore()).isEqualTo("700");
        assertThat(commarea.getCustomerId()).isEqualTo(9L);
    }

    @Test
    void rejectsBlankAccountFilter() {
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                AccountUpdateData.builder().accountId("   ").build());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.DETAILS_NOT_FETCHED);
        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.NO_SEARCH_CRITERIA_RECEIVED);
    }

    @Test
    void reportsAccountMissingFromCrossReference() {
        when(cardXrefRepository.findFirstByAccountId(2L)).thenReturn(Optional.empty());

        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                AccountUpdateData.builder().accountId("00000000002").build());

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.DID_NOT_FIND_ACCT_IN_CARDXREF);
    }

    @Test
    void detectsNoChanges() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER, state.getOldDetails().toBuilder()
                .build());

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.NO_CHANGES_DETECTED);
        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.SHOW_DETAILS);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void validatedChangesWaitForConfirmation() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER, changed());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_OK_NOT_CONFIRMED);
        assertThat(response.getInfoMessage()).isEqualTo(AccountUpdateService.PROMPT_FOR_CONFIRMATION);
        assertThat(response.getErrorMessage()).isNull();
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void rejectsInvalidAccountStatus() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                changed().toBuilder().activeStatus("X").build());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_NOT_OK);
        assertThat(response.getFieldErrors()).containsKey("activeStatus");
        assertThat(response.getFieldErrors().get("activeStatus")).contains("Account Status");
    }

    @Test
    void rejectsInvalidFicoScore() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                changed().toBuilder().ficoScore("100").build());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_NOT_OK);
        assertThat(response.getFieldErrors()).containsKey("ficoScore");
    }

    @Test
    void rejectsNonAlphabeticFirstName() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                changed().toBuilder().firstName("Imm4nuel").build());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_NOT_OK);
        assertThat(response.getFieldErrors()).containsKey("firstName");
    }

    @Test
    void rejectsZipCodeThatDoesNotMatchTheState() {
        fetch();
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_ENTER,
                changed().toBuilder().zipCode("99999").build());

        assertThat(response.getFieldErrors()).containsEntry("zipCode", "Invalid zip code for state");
        assertThat(response.getFieldErrors()).containsEntry("stateCode", "Invalid zip code for state");
    }

    @Test
    void commitsConfirmedChanges() {
        fetch();
        send(AccountUpdateRequest.ACTION_ENTER, changed());
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF5, changed());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_OKAYED_AND_DONE);
        assertThat(response.getInfoMessage()).isEqualTo(AccountUpdateService.CONFIRM_UPDATE_SUCCESS);
        verify(accountRepository).save(any(Account.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void refusesToCommitWhenAnotherUserChangedTheRecord() {
        fetch();
        send(AccountUpdateRequest.ACTION_ENTER, changed());

        Account meanwhile = account();
        meanwhile.setCreditLimit(new BigDecimal("9999.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(meanwhile));

        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF5, changed());

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.DATA_WAS_CHANGED_BEFORE_UPDATE);
        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.SHOW_DETAILS);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void reportsWhenTheAccountRecordCannotBeLocked() {
        fetch();
        send(AccountUpdateRequest.ACTION_ENTER, changed());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF5, changed());

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.COULD_NOT_LOCK_ACCT_FOR_UPDATE);
        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_OKAYED_LOCK_ERROR);
        assertThat(response.getInfoMessage()).isEqualTo(AccountUpdateService.INFORM_FAILURE);
    }

    @Test
    void reportsWhenTheUpdateItselfFails() {
        fetch();
        send(AccountUpdateRequest.ACTION_ENTER, changed());
        when(accountRepository.save(any(Account.class))).thenThrow(new IllegalStateException("db down"));

        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF5, changed());

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.LOCKED_BUT_UPDATE_FAILED);
        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.CHANGES_OKAYED_BUT_FAILED);
    }

    @Test
    void pf12RereadsTheRecordAndDiscardsChanges() {
        fetch();
        send(AccountUpdateRequest.ACTION_ENTER, changed());

        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF12, changed());

        assertThat(response.getChangeAction()).isEqualTo(AccountUpdateState.SHOW_DETAILS);
        assertThat(response.getData().getCreditLimit()).isEqualTo("2020.00");
    }

    @Test
    void pf3ReturnsToTheCallingProgram() {
        commarea.setFromProgram("COMEN01C");
        AccountUpdateResponse response = send(AccountUpdateRequest.ACTION_PF3, null);

        assertThat(response.getErrorMessage()).isEqualTo(AccountUpdateService.EXIT_MESSAGE);
        assertThat(response.getNextProgram()).isEqualTo("COMEN01C");
        assertThat(state.getChangeAction()).isEqualTo(AccountUpdateState.DETAILS_NOT_FETCHED);
    }

    /** The fetched details with one changed field that passes every edit. */
    private AccountUpdateData changed() {
        return state.getOldDetails().toBuilder().creditLimit("3000.00").build();
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
                .phoneNumber2("(212)693-8684")
                .ssn(123456789L)
                .governmentIssuedId("0053581756")
                .dateOfBirth("1961-06-08")
                .eftAccountId("0053581756")
                .primaryCardHolderIndicator("Y")
                .ficoCreditScore(700)
                .build();
    }
}
