package com.carddemo.fixture;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWidthFixtureLoaderTest {

    private final FixedWidthFixtureLoader loader = new FixedWidthFixtureLoader();

    @Test
    void loadsEverySampleFile() {
        CardDemoDataSet dataSet = loader.loadAll();
        assertThat(dataSet.accounts()).hasSize(50);
        assertThat(dataSet.cards()).hasSize(50);
        assertThat(dataSet.cardXrefs()).hasSize(50);
        assertThat(dataSet.customers()).hasSize(50);
        assertThat(dataSet.dailyTransactions()).hasSize(300);
        assertThat(dataSet.disclosureGroups()).hasSize(51);
        assertThat(dataSet.transactionCategoryBalances()).hasSize(50);
        assertThat(dataSet.transactionCategories()).hasSize(18);
        assertThat(dataSet.transactionTypes()).hasSize(7);
        assertThat(dataSet.users()).hasSize(10);
    }

    @Test
    void parsesAccountFieldsAndDecodesBalances() {
        Account account = loader.loadAccounts().get(0);
        assertThat(account.getAccountId()).isEqualTo("00000000001");
        assertThat(account.getActiveStatus()).isEqualTo('Y');
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("194.00");
        assertThat(account.getCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(account.getCashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(account.getOpenDate()).isEqualTo("2014-11-20");
        // The sample file carries "A000000000" in the ACCT-ADDR-ZIP position and leaves
        // ACCT-GROUP-ID blank, so interest calculation falls back to the DEFAULT disclosure group.
        assertThat(account.getAddressZip()).isEqualTo("A000000000");
        assertThat(account.getGroupId()).isEmpty();
    }

    @Test
    void parsesCardAndCrossReferenceRecords() {
        Card card = loader.loadCards().get(0);
        assertThat(card.getCardNumber()).isEqualTo("0500024453765740");
        assertThat(card.getAccountId()).isEqualTo("00000000050");
        assertThat(card.getCvvCode()).isEqualTo(747);
        assertThat(card.getEmbossedName()).isEqualTo("Aniya Von");
        assertThat(card.isActive()).isTrue();

        CardXref xref = loader.loadCardXrefs().get(0);
        assertThat(xref.getCardNumber()).isEqualTo("0500024453765740");
        assertThat(xref.getCustomerId()).isEqualTo("000000050");
        assertThat(xref.getAccountId()).isEqualTo("00000000050");
    }

    @Test
    void parsesCustomerRecords() {
        Customer customer = loader.loadCustomers().get(0);
        assertThat(customer.getCustomerId()).isEqualTo("000000001");
        assertThat(customer.getFirstName()).isEqualTo("Immanuel");
        assertThat(customer.getLastName()).isEqualTo("Kessler");
        assertThat(customer.getStateCode()).isEqualTo("NC");
        assertThat(customer.getCountryCode()).isEqualTo("USA");
        assertThat(customer.getFicoCreditScore()).isEqualTo(274);
    }

    @Test
    void parsesDailyTransactionAmounts() {
        Transaction transaction = loader.loadDailyTransactions().get(0);
        assertThat(transaction.getTransactionId()).isEqualTo("0000000000683580");
        assertThat(transaction.getTypeCode()).isEqualTo("01");
        assertThat(transaction.getCategoryCode()).isEqualTo(1);
        assertThat(transaction.getSource()).isEqualTo("POS TERM");
        assertThat(transaction.getAmount()).isEqualByComparingTo("504.77");
        assertThat(transaction.getCardNumber()).isEqualTo("4859452612877065");
    }

    @Test
    void parsesDisclosureGroupInterestRates() {
        DisclosureGroup group = loader.loadDisclosureGroups().get(0);
        assertThat(group.accountGroupId()).isEqualTo("A000000000");
        assertThat(group.typeCode()).isEqualTo("01");
        assertThat(group.categoryCode()).isEqualTo(1);
        assertThat(group.interestRate()).isEqualByComparingTo("15.00");
    }

    @Test
    void generatesMockUsersIncludingTheReadmeSeedLogins() {
        assertThat(loader.loadUsers())
                .anySatisfy(user -> {
                    assertThat(user.getUserId()).isEqualTo("ADMIN001");
                    assertThat(user.getPassword()).isEqualTo("PASSWORD");
                    assertThat(user.isAdmin()).isTrue();
                })
                .anySatisfy(user -> {
                    assertThat(user.getUserId()).isEqualTo("USER0001");
                    assertThat(user.getUserType()).isEqualTo(User.TYPE_REGULAR);
                });
    }

    @Test
    void roundTripsRecordsBackToTheirFixedWidthForm() {
        loader.load("acctdata.txt", Account.RECORD_LENGTH, record -> {
            assertThat(Account.parse(record).format()).isEqualTo(record);
            return record;
        });
        loader.load("carddata.txt", Card.RECORD_LENGTH, record -> {
            assertThat(Card.parse(record).format()).isEqualTo(record);
            return record;
        });
        loader.load("custdata.txt", Customer.RECORD_LENGTH, record -> {
            assertThat(Customer.parse(record).format()).isEqualTo(record);
            return record;
        });
        loader.load("dailytran.txt", Transaction.RECORD_LENGTH, record -> {
            assertThat(Transaction.parse(record).format()).isEqualTo(record);
            return record;
        });
    }
}
