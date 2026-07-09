package com.carddemo.service.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.Customer;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.web.account.dto.AccountUpdateRequest;
import com.carddemo.web.account.dto.AccountUpdateResponse;
import com.carddemo.web.account.dto.AccountViewResponse;

/**
 * Service-level tests for the ported {@code COACTVWC}/{@code COACTUPC} logic, driven against
 * the real seed data (enabled via {@code carddemo.seed.enabled=true}).
 *
 * <p>Fixture: account {@code 00000000050} resolves through {@code cardxref.txt} to customer
 * {@code 000000005} (Treva Manley Schowalter, state MI, FICO 529). The class is
 * {@code @Transactional} so update mutations roll back after each test.</p>
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
class AccountServiceTest {

    private static final String ACCT_ID = "00000000050";
    private static final String CUST_ID = "000000050";

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void viewReturnsAccountAndAssociatedCustomer() {
        Customer customer = customerRepository.findById(CUST_ID).orElseThrow();
        AccountViewResponse response = accountService.view(ACCT_ID);

        assertThat(response.acctId()).isEqualTo(ACCT_ID);
        assertThat(response.custId()).isEqualTo(CUST_ID);
        assertThat(response.lastName()).isEqualTo(customer.getCustLastName().trim());
        assertThat(response.ssn()).matches("\\d{3}-\\d{2}-\\d{4}");
        assertThat(response.ficoScore()).isEqualTo(customer.getCustFicoCreditScore());
        assertThat(response.creditLimit()).isNotNull();
    }

    @Test
    void viewAcceptsUnpaddedAccountId() {
        AccountViewResponse response = accountService.view("50");
        assertThat(response.acctId()).isEqualTo(ACCT_ID);
    }

    @Test
    void viewOfUnknownAccountReportsCardXrefMiss() {
        assertThatThrownBy(() -> accountService.view("00000099999"))
            .isInstanceOf(AccountNotFoundException.class)
            .hasMessage("Did not find this account in account card xref file");
    }

    @Test
    void viewOfZeroAccountIdIsRejected() {
        assertThatThrownBy(() -> accountService.view("00000000000"))
            .isInstanceOf(AccountInputException.class)
            .hasMessage("Account Filter must  be a non-zero 11 digit number");
    }

    @Test
    void validUpdateCommitsChanges() {
        AccountUpdateResponse response = accountService.update(ACCT_ID, validRequest().ficoScore("600").build());

        assertThat(response.message()).isEqualTo("Changes committed to database");
        assertThat(response.account().ficoScore()).isEqualTo(600);
        assertThat(customerRepository.findById(CUST_ID).orElseThrow().getCustFicoCreditScore())
            .isEqualTo(600);
    }

    @Test
    void invalidFicoUpdateIsRejectedWithCobolMessage() {
        assertThatThrownBy(() -> accountService.update(ACCT_ID, validRequest().ficoScore("999").build()))
            .isInstanceOf(AccountInputException.class)
            .hasMessage("FICO Score: should be between 300 and 850");
    }

    @Test
    void invalidStateZipComboIsRejected() {
        AccountUpdateRequest request = validRequest().stateCode("MI").zipCode("90001").build();
        assertThatThrownBy(() -> accountService.update(ACCT_ID, request))
            .isInstanceOf(AccountInputException.class)
            .hasMessage("Invalid zip code for state");
    }

    @Test
    void resubmittingFetchedValuesReportsNoChange() {
        AccountViewResponse current = accountService.view(ACCT_ID);
        AccountUpdateResponse response = accountService.update(ACCT_ID, fromView(current));
        assertThat(response.message()).isEqualTo("No change detected with respect to values fetched.");
    }

    // ---- fixtures --------------------------------------------------------------------

    /** A fully valid update payload (state/zip MI/48226 is a valid combo, area code 978 valid). */
    private static Builder validRequest() {
        return new Builder()
            .acctActiveStatus("Y")
            .open("2011", "04", "22")
            .creditLimit("5000.00")
            .expiry("2025", "03", "09")
            .cashCreditLimit("1000.00")
            .reissue("2024", "03", "09")
            .currentBalance("100.00")
            .currentCycleCredit("0.00")
            .currentCycleDebit("0.00")
            .accountGroupId("A0001")
            .ssn("611", "26", "4288")
            .dob("1971", "09", "29")
            .ficoScore("600")
            .firstName("Treva").middleName("Manley").lastName("Schowalter")
            .addressLine1("5653 Legros Plaza").addressLine2("Apt 968").city("Alvinaport")
            .stateCode("MI").zipCode("48226").countryCode("USA")
            .phone1("978", "775", "4633").phone2("", "", "")
            .governmentIssuedId("0006365573").eftAccountId("0000000001")
            .primaryCardHolderIndicator("Y");
    }

    /** Rebuild an update payload from a fetched view so every value matches what is on file. */
    private static AccountUpdateRequest fromView(AccountViewResponse v) {
        String[] open = v.openDate().split("-");
        String[] exp = v.expirationDate().split("-");
        String[] ris = v.reissueDate().split("-");
        String[] dob = v.dateOfBirth().split("-");
        String ssn = v.ssn().replace("-", "");
        String[] p1 = splitPhone(v.phoneNum1());
        String[] p2 = splitPhone(v.phoneNum2());
        return new Builder()
            .acctActiveStatus(v.acctActiveStatus())
            .open(open[0], open[1], open[2])
            .creditLimit(v.creditLimit().toPlainString())
            .expiry(exp[0], exp[1], exp[2])
            .cashCreditLimit(v.cashCreditLimit().toPlainString())
            .reissue(ris[0], ris[1], ris[2])
            .currentBalance(v.currentBalance().toPlainString())
            .currentCycleCredit(v.currentCycleCredit().toPlainString())
            .currentCycleDebit(v.currentCycleDebit().toPlainString())
            .accountGroupId(v.accountGroupId())
            .ssn(ssn.substring(0, 3), ssn.substring(3, 5), ssn.substring(5))
            .dob(dob[0], dob[1], dob[2])
            .ficoScore(String.valueOf(v.ficoScore()))
            .firstName(v.firstName()).middleName(v.middleName()).lastName(v.lastName())
            .addressLine1(v.addressLine1()).addressLine2(v.addressLine2()).city(v.city())
            .stateCode(v.stateCode()).zipCode(v.zipCode()).countryCode(v.countryCode())
            .phone1(p1[0], p1[1], p1[2]).phone2(p2[0], p2[1], p2[2])
            .governmentIssuedId(v.governmentIssuedId()).eftAccountId(v.eftAccountId())
            .primaryCardHolderIndicator(v.primaryCardHolderIndicator())
            .build();
    }

    private static String[] splitPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return new String[] {"", "", ""};
        }
        // stored form: (999)999-9999
        String area = phone.substring(1, 4);
        String prefix = phone.substring(5, 8);
        String line = phone.substring(9);
        return new String[] {area, prefix, line};
    }

    /** Small builder so each test tweaks only the field(s) under test. */
    private static final class Builder {
        private String acctActiveStatus;
        private String openYear;
        private String openMonth;
        private String openDay;
        private String creditLimit;
        private String expiryYear;
        private String expiryMonth;
        private String expiryDay;
        private String cashCreditLimit;
        private String reissueYear;
        private String reissueMonth;
        private String reissueDay;
        private String currentBalance;
        private String currentCycleCredit;
        private String currentCycleDebit;
        private String accountGroupId;
        private String ssnPart1;
        private String ssnPart2;
        private String ssnPart3;
        private String dobYear;
        private String dobMonth;
        private String dobDay;
        private String ficoScore;
        private String firstName;
        private String middleName;
        private String lastName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateCode;
        private String zipCode;
        private String countryCode;
        private String phone1Area;
        private String phone1Prefix;
        private String phone1Line;
        private String phone2Area;
        private String phone2Prefix;
        private String phone2Line;
        private String governmentIssuedId;
        private String eftAccountId;
        private String primaryCardHolderIndicator;

        Builder acctActiveStatus(String v) { this.acctActiveStatus = v; return this; }
        Builder open(String y, String m, String d) { this.openYear = y; this.openMonth = m; this.openDay = d; return this; }
        Builder creditLimit(String v) { this.creditLimit = v; return this; }
        Builder expiry(String y, String m, String d) { this.expiryYear = y; this.expiryMonth = m; this.expiryDay = d; return this; }
        Builder cashCreditLimit(String v) { this.cashCreditLimit = v; return this; }
        Builder reissue(String y, String m, String d) { this.reissueYear = y; this.reissueMonth = m; this.reissueDay = d; return this; }
        Builder currentBalance(String v) { this.currentBalance = v; return this; }
        Builder currentCycleCredit(String v) { this.currentCycleCredit = v; return this; }
        Builder currentCycleDebit(String v) { this.currentCycleDebit = v; return this; }
        Builder accountGroupId(String v) { this.accountGroupId = v; return this; }
        Builder ssn(String p1, String p2, String p3) { this.ssnPart1 = p1; this.ssnPart2 = p2; this.ssnPart3 = p3; return this; }
        Builder dob(String y, String m, String d) { this.dobYear = y; this.dobMonth = m; this.dobDay = d; return this; }
        Builder ficoScore(String v) { this.ficoScore = v; return this; }
        Builder firstName(String v) { this.firstName = v; return this; }
        Builder middleName(String v) { this.middleName = v; return this; }
        Builder lastName(String v) { this.lastName = v; return this; }
        Builder addressLine1(String v) { this.addressLine1 = v; return this; }
        Builder addressLine2(String v) { this.addressLine2 = v; return this; }
        Builder city(String v) { this.city = v; return this; }
        Builder stateCode(String v) { this.stateCode = v; return this; }
        Builder zipCode(String v) { this.zipCode = v; return this; }
        Builder countryCode(String v) { this.countryCode = v; return this; }
        Builder phone1(String a, String p, String l) { this.phone1Area = a; this.phone1Prefix = p; this.phone1Line = l; return this; }
        Builder phone2(String a, String p, String l) { this.phone2Area = a; this.phone2Prefix = p; this.phone2Line = l; return this; }
        Builder governmentIssuedId(String v) { this.governmentIssuedId = v; return this; }
        Builder eftAccountId(String v) { this.eftAccountId = v; return this; }
        Builder primaryCardHolderIndicator(String v) { this.primaryCardHolderIndicator = v; return this; }

        AccountUpdateRequest build() {
            return new AccountUpdateRequest(
                acctActiveStatus, openYear, openMonth, openDay, creditLimit,
                expiryYear, expiryMonth, expiryDay, cashCreditLimit,
                reissueYear, reissueMonth, reissueDay, currentBalance,
                currentCycleCredit, currentCycleDebit, accountGroupId,
                ssnPart1, ssnPart2, ssnPart3, dobYear, dobMonth, dobDay, ficoScore,
                firstName, middleName, lastName, addressLine1, addressLine2, city,
                stateCode, zipCode, countryCode,
                phone1Area, phone1Prefix, phone1Line, phone2Area, phone2Prefix, phone2Line,
                governmentIssuedId, eftAccountId, primaryCardHolderIndicator);
        }
    }
}
