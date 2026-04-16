package com.carddemo.accountservice.service;

import com.carddemo.accountservice.dto.AccountUpdateRequest;
import com.carddemo.accountservice.dto.AccountViewResponse;
import com.carddemo.accountservice.entity.Account;
import com.carddemo.accountservice.entity.CardXref;
import com.carddemo.accountservice.entity.Customer;
import com.carddemo.accountservice.exception.AccountNotFoundException;
import com.carddemo.accountservice.exception.AccountValidationException;
import com.carddemo.accountservice.exception.CardXrefNotFoundException;
import com.carddemo.accountservice.exception.CustomerNotFoundException;
import com.carddemo.accountservice.repository.AccountRepository;
import com.carddemo.accountservice.repository.CardXrefRepository;
import com.carddemo.accountservice.repository.CustomerRepository;
import com.carddemo.accountservice.validation.AccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Spy
    private AccountValidator accountValidator;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private Customer testCustomer;
    private CardXref testXref;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(12345678901L);
        testAccount.setAcctActiveStatus("Y");
        testAccount.setAcctCurrBal(new BigDecimal("1000.00"));
        testAccount.setAcctCreditLimit(new BigDecimal("5000.00"));
        testAccount.setAcctCashCreditLimit(new BigDecimal("2000.00"));
        testAccount.setAcctOpenDate("2020-01-15");
        testAccount.setAcctExpirationDate("2025-12-31");
        testAccount.setAcctReissueDate("2023-06-01");
        testAccount.setAcctCurrCycCredit(new BigDecimal("500.00"));
        testAccount.setAcctCurrCycDebit(new BigDecimal("200.00"));
        testAccount.setAcctAddrZip("10001");
        testAccount.setAcctGroupId("GRP001");
        testAccount.setVersion(0L);

        testCustomer = new Customer();
        testCustomer.setCustId(123456789L);
        testCustomer.setCustFirstName("John");
        testCustomer.setCustMiddleName("M");
        testCustomer.setCustLastName("Doe");
        testCustomer.setCustAddrLine1("123 Main St");
        testCustomer.setCustAddrLine2("Apt 4B");
        testCustomer.setCustAddrLine3("");
        testCustomer.setCustAddrStateCd("NY");
        testCustomer.setCustAddrCountryCd("USA");
        testCustomer.setCustAddrZip("10001");
        testCustomer.setCustPhoneNum1("(555)123-4567");
        testCustomer.setCustPhoneNum2("(555)987-6543");
        testCustomer.setCustSsn(123456789L);
        testCustomer.setCustGovtIssuedId("DL12345678");
        testCustomer.setCustDobYyyyMmDd("1985-06-15");
        testCustomer.setCustEftAccountId("EFT001");
        testCustomer.setCustPriCardHolderInd("Y");
        testCustomer.setCustFicoCreditScore(750);
        testCustomer.setVersion(0L);

        testXref = new CardXref();
        testXref.setXrefCardNum("4111111111111111");
        testXref.setXrefCustId(123456789L);
        testXref.setXrefAcctId(12345678901L);
    }

    @Nested
    @DisplayName("View Account")
    class ViewAccount {

        @Test
        @DisplayName("Successfully view account with all details")
        void viewAccountSuccess() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));

            AccountViewResponse response = accountService.viewAccount(12345678901L);

            assertNotNull(response);
            assertEquals(12345678901L, response.account().acctId());
            assertEquals("Y", response.account().activeStatus());
            assertEquals(new BigDecimal("1000.00"), response.account().currentBalance());
            assertEquals("John", response.customer().firstName());
            assertEquals("Doe", response.customer().lastName());
            assertEquals("4111111111111111", response.card().cardNumber());
        }

        @Test
        @DisplayName("View account throws when xref not found")
        void viewAccountXrefNotFound() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.empty());

            CardXrefNotFoundException ex = assertThrows(
                    CardXrefNotFoundException.class,
                    () -> accountService.viewAccount(12345678901L));
            assertEquals("Did not find this account in account card xref file", ex.getMessage());
        }

        @Test
        @DisplayName("View account throws when account not found in master")
        void viewAccountNotFound() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.empty());

            AccountNotFoundException ex = assertThrows(
                    AccountNotFoundException.class,
                    () -> accountService.viewAccount(12345678901L));
            assertEquals("Did not find this account in account master file", ex.getMessage());
        }

        @Test
        @DisplayName("View account throws when customer not found")
        void viewAccountCustomerNotFound() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.empty());

            CustomerNotFoundException ex = assertThrows(
                    CustomerNotFoundException.class,
                    () -> accountService.viewAccount(12345678901L));
            assertEquals("Did not find associated customer in master file", ex.getMessage());
        }

        @Test
        @DisplayName("View account formats SSN as 9 digits with leading zeros")
        void viewAccountSsnFormatting() {
            testCustomer.setCustSsn(1234L);
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));

            AccountViewResponse response = accountService.viewAccount(12345678901L);
            assertEquals("000001234", response.customer().ssn());
        }
    }

    @Nested
    @DisplayName("Update Account")
    class UpdateAccount {

        @Test
        @DisplayName("Successfully update account status")
        void updateAccountStatus() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            AccountUpdateRequest request = new AccountUpdateRequest(
                    "N", null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
            AccountViewResponse response = accountService.updateAccount(12345678901L, request);

            assertNotNull(response);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Update with no changes throws validation error")
        void updateNoChanges() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));

            // Pass existing values - no change
            AccountUpdateRequest request = new AccountUpdateRequest(
                    "Y", null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);

            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> accountService.updateAccount(12345678901L, request));
            assertEquals("No change detected with respect to values fetched.", ex.getMessage());
        }

        @Test
        @DisplayName("Update customer name successfully")
        void updateCustomerName() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

            AccountUpdateRequest request = new AccountUpdateRequest(
                    null, null, null, null, null, null, null, null, null, null,
                    "Jane", null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
            AccountViewResponse response = accountService.updateAccount(12345678901L, request);

            assertNotNull(response);
            verify(customerRepository).save(any(Customer.class));
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("Update with invalid SSN is rejected before persistence")
        void updateInvalidSsn() {
            AccountUpdateRequest request = new AccountUpdateRequest(
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, "000123456", null, null, null, null, null);

            assertThrows(AccountValidationException.class,
                    () -> accountService.updateAccount(12345678901L, request));

            verify(accountRepository, never()).save(any());
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update credit limit successfully")
        void updateCreditLimit() {
            when(cardXrefRepository.findFirstByXrefAcctId(12345678901L))
                    .thenReturn(Optional.of(testXref));
            when(accountRepository.findById(12345678901L))
                    .thenReturn(Optional.of(testAccount));
            when(customerRepository.findById(123456789L))
                    .thenReturn(Optional.of(testCustomer));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            AccountUpdateRequest request = new AccountUpdateRequest(
                    null, null, new BigDecimal("10000.00"), null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);
            AccountViewResponse response = accountService.updateAccount(12345678901L, request);

            assertNotNull(response);
            verify(accountRepository).save(any(Account.class));
        }
    }
}
